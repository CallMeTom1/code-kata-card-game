package com.pokemonarena.game;

import java.util.List;

/**
 * Something that happened in a match, published by the engine to its {@link MatchListener}s.
 * Events are plain data (ids, numbers) so a logger, the statistics or a future frontend replay
 * can observe a match without containing any game rule.
 * <p>
 * Players are identified by their side, {@code 1} or {@code 2}, so mirror matches stay unambiguous.
 */
public sealed interface MatchEvent {

    record MatchStarted(String hero1, String hero2, int firstPlayer) implements MatchEvent {
    }

    record StartingHand(int player, List<String> cardIds) implements MatchEvent {
    }

    record TurnStarted(int turn, int player) implements MatchEvent {
    }

    record PhaseChanged(int turn, Phase phase) implements MatchEvent {
    }

    record CardDrawn(int player, String cardId) implements MatchEvent {
    }

    record DeckEmpty(int player) implements MatchEvent {
    }

    record ManaChanged(int player, int available, int max) implements MatchEvent {
    }

    record CardPlayed(int player, String cardId, int manaCost) implements MatchEvent {
    }

    record PokemonEntered(int player, String cardId, int attack, int hp) implements MatchEvent {
    }

    record HeroPowerUsed(int player, String powerId) implements MatchEvent {
    }

    /** {@code targetId} is {@code null} when the opposing Hero is attacked. */
    record AttackDeclared(int player, String attackerId, String targetId) implements MatchEvent {
    }

    record PokemonDamaged(int owner, String pokemonId, int amount, int remainingHp) implements MatchEvent {
    }

    /**
     * {@code source} is the card, Hero Power or attacking Pokémon id causing the damage;
     * {@code blockedBy} lists the sources of the damage reductions consumed by this hit, oldest first.
     */
    record HeroDamaged(int player, int incoming, int taken, int remainingHp, String source, List<String> blockedBy)
            implements MatchEvent {
        public HeroDamaged {
            blockedBy = List.copyOf(blockedBy);
        }
    }

    /** {@code amount} is the HP actually gained (0 at full HP); {@code source} is the healing card or power id. */
    record HeroHealed(int player, int amount, int hp, String source) implements MatchEvent {
    }

    record EffectQueued(int player, TemporaryEffect effect) implements MatchEvent {
    }

    record PokemonReturnedToHand(int player, String cardId) implements MatchEvent {
    }

    record PokemonDefeated(int owner, String pokemonId) implements MatchEvent {
    }

    record TurnEnded(int turn, int player) implements MatchEvent {
    }

    record MatchEnded(MatchResult result) implements MatchEvent {
    }
}
