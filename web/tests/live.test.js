(function (root) {
  const { test, eq } = root.Arena.Test;

  test("given_the_form_choices_when_the_request_is_built_then_bots_and_classes_are_joined_like_the_command_line", () => {
    const body = root.Arena.Live.requestBody({ name1: " Alice ", bot1: "Llm", class1: "auto",
      name2: "Bob", bot2: "Defensive", class2: "Tank" });
    eq(body, { p1: "Llm:auto", p2: "Defensive:Tank", names: ["Alice", "Bob"] });
  });

  test("given_no_api_key_when_the_bot_options_are_listed_then_llm_is_shown_but_disabled", () => {
    const options = root.Arena.Live.botOptions({ bots: ["Aggressive", "Llm"], llmReady: false });
    eq(options, [{ value: "Aggressive", label: "Aggressive", disabled: false },
      { value: "Llm", label: "Llm (Claude) — clé API manquante", disabled: true }]);
  });
})(typeof window !== "undefined" ? window : globalThis);
