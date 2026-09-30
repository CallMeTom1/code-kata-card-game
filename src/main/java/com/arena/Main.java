package com.arena;

import java.io.PrintStream;

/**
 * Entry point of the simulator: wires the concrete classes together (Dependency Inversion).
 */
public final class Main {

    private Main() {
    }

    /** Delegates to {@link #run} so the whole program can be tested without touching System.out. */
    public static void main(String[] args) {
        run(args, System.out);
    }

    /** Runs the program against any output stream, so tests can capture what a user would see. */
    public static void run(String[] args, PrintStream out) {
        out.println("Skirmish Arena");
    }
}
