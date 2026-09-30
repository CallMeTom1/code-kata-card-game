package com.pokemonarena.bot;

import com.pokemonarena.cards.CardCategory;
import com.pokemonarena.game.Action;
import com.pokemonarena.game.GameView;

import java.util.Comparator;
import java.util.Optional;

import static com.pokemonarena.bot.BotSupport.*;

/**
 * Pressure first: lethal, value trades, the strongest affordable Pokémon, direct damage from
 * Attack Items and Hero Powers, then face attacks. Resources are used when they enable
 * stronger plays.
 */
public final class AggressiveBot implements BotStrategy {

    @Override
    public String name() {
        return "Aggressive";
    }

    @Override
    public Action chooseAction(GameView view) {
        return lethal(view)
                .or(() -> bestFavorableTrade(view).map(Action.class::cast))
                .or(() -> strongestPokemon(view))
                .or(() -> bestItemPlay(view, CardCategory.ATTACK).map(Action.class::cast))
                .or(() -> heroPower(view, CardCategory.ATTACK))
                .or(() -> heroAttacks(view).findFirst().map(Action.class::cast))
                .or(() -> hasUnaffordableCard(view)
                        ? bestItemPlay(view, CardCategory.RESOURCE).map(Action.class::cast)
                        : Optional.empty())
                .or(() -> bestItemPlay(view, CardCategory.UTILITY).map(Action.class::cast))
                .or(() -> heroPower(view))
                .orElseGet(BotSupport::endTurn);
    }

    private static Optional<Action> lethal(GameView view) {
        if (readyAttackPower(view) >= view.opponent().currentHp()) {
            return heroAttacks(view).findFirst().map(Action.class::cast);
        }
        return Optional.empty();
    }

    private static Optional<Action> strongestPokemon(GameView view) {
        return pokemonPlays(view)
                .max(Comparator.comparingInt((Action.PlayCard play) -> card(view, play).manaCost())
                        .thenComparingInt(play -> -play.handIndex()))
                .map(Action.class::cast);
    }
}
