package com.arena.bots;

import com.arena.engine.cards.Card;
import com.arena.engine.cards.CardCategory;
import com.arena.engine.match.Action;
import com.arena.engine.match.Bot;
import com.arena.engine.match.EndTurn;
import com.arena.engine.match.GameView;
import com.arena.engine.match.PlayCard;
import com.arena.engine.match.UseHeroPower;

import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.arena.bots.HandChoices.anyOther;
import static com.arena.bots.HandChoices.best;

/**
 * Plays for damage (DESIGN.md): Resource cards first, then the highest-damage Attack,
 * buffs only when an Attack can follow, leftovers, then the hero power.
 */
public final class AggressiveBot implements Bot {

    private static final int TEMP_MANA_REACH = 2;
    private static final int MULLIGAN_FROM_COST = 4;

    @Override
    public String name() {
        return "Aggressive";
    }

    @Override
    public Action nextAction(GameView view) {
        for (OptionalInt choice : List.of(
                best(view, this::isCrystal, Comparator.comparingInt(Card::cost).reversed()),
                best(view, c -> isTempMana(c) && unlocksACard(view, c), Comparator.comparingInt(Card::cost)),
                best(view, c -> c.traits().attackBuff() && attackCanFollow(view, c), Comparator.comparingInt(Card::cost)),
                best(view, c -> isAttack(c) && fitsOnBoard(view, c), byDamage()),
                best(view, c -> isUsefulLeftover(view, c), Comparator.comparingInt(Card::cost)))) {
            if (choice.isPresent()) {
                return new PlayCard(choice.getAsInt());
            }
        }
        return view.heroPowerAvailable() ? new UseHeroPower() : new EndTurn();
    }

    /** Puts back expensive cards, since an aggressive hand wants to act early. */
    @Override
    public List<Integer> mulligan(List<Card> openingHand) {
        return costingAtLeast(openingHand, MULLIGAN_FROM_COST);
    }

    static List<Integer> costingAtLeast(List<Card> hand, int cost) {
        return IntStream.range(0, hand.size()).filter(i -> hand.get(i).cost() >= cost).boxed().toList();
    }

    static Comparator<Card> byDamage() {
        return Comparator.comparingInt((Card c) -> c.traits().damage()).thenComparingInt(Card::cost);
    }

    static boolean isAttack(Card card) {
        return card.category() == CardCategory.ATTACK && card.traits().damage() > 0 && !card.traits().attackBuff();
    }

    private boolean isCrystal(Card card) {
        return card.category() == CardCategory.RESOURCE && card.cost() > 0;
    }

    private boolean isTempMana(Card card) {
        return card.category() == CardCategory.RESOURCE && card.cost() == 0;
    }

    private boolean unlocksACard(GameView view, Card tempMana) {
        int mana = view.myMana();
        Predicate<Card> unlocked = c -> c.category() != CardCategory.RESOURCE && c.cost() > mana;
        return anyOther(view, tempMana, mana + TEMP_MANA_REACH, unlocked);
    }

    private boolean attackCanFollow(GameView view, Card buff) {
        return anyOther(view, buff, view.myMana() - buff.cost(), c -> c.traits().usesAttackBonus());
    }

    private static boolean fitsOnBoard(GameView view, Card card) {
        return card.traits().usesAttackBonus() || view.myBoardSize() < 7;
    }

    private boolean isUsefulLeftover(GameView view, Card card) {
        if (card.category() == CardCategory.RESOURCE || card.traits().attackBuff()) {
            return false;
        }
        return card.traits().heal() == 0 || view.myHp() + card.traits().heal() <= 30;
    }
}
