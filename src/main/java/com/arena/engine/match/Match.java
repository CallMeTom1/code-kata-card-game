package com.arena.engine.match;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.cards.TheCoin;
import com.arena.engine.combat.ArmorDamageResolver;
import com.arena.engine.combat.DamageResolver;
import com.arena.engine.effects.SimpleEffectContext;
import com.arena.engine.events.ArmorExpired;
import com.arena.engine.events.CardBurned;
import com.arena.engine.events.CardDrawn;
import com.arena.engine.events.CardPlayed;
import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.FatigueDamage;
import com.arena.engine.events.HeroPowerUsed;
import com.arena.engine.events.ManaRefilled;
import com.arena.engine.events.MatchEnded;
import com.arena.engine.events.MatchStarted;
import com.arena.engine.events.TurnStarted;
import com.arena.engine.player.Champion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Runs one full match: shuffles decks, alternates turns and phases, and reports the result.
 * The only class that mutates {@link Champion} state through the match rules (Single Responsibility).
 */
public final class Match {

    public static final int TURN_LIMIT = 50;

    private final Champion player1;
    private final Champion player2;
    private final Bot bot1;
    private final Bot bot2;
    private final Random random;
    private final EventPublisher events;
    private final DamageResolver damage;
    private final TieBreaker tieBreaker = new TieBreaker();

    private int damageByPlayer1;
    private int damageByPlayer2;

    public Match(Champion player1, Bot bot1, Champion player2, Bot bot2, Random random, EventPublisher events) {
        this.player1 = player1;
        this.player2 = player2;
        this.bot1 = bot1;
        this.bot2 = bot2;
        this.random = random;
        this.events = events;
        this.damage = new ArmorDamageResolver(events);
        events.subscribe(this::trackDamage);
    }

    private void trackDamage(com.arena.engine.events.GameEvent event) {
        if (event instanceof DamageDealt dealt) {
            if (dealt.source().equals(player1.name())) {
                damageByPlayer1 += dealt.amount();
            } else if (dealt.source().equals(player2.name())) {
                damageByPlayer2 += dealt.amount();
            }
        }
    }

    /** Plays the whole match, turn by turn, and returns who won and why. */
    public MatchResult play() {
        shuffle(player1);
        shuffle(player2);
        boolean player1First = random.nextBoolean();
        Champion first = player1First ? player1 : player2;
        Champion second = player1First ? player2 : player1;
        Bot firstBot = player1First ? bot1 : bot2;
        Bot secondBot = player1First ? bot2 : bot1;

        dealOpeningHand(first, 3);
        dealOpeningHand(second, 4);
        second.addToHand(TheCoin.card());

        events.publish(new MatchStarted(player1.name(), player2.name(), currentSeed(), first.name()));

        for (int turn = 1; turn <= TURN_LIMIT; turn++) {
            playTurn(turn, first, firstBot, second);
            MatchResult finished = checkDeath(turn);
            if (finished != null) {
                return finished;
            }
            playTurn(turn, second, secondBot, first);
            finished = checkDeath(turn);
            if (finished != null) {
                return finished;
            }
        }

        String winner = tieBreaker.decide(player1, player2, damageByPlayer1, damageByPlayer2);
        events.publish(new MatchEnded(winner, "turn-limit", TURN_LIMIT));
        return new MatchResult(winner, "turn-limit", TURN_LIMIT, damageByPlayer1, damageByPlayer2);
    }

    private long currentSeed() {
        return random.hashCode();
    }

    private void shuffle(Champion champion) {
        List<Card> cards = new ArrayList<>(champion.heroClass().deck());
        Collections.shuffle(cards, random);
        champion.deck().clear();
        champion.deck().addAll(cards);
    }

    private void dealOpeningHand(Champion champion, int count) {
        for (int i = 0; i < count; i++) {
            drawCard(champion);
        }
    }

    private void playTurn(int turn, Champion active, Bot bot, Champion opponent) {
        events.publish(new TurnStarted(turn, active.name()));
        active.resetTurnFlags();
        drawPhase(active);
        manaPhase(active);
        List<Card> queued = playPhase(turn, active, bot, opponent);
        resolvePhase(active, opponent, queued);
        endPhase(active);
    }

