/* App shell: tabs, file loading (picker, drag and drop, samples), keyboard shortcuts, errors. */
(function (root) {
  const Arena = root.Arena;
  const doc = root.document;
  const t = Arena.I18n.t;
  Arena.I18n.applyStatic();
  const sound = createSoundController();
  const replay = Arena.ReplayView.create({ onStep: (event) => sound.onStep(event), onFrame: (state) => sound.onFrame(state) });
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
    sound.onTab(name);
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
      showError((fileName ? fileName + ": " : "") + e.message);
    }
  }

  function loadFile(file) {
    const reader = new FileReader();
    reader.onload = () => loadText(String(reader.result), file.name);
    reader.onerror = () => showError(t("app.cannot-read", { name: file.name }));
    reader.readAsText(file);
  }

  doc.querySelectorAll(".tab").forEach((tab) => tab.addEventListener("click", () => selectTab(tab.dataset.tab)));
  doc.getElementById("file-input").addEventListener("change", (e) => {
    [...e.target.files].forEach(loadFile);
    e.target.value = "";
  });
  doc.getElementById("load-samples").addEventListener("click", () => {
    const samples = root.ArenaSamples || {};
    if (samples.statsJson) loadText(samples.statsJson, t("app.sample-stats"));
    if (samples.matchJsonl) loadText(samples.matchJsonl, t("app.sample-match"));
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
    if ((e.key === "b" || e.key === "B") && !e.target.closest("input, select, textarea")) sound.toggleMusic();
    if ((e.key === "d" || e.key === "D") && !e.target.closest("input, select, textarea")) design.next();
    if ((e.key === "l" || e.key === "L") && !e.target.closest("input, select, textarea")) Arena.I18n.next();
    if ((e.key === "f" || e.key === "F") && !e.target.closest("input, select, textarea")) toggleFullscreen();
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

  /** Owns the audio context (browsers only allow sound after a click or key press), music and the user's settings. */
  function createSoundController() {
    const toggleButton = doc.getElementById("sound-toggle");
    const musicButton = doc.getElementById("music-toggle");
    const volume = doc.getElementById("volume");
    const themeSelect = doc.getElementById("music-theme");
    const settings = { enabled: true, music: true, volume: 60, theme: "epic" };
    let music = null;
    let lastState = null;
    let tab = "replay";
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

    /** Music theme names follow the page language; the selected theme is restored by show(). */
    function fillThemes() {
      themeSelect.innerHTML = Arena.Sound.MUSIC_THEMES.map((theme) =>
        `<option value="${theme.id}">${t("music." + theme.id, null, theme.name)}</option>`).join("");
    }

    function show() {
      toggleButton.textContent = settings.enabled ? "🔊" : "🔇";
      toggleButton.setAttribute("aria-pressed", String(settings.enabled));
      toggleButton.setAttribute("aria-label", t(settings.enabled ? "sound.on.label" : "sound.off.label"));
      toggleButton.title = t(settings.enabled ? "sound.on.title" : "sound.off.title");
      volume.value = String(settings.volume);
      if (synth) synth.setVolume(settings.volume / 100);
      if (music) music.setVolume((settings.volume / 100) * 0.7);
      musicButton.textContent = "🎵";
      musicButton.setAttribute("aria-pressed", String(settings.music));
      musicButton.setAttribute("aria-label", t(settings.music ? "music.on.label" : "music.off.label"));
      musicButton.title = t(settings.music ? "music.on.title" : "music.off.title");
      themeSelect.value = Arena.Sound.musicTheme(settings.theme).id;
      if (music) music.setTheme(settings.theme);
      updateMusic();
    }

    function updateMusic() {
      if (!music) return;
      const playing = settings.music && tab === "replay" && replay && replay.isLoaded();
      music.setLevel(playing ? Arena.Sound.musicLevelFor(lastState) : -1);
    }

    function unlock() {
      const AudioContextClass = root.AudioContext || root.webkitAudioContext;
      if (!AudioContextClass) return;
      if (!ctx) {
        ctx = new AudioContextClass();
        synth = Arena.Sound.createSynth(ctx);
        synth.setVolume(settings.volume / 100);
        music = Arena.Sound.createMusic(ctx);
        music.setTheme(settings.theme);
        music.setVolume((settings.volume / 100) * 0.7);
      }
      if (ctx.state === "suspended") ctx.resume();
      updateMusic();
    }

    function onFrame(state) {
      lastState = state;
      updateMusic();
    }

    function onTab(name) {
      tab = name;
      updateMusic();
    }

    function toggleMusic() {
      settings.music = !settings.music;
      save();
      show();
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
    musicButton.addEventListener("click", () => {
      unlock();
      toggleMusic();
    });
    themeSelect.addEventListener("change", () => {
      settings.theme = themeSelect.value;
      if (!settings.music) settings.music = true;
      unlock();
      save();
      show();
    });
    volume.addEventListener("input", () => {
      settings.volume = Number(volume.value);
      if (settings.volume > 0 && !settings.enabled) settings.enabled = true;
      save();
      show();
    });
    function relabel() {
      fillThemes();
      show();
    }

    fillThemes();
    show();
    return { unlock, onStep, onFrame, onTab, toggle, toggleMusic, relabel };
  }

  /** Design themes swap palette and pictures; the choice is kept like the sound settings. */
  function createDesignController() {
    const select = doc.getElementById("design-theme");
    const credits = doc.getElementById("theme-credits");
    const themes = Arena.Themes.THEMES;
    let current = "classic";
    try {
      current = root.localStorage.getItem("arena.design") || current;
    } catch (e) {
      /* storage unavailable: classic theme */
    }

    /** Theme names and credits follow the page language. */
    function relabel() {
      select.innerHTML = themes.map((theme) =>
        `<option value="${theme.id}">${t("design." + theme.id, null, theme.name)}</option>`).join("");
      select.value = current;
      const theme = Arena.Themes.themeById(current);
      credits.textContent = t("credits." + theme.id, null, theme.credits);
      credits.hidden = !theme.credits;
    }

    function apply(id) {
      const theme = Arena.Themes.themeById(id);
      current = theme.id;
      doc.documentElement.dataset.theme = theme.id;
      relabel();
      replay.redraw();
      try {
        root.localStorage.setItem("arena.design", theme.id);
      } catch (e) {
        /* storage unavailable: the theme lasts for this visit only */
      }
    }

    function next() {
      const i = themes.findIndex((t) => t.id === current);
      apply(themes[(i + 1) % themes.length].id);
    }

    select.addEventListener("change", () => apply(select.value));
    apply(current);
    return { apply, next, relabel };
  }
  const design = createDesignController();

  /** The page layout follows the window; the real fullscreen hides the browser around it. */
  function toggleFullscreen() {
    if (doc.fullscreenElement) doc.exitFullscreen();
    else if (doc.documentElement.requestFullscreen) doc.documentElement.requestFullscreen().catch(() => {});
  }

  const fullscreenButton = doc.getElementById("fullscreen-toggle");
  fullscreenButton.addEventListener("click", toggleFullscreen);

  function showFullscreenState() {
    const on = !!doc.fullscreenElement;
    fullscreenButton.setAttribute("aria-pressed", String(on));
    fullscreenButton.setAttribute("aria-label", t(on ? "fullscreen.exit.label" : "fullscreen.enter.label"));
    fullscreenButton.title = t(on ? "fullscreen.exit.title" : "fullscreen.enter.title");
  }
  doc.addEventListener("fullscreenchange", () => {
    showFullscreenState();
    fitToWindow();
  });
  showFullscreenState();

  /** Language picker: English by default; a change redraws every text the views wrote themselves. */
  const langSelect = doc.getElementById("lang-select");
  langSelect.value = Arena.I18n.lang();
  langSelect.addEventListener("change", () => Arena.I18n.setLang(langSelect.value));
  Arena.I18n.onChange((lang) => {
    langSelect.value = lang;
    sound.relabel();
    design.relabel();
    showFullscreenState();
    replay.relabel();
    stats.relabel();
    fitToWindow();
  });

  /** Lets the CSS size the game to the window below the top bar, whose height changes when it wraps. */
  function fitToWindow() {
    doc.documentElement.style.setProperty("--topbar-h", doc.querySelector(".topbar").offsetHeight + "px");
  }
  root.addEventListener("resize", fitToWindow);
  fitToWindow();

  if (new URLSearchParams(root.location.search).has("sample")) {
    doc.getElementById("load-samples").click();
  }
})(window);
