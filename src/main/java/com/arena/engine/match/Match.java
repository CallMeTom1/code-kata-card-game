package com.arena.engine.match;

import com.arena.engine.board.Minion;
import com.arena.engine.board.MinionAbility;
import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.cards.NeutralCards;
import com.arena.engine.classes.HeroPower;
import com.arena.engine.combat.Combat;
import com.arena.engine.effects.Effect;
import com.arena.engine.events.ArmorExpired;
import com.arena.engine.events.CardPlayed;
import com.arena.engine.events.DeckEntry;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.FirstPlayerChosen;
import com.arena.engine.events.Healed;
import com.arena.engine.events.HeroPowerUsed;
import com.arena.engine.events.IllegalAction;
import com.arena.engine.events.ManaRefilled;
import com.arena.engine.events.MatchEnded;
import com.arena.engine.events.MatchStarted;
import com.arena.engine.events.MinionAttacked;
import com.arena.engine.events.MulliganDone;
import com.arena.engine.events.OpeningHand;
import com.arena.engine.events.PlayerSetUp;
import com.arena.engine.events.PoisonTicked;
import com.arena.engine.events.StatusApplied;
import com.arena.engine.events.TurnStarted;
import com.arena.engine.player.CardDrawer;
import com.arena.engine.player.Champion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.TreeSet;

/**
 * Plays one match from setup to verdict, following the turn structure of the brief:
 * draw, mana, play, resolve, end. It never prints: everything goes through events.
 */
public final class Match {

    /** Round limit from the brief. */
    public static final int MAX_ROUNDS = 50;
    private static final int MAX_ACTIONS_PER_TURN = 60;
    private static final int FIRST_PLAYER_HAND = 3;
    private static final int SECOND_PLAYER_HAND = 4;

    private final Seat seat1;
    private final Seat seat2;
    private final long seed;
    private final EventPublisher events;
    private final CoinFlip coinFlip;
    private final Random random;
    private final Combat combat;
    private final CardDrawer drawer;
    private final DamageTracker damage = new DamageTracker();
    private Seat first;
    private int round;

    /** A match with a seeded coin flip, as in DESIGN.md. */
    public Match(Contender player1, Contender player2, long seed, EventPublisher events) {
        this(player1, player2, seed, events, CoinFlip.RANDOM);
    }

    /** A match with an injected coin flip, so tests can choose who goes first. */
    public Match(Contender player1, Contender player2, long seed, EventPublisher events, CoinFlip coinFlip) {
        this.seat1 = new Seat(player1, new Champion(player1.name(), player1.deck()));
        this.seat2 = new Seat(player2, new Champion(player2.name(), player2.deck()));
        this.seed = seed;
        this.events = events;
        this.coinFlip = coinFlip;
        this.random = Seeds.random(seed);
        this.combat = new Combat(events);
        this.drawer = new CardDrawer(events);
        events.subscribe(damage);
    }

    /** Runs the whole match; the same seed and bots always give the same events. */
    public MatchResult play() {
        Verdict verdict;
        try {
            setUp();
            verdict = playRounds();
        } catch (MatchOver over) {
            verdict = over.verdict;
        }
        Champion c1 = seat1.champion;
        Champion c2 = seat2.champion;
        int damage1 = damage.hpLostBy(c2.name());
        int damage2 = damage.hpLostBy(c1.name());
        events.publish(new MatchEnded(verdict.isDraw() ? "DRAW" : verdict.winner(), verdict.reason(), round,
                c1.name(), c1.hp(), damage1, c2.name(), c2.hp(), damage2));
        return new MatchResult(c1.name(), c2.name(), first == null ? c1.name() : first.name(), verdict.winner(),
                verdict.reason(), round, c1.hp(), c2.hp(), damage1, damage2);
    }

    private void setUp() {
        events.publish(new MatchStarted(seat1.name(), seat1.contender.label(), seat2.name(),
                seat2.contender.label(), seed));
        publishSetUp(seat1);
        publishSetUp(seat2);
        first = coinFlip.player1First(random) ? seat1 : seat2;
        Seat second = other(first);
        events.publish(new FirstPlayerChosen(first.name(), second.name()));
        seat1.champion.shuffleIntoDeck(List.of(), random);
        seat2.champion.shuffleIntoDeck(List.of(), random);
        dealSilently(first.champion, FIRST_PLAYER_HAND);
        dealSilently(second.champion, SECOND_PLAYER_HAND);
        mulligan(first);
        mulligan(second);
        second.champion.addToHand(NeutralCards.THE_COIN);
        publishHand(first);
        publishHand(second);
    }

