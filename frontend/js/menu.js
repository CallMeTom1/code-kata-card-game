// Start menu (PLAY / SIMULATION), simulation results and the event replay of the first match.
// Options are filled from the Java catalog; results and replay only display engine output.

import { api } from './api.js';
import { card, getCatalog, hero } from './game-state.js';
import { describe } from './events.js';
import { artwork, esc, loadArtwork, log } from './renderer.js';

const $ = id => document.getElementById(id);

const heroOptions = () => getCatalog().heroes
    .map(h => `<option value="${esc(h.id)}">${esc(h.name)} — ${esc(h.power.name)}</option>`).join('');
const deckOptions = () => getCatalog().decks
    .map(d => `<option value="${esc(d.id)}">${esc(d.id.charAt(0) + d.id.slice(1).toLowerCase())}</option>`).join('');
const botOptions = () => getCatalog().bots.map(b => `<option value="${esc(b)}">${esc(b)}</option>`).join('');

// Presentation-only flavour text (documentation of the deck identity, never used by rules).
const DECK_BLURB = {
    AGGRO: 'Cheap Pokémon and burn. End it fast.',
    CONTROL: 'Walls, heals and shields. Outlast them.',
    ENERGY: 'Ramp mana, then drop giants.',
    BALANCED: 'A bit of everything.',
};
const BOT_BLURB = { Aggressive: 'Goes face, trades greedily.', Defensive: 'Protects its Hero first.' };
const CATEGORIES = ['ATTACK', 'DEFENSE', 'UTILITY', 'RESOURCE'];
const title = id => id.charAt(0) + id.slice(1).toLowerCase();

function heroChoice(h) {
    return `<button type="button" class="choice choice--hero cat--${h.power.category.toLowerCase()}" data-value="${esc(h.id)}">
        ${artwork(h.id, true, 'HERO')}
        <span class="choice-name">${esc(h.name)}</span>
        <span class="choice-power"><b>${h.power.manaCost}</b> ${esc(h.power.name)}</span>
        <span class="choice-text">${esc(h.power.text)}</span>
    </button>`;
}

function deckChoice(d) {
    const counts = Object.fromEntries(CATEGORIES.map(c => [c, 0]));
    d.cards.forEach(id => { counts[card(id).category]++; });
    const bar = CATEGORIES.map(c => counts[c]
        ? `<i class="cat--${c.toLowerCase()}" style="flex:${counts[c]}" title="${title(c)}: ${counts[c]}"></i>` : '').join('');
    return `<button type="button" class="choice choice--deck" data-value="${esc(d.id)}">
        <span class="choice-name">${esc(title(d.id))}</span>
        <span class="choice-text">${esc(DECK_BLURB[d.id] ?? '')}</span>
        <span class="deck-mix">${bar}</span>
    </button>`;
}

const botChoice = b => `<button type="button" class="choice choice--bot" data-value="${esc(b)}">
        <span class="choice-name">${esc(b)}</span><span class="choice-text">${esc(BOT_BLURB[b] ?? '')}</span></button>`;

function fillChoices(grid) {
    const c = getCatalog();
    const items = { hero: c.heroes.map(heroChoice), deck: c.decks.map(deckChoice), bot: c.bots.map(botChoice) };
    grid.innerHTML = items[grid.dataset.choice].join('') +
        `<input type="hidden" name="${grid.dataset.name}" value="${esc(grid.dataset.default)}">`;
    const select = value => {
        grid.querySelector('input').value = value;
        grid.querySelectorAll('.choice').forEach(b => b.classList.toggle('is-chosen', b.dataset.value === value));
    };
    grid.addEventListener('click', event => {
        const choice = event.target.closest('.choice');
        if (choice) select(choice.dataset.value);
    });
    select(grid.dataset.default);
}

/** Fills the visual choice grids and every <select data-options="hero|deck|bot"> with distinct defaults. */
export function fillMenu() {
    const builders = { hero: heroOptions, deck: deckOptions, bot: botOptions };
    document.querySelectorAll('select[data-options]').forEach(select => {
        select.innerHTML = builders[select.dataset.options]();
        if (select.dataset.default) select.value = select.dataset.default;
    });
    document.querySelectorAll('.choice-grid').forEach(fillChoices);
    loadArtwork();
}

const value = (form, name) => form.elements[name].value;
const seedOf = form => value(form, 'seed').trim() || undefined;

export function playSetup(form) {
    return {
        humanHero: value(form, 'humanHero'),
        humanDeck: value(form, 'humanDeck'),
        aiHero: value(form, 'aiHero'),
        aiDeck: value(form, 'aiDeck'),
        aiBot: value(form, 'aiBot'),
        seed: seedOf(form),
    };
}

