package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Condition;
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
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class SingleFileFrameEdgesTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler, new Variables(), () -> {
    });

    private final Condition always = context -> true;

    private Statement statement(int line, Function<Context, Flow> body) {
        return new Statement() {
            @Override
            public int line() {
                return line;
            }

            @Override
            public Flow execute(Context context) {
                return body.apply(context);
            }
        };
    }

    private Statement drop(String file) {
        return statement(1, context -> {
            dispatcher.dropFrames(file);
            return Flow.CONTINUE;
        });
    }

    private Statement say(String text) {
        return statement(2, context -> {
            context.game().showMessage(text);
            return Flow.CONTINUE;
        });
    }

    private Statement park() {
        return statement(3, context -> new Flow.Park(always, 100));
    }

    private Statement pause(int amount) {
        return statement(4, context -> new Flow.Wait(amount));
    }

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
    void aFrameAlreadyDrainedInThisBatchIsSkippedWhenAnEarlierFrameReloadsItsFile() {
        Trigger first = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(pause(1), drop("a.ms"))));
        Trigger second = new Trigger("a.ms", 2, new Event.Load(), new Block(List.of(pause(1), say("late"))));
        registry.replace(List.of(new ParsedScript("a.ms", List.of(first, second), List.of())));
        dispatcher.onLoad();
        assertEquals(2, scheduler.size());
        ticks(1);
        assertEquals(List.of(), game.messages);
        ticks(10);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void twoReloadsOfTheSameFileInOneTickDropItAndLeaveTheOtherFilesResumeTickExact() {
        Trigger reloader = new Trigger("a.ms", 1, new Event.Load(),
                new Block(List.of(drop("a.ms"), drop("a.ms"), pause(2), say("resumed"))));
        registry.replace(List.of(
                parse("b.ms", "on load:\n    wait 5 ticks\n    send \"b\"\n"),
                new ParsedScript("a.ms", List.of(reloader), List.of())));
        dispatcher.onLoad();
        assertEquals(1, scheduler.size());
        ticks(4);
        assertEquals(List.of(), game.messages);
        ticks(1);
        assertEquals(List.of("b"), game.messages);
        ticks(10);
        assertEquals(List.of("b"), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aSecondReloadOfAFileDropsAFrameThatWasCreatedAfterTheFirstReload() {
        Trigger first = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(pause(1), drop("a.ms"))));
        Trigger second = new Trigger("a.ms", 2, new Event.Load(), new Block(List.of(pause(1), say("late"))));
        registry.replace(List.of(new ParsedScript("a.ms", List.of(first, second), List.of())));
        dispatcher.dropFrames("a.ms");
        dispatcher.onLoad("a.ms");
        assertEquals(2, scheduler.size());
        ticks(1);
        assertEquals(List.of(), game.messages);
        ticks(10);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aTimedWaitLeavesNoSchedulerBookkeepingBehindOnceItHasResumed() {
        Trigger waiter = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(pause(1), say("done"))));
        registry.replace(List.of(new ParsedScript("a.ms", List.of(waiter), List.of())));
        dispatcher.onLoad();
        assertEquals(1, scheduler.size());
        assertEquals(1, dispatcher.scheduledSize());
        ticks(1);
        assertEquals(List.of("done"), game.messages);
        assertEquals(0, scheduler.size());
        assertEquals(0, dispatcher.scheduledSize());
        ticks(10);
        assertEquals(0, dispatcher.scheduledSize());
        assertEquals(List.of(), game.errors);
    }

    @Test
    void reloadingAFileAndLosingTheWorldBothClearTheSchedulerBookkeeping() {
        Trigger waiter = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(pause(50), say("a"))));
        Trigger other = new Trigger("b.ms", 1, new Event.Load(), new Block(List.of(pause(50), say("b"))));
        registry.replace(List.of(
                new ParsedScript("a.ms", List.of(waiter), List.of()),
                new ParsedScript("b.ms", List.of(other), List.of())));
        dispatcher.onLoad();
        assertEquals(2, dispatcher.scheduledSize());
        dispatcher.dropFrames("a.ms");
        assertEquals(1, dispatcher.scheduledSize());
        assertEquals(1, scheduler.size());
        dispatcher.onDisconnect();
        assertEquals(0, dispatcher.scheduledSize());
        assertEquals(0, scheduler.size());
        ticks(60);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aFrameThatRepacksDuringTheResumePassIsStillDroppedByALaterReloadInThatPass() {
        Trigger repacker = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(park(), park(), say("x"))));
        Trigger reloader = new Trigger("a.ms", 2, new Event.Load(), new Block(List.of(park(), drop("a.ms"))));
        registry.replace(List.of(new ParsedScript("a.ms", List.of(repacker, reloader), List.of())));
        dispatcher.onLoad();
        ticks(20);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void reloadingOneFileFromInsideAResumePassLeavesAnotherFilesParkedFrameAlive() {
        Trigger reloader = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(park(), drop("a.ms"))));
        Trigger other = new Trigger("b.ms", 1, new Event.Load(), new Block(List.of(park(), say("b"))));
        registry.replace(List.of(
                new ParsedScript("a.ms", List.of(reloader), List.of()),
                new ParsedScript("b.ms", List.of(other), List.of())));
        dispatcher.onLoad();
        ticks(2);
        assertEquals(List.of("b"), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aLaterParkedFrameOfTheReloadedFileIsSkippedWhenAnEarlierFrameInThePassReloadsIt() {
        Trigger reloader = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(park(), drop("a.ms"))));
        Trigger victim = new Trigger("a.ms", 2, new Event.Load(), new Block(List.of(park(), say("victim"))));
        registry.replace(List.of(new ParsedScript("a.ms", List.of(reloader, victim), List.of())));
        dispatcher.onLoad();
        ticks(20);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aSurvivingParkedFrameOfTheReloadedFileIsNotPutBackWhenThePassReloadsItsFile() {
        Condition sneaking = context -> game.sneaking;
        Statement waitForSneak = statement(5, context -> new Flow.Park(sneaking, 1000));
        Trigger survivor = new Trigger("a.ms", 1, new Event.Load(), new Block(List.of(waitForSneak, say("survivor"))));
        Trigger reloader = new Trigger("a.ms", 2, new Event.Load(), new Block(List.of(park(), drop("a.ms"))));
        registry.replace(List.of(new ParsedScript("a.ms", List.of(survivor, reloader), List.of())));
        dispatcher.onLoad();
        ticks(1);
        game.sneaking = true;
        ticks(20);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.errors);
    }
}
