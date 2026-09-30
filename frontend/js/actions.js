// Interaction handling: turns clicks into engine Actions (same JSON shape as
// com.pokemonarena.game.Action) and sends them to the Java server. Only actions listed in the
// engine's legalActions are offered; the engine still validates everything it receives.

import { api } from './api.js';
import { applyResponse, card, findLegal, hero, legalAttacks, legalPlays, match } from './game-state.js';
import { describe } from './events.js';
import { esc, flash, log, lunge, render } from './renderer.js';

const EVENT_DELAY_MS = 220;
const same = (a, b) => JSON.stringify(a) === JSON.stringify(b);

export const ui = { selected: null, busy: false, aiPlaying: false };

export const rerender = () => render(ui);

const sideName = side => side === match.humanSide
    ? `${hero(match.view.self.heroId).name} (you)`
    : hero(match.view.opponent.heroId).name;

/** Plays the events returned by the engine: log lines plus hit/heal feedback on the Heroes. */
export async function playEvents(events) {
    for (const event of events) {
        const line = describe(event, sideName);
        if (line) log(line);
        const heroEl = side => document.querySelector(
            `[data-location="${side === match.humanSide ? 'hero' : 'enemy-hero'}"]`);
        if (event.type === 'HeroDamaged') flash(heroEl(event.player), 'damage', event.taken);
        else if (event.type === 'HeroHealed') flash(heroEl(event.player), 'heal', event.amount);
        else if (event.type === 'PokemonDamaged') {
            const loc = event.owner === match.humanSide ? 'board' : 'enemy-board';
            flash(document.querySelector(`[data-location="${loc}"][data-card="${event.pokemonId}"]`), 'damage', event.amount);
        }
        else if (!line) continue;
        await new Promise(resolve => setTimeout(resolve, EVENT_DELAY_MS));
    }
}

/** Applies a server response (view + events) and animates it. */
export async function showResponse(response) {
    const aiTurn = response.events.some(e => e.type === 'TurnStarted' && e.player !== match.humanSide);
    applyResponse(response);
    ui.selected = null;
    if (aiTurn) ui.aiPlaying = true;
    rerender();
    await playEvents(response.events);
    ui.aiPlaying = false;
}

async function send(action) {
    if (ui.busy) return;
    ui.busy = true;
    ui.aiPlaying = action.type === 'EndTurn';
    rerender();
    try {
        await showResponse(await api.submitAction(match.id, action));
    } catch (error) {
        log(`✖ ${error.message}`);
    } finally {
        ui.busy = false;
        ui.aiPlaying = false;
        rerender();
    }
}

// Lets the attack animation play before the new view replaces the Board.
function attackWithLunge(action, targetEl) {
    lunge(document.querySelector(`[data-location="board"][data-index="${action.attackerIndex}"]`), targetEl);
    flash(targetEl, 'damage');
    setTimeout(() => send(action), 320);
}

function clearSelection() {
    ui.selected = null;
    rerender();
}

/** Lets the human pick which discarded Pokémon Rappel returns (options come from legalActions). */
function pickDiscardTarget(plays) {
    const picker = document.getElementById('picker');
    const discard = match.view.self.discard;
    picker.innerHTML = `<div class="picker-box"><p>Return which Pokémon?</p>
        ${plays.map((a, i) => `<button type="button" data-choice="${i}">${esc(card(discard[a.targetIndex]).name)}</button>`).join('')}
        <button type="button" data-choice="cancel" class="secondary">Cancel</button></div>`;
    picker.hidden = false;
    picker.onclick = event => {
        const choice = event.target.closest('[data-choice]')?.dataset.choice;
        if (choice === undefined) return;
        picker.hidden = true;
        if (choice !== 'cancel') send(plays[Number(choice)]);
        else clearSelection();
    };
}

function onHandClick(index) {
    const plays = legalPlays(index);
    const def = card(match.view.self.hand[index]);
    if (plays.length === 0) {
        log(`${def.name} cannot be played now`);
        return clearSelection();
    }
    if (def.target === 'OPPOSING_POKEMON') {
        ui.selected = { location: 'hand', index };
        return rerender();
    }
    if (ui.selected?.location === 'hand' && ui.selected.index === index) {
        if (plays.length > 1) return pickDiscardTarget(plays);
        return send(plays[0]);
    }
    ui.selected = { location: 'hand', index };
    rerender();
}

/** Wires DOM events once. */
export function bindActions() {
    document.querySelector('.arena').addEventListener('click', event => {
        const el = event.target.closest('[data-location]');
        if (!el || ui.busy || !match.view) return;
        const index = Number(el.dataset.index);

        switch (el.dataset.location) {
            case 'hand':
                return onHandClick(index);
            case 'board':
                if (legalAttacks(index).length === 0) return clearSelection();
                ui.selected = { location: 'board', index };
                return rerender();
            case 'enemy-board': {
                if (!ui.selected) return;
                const action = ui.selected.location === 'board'
                    ? findLegal(a => same(a, { type: 'Attack', attackerIndex: ui.selected.index, targetIndex: index }))
                    : findLegal(a => same(a, { type: 'PlayCard', handIndex: ui.selected.index, targetIndex: index }));
                if (!action) return;
                if (action.type !== 'Attack') return send(action);
                return attackWithLunge(action, el);
            }
            case 'enemy-hero': {
                if (ui.selected?.location !== 'board') return;
                const action = findLegal(a => same(a, { type: 'Attack', attackerIndex: ui.selected.index, targetIndex: -1 }));
                if (!action) return;
                return attackWithLunge(action, el);
            }
            case 'power': {
                const action = findLegal(a => a.type === 'UseHeroPower');
                return action ? send(action) : clearSelection();
            }
            default:
                return clearSelection();
        }
    });

    document.getElementById('end-turn').addEventListener('click', () => {
        const action = findLegal(a => a.type === 'EndTurn');
        if (action) send(action);
    });

    document.addEventListener('keydown', event => {
        if (event.key === 'Escape') clearSelection();
    });
}
