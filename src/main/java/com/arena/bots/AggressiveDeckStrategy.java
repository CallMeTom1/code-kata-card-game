package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.decks.DeckStrategy;
import com.arena.engine.decks.RankedDeckBuilder;

import java.util.Comparator;
import java.util.List;

/** Attacks with the best damage per mana first, then Resource cards, no pure heals (DESIGN.md). */
public final class AggressiveDeckStrategy implements DeckStrategy {

    /** Default class with {@code auto}: the most damage per mana. */
    public static final String DEFAULT_CLASS = "Assassin";

    @Override
    public HeroClass chooseClass(HeroClasses classes) {
        return classes.byName(DEFAULT_CLASS).orElse(classes.all().getFirst());
    }

    @Override
    public List<Card> buildDeck(HeroClass heroClass, List<Card> neutralCards) {
        Comparator<Card> preference = Comparator.comparingInt(AggressiveDeckStrategy::group)
                .thenComparing(Comparator.comparingDouble(AggressiveDeckStrategy::damagePerMana).reversed())
                .thenComparingInt(Card::cost)
                .thenComparing(Card::name);
        return RankedDeckBuilder.build(heroClass, neutralCards, preference,
                c -> !(c.traits().heal() > 0 && c.traits().damage() == 0), 0);
    }

    private static double damagePerMana(Card card) {
        return (double) card.traits().damage() / (card.cost() + 1);
    }

    private static int group(Card card) {
        if (card.category() == CardCategory.ATTACK && card.traits().damage() > 0) {
            return 0;
        }
        return card.category() == CardCategory.RESOURCE ? 1 : 2;
    }
}
