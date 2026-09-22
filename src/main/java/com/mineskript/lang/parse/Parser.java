package com.mineskript.lang.parse;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.IfChain;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.lexer.LexResult;
import com.mineskript.lang.lexer.Lexer;
import com.mineskript.lang.lexer.Node;
import java.util.ArrayList;
import java.util.List;

public final class Parser {
    private static final class Failure extends RuntimeException {
        private final int line;

        private Failure(int line, String message) {
            super(message);
            this.line = line;
        }
    }

    private final SyntaxRegistry registry;
    private final ExpressionParser expressions;

    public Parser(SyntaxRegistry registry) {
        this.registry = registry;
        this.expressions = new ExpressionParser(registry);
    }

    public ParsedScript parse(String file, String source) {
        expressions.clearCache();
        LexResult lexed = Lexer.lex(file, source);
        List<ParseError> errors = new ArrayList<>(lexed.errors());
        List<Trigger> triggers = new ArrayList<>();
        for (Node node : lexed.nodes()) {
            try {
                triggers.add(parseTrigger(file, node));
            } catch (Failure failure) {
                errors.add(new ParseError(file, failure.line, failure.getMessage()));
            }
        }
        errors.sort((a, b) -> Integer.compare(a.line(), b.line()));
        return new ParsedScript(file, List.copyOf(triggers), List.copyOf(errors));
    }

    private Trigger parseTrigger(String file, Node node) {
        if (!node.section()) {
            throw new Failure(node.line(), "expected an event section");
        }
        ParseScope scope = new ParseScope(file, node.line(), null);
        Event event = guarded(node, () -> registry.matchFirst(registry.events(), tokens(node, node.text()), expressions, scope)
                .orElseThrow(() -> new Failure(node.line(), "unknown event \"" + node.text() + "\"")));
        Block body = parseBlock(file, node.children(), event);
        return new Trigger(file, node.line(), event, body);
    }

    private Block parseBlock(String file, List<Node> nodes, Event event) {
        List<Statement> statements = new ArrayList<>();
        int i = 0;
        while (i < nodes.size()) {
            Node node = nodes.get(i);
            if (node.section() && startsWith(node.text(), "if ")) {
                List<IfChain.Branch> branches = new ArrayList<>();
                Block otherwise = null;
                branches.add(new IfChain.Branch(parseCondition(file, node, node.text().substring(3), event), parseBlock(file, node.children(), event)));
                int line = node.line();
                i++;
                while (i < nodes.size() && nodes.get(i).section() && otherwise == null) {
                    Node next = nodes.get(i);
                    if (startsWith(next.text(), "else if ")) {
                        branches.add(new IfChain.Branch(parseCondition(file, next, next.text().substring(8), event), parseBlock(file, next.children(), event)));
                    } else if (next.text().equalsIgnoreCase("else")) {
                        otherwise = parseBlock(file, next.children(), event);
                    } else {
                        break;
                    }
                    i++;
                }
                statements.add(new IfChain(line, List.copyOf(branches), otherwise));
                continue;
            }
            if (node.section()) {
                if (startsWith(node.text(), "else if ") || node.text().equalsIgnoreCase("else")) {
                    throw new Failure(node.line(), "\"else\" without a matching \"if\"");
                }
                throw new Failure(node.line(), "unknown section \"" + node.text() + "\"");
            }
            statements.add(parseEffect(file, node, event));
            i++;
        }
        return new Block(List.copyOf(statements));
    }

    private Condition parseCondition(String file, Node node, String text, Event event) {
        ParseScope scope = new ParseScope(file, node.line(), event);
        return guarded(node, () -> registry.matchFirst(registry.conditions(), tokens(node, text), expressions, scope)
                .orElseThrow(() -> new Failure(node.line(), "unknown condition \"" + text.trim() + "\"")));
    }

    private Statement parseEffect(String file, Node node, Event event) {
        ParseScope scope = new ParseScope(file, node.line(), event);
        return guarded(node, () -> registry.matchFirst(registry.effects(), tokens(node, node.text()), expressions, scope)
                .orElseThrow(() -> new Failure(node.line(), "unknown effect \"" + node.text() + "\"")));
    }

    private static List<Token> tokens(Node node, String text) {
        try {
            return Tokenizer.tokenize(text);
        } catch (TokenizeException error) {
            throw new Failure(node.line(), error.getMessage());
        }
    }

    private static <T> T guarded(Node node, java.util.function.Supplier<T> action) {
        try {
            return action.get();
        } catch (SyntaxException error) {
            throw new Failure(node.line(), error.getMessage());
        }
    }

    private static boolean startsWith(String text, String prefix) {
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
