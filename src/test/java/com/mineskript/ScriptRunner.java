package com.mineskript;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Execution;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.ScriptError;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import java.util.Map;

public final class ScriptRunner {
    public final FakeGameBridge game = new FakeGameBridge();
    public final Variables variables = new Variables();
    private final Parser parser = new Parser(DefaultSyntax.registry());
    private final Interpreter interpreter = new Interpreter(10_000);

    public ParsedScript parse(String source) {
        return parser.parse("t.ms", source);
    }

    public List<String> errorsOf(String source) {
        return parse(source).errors().stream().map(ParseError::toString).toList();
    }

    public ParsedScript run(String source) {
        ParsedScript script = parse(source);
        if (!script.errors().isEmpty()) {
            throw new AssertionError("parse errors: " + script.errors());
        }
        for (Trigger trigger : script.triggers()) {
            runTrigger(trigger, Map.of());
        }
        return script;
    }

    public void runTrigger(Trigger trigger, Map<String, Object> values) {
        Execution execution = new Execution(trigger, new Context(game, "t.ms", values, variables));
        try {
            while (interpreter.run(execution) == Interpreter.Outcome.WAITING) {
                if (execution.waitCondition() != null) {
                    game.calls.add("waitUntil");
                    break;
                }
                game.calls.add("wait:" + execution.waitTicks());
            }
        } catch (ScriptError error) {
            game.errors.add(error.toString());
        }
    }

    public Object global(String name) {
        return variables.global().getOrDefault(name, None.NONE);
    }
}
