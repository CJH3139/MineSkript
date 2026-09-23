package com.mineskript.lang.runtime;

public final class ScriptError extends RuntimeException {
    private final String file;
    private final int line;

    public ScriptError(String message) {
        this(null, 0, message);
    }

    public ScriptError(String file, int line, String message) {
        super(message);
        this.file = file;
        this.line = line;
    }

    public String file() {
        return file;
    }

    public int line() {
        return line;
    }

    public boolean located() {
        return file != null;
    }

    public ScriptError at(String file, int line) {
        return located() ? this : new ScriptError(file, line, getMessage());
    }

    @Override
    public String toString() {
        if (!located()) {
            return getMessage();
        }
        return line > 0 ? file + ":" + line + ": " + getMessage() : file + ": " + getMessage();
    }
}
