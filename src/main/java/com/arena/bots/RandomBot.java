package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.GameView;
import com.arena.engine.match.PlayCard;
import com.arena.engine.match.UseHeroPower;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/** Baseline: plays random affordable things. Both real bots should beat it clearly. */
public final class RandomBot implements Bot {

    private final Random random;

    /** Seeded, so a match with a Random bot can still be replayed. */
    public RandomBot(Random random) {
        this.random = random;
    }

    @Override
    public String name() {
        return "Random";
    }

    @Override
    public Action nextAction(GameView view) {
        List<Action> options = new ArrayList<>();
        List<Card> hand = view.myHand();
        IntStream.range(0, hand.size()).filter(i -> hand.get(i).cost() <= view.myMana())
                .forEach(i -> options.add(new PlayCard(i)));
        if (view.heroPowerAvailable()) {
            options.add(new UseHeroPower());
        }
        return options.isEmpty() ? new EndTurn() : options.get(random.nextInt(options.size()));
    }

    @Override
    public List<Integer> mulligan(List<Card> openingHand) {
        return IntStream.range(0, openingHand.size()).filter(i -> random.nextBoolean()).boxed().toList();
    }
}
