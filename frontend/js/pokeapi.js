// Visual enrichment ONLY: fetches and caches artwork URLs from PokéAPI, keyed by our stable ids.
// No PokéAPI stat (HP, attack, types...) is ever returned or stored — gameplay comes from the engine.

const API = 'https://pokeapi.co/api/v2/pokemon/';
const STORAGE_KEY = 'pokeapi-artwork-v1';

const memory = new Map();          // id -> Promise<string|null>
const persisted = loadPersisted(); // id -> url (survives reloads, URLs only)

function loadPersisted() {
    try {
        return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {};
    } catch {
        return {};
    }
}

function persist(id, url) {
    persisted[id] = url;
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(persisted));
    } catch {
        // storage unavailable (private mode, file://): the in-memory cache is enough
    }
}

async function fetchArtwork(id) {
    const response = await fetch(API + encodeURIComponent(id));
    if (!response.ok) throw new Error(`PokéAPI ${response.status} for ${id}`);
    const data = await response.json();
    const sprites = data.sprites || {};
    const other = sprites.other || {};
    return (other['official-artwork'] && other['official-artwork'].front_default)
        || (other.home && other.home.front_default)
        || sprites.front_default
        || null;
}

/** Resolves to an artwork URL for a Pokémon id, or null when unavailable. Never rejects. */
export function artworkUrl(id) {
    if (persisted[id]) return Promise.resolve(persisted[id]);
    if (!memory.has(id)) {
        const request = fetchArtwork(id)
            .then(url => {
                if (url) persist(id, url);
                return url;
            })
            .catch(error => {
                console.warn(error.message);
                memory.delete(id); // allow a retry on the next render
                return null;
            });
        memory.set(id, request);
    }
    return memory.get(id);
}
