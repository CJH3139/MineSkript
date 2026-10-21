package com.mineskript.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.Structure;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Token;
import com.mineskript.lang.parse.Tokenizer;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Execution;
import com.mineskript.syntax.DefaultSyntax;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class AddonHooksTest {
    private final SyntaxRegistry registry = DefaultSyntax.registry();
    private final List<String> seen = new ArrayList<>();

    @Test
    void tokensKeepTheirOriginalSpelling() {
        List<Token> tokens = Tokenizer.tokenize("Minecraft.getInstance() Steve's");
        assertEquals(List.of("minecraft.getinstance", "(", ")", "steve", "'s"), tokens.stream().map(Token::text).toList());
        assertEquals(List.of("Minecraft.getInstance", "(", ")", "Steve", "'s"),
                tokens.stream().map(Token::original).toList());
        assertEquals(new Token("steve", false), tokens.get(3));
    }

    @Test
    void aStructureGetsItsLinesBeforeTheTriggersAndIsNotATrigger() {
        registry.addScriptListener((file, source) -> seen.add("script " + file + " " + source.length()));
        registry.addStructure("import", structure -> {
            seen.add("header " + structure.header() + " at " + structure.line());
            for (Structure.Entry entry : structure.entries()) {
                seen.add(entry.line() + " " + entry.text());
            }
        });
        String source = "import:\n    java.lang.System\n    Foo.Bar as FB\non load:\n    send \"hi\"\n";
        ParsedScript script = new Parser(registry).parse("t.ms", source);
        assertEquals(List.of(), script.errors());
        assertEquals(1, script.triggers().size());
        assertEquals(List.of("script t.ms " + source.length(), "header import at 1", "2 java.lang.System",
                "3 Foo.Bar as FB"), seen);
    }

    @Test
    void aStructureThatThrowsIsAParseErrorOnItsHeader() {
        registry.addStructure("import", structure -> {
            throw new SyntaxException("not allowed");
        });
        ParsedScript script = new Parser(registry).parse("t.ms", "on load:\n    send \"a\"\nimport:\n    x\n");
        assertEquals(List.of("t.ms:3: not allowed"), script.errors().stream().map(Object::toString).toList());
    }

    @Test
    void theLanguagesOwnWordsCannotBeStructures() {
        assertThrows(IllegalArgumentException.class, () -> registry.addStructure("on", structure -> {
        }));
        registry.addStructure("import", structure -> {
        });
        assertThrows(IllegalArgumentException.class, () -> registry.addStructure("IMPORT", structure -> {
        }));
    }

    @Test
    void rawExpressionsAndEffectsReadTokensNothingElseUnderstood() {
        registry.addRawExpression((tokens, resolver, scope) -> tokens.size() == 1 && tokens.get(0).original().equals("Magic.Value")
                ? Optional.of(new ConstantExpression(SkType.NUMBER, 42.0)) : Optional.empty());
        registry.addRawEffect((tokens, resolver, scope) -> tokens.size() == 1 && tokens.get(0).original().startsWith("Do.")
                ? Optional.of(new Statement() {
                    @Override
                    public int line() {
                        return scope.line();
                    }

                    @Override
                    public Flow execute(Context context) {
                        context.game().showMessage("did " + tokens.get(0).original());
                        return Flow.CONTINUE;
                    }
                }) : Optional.empty());
        ParsedScript script = new Parser(registry).parse("t.ms",
                "on load:\n    send \"%Magic.Value + 1%\"\n    Do.Thing\n    send \"%magic.value%\"\n");
        assertEquals(List.of("t.ms:4: unknown expression \"magic.value\" in string"),
                script.errors().stream().map(Object::toString).toList());
        FakeGameBridge game = new FakeGameBridge();
        script = new Parser(registry).parse("t.ms", "on load:\n    send \"%Magic.Value + 1%\"\n    Do.Thing\n");
        new Interpreter(1000).run(new Execution(script.triggers().get(0), new Context(game, "t.ms", Map.of())));
        assertEquals(List.of("43", "did Do.Thing"), game.messages);
    }
}
