package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.List;
import org.junit.jupiter.api.Test;

class SingleFileFramesTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler, new Variables(), () -> {
    });

    private ParsedScript parse(String file, String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse(file, source);
        assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
        return script;
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void aScheduledFrameOfTheReloadedFileIsDroppedAndTheOtherFileKeepsItsResumeTick() {
        registry.replace(List.of(
                parse("a.ms", "on world join:\n    wait 5 ticks\n    send \"a\"\n"),
                parse("b.ms", "on world join:\n    wait 5 ticks\n    send \"b\"\n")));
        ticks(1);
        assertEquals(2, scheduler.size());
        dispatcher.dropFrames("a.ms");
        assertEquals(1, scheduler.size());
        ticks(4);
        assertEquals(List.of(), game.messages);
        ticks(1);
        assertEquals(List.of("b"), game.messages);
        ticks(10);
        assertEquals(List.of("b"), game.messages);
    }

    @Test
    void aParkedFrameOfTheReloadedFileIsDroppedAndAnotherFilesParkedFrameStillResumes() {
        registry.replace(List.of(
                parse("a.ms", "on world join:\n    wait until player is sneaking\n    send \"a\"\n"),
                parse("b.ms", "on world join:\n    wait until player is sneaking\n    send \"b\"\n")));
        ticks(1);
        dispatcher.dropFrames("a.ms");
        game.sneaking = true;
        ticks(2);
        assertEquals(List.of("b"), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aFrameThatIsRunningWhenItsOwnFileIsReloadedNeverParksAndNeverResumes() {
        Statement drop = new Statement() {
            @Override
            public int line() {
                return 1;
            }

            @Override
            public Flow execute(Context context) {
                dispatcher.dropFrames("a.ms");
                return Flow.CONTINUE;
            }
        };
        Statement pause = new Statement() {
            @Override
            public int line() {
                return 2;
            }

            @Override
            public Flow execute(Context context) {
                return new Flow.Wait(2);
            }
        };
        Statement after = new Statement() {
            @Override
            public int line() {
                return 3;
            }

            @Override
            public Flow execute(Context context) {
                context.game().showMessage("resumed");
                return Flow.CONTINUE;
            }
        };
        Trigger trigger = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(drop, pause, after)));
        registry.replace(List.of(new ParsedScript("a.ms", List.of(trigger), List.of())));
        dispatcher.onLoad();
        assertEquals(0, scheduler.size());
        ticks(10);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void onLoadForOneFileFiresOnlyThatFilesLoadTriggers() {
        registry.replace(List.of(
                parse("a.ms", "on load:\n    send \"a\"\n"),
                parse("b.ms", "on load:\n    send \"b\"\n")));
        dispatcher.onLoad("b.ms");
        assertEquals(List.of("b"), game.messages);
    }

    @Test
    void replacingOneScriptKeepsTheOrderOfTheRestAndAddingOneSortsItIn() {
        registry.replace(List.of(
                parse("a.ms", "on load:\n    send \"a\"\n"),
                parse("c.ms", "on load:\n    send \"c\"\n")));
        registry.replaceScript(parse("a.ms", "on load:\n    send \"a2\"\non chat:\n    send \"chat\"\n"));
        assertEquals(List.of("a.ms", "c.ms"), registry.scripts().stream().map(ParsedScript::file).toList());
        assertEquals(3, registry.triggers().size());
        registry.replaceScript(parse("b.ms", "on load:\n    send \"b\"\n"));
        assertEquals(List.of("a.ms", "b.ms", "c.ms"), registry.scripts().stream().map(ParsedScript::file).toList());
        registry.removeScript("a.ms");
        assertEquals(List.of("b.ms", "c.ms"), registry.scripts().stream().map(ParsedScript::file).toList());
        assertEquals(2, registry.triggers().size());
    }

    @Test
    void everyPerFileChangeBumpsTheGenerationAndScriptLooksUpByName() {
        int before = registry.generation();
        registry.replaceScript(parse("a.ms", "on load:\n    send \"a\"\n"));
        assertEquals("a.ms", registry.script("a.ms").file());
        assertNull(registry.script("b.ms"));
        registry.removeScript("a.ms");
        assertEquals(before + 2, registry.generation());
        assertNull(registry.script("a.ms"));
    }
}
