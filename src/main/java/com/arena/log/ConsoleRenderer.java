package com.arena.log;

import com.arena.engine.events.ArmorExpired;
import com.arena.engine.events.ArmorGained;
import com.arena.engine.events.CardBurned;
import com.arena.engine.events.CardDrawn;
import com.arena.engine.events.CardPlayed;
import com.arena.engine.events.DamageDealt;
import com.arena.engine.events.DeckEntry;
import com.arena.engine.events.EvasionTriggered;
import com.arena.engine.events.FatigueDamage;
import com.arena.engine.events.FirstPlayerChosen;
import com.arena.engine.events.GameEvent;
import com.arena.engine.events.GameEventListener;
import com.arena.engine.events.Healed;
import com.arena.engine.events.HeroPowerUsed;
import com.arena.engine.events.IllegalAction;
import com.arena.engine.events.ManaGained;
import com.arena.engine.events.ManaRefilled;
import com.arena.engine.events.MatchEnded;
import com.arena.engine.events.MatchStarted;
import com.arena.engine.events.MinionAttacked;
import com.arena.engine.events.MinionDamaged;
import com.arena.engine.events.MinionDied;
import com.arena.engine.events.MinionSummoned;
import com.arena.engine.events.MulliganDone;
import com.arena.engine.events.OpeningHand;
import com.arena.engine.events.PlayerSetUp;
import com.arena.engine.events.PoisonTicked;
import com.arena.engine.events.StatusApplied;
import com.arena.engine.events.SummonFizzled;
import com.arena.engine.events.TurnStarted;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Turns events into the bracket-tagged log of DESIGN.md: one line per event, fixed-width
 * tags, easy to read and to grep. It only reads events, so the engine never prints.
 */
public final class ConsoleRenderer implements GameEventListener {

    private static final List<String> CATEGORIES = List.of("ATTACK", "DEFENSE", "RESOURCE", "UTILITY");
    private static final int MAX_BOARD = 7;

    private final PrintStream out;
    private final Map<String, Integer> maxMana = new HashMap<>();
    private int nameWidth = 2;
    private int round;
    private String current = "";

    /** Writes to any stream, so tests can capture the output. */
    public ConsoleRenderer(PrintStream out) {
        this.out = out;
    }

    @Override
    public void on(GameEvent event) {
        switch (event) {
            case MatchStarted e -> {
                nameWidth = Math.max(e.player1().length(), e.player2().length());
                line("[MATCH  ] " + e.player1() + " [" + e.player1Label() + "] vs " + e.player2() + " ["
                        + e.player2Label() + "] | seed " + e.seed());
            }
            case PlayerSetUp e -> setUp(e);
            case FirstPlayerChosen e -> line("[START  ] " + e.first() + " goes first (coin flip) | " + e.second()
                    + " gets The Coin");
            case MulliganDone e -> log(e.player(), "SWAP", e.putBack().isEmpty() ? "keeps the whole hand"
                    : "puts back " + String.join(", ", e.putBack()) + " → draws " + String.join(", ", e.drawn()));
            case OpeningHand e -> log(e.player(), "HAND", String.join(", ", e.cards()));
            case TurnStarted e -> {
                if (e.round() != round) {
                    round = e.round();
                    line(roundTag() + " " + "=".repeat(70));
                }
                current = e.player();
                act("TURN", "[HP " + e.hp() + "/30] [ARMOR " + e.armor() + "] [HAND " + e.handSize() + "] [DECK "
                        + e.deckSize() + "]");
            }
            case ManaRefilled e -> {
                maxMana.put(e.player(), e.maxMana());
                act("MANA", "[MANA " + e.mana() + "/" + e.maxMana() + "]" + (e.frozen() ? " [FROZEN -1]" : ""));
            }
            case CardDrawn e -> act("DRAW", e.card());
            case CardBurned e -> act("BURN", e.card() + " (hand full, card destroyed)");
            case FatigueDamage e -> act("FATIGUE", e.amount() + " dmg [HP " + e.hpBefore() + " → " + e.hpAfter() + "]");
            case ArmorExpired e -> act("ARMOR", e.amount() + " armor expired → [ARMOR " + e.armorLeft() + "]");
            case PoisonTicked e -> act("POISON", e.amount() + " dmg [HP " + e.hpBefore() + " → " + e.hpAfter() + "]");
            case CardPlayed e -> act("PLAY", e.card() + " (" + e.cost() + ") → " + mana(e.player(), e.manaLeft()));
            case HeroPowerUsed e -> act("POWER", e.power() + " (" + e.cost() + ") → " + mana(e.player(), e.manaLeft()));
            case IllegalAction e -> act("ILLEGAL", e.reason() + " → turn ends");
            case DamageDealt e -> act("DAMAGE", e.source() + " → " + e.target() + " : " + e.amount() + " dmg [ABSORBED "
                    + e.absorbed() + "] [HP " + e.hpBefore() + " → " + e.hpAfter() + "]");
            case EvasionTriggered e -> act("EVADE", e.player() + "'s Evasion prevents " + e.prevented() + " dmg from "
                    + e.source());
            case Healed e -> act("HEAL", e.source() + " → " + e.player() + " : +" + e.amount() + " [HP " + e.hpBefore()
                    + " → " + e.hpAfter() + "]");
            case ArmorGained e -> act("ARMOR", e.source() + " → " + e.player() + " : +" + e.amount() + " (" + e.turns()
                    + " turns) [ARMOR " + e.totalArmor() + "]");
            case ManaGained e -> {
                maxMana.put(e.player(), e.maxMana());
                act("MANA", e.source() + " → [MANA " + e.mana() + "/" + e.maxMana() + "]");
            }
            case StatusApplied e -> act("STATUS", e.target() + " [" + e.status() + "] from " + e.source());
            case MinionSummoned e -> act("SUMMON", e.minion() + " [" + e.attack() + "/" + e.health() + "]"
                    + (e.taunt() ? " [TAUNT]" : "") + " → [BOARD " + e.boardSize() + "/" + MAX_BOARD + "]");
            case SummonFizzled e -> act("SUMMON", e.minion() + " fizzles (board full)");
            case MinionAttacked e -> act("MINION", e.minion() + " [" + e.attack() + "/" + e.health() + "] attacks "
                    + e.target());
            case MinionDamaged e -> act("MINION", e.minion() + " (" + e.owner() + ") takes " + e.amount() + " from "
                    + e.source() + " [HEALTH " + e.healthLeft() + "]");
            case MinionDied e -> act("DEATH", e.minion() + " (" + e.owner() + ")");
            case MatchEnded e -> line("[RESULT ] " + ("DRAW".equals(e.winner()) ? "DRAW" : e.winner() + " WINS")
                    + " | reason: " + e.reason() + " | turns: " + e.rounds() + " | HP " + e.player1() + " " + e.hp1()
                    + " / " + e.player2() + " " + e.hp2() + " | damage " + e.player1() + " " + e.damage1() + " / "
                    + e.player2() + " " + e.damage2());
        }
    }

