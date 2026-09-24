package com.mineskript.scripttest;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.ParsedScript;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class ScriptTests {
    private static final String ROOT = "scripts";

    private final ScriptTestHarness harness = new ScriptTestHarness();

    @TestFactory
    Stream<DynamicNode> scripts() throws IOException, URISyntaxException {
        Path root = root();
        List<Path> files;
        try (Stream<Path> walk = Files.walk(root)) {
            files = walk.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".ms")).sorted().toList();
        }
        if (files.isEmpty()) {
            throw new AssertionError("no .ms files under " + root);
        }
        List<DynamicNode> nodes = new ArrayList<>();
        for (Path file : files) {
            nodes.add(container(root, file));
        }
        return nodes.stream();
    }

    private static Path root() throws URISyntaxException {
        URL url = ScriptTests.class.getClassLoader().getResource(ROOT);
        if (url == null) {
            throw new AssertionError("the test resources have no " + ROOT + " folder");
        }
        return Path.of(url.toURI());
    }

    private DynamicContainer container(Path root, Path path) throws IOException {
        String file = root.relativize(path).toString().replace(File.separatorChar, '/');
        ParsedScript script = harness.parse(file, Files.readString(path, StandardCharsets.UTF_8));
        if (!script.errors().isEmpty()) {
            List<String> errors = script.errors().stream().map(ParseError::toString).toList();
            return DynamicContainer.dynamicContainer(file, path.toUri(), Stream.of(DynamicTest.dynamicTest("parses",
                    () -> fail(file + " does not parse", errors))));
        }
        List<DynamicTest> tests = new ArrayList<>();
        for (Trigger trigger : ScriptTestHarness.tests(script)) {
            tests.add(DynamicTest.dynamicTest(ScriptTestHarness.label(trigger), path.toUri(), () -> {
                List<String> failures = harness.run(trigger);
                if (!failures.isEmpty()) {
                    fail(file + " " + ScriptTestHarness.label(trigger) + " failed", failures);
                }
            }));
        }
        if (tests.isEmpty()) {
            tests.add(DynamicTest.dynamicTest("has tests", () -> fail(file + " has no test triggers", List.of())));
        }
        return DynamicContainer.dynamicContainer(file, path.toUri(), tests.stream());
    }

    private static void fail(String headline, List<String> lines) {
        StringBuilder message = new StringBuilder(headline);
        for (String line : lines) {
            message.append(System.lineSeparator()).append("    ").append(line);
        }
        throw new AssertionError(message.toString());
    }
}
