// Thin fetch wrappers around the Java server's JSON API. No game logic.

async function request(method, path, body) {
    const response = await fetch(path, {
        method,
        headers: body === undefined ? {} : { 'Content-Type': 'application/json' },
        body: body === undefined ? undefined : JSON.stringify(body),
    });
    const json = await response.json().catch(() => ({}));
    if (!response.ok) {
        throw new Error(json.error || `HTTP ${response.status}`);
    }
    return json;
}

export const api = {
    catalog: () => request('GET', '/api/catalog'),
    /** setup = { humanHero, humanDeck, aiHero, aiDeck, aiBot, seed? } */
    createMatch: setup => request('POST', '/api/matches', setup),
    getMatch: id => request('GET', `/api/matches/${encodeURIComponent(id)}`),
    submitAction: (id, action) => request('POST', `/api/matches/${encodeURIComponent(id)}/actions`, action),
    /** config = { p1: {hero, deck, bot}, p2: {...}, matches, seed? } */
    simulate: config => request('POST', '/api/simulations', config),
};
