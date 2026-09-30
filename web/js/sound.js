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

  /** Music intensity: -1 silent (no match or match over), 0 calm (setup), 1 battle, 2 climax (a champion at 10 HP or less). */
  function musicLevelFor(state) {
    if (!state || state.phase === "ended") return -1;
    if (state.phase === "setup") return 0;
    const lowest = Math.min(...state.order.map((name) => state.players[name].hp));
    return lowest <= 10 ? 2 : 1;
  }

  const SEMITONES = { C: -9, D: -7, E: -5, F: -4, G: -2, A: 0, B: 2 };

  /** Frequency of a note like "A4", "Bb2" or "F#3" (equal temperament, A4 = 440 Hz). */
  function noteFrequency(note) {
    const [, letter, accidental, octave] = note.match(/^([A-G])(#|b)?(-?\d)$/);
    const offset = SEMITONES[letter] + (accidental === "#" ? 1 : accidental === "b" ? -1 : 0) + (Number(octave) - 4) * 12;
    return Math.round(440 * Math.pow(2, offset / 12) * 100) / 100;
  }

  /*
   * Original "epic" loop in D minor (Dm - Bb - F - C, 84 bpm), composed from oscillators and noise:
   * string pads (level 0+), taiko drums, bass and brass stabs (level 1+), a heroic melody and faster
   * drums (level 2). Notes are scheduled slightly ahead of time so the rhythm stays steady.
   */
  function createMusic(ctx) {
    const TEMPO = 84;
    const EIGHTH = 60 / TEMPO / 2;
    const CHORDS = [["D3", "F3", "A3"], ["Bb2", "D3", "F3"], ["F2", "A2", "C3"], ["C3", "E3", "G3"]];
    const BASS = ["D2", "Bb1", "F1", "C2"];
    const MELODY = [
      ["D5", 3], ["F5", 1], ["A5", 2], ["G5", 2], ["F5", 3], ["D5", 1], ["E5", 2], ["C5", 2],
      ["F5", 3], ["A5", 1], ["C6", 2], ["Bb5", 2], ["A5", 4], ["G5", 2], ["E5", 2],
    ];
    const out = ctx.createGain();
    out.gain.value = 0;
    out.connect(ctx.destination);
    const noiseBuffer = ctx.createBuffer(1, ctx.sampleRate, ctx.sampleRate);
    const data = noiseBuffer.getChannelData(0);
    for (let i = 0; i < data.length; i++) data[i] = Math.random() * 2 - 1;

    let level = -1;
    let volume = 0.5;
    let timer = null;
    let step = 0;
    let nextTime = 0;

    function voice(freq, time, dur, { type = "sawtooth", gain = 0.05, attack = 0.02, cutoff = 1200, detune = 0 }) {
      const osc = ctx.createOscillator();
      const filter = ctx.createBiquadFilter();
      const g = ctx.createGain();
      osc.type = type;
      osc.frequency.value = freq;
      osc.detune.value = detune;
      filter.type = "lowpass";
      filter.frequency.value = cutoff;
      g.gain.setValueAtTime(0.0001, time);
      g.gain.exponentialRampToValueAtTime(gain, time + attack);
      g.gain.setValueAtTime(gain, time + Math.max(attack, dur - 0.15));
      g.gain.exponentialRampToValueAtTime(0.0001, time + dur);
      osc.connect(filter).connect(g).connect(out);
      osc.start(time);
      osc.stop(time + dur + 0.05);
    }

    function drum(time, strength) {
      const osc = ctx.createOscillator();
      const g = ctx.createGain();
      osc.frequency.setValueAtTime(95, time);
      osc.frequency.exponentialRampToValueAtTime(38, time + 0.35);
      g.gain.setValueAtTime(0.0001, time);
      g.gain.exponentialRampToValueAtTime(0.55 * strength, time + 0.008);
      g.gain.exponentialRampToValueAtTime(0.0001, time + 0.5);
      osc.connect(g).connect(out);
      osc.start(time);
      osc.stop(time + 0.55);
      const src = ctx.createBufferSource();
      const filter = ctx.createBiquadFilter();
      const n = ctx.createGain();
      src.buffer = noiseBuffer;
      filter.type = "lowpass";
      filter.frequency.value = 700;
      n.gain.setValueAtTime(0.0001, time);
      n.gain.exponentialRampToValueAtTime(0.25 * strength, time + 0.005);
      n.gain.exponentialRampToValueAtTime(0.0001, time + 0.18);
      src.connect(filter).connect(n).connect(out);
      src.start(time);
      src.stop(time + 0.2);
    }

    let melodyIndex = 0;
    let melodyWait = 0;

    function schedule(eighth, time) {
      const bar = Math.floor(eighth / 8) % CHORDS.length;
      const inBar = eighth % 8;
      const barLength = EIGHTH * 8;
      if (inBar === 0) {
        CHORDS[bar].forEach((note) => [-7, 7].forEach((d) =>
          voice(noteFrequency(note), time, barLength + 0.1, { gain: 0.035, attack: 0.5, cutoff: 900, detune: d })));
      }
      if (level >= 1) {
        if (inBar === 0 || inBar === 4) voice(noteFrequency(BASS[bar]), time, EIGHTH * 3, { type: "triangle", gain: 0.16, cutoff: 500 });
        if (inBar === 0 || inBar === 3 || inBar === 4) drum(time, inBar === 0 ? 1 : 0.7);
        if (inBar === 0) CHORDS[bar].forEach((note) =>
          voice(noteFrequency(note) * 2, time, EIGHTH * 1.5, { gain: 0.03, attack: 0.01, cutoff: 2200 }));
      }
      if (level >= 2) {
        if (inBar === 6 || inBar === 7) drum(time, 0.5);
        if (melodyWait <= 0) {
          const [note, eighths] = MELODY[melodyIndex % MELODY.length];
          voice(noteFrequency(note), time, EIGHTH * eighths * 0.95, { type: "square", gain: 0.025, attack: 0.03, cutoff: 2600 });
          melodyWait = eighths;
          melodyIndex++;
        }
        melodyWait--;
      }
    }

    function tick() {
      while (nextTime < ctx.currentTime + 0.15) {
        schedule(step, nextTime);
        nextTime += EIGHTH;
        step++;
      }
    }

    function fadeTo(value, seconds) {
      out.gain.cancelScheduledValues(ctx.currentTime);
      out.gain.setValueAtTime(Math.max(0.0001, out.gain.value), ctx.currentTime);
      out.gain.linearRampToValueAtTime(value, ctx.currentTime + seconds);
    }

    return {
      /** Starts, adapts or fades out the music; calling it with the same level does nothing. */
      setLevel(next) {
        if (next === level) return;
        const wasSilent = level < 0;
        level = next;
        if (level < 0) {
          fadeTo(0, 1.2);
          setTimeout(() => {
            if (level < 0 && timer) {
              clearInterval(timer);
              timer = null;
            }
          }, 1300);
          return;
        }
        if (wasSilent || !timer) {
          if (!timer) {
            step = 0;
            melodyIndex = 0;
            melodyWait = 0;
            nextTime = ctx.currentTime + 0.1;
            timer = setInterval(tick, 25);
          }
          fadeTo(volume, 1.5);
        }
      },
      setVolume(v) {
        volume = Math.max(0, Math.min(1, v));
        if (level >= 0) fadeTo(volume, 0.2);
      },
    };
  }

  Arena.Sound = { cueFor, isMinor, createSynth, musicLevelFor, noteFrequency, createMusic };
})(typeof window !== "undefined" ? window : globalThis);
