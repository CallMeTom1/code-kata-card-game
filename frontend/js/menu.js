// Start menu (PLAY / SIMULATION), simulation results and the event replay of the first match.
// Options are filled from the Java catalog; results and replay only display engine output.

import { api } from './api.js';
import { getCatalog, hero } from './game-state.js';
import { describe } from './events.js';
import { artwork, esc, loadArtwork, log } from './renderer.js';

const $ = id => document.getElementById(id);

const heroOptions = () => getCatalog().heroes
    .map(h => `<option value="${esc(h.id)}">${esc(h.name)} — ${esc(h.power.name)}</option>`).join('');
const deckOptions = () => getCatalog().decks
    .map(d => `<option value="${esc(d.id)}">${esc(d.id.charAt(0) + d.id.slice(1).toLowerCase())}</option>`).join('');
const botOptions = () => getCatalog().bots.map(b => `<option value="${esc(b)}">${esc(b)}</option>`).join('');

/** Fills every <select data-options="hero|deck|bot"> and sets distinct defaults. */
export function fillMenu() {
    const builders = { hero: heroOptions, deck: deckOptions, bot: botOptions };
    document.querySelectorAll('select[data-options]').forEach(select => {
        select.innerHTML = builders[select.dataset.options]();
        if (select.dataset.default) select.value = select.dataset.default;
    });
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
    return `<table class="stats">
        <tr><th></th><th>Player 1<br><small>${label(1)}</small></th><th>Player 2<br><small>${label(2)}</small></th></tr>
        <tr><td>Wins</td><td>${s.winsSide1}</td><td>${s.winsSide2}</td></tr>
        <tr><td>Win rate</td><td>${percent(s.winRateSide1)}</td><td>${percent(s.winRateSide2)}</td></tr>
        <tr><td>Avg. damage dealt to the opposing Hero</td><td>${s.averageDamageSide1.toFixed(1)}</td><td>${s.averageDamageSide2.toFixed(1)}</td></tr>
        <tr><td>Draws</td><td colspan="2">${s.draws} (${percent(s.drawRate)})</td></tr>
        <tr><td>Average match length</td><td colspan="2">${s.averageTurns.toFixed(1)} turns</td></tr>
        <tr><td>Matches · seed</td><td colspan="2">${s.matches} · ${esc(result.seed)}</td></tr>
    </table>`;
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
                <div>${esc(sideName(side))}</div><div class="replay-hp" id="replay-hp-${side}">${hp[side]} HP</div></div>`).join('')}
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
        [1, 2].forEach(side => { $(`replay-hp-${side}`).textContent = `${hp[side]} HP`; });
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
        output.innerHTML = '<p>Running…</p>';
        $('replay').hidden = true;
        try {
            const result = await api.simulate(config);
            output.innerHTML = statsHtml(config, result) +
                '<button type="button" id="watch-replay" class="primary">Watch the first match</button>';
            $('watch-replay').addEventListener('click', () => startReplay(result.firstMatchEvents));
        } catch (error) {
            output.innerHTML = `<p class="error">✖ ${esc(error.message)}</p>`;
        }
    });
}

export const stopReplay = () => clearInterval(replayTimer);
