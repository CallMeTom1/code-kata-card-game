/* App shell: tabs, file loading (picker, drag and drop, samples), keyboard shortcuts, errors. */
(function (root) {
  const Arena = root.Arena;
  const doc = root.document;
  const sound = createSoundController();
  const replay = Arena.ReplayView.create({ onStep: (event) => sound.onStep(event) });
  const stats = Arena.StatsView.create();
  const toast = doc.getElementById("toast");
  let toastTimer = null;

  function showError(message) {
    toast.textContent = message;
    toast.hidden = false;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => (toast.hidden = true), 6000);
  }

  function selectTab(name) {
    doc.querySelectorAll(".tab").forEach((tab) => {
      const active = tab.dataset.tab === name;
      tab.classList.toggle("is-active", active);
      tab.setAttribute("aria-selected", String(active));
    });
    doc.getElementById("tab-replay").hidden = name !== "replay";
    doc.getElementById("tab-stats").hidden = name !== "stats";
    if (name !== "replay" && replay.isLoaded()) replay.action("pause");
  }

  /** A match export is JSON lines of events; a stats export is one object with "results". */
  function loadText(text, fileName) {
    const trimmed = text.trim();
    try {
      if (trimmed.startsWith('{"type"')) {
        replay.load(Arena.Replay.parseJsonl(trimmed));
        selectTab("replay");
      } else {
        stats.load(Arena.Stats.parse(trimmed));
        selectTab("stats");
      }
    } catch (e) {
      showError((fileName ? fileName + " : " : "") + e.message);
    }
  }

  function loadFile(file) {
    const reader = new FileReader();
    reader.onload = () => loadText(String(reader.result), file.name);
    reader.onerror = () => showError("Impossible de lire " + file.name);
    reader.readAsText(file);
  }

  doc.querySelectorAll(".tab").forEach((tab) => tab.addEventListener("click", () => selectTab(tab.dataset.tab)));
  doc.getElementById("file-input").addEventListener("change", (e) => {
    [...e.target.files].forEach(loadFile);
    e.target.value = "";
  });
  doc.getElementById("load-samples").addEventListener("click", () => {
    const samples = root.ArenaSamples || {};
    if (samples.statsJson) loadText(samples.statsJson, "exemple stats");
    if (samples.matchJsonl) loadText(samples.matchJsonl, "exemple partie");
  });

  doc.addEventListener("dragover", (e) => {
    e.preventDefault();
    doc.body.classList.add("dragging");
  });
  doc.addEventListener("dragleave", (e) => {
    if (!e.relatedTarget) doc.body.classList.remove("dragging");
  });
  doc.addEventListener("drop", (e) => {
    e.preventDefault();
    doc.body.classList.remove("dragging");
    [...e.dataTransfer.files].forEach(loadFile);
  });

  doc.addEventListener("pointerdown", () => sound.unlock());
  doc.addEventListener("keydown", (e) => {
    sound.unlock();
    if ((e.key === "m" || e.key === "M") && !e.target.closest("input, select, textarea")) sound.toggle();
    if (doc.getElementById("tab-replay").hidden || !replay.isLoaded()) return;
    if (e.target.closest("input, select, textarea")) return;
    const keys = { " ": "play", ArrowRight: e.shiftKey ? "next-turn" : "next", ArrowLeft: e.shiftKey ? "prev-turn" : "prev",
      Home: "start", End: "end" };
    const action = keys[e.key];
    if (action) {
      e.preventDefault();
      replay.action(action);
    }
  });

  /** Owns the audio context (browsers only allow sound after a click or key press) and the user's settings. */
  function createSoundController() {
    const toggleButton = doc.getElementById("sound-toggle");
    const volume = doc.getElementById("volume");
    const settings = { enabled: true, volume: 60 };
    try {
      Object.assign(settings, JSON.parse(root.localStorage.getItem("arena.sound") || "{}"));
    } catch (e) {
      /* storage unavailable: keep defaults */
    }
    let ctx = null;
    let synth = null;

    function save() {
      try {
        root.localStorage.setItem("arena.sound", JSON.stringify(settings));
      } catch (e) {
        /* storage unavailable: settings last for this visit only */
      }
    }

    function show() {
      toggleButton.textContent = settings.enabled ? "🔊" : "🔇";
      toggleButton.setAttribute("aria-pressed", String(settings.enabled));
      toggleButton.setAttribute("aria-label", settings.enabled ? "Couper le son" : "Activer le son");
      toggleButton.title = settings.enabled ? "Son activé (M)" : "Son coupé (M)";
      volume.value = String(settings.volume);
      if (synth) synth.setVolume(settings.volume / 100);
    }

    function unlock() {
      const AudioContextClass = root.AudioContext || root.webkitAudioContext;
      if (!AudioContextClass) return;
      if (!ctx) {
        ctx = new AudioContextClass();
        synth = Arena.Sound.createSynth(ctx);
        synth.setVolume(settings.volume / 100);
      }
      if (ctx.state === "suspended") ctx.resume();
    }

    function onStep(event) {
      if (!settings.enabled || !synth) return;
      const cue = Arena.Sound.cueFor(event);
      if (!cue || (replay.speed() >= 4 && Arena.Sound.isMinor(cue.name))) return;
      synth.play(cue);
    }

    function toggle() {
      settings.enabled = !settings.enabled;
      save();
      show();
    }

    toggleButton.addEventListener("click", () => {
      unlock();
      toggle();
    });
    volume.addEventListener("input", () => {
      settings.volume = Number(volume.value);
      if (settings.volume > 0 && !settings.enabled) settings.enabled = true;
      save();
      show();
    });
    show();
    return { unlock, onStep, toggle };
  }

  if (new URLSearchParams(root.location.search).has("sample")) {
    doc.getElementById("load-samples").click();
  }
})(window);