    private void publishSetUp(Seat seat) {
        Contender c = seat.contender;
        HeroPower power = c.heroClass().heroPower();
        List<DeckEntry> deck = c.deck().stream()
                .map(card -> new DeckEntry(card.name(), card.cost(), card.category().name(), card.text())).toList();
        events.publish(new PlayerSetUp(c.name(), c.bot().name(), c.heroClass().name(), c.classChoice(),
                c.deckSource(), power.name(), power.cost(), power.description(), deck));
    }

    private void dealSilently(Champion champion, int count) {
        for (int i = 0; i < count; i++) {
            champion.takeTopCard().ifPresent(champion::addToHand);
        }
    }

    private void mulligan(Seat seat) {
        Champion champion = seat.champion;
        List<Card> hand = champion.hand();
        TreeSet<Integer> positions = new TreeSet<>();
        for (Integer index : seat.contender.bot().mulligan(hand)) {
            if (index != null && index >= 0 && index < hand.size()) {
                positions.add(index);
            }
        }
        List<Card> putBack = new ArrayList<>();
        for (int index : positions.descendingSet()) {
            putBack.addFirst(champion.removeFromHand(index));
        }
        champion.shuffleIntoDeck(putBack, random);
        int before = champion.hand().size();
        dealSilently(champion, putBack.size());
        List<String> drawn = champion.hand().subList(before, champion.hand().size()).stream().map(Card::name).toList();
        events.publish(new MulliganDone(champion.name(), putBack.stream().map(Card::name).toList(), drawn));
    }

    private void publishHand(Seat seat) {
        events.publish(new OpeningHand(seat.name(), seat.champion.hand().stream().map(Card::name).toList()));
    }

    private Verdict playRounds() {
        Seat second = other(first);
        for (round = 1; round <= MAX_ROUNDS; round++) {
            takeTurn(first);
            takeTurn(second);
        }
        round = MAX_ROUNDS;
        Champion c1 = seat1.champion;
        Champion c2 = seat2.champion;
        return Referee.tieBreak(c1.name(), c1.hp(), damage.hpLostBy(c2.name()),
                c2.name(), c2.hp(), damage.hpLostBy(c1.name()));
    }

    private void takeTurn(Seat seat) {
        Champion me = seat.champion;
        events.publish(new TurnStarted(round, me.name(), me.hp(), me.defenses().armor(), me.hand().size(),
                me.deck().size()));
        startOfTurn(me);
        drawer.draw(me, 1);
        checkDeath();
        boolean frozen = me.isFrozen();
        me.growMana();
        events.publish(new ManaRefilled(me.name(), me.mana(), me.maxMana(), frozen));
        List<Pending> queue = playPhase(seat);
        for (Pending pending : queue) {
            resolve(pending, seat);
            checkDeath();
        }
        minionsAttack(seat);
        endOfTurn(me);
    }

    private void startOfTurn(Champion me) {
        int expired = me.defenses().onOwnerTurnStart();
        if (expired > 0) {
            events.publish(new ArmorExpired(me.name(), expired, me.defenses().armor()));
        }
        int poison = me.tickPoison();
        if (poison > 0) {
            int before = me.hp();
            me.loseHp(poison);
            events.publish(new PoisonTicked(me.name(), poison, before, me.hp()));
            checkDeath();
        }
    }

    private List<Pending> playPhase(Seat seat) {
        Champion me = seat.champion;
        HeroPower power = seat.contender.heroClass().heroPower();
        List<Pending> queue = new ArrayList<>();
        boolean powerUsed = false;
        int cardsPlayed = 0;
        for (int i = 0; i < MAX_ACTIONS_PER_TURN; i++) {
            Action action = seat.contender.bot().nextAction(view(seat, powerUsed));
            switch (action) {
                case EndTurn end -> {
                    return queue;
                }
                case PlayCard play -> {
                    if (play.handIndex() >= me.hand().size()) {
                        return refuse(me, "no card at position " + play.handIndex(), queue);
                    }
                    Card card = me.hand().get(play.handIndex());
                    if (card.cost() > me.mana()) {
                        return refuse(me, card.name() + " costs " + card.cost() + ", only " + me.mana() + " mana",
                                queue);
                    }
                    me.spendMana(card.cost());
                    me.removeFromHand(play.handIndex());
                    boolean combo = cardsPlayed > 0;
                    cardsPlayed++;
                    events.publish(new CardPlayed(me.name(), card.name(), card.cost(), me.mana()));
                    boolean usesBonus = card.category() == CardCategory.ATTACK && card.traits().usesAttackBonus();
                    Pending pending = new Pending(card.name(), card.effect(), combo, usesBonus);
                    if (card.immediate()) {
                        resolve(pending, seat);
                        checkDeath();
                    } else {
                        queue.add(pending);
                    }
                }
                case UseHeroPower use -> {
                    if (powerUsed) {
                        return refuse(me, "hero power already used this turn", queue);
                    }
                    if (power.cost() > me.mana()) {
                        return refuse(me, power.name() + " costs " + power.cost() + ", only " + me.mana() + " mana",
                                queue);
                    }
                    me.spendMana(power.cost());
                    powerUsed = true;
                    events.publish(new HeroPowerUsed(me.name(), power.name(), power.cost(), me.mana()));
                    queue.add(new Pending(power.name(), power.effect(), false, false));
                }
            }
        }
        return queue;
    }

