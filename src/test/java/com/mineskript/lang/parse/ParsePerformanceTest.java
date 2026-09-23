package com.mineskript.lang.parse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import com.mineskript.syntax.DefaultSyntax;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParsePerformanceTest {
    private static final Duration LIMIT = Duration.ofSeconds(2);

    private static final String SOURCE = """
            on load:
                set {w} to "minecraft"
                send "%minimum of 3 and 8%"
                send "%{w} from character 1 to 4%"
                set {n} to random number between 5 and 10
            """;

    @Test
    void theExpressionsThatOnceHungTheParserStillParseAtOnce() {
        assertTimeoutPreemptively(LIMIT, () -> {
            ParsedScript script = new Parser(DefaultSyntax.registry()).parse("t.ms", SOURCE);
            assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
            assertEquals(1, script.triggers().size());
        });
    }

    @Test
    void eachOfThoseExpressionsParsesOnItsOwnWellInsideTheLimit() {
        assertTimeoutPreemptively(LIMIT, () -> {
            Parser parser = new Parser(DefaultSyntax.registry());
            for (String line : List.of(
                    "send \"%minimum of 3 and 8%\"",
                    "send \"%{w} from character 1 to 4%\"",
                    "set {n} to random number between 5 and 10")) {
                ParsedScript script = parser.parse("t.ms", "on load:\n    " + line + "\n");
                assertEquals(List.of(), script.errors().stream().map(Object::toString).toList());
                assertEquals(1, script.triggers().size());
            }
        });
    }
}
