// Pure presentation: turns the engine's GameView (+ UI selection) into DOM. No rules here —
// what is playable/targetable is read from the view's legalActions.

import { card, getCatalog, hero, legalActions, legalAttacks, legalPlays, match } from './game-state.js';
import { artworkUrl } from './pokeapi.js';

const $ = id => document.getElementById(id);

export const esc = value => String(value).replace(/[&<>"']/g, c =>
    ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

// Items have no PokéAPI entry: a simple glyph per category is their "artwork".
const ITEM_GLYPH = { ATTACK: '⚡', DEFENSE: '🛡', UTILITY: '✚', RESOURCE: '◆' };

export function artwork(id, isPokemon, category) {
    if (!isPokemon) return `<div class="art art--item">${ITEM_GLYPH[category] || '★'}</div>`;
    return `<div class="art"><img data-art="${esc(id)}" alt="${esc(id)}" hidden>` +
        `<span class="art-fallback">${esc(id.charAt(0).toUpperCase())}</span></div>`;
}

function cardHtml(def, { location, index, selected = false, playable = false }) {
    const isPokemon = def.nature === 'POKEMON';
    const classes = ['card', isPokemon ? 'card--pokemon' : 'card--item', `cat--${def.category.toLowerCase()}`];
    if (selected) classes.push('is-selected');
    classes.push(playable ? 'is-playable' : 'is-unplayable');
    return `<div class="${classes.join(' ')}" data-location="${location}" data-index="${index}" tabindex="0">
        <span class="cost">${def.manaCost}</span>
        ${artwork(def.id, isPokemon, def.category)}
        <div class="card-name">${esc(def.name)}</div>
        <div class="card-category">${isPokemon ? 'Pokémon' : 'Item'} · ${def.category}</div>
        ${def.text ? `<div class="card-text">${esc(def.text)}</div>` : ''}
        ${isPokemon ? `<span class="stat stat--attack">${def.attack}</span><span class="stat stat--hp">${def.maxHp}</span>` : ''}
    </div>`;
}

/** Legal actions of the current selection that target an opposing slot (or -1 = the Hero). */
function selectionTargets(ui) {
    if (!ui.selected) return [];
    const actions = ui.selected.location === 'board' ? legalAttacks(ui.selected.index)
        : legalPlays(ui.selected.index).filter(() => card(handIds()[ui.selected.index]).target === 'OPPOSING_POKEMON');
    return actions.map(a => a.targetIndex);
}

const handIds = () => match.view.self.hand;

function minionHtml(inPlay, index, owner, ui, targets) {
    const def = card(inPlay.cardId);
    const damaged = inPlay.currentHp < inPlay.maxHp;
    const selected = owner === 'self' && ui.selected?.location === 'board' && ui.selected.index === index;
    const classes = ['minion', `cat--${def.category.toLowerCase()}`];
    const mayAttack = owner === 'self' && legalAttacks(index).length > 0;
    if (mayAttack) classes.push('can-attack');
    if (owner === 'self' && !inPlay.canAttack) classes.push('is-sleeping');
    if (selected) classes.push('is-selected');
    if (owner === 'opponent' && targets.includes(index)) classes.push('is-targetable');
    return `<div class="${classes.join(' ')}" data-location="${owner === 'self' ? 'board' : 'enemy-board'}" data-index="${index}" data-card="${esc(inPlay.cardId)}"
                 tabindex="0" title="${esc(def.name)}${def.text ? ' — ' + esc(def.text) : ''}">
        ${artwork(def.id, true, def.category)}
        <div class="minion-name">${esc(def.name)}</div>
        ${hpBar(inPlay.currentHp, inPlay.maxHp)}
        ${owner === 'self' && !inPlay.canAttack ? '<span class="status-badge" title="Cannot attack this turn">Zz</span>' : ''}
        <span class="stat stat--attack">${inPlay.attack}</span>
        <span class="stat stat--hp ${damaged ? 'is-damaged' : ''}">${inPlay.currentHp}</span>
    </div>`;
}

function hpBar(hp, max) {
    const ratio = Math.max(0, Math.min(1, hp / max));
    const level = ratio > .6 ? 'high' : ratio > .3 ? 'mid' : 'low';
    return `<div class="hp-bar hp-bar--${level}"><i style="width:${ratio * 100}%"></i></div>`;
}

function boardHtml(player, owner, ui, targets) {
    const slots = [];
    for (let i = 0; i < getCatalog().boardSize; i++) {
        const inPlay = player.board[i];
        slots.push(inPlay ? minionHtml(inPlay, i, owner, ui, targets) : '<div class="slot slot--empty"><span></span></div>');
    }
    return slots.join('');
}

export function heroHtml(player, owner, targetable = false) {
    const def = hero(player.heroId);
    const effects = player.temporaryEffects.map(effect =>
        `<span class="effect-badge" title="${esc(effect.kind)} from ${esc(effect.source)}">
            ${effect.kind === 'DAMAGE_REDUCTION' ? '🛡' : '⚔'} ${effect.value}</span>`).join('');
    return `<div class="hero ${targetable ? 'is-targetable' : ''}" data-location="${owner === 'self' ? 'hero' : 'enemy-hero'}" tabindex="0">
        ${artwork(def.id, true, 'HERO')}
        <div class="hero-plate">
            <div class="hero-name">${esc(def.name)}</div>
            ${hpBar(player.currentHp, player.maxHp)}
        </div>
        <span class="hero-hp ${player.currentHp < player.maxHp ? 'is-damaged' : ''}">${player.currentHp}</span>
        <div class="effects">${effects}</div>
    </div>`;
}

function powerHtml(player, owner) {
    const power = hero(player.heroId).power;
    const usable = owner === 'self' && legalActions().some(a => a.type === 'UseHeroPower');
    return `<button type="button" class="hero-power cat--${power.category.toLowerCase()}
                ${player.heroPowerUsedThisTurn ? 'is-used' : ''} ${usable ? 'is-playable' : ''}"
                data-location="${owner === 'self' ? 'power' : 'enemy-power'}" title="${esc(power.text)}"
                ${usable ? '' : 'disabled'}>
        <span class="cost">${power.manaCost}</span>
        <span class="power-name">${esc(power.name)}</span>
        <span class="power-text">${esc(power.text)}</span>
        <span class="power-state">${player.heroPowerUsedThisTurn ? 'Used' : usable ? 'Ready' : ''}</span>
    </button>`;
}

function manaHtml(player) {
    const crystals = [];
    for (let i = 0; i < getCatalog().maxMana; i++) {
        const state = i < player.availableMana ? 'full' : i < player.maxMana ? 'spent' : 'locked';
        crystals.push(`<span class="crystal crystal--${state}"></span>`);
    }
    return `<span class="mana-count">${player.availableMana}/${player.maxMana}</span>${crystals.join('')}`;
}

function pilesHtml(player) {
    return `<span title="Cards left in deck">🂠 ${player.deckCount}</span>
            <span title="${esc(player.discard.map(id => card(id).name).join(', ') || 'empty')}">🗑 ${player.discard.length}</span>`;
}

export function resultText(result, humanSide) {
    if (!result) return '';
    if (result.outcome === 'DRAW') return 'Draw';
    const winner = result.outcome === 'PLAYER_ONE_WINS' ? 1 : 2;
    return winner === humanSide ? 'You win!' : 'You lose';
}

export function render(ui) {
    const view = match.view;
    if (!view) return;
    const self = view.self;
    const opponent = view.opponent;
    const selfActive = view.activeSide === self.side && !view.over;
    const targets = selectionTargets(ui);

    $('opponent-hero').innerHTML = heroHtml(opponent, 'opponent', targets.includes(-1) && ui.selected?.location === 'board');
    $('opponent-power').innerHTML = powerHtml(opponent, 'opponent');
    $('opponent-mana').innerHTML = manaHtml(opponent);
    $('opponent-piles').innerHTML = pilesHtml(opponent);
    $('opponent-hand').innerHTML = '<span class="card-back"></span>'.repeat(opponent.handSize ?? 0);
    $('opponent-board').innerHTML = boardHtml(opponent, 'opponent', ui, targets);

    $('player-board').innerHTML = boardHtml(self, 'self', ui, []);
    $('player-hero').innerHTML = heroHtml(self, 'self');
    $('player-power').innerHTML = powerHtml(self, 'self');
    $('player-mana').innerHTML = manaHtml(self);
    $('player-piles').innerHTML = pilesHtml(self);
    $('player-hand').innerHTML = self.hand.map((id, index) => cardHtml(card(id), {
        location: 'hand',
        index,
        selected: ui.selected?.location === 'hand' && ui.selected.index === index,
        playable: legalPlays(index).length > 0,
    })).join('');

    const who = hero(opponent.heroId).name;
    $('turn-indicator').innerHTML = view.over
        ? `<strong>Match over — ${esc(resultText(match.result, match.humanSide))}</strong>
           <span class="turn-phase">${esc(match.result?.reason ?? '')}</span>`
        : `<span class="turn-number">Turn ${view.turnNumber} / ${getCatalog().maxTurns}</span>
           <span class="turn-owner ${selfActive ? 'is-self' : 'is-opponent'}">${selfActive ? 'Your turn' : 'AI turn'}</span>
           <span class="turn-phase">${view.phase}</span>`;
    $('end-turn').disabled = !legalActions().some(a => a.type === 'EndTurn') || ui.busy;
    const aiPlaying = !view.over && (ui.aiPlaying || !selfActive);
    const arena = document.querySelector('.arena');
    arena.classList.toggle('is-self-turn', selfActive && !ui.aiPlaying);
    arena.classList.toggle('is-ai-turn', aiPlaying);
    $('player-hero').classList.toggle('is-active', selfActive && !ui.aiPlaying);
    $('opponent-hero').classList.toggle('is-active', aiPlaying);
    if (aiPlaying) {
        $('turn-indicator').innerHTML = `<span class="turn-number">Turn ${view.turnNumber} / ${getCatalog().maxTurns}</span>
           <span class="turn-owner is-opponent"><span class="thinking-dot"></span>AI is thinking…</span>
           <span class="turn-phase">${esc(who)}</span>`;
    }

    loadArtwork();
}

/** Fills every pending <img data-art> with PokéAPI artwork; keeps the letter fallback on failure. */
export function loadArtwork() {
    document.querySelectorAll('img[data-art]').forEach(img => {
        artworkUrl(img.dataset.art).then(url => {
            if (!url || !img.isConnected) return;
            img.onload = () => {
                img.hidden = false;
                img.nextElementSibling?.remove();
            };
            img.onerror = () => img.remove();
            img.src = url;
        });
    });
}

// --- visual feedback ------------------------------------------------------------------------

export function flash(element, kind, amount) {
    if (!element) return;
    if (kind === 'damage' && amount === 0) kind = 'block';
    element.classList.remove('fx-damage', 'fx-heal', 'fx-block');
    void element.offsetWidth; // restart the animation
    element.classList.add(`fx-${kind}`);
    if (amount !== undefined) {
        const bubble = document.createElement('span');
        bubble.className = `fx-number fx-number--${kind}`;
        bubble.textContent = kind === 'block' ? '🛡 0' : (kind === 'heal' ? '+' : '-') + amount;
        element.appendChild(bubble);
        bubble.addEventListener('animationend', () => bubble.remove());
    }
}

export function lunge(attacker, target) {
    if (!attacker || !target) return;
    const a = attacker.getBoundingClientRect();
    const t = target.getBoundingClientRect();
    attacker.style.setProperty('--dx', `${(t.left - a.left) * 0.6}px`);
    attacker.style.setProperty('--dy', `${(t.top - a.top) * 0.6}px`);
    attacker.classList.add('fx-lunge');
    attacker.addEventListener('animationend', () => attacker.classList.remove('fx-lunge'), { once: true });
}

export function log(message, container = $('action-log'), max = 10) {
    const entry = document.createElement('div');
    entry.className = 'log-entry';
    if (/Turn \d/.test(message) && message.length < 60) entry.classList.add('log-entry--turn');
    if (message.startsWith('✖')) entry.classList.add('log-entry--error');
    entry.textContent = message;
    container.prepend(entry);
    while (container.children.length > max) container.lastChild.remove();
}
