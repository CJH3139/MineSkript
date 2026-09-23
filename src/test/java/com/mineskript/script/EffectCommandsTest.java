package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.parse.ParsedEffect;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.ScriptError;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EffectCommandsTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Scheduler scheduler = new Scheduler();
    private final Variables variables = new Variables();
    private final List<String> saves = new ArrayList<>();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), scheduler, variables,
            () -> saves.add("save"));
    private final ConfigFile config = new ConfigFile(Path.of("config.txt"));
    private final EffectCommands effects = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, config, game);

    private void load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
        registry.replace(List.of(script));
    }

    private void typeInChat(String line) {
        if (effects.allowChat(line)) {
            dispatcher.onChatSend(line);
        }
    }

    private void ticks(int count) {
        for (int i = 0; i < count; i++) {
            dispatcher.tick();
        }
    }

    @Test
    void aPrefixedLineRunsAndIsCancelled() {
        assertFalse(effects.allowChat("?send \"hello\""));
        assertEquals(List.of("hello"), game.messages);
        assertEquals(List.of("ran send \"hello\""), game.infos);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void anOrdinaryLineIsLeftAloneAndNothingIsPrinted() {
        assertTrue(effects.allowChat("hello everyone"));
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.infos);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aSpaceAfterThePrefixIsFine() {
        assertFalse(effects.allowChat("?   send \"hi\""));
        assertEquals(List.of("hi"), game.messages);
        assertEquals(List.of("ran send \"hi\""), game.infos);
    }

    @Test
    void aFailedParseRunsNothingAndShowsTheErrorInTheUsualShape() {
        assertEquals(EffectCommands.Outcome.FAILED, effects.run("?frobnicate the widget"));
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.infos);
        assertEquals(List.of("effect command: unknown effect \"frobnicate the widget\""), game.errors);
    }

    @Test
    void aFailedParseStillCancelsTheLineSoTheServerNeverSeesIt() {
        assertFalse(effects.allowChat("?frobnicate the widget"));
    }

    @Test
    void aBarePrefixIsSwallowedAndAnsweredWithAHintRatherThanSent() {
        assertEquals(EffectCommands.Outcome.EMPTY, effects.run("?"));
        assertEquals(List.of(), game.errors);
        assertEquals(List.of("type an effect after ? to run it, like ?send \"hello\""), game.infos);
        assertFalse(effects.allowChat("?   "));
        assertEquals(2, game.infos.size());
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aLineThatParsesAndThenThrowsShowsTheErrorAndNeverSaysItRan() {
        SyntaxRegistry syntax = new SyntaxRegistry();
        syntax.addEffect((match, scope) -> Optional.<Statement>of(new Statement() {
            @Override
            public int line() {
                return scope.line();
            }

            @Override
            public Flow execute(Context context) {
                throw new ScriptError("the effect gave up halfway");
            }
        }), "give up halfway");
        DefaultSyntax.registerAll(syntax);
        EffectCommands throwing = new EffectCommands(new Parser(syntax), dispatcher, config, game);
        assertEquals(EffectCommands.Outcome.FAILED, throwing.run("?give up halfway"));
        assertEquals(List.of("effect command: the effect gave up halfway"), game.errors);
        assertEquals(List.of(), game.infos);
        assertFalse(throwing.allowChat("?give up halfway"));
    }

    @Test
    void anEventValueIsRefusedWithAMessageThatSaysWhy() {
        assertEquals(EffectCommands.Outcome.FAILED, effects.run("?send \"%event-damage%\""));
        assertEquals(List.of("effect command: event-damage needs an event, and an effect command typed in chat has none"), game.errors);
    }

    @Test
    void anEffectCommandDoesNotFireOnChatSend() {
        load("on chat send:\n    send \"sent %message%\"\n");
        dispatcher.tick();
        typeInChat("?send \"quiet\"");
        assertEquals(List.of("quiet"), game.messages);
        typeInChat("?frobnicate the widget");
        assertEquals(List.of("quiet"), game.messages);
        typeInChat("?");
        assertEquals(List.of("quiet"), game.messages);
        typeInChat("hello everyone");
        assertEquals(List.of("quiet", "sent hello everyone"), game.messages);
        assertEquals(List.of("effect command: unknown effect \"frobnicate the widget\""), game.errors);
    }

    @Test
    void ramVariablesAreSharedWithARunningScript() {
        load("on chat:\n    send \"block is %{-block}%\"\n");
        dispatcher.tick();
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?set {-block} to cobblestone"));
        dispatcher.onChat("anything");
        assertEquals(List.of("block is cobblestone"), game.messages);
    }

    @Test
    void globalVariablesAreSharedWithARunningScriptAndAreSeenAsAChange() {
        load("on chat:\n    send \"count is %{count}%\"\n");
        dispatcher.tick();
        int before = variables.version();
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?set {count} to 4"));
        assertEquals(4.0, variables.global().get("count"));
        assertTrue(variables.version() != before);
        dispatcher.onChat("anything");
        assertEquals(List.of("count is 4"), game.messages);
        assertEquals(List.of(), saves);
        ticks(EventDispatcher.SAVE_DELAY_TICKS + 2);
        assertEquals(List.of("save"), saves);
    }

    @Test
    void aScriptCanSetAValueAnEffectCommandThenReads() {
        load("on chat:\n    set {-seen} to \"yes\"\n");
        dispatcher.tick();
        dispatcher.onChat("anything");
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?send \"seen is %{-seen}%\""));
        assertEquals(List.of("seen is yes"), game.messages);
    }

    @Test
    void waitParksTheEffectCommandsFrameAndItResumesOnSchedule() {
        dispatcher.tick();
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait 5 ticks"));
        assertEquals(1, scheduler.size());
        assertEquals(List.of("ran wait 5 ticks"), game.infos);
        ticks(4);
        assertEquals(1, scheduler.size());
        ticks(2);
        assertEquals(0, scheduler.size());
    }

    @Test
    void waitUntilParksTheEffectCommandsFrameAndItResumesWhenTheConditionIsTrue() {
        dispatcher.tick();
        game.sneaking = false;
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait until player is sneaking"));
        assertEquals(1, dispatcher.parkedSize());
        ticks(3);
        assertEquals(1, dispatcher.parkedSize());
        game.sneaking = true;
        ticks(2);
        assertEquals(0, dispatcher.parkedSize());
        assertEquals(List.of(), game.errors);
    }

    @Test
    void waitUntilTimesOutIfTheConditionNeverBecomesTrue() {
        dispatcher.tick();
        game.sneaking = false;
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait until player is sneaking"));
        ticks(WaitUntil.TIMEOUT_TICKS + 2);
        assertEquals(0, dispatcher.parkedSize());
        assertEquals(List.of("effect command: wait until timed out after 30 seconds"), game.errors);
    }

    @Test
    void aParkedEffectCommandDoesNotBlockTheNextOneTyped() {
        dispatcher.tick();
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait 20 ticks"));
        assertEquals(1, scheduler.size());
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?send \"while the first one is parked\""));
        assertEquals(List.of("while the first one is parked"), game.messages);
        assertEquals(1, scheduler.size());
        ticks(22);
        assertEquals(0, scheduler.size());
    }

    @Test
    void aDisabledFeatureLeavesAPrefixedLineAlone(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("config.txt"), "effect commands: false\n", StandardCharsets.UTF_8);
        ConfigFile off = new ConfigFile(dir.resolve("config.txt"));
        assertTrue(off.load());
        EffectCommands disabled = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, off, game);
        assertTrue(disabled.allowChat("?send \"hello\""));
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.infos);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void anEmptyPrefixLeavesEveryLineAlone(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("config.txt"), "effect command prefix:\n", StandardCharsets.UTF_8);
        ConfigFile empty = new ConfigFile(dir.resolve("config.txt"));
        assertTrue(empty.load());
        EffectCommands off = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, empty, game);
        assertTrue(off.allowChat("send \"hello\""));
        assertEquals(List.of(), game.messages);
    }

    @Test
    void aChangedPrefixTakesEffectOnTheVeryNextLine(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("config.txt"), "effect command prefix: >>\n", StandardCharsets.UTF_8);
        ConfigFile changed = new ConfigFile(dir.resolve("config.txt"));
        assertTrue(changed.load());
        EffectCommands other = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, changed, game);
        assertTrue(other.allowChat("?send \"not mine\""));
        assertFalse(other.allowChat(">>send \"mine\""));
        assertEquals(List.of("mine"), game.messages);
        Files.writeString(dir.resolve("config.txt"), "effect command prefix: !\n", StandardCharsets.UTF_8);
        assertTrue(changed.load());
        assertTrue(other.allowChat(">>send \"not mine either\""));
        assertFalse(other.allowChat("!send \"mine again\""));
        assertEquals(List.of("mine", "mine again"), game.messages);
    }

    @Test
    void anEffectCommandCannotStartAnotherOne() {
        Loop loop = new Loop();
        assertEquals(EffectCommands.Outcome.RAN, loop.effects.run("?set {-inner} to \"?set {-reached} to 1\""));
        assertEquals(EffectCommands.Outcome.RAN, loop.effects.run("?make player say \"%{-inner}%\""));
        assertEquals(List.of("chat:?set {-reached} to 1"), loop.game.calls);
        assertEquals(List.of("effect command: an effect command cannot start another effect command"), loop.game.errors);
        assertFalse(loop.variables.ram().containsKey("reached"));
    }

    @Test
    void anEffectCommandThatReachesAReloadWhileOneIsRunningIsRefusedByTheSameGuard(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("a.ms"),
                "on script load:\n    set {-command} to \"/ms reload config\"\n    make player say \"?execute command {-command}\"\n",
                StandardCharsets.UTF_8);
        Reload reload = new Reload(root);
        reload.start();
        assertEquals(List.of("config:BUSY"), reload.seen);
    }

    @Test
    void anEffectCommandThatReachesAReloadWithNothingRunningIsAllowed(@TempDir Path root) throws IOException {
        Files.writeString(root.resolve("a.ms"), "on load:\n    send \"loaded\"\n", StandardCharsets.UTF_8);
        Reload reload = new Reload(root);
        reload.start();
        reload.seen.clear();
        assertEquals(EffectCommands.Outcome.RAN, reload.effects.run("?execute command \"/ms reload config\""));
        assertEquals(List.of("config:RELOADED"), reload.seen);
    }

    @Test
    void aParserThatThrowsIsAnsweredInTheUsualShapeAndTheLineIsStillCancelled() {
        SyntaxRegistry syntax = new SyntaxRegistry();
        syntax.addEffect((match, scope) -> {
            throw new IllegalStateException("the parser fell over");
        }, "fall over");
        DefaultSyntax.registerAll(syntax);
        EffectCommands brittle = new EffectCommands(new Parser(syntax), dispatcher, config, game);
        assertEquals(EffectCommands.Outcome.FAILED, brittle.run("?fall over"));
        assertEquals(List.of("effect command: the parser fell over"), game.errors);
        assertEquals(List.of(), game.infos);
        assertEquals(List.of(), game.messages);
        assertFalse(brittle.allowChat("?fall over"));
    }

    @Test
    void aParserThatOverflowsTheStackIsAnsweredRatherThanReachingTheGame() {
        SyntaxRegistry syntax = new SyntaxRegistry();
        syntax.addEffect((match, scope) -> overflow(), "go too deep");
        DefaultSyntax.registerAll(syntax);
        EffectCommands brittle = new EffectCommands(new Parser(syntax), dispatcher, config, game);
        assertEquals(EffectCommands.Outcome.FAILED, brittle.run("?go too deep"));
        assertEquals(List.of("effect command: StackOverflowError"), game.errors);
        assertEquals(List.of(), game.infos);
    }

    @Test
    void aThrowOutOfTheRunnerLeavesTheNextLineFreeToRun() {
        FakeGameBridge bridge = new FakeGameBridge();
        GameBridge brittle = (GameBridge) Proxy.newProxyInstance(GameBridge.class.getClassLoader(),
                new Class<?>[] {GameBridge.class}, new Brittle(bridge));
        EventDispatcher own = new EventDispatcher(registry, brittle, new Interpreter(10_000), new Scheduler(), variables, () -> {
        });
        EffectCommands runner = new EffectCommands(new Parser(DefaultSyntax.registry()), own, config, brittle);
        assertThrows(AssertionError.class, () -> runner.run("?send \"boom\""));
        assertEquals(EffectCommands.Outcome.RAN, runner.run("?set {-after} to 1"));
        assertEquals(List.of(), bridge.errors);
    }

    @Test
    void aNestedEffectCommandIsRefusedAsBusyRatherThanSent() {
        Loop loop = new Loop();
        assertEquals(EffectCommands.Outcome.RAN, loop.effects.run("?set {-inner} to \"?set {-leaked} to 1\""));
        assertEquals(EffectCommands.Outcome.RAN, loop.effects.run("?make player say \"%{-inner}%\""));
        assertEquals(List.of(EffectCommands.Outcome.BUSY), loop.nested);
        assertFalse(loop.variables.ram().containsKey("leaked"));
    }

    @Test
    void theEffectsOwnOutputComesBeforeTheConfirmation() {
        assertFalse(effects.allowChat("?send \"hello\""));
        assertEquals(List.of("message:hello", "info:ran send \"hello\""), game.shown);
    }

    @Test
    void aLineThatThrowsHalfwayShowsItsOutputAndThenOneRedLineAndNothingGreen() {
        SyntaxRegistry syntax = new SyntaxRegistry();
        syntax.addEffect((match, scope) -> Optional.<Statement>of(new Statement() {
            @Override
            public int line() {
                return scope.line();
            }

            @Override
            public Flow execute(Context context) {
                context.game().showMessage("half done");
                throw new ScriptError("the effect gave up halfway");
            }
        }), "give up after sending");
        DefaultSyntax.registerAll(syntax);
        EffectCommands throwing = new EffectCommands(new Parser(syntax), dispatcher, config, game);
        assertEquals(EffectCommands.Outcome.FAILED, throwing.run("?give up after sending"));
        assertEquals(List.of("message:half done", "error:effect command: the effect gave up halfway"), game.shown);
    }

    @Test
    void anEffectCommandWithNoWorldRunsNothingAndSaysWhyRatherThanClaimingItRan() {
        game.hasWorld = false;
        assertEquals(EffectCommands.Outcome.FAILED, effects.run("?send \"hello\""));
        assertEquals(List.of("effect command: there is no world to run in"), game.errors);
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.infos);
        assertFalse(effects.allowChat("?send \"hello\""));
    }

    @Test
    void theDispatcherRefusesAOneOffWhenThereIsNoWorld() {
        ParsedEffect parsed = new Parser(DefaultSyntax.registry())
                .parseEffect(EffectCommands.FILE, 1, new Event.EffectCommand(), "send \"hello\"");
        Trigger trigger = new Trigger(EffectCommands.FILE, 1, new Event.EffectCommand(),
                new Block(List.of(parsed.statement())));
        game.hasWorld = false;
        assertFalse(dispatcher.runOneOff(trigger));
        assertEquals(List.of(), game.messages);
        assertEquals(List.of(), game.infos);
    }

    @Test
    void waitUntilSaysItRanAtOnceAndReportsTheTimeoutLater() {
        dispatcher.tick();
        game.sneaking = false;
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait until player is sneaking"));
        assertEquals(List.of("info:ran wait until player is sneaking"), game.shown);
        ticks(WaitUntil.TIMEOUT_TICKS + 2);
        assertEquals(List.of("info:ran wait until player is sneaking",
                "error:effect command: wait until timed out after 30 seconds"), game.shown);
    }

    private static Optional<Statement> overflow() {
        Optional<Statement> result = overflow();
        return result.isPresent() ? result : Optional.empty();
    }

    private static final class Brittle implements InvocationHandler {
        private final FakeGameBridge game;

        private Brittle(FakeGameBridge game) {
            this.game = game;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("showMessage")) {
                throw new AssertionError("the chat window is gone");
            }
            try {
                return method.invoke(game, args);
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
        }
    }

    private static final class Loop {
        private final FakeGameBridge game = new FakeGameBridge();
        private final ScriptRegistry registry = new ScriptRegistry();
        private final Variables variables = new Variables();
        private final List<EffectCommands.Outcome> nested = new ArrayList<>();
        private final ConfigFile config = new ConfigFile(Path.of("config.txt"));
        private final EventDispatcher dispatcher;
        private final EffectCommands effects;

        private Loop() {
            GameBridge reentrant = (GameBridge) Proxy.newProxyInstance(GameBridge.class.getClassLoader(),
                    new Class<?>[] {GameBridge.class}, new Reentrant(this));
            dispatcher = new EventDispatcher(registry, reentrant, new Interpreter(10_000), new Scheduler(), variables, () -> {
            });
            effects = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, config, reentrant);
        }
    }

    private static final class Reentrant implements InvocationHandler {
        private final Loop loop;

        private Reentrant(Loop loop) {
            this.loop = loop;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result;
            try {
                result = method.invoke(loop.game, args);
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
            if (method.getName().equals("sendChat")) {
                loop.nested.add(loop.effects.run((String) args[0]));
            }
            return result;
        }
    }

    private static final class Reload {
        private final List<String> seen = new ArrayList<>();
        private final FakeGameBridge game = new FakeGameBridge();
        private final ScriptRegistry registry = new ScriptRegistry();
        private final Variables variables = new Variables();
        private final ConfigFile config;
        private final EventDispatcher dispatcher;
        private final EffectCommands effects;
        private final ScriptService service;

        private Reload(Path root) {
            config = new ConfigFile(root.resolve("config.txt"));
            GameBridge bridge = (GameBridge) Proxy.newProxyInstance(GameBridge.class.getClassLoader(),
                    new Class<?>[] {GameBridge.class}, new ReloadHandler(this));
            dispatcher = new EventDispatcher(registry, bridge, new Interpreter(10_000), new Scheduler(), variables, () -> {
            });
            VariablePersistence persistence = new VariablePersistence(new VariableStore(), root.resolve("variables.json"), variables, bridge);
            service = new ScriptService(root, new ScriptLoader(new Parser(DefaultSyntax.registry())), registry, dispatcher, persistence, config);
            effects = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, config, bridge);
        }

        private void start() {
            service.start();
        }
    }

    private static final class ReloadHandler implements InvocationHandler {
        private final Reload reload;

        private ReloadHandler(Reload reload) {
            this.reload = reload;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result;
            try {
                result = method.invoke(reload.game, args);
            } catch (InvocationTargetException failure) {
                throw failure.getCause();
            }
            if (method.getName().equals("sendChat")) {
                reload.effects.allowChat((String) args[0]);
            } else if (method.getName().equals("sendCommand") && args[0].equals("ms reload config")) {
                reload.seen.add("config:" + reload.service.reloadConfig());
            }
            return result;
        }
    }
}
