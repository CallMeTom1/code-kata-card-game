/* Tiny test harness: no library, runs in the browser (tests.html) and in Node (run-node.js). */
(function (root) {
  const Arena = (root.Arena = root.Arena || {});
  const tests = [];

  function fail(message) {
    throw new Error(message);
  }

  function show(value) {
    return typeof value === "string" ? JSON.stringify(value) : JSON.stringify(value, null, 0);
  }

  Arena.Test = {
    /** Registers a test; names follow given_when_then like the Java tests. */
    test(name, fn) {
      tests.push({ name, fn });
    },
    eq(actual, expected, label) {
      if (show(actual) !== show(expected)) {
        fail((label ? label + ": " : "") + "expected " + show(expected) + " but got " + show(actual));
      }
    },
    ok(condition, label) {
      if (!condition) fail(label || "expected true");
    },
    throws(fn, fragment) {
      try {
        fn();
      } catch (e) {
        if (fragment && !String(e.message).includes(fragment)) {
          fail("error message " + show(e.message) + " does not contain " + show(fragment));
        }
        return;
      }
      fail("expected an error");
    },
    /** Runs every registered test and returns one result per test. */
    run() {
      return tests.map(({ name, fn }) => {
        try {
          fn();
          return { name, passed: true };
        } catch (e) {
          return { name, passed: false, error: e.message };
        }
      });
    },
  };
})(typeof window !== "undefined" ? window : globalThis);
