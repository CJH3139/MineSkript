package com.mineskript.script;

import com.mineskript.lang.ParseError;
import java.util.List;

public record FileReload(String file, FileReload.Outcome outcome, List<ParseError> errors, int triggerCount, long millis) {
    public enum Outcome {
        RELOADED,
        ADDED,
        KEPT,
        REMOVED,
        MISSING,
        REFUSED,
        BUSY
    }
}
