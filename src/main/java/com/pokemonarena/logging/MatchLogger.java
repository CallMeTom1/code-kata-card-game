package com.pokemonarena.logging;

import com.pokemonarena.cards.CardCatalog;
import com.pokemonarena.game.MatchEvent;
import com.pokemonarena.game.MatchListener;
import com.pokemonarena.hero.HeroCatalog;

import java.io.PrintStream;
import java.util.stream.Collectors;

/** Human-readable turn-by-turn log. It only formats engine events and contains no game rule. */
public final class MatchLogger implements MatchListener {

    private final PrintStream out;
    private final String[] heroNames = new String[3];

    public MatchLogger(PrintStream out) {
        this.out = out;
    }

    @Override
    public void onEvent(MatchEvent event) {
        switch (event) {
            case MatchEvent.MatchStarted e -> {
                heroNames[1] = HeroCatalog.byId(e.hero1()).name() + " (P1)";
                heroNames[2] = HeroCatalog.byId(e.hero2()).name() + " (P2)";
                out.println("=== " + heroNames[1] + " vs " + heroNames[2] + " — " + heroNames[e.firstPlayer()]
                        + " plays first ===");
            }
            case MatchEvent.StartingHand e -> out.println(hero(e.player()) + " starting hand: "
                    + e.cardIds().stream().map(MatchLogger::card).collect(Collectors.joining(", ")));
            case MatchEvent.TurnStarted e -> out.println(System.lineSeparator() + "=== Turn " + e.turn() + " — "
                    + hero(e.player()) + " ===");
            case MatchEvent.PhaseChanged e -> out.println("  [" + e.phase() + "]");
            case MatchEvent.CardDrawn e -> out.println("  Draw: " + card(e.cardId()));
            case MatchEvent.DeckEmpty e -> out.println("  " + hero(e.player()) + " has no card left to draw");
            case MatchEvent.ManaChanged e -> out.println("  " + hero(e.player()) + " mana " + e.available() + "/" + e.max());
            case MatchEvent.CardPlayed e -> out.println("  Play: " + card(e.cardId()) + " (" + e.manaCost() + " mana)");
            case MatchEvent.PokemonEntered e -> out.println("  " + card(e.cardId()) + " enters the Board ("
                    + e.attack() + " ATK / " + e.hp() + " HP)");
            case MatchEvent.HeroPowerUsed e -> out.println("  Hero Power: " + e.powerId());
            case MatchEvent.AttackDeclared e -> out.println("  Attack: " + card(e.attackerId()) + " → "
                    + (e.targetId() == null ? hero(3 - e.player()) : card(e.targetId())));
            case MatchEvent.PokemonDamaged e -> out.println("  " + card(e.pokemonId()) + " takes " + e.amount()
                    + " damage → " + e.remainingHp() + " HP");
            case MatchEvent.HeroDamaged e -> out.println("  " + hero(e.player()) + " takes " + e.taken() + " damage"
                    + " from " + source(e.source())
                    + (e.blockedBy().isEmpty() ? "" : " (blocked by " + e.blockedBy().stream()
                    .map(MatchLogger::source).collect(Collectors.joining(" + ")) + ")")
                    + " → " + e.remainingHp() + " HP");
            case MatchEvent.HeroHealed e -> out.println("  " + hero(e.player()) + " heals " + e.amount()
                    + " from " + source(e.source()) + " → " + e.hp() + " HP");
            case MatchEvent.EffectQueued e -> out.println("  " + hero(e.player()) + " gains "
                    + e.effect().kind() + " " + e.effect().value() + " from " + e.effect().source());
            case MatchEvent.PokemonReturnedToHand e -> out.println("  " + card(e.cardId()) + " returns to the hand");
            case MatchEvent.PokemonDefeated e -> out.println("  " + card(e.pokemonId()) + " is defeated");
            case MatchEvent.TurnEnded e -> out.println("  End of turn.");
            case MatchEvent.MatchEnded e -> out.println(System.lineSeparator() + "=== Result after " + e.result().turns()
                    + " turns: " + e.result().outcome() + " (" + e.result().reason() + ") ===");
        }
    }

    private String hero(int player) {
        return heroNames[player];
    }

    /** Display name of an effect source: a card id or a Hero Power id. */
    private static String source(String id) {
        return HeroCatalog.all().stream().filter(h -> h.heroPower().id().equals(id))
                .map(h -> h.heroPower().name()).findFirst()
                .orElseGet(() -> CardCatalog.all().stream().filter(c -> c.id().equals(id))
                        .map(c -> c.name()).findFirst().orElse(String.valueOf(id)));
    }

    private static String card(String id) {
        return CardCatalog.byId(id).name();
    }
}
