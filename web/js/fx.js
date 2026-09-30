/*
 * Animation engine of the replay: draws the effects chosen by visuals.js with the Web Animations API.
 * Everything happens in an overlay layer, so the board can be re-rendered at any time.
 * Durations follow the playback speed; nothing runs when the system asks for reduced motion.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const doc = root.document;
  const CATEGORY_NAMES = { ATTACK: "Attaque", DEFENSE: "Défense", RESOURCE: "Ressource", UTILITY: "Utilitaire" };
  const BASE_MS = 900;

  const esc = (text) => String(text).replace(/[&<>"']/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;",
    '"': "&quot;", "'": "&#39;" }[c]));

  /** Large card used for the reveal and for hovering a card in hand. */
  function bigCardHtml(card) {
    return `<div class="big-card" data-cat="${esc(card.category || "")}">
      <div class="cost"><span>${card.cost}</span></div>
      <div class="big-card-art">${card.icon || "🎴"}</div>
      <div class="big-card-name">${esc(card.name)}</div>
      <div class="big-card-text">${esc(card.text || "")}</div>
      <div class="big-card-cat">${esc(CATEGORY_NAMES[card.category] || card.category || "")}</div>
    </div>`;
  }

  function create({ board, layer, speed }) {
    const reduced = () => root.matchMedia && root.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const budget = () => BASE_MS / Math.max(1, speed());
    const fast = () => speed() >= 4;

    function find(ref) {
      if (!ref) return null;
      if (ref.hero) return board.querySelector(`.hero[data-hero="${CSS.escape(ref.hero)}"]`);
      if (ref.minionId) return board.querySelector(`.minion[data-minion-id="${ref.minionId}"]`);
      return null;
    }

    function centerOf(rect) {
      const origin = layer.getBoundingClientRect();
      return { x: rect.left - origin.left + rect.width / 2, y: rect.top - origin.top + rect.height / 2 };
    }

    function center(ref) {
      const el = find(ref);
      return el ? centerOf(el.getBoundingClientRect()) : null;
    }

    function spawn(className, at, html) {
      const node = doc.createElement("div");
      node.className = className;
      if (html) node.innerHTML = html;
      node.style.left = at.x + "px";
      node.style.top = at.y + "px";
      layer.appendChild(node);
      return node;
    }

    function run(node, keyframes, options) {
      const animation = node.animate(keyframes, { fill: "forwards", easing: "ease-out", ...options });
      animation.onfinish = () => node.remove();
      return animation;
    }

    function particles(at, count, className, spread, duration, rise) {
      for (let i = 0; i < count; i++) {
        const angle = (Math.PI * 2 * i) / count + Math.random() * 0.5;
        const distance = spread * (0.6 + Math.random() * 0.6);
        const p = spawn("fx-particle " + className, at);
        run(p, [
          { transform: "translate(-50%, -50%) scale(1)", opacity: 1 },
          { transform: `translate(calc(-50% + ${Math.cos(angle) * distance}px), calc(-50% + ${Math.sin(angle) * distance - (rise || 0)}px)) scale(0.3)`, opacity: 0 },
        ], { duration, delay: Math.random() * duration * 0.2 });
      }
    }

    /** Positions that disappear with the next render (dying minions, the card leaving the hand). */
    function capture(effects) {
      const captured = {};
      effects.forEach((fx) => {
        if (fx.kind === "death") {
          const el = find({ minionId: fx.minionId });
          if (el) captured["death" + fx.minionId] = { rect: el.getBoundingClientRect(), html: el.outerHTML };
        }
        if (fx.kind === "reveal") {
          const card = [...board.querySelectorAll(`.hand[data-player="${CSS.escape(fx.player)}"] .card`)]
            .find((c) => c.dataset.name === fx.card.name);
          if (card) captured.reveal = card.getBoundingClientRect();
        }
      });
      return captured;
    }

    const handlers = {
      banner(fx) {
        if (fast()) return;
        const node = spawn("fx-banner", { x: layer.clientWidth / 2, y: layer.clientHeight / 2 }, `<span>${esc(fx.text)}</span>`);
        run(node, [
          { transform: "translate(-150%, -50%) skewX(-12deg)", opacity: 0 },
          { transform: "translate(-50%, -50%) skewX(-12deg)", opacity: 1, offset: 0.25 },
          { transform: "translate(-50%, -50%) skewX(-12deg)", opacity: 1, offset: 0.75 },
          { transform: "translate(50%, -50%) skewX(-12deg)", opacity: 0 },
        ], { duration: budget() * 1.4, easing: "ease-in-out" });
      },
      mana(fx) {
        board.querySelectorAll(`.side[data-player="${CSS.escape(fx.player)}"] .crystal.is-full`).forEach((gem, i) => {
          gem.animate([{ transform: "rotate(45deg) scale(0.2)", opacity: 0.3 }, { transform: "rotate(45deg) scale(1.4)" },
            { transform: "rotate(45deg) scale(1)" }], { duration: budget() * 0.5, delay: i * budget() * 0.04 });
        });
      },
      draw(fx) {
        if (fast()) return;
        const pile = board.querySelector(`.side[data-player="${CSS.escape(fx.player)}"] .deck-pile`);
        const hand = board.querySelector(`.hand[data-player="${CSS.escape(fx.player)}"] .card:last-child`);
        if (!pile || !hand) return;
        const from = centerOf(pile.getBoundingClientRect());
        const to = centerOf(hand.getBoundingClientRect());
        const back = spawn("fx-card-back", from);
        run(back, [
          { transform: "translate(-50%, -50%) scale(0.5) rotate(-20deg)", opacity: 0.4 },
          { transform: `translate(calc(-50% + ${to.x - from.x}px), calc(-50% + ${to.y - from.y}px)) scale(1) rotate(0)`, opacity: 1 },
        ], { duration: budget() * 0.6 });
      },
      power(fx) {
        const power = board.querySelector(`.side[data-player="${CSS.escape(fx.player)}"] .power`);
        if (power) power.animate([{ transform: "scale(1)", filter: "brightness(1)" }, { transform: "scale(1.35)", filter: "brightness(1.8)" },
          { transform: "scale(1)", filter: "brightness(1)" }], { duration: budget() * 0.6 });
        if (power) particles(centerOf(power.getBoundingClientRect()), 10, "is-gold", 50, budget() * 0.7);
      },
      reveal(fx, captured) {
        const middle = { x: layer.clientWidth / 2, y: layer.clientHeight / 2 };
        const start = captured.reveal ? centerOf(captured.reveal) : middle;
        const node = spawn("fx-reveal", middle, bigCardHtml(fx.card));
        const dx = start.x - middle.x;
        const dy = start.y - middle.y;
        run(node, [
          { transform: `translate(calc(-50% + ${dx}px), calc(-50% + ${dy}px)) scale(0.35)`, opacity: 0.2 },
          { transform: "translate(-50%, -50%) scale(1.08)", opacity: 1, offset: 0.25 },
          { transform: "translate(-50%, -50%) scale(1)", opacity: 1, offset: 0.4 },
          { transform: "translate(-50%, -50%) scale(1)", opacity: 1, offset: 0.8 },
          { transform: "translate(-50%, -50%) scale(0.85)", opacity: 0 },
        ], { duration: budget() * (fast() ? 0.9 : 1.6), easing: "ease-in-out" });
      },
      projectile(fx) {
        const from = center(fx.from);
        const to = center(fx.to);
        if (!from || !to) return;
        const node = spawn("fx-projectile fx-" + fx.style, from);
        const lift = -Math.min(120, Math.abs(to.y - from.y) * 0.35);
        const mid = { x: (to.x - from.x) / 2 + (to.x === from.x ? 60 : 0), y: (to.y - from.y) / 2 + lift };
        run(node, [
          { transform: "translate(-50%, -50%) scale(0.6)", opacity: 0.9 },
          { transform: `translate(calc(-50% + ${mid.x}px), calc(-50% + ${mid.y}px)) scale(1.1) rotate(180deg)`, opacity: 1 },
          { transform: `translate(calc(-50% + ${to.x - from.x}px), calc(-50% + ${to.y - from.y}px)) scale(0.9) rotate(360deg)`, opacity: 1 },
        ], { duration: budget() * 0.45, easing: "ease-in" });
      },
      wave(fx) {
        const from = center(fx.from);
        if (!from) return;
        const node = spawn("fx-wave fx-" + fx.style, from);
        run(node, [{ transform: "translate(-50%, -50%) scale(0.2)", opacity: 0.9 },
          { transform: "translate(-50%, -50%) scale(9)", opacity: 0 }], { duration: budget() * 0.7 });
      },
      impact(fx) {
        const target = find(fx.target);
        if (!target) return;
        const at = centerOf(target.getBoundingClientRect());
        target.animate([{ transform: "translateX(0)" }, { transform: "translateX(-7px) rotate(-2deg)" },
          { transform: "translateX(7px) rotate(2deg)" }, { transform: "translateX(-4px)" }, { transform: "translateX(0)" }],
        { duration: budget() * 0.4 });
        const flash = spawn("fx-flash", at);
        run(flash, [{ transform: "translate(-50%, -50%) scale(0.4)", opacity: 0.95 },
          { transform: "translate(-50%, -50%) scale(1.6)", opacity: 0 }], { duration: budget() * 0.45 });
        particles(at, fx.heavy ? 14 : 7, "is-red", fx.heavy ? 70 : 40, budget() * 0.5);
        if (fx.heavy && !fast()) {
          board.animate([{ transform: "translate(0,0)" }, { transform: "translate(-6px,3px)" }, { transform: "translate(6px,-4px)" },
            { transform: "translate(-4px,2px)" }, { transform: "translate(0,0)" }], { duration: 350 });
          run(spawn("fx-vignette", { x: layer.clientWidth / 2, y: layer.clientHeight / 2 }),
            [{ opacity: 0.8 }, { opacity: 0 }], { duration: 500 });
        }
      },
      block(fx) {
        const at = center(fx.target);
        if (!at) return;
        run(spawn("fx-shield", at), [{ transform: "translate(-50%, -50%) scale(0.6)", opacity: 1 },
          { transform: "translate(-50%, -50%) scale(1.3)", opacity: 0 }], { duration: budget() * 0.5 });
        particles(at, 8, "is-white", 45, budget() * 0.4);
      },
      lunge(fx) {
        const minion = find({ minionId: fx.minionId });
        const to = center(fx.to);
        if (!minion || !to) return;
        const from = centerOf(minion.getBoundingClientRect());
        const dx = (to.x - from.x) * 0.8;
        const dy = (to.y - from.y) * 0.8;
        minion.style.zIndex = "4";
        minion.animate([{ transform: "translate(0,0) scale(1)" }, { transform: "translate(0,0) scale(1.15)", offset: 0.2 },
          { transform: `translate(${dx}px, ${dy}px) scale(1.1)`, offset: 0.55, easing: "ease-in" },
          { transform: "translate(0,0) scale(1)" }], { duration: budget() * 0.8 });
      },
      summon(fx) {
        const minion = find({ minionId: fx.minionId });
        if (!minion) return;
        minion.animate([{ transform: "translateY(-40px) scale(1.5)", opacity: 0 }, { transform: "translateY(4px) scale(0.95)", opacity: 1, offset: 0.7 },
          { transform: "translateY(0) scale(1)" }], { duration: budget() * 0.6, easing: "ease-in" });
        const at = centerOf(minion.getBoundingClientRect());
        run(spawn("fx-dust", { x: at.x, y: at.y + 40 }), [{ transform: "translate(-50%, -50%) scale(0.3)", opacity: 0.9 },
          { transform: "translate(-50%, -50%) scale(2.2)", opacity: 0 }], { duration: budget() * 0.6, delay: budget() * 0.35 });
      },
      death(fx, captured) {
        const snapshot = captured["death" + fx.minionId];
        if (!snapshot) return;
        const at = centerOf(snapshot.rect);
        const ghost = spawn("fx-ghost", at, snapshot.html);
        run(ghost, [{ transform: "translate(-50%, -50%) rotate(0) scale(1)", opacity: 1, filter: "grayscale(0)" },
          { transform: "translate(-50%, -50%) rotate(-4deg) scale(1.05)", opacity: 1, filter: "grayscale(1) brightness(0.6)", offset: 0.35 },
          { transform: "translate(-50%, -30%) rotate(8deg) scale(0.6)", opacity: 0, filter: "grayscale(1) brightness(0.3)" }],
        { duration: budget() * 0.9, easing: "ease-in" });
        particles(at, 10, "is-stone", 55, budget() * 0.8);
      },
      aura(fx) {
        const at = center(fx.target);
        if (!at) return;
        run(spawn("fx-aura fx-" + fx.style, at), [{ transform: "translate(-50%, -50%) scale(0.5)", opacity: 0 },
          { transform: "translate(-50%, -50%) scale(1.15)", opacity: 1, offset: 0.4 },
          { transform: "translate(-50%, -50%) scale(1.3)", opacity: 0 }], { duration: budget() * 0.9 });
        const kinds = { heal: ["is-green", -60], poison: ["is-poison", -30], frost: ["is-frost", 0], armor: ["is-white", 0],
          evade: ["is-white", -20], buff: ["is-gold", -20] };
        const [className, rise] = kinds[fx.style] || ["is-gold", 0];
        particles(at, 10, className, 50, budget() * 0.8, rise);
      },
      victory(fx) {
        const winner = center({ hero: fx.winner });
        const loser = find({ hero: fx.loser });
        if (loser) loser.animate([{ transform: "scale(1)", filter: "none" }, { transform: "scale(1.1) rotate(-3deg)" },
          { transform: "scale(0.9) rotate(3deg)", filter: "grayscale(1) brightness(0.5)" }], { duration: 1200, fill: "forwards" });
        if (loser) particles(centerOf(loser.getBoundingClientRect()), 24, "is-stone", 120, 1400);
        if (winner) {
          run(spawn("fx-rays", winner), [{ transform: "translate(-50%, -50%) rotate(0) scale(0.3)", opacity: 0 },
            { transform: "translate(-50%, -50%) rotate(90deg) scale(1)", opacity: 1, offset: 0.3 },
            { transform: "translate(-50%, -50%) rotate(240deg) scale(1.2)", opacity: 0 }], { duration: 3000 });
          for (let i = 0; i < 40; i++) {
            const piece = spawn("fx-confetti", { x: Math.random() * layer.clientWidth, y: -10 });
            piece.style.background = ["#f4d58d", "#3987e5", "#d95926", "#7dff8a", "#ffffff"][i % 5];
            run(piece, [{ transform: "translateY(0) rotate(0)", opacity: 1 },
              { transform: `translateY(${layer.clientHeight + 40}px) rotate(${360 + Math.random() * 720}deg)`, opacity: 0.8 }],
            { duration: 1800 + Math.random() * 1400, delay: Math.random() * 600, easing: "ease-in" });
          }
        }
      },
    };

    /** Draws the effects of one step; delayed ones (impacts) wait for their projectile to land. */
    function play(effects, captured) {
      if (reduced() || !effects.length) return;
      effects.forEach((fx) => {
        const handler = handlers[fx.kind];
        if (!handler) return;
        if (fx.delayed) setTimeout(() => handler(fx, captured || {}), budget() * 0.42);
        else handler(fx, captured || {});
      });
    }

    function clear() {
      layer.innerHTML = "";
    }

    return { capture, play, clear };
  }

  Arena.Fx = { create, bigCardHtml };
})(window);
