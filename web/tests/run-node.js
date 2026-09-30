/* Runs the browser tests with Node when it is available (not required on team PCs: open tests.html). */
const fs = require("fs");
const path = require("path");
const vm = require("vm");

const web = path.join(__dirname, "..");
const files = ["tests/harness.js", "data/sample-match.js", "tests/expected-log.js", "js/replay.js",
  "js/logformat.js", "js/stats.js", "js/sound.js", "js/visuals.js", "tests/replay.test.js", "tests/logformat.test.js", "tests/stats.test.js",
  "tests/sound.test.js", "tests/visuals.test.js", "js/themes.js", "tests/themes.test.js"];
const context = vm.createContext({ console, structuredClone });
context.globalThis = context;
for (const file of files) {
  vm.runInContext(fs.readFileSync(path.join(web, file), "utf8"), context, { filename: file });
}
const results = context.Arena.Test.run();
for (const r of results) {
  console.log((r.passed ? "  ok   " : "  FAIL ") + r.name + (r.passed ? "" : "\n         " + r.error));
}
const failed = results.filter((r) => !r.passed).length;
console.log(`\n${results.length - failed} passed, ${failed} failed`);
process.exit(failed ? 1 : 0);