    private void setUp(PlayerSetUp e) {
        log(e.player(), "SETUP", "[" + e.bot() + "] plays [" + e.heroClass() + "] (" + e.classChoice() + ") | deck: "
                + e.deckSource());
        log(e.player(), "POWER", e.heroPower() + " (" + e.heroPowerCost() + ")"
                + (e.heroPowerText().isEmpty() ? "" : " — " + e.heroPowerText()));
        for (String category : CATEGORIES) {
            Map<String, Long> cards = e.deck().stream().filter(c -> c.category().equals(category))
                    .collect(Collectors.groupingBy(DeckEntry::name, LinkedHashMap::new, Collectors.counting()));
            if (!cards.isEmpty()) {
                long total = cards.values().stream().mapToLong(Long::longValue).sum();
                String list = cards.entrySet().stream().map(c -> c.getKey() + " x" + c.getValue())
                        .collect(Collectors.joining(", "));
                log(e.player(), "DECK", String.format("[%-8s %2d] %s", category, total, list));
            }
        }
        log(e.player(), "CURVE", curve(e.deck()));
    }

    private static String curve(List<DeckEntry> deck) {
        int[] buckets = new int[6];
        deck.forEach(card -> buckets[Math.min(card.cost(), 5)]++);
        StringBuilder text = new StringBuilder();
        for (int cost = 0; cost <= 5; cost++) {
            if (cost == 0 && buckets[0] == 0) {
                continue;
            }
            text.append("[").append(cost == 5 ? "5+" : String.valueOf(cost)).append(": ").append(buckets[cost])
                    .append("] ");
        }
        double average = deck.stream().mapToInt(DeckEntry::cost).average().orElse(0);
        return text + "| avg cost " + String.format(Locale.ROOT, "%.1f", average);
    }

    private String mana(String player, int left) {
        return "[MANA " + left + "/" + maxMana.getOrDefault(player, left) + "]";
    }

    private void act(String tag, String text) {
        log(current, tag, text);
    }

    /** Before turn 1 the tag leads ("[SETUP  ][P1]"); during turns the round and player lead. */
    private void log(String player, String tag, String text) {
        String tagText = "[" + String.format("%-7s", tag) + "]";
        String name = "[" + pad(player) + "]";
        line(round > 0 ? roundTag() + name + tagText + " " + text : tagText + name + " " + text);
    }

    private String roundTag() {
        return String.format("[T%02d]", round);
    }

    private String pad(String name) {
        return String.format("%-" + nameWidth + "s", name);
    }

    private void line(String text) {
        out.println(text);
    }
}
