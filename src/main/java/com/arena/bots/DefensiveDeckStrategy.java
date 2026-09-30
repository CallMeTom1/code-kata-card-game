package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.classes.HeroClass;
import com.arena.engine.classes.HeroClasses;
import com.arena.engine.decks.DeckStrategy;
import com.arena.engine.decks.RankedDeckBuilder;

import java.util.Comparator;
import java.util.List;

/** Defense, heals and Taunt first, with at least 6 Attack cards so it can still win (DESIGN.md). */
public final class DefensiveDeckStrategy implements DeckStrategy {

    /** Default class with {@code auto}: the most armor. */
    public static final String DEFAULT_CLASS = "Tank";
    private static final int MIN_ATTACKS = 6;

    @Override
    public HeroClass chooseClass(HeroClasses classes) {
        return classes.byName(DEFAULT_CLASS).orElse(classes.all().getFirst());
    }

    @Override
    public List<Card> buildDeck(HeroClass heroClass, List<Card> neutralCards) {
        Comparator<Card> preference = Comparator.comparingInt(DefensiveDeckStrategy::group)
                .thenComparing(Comparator.comparingInt(DefensiveDeckStrategy::value).reversed())
                .thenComparing(Card::name);
        return RankedDeckBuilder.build(heroClass, neutralCards, preference, c -> true, MIN_ATTACKS);
    }

    private static int group(Card card) {
        if (card.traits().heal() > 0 || card.traits().armor() > 0 || card.traits().taunt()) {
            return 0;
        }
        if (card.category() == CardCategory.ATTACK) {
            return 1;
        }
        return card.category() == CardCategory.RESOURCE ? 2 : 3;
    }

    private static int value(Card card) {
        return card.traits().heal() + card.traits().armor() + card.traits().damage();
    }
}
