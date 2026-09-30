/*
 * Live matches: when the page is served by the Java server (--serve), a "New match" button
 * starts a match on the server and the replay follows its events as they arrive (Server-Sent Events).
 * Opened as a file, the page stays a replay viewer and this module does nothing.
 */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const t = (key, params) => Arena.I18n.t(key, params);

  /** Form values to the server's request: "Bot:Class" as on the command line. */
  function requestBody(form) {
    return { p1: form.bot1 + ":" + form.class1, p2: form.bot2 + ":" + form.class2,
      names: [form.name1.trim(), form.name2.trim()] };
  }

  /** The bot list for the form; the Llm bot is visible but disabled without an API key. */
  function botOptions(options) {
    return options.bots.map((bot) => {
      const llm = bot === "Llm";
      const missing = llm && !options.llmReady;
      return { value: bot, label: llm ? t("live.llm") + (missing ? t("live.no-key") : "") : bot, disabled: missing };
    });
  }

  function create({ replay, showError, selectTab }) {
    const doc = root.document;
    if (!/^https?:$/.test(root.location.protocol)) return;
    const button = doc.getElementById("new-match");
    const dialog = doc.getElementById("new-match-dialog");
    const form = doc.getElementById("new-match-form");
    let source = null;
    let options = null;

    fetch("api/options").then((r) => (r.ok ? r.json() : null)).then((answer) => {
      if (!answer) return;
      options = answer;
      fill();
      reset();
      button.hidden = false;
    }).catch(() => {});

    /** Option labels in the current language, keeping the player's choices. */
    function fill() {
      const bots = botOptions(options).map((o) =>
        `<option value="${o.value}"${o.disabled ? " disabled" : ""}>${o.label}</option>`).join("");
      const classes = `<option value="auto">${t("live.auto")}</option>`
        + options.classes.map((c) => `<option value="${c}">${c}</option>`).join("");
      ["1", "2"].forEach((n) => {
        const bot = form.elements["bot" + n].value;
        const heroClass = form.elements["class" + n].value;
        form.elements["bot" + n].innerHTML = bots;
        form.elements["class" + n].innerHTML = classes;
        if (bot) form.elements["bot" + n].value = bot;
        if (heroClass) form.elements["class" + n].value = heroClass;
      });
    }

    /** First defaults: two AIs when a key is set, two classic bots otherwise. */
    function reset() {
      const llm = options.llmReady;
      form.elements.bot1.value = llm ? "Llm" : "Aggressive";
      form.elements.bot2.value = llm ? "Llm" : "Defensive";
      form.elements.name1.value = llm ? "Claude-A" : "Alice";
      form.elements.name2.value = llm ? "Claude-B" : "Bob";
    }

    button.addEventListener("click", () => {
      fill();
      dialog.showModal();
    });
    form.addEventListener("submit", (e) => {
      if (e.submitter && e.submitter.value === "cancel") return;
      e.preventDefault();
      const values = Object.fromEntries(new FormData(form));
      dialog.close();
      start(requestBody(values));
    });

    async function start(body) {
      if (source) source.close();
      let response;
      try {
        response = await fetch("api/matches", { method: "POST", headers: { "Content-Type": "application/json" },
          body: JSON.stringify(body) });
      } catch (e) {
        showError(t("live.unreachable", { message: e.message }));
        return;
      }
      const answer = await response.json();
      if (!response.ok) {
        showError(answer.error || t("live.failed"));
        return;
      }
      selectTab("replay");
      replay.startLive();
      source = new EventSource("api/matches/" + answer.id + "/events");
      source.onmessage = (e) => replay.append(JSON.parse(e.data));
      source.addEventListener("end", () => finish());
      source.addEventListener("failure", (e) => {
        finish();
        showError(t("live.stopped", { message: JSON.parse(e.data).message }));
      });
      source.onerror = () => {
        if (!source) return;
        finish();
        showError(t("live.lost"));
      };
    }

    function finish() {
      if (source) source.close();
      source = null;
      replay.endLive();
    }
  }

  Arena.Live = { requestBody, botOptions, create };
})(typeof window !== "undefined" ? window : globalThis);
