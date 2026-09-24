package com.mineskript.script;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.Language;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ParsedEffect;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.ScriptError;
import java.util.List;

public final class EffectCommands {
    public static final String FILE = "effect command";

    public enum Outcome {
        NOT_MINE,
        RAN,
        FAILED,
        BUSY,
        EMPTY
    }

    private final Parser parser;
    private final EventDispatcher dispatcher;
    private final ConfigFile config;
    private final GameBridge game;
    private boolean running;

    public EffectCommands(Parser parser, EventDispatcher dispatcher, ConfigFile config, GameBridge game) {
        this.parser = parser;
        this.dispatcher = dispatcher;
        this.config = config;
        this.game = game;
    }

    public boolean allowChat(String message) {
        return run(message) == Outcome.NOT_MINE;
    }

    public Outcome run(String message) {
        if (game.sendingOwnChat()) {
            return Outcome.NOT_MINE;
        }
        Config current = config.current();
        if (!current.active() || !message.startsWith(current.effectCommandPrefix())) {
            return Outcome.NOT_MINE;
        }
        if (running) {
            game.showError(new ScriptError(FILE, 0, Language.get("effect-command.nested")).toString());
            return Outcome.BUSY;
        }
        if (!game.hasWorld()) {
            game.showError(new ScriptError(FILE, 0, Language.get("effect-command.no-world")).toString());
            return Outcome.FAILED;
        }
        String text = message.substring(current.effectCommandPrefix().length()).strip();
        if (text.isEmpty()) {
            game.showInfo(Language.format("effect-command.empty", current.effectCommandPrefix()));
            return Outcome.EMPTY;
        }
        ParsedEffect parsed;
        try {
            parsed = parser.parseEffect(FILE, 0, new Event.EffectCommand(), text);
        } catch (RuntimeException | StackOverflowError error) {
            game.showError(describe(error));
            return Outcome.FAILED;
        }
        if (parsed.failed()) {
            game.showError(parsed.error().toString());
            return Outcome.FAILED;
        }
        Trigger trigger = new Trigger(FILE, 0, new Event.EffectCommand(), new Block(List.of(parsed.statement())));
        running = true;
        boolean ran;
        try {
            ran = dispatcher.runOneOff(trigger);
        } finally {
            running = false;
        }
        if (!ran) {
            return Outcome.FAILED;
        }
        game.showInfo(Language.format("effect-command.ran", text));
        return Outcome.RAN;
    }

    private static String describe(Throwable error) {
        String message = error.getMessage();
        return new ScriptError(FILE, 0, message == null || message.isBlank()
                ? error.getClass().getSimpleName()
                : message).toString();
    }
}
