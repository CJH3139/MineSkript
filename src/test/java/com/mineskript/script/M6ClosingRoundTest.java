package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class M6ClosingRoundTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Variables variables = new Variables();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), variables, () -> {
    });
    private final ConfigFile config = new ConfigFile(Path.of(Config.NAME));
    private final EffectCommands effects = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, config, game);

    M6ClosingRoundTest() {
        game.chatHook = effects::allowChat;
    }

    @Test
    void aPrefixedLineSentFromInsideAnEffectCommandReachesTheServerRatherThanBeingRefusedAsNested() {
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?make player say \"?tpa Steve\""));
        assertEquals(List.of("?tpa Steve"), game.sentChat);
        assertEquals(List.of(), game.errors);
        assertEquals(List.of("ran make player say \"?tpa Steve\""), game.infos);
    }

    @Test
    void thePrefixedLineSentFromInsideAnEffectCommandIsNotRunAsAnEffectCommandItself() {
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?make player say \"?set {-executed} to 1\""));
        assertEquals(List.of("?set {-executed} to 1"), game.sentChat);
        assertFalse(variables.ram().containsKey("executed"));
        assertEquals(List.of(), game.errors);
    }

    @Test
    void theOwnChatCheckStillAnswersNotMineWhileAnEffectCommandIsRunning() {
        List<EffectCommands.Outcome> nested = new ArrayList<>();
        game.chatHook = text -> {
            nested.add(effects.run(text));
            return true;
        };
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?make player say \"?tpa Steve\""));
        assertEquals(List.of(EffectCommands.Outcome.NOT_MINE), nested);
        assertEquals(List.of("?tpa Steve"), game.sentChat);
        assertEquals(List.of(), game.errors);
    }
}
