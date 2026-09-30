package com.pokemonarena.game;

import com.pokemonarena.board.PokemonInPlay;
import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.ItemCard;
import com.pokemonarena.cards.PokemonCard;
import com.pokemonarena.cards.effects.EffectContext;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.hero.Hero;
import com.pokemonarena.hero.HeroPower;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The rules engine. It owns every mutation of the game state: callers only submit
 * {@link Action}s during the PLAY phase and the engine validates them.
 * <p>
 * A turn always runs DRAW → MANA → PLAY → RESOLVE → END. Combat and card effects resolve
 * immediately when they are played; the RESOLVE phase performs the shared pending work
 * (death cleanup and the win check), which also runs after each individual action so the
 * caller always sees a consistent Board.
 */
public final class Match {

    public static final int MAX_TURNS = 50;
    /** Cards drawn before turn 1 by the Hero playing first. */
    public static final int FIRST_PLAYER_HAND_SIZE = 3;
    /** Cards drawn before turn 1 by the Hero playing second, to compensate the tempo loss. */
    public static final int SECOND_PLAYER_HAND_SIZE = 4;

    private final PlayerState player1;
    private final PlayerState player2;
    private final PlayerState firstPlayer;

    private PlayerState active;
    private PlayerState waiting;
    private int turnNumber;
    private Phase phase = Phase.END;
    private MatchResult result;
    private String currentSource = "";
    private final MatchListener listener;

    public Match(Hero hero1, Deck deck1, Hero hero2, Deck deck2, Random random) {
        this(hero1, deck1, hero2, deck2, random, MatchListener.NONE);
    }

    /** Creates a match observed by {@code listener} from the very first event (starting hands). */
    public Match(Hero hero1, Deck deck1, Hero hero2, Deck deck2, Random random, MatchListener listener) {
        this.listener = Objects.requireNonNull(listener, "listener");
        this.player1 = new PlayerState(hero1, deck1);
        this.player2 = new PlayerState(hero2, deck2);
        boolean playerOneStarts = random.nextBoolean();
        this.active = playerOneStarts ? player1 : player2;
        this.waiting = playerOneStarts ? player2 : player1;
        this.firstPlayer = active;
        emit(new MatchEvent.MatchStarted(hero1.id(), hero2.id(), side(active)));
        drawStartingHand(active, FIRST_PLAYER_HAND_SIZE);
        drawStartingHand(waiting, SECOND_PLAYER_HAND_SIZE);
    }

    private void drawStartingHand(PlayerState player, int cards) {
        for (int i = 0; i < cards; i++) {
            player.drawCard();
        }
        emit(new MatchEvent.StartingHand(side(player), player.hand().stream().map(Card::id).toList()));
    }

    /** Side number of a player: {@code 1} or {@code 2}. */
    public int side(PlayerState player) {
        return player == player1 ? 1 : 2;
    }

    private void emit(MatchEvent event) {
        listener.onEvent(event);
    }

    private void drawFor(PlayerState player) {
        player.drawCard().ifPresentOrElse(
                card -> emit(new MatchEvent.CardDrawn(side(player), card.id())),
                () -> emit(new MatchEvent.DeckEmpty(side(player))));
    }

    private void emitMana(PlayerState player) {
        emit(new MatchEvent.ManaChanged(side(player), player.availableMana(), player.maxMana()));
    }

    /** The Hero that plays turn 1. */
    public PlayerState firstPlayer() {
        return firstPlayer;
    }

    public PlayerState player1() {
        return player1;
    }

    public PlayerState player2() {
        return player2;
    }

    /** The Hero whose turn it is. */
    public PlayerState active() {
        return active;
    }

    /** The Hero waiting for its turn. */
    public PlayerState waiting() {
        return waiting;
    }

    public int turnNumber() {
        return turnNumber;
    }

    public Phase phase() {
        return phase;
    }

    public boolean isOver() {
        return result != null;
    }