    private void drawPhase(Champion active) {
        drawCard(active);
    }

    private void drawCard(Champion champion) {
        if (champion.deck().isEmpty()) {
            int fatigue = champion.nextFatigueDamage();
            champion.loseHp(fatigue);
            events.publish(new FatigueDamage(champion.name(), fatigue, champion.hp()));
            return;
        }
        Card card = champion.deck().pollFirst();
        boolean burned = champion.addToHand(card);
        if (burned) {
            events.publish(new CardBurned(champion.name(), card.name()));
        } else {
            events.publish(new CardDrawn(champion.name(), card.name()));
        }
    }

    private void manaPhase(Champion active) {
        active.growAndRefillMana();
        events.publish(new ManaRefilled(active.name(), active.maxMana()));
    }

    private List<Card> playPhase(int turn, Champion active, Bot bot, Champion opponent) {
        List<Card> queued = new ArrayList<>();
        while (true) {
            GameView view = new GameViewImpl(turn, active, opponent);
            Action action = bot.nextAction(view);
            if (action instanceof Action.EndTurn) {
                return queued;
            }
            if (action instanceof Action.UseHeroPower) {
                if (!tryUseHeroPower(active, opponent)) {
                    return queued;
                }
                continue;
            }
            if (action instanceof Action.PlayCard playCard) {
                Card card = tryPlayCard(active, opponent, playCard.handIndex());
                if (card == null) {
                    return queued;
                }
                if (card.immediate()) {
                    applyEffect(card, active, opponent);
                } else {
                    queued.add(card);
                }
                active.markCardPlayed();
                continue;
            }
            return queued;
        }
    }

    private boolean tryUseHeroPower(Champion active, Champion opponent) {
        var heroPower = active.heroClass().heroPower();
        if (active.heroPowerUsedThisTurn() || !active.canAfford(heroPower.cost())) {
            return false;
        }
        active.spendMana(heroPower.cost());
        active.markHeroPowerUsed();
        events.publish(new HeroPowerUsed(active.name(), heroPower.name(), active.mana()));
        heroPower.effect().apply(new SimpleEffectContext(active, opponent, damage, events, active.cardPlayedThisTurn()));
        return true;
    }

    private Card tryPlayCard(Champion active, Champion opponent, int handIndex) {
        List<Card> hand = active.hand();
        if (handIndex < 0 || handIndex >= hand.size()) {
            return null;
        }
        Card card = hand.get(handIndex);
        if (!active.canAfford(card.cost())) {
            return null;
        }
        active.spendMana(card.cost());
        active.removeFromHand(card);
        events.publish(new CardPlayed(active.name(), card.name(), card.cost(), active.mana()));
        return card;
    }

    private void applyEffect(Card card, Champion active, Champion opponent) {
        boolean comboActive = active.cardPlayedThisTurn();
        card.effect().apply(new SimpleEffectContext(active, opponent, damage, events, comboActive));
    }

    /** Defense/utility resolve first, then Attack cards, in the order they were played. */
    private void resolvePhase(Champion active, Champion opponent, List<Card> queued) {
        List<Card> ordered = new ArrayList<>(queued);
        ordered.sort((a, b) -> Boolean.compare(a.category() == CardCategory.ATTACK, b.category() == CardCategory.ATTACK));
        for (Card card : ordered) {
            applyEffect(card, active, opponent);
        }
    }

    private void endPhase(Champion active) {
        for (var expired : active.tickArmorForNewTurn()) {
            events.publish(new ArmorExpired(active.name(), expired.amount()));
        }
        active.tickParryForNewTurn();
        int poisonDamage = active.tickPoisonForNewTurn();
        if (poisonDamage > 0) {
            damage.dealIgnoringArmor(active.name() + "-poison", active, poisonDamage);
        }
    }

    private MatchResult checkDeath(int turn) {
        if (player1.isDead() || player2.isDead()) {
            String winner = player1.isDead() && player2.isDead() ? null
                    : player1.isDead() ? player2.name() : player1.name();
            events.publish(new MatchEnded(winner, "hp-zero", turn));
            return new MatchResult(winner, "hp-zero", turn, damageByPlayer1, damageByPlayer2);
        }
        return null;
    }
}
