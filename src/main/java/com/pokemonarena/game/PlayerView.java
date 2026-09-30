package com.pokemonarena.game;

import com.pokemonarena.board.Board;
import com.pokemonarena.cards.Card;
import com.pokemonarena.hero.Hero;

import java.util.List;

/**
 * Immutable snapshot of one Hero's match state. Every collection is an immutable copy, and
 * cards, Heroes and temporary effects are immutable themselves, so nothing reachable from a
 * view can change the match: {@link Match#perform(Action)} stays the only way to mutate it.
 */
public record PlayerView(
        int side,
        Hero hero,
        int currentHp,
        int maxHp,
        int availableMana,
        int maxMana,
        boolean heroPowerUsedThisTurn,
        List<Card> hand,
        List<PokemonView> board,
        int deckCount,
        List<Card> discard,
        List<TemporaryEffect> temporaryEffects,
        int damageDealtToOpposingHero) {

    public PlayerView {
        hand = List.copyOf(hand);
        board = List.copyOf(board);
        discard = List.copyOf(discard);
        temporaryEffects = List.copyOf(temporaryEffects);
    }

    static PlayerView of(int side, PlayerState state) {
        return new PlayerView(side, state.hero(), state.currentHp(), state.hero().startingHp(),
                state.availableMana(), state.maxMana(), state.heroPowerUsedThisTurn(), state.hand(),
                state.board().pokemon().stream().map(PokemonView::of).toList(), state.deck().size(),
                state.discard(), state.temporaryEffects(), state.damageDealtToOpposingHero());
    }

    public String name() {
        return hero.name();
    }

    public boolean isBoardFull() {
        return board.size() >= Board.MAX_POKEMON;
    }
}
