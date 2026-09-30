package com.pokemonarena.bot;

import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardCategory;
import com.pokemonarena.cards.ItemCard;
import com.pokemonarena.cards.PokemonCard;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;
import com.pokemonarena.game.PokemonView;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Read-only helpers shared by the Bots to classify the engine's legal actions. They contain
 * decision heuristics only: legality always comes from {@link GameView#legalActions()}.
 */
final class BotSupport {

    private BotSupport() {
    }

    static Card card(GameView view, Action.PlayCard play) {
        return view.self().hand().get(play.handIndex());
    }

    static PokemonView attacker(GameView view, Action.Attack attack) {
        return view.self().board().get(attack.attackerIndex());
    }

    static PokemonView defender(GameView view, Action.Attack attack) {
        return view.opponent().board().get(attack.targetIndex());
    }

    static Stream<Action.PlayCard> cardPlays(GameView view) {
        return view.legalActions().stream()
                .filter(Action.PlayCard.class::isInstance)
                .map(Action.PlayCard.class::cast);
    }

    static Stream<Action.PlayCard> pokemonPlays(GameView view) {
        return cardPlays(view).filter(play -> card(view, play) instanceof PokemonCard);
    }

    static Stream<Action.PlayCard> itemPlays(GameView view, CardCategory category) {
        return cardPlays(view).filter(play -> card(view, play) instanceof ItemCard item && item.category() == category);
    }

    static Stream<Action.Attack> heroAttacks(GameView view) {
        return attacks(view).filter(attack -> attack.targetIndex() == Action.NO_POKEMON_TARGET);
    }

    static Stream<Action.Attack> pokemonAttacks(GameView view) {
        return attacks(view).filter(attack -> attack.targetIndex() != Action.NO_POKEMON_TARGET);
    }

    private static Stream<Action.Attack> attacks(GameView view) {
        return view.legalActions().stream()
                .filter(Action.Attack.class::isInstance)
                .map(Action.Attack.class::cast);
    }

    static boolean kills(GameView view, Action.Attack attack) {
        return defender(view, attack).currentHp() <= attacker(view, attack).attack();
    }

    static boolean survives(GameView view, Action.Attack attack) {
        return attacker(view, attack).currentHp() > defender(view, attack).attack();
    }

    /** Kills the defender and keeps the attacker alive; the most dangerous defender first. */
    static Optional<Action.Attack> bestFavorableTrade(GameView view) {
        return pokemonAttacks(view)
                .filter(attack -> kills(view, attack) && survives(view, attack))
                .max(Comparator.comparingInt(attack -> defender(view, attack).attack()));
    }

    /** Kills the most dangerous defender of at least {@code minAttack}, even at the cost of the attacker. */
    static Optional<Action.Attack> bestRemoval(GameView view, int minAttack) {
        return pokemonAttacks(view)
                .filter(attack -> kills(view, attack) && defender(view, attack).attack() >= minAttack)
                .max(Comparator.comparingInt((Action.Attack attack) -> defender(view, attack).attack())
                        .thenComparingInt(attack -> -attacker(view, attack).card().manaCost()));
    }

    static Optional<Action> heroPower(GameView view, CardCategory... categories) {
        CardCategory category = view.self().hero().heroPower().category();
        boolean matches = categories.length == 0 || List.of(categories).contains(category);
        return view.legalActions().stream().filter(Action.UseHeroPower.class::isInstance).filter(a -> matches).findFirst();
    }

    /** Targeted Items aim at the opposing Pokémon with the highest attack. */
    static Optional<Action.PlayCard> bestItemPlay(GameView view, CardCategory category) {
        return itemPlays(view, category).max(Comparator.comparingInt(play -> targetThreat(view, play)));
    }

    private static int targetThreat(GameView view, Action.PlayCard play) {
        if (card(view, play) instanceof ItemCard item && item.requiresPokemonTarget()) {
            return view.opponent().board().get(play.targetIndex()).attack();
        }
        return 0;
    }

    static int readyAttackPower(GameView view) {
        return heroAttacks(view).mapToInt(attack -> attacker(view, attack).attack()).sum();
    }

    static int opposingBoardAttack(GameView view) {
        return view.opponent().board().stream().mapToInt(PokemonView::attack).sum();
    }

    /** True when a card of the hand cannot be paid now: ramping would enable a stronger play. */
    static boolean hasUnaffordableCard(GameView view) {
        return view.self().hand().stream().anyMatch(card -> card.manaCost() > view.self().availableMana());
    }

    static Action endTurn() {
        return new Action.EndTurn();
    }
}
