package com.mineskript.scripttest;

import java.util.ArrayList;
import java.util.List;

/** The assertions that failed while one script test ran, in the order they failed. */
public final class AssertionLog {
    private final List<String> failures = new ArrayList<>();

    void fail(String file, int line, String message) {
        failures.add(file + ":" + line + ": " + message);
    }

    void add(String failure) {
        failures.add(failure);
    }

    void clear() {
        failures.clear();
    }

    List<String> failures() {
        return List.copyOf(failures);
    }
}
