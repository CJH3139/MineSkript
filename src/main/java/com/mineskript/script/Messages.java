package com.mineskript.script;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Messages {
    public static final String PREFIX = "MineSkript";
    public static final String ALIAS = "ms";
    public static final String INDENT = " ".repeat(PREFIX.length() + 1);

    private Messages() {
    }

    public static boolean typedTheAlias(String input) {
        String text = input.strip();
        int space = text.indexOf(' ');
        return (space < 0 ? text : text.substring(0, space)).equals(ALIAS);
    }

    public static List<MessageLine> unknownBranch() {
        return List.of(new MessageLine(MessageLine.Kind.ERROR, "not a MineSkript command, type /mineskript help for the tree"));
    }

    public static List<MessageLine> help() {
        return List.of(
                new MessageLine(MessageLine.Kind.SUCCESS, "the command tree, /mineskript or /ms"),
                new MessageLine(MessageLine.Kind.INFO, "/ms help, this list"),
                new MessageLine(MessageLine.Kind.INFO, "/ms reload, reload every script"),
                new MessageLine(MessageLine.Kind.INFO, "/ms reload scripts, the same thing spelled out"),
                new MessageLine(MessageLine.Kind.INFO, "/ms reload <file.ms>, reload one script"),
                new MessageLine(MessageLine.Kind.INFO, "/ms reload variables, re-read variables.json"),
                new MessageLine(MessageLine.Kind.INFO, "/ms reload all, the config, variables and every script"),
                new MessageLine(MessageLine.Kind.INFO, "/ms reload config, re-read " + Config.NAME),
                new MessageLine(MessageLine.Kind.INFO, "/ms list, what is loaded now"),
                new MessageLine(MessageLine.Kind.INFO, "/ms errors, the errors from the last load"),
                new MessageLine(MessageLine.Kind.INFO, "/ms info, version, folder, counts and ticks since the last full reload"));
    }

    public static List<MessageLine> startingScripts() {
        return starting("every script");
    }

    public static List<MessageLine> startingFile(String file) {
        return starting(file);
    }

    public static List<MessageLine> startingVariables(Path file) {
        return starting(file.getFileName().toString());
    }

    public static List<MessageLine> startingEverything() {
        return starting("the config, variables and every script");
    }

    private static List<MessageLine> starting(String what) {
        return List.of(new MessageLine(MessageLine.Kind.INFO, "reloading " + what));
    }

    public static List<MessageLine> reloaded(LoadReport report, long millis) {
        if (report == null) {
            return List.of(new MessageLine(MessageLine.Kind.WARNING, "a reload is already running, ignored"));
        }
        String body = "loaded " + LoadReport.plural(report.scriptCount(), "script") + ", " + LoadReport.plural(report.triggerCount(), "trigger");
        int errors = report.errors().size();
        if (errors == 0) {
            return List.of(new MessageLine(MessageLine.Kind.SUCCESS, body + " (" + millis + " ms)"));
        }
        return List.of(new MessageLine(MessageLine.Kind.WARNING, body + ", " + LoadReport.plural(errors, "error") + " (" + millis + " ms)"));
    }

    public static List<MessageLine> fileReload(FileReload result, Path dir) {
        String triggers = LoadReport.plural(result.triggerCount(), "trigger");
        String took = " (" + result.millis() + " ms)";
        return switch (result.outcome()) {
            case RELOADED -> List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                    "reloaded " + result.file() + ", " + triggers + took));
            case ADDED -> result.errors().isEmpty()
                    ? List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                            "added " + result.file() + ", " + triggers + took))
                    : List.of(new MessageLine(MessageLine.Kind.WARNING,
                            "added " + result.file() + ", " + triggers + ", " + LoadReport.plural(result.errors().size(), "error") + took));
            case KEPT -> List.of(
                    new MessageLine(MessageLine.Kind.ERROR,
                            "did not reload " + result.file() + ", " + LoadReport.plural(result.errors().size(), "error") + took),
                    new MessageLine(MessageLine.Kind.WARNING, result.triggerCount() == 0
                            ? "nothing from " + result.file() + " is running, it has no triggers"
                            : "the version already running is unchanged, " + triggers));
            case REMOVED -> List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                    "unloaded " + result.file() + ", it is no longer in the folder" + took));
            case MISSING -> List.of(new MessageLine(MessageLine.Kind.ERROR,
                    "no script named " + result.file() + " in " + dir + took));
            case REFUSED -> List.of(new MessageLine(MessageLine.Kind.ERROR,
                    "refused the name " + result.file() + ", a reload only reads .ms files directly in " + dir + took));
            case BUSY -> List.of(new MessageLine(MessageLine.Kind.WARNING, "a reload is already running, ignored"));
        };
    }

    public static List<MessageLine> variablesReloaded(Path file, VariablesReload result, long millis) {
        String took = " (" + millis + " ms)";
        return switch (result) {
            case RELOADED -> List.of(new MessageLine(MessageLine.Kind.SUCCESS, "re-read " + file.getFileName() + took));
            case UNREADABLE -> List.of(new MessageLine(MessageLine.Kind.ERROR,
                    "did not re-read " + file.getFileName() + ", it could not be read" + took));
            case BUSY -> List.of(new MessageLine(MessageLine.Kind.WARNING, "a reload is already running, ignored"));
        };
    }

    public static List<MessageLine> startingConfig(Path file) {
        return starting(file.getFileName().toString());
    }

    public static List<MessageLine> configReloaded(Path file, ConfigReload result, List<String> warnings, long millis) {
        String took = " (" + millis + " ms)";
        if (result == ConfigReload.BUSY) {
            return List.of(new MessageLine(MessageLine.Kind.WARNING, "a reload is already running, ignored"));
        }
        List<MessageLine> lines = new ArrayList<>();
        if (result == ConfigReload.RELOADED) {
            lines.add(new MessageLine(MessageLine.Kind.SUCCESS, "re-read " + file.getFileName() + took));
        } else {
            lines.add(new MessageLine(MessageLine.Kind.ERROR, "did not re-read " + file.getFileName() + ", it could not be read" + took));
        }
        lines.addAll(configWarnings(warnings));
        return List.copyOf(lines);
    }

    public static List<MessageLine> configWarnings(List<String> warnings) {
        List<MessageLine> lines = new ArrayList<>();
        for (String warning : warnings) {
            lines.add(new MessageLine(MessageLine.Kind.WARNING, warning));
        }
        return List.copyOf(lines);
    }

    public static List<MessageLine> list(Path dir, List<ParsedScript> scripts, List<ParseError> errors) {
        if (scripts.isEmpty()) {
            return List.of(new MessageLine(MessageLine.Kind.WARNING, "no scripts loaded from " + dir));
        }
        List<MessageLine> lines = new ArrayList<>();
        lines.add(new MessageLine(MessageLine.Kind.SUCCESS, LoadReport.plural(scripts.size(), "script") + " loaded from " + dir));
        for (ParsedScript script : scripts) {
            String text = script.file() + ", " + LoadReport.plural(script.triggers().size(), "trigger");
            int count = 0;
            for (ParseError error : errors) {
                if (error.file().equals(script.file())) {
                    count++;
                }
            }
            if (count == 0) {
                lines.add(new MessageLine(MessageLine.Kind.INFO, text));
            } else {
                lines.add(new MessageLine(MessageLine.Kind.WARNING, text + ", " + LoadReport.plural(count, "error")));
            }
        }
        return List.copyOf(lines);
    }

    public static List<MessageLine> errors(String origin, List<ParseError> errors, ScriptSources sources) {
        if (errors.isEmpty()) {
            return List.of(new MessageLine(MessageLine.Kind.SUCCESS, "no errors from " + origin));
        }
        List<MessageLine> lines = new ArrayList<>();
        lines.add(new MessageLine(MessageLine.Kind.ERROR, LoadReport.plural(errors.size(), "error") + " from " + origin));
        for (ParseError error : errors) {
            lines.add(new MessageLine(MessageLine.Kind.ERROR, error.toString()));
            String source = sources.line(error.file(), error.line());
            if (!source.isEmpty()) {
                lines.add(new MessageLine(MessageLine.Kind.DETAIL, "    " + source));
            }
        }
        return List.copyOf(lines);
    }

    public static List<MessageLine> info(String version, Path dir, int scripts, int triggers, long ticks) {
        return List.of(
                new MessageLine(MessageLine.Kind.SUCCESS, "MineSkript " + version),
                new MessageLine(MessageLine.Kind.INFO, "folder " + dir),
                new MessageLine(MessageLine.Kind.INFO, LoadReport.plural(scripts, "script") + ", " + LoadReport.plural(triggers, "trigger")),
                new MessageLine(MessageLine.Kind.INFO, ticks + (ticks == 1 ? " tick" : " ticks") + " since the last full reload"));
    }
}
