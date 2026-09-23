package com.mineskript.lang.runtime;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.LoopState;
import com.mineskript.lang.ast.None;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class Context {
    private final GameBridge game;
    private final Map<String, Object> eventValues;
    private final Variables variables;
    private final Deque<Scope> scopes = new ArrayDeque<>();
    private final Deque<LoopState> loops = new ArrayDeque<>();
    private ScriptControl control = ScriptControl.NONE;

    private record Scope(String file, Map<String, Object> locals) {
    }

    public Context(GameBridge game, String file, Map<String, Object> eventValues) {
        this(game, file, eventValues, new Variables());
    }

    public Context(GameBridge game, String file, Map<String, Object> eventValues, Variables variables) {
        this.game = game;
        this.eventValues = Map.copyOf(eventValues);
        this.variables = variables;
        scopes.push(new Scope(file, new HashMap<>()));
    }

    public ScriptControl control() {
        return control;
    }

    public Context control(ScriptControl control) {
        this.control = control;
        return this;
    }

    public String triggerFile() {
        return scopes.peekLast().file();
    }

    public GameBridge game() {
        return game;
    }

    public String file() {
        return scopes.peek().file();
    }

    public Variables variables() {
        return variables;
    }

    public Object eventValue(String key) {
        Object value = eventValues.get(key);
        if (value == null) {
            throw new ScriptError("\"" + key + "\" is not available in this event");
        }
        return value;
    }

    public GameBridge world() {
        if (!game.hasWorld()) {
            throw new ScriptError("no world");
        }
        return game;
    }

    public Object getVariable(VariableScope scope, String name) {
        Object value = scope == VariableScope.LOCAL ? locals().get(name) : variables.get(scope, name);
        return value == null ? None.NONE : value;
    }

    public void setVariable(VariableScope scope, String name, Object value) {
        if (scope == VariableScope.LOCAL) {
            locals().put(name, value);
        } else {
            variables.set(scope, name, value);
        }
    }

    public void deleteVariable(VariableScope scope, String name) {
        if (scope == VariableScope.LOCAL) {
            locals().remove(name);
        } else {
            variables.delete(scope, name);
        }
    }

    public int functionDepth() {
        return scopes.size() - 1;
    }

    void enterFunction(String functionFile, Map<String, Object> arguments) {
        scopes.push(new Scope(functionFile, new HashMap<>(arguments)));
    }

    void exitFunction() {
        if (scopes.size() > 1) {
            scopes.pop();
        }
    }

    private Map<String, Object> locals() {
        return scopes.peek().locals();
    }

    public void pushLoop(LoopState state) {
        loops.push(state);
    }

    public void popLoop() {
        loops.pop();
    }

    public LoopState currentLoop() {
        LoopState state = loops.peek();
        if (state == null) {
            throw new ScriptError("not inside a loop");
        }
        return state;
    }

    public void clearLoops() {
        loops.clear();
    }
}
