package com.pokemonarena.game;

import com.pokemonarena.board.Board;
import com.pokemonarena.cards.Card;
import com.pokemonarena.cards.CardNature;
import com.pokemonarena.deck.Deck;
import com.pokemonarena.hero.Hero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mutable in-match state of one Hero: HP, mana, hand, deck, discard pile, Board, temporary
 * effects and Hero Power usage. The {@link Hero} definition itself stays immutable.
 */
public final class PlayerState {

    public static final int MAX_MANA = 10;

    private final Hero hero;
    private final Deck deck;
    private final Board board = new Board();
    private final List<Card> hand = new ArrayList<>();
    private final List<Card> discard = new ArrayList<>();
    private final List<TemporaryEffect> temporaryEffects = new ArrayList<>();

    private int currentHp;
    private int maxMana;
    private int availableMana;
    private boolean heroPowerUsedThisTurn;
    private int damageDealtToOpposingHero;

    public PlayerState(Hero hero, Deck deck) {
        this.hero = Objects.requireNonNull(hero, "hero");
        this.deck = Objects.requireNonNull(deck, "deck");
        this.currentHp = hero.startingHp();
    }

    public Hero hero() {
        return hero;
    }

    public String name() {
        return hero.name();
    }

    public Board board() {
        return board;
    }

    public Deck deck() {
        return deck;
    }

    public List<Card> hand() {
        return Collections.unmodifiableList(hand);
    }

    public List<Card> discard() {
        return Collections.unmodifiableList(discard);
    }

    public List<TemporaryEffect> temporaryEffects() {
        return Collections.unmodifiableList(temporaryEffects);
    }

    public int currentHp() {
        return currentHp;
    }

    public int maxMana() {
        return maxMana;
    }

    public int availableMana() {
        return availableMana;
    }

    public boolean isAlive() {
        return currentHp > 0;
    }

    public boolean heroPowerUsedThisTurn() {
        return heroPowerUsedThisTurn;
    }

    public int damageDealtToOpposingHero() {
        return damageDealtToOpposingHero;
    }

    // --- turn bookkeeping ---------------------------------------------------

    /** Beginning of this Hero's turn, before the DRAW phase. */
    void beginTurn() {
        heroPowerUsedThisTurn = false;
        board.refreshForNewTurn();
    }

    /** DRAW phase: draws one card; nothing happens when the deck is empty (no fatigue). */
    Optional<Card> drawCard() {
        Optional<Card> card = deck.draw();
        card.ifPresent(hand::add);
        return card;
    }

    /** MANA phase: maximum mana grows by 1 up to the cap, then available mana is refilled. */
    void gainMana() {
        maxMana = Math.min(MAX_MANA, maxMana + 1);
        availableMana = maxMana;
    }

    /** END phase: drops the temporary effects whose duration is over. */
    void expireTemporaryEffects(int turnNumber) {
        temporaryEffects.removeIf(effect -> effect.isExpiredAfter(turnNumber));
    }

    // --- resources ----------------------------------------------------------

    boolean canAfford(int manaCost) {
        return availableMana >= manaCost;
    }

    void spendMana(int manaCost) {
        if (!canAfford(manaCost)) {
            throw new IllegalArgumentException("not enough mana: " + manaCost + " > " + availableMana);
        }
        availableMana -= manaCost;
    }

    void restoreAvailableMana(int amount) {
        availableMana = Math.min(maxMana, availableMana + amount);
    }

    void increaseMaxMana(int maxManaGain, int availableManaGain) {
        maxMana = Math.min(MAX_MANA, maxMana + maxManaGain);
        restoreAvailableMana(availableManaGain);
    }

    void markHeroPowerUsed() {
        heroPowerUsedThisTurn = true;
    }

    // --- HP -----------------------------------------------------------------

    /**
     * Applies damage to the Hero after its queued reductions. Reductions are consumed oldest
     * first and stack additively until the damage is fully absorbed.
     *
     * @return the damage actually taken
     */
    int receiveDamage(int amount) {
        int remaining = amount;
        List<TemporaryEffect> consumed = new ArrayList<>();
        for (TemporaryEffect effect : temporaryEffects) {
            if (remaining <= 0) {
                break;
            }
            if (effect.kind() == TemporaryEffect.Kind.DAMAGE_REDUCTION) {
                remaining = Math.max(0, remaining - effect.value());
                consumed.add(effect);
            }
        }
        temporaryEffects.removeAll(consumed);
        lastBlockedBy = consumed.stream().map(TemporaryEffect::source).toList();

        int taken = Math.min(remaining, currentHp);
        currentHp -= taken;
        return taken;
    }

    /** Sources of the reductions consumed by the latest {@link #receiveDamage} call, oldest first. */
    List<String> lastBlockedBy() {
        return lastBlockedBy;
    }

    private List<String> lastBlockedBy = List.of();

    void heal(int amount) {
        currentHp = Math.min(hero.startingHp(), currentHp + amount);
    }

    void recordDamageDealtToOpposingHero(int amount) {
        damageDealtToOpposingHero += amount;
    }

    // --- temporary effects --------------------------------------------------

    void addTemporaryEffect(TemporaryEffect effect) {
        temporaryEffects.add(effect);
    }

    /** Sums and consumes every pending attack buff; called by a damage event of an Attack effect. */
    int consumeAttackBuffs() {
        int bonus = 0;
        for (TemporaryEffect effect : temporaryEffects) {
            if (effect.kind() == TemporaryEffect.Kind.ATTACK_BUFF) {
                bonus += effect.value();
            }
        }
        temporaryEffects.removeIf(effect -> effect.kind() == TemporaryEffect.Kind.ATTACK_BUFF);
        return bonus;
    }

    // --- cards --------------------------------------------------------------

    Card removeFromHand(int handIndex) {
        return hand.remove(handIndex);
    }

    void discard(Card card) {
        discard.add(card);
    }

    boolean hasPokemonInDiscard() {
        return discard.stream().anyMatch(card -> card.nature() == CardNature.POKEMON);
    }

    boolean hasPokemonInDiscardAt(int discardIndex) {
        return discardIndex >= 0 && discardIndex < discard.size()
                && discard.get(discardIndex).nature() == CardNature.POKEMON;
    }

    /** Rappel: the chosen Pokémon of the discard pile goes back to the hand. */
    void returnPokemonFromDiscard(int discardIndex) {
        if (!hasPokemonInDiscardAt(discardIndex)) {
            throw new IllegalArgumentException("no Pokémon at discard index " + discardIndex);
        }
        hand.add(discard.remove(discardIndex));
    }


    @Override
    public String toString() {
        return hero.name() + " " + currentHp + " HP, " + availableMana + "/" + maxMana + " mana, board "
                + board + ", hand " + hand.size();
    }
}
