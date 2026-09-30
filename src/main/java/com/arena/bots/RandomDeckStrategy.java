package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.decks.DeckStrategy;
import com.arena.engine.decks.RankedDeckBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** Random class and random legal deck, seeded so the result can be replayed. */
public final class RandomDeckStrategy implements DeckStrategy {

    private final Random random;

    /** Seeded, like every other source of chance in the project. */
    public RandomDeckStrategy(Random random) {
        this.random = random;
    }

    @Override
    public HeroClass chooseClass(HeroClasses classes) {
        return classes.all().get(random.nextInt(classes.all().size()));
    }

    @Override
    public List<Card> buildDeck(HeroClass heroClass, List<Card> neutralCards) {
        List<Card> pool = new ArrayList<>(heroClass.classCards());
        pool.addAll(neutralCards);
        pool.sort(Comparator.comparing(Card::name));
        Collections.shuffle(pool, random);
        Map<String, Integer> rank = new HashMap<>();
        for (int i = 0; i < pool.size(); i++) {
            rank.put(pool.get(i).name(), i);
        }
        return RankedDeckBuilder.build(heroClass, neutralCards, Comparator.comparingInt(c -> rank.get(c.name())),
                c -> true, 0);
    }
}