    private List<Pending> refuse(Champion me, String reason, List<Pending> queue) {
        events.publish(new IllegalAction(me.name(), reason));
        return queue;
    }

    private void resolve(Pending pending, Seat seat) {
        Champion me = seat.champion;
        int bonus = pending.usesAttackBonus() ? me.takeAttackBonus() : 0;
        pending.effect().apply(new TurnContext(me, other(seat).champion, combat, events, pending.source(),
                pending.combo(), bonus, round, drawer));
    }

    private void minionsAttack(Seat seat) {
        Champion me = seat.champion;
        Champion foe = other(seat).champion;
        for (Minion minion : me.board().minions()) {
            if (minion.isDead() || !minion.canAttack(round)) {
                continue;
            }
            Optional<Minion> taunt = foe.board().firstTaunt();
            if (taunt.isPresent()) {
                Minion blocker = taunt.get();
                if (isSuicide(minion, blocker)) {
                    continue;
                }
                events.publish(new MinionAttacked(me.name(), minion.name(), minion.attack(), minion.health(),
                        blocker.name()));
                combat.damageMinion(minion.name(), foe, blocker, minion.attack());
                combat.damageMinion(blocker.name(), me, minion, blocker.attack());
            } else {
                events.publish(new MinionAttacked(me.name(), minion.name(), minion.attack(), minion.health(),
                        foe.name()));
                combat.deal(minion.name(), foe, minion.attack());
                if (minion.ability() instanceof MinionAbility.PoisonOnHit poison) {
                    foe.applyPoison(poison.amount(), poison.turns());
                    events.publish(new StatusApplied(foe.name(), minion.name(),
                            "POISON " + poison.amount() + " (" + poison.turns() + " turns)"));
                }
                checkDeath();
            }
        }
    }

    /** Attacking is optional in Hearthstone: a minion stays back rather than die without killing the Taunt. */
    private static boolean isSuicide(Minion attacker, Minion blocker) {
        return blocker.attack() >= attacker.health() && attacker.attack() < blocker.health();
    }

    private void endOfTurn(Champion me) {
        for (Minion minion : me.board().minions()) {
            if (minion.ability() instanceof MinionAbility.HealOwnerAtEndOfTurn heal && me.hp() < Champion.MAX_HP) {
                int before = me.hp();
                me.heal(heal.amount());
                events.publish(new Healed(me.name(), minion.name(), me.hp() - before, before, me.hp()));
            }
        }
    }

    private GameView view(Seat seat, boolean powerUsed) {
        Champion me = seat.champion;
        Champion foe = other(seat).champion;
        int powerCost = seat.contender.heroClass().heroPower().cost();
        return new SeatView(round, me.hp(), me.defenses().armor(), me.mana(), me.hand(),
                !powerUsed && me.mana() >= powerCost, powerCost, me.board().minions().size(), foe.hp(),
                foe.defenses().armor(), foe.hand().size(), foe.board().minions().size());
    }

    private void checkDeath() {
        Champion c1 = seat1.champion;
        Champion c2 = seat2.champion;
        if (c1.isDead() || c2.isDead()) {
            throw new MatchOver(Referee.onDeath(c1.name(), c1.isDead(), c2.name(), c2.isDead()));
        }
    }

    private Seat other(Seat seat) {
        return seat == seat1 ? seat2 : seat1;
    }

    private record Seat(Contender contender, Champion champion) {
        String name() {
            return contender.name();
        }
    }

    private record Pending(String source, Effect effect, boolean combo, boolean usesAttackBonus) {
    }

    /** Stops the match at once when a champion dies, wherever that happens in the turn. */
    private static final class MatchOver extends RuntimeException {
        private final transient Verdict verdict;

        private MatchOver(Verdict verdict) {
            super(null, null, false, false);
            this.verdict = verdict;
        }
    }
}
