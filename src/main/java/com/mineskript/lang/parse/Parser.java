package com.mineskript.lang.parse;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.AndCondition;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.IfChain;
import com.mineskript.lang.ast.LoopKind;
import com.mineskript.lang.ast.LoopStatement;
import com.mineskript.lang.ast.OrCondition;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.lexer.LexResult;
import com.mineskript.lang.lexer.Lexer;
import com.mineskript.lang.lexer.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private final Map<String, Optional<Condition>> conditionCache = new HashMap<>();

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

    public ParsedEffect parseEffect(String file, int line, Event event, String text) {
        expressions.clearCache();
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return new ParsedEffect(null, new ParseError(file, line, "there is nothing here to run"));
        }
        Node node = new Node(trimmed, line, false, List.of());
        try {
            return new ParsedEffect(parseEffect(node, new ParseScope(file, line, event)), null);
        } catch (Failure failure) {
            return new ParsedEffect(null, new ParseError(file, failure.line, failure.getMessage()));
        }
    }

    private Trigger parseTrigger(String file, Node node) {
        if (!node.section()) {
            throw new Failure(node.line(), "expected an event section");
        }
        ParseScope scope = new ParseScope(file, node.line(), null);
        Event event = guarded(node, () -> registry.matchFirst(registry.events(), tokens(node, node.text()), expressions, scope)
                .orElseThrow(() -> new Failure(node.line(), "unknown event \"" + node.text() + "\"")));
        Block body = parseBlock(node.children(), new ParseScope(file, node.line(), event));
        return new Trigger(file, node.line(), event, body);
    }

    private Block parseBlock(List<Node> nodes, ParseScope scope) {
        List<Statement> statements = new ArrayList<>();
        int i = 0;
        while (i < nodes.size()) {
            Node node = nodes.get(i);
            if (!node.section()) {
                statements.add(parseEffect(node, at(scope, node)));
                i++;
                continue;
            }
            String head = node.text();
            if (startsWith(head, "if ")) {
                i = parseIfChain(nodes, i, scope, statements);
                continue;
            }
            if (startsWith(head, "while ")) {
                Condition condition = parseCondition(node, head.substring(6), at(scope, node));
                Block body = parseBlock(node.children(), nested(scope, LoopKind.WHILE));
                statements.add(new LoopStatement(node.line(), new LoopStatement.While(condition), body));
                i++;
                continue;
            }
            if (startsWith(head, "loop ")) {
                LoopStatement.Kind kind = parseLoopHeader(node, head.substring(5), at(scope, node));
                LoopKind loopKind = kind instanceof LoopStatement.Times ? LoopKind.TIMES : LoopKind.OVER;
                Block body = parseBlock(node.children(), nested(scope, loopKind));
                statements.add(new LoopStatement(node.line(), kind, body));
                i++;
                continue;
            }
            if (startsWith(head, "else if ") || head.equalsIgnoreCase("else")) {
                throw new Failure(node.line(), "\"else\" without a matching \"if\"");
            }
            if (head.equalsIgnoreCase("then")) {
                throw new Failure(node.line(), "\"then\" without \"if all\" or \"if any\"");
            }
            throw new Failure(node.line(), "unknown section \"" + head + "\"");
        }
        return new Block(List.copyOf(statements));
    }

    private int parseIfChain(List<Node> nodes, int index, ParseScope scope, List<Statement> statements) {
        Node first = nodes.get(index);
        List<IfChain.Branch> branches = new ArrayList<>();
        Block otherwise = null;
        int next = parseBranch(nodes, index, first.text().substring(3), scope, branches);
        while (next < nodes.size() && otherwise == null) {
            Node node = nodes.get(next);
            if (!node.section()) {
                break;
            }
            if (startsWith(node.text(), "else if ")) {
                next = parseBranch(nodes, next, node.text().substring(8), scope, branches);
            } else if (node.text().equalsIgnoreCase("else")) {
                otherwise = parseBlock(node.children(), scope);
                next++;
            } else {
                break;
            }
        }
        statements.add(new IfChain(first.line(), List.copyOf(branches), otherwise));
        return next;
    }

    private int parseBranch(List<Node> nodes, int index, String conditionText, ParseScope scope, List<IfChain.Branch> branches) {
        Node node = nodes.get(index);
        String trimmed = conditionText.trim();
        boolean all = trimmed.equalsIgnoreCase("all");
        boolean any = trimmed.equalsIgnoreCase("any");
        if (!all && !any) {
            branches.add(new IfChain.Branch(parseCondition(node, conditionText, at(scope, node)), parseBlock(node.children(), scope)));
            return index + 1;
        }
        String keyword = all ? "if all" : "if any";
        List<Condition> conditions = new ArrayList<>();
        for (Node child : node.children()) {
            if (child.section()) {
                throw new Failure(child.line(), "\"" + keyword + "\" may not contain sections");
            }
            conditions.add(parseCondition(child, child.text(), at(scope, child)));
        }
        if (conditions.size() < 2) {
            throw new Failure(node.line(), "\"" + keyword + "\" needs at least two conditions");
        }
        Node then = index + 1 < nodes.size() ? nodes.get(index + 1) : null;
        if (then == null || !then.section() || !then.text().equalsIgnoreCase("then")) {
            throw new Failure(node.line(), "\"" + keyword + "\" must be followed by \"then\"");
        }
        Condition combined = all ? new AndCondition(List.copyOf(conditions)) : new OrCondition(List.copyOf(conditions));
        branches.add(new IfChain.Branch(combined, parseBlock(then.children(), scope)));
        return index + 2;
    }

    private LoopStatement.Kind parseLoopHeader(Node node, String text, ParseScope scope) {
        List<Token> tokens = tokens(node, text);
        if (tokens.size() == 1 && !tokens.get(0).quoted()) {
            switch (tokens.get(0).text()) {
                case "once" -> {
                    return new LoopStatement.Times(new ConstantExpression(SkType.NUMBER, 1.0));
                }
                case "twice" -> {
                    return new LoopStatement.Times(new ConstantExpression(SkType.NUMBER, 2.0));
                }
                case "thrice" -> {
                    return new LoopStatement.Times(new ConstantExpression(SkType.NUMBER, 3.0));
                }
                default -> {
                }
            }
        }
        if (tokens.size() >= 2) {
            Token last = tokens.get(tokens.size() - 1);
            if (last.is("time") || last.is("times")) {
                List<Token> count = tokens.subList(0, tokens.size() - 1);
                Expression expression = guarded(node, () -> expressions.parse(count, List.of(SkType.NUMBER), scope))
                        .orElseThrow(() -> new Failure(node.line(), "loop needs a number of times or a list"));
                if (expression.isList()) {
                    throw new Failure(node.line(), "loop needs a number of times or a list");
                }
                return new LoopStatement.Times(expression);
            }
        }
        Expression list = guarded(node, () -> expressions.parse(tokens, List.of(SkType.OBJECT), scope))
                .orElseThrow(() -> new Failure(node.line(), "loop needs a number of times or a list"));
        if (!list.isList() && !(list instanceof VariableExpression)) {
            throw new Failure(node.line(), "loop needs a number of times or a list");
        }
        return new LoopStatement.Over(list);
    }

    private Condition parseCondition(Node node, String text, ParseScope scope) {
        List<Token> tokens = tokens(node, text);
        conditionCache.clear();
        return guarded(node, () -> combine(tokens, scope))
                .orElseThrow(() -> new Failure(node.line(), "unknown condition \"" + text.trim() + "\""));
    }

    private Optional<Condition> combine(List<Token> tokens, ParseScope scope) {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        String key = tokens.toString();
        Optional<Condition> cached = conditionCache.get(key);
        if (cached != null) {
            return cached;
        }
        Optional<Condition> result = combineUncached(tokens, scope);
        conditionCache.put(key, result);
        return result;
    }

    private Optional<Condition> combineUncached(List<Token> tokens, ParseScope scope) {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        Optional<Condition> whole = registry.matchFirst(registry.conditions(), tokens, expressions, scope);
        if (whole.isPresent()) {
            return whole;
        }
        Optional<Condition> split = splitAt(tokens, "or", scope, OrCondition::new);
        if (split.isPresent()) {
            return split;
        }
        split = splitAt(tokens, "and", scope, AndCondition::new);
        if (split.isPresent()) {
            return split;
        }
        if (Arithmetic.wrapped(tokens)) {
            return combine(tokens.subList(1, tokens.size() - 1), scope);
        }
        return Optional.empty();
    }

    private Optional<Condition> splitAt(List<Token> tokens, String word, ParseScope scope, Function<List<Condition>, Condition> combinator) {
        int depth = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.is("(")) {
                depth++;
            } else if (token.is(")")) {
                depth--;
            } else if (depth == 0 && token.is(word) && i > 0 && i < tokens.size() - 1) {
                Optional<Condition> left = combine(tokens.subList(0, i), scope);
                if (left.isEmpty()) {
                    continue;
                }
                Optional<Condition> right = combine(tokens.subList(i + 1, tokens.size()), scope);
                if (right.isEmpty()) {
                    continue;
                }
                return Optional.of(combinator.apply(List.of(left.get(), right.get())));
            }
        }
        return Optional.empty();
    }

    private Statement parseEffect(Node node, ParseScope scope) {
        String text = node.text();
        if (startsWith(text, "wait until ")) {
            return new WaitUntil(scope.line(), parseCondition(node, text.substring(11), scope));
        }
        if (startsWith(text, "halt until ")) {
            return new WaitUntil(scope.line(), parseCondition(node, text.substring(11), scope));
        }
        return guarded(node, () -> registry.matchFirst(registry.effects(), tokens(node, text), expressions, scope)
                .orElseThrow(() -> new Failure(node.line(), "unknown effect \"" + text + "\"")));
    }

    private static ParseScope at(ParseScope scope, Node node) {
        return new ParseScope(scope.file(), node.line(), scope.event(), scope.loopDepth(), scope.loop());
    }

    private static ParseScope nested(ParseScope scope, LoopKind kind) {
        return new ParseScope(scope.file(), scope.line(), scope.event(), scope.loopDepth() + 1, kind);
    }

    private static List<Token> tokens(Node node, String text) {
        try {
            return Tokenizer.tokenize(text);
        } catch (TokenizeException error) {
            throw new Failure(node.line(), error.getMessage());
        }
    }

    private static <T> T guarded(Node node, Supplier<T> action) {
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
