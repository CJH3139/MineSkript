package com.mineskript.script;

import com.mineskript.lang.Language;
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

    /** The one line every reload shows when another reload is still running. */
    private static List<MessageLine> busy() {
        return List.of(new MessageLine(MessageLine.Kind.WARNING, Language.get("command.busy")));
    }

    public static boolean typedTheAlias(String input) {
        String text = input.strip();
        int space = text.indexOf(' ');
        return (space < 0 ? text : text.substring(0, space)).equals(ALIAS);
    }

    public static List<MessageLine> unknownBranch() {
        return List.of(new MessageLine(MessageLine.Kind.ERROR, Language.get("command.unknown")));
    }

    public static List<MessageLine> help() {
        return List.of(
                new MessageLine(MessageLine.Kind.SUCCESS, Language.get("command.help.title")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.help")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.reload")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.reload-scripts")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.reload-file")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.reload-variables")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.reload-all")),
                new MessageLine(MessageLine.Kind.INFO, Language.format("command.help.reload-config", Config.NAME)),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.list")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.errors")),
                new MessageLine(MessageLine.Kind.INFO, Language.get("command.help.info")));
    }

    public static List<MessageLine> startingScripts() {
        return starting(Language.get("command.starting.scripts"));
    }

    public static List<MessageLine> startingFile(String file) {
        return starting(file);
    }

    public static List<MessageLine> startingVariables(Path file) {
        return starting(file.getFileName().toString());
    }

    public static List<MessageLine> startingEverything() {
        return starting(Language.get("command.starting.everything"));
    }

    private static List<MessageLine> starting(String what) {
        return List.of(new MessageLine(MessageLine.Kind.INFO, Language.format("command.starting", what)));
    }

    public static List<MessageLine> reloaded(LoadReport report, long millis) {
        if (report == null) {
            return busy();
        }
        String scripts = LoadReport.plural(report.scriptCount(), "script");
        String triggers = LoadReport.plural(report.triggerCount(), "trigger");
        int errors = report.errors().size();
        if (errors == 0) {
            return List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                    Language.format("command.reloaded", scripts, triggers, millis)));
        }
        return List.of(new MessageLine(MessageLine.Kind.WARNING, Language.format("command.reloaded-with-errors",
                scripts, triggers, LoadReport.plural(errors, "error"), millis)));
    }

    public static List<MessageLine> fileReload(FileReload result, Path dir) {
        String triggers = LoadReport.plural(result.triggerCount(), "trigger");
        String errors = LoadReport.plural(result.errors().size(), "error");
        long millis = result.millis();
        return switch (result.outcome()) {
            case RELOADED -> List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                    Language.format("command.file.reloaded", result.file(), triggers, millis)));
            case ADDED -> result.errors().isEmpty()
                    ? List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                            Language.format("command.file.added", result.file(), triggers, millis)))
                    : List.of(new MessageLine(MessageLine.Kind.WARNING,
                            Language.format("command.file.added-with-errors",
                                    result.file(), triggers, errors, millis)));
            case KEPT -> List.of(
                    new MessageLine(MessageLine.Kind.ERROR,
                            Language.format("command.file.kept", result.file(), errors, millis)),
                    new MessageLine(MessageLine.Kind.WARNING, result.triggerCount() == 0
                            ? Language.format("command.file.kept-no-triggers", result.file())
                            : Language.format("command.file.kept-unchanged", triggers)));
            case REMOVED -> List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                    Language.format("command.file.removed", result.file(), millis)));
            case MISSING -> List.of(new MessageLine(MessageLine.Kind.ERROR,
                    Language.format("command.file.missing", result.file(), dir, millis)));
            case REFUSED -> List.of(new MessageLine(MessageLine.Kind.ERROR,
                    Language.format("command.file.refused", result.file(), dir, millis)));
            case BUSY -> busy();
        };
    }

    public static List<MessageLine> variablesReloaded(Path file, VariablesReload result, long millis) {
        return switch (result) {
            case RELOADED -> List.of(new MessageLine(MessageLine.Kind.SUCCESS,
                    Language.format("command.reread", file.getFileName(), millis)));
            case UNREADABLE -> List.of(new MessageLine(MessageLine.Kind.ERROR,
                    Language.format("command.reread-failed", file.getFileName(), millis)));
            case BUSY -> busy();
        };
    }

    public static List<MessageLine> startingConfig(Path file) {
        return starting(file.getFileName().toString());
    }

    public static List<MessageLine> configReloaded(Path file, ConfigReload result, List<String> warnings, long millis) {
        if (result == ConfigReload.BUSY) {
            return busy();
        }
        List<MessageLine> lines = new ArrayList<>();
        if (result == ConfigReload.RELOADED) {
            lines.add(new MessageLine(MessageLine.Kind.SUCCESS,
                    Language.format("command.reread", file.getFileName(), millis)));
        } else {
            lines.add(new MessageLine(MessageLine.Kind.ERROR,
                    Language.format("command.reread-failed", file.getFileName(), millis)));
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
            return List.of(new MessageLine(MessageLine.Kind.WARNING, Language.format("command.list.empty", dir)));
        }
        List<MessageLine> lines = new ArrayList<>();
        lines.add(new MessageLine(MessageLine.Kind.SUCCESS,
                Language.format("command.list.title", LoadReport.plural(scripts.size(), "script"), dir)));
        for (ParsedScript script : scripts) {
            String triggers = LoadReport.plural(script.triggers().size(), "trigger");
            int count = 0;
            for (ParseError error : errors) {
                if (error.file().equals(script.file())) {
                    count++;
                }
            }
            if (count == 0) {
                lines.add(new MessageLine(MessageLine.Kind.INFO,
                        Language.format("command.list.script", script.file(), triggers)));
            } else {
                lines.add(new MessageLine(MessageLine.Kind.WARNING, Language.format("command.list.script-with-errors",
                        script.file(), triggers, LoadReport.plural(count, "error"))));
            }
        }
        return List.copyOf(lines);
    }

    public static List<MessageLine> errors(String origin, List<ParseError> errors, ScriptSources sources) {
        if (errors.isEmpty()) {
            return List.of(new MessageLine(MessageLine.Kind.SUCCESS, Language.format("command.errors.none", origin)));
        }
        List<MessageLine> lines = new ArrayList<>();
        lines.add(new MessageLine(MessageLine.Kind.ERROR,
                Language.format("command.errors.title", LoadReport.plural(errors.size(), "error"), origin)));
        for (ParseError error : errors) {
            lines.add(new MessageLine(MessageLine.Kind.ERROR, error.toString()));
            String source = sources.line(error.file(), error.line());
            if (!source.isEmpty()) {
                lines.add(new MessageLine(MessageLine.Kind.DETAIL, "    " + source));
            }
        }
        return List.copyOf(lines);
    }

    /** The /ms info lines, ending with the addons that are loaded. */
    public static List<MessageLine> info(String version, Path dir, int scripts, int triggers, long ticks,
            List<String> addons) {
        List<MessageLine> lines = new ArrayList<>(info(version, dir, scripts, triggers, ticks));
        lines.add(new MessageLine(MessageLine.Kind.INFO, addons.isEmpty()
                ? Language.get("command.info.no-addons")
                : Language.format("command.info.addons",
                        LoadReport.plural(addons.size(), "addon"), String.join(", ", addons))));
        return List.copyOf(lines);
    }

    public static List<MessageLine> info(String version, Path dir, int scripts, int triggers, long ticks) {
        return List.of(
                new MessageLine(MessageLine.Kind.SUCCESS, Language.format("command.info.version", version)),
                new MessageLine(MessageLine.Kind.INFO, Language.format("command.info.folder", dir)),
                new MessageLine(MessageLine.Kind.INFO, Language.format("command.info.counts",
                        LoadReport.plural(scripts, "script"), LoadReport.plural(triggers, "trigger"))),
                new MessageLine(MessageLine.Kind.INFO,
                        Language.format(ticks == 1 ? "command.info.tick" : "command.info.ticks", ticks)));
    }
}
