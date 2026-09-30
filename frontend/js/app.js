// Entry point: loads the catalog from Java, shows the start menu, and switches between the
// Human vs AI battlefield and the Simulation screen.

import { api } from './api.js';
import { setCatalog } from './game-state.js';
import { bindActions, rerender, showResponse, ui } from './actions.js';
import { bindSimulation, fillMenu, playSetup, stopReplay } from './menu.js';

const $ = id => document.getElementById(id);
const screens = ['menu', 'battle', 'simulation'];

function show(screen) {
    screens.forEach(name => { $(name).hidden = name !== screen; });
    if (screen !== 'simulation') stopReplay();
}

async function start() {
    try {
        setCatalog(await api.catalog());
    } catch (error) {
        $('menu-error').textContent = `Cannot reach the Java server: ${error.message}`;
        return;
    }
    fillMenu();
    bindActions();
    bindSimulation();

    $('play-form').addEventListener('submit', async event => {
        event.preventDefault();
        $('menu-error').textContent = '';
        try {
            const response = await api.createMatch(playSetup(event.target));
            $('action-log').innerHTML = '';
            show('battle');
            ui.busy = true;
            await showResponse(response);
        } catch (error) {
            $('menu-error').textContent = `✖ ${error.message}`;
        } finally {
            ui.busy = false;
            rerender();
        }
    });
    $('open-simulation').addEventListener('click', () => show('simulation'));
    document.querySelectorAll('[data-back]').forEach(button => button.addEventListener('click', () => show('menu')));
    show('menu');
}

start();
