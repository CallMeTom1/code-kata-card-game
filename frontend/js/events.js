// Turns engine MatchEvents into log lines. Pure formatting: every number comes from the event.

import { card, getCatalog, hero } from './game-state.js';

const cardName = id => {
    try { return card(id).name; } catch { return id; }
};

/**
 * @param event    a MatchEvent as JSON ({type, ...fields})
 * @param sideName function side -> display name
 * @returns a log line, or null for events that are not worth a line
 */
export function describe(event, sideName) {
    const who = side => sideName(side);
    switch (event.type) {
        case 'MatchStarted': return `Match: ${hero(event.hero1).name} vs ${hero(event.hero2).name} — ${who(event.firstPlayer)} starts`;
        case 'StartingHand': return null;
        case 'TurnStarted': return `— Turn ${event.turn}: ${who(event.player)} —`;
        case 'PhaseChanged': return null;
        case 'CardDrawn': return null;
        case 'DeckEmpty': return `${who(event.player)} has no card left to draw`;
        case 'ManaChanged': return null;
        case 'CardPlayed': return `${who(event.player)} plays ${cardName(event.cardId)} (${event.manaCost} mana)`;
        case 'PokemonEntered': return `${cardName(event.cardId)} enters the Board (${event.attack}/${event.hp})`;
        case 'HeroPowerUsed': return `${who(event.player)} uses ${powerName(event.powerId)}`;
        case 'AttackDeclared': return `${cardName(event.attackerId)} attacks ${event.targetId ? cardName(event.targetId) : 'the Hero'}`;
        case 'PokemonDamaged': return `${cardName(event.pokemonId)} takes ${event.amount} (${event.remainingHp} HP left)`;
        case 'HeroDamaged': return `${who(event.player)} takes ${event.taken} from ${sourceName(event.source)}` +
            (event.blockedBy?.length
                ? ` (blocked by ${event.blockedBy.map(sourceName).join(' + ')})`
                : '') + ` → ${event.remainingHp} HP`;
        case 'HeroHealed': return `${who(event.player)} heals ${event.amount} from ${sourceName(event.source)} → ${event.hp} HP`;
        case 'EffectQueued': return `${who(event.player)}: ${event.effect.kind === 'DAMAGE_REDUCTION' ? '🛡 damage reduction' : '⚔ attack buff'} ${event.effect.value} from ${sourceName(event.effect.source)}`;
        case 'PokemonReturnedToHand': return `${cardName(event.cardId)} returns to ${who(event.player)}'s hand`;
        case 'PokemonDefeated': return `${cardName(event.pokemonId)} is defeated`;
        case 'TurnEnded': return null;
        case 'MatchEnded': return `Match over after ${event.result.turns} turns: ${event.result.reason}`;
        default: return null;
    }
}

const powerName = id => getCatalog().heroes.find(h => h.power.id === id)?.power.name ?? id;

/** Display name of an effect source: a Hero Power id or a card id. */
export const sourceName = id => {
    if (!id) return 'unknown';
    const power = getCatalog().heroes.find(h => h.power.id === id);
    return power ? power.power.name : cardName(id);
};
