package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.game.FakeGameBridge;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigTest {
    @Test
    void anEmptyFileIsTheDefaults() {
        Config.Loaded loaded = Config.parse("");
        assertEquals(Config.DEFAULTS, loaded.config());
        assertEquals(List.of(), loaded.warnings());
        assertTrue(loaded.config().active());
    }

    @Test
    void theTemplateParsesBackToTheDefaults() {
        Config.Loaded loaded = Config.parse(Config.TEMPLATE);
        assertEquals(Config.DEFAULTS, loaded.config());
        assertEquals(List.of(), loaded.warnings());
    }

    @Test
    void bothSettingsAreRead() {
        Config.Loaded loaded = Config.parse("effect commands: false\neffect command prefix: !!\n");
        assertEquals(new Config(false, "!!"), loaded.config());
        assertEquals(List.of(), loaded.warnings());
        assertFalse(loaded.config().active());
    }

    @Test
    void commentsBlankLinesAndOddSpacingAreIgnored() {
        Config.Loaded loaded = Config.parse("# a comment\n\n   \n   EFFECT Commands  :   FALSE   \n# another\n");
        assertEquals(new Config(false, "?"), loaded.config());
        assertEquals(List.of(), loaded.warnings());
    }

    @Test
    void aHashIsAValidPrefixBecauseCommentsAreWholeLinesOnly() {
        assertEquals("#", Config.parse("effect command prefix: #\n").config().effectCommandPrefix());
    }

    @Test
    void aColonIsAValidPrefixBecauseTheSplitTakesTheFirstOne() {
        assertEquals(":", Config.parse("effect command prefix: :\n").config().effectCommandPrefix());
    }

    @Test
    void anUnknownKeyWarnsNamingTheLineAndIsIgnored() {
        Config.Loaded loaded = Config.parse("effect commands: false\nfly speed: 9\n");
        assertEquals(new Config(false, "?"), loaded.config());
        assertEquals(List.of("config.txt line 2 is not a setting MineSkript knows, ignored: fly speed: 9"), loaded.warnings());
    }

    @Test
    void aLineWithNoColonWarnsNamingTheLine() {
        assertEquals(List.of("config.txt line 1 has no \":\", ignored: effect commands true"),
                Config.parse("effect commands true\n").warnings());
    }

    @Test
    void aNonBooleanWarnsAndKeepsTheDefault() {
        Config.Loaded loaded = Config.parse("effect commands: yes\n");
        assertEquals(Config.DEFAULTS, loaded.config());
        assertEquals(List.of("config.txt line 1 wants true or false, ignored: effect commands: yes"), loaded.warnings());
    }

    @Test
    void anEmptyPrefixTurnsEffectCommandsOffWhateverTheOrderOfTheTwoSettings() {
        Config.Loaded first = Config.parse("effect command prefix:\neffect commands: true\n");
        assertFalse(first.config().active());
        assertEquals(List.of("config.txt line 1 has an empty prefix, so effect commands are off, applied as written: effect command prefix:"),
                first.warnings());
        Config.Loaded second = Config.parse("effect commands: true\neffect command prefix:\n");
        assertFalse(second.config().active());
    }

    @Test
    void aSlashPrefixIsKeptAndTheWarningSaysSo() {
        Config.Loaded loaded = Config.parse("effect command prefix: /ms run\n");
        assertEquals("/ms run", loaded.config().effectCommandPrefix());
        assertTrue(loaded.config().active());
        assertEquals(List.of("config.txt line 1 has a prefix starting with \"/\", which the chat box sends as a command,"
                        + " so it never reaches MineSkript, applied as written: effect command prefix: /ms run"),
                loaded.warnings());
    }

    @Test
    void aRepeatedSettingKeepsTheLastOneAndTheWarningSaysWhichLineWon() {
        Config.Loaded loaded = Config.parse("effect command prefix: !\neffect commands: false\neffect command prefix: >>\n");
        assertEquals(new Config(false, ">>"), loaded.config());
        assertEquals(List.of("config.txt line 3 sets \"effect command prefix\" again, so line 3 is the one that counts, not line 1"),
                loaded.warnings());
    }

    @Test
    void aRefusedBooleanDoesNotClaimToHaveBeenOverridden() {
        Config.Loaded loaded = Config.parse("effect commands: yes\neffect commands: false\n");
        assertEquals(new Config(false, "?"), loaded.config());
        assertEquals(List.of("config.txt line 1 wants true or false, ignored: effect commands: yes"), loaded.warnings());
    }

    @Test
    void warningsNameTheFileTheyCameFromRatherThanAFixedName(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("settings.txt"), "fly speed: 9\n", StandardCharsets.UTF_8);
        ConfigFile present = new ConfigFile(dir.resolve("settings.txt"));
        assertTrue(present.load());
        assertEquals(List.of("settings.txt line 1 is not a setting MineSkript knows, ignored: fly speed: 9"),
                present.takeWarnings());
        ConfigFile missing = new ConfigFile(dir.resolve("elsewhere.txt"));
        assertFalse(missing.load());
        assertTrue(missing.takeWarnings().get(0).startsWith("could not read elsewhere.txt, using the defaults: "));
    }

    @Test
    void createWritesTheTemplateOnceAndNeverRewritesIt(@TempDir Path dir) throws IOException {
        ConfigFile config = new ConfigFile(dir.resolve("config.txt"));
        assertTrue(config.create());
        Files.writeString(dir.resolve("config.txt"), "# mine\neffect command prefix: !\n", StandardCharsets.UTF_8);
        assertFalse(config.create());
        assertEquals("# mine\neffect command prefix: !\n", Files.readString(dir.resolve("config.txt"), StandardCharsets.UTF_8));
        assertTrue(config.load());
        assertEquals("!", config.current().effectCommandPrefix());
        assertEquals(List.of(), config.takeWarnings());
    }

    @Test
    void aMissingFileFallsBackToTheDefaultsAndSaysSoOnce(@TempDir Path dir) {
        ConfigFile config = new ConfigFile(dir.resolve("config.txt"));
        assertFalse(config.load());
        assertEquals(Config.DEFAULTS, config.current());
        assertEquals(1, config.takeWarnings().size());
        assertEquals(List.of(), config.takeWarnings());
    }

    @Test
    void warningsWaitForAWorldAndThenPrintOnce(@TempDir Path dir) throws IOException {
        FakeGameBridge game = new FakeGameBridge();
        game.hasWorld = false;
        Files.writeString(dir.resolve("config.txt"), "fly speed: 9\n", StandardCharsets.UTF_8);
        ConfigFile config = new ConfigFile(dir.resolve("config.txt"));
        assertTrue(config.load());
        config.flushTo(game);
        assertEquals(List.of(), game.warnings);
        game.hasWorld = true;
        config.flushTo(game);
        assertEquals(List.of("config.txt line 1 is not a setting MineSkript knows, ignored: fly speed: 9"), game.warnings);
        config.flushTo(game);
        assertEquals(1, game.warnings.size());
        assertEquals(List.of(), game.errors);
    }
}
