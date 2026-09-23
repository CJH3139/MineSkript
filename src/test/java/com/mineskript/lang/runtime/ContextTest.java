package com.mineskript.lang.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.LoopState;
import com.mineskript.lang.ast.None;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ContextTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final Variables variables = new Variables();

    private Context context() {
        return new Context(game, "t.ms", Map.of(), variables);
    }

    @Test
    void scopesAreIsolatedFromEachOther() {
        Context context = context();
        context.setVariable(VariableScope.GLOBAL, "x", 1.0);
        context.setVariable(VariableScope.RAM, "x", 2.0);
        context.setVariable(VariableScope.LOCAL, "x", 3.0);
        assertEquals(1.0, context.getVariable(VariableScope.GLOBAL, "x"));
        assertEquals(2.0, context.getVariable(VariableScope.RAM, "x"));
        assertEquals(3.0, context.getVariable(VariableScope.LOCAL, "x"));
    }

    @Test
    void unsetReadsAsNone() {
        assertSame(None.NONE, context().getVariable(VariableScope.GLOBAL, "missing"));
        assertSame(None.NONE, context().getVariable(VariableScope.LOCAL, "missing"));
    }

    @Test
    void localsBelongToOneContextButGlobalsAndRamAreShared() {
        Context first = context();
        Context second = context();
        first.setVariable(VariableScope.LOCAL, "l", "a");
        first.setVariable(VariableScope.GLOBAL, "g", "b");
        first.setVariable(VariableScope.RAM, "r", "c");
        assertSame(None.NONE, second.getVariable(VariableScope.LOCAL, "l"));
        assertEquals("b", second.getVariable(VariableScope.GLOBAL, "g"));
        assertEquals("c", second.getVariable(VariableScope.RAM, "r"));
    }

    @Test
    void deleteRemovesAndOnlyGlobalWritesBumpTheVersion() {
        Context context = context();
        assertEquals(0, variables.version());
        context.setVariable(VariableScope.RAM, "r", 1.0);
        context.setVariable(VariableScope.LOCAL, "l", 1.0);
        assertEquals(0, variables.version());
        context.setVariable(VariableScope.GLOBAL, "g", 1.0);
        assertEquals(1, variables.version());
        context.deleteVariable(VariableScope.GLOBAL, "g");
        assertEquals(2, variables.version());
        assertSame(None.NONE, context.getVariable(VariableScope.GLOBAL, "g"));
        context.deleteVariable(VariableScope.RAM, "r");
        assertSame(None.NONE, context.getVariable(VariableScope.RAM, "r"));
        assertEquals(2, variables.version());
    }

    @Test
    void threeArgConstructorStillWorksWithItsOwnVariables() {
        Context context = new Context(game, "t.ms", Map.of());
        context.setVariable(VariableScope.GLOBAL, "g", 5.0);
        assertEquals(5.0, context.getVariable(VariableScope.GLOBAL, "g"));
        assertSame(None.NONE, context().getVariable(VariableScope.GLOBAL, "g"));
    }

    @Test
    void loopStackTracksTheInnermostLoop() {
        Context context = context();
        ScriptError error = assertThrows(ScriptError.class, context::currentLoop);
        assertEquals("not inside a loop", error.getMessage());
        LoopState outer = new LoopState();
        LoopState inner = new LoopState();
        context.pushLoop(outer);
        context.pushLoop(inner);
        assertSame(inner, context.currentLoop());
        context.popLoop();
        assertSame(outer, context.currentLoop());
        context.clearLoops();
        assertThrows(ScriptError.class, context::currentLoop);
    }

    @Test
    void loopStateCountsIterationsAndHoldsTheValue() {
        LoopState state = new LoopState();
        assertSame(None.NONE, state.value());
        assertEquals(0, state.iteration());
        state.next("a");
        state.next("b");
        assertEquals("b", state.value());
        assertEquals(2, state.iteration());
    }

    @Test
    void variableScopeParsesBraceTokens() {
        assertEquals(new VariableScope.Parsed(VariableScope.GLOBAL, "count"), VariableScope.parse("{count}"));
        assertEquals(new VariableScope.Parsed(VariableScope.RAM, "count"), VariableScope.parse("{-count}"));
        assertEquals(new VariableScope.Parsed(VariableScope.LOCAL, "count"), VariableScope.parse("{_count}"));
        assertEquals(new VariableScope.Parsed(VariableScope.LOCAL, "my counter"), VariableScope.parse("{_ my counter }"));
        assertEquals(new VariableScope.Parsed(VariableScope.GLOBAL, ""), VariableScope.parse("{}"));
    }
}
