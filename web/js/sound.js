/*
 * Sounds for the replay, synthesized with the Web Audio API: no audio files, works from disk.
 * cueFor() (pure, tested) picks a sound for an event; createSynth() plays it.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const MINOR = new Set(["draw", "mana", "card", "swipe"]);

  const cue = (name, intensity) => ({ name, intensity });

  /** Which sound an event makes, and how loud (0..1); null for quiet events. */
  function cueFor(e) {
    switch (e.type) {
      case "FirstPlayerChosen": return cue("coin", 0.6);
      case "TurnStarted": return cue("turn", 0.6);
      case "CardDrawn": return cue("draw", 0.5);
      case "CardBurned": return cue("burn", 0.7);
      case "CardPlayed": return cue("card", 0.6);
      case "HeroPowerUsed": return cue("power", 0.7);
      case "IllegalAction": case "SummonFizzled": return cue("error", 0.5);
      case "DamageDealt": {
        const lost = e.hpBefore - e.hpAfter;
        if (lost <= 0) return cue("block", 0.5);
        return cue("hit", lost >= 6 ? 1 : lost >= 3 ? 0.7 : 0.4);
      }
      case "MinionDamaged": return cue("hit", 0.3);
      case "EvasionTriggered": return cue("whoosh", 0.8);
      case "Healed": return e.amount > 0 ? cue("heal", 0.6) : null;
      case "ArmorGained": return cue("armor", 0.6);
      case "ManaGained": return cue("mana", 0.5);
      case "StatusApplied":
        if (e.status.startsWith("FROZEN")) return cue("freeze", 0.7);
        if (e.status.startsWith("POISON")) return cue("poison", 0.6);
        return cue("shimmer", 0.6);
      case "PoisonTicked": return cue("poison", 0.5);
      case "FatigueDamage": return cue("fatigue", 0.8);
      case "MinionSummoned": return cue("summon", e.taunt ? 0.9 : 0.6);
      case "MinionAttacked": return cue("swipe", 0.6);
      case "MinionDied": return cue("death", 0.6);
      case "MatchEnded": return e.winner === "DRAW" ? cue("draw-game", 0.7) : cue("fanfare", 1);
      default: return null;
    }
  }

  /** Minor sounds are skipped when playback is fast, so the important ones stay audible. */
  function isMinor(name) {
    return MINOR.has(name);
  }

  /** A tiny synthesizer: oscillators and filtered noise, one recipe per cue. */
  function createSynth(ctx) {
    const master = ctx.createGain();
    master.gain.value = 0.5;
    master.connect(ctx.destination);
    const noiseBuffer = ctx.createBuffer(1, ctx.sampleRate, ctx.sampleRate);
    const data = noiseBuffer.getChannelData(0);
    for (let i = 0; i < data.length; i++) data[i] = Math.random() * 2 - 1;

    function envelope(gainNode, t0, peak, attack, duration) {
      gainNode.gain.setValueAtTime(0.0001, t0);
      gainNode.gain.exponentialRampToValueAtTime(Math.max(0.0002, peak), t0 + attack);
      gainNode.gain.exponentialRampToValueAtTime(0.0001, t0 + duration);
    }

    function tone({ freq, to, type = "sine", start = 0, dur = 0.2, gain = 0.3, attack = 0.005 }) {
      const t0 = ctx.currentTime + start;
      const osc = ctx.createOscillator();
      const g = ctx.createGain();
      osc.type = type;
      osc.frequency.setValueAtTime(freq, t0);
      if (to) osc.frequency.exponentialRampToValueAtTime(to, t0 + dur);
      envelope(g, t0, gain, attack, dur);
      osc.connect(g).connect(master);
      osc.start(t0);
      osc.stop(t0 + dur + 0.05);
    }

    function noise({ start = 0, dur = 0.15, gain = 0.25, freq = 1200, to, q = 1, type = "bandpass" }) {
      const t0 = ctx.currentTime + start;
      const src = ctx.createBufferSource();
      const filter = ctx.createBiquadFilter();
      const g = ctx.createGain();
      src.buffer = noiseBuffer;
      filter.type = type;
      filter.Q.value = q;
      filter.frequency.setValueAtTime(freq, t0);
      if (to) filter.frequency.exponentialRampToValueAtTime(to, t0 + dur);
      envelope(g, t0, gain, 0.005, dur);
      src.connect(filter).connect(g).connect(master);
      src.start(t0);
      src.stop(t0 + dur + 0.05);
    }

    const chord = (freqs, opts) => freqs.forEach((f, i) => tone({ freq: f, start: (opts.step || 0) * i, ...opts }));

    const recipes = {
      coin: (k) => { tone({ freq: 1760, type: "triangle", dur: 0.12, gain: 0.2 * k }); tone({ freq: 2349, type: "triangle", start: 0.09, dur: 0.3, gain: 0.18 * k }); },
      turn: (k) => { tone({ freq: 392, type: "triangle", dur: 0.6, gain: 0.22 * k, attack: 0.01 }); tone({ freq: 587, type: "sine", start: 0.04, dur: 0.5, gain: 0.12 * k }); },
      draw: (k) => noise({ dur: 0.12, gain: 0.18 * k, freq: 3000, to: 1200, q: 0.8 }),
      card: (k) => { noise({ dur: 0.1, gain: 0.2 * k, freq: 1800, to: 700, q: 0.7 }); tone({ freq: 140, to: 90, start: 0.08, dur: 0.12, gain: 0.25 * k }); },
      power: (k) => chord([523, 659, 784, 1047], { type: "sine", dur: 0.35, gain: 0.1 * k, step: 0.05 }),
      error: (k) => { tone({ freq: 180, type: "square", dur: 0.12, gain: 0.08 * k }); tone({ freq: 150, type: "square", start: 0.13, dur: 0.15, gain: 0.08 * k }); },
      hit: (k) => { noise({ dur: 0.12 + 0.12 * k, gain: 0.35 * k, freq: 900, to: 200, q: 0.9, type: "lowpass" }); tone({ freq: 110 + 60 * k, to: 45, dur: 0.18 + 0.15 * k, gain: 0.45 * k }); },
      block: (k) => { tone({ freq: 1250, type: "triangle", dur: 0.25, gain: 0.2 * k }); tone({ freq: 1870, type: "sine", dur: 0.18, gain: 0.12 * k }); noise({ dur: 0.05, gain: 0.15 * k, freq: 5000, q: 2 }); },
      whoosh: (k) => noise({ dur: 0.4, gain: 0.3 * k, freq: 400, to: 3500, q: 1.2 }),
      heal: (k) => chord([523, 659, 784, 1047, 1319], { type: "sine", dur: 0.4, gain: 0.09 * k, step: 0.06 }),
      armor: (k) => { tone({ freq: 880, type: "triangle", dur: 0.3, gain: 0.16 * k }); tone({ freq: 1320, type: "triangle", start: 0.05, dur: 0.35, gain: 0.1 * k }); },
      mana: (k) => { tone({ freq: 1568, type: "sine", dur: 0.2, gain: 0.12 * k }); tone({ freq: 2093, type: "sine", start: 0.06, dur: 0.25, gain: 0.08 * k }); },
      freeze: (k) => { chord([2637, 3136, 3520], { type: "sine", dur: 0.3, gain: 0.06 * k, step: 0.04 }); noise({ dur: 0.35, gain: 0.12 * k, freq: 7000, q: 3 }); },
      poison: (k) => { for (let i = 0; i < 3; i++) tone({ freq: 300 + 80 * i, to: 180, start: 0.07 * i, dur: 0.12, gain: 0.14 * k }); },
      shimmer: (k) => chord([988, 1319, 1760], { type: "sine", dur: 0.3, gain: 0.08 * k, step: 0.05 }),
      fatigue: (k) => tone({ freq: 90, to: 60, type: "sawtooth", dur: 0.5, gain: 0.12 * k }),
      burn: (k) => noise({ dur: 0.4, gain: 0.2 * k, freq: 2500, to: 600, q: 0.5 }),
      summon: (k) => { tone({ freq: 196, to: 392, type: "triangle", dur: 0.25, gain: 0.25 * k }); noise({ start: 0.05, dur: 0.15, gain: 0.12 * k, freq: 800, q: 1 }); },
      swipe: (k) => noise({ dur: 0.18, gain: 0.25 * k, freq: 600, to: 2600, q: 1.5 }),
      death: (k) => tone({ freq: 440, to: 110, type: "triangle", dur: 0.5, gain: 0.18 * k }),
      fanfare: (k) => { chord([523, 659, 784], { type: "triangle", dur: 0.25, gain: 0.18 * k, step: 0.14 }); chord([1047, 784, 659], { type: "triangle", start: 0.45, dur: 0.9, gain: 0.14 * k }); },
      "draw-game": (k) => chord([392, 466, 392], { type: "triangle", dur: 0.35, gain: 0.15 * k, step: 0.18 }),
    };

    return {
      play(c) {
        if (c && recipes[c.name]) recipes[c.name](c.intensity);
      },
      setVolume(v) {
        master.gain.value = Math.max(0, Math.min(1, v));
      },
    };
  }

  Arena.Sound = { cueFor, isMinor, createSynth };
})(typeof window !== "undefined" ? window : globalThis);
