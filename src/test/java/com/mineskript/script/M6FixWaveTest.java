package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ParsedEffect;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class M6FixWaveTest {
    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final Variables variables = new Variables();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler(), variables, () -> {
    });
    private final ConfigFile config = new ConfigFile(Path.of(Config.NAME));
    private final EffectCommands effects = new EffectCommands(new Parser(DefaultSyntax.registry()), dispatcher, config, game);

    M6FixWaveTest() {
        game.chatHook = effects::allowChat;
    }

    private void load(String source) {
        ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", source);
        assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
        registry.replace(List.of(script));
    }

    @Test
    void aChatLineAScriptSendsReachesTheServerAndIsNotRunAsAnEffectCommand() {
        load("on load:\n    set {-remote} to \"?set {-executed} to 1\"\n    make player say \"%{-remote}%\"\n");
        dispatcher.onLoad();
        assertEquals(List.of("?set {-executed} to 1"), game.sentChat);
        assertFalse(variables.ram().containsKey("executed"));
        assertEquals(List.of(), game.errors);
        assertEquals(List.of(), game.infos);
    }

    @Test
    void theSameTextTypedByThePlayerStillRunsAsAnEffectCommand() {
        assertFalse(effects.allowChat("?set {-executed} to 1"));
        assertTrue(variables.ram().containsKey("executed"));
        assertEquals(List.of("ran set {-executed} to 1"), game.infos);
        assertEquals(List.of(), game.sentChat);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void aScriptCanSendAServerLineThatHappensToUseThePrefix() {
        load("on load:\n    make player say \"?tpa Steve\"\n");
        dispatcher.onLoad();
        assertEquals(List.of("?tpa Steve"), game.sentChat);
        assertEquals(List.of(), game.errors);
        assertEquals(List.of(), game.infos);
    }

    @Test
    void aScriptChatSendWhileAOneOffFrameIsParkedIsStillNotAnEffectCommand() {
        load("on load:\n    set {-remote} to \"?set {-executed} to 1\"\n    make player say \"%{-remote}%\"\n");
        dispatcher.tick();
        game.sneaking = false;
        assertEquals(EffectCommands.Outcome.RAN, effects.run("?wait until player is sneaking"));
        game.infos.clear();
        dispatcher.onLoad();
        assertEquals(List.of("?set {-executed} to 1"), game.sentChat);
        assertFalse(variables.ram().containsKey("executed"));
        assertEquals(List.of(), game.errors);
        assertEquals(List.of(), game.infos);
    }

    @Test
    void aByteOrderMarkDoesNotTurnTheFirstSettingIntoAnUnknownKey(@TempDir Path dir) throws IOException {
        Path file = dir.resolve(Config.NAME);
        Files.writeString(file, Config.BOM + "effect commands: false\n", StandardCharsets.UTF_8);
        ConfigFile marked = new ConfigFile(file);
        assertTrue(marked.load());
        assertFalse(marked.current().effectCommands());
        assertEquals(List.of(), marked.takeWarnings());
    }

    @Test
    void aByteOrderMarkInFrontOfThePrefixIsNotPartOfThePrefix(@TempDir Path dir) throws IOException {
        Path file = dir.resolve(Config.NAME);
        Files.writeString(file, Config.BOM + "effect command prefix: >>\n", StandardCharsets.UTF_8);
        ConfigFile marked = new ConfigFile(file);
        assertTrue(marked.load());
        assertEquals(">>", marked.current().effectCommandPrefix());
        assertEquals(List.of(), marked.takeWarnings());
    }

    @Test
    void aConfigWarningIsYellowWhenItWaitsForAWorld(@TempDir Path dir) throws IOException {
        Path file = dir.resolve(Config.NAME);
        Files.writeString(file, "fly speed: 9\n", StandardCharsets.UTF_8);
        ConfigFile bad = new ConfigFile(file);
        assertTrue(bad.load());
        bad.flushTo(game);
        assertEquals(List.of("warning:config.txt line 1 is not a setting MineSkript knows, ignored: fly speed: 9"), game.shown);
        assertEquals(List.of(), game.errors);
    }

    @Test
    void anEmptyConfigFileSaysSomethingJustLikeAMissingOne(@TempDir Path dir) throws IOException {
        Path file = dir.resolve(Config.NAME);
        Files.writeString(file, "", StandardCharsets.UTF_8);
        ConfigFile blank = new ConfigFile(file);
        assertTrue(blank.load());
        assertEquals(Config.DEFAULTS, blank.current());
        assertEquals(List.of("config.txt has no settings in it, using the defaults"), blank.takeWarnings());
    }

    @Test
    void aConfigFileOfNothingButCommentsSaysSomethingToo(@TempDir Path dir) throws IOException {
        Path file = dir.resolve(Config.NAME);
        Files.writeString(file, "# everything off\n\n   \n", StandardCharsets.UTF_8);
        ConfigFile blank = new ConfigFile(file);
        assertTrue(blank.load());
        assertEquals(List.of("config.txt has no settings in it, using the defaults"), blank.takeWarnings());
    }

    @Test
    void aConfigFileWithASettingInItSaysNothingExtra(@TempDir Path dir) throws IOException {
        Path file = dir.resolve(Config.NAME);
        Files.writeString(file, "# a comment\neffect commands: false\n", StandardCharsets.UTF_8);
        ConfigFile filled = new ConfigFile(file);
        assertTrue(filled.load());
        assertEquals(List.of(), filled.takeWarnings());
    }

    @Test
    void theDispatcherAnswersFalseForAOneOffItNeverRan() {
        ParsedEffect parsed = new Parser(DefaultSyntax.registry())
                .parseEffect(EffectCommands.FILE, 1, new Event.EffectCommand(), "set {-never} to 1");
        Trigger trigger = new Trigger(EffectCommands.FILE, 1, new Event.EffectCommand(), new Block(List.of(parsed.statement())));
        game.hasWorld = false;
        assertFalse(dispatcher.runOneOff(trigger));
        assertFalse(variables.ram().containsKey("never"));
        assertEquals(List.of(), game.infos);
    }

    @Test
    void everyScriptServiceConstructorIsHandedTheConfigFileItMustShare() {
        for (Constructor<?> constructor : ScriptService.class.getConstructors()) {
            assertTrue(List.of(constructor.getParameterTypes()).contains(ConfigFile.class),
                    "a ScriptService constructor that mints its own ConfigFile: " + constructor);
        }
    }
}
