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
})(typeof window !== "undefined" ? window : globalThis);
