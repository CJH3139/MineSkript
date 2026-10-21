package com.mineskript.lang.parse;

/** Told about every script just before it is parsed, with its full text. Register one with {@link SyntaxRegistry#addScriptListener}. */
@FunctionalInterface
public interface ScriptListener {
    void parsing(String file, String source);
}