    public MatchResult result() {
        if (result == null) {
            throw new IllegalStateException("the match is still running");
        }
        return result;
    }

    // --- turn loop ----------------------------------------------------------

    /** Runs the DRAW and MANA phases, then opens the PLAY phase. */
    public void beginTurn() {
        requireRunning();
        if (phase != Phase.END) {
            throw new IllegalStateException("a turn is already in progress");
        }
        turnNumber++;
        active.beginTurn();
        emit(new MatchEvent.TurnStarted(turnNumber, side(active)));

        enterPhase(Phase.DRAW);
        drawFor(active);

        enterPhase(Phase.MANA);
        active.gainMana();
        emitMana(active);

        enterPhase(Phase.PLAY);
    }

    private void enterPhase(Phase next) {
        phase = next;
        emit(new MatchEvent.PhaseChanged(turnNumber, next));
    }

    /**
     * Every action the active Hero may legally perform right now, always ending with
     * {@link Action.EndTurn}. Decision-makers (Bots, a human UI) choose among these; the
     * engine still validates whatever is submitted to {@link #perform}.
     */
    public List<Action> legalActions() {
        if (isOver() || phase != Phase.PLAY) {
            return List.of();
        }
        List<Action> actions = new ArrayList<>();
        List<Card> hand = active.hand();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (!active.canAfford(card.manaCost())) {
                continue;
            }
            switch (card) {
                case PokemonCard ignored -> {
                    if (!active.board().isFull()) {
                        actions.add(new Action.PlayCard(i));
                    }
                }
                case ItemCard item -> addItemActions(actions, i, item);
            }
        }
        HeroPower power = active.hero().heroPower();
        if (!active.heroPowerUsedThisTurn() && active.canAfford(power.manaCost())) {
            actions.add(new Action.UseHeroPower());
        }
        for (int a = 0; a < active.board().size(); a++) {
            if (!active.board().at(a).canAttack()) {
                continue;
            }
            actions.add(Action.Attack.onHero(a));
            for (int t = 0; t < waiting.board().size(); t++) {
                actions.add(new Action.Attack(a, t));
            }
        }
        actions.add(new Action.EndTurn());
        return List.copyOf(actions);
    }

    private void addItemActions(List<Action> actions, int handIndex, ItemCard item) {
        switch (item.target()) {
            case NONE -> actions.add(new Action.PlayCard(handIndex));
            case OPPOSING_POKEMON -> {
                for (int t = 0; t < waiting.board().size(); t++) {
                    actions.add(new Action.PlayCard(handIndex, t));
                }
            }
            case DISCARDED_POKEMON -> {
                if (!active.hasPokemonInDiscard()) {
                    actions.add(new Action.PlayCard(handIndex));
                } else {
                    for (int d = 0; d < active.discard().size(); d++) {
                        if (active.hasPokemonInDiscardAt(d)) {
                            actions.add(new Action.PlayCard(handIndex, d));
                        }
                    }
                }
            }
        }
    }

    /** Performs one action of the PLAY phase. Illegal actions leave the state untouched. */
    public void perform(Action action) {
        requireRunning();
        if (phase != Phase.PLAY) {
            throw new IllegalStateException("actions are only allowed during the PLAY phase, not " + phase);
        }
        int manaBefore = active.availableMana();
        int maxManaBefore = active.maxMana();
        PlayerState actor = active;
        switch (action) {
            case Action.PlayCard playCard -> playCard(playCard);
            case Action.UseHeroPower ignored -> useHeroPower();
            case Action.Attack attack -> attack(attack);
            case Action.EndTurn ignored -> {
                endTurn();
                return;
            }
        }
        if (actor.availableMana() != manaBefore || actor.maxMana() != maxManaBefore) {
            emitMana(actor);
        }
        resolvePending();
    }

    /** Runs the RESOLVE and END phases and passes the turn to the other Hero. */
    public void endTurn() {
        requireRunning();
        if (phase != Phase.PLAY) {
            throw new IllegalStateException("the turn can only be ended during the PLAY phase, not " + phase);
        }

        enterPhase(Phase.RESOLVE);
        resolvePending();
        if (isOver()) {
            return;
        }

        enterPhase(Phase.END);
        emit(new MatchEvent.TurnEnded(turnNumber, side(active)));
        player1.expireTemporaryEffects(turnNumber);
        player2.expireTemporaryEffects(turnNumber);

        PlayerState previouslyActive = active;
        active = waiting;
        waiting = previouslyActive;

        if (turnNumber >= MAX_TURNS) {
            finishByTieBreak();
        }
    }

    // --- actions ------------------------------------------------------------

    private void playCard(Action.PlayCard action) {
        int handIndex = action.handIndex();
        if (handIndex < 0 || handIndex >= active.hand().size()) {
            throw new IllegalActionException("no card at hand index " + handIndex);
        }
        Card card = active.hand().get(handIndex);
        if (!active.canAfford(card.manaCost())) {
            throw new IllegalActionException("not enough mana to play " + card.id()
                    + " (" + card.manaCost() + " > " + active.availableMana() + ")");
        }
        switch (card) {
            case PokemonCard pokemon -> playPokemon(handIndex, pokemon);
            case ItemCard item -> playItem(handIndex, item, action.targetIndex());
        }
    }

    private void playPokemon(int handIndex, PokemonCard card) {
        if (active.board().isFull()) {
            throw new IllegalActionException("the Board is full, cannot play " + card.id());
        }
        active.spendMana(card.manaCost());
        active.removeFromHand(handIndex);
        emit(new MatchEvent.CardPlayed(side(active), card.id(), card.manaCost()));
        active.board().summon(card);
        emit(new MatchEvent.PokemonEntered(side(active), card.id(), card.attack(), card.maxHp()));
        card.ability().ifPresent(effect -> {
            currentSource = card.id();
            effect.apply(effectContext(null, Action.NO_POKEMON_TARGET));
        });
    }

    private void playItem(int handIndex, ItemCard card, int targetIndex) {
        PokemonInPlay target = card.requiresPokemonTarget() ? requireOpposingPokemon(card, targetIndex) : null;
        int discardIndex = card.target() == ItemCard.Target.DISCARDED_POKEMON
                ? requireDiscardedPokemon(card, targetIndex)
                : Action.NO_POKEMON_TARGET;

        active.spendMana(card.manaCost());
        active.removeFromHand(handIndex);
        emit(new MatchEvent.CardPlayed(side(active), card.id(), card.manaCost()));
        currentSource = card.id();
        card.effect().apply(effectContext(target, discardIndex));
        active.discard(card);
    }

    private void useHeroPower() {
        HeroPower power = active.hero().heroPower();
        if (active.heroPowerUsedThisTurn()) {
            throw new IllegalActionException(power.id() + " was already used this turn");
        }
        if (!active.canAfford(power.manaCost())) {
            throw new IllegalActionException("not enough mana to use " + power.id()
                    + " (" + power.manaCost() + " > " + active.availableMana() + ")");
        }
        active.spendMana(power.manaCost());
        active.markHeroPowerUsed();
        emit(new MatchEvent.HeroPowerUsed(side(active), power.id()));
        currentSource = power.id();
        power.effect().apply(effectContext(null, Action.NO_POKEMON_TARGET));
    }

    private void attack(Action.Attack action) {
        if (!active.board().hasSlot(action.attackerIndex())) {
            throw new IllegalActionException("no Pokémon in slot " + action.attackerIndex());
        }
        PokemonInPlay attacker = active.board().at(action.attackerIndex());
        if (!attacker.canAttack()) {
            throw new IllegalActionException(attacker.id() + " cannot attack this turn");
        }

        if (action.targetIndex() == Action.NO_POKEMON_TARGET) {
            emit(new MatchEvent.AttackDeclared(side(active), attacker.id(), null));
            damageOpposingHero(attacker.attack(), false, attacker.id());
        } else {
            PokemonInPlay defender = requireOpposingPokemon(attacker.card(), action.targetIndex());
            emit(new MatchEvent.AttackDeclared(side(active), attacker.id(), defender.id()));
            // Both Pokémon damage each other simultaneously.
            int attackerDamage = attacker.attack();
            int defenderDamage = defender.attack();
            int dealt = defender.receiveDamage(attackerDamage);
            int received = attacker.receiveDamage(defenderDamage);
            emit(new MatchEvent.PokemonDamaged(side(waiting), defender.id(), dealt, defender.currentHp()));
            emit(new MatchEvent.PokemonDamaged(side(active), attacker.id(), received, attacker.currentHp()));
        }
        attacker.markAttacked();
    }

    private PokemonInPlay requireOpposingPokemon(Card source, int targetIndex) {
        if (!waiting.board().hasSlot(targetIndex)) {
            throw new IllegalActionException(source.id() + " requires an opposing Pokémon, none in slot "
                    + targetIndex);
        }
        PokemonInPlay target = waiting.board().at(targetIndex);
        if (!target.isAlive()) {
            throw new IllegalActionException(source.id() + " cannot target the dead " + target.id());
        }
        return target;
    }

    /**
     * Validates a discard-pile target. With no Pokémon in the discard pile the play stays legal
     * and has no effect; otherwise the index must designate one of the Hero's discarded Pokémon.
     */
    private int requireDiscardedPokemon(Card source, int discardIndex) {
        if (!active.hasPokemonInDiscard()) {
            return Action.NO_POKEMON_TARGET;
        }
        if (!active.hasPokemonInDiscardAt(discardIndex)) {
            throw new IllegalActionException(source.id() + " requires a Pokémon of the discard pile, none at index "
                    + discardIndex);
        }
        return discardIndex;
    }

    // --- damage -------------------------------------------------------------

    private void damageOpposingHero(int amount, boolean fromAttackEffect, String source) {
        int total = amount + (fromAttackEffect ? active.consumeAttackBuffs() : 0);
        int taken = waiting.receiveDamage(total);
        active.recordDamageDealtToOpposingHero(taken);
        emit(new MatchEvent.HeroDamaged(side(waiting), total, taken, waiting.currentHp(), source,
                waiting.lastBlockedBy()));
    }

    private void damagePokemon(PokemonInPlay target, int amount, boolean fromAttackEffect) {
        if (target == null) {
            return;
        }
        int total = amount + (fromAttackEffect ? active.consumeAttackBuffs() : 0);
        int taken = target.receiveDamage(total);
        emit(new MatchEvent.PokemonDamaged(side(waiting), target.id(), taken, target.currentHp()));
    }

    // --- resolution ---------------------------------------------------------

    /** Death cleanup plus the win check; shared by the PLAY and RESOLVE phases. */
    private void resolvePending() {
        moveDeadPokemonToDiscard(player1);
        moveDeadPokemonToDiscard(player2);

        if (player1.isAlive() && player2.isAlive()) {
            return;
        }
        if (!player1.isAlive() && !player2.isAlive()) {
            finish(MatchResult.Outcome.DRAW, "both Heroes reached 0 HP");
        } else if (!player2.isAlive()) {
            finish(MatchResult.Outcome.PLAYER_ONE_WINS, player2.name() + " reached 0 HP");
        } else {
            finish(MatchResult.Outcome.PLAYER_TWO_WINS, player1.name() + " reached 0 HP");
        }
    }

    private void moveDeadPokemonToDiscard(PlayerState player) {
        player.board().removeDead().forEach(dead -> {
            player.discard(dead.card());
            emit(new MatchEvent.PokemonDefeated(side(player), dead.id()));
        });
    }

    private void finishByTieBreak() {
        if (player1.currentHp() != player2.currentHp()) {
            boolean playerOneWins = player1.currentHp() > player2.currentHp();
            finish(playerOneWins ? MatchResult.Outcome.PLAYER_ONE_WINS : MatchResult.Outcome.PLAYER_TWO_WINS,
                    "turn limit reached, highest remaining HP wins");
            return;
        }
        int damage1 = player1.damageDealtToOpposingHero();
        int damage2 = player2.damageDealtToOpposingHero();
        if (damage1 != damage2) {
            finish(damage1 > damage2 ? MatchResult.Outcome.PLAYER_ONE_WINS : MatchResult.Outcome.PLAYER_TWO_WINS,
                    "turn limit reached, equal HP, most damage dealt wins");
            return;
        }
        finish(MatchResult.Outcome.DRAW, "turn limit reached, equal HP and equal damage dealt");
    }

    private void finish(MatchResult.Outcome outcome, String reason) {
        result = new MatchResult(outcome, turnNumber, reason);
        emit(new MatchEvent.MatchEnded(result));
    }

    private void requireRunning() {
        if (isOver()) {
            throw new IllegalStateException("the match is over: " + result.reason());
        }
    }

    private EffectContext effectContext(PokemonInPlay target, int discardIndex) {
        return new MatchEffectContext(target, discardIndex);
    }

    /**
     * Gives effects a narrow, read-mostly access to the engine. "Self" is always the Hero
     * currently taking its turn, which is the Hero that played the card or used the power.
     */
    private final class MatchEffectContext implements EffectContext {

        private final PokemonInPlay target;
        private final int discardIndex;
        private final String source = currentSource;

        private MatchEffectContext(PokemonInPlay target, int discardIndex) {
            this.target = target;
            this.discardIndex = discardIndex;
        }

        @Override
        public int opposingHeroHp() {
            return waiting.currentHp();
        }

        @Override
        public void damageOpposingHero(int amount, boolean fromAttackEffect) {
            Match.this.damageOpposingHero(amount, fromAttackEffect, source);
        }

        @Override
        public void damageTargetPokemon(int amount, boolean fromAttackEffect) {
            damagePokemon(target, amount, fromAttackEffect);
        }

        @Override
        public void healSelfHero(int amount) {
            int before = active.currentHp();
            active.heal(amount);
            emit(new MatchEvent.HeroHealed(side(active), active.currentHp() - before, active.currentHp(), source));
        }

        @Override
        public void drawCards(int count) {
            for (int i = 0; i < count; i++) {
                drawFor(active);
            }
        }

        @Override
        public void restoreAvailableMana(int amount) {
            active.restoreAvailableMana(amount);
        }

        @Override
        public void increaseMaxMana(int maxManaGain, int availableManaGain) {
            active.increaseMaxMana(maxManaGain, availableManaGain);
        }

        @Override
        public void addHeroDamageReduction(int amount, boolean expiresAtEndOfFollowingTurn) {
            int expiry = expiresAtEndOfFollowingTurn ? turnNumber + 1 : TemporaryEffect.NEVER_EXPIRES;
            queue(TemporaryEffect.damageReduction(source, amount, expiry));
        }

        private void queue(TemporaryEffect effect) {
            active.addTemporaryEffect(effect);
            emit(new MatchEvent.EffectQueued(side(active), effect));
        }

        @Override
        public void addAttackBuff(int amount, boolean thisTurnOnly) {
            int expiry = thisTurnOnly ? turnNumber : TemporaryEffect.NEVER_EXPIRES;
            queue(TemporaryEffect.attackBuff(source, amount, expiry));
        }

        @Override
        public void returnChosenPokemonFromDiscard() {
            if (discardIndex != Action.NO_POKEMON_TARGET) {
                String cardId = active.discard().get(discardIndex).id();
                active.returnPokemonFromDiscard(discardIndex);
                emit(new MatchEvent.PokemonReturnedToHand(side(active), cardId));
            }
        }
    }
}
