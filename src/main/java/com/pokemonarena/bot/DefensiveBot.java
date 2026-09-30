package com.pokemonarena.bot;

import com.pokemonarena.cards.CardCategory;
import com.pokemonarena.cards.PokemonCard;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;

import java.util.Comparator;
import java.util.Optional;

import static com.pokemonarena.bot.BotSupport.*;

/**
 * Board and survival first. Above {@value #LOW_HP} HP it develops a sturdy Board, removes
 * dangerous Pokémon and only goes face when it is safe; at {@value #LOW_HP} HP or below it
 * heals, reduces incoming damage and removes threats before anything else.
 */
public final class DefensiveBot implements BotStrategy {

    public static final int LOW_HP = 15;
    /** Opposing Pokémon with at least this attack are worth trading a Pokémon for. */
    private static final int DANGEROUS_ATTACK = 4;

    @Override
    public String name() {
        return "Defensive";
    }

    @Override
    public Action chooseAction(GameView view) {
        return view.self().currentHp() <= LOW_HP ? survive(view) : develop(view);
    }

    private static Action develop(GameView view) {
        return bestFavorableTrade(view).map(Action.class::cast)
                .or(() -> bestRemoval(view, DANGEROUS_ATTACK).map(Action.class::cast))
                .or(() -> sturdiestPokemon(view))
                .or(() -> view.opponent().board().isEmpty()
                        ? Optional.empty()
                        : bestItemPlay(view, CardCategory.DEFENSE).map(Action.class::cast))
                .or(() -> hasUnaffordableCard(view)
                        ? bestItemPlay(view, CardCategory.RESOURCE).map(Action.class::cast)
                        : Optional.empty())
                .or(() -> bestItemPlay(view, CardCategory.UTILITY).map(Action.class::cast))
                .or(() -> safeToGoFace(view) ? heroAttacks(view).findFirst().map(Action.class::cast) : Optional.empty())
                .or(() -> heroPower(view))
                .orElseGet(BotSupport::endTurn);
    }

    private static Action survive(GameView view) {
        return bestItemPlay(view, CardCategory.UTILITY).map(Action.class::cast)
                .or(() -> bestItemPlay(view, CardCategory.DEFENSE).map(Action.class::cast))
                .or(() -> heroPower(view, CardCategory.DEFENSE, CardCategory.UTILITY))
                .or(() -> bestRemoval(view, 0).map(Action.class::cast))
                .or(() -> sturdiestPokemon(view))
                .or(() -> view.opponent().board().isEmpty()
                        ? heroAttacks(view).findFirst().map(Action.class::cast)
                        : Optional.empty())
                .or(() -> heroPower(view))
                .orElseGet(BotSupport::endTurn);
    }

    /** Going face is safe when the opposing Board cannot bring this Hero down to the low-HP zone. */
    private static boolean safeToGoFace(GameView view) {
        return view.self().currentHp() - opposingBoardAttack(view) > LOW_HP;
    }

    private static Optional<Action> sturdiestPokemon(GameView view) {
        return pokemonPlays(view)
                .max(Comparator.comparingInt((Action.PlayCard play) -> ((PokemonCard) card(view, play)).maxHp())
                        .thenComparingInt(play -> -play.handIndex()))
                .map(Action.class::cast);
    }
}
