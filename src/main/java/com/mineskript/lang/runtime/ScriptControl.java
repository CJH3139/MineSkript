package com.mineskript.lang.runtime;

public interface ScriptControl {
    ScriptControl NONE = new ScriptControl() {
        @Override
        public void stopAll() {
        }

        @Override
        public void stopScript(String file) {
        }
    };

    void stopAll();

    void stopScript(String file);
}
