// Frontend game state: the catalog and the latest GameView, both received from the Java engine.
//
// Nothing here is a game rule. Card/Hero values come from GET /api/catalog; the match state is
// the last `view` returned by the server (always seen from the human side). Helpers below only
// *look up* the engine's legalActions list — they never decide legality themselves.

let catalog = null;
const cardsById = new Map();
const heroesById = new Map();

export function setCatalog(data) {
    catalog = data;
    cardsById.clear();
    heroesById.clear();
    data.cards.forEach(c => cardsById.set(c.id, c));
    data.heroes.forEach(h => heroesById.set(h.id, h));
}

export const getCatalog = () => catalog;

export function card(id) {
    const found = cardsById.get(id);
    if (!found) throw new Error(`unknown card id: ${id}`);
    return found;
}

export function hero(id) {
    const found = heroesById.get(id);
    if (!found) throw new Error(`unknown hero id: ${id}`);
    return found;
}

// --- the current match (presentation container around the engine's view) -------------------

export const match = {
    id: null,
    seed: null,
    humanSide: 1,
    view: null,
    result: null,
};

export function applyResponse(response) {
    match.id = response.matchId;
    match.seed = response.seed;
    match.humanSide = response.humanSide;
    match.view = response.view;
    match.result = response.result;
}

// --- read-only lookups in the engine's legalActions ------------------------------------------

export const legalActions = () => match.view?.legalActions ?? [];

export const legalPlays = handIndex =>
    legalActions().filter(a => a.type === 'PlayCard' && a.handIndex === handIndex);

export const legalAttacks = attackerIndex =>
    legalActions().filter(a => a.type === 'Attack' && a.attackerIndex === attackerIndex);

export const findLegal = predicate => legalActions().find(predicate) ?? null;

export const humanCanAct = () => legalActions().length > 0;
