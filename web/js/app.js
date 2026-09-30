/* App shell: tabs, file loading (picker, drag and drop, samples), keyboard shortcuts, errors. */
(function (root) {
  const Arena = root.Arena;
  const doc = root.document;
  const replay = Arena.ReplayView.create();
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

  doc.addEventListener("keydown", (e) => {
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

  if (new URLSearchParams(root.location.search).has("sample")) {
    doc.getElementById("load-samples").click();
  }
})(window);
