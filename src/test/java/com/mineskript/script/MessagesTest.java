package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.syntax.DefaultSyntax;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MessagesTest {
    private static List<String> text(List<MessageLine> lines) {
        return lines.stream().map(line -> line.kind() + " " + line.text()).toList();
    }

    private static ParsedScript parse(String file, String source) {
        return new Parser(DefaultSyntax.registry()).parse(file, source);
    }

    @Test
    void aCleanFullReloadIsOneSuccessLineWithTheTime() {
        LoadReport report = new LoadReport(List.of(parse("a.ms", "on load:\n    send \"a\"\n")));
        assertEquals(List.of("SUCCESS loaded 1 script, 1 trigger (7 ms)"), text(Messages.reloaded(report, 7)));
    }

    @Test
    void aFullReloadWithErrorsIsOneWarningLineWithTheCount() {
        LoadReport report = new LoadReport(List.of(parse("a.ms", "on load:\n    fly\n")));
        assertEquals(List.of("WARNING loaded 1 script, 0 triggers, 1 error (7 ms)"), text(Messages.reloaded(report, 7)));
    }

    @Test
    void eachSingleFileOutcomeHasItsOwnWording(@TempDir Path dir) {
        ParseError error = new ParseError("Chat.ms", 2, "unknown effect \"fly\"");
        assertEquals(List.of("SUCCESS reloaded Chat.ms, 3 triggers (4 ms)"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.RELOADED, List.of(), 3, 4), dir)));
        assertEquals(List.of("SUCCESS added Chat.ms, 1 trigger (4 ms)"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.ADDED, List.of(), 1, 4), dir)));
        assertEquals(List.of("WARNING added Chat.ms, 0 triggers, 1 error (4 ms)"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.ADDED, List.of(error), 0, 4), dir)));
        assertEquals(List.of(
                        "ERROR did not reload Chat.ms, 1 error (4 ms)",
                        "WARNING the version already running is unchanged, 3 triggers"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.KEPT, List.of(error), 3, 4), dir)));
        assertEquals(List.of("SUCCESS unloaded Chat.ms, it is no longer in the folder (4 ms)"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.REMOVED, List.of(), 0, 4), dir)));
        assertEquals(List.of("ERROR no script named Chat.ms in " + dir + " (4 ms)"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.MISSING, List.of(), 0, 4), dir)));
        assertEquals(List.of("WARNING a reload is already running, ignored"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.BUSY, List.of(), 0, 0), dir)));
    }

    @Test
    void aNameTheGuardTurnedAwaySaysItWasRefusedAndWhy(@TempDir Path dir) {
        assertEquals(List.of("ERROR refused the name ../secret.ms, a reload only reads .ms files directly in " + dir + " (4 ms)"),
                text(Messages.fileReload(new FileReload("../secret.ms", FileReload.Outcome.REFUSED, List.of(), 0, 4), dir)));
        assertEquals(List.of("ERROR refused the name notes.txt, a reload only reads .ms files directly in " + dir + " (4 ms)"),
                text(Messages.fileReload(new FileReload("notes.txt", FileReload.Outcome.REFUSED, List.of(), 0, 4), dir)));
        assertEquals(List.of("ERROR no script named nope.ms in " + dir + " (4 ms)"),
                text(Messages.fileReload(new FileReload("nope.ms", FileReload.Outcome.MISSING, List.of(), 0, 4), dir)));
    }

    @Test
    void aKeptFileWithNoTriggersSaysNothingIsRunningInsteadOfUnchanged(@TempDir Path dir) {
        ParseError error = new ParseError("Chat.ms", 2, "unknown effect \"fly\"");
        assertEquals(List.of(
                        "ERROR did not reload Chat.ms, 1 error (4 ms)",
                        "WARNING nothing from Chat.ms is running, it has no triggers"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.KEPT, List.of(error), 0, 4), dir)));
        assertEquals(List.of(
                        "ERROR did not reload Chat.ms, 1 error (4 ms)",
                        "WARNING the version already running is unchanged, 1 trigger"),
                text(Messages.fileReload(new FileReload("Chat.ms", FileReload.Outcome.KEPT, List.of(error), 1, 4), dir)));
    }

    @Test
    void anErrorCarriesItsSourceLineIndentedUnderIt(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve("a.ms"), "on load:\n    fly\n", StandardCharsets.UTF_8);
        List<ParseError> errors = List.of(
                new ParseError("a.ms", 2, "unknown effect \"fly\""),
                new ParseError("gone.ms", 9, "nothing there"));
        assertEquals(List.of(
                "ERROR 2 errors from the last load",
                "ERROR a.ms:2: unknown effect \"fly\"",
                "DETAIL     fly",
                "ERROR gone.ms:9: nothing there"), text(Messages.errors("the last load", errors, new ScriptSources(dir))));
        assertEquals(List.of(
                "ERROR 1 error from a.ms",
                "ERROR a.ms:2: unknown effect \"fly\"",
                "DETAIL     fly"), text(Messages.errors("a.ms", errors.subList(0, 1), new ScriptSources(dir))));
    }

    @Test
    void noErrorsIsItsOwnLine(@TempDir Path dir) {
        assertEquals(List.of("SUCCESS no errors from the last load"), text(Messages.errors("the last load", List.of(), new ScriptSources(dir))));
        assertEquals(List.of("SUCCESS no errors from a.ms"), text(Messages.errors("a.ms", List.of(), new ScriptSources(dir))));
    }

    @Test
    void listNamesTheFolderWhenEmptyAndOneLinePerScriptOtherwise(@TempDir Path dir) {
        assertEquals(List.of("WARNING no scripts loaded from " + dir), text(Messages.list(dir, List.of(), List.of())));
        ParsedScript clean = parse("a.ms", "on load:\n    send \"a\"\non chat:\n    send \"c\"\n");
        ParsedScript broken = parse("b.ms", "on load:\n    fly\n");
        assertEquals(List.of(
                "SUCCESS 2 scripts loaded from " + dir,
                "INFO a.ms, 2 triggers",
                "WARNING b.ms, 0 triggers, 1 error"), text(Messages.list(dir, List.of(clean, broken), broken.errors())));
    }

    @Test
    void infoNamesTheVersionFolderCountsAndTicks(@TempDir Path dir) {
        assertEquals(List.of(
                "SUCCESS MineSkript 1.0.0-alpha",
                "INFO folder " + dir,
                "INFO 2 scripts, 5 triggers",
                "INFO 1200 ticks since the last full reload"), text(Messages.info("1.0.0-alpha", dir, 2, 5, 1200)));
        assertEquals("INFO 1 tick since the last full reload", text(Messages.info("1.0.0-alpha", dir, 0, 0, 1)).get(3));
    }

    @Test
    void helpHasOneLinePerBranchOfTheTree() {
        List<String> lines = text(Messages.help());
        assertEquals(11, lines.size());
        assertEquals("SUCCESS the command tree, /mineskript or /ms", lines.get(0));
        assertEquals("INFO /ms reload <file.ms>, reload one script", lines.get(4));
        assertEquals("INFO /ms reload config, re-read config.txt", lines.get(7));
        assertEquals("INFO /ms info, version, folder, counts and ticks since the last full reload", lines.get(10));
    }

    @Test
    void everyReloadAnnouncesWhatItIsAboutToDo(@TempDir Path dir) {
        assertEquals(List.of("INFO reloading every script"), text(Messages.startingScripts()));
        assertEquals(List.of("INFO reloading the config, variables and every script"), text(Messages.startingEverything()));
        assertEquals(List.of("INFO reloading config.txt"), text(Messages.startingConfig(dir.resolve("config.txt"))));
        assertEquals(List.of("INFO reloading variables.json"), text(Messages.startingVariables(dir.resolve("variables.json"))));
        assertEquals(List.of("INFO reloading Chat.ms"), text(Messages.startingFile("Chat.ms")));
    }

    @Test
    void aReloadTurnedAwayByTheGuardSaysSoInsteadOfReportingCounts() {
        assertEquals(List.of("WARNING a reload is already running, ignored"), text(Messages.reloaded(null, 0)));
        assertEquals(List.of("WARNING a reload is already running, ignored"), text(Messages.reloaded(null, 812)));
    }

    @Test
    void variablesReloadNamesTheFileOrSaysWhyItDidNot(@TempDir Path dir) {
        assertEquals(List.of("SUCCESS re-read variables.json (2 ms)"),
                text(Messages.variablesReloaded(dir.resolve("variables.json"), VariablesReload.RELOADED, 2)));
        assertEquals(List.of("WARNING a reload is already running, ignored"),
                text(Messages.variablesReloaded(dir.resolve("variables.json"), VariablesReload.BUSY, 0)));
        assertEquals(List.of("ERROR did not re-read variables.json, it could not be read (2 ms)"),
                text(Messages.variablesReloaded(dir.resolve("variables.json"), VariablesReload.UNREADABLE, 2)));
    }
}