function simulationConfig(form) {
    const side = n => ({ hero: value(form, `p${n}Hero`), deck: value(form, `p${n}Deck`), bot: value(form, `p${n}Bot`) });
    return { p1: side(1), p2: side(2), matches: Number(value(form, 'matches')), seed: seedOf(form) };
}

const percent = rate => `${(rate * 100).toFixed(1)}%`;

function statsHtml(config, result) {
    const s = result.stats;
    const label = n => `${esc(hero(config[`p${n}`].hero).name)} · ${esc(config[`p${n}`].deck)} · ${esc(config[`p${n}`].bot)}`;
    const side = n => `<div class="stat-side stat-side--p${n}">
            ${artwork(config[`p${n}`].hero, true, 'HERO')}
            <div class="stat-label">Player ${n}<small>${label(n)}</small></div>
            <div class="stat-big">${percent(s[`winRateSide${n}`])}</div>
            <div class="stat-sub">${s[`winsSide${n}`]} wins · ${s[`averageDamageSide${n}`].toFixed(1)} avg. damage</div>
        </div>`;
    return `<div class="stats-head">${side(1)}<div class="versus versus--small">VS</div>${side(2)}</div>
        <div class="winbar" title="P1 / draws / P2">
            <i class="winbar-p1" style="flex:${s.winsSide1}"></i><i class="winbar-draw" style="flex:${s.draws}"></i><i class="winbar-p2" style="flex:${s.winsSide2}"></i>
        </div>
        <div class="stat-tiles">
            <div class="tile"><b>${s.draws}</b><span>Draws (${percent(s.drawRate)})</span></div>
            <div class="tile"><b>${s.averageTurns.toFixed(1)}</b><span>Avg. turns</span></div>
            <div class="tile"><b>${s.matches}</b><span>Matches</span></div>
            <div class="tile"><b>${esc(result.seed)}</b><span>Seed</span></div>
        </div>`;
}

let replayTimer = null;

/** Replays the first match from its events only: Hero HP and the log, turn by turn. */
function startReplay(events) {
    clearInterval(replayTimer);
    const started = events.find(e => e.type === 'MatchStarted');
    const heroes = { 1: hero(started.hero1), 2: hero(started.hero2) };
    const hp = { 1: heroes[1].maxHp, 2: heroes[2].maxHp };
    const sideName = side => `${heroes[side].name} (P${side})`;
    const box = $('replay');
    box.hidden = false;
    box.innerHTML = `<div class="replay-heroes">
            ${[1, 2].map(side => `<div class="replay-hero">${artwork(heroes[side].id, true, 'HERO')}
                <div>${esc(sideName(side))}</div><div class="replay-hp" id="replay-hp-${side}">${hp[side]} HP</div>
                <div class="hp-bar"><i id="replay-bar-${side}" style="width:100%"></i></div></div>`).join('')}
        </div>
        <div class="replay-turn" id="replay-turn">Starting…</div>
        <div class="replay-log" id="replay-log"></div>`;
    loadArtwork();
    let i = 0;
    replayTimer = setInterval(() => {
        if (i >= events.length) return clearInterval(replayTimer);
        const event = events[i++];
        if (event.type === 'HeroDamaged') hp[event.player] = event.remainingHp;
        if (event.type === 'HeroHealed') hp[event.player] = event.hp;
        if (event.type === 'TurnStarted') $('replay-turn').textContent = `Turn ${event.turn} — ${sideName(event.player)}`;
        if (event.type === 'MatchEnded') $('replay-turn').textContent = `Result after ${event.result.turns} turns: ${event.result.reason}`;
        [1, 2].forEach(side => {
            $(`replay-hp-${side}`).textContent = `${hp[side]} HP`;
            $(`replay-bar-${side}`).style.width = `${100 * hp[side] / heroes[side].maxHp}%`;
        });
        const line = describe(event, sideName);
        if (line) log(line, $('replay-log'), 400);
    }, 60);
}

export function bindSimulation() {
    const form = $('simulation-form');
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const config = simulationConfig(form);
        const output = $('simulation-results');
        output.innerHTML = '<div class="running"><span class="spinner"></span>Running simulation…</div>';
        $('replay').hidden = true;
        try {
            const result = await api.simulate(config);
            output.innerHTML = statsHtml(config, result) +
                '<button type="button" id="watch-replay" class="primary">▶ Watch the first match</button>';
            loadArtwork();
            $('watch-replay').addEventListener('click', () => startReplay(result.firstMatchEvents));
        } catch (error) {
            output.innerHTML = `<p class="error">✖ ${esc(error.message)}</p>`;
        }
    });
}

export const stopReplay = () => clearInterval(replayTimer);
