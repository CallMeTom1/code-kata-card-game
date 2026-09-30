package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.GameView;
import com.arena.engine.match.PlayCard;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;

import static com.arena.bots.HandChoices.best;

/**
 * Plays like Aggressive while healthy; at 15 HP or below it heals first, then plays its
 * biggest defense or Taunt, and only then attacks (DESIGN.md).
 */
public final class DefensiveBot implements Bot {

    /** HP at or below which the bot turns defensive. */
    public static final int DANGER_HP = 15;
    private static final int MULLIGAN_FROM_COST = 5;

    private final AggressiveBot aggressive = new AggressiveBot();

    @Override
    public String name() {
        return "Defensive";
    }

    @Override
    public Action nextAction(GameView view) {
        if (view.myHp() <= DANGER_HP) {
            OptionalInt heal = best(view, c -> c.traits().heal() > 0, Comparator.comparingInt(c -> c.traits().heal()));
            if (heal.isPresent()) {
                return new PlayCard(heal.getAsInt());
            }
            OptionalInt defense = best(view, this::isDefense, Comparator.comparingInt(c -> c.traits().armor()));
            if (defense.isPresent()) {
                return new PlayCard(defense.getAsInt());
            }
        }
        return aggressive.nextAction(view);
    }

    /** Keeps more mid-cost cards than Aggressive, since it plans a longer game. */
    @Override
    public List<Integer> mulligan(List<Card> openingHand) {
        return AggressiveBot.costingAtLeast(openingHand, MULLIGAN_FROM_COST);
    }

    private boolean isDefense(Card card) {
        return card.category() == CardCategory.DEFENSE || card.traits().taunt();
    }
}
