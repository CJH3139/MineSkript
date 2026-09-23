package com.mineskript.script;

public final class OwnChatGuard {
    private int depth;

    public boolean sending() {
        return depth > 0;
    }

    public void around(Runnable send) {
        depth++;
        try {
            send.run();
        } finally {
            depth--;
        }
    }
}
