package com.mineskript;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
    private static final Path SOURCES = Path.of("src/main/java/com/mineskript");
    private static final Pattern MINECRAFT = Pattern.compile("\\bnet\\.(minecraft|fabricmc)\\.");
    private static final Pattern GAME = Pattern.compile("\\bcom\\.mineskript\\.game\\.|\\.(world|game)\\(\\)");
    private static final Set<String> MINECRAFT_ALLOWED = Set.of("MineSkriptClient.java", "MineSkriptCommand.java",
            "MineSkriptMessages.java");

    @Test
    void theSourcesAreWhereTheTestLooks() throws IOException {
        assertTrue(Files.isDirectory(SOURCES.resolve("common")), SOURCES.toAbsolutePath().toString());
        assertTrue(sources().size() > 100);
    }

    @Test
    void onlyTheGameMixinsAndEntrypointsUseMinecraftOrFabric() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : sources()) {
            Path relative = SOURCES.relativize(file);
            String first = relative.getName(0).toString();
            boolean allowed = first.equals("game") || first.equals("mixin")
                    || relative.getNameCount() == 1 && MINECRAFT_ALLOWED.contains(first);
            if (!allowed) {
                offenders.addAll(matches(file, MINECRAFT));
            }
        }
        assertEquals(List.of(), offenders);
    }

    @Test
    void theCommonModuleNeverTouchesTheGame() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path file : sources()) {
            if (SOURCES.relativize(file).getName(0).toString().equals("common")) {
                offenders.addAll(matches(file, MINECRAFT));
                offenders.addAll(matches(file, GAME));
            }
        }
        assertEquals(List.of(), offenders);
    }

    private static List<Path> sources() throws IOException {
        try (Stream<Path> files = Files.walk(SOURCES)) {
            return files.filter(file -> file.toString().endsWith(".java")).sorted().toList();
        }
    }

    private static List<String> matches(Path file, Pattern pattern) throws IOException {
        List<String> found = new ArrayList<>();
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = pattern.matcher(lines.get(i));
            if (matcher.find()) {
                found.add(SOURCES.relativize(file) + ":" + (i + 1) + ": " + lines.get(i).strip());
            }
        }
        return found;
    }
}
