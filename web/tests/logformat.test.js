(function (root) {
  const { test, eq } = root.Arena.Test;

  test("given_the_sample_match_when_formatted_then_lines_are_identical_to_the_java_console_log", () => {
    const events = root.Arena.Replay.parseJsonl(root.ArenaSamples.matchJsonl);
    const format = root.Arena.LogFormat.create();
    const lines = events.flatMap((event) => format(event));
    const expected = root.ArenaFixtures.expectedLog.split("\n").filter((line) => line.startsWith("["));
    const matchLines = expected.filter((line) => !line.startsWith("[STATS") && !line.startsWith("[EXPORT"));
    eq(lines.length, matchLines.length, "line count");
    lines.forEach((line, i) => eq(line, matchLines[i], "line " + (i + 1)));
  });

  test("given_a_bot_that_thinks_and_speaks_when_formatted_then_think_and_say_lines_match_the_java_console", () => {
    const format = root.Arena.LogFormat.create();
    format({ type: "MatchStarted", player1: "Alice", player1Label: "Llm:auto", player2: "Bob", player2Label: "Llm:auto", seed: 1 });
    eq(format({ type: "BotSpoke", player: "Alice", thought: "Bob has no Taunt.", message: "Your castle will fall!" }),
      ["[THINK  ][Alice] Bob has no Taunt.", "[SAY    ][Alice] « Your castle will fall! »"]);
    eq(format({ type: "BotSpoke", player: "Bob", thought: "", message: "Never!" }), ["[SAY    ][Bob  ] « Never! »"]);
  });
})(typeof window !== "undefined" ? window : globalThis);
