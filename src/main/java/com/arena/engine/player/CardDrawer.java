package com.arena.engine.player;

import com.arena.engine.cards.Card;
import com.arena.engine.events.CardBurned;
import com.arena.engine.events.CardDrawn;
import com.arena.engine.events.EventPublisher;
import com.arena.engine.events.FatigueDamage;

import java.util.Optional;

/** The one place that applies Hearthstone drawing rules: burn at 10 cards, fatigue on an empty deck. */
public final class CardDrawer {

    /** Hand size limit, as in Hearthstone. */
    public static final int MAX_HAND = 10;

    private final EventPublisher events;

    /** Takes the publisher so every draw, burn and fatigue hit is logged. */
    public CardDrawer(EventPublisher events) {
        this.events = events;
    }

    /** Draws cards one by one, so fatigue can grow within a single multi-draw. */
    public void draw(Champion champion, int count) {
        for (int i = 0; i < count && !champion.isDead(); i++) {
            drawOne(champion);
        }
    }

    private void drawOne(Champion champion) {
        Optional<Card> top = champion.takeTopCard();
        if (top.isEmpty()) {
            int hpBefore = champion.hp();
            int fatigue = champion.nextFatigue();
            champion.loseHp(fatigue);
            events.publish(new FatigueDamage(champion.name(), fatigue, hpBefore, champion.hp()));
            return;
        }
        Card card = top.get();
        if (champion.hand().size() >= MAX_HAND) {
            events.publish(new CardBurned(champion.name(), card.name()));
            return;
        }
        champion.addToHand(card);
        events.publish(new CardDrawn(champion.name(), card.name()));
    }
}
