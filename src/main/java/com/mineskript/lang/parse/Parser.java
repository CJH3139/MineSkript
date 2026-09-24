package com.mineskript.lang.parse;

import com.mineskript.lang.Language;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.AndCondition;
import com.mineskript.lang.ast.Block;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Function;
import com.mineskript.lang.ast.IfChain;
import com.mineskript.lang.ast.LoopKind;
import com.mineskript.lang.ast.LoopStatement;
import com.mineskript.lang.ast.OrCondition;
import com.mineskript.lang.ast.Return;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.ast.TryStatement;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.function.FunctionInfo;
import com.mineskript.lang.function.FunctionParameter;
import com.mineskript.lang.function.NativeFunctionCall;
import com.mineskript.lang.lexer.LexResult;
import com.mineskript.lang.lexer.Lexer;
import com.mineskript.lang.lexer.Node;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.FunctionCall;
import com.mineskript.lang.runtime.Functions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;

public final class Parser {
    private static final class Failure extends RuntimeException {
        private final int line;

        private Failure(int line, String message) {
            super(message);
            this.line = line;
        }
    }

    private record Prepared(List<Node> nodes, List<ParseError> errors) {
    }

    private record Declared(Node node, Function function) {
    }

    private static final java.util.regex.Pattern HEADER = java.util.regex.Pattern.compile(
            "(?i)(local\\s+)?function\\s+(.*?)\\s*\\((.*)\\)\\s*(?:(?:::|returns?)\\s*(.*))?");
    private static final java.util.regex.Pattern PARAMETER = java.util.regex.Pattern.compile(
            "(?i)([a-z_][a-z0-9_]*)\\s*(?::\\s*([a-z ]+?))?\\s*(?:=\\s*(.+))?");
    private static final java.util.regex.Pattern NAME = java.util.regex.Pattern.compile("[a-z_][a-z0-9_]*");
    private static final java.util.regex.Pattern OPTION = java.util.regex.Pattern.compile("\\{@([^}]*)}");

    private static final java.util.Set<String> COMPARISONS = java.util.Set.of("<", ">", "<=", ">=", "=", "!=");

    private final SyntaxRegistry registry;
    private final ExpressionParser expressions;
    private final Functions functions;
    private final Map<String, Optional<Condition>> conditionCache = new HashMap<>();
    private Map<String, Function> fileFunctions = Map.of();
    private Function current;

    public Parser(SyntaxRegistry registry) {
        this(registry, new Functions());
    }

    public Parser(SyntaxRegistry registry, Functions functions) {
        this.registry = registry;
        this.functions = functions;
        this.expressions = new ExpressionParser(registry);
        expressions.attach(this);
    }

    public Functions functions() {
        return functions;
    }

    public void declare(String file, String source) {
        expressions.clearCache();
        List<Function> declared = new ArrayList<>();
        for (Declared entry : declareAll(file, prepare(file, source).nodes(), new ArrayList<>())) {
            declared.add(entry.function());
        }
        functions.declare(file, declared);
    }

    public ParsedScript parse(String file, String source) {
        expressions.clearCache();
        conditionCache.clear();
        Prepared prepared = prepare(file, source);
        List<ParseError> errors = new ArrayList<>(prepared.errors());
        List<Trigger> triggers = new ArrayList<>();
        List<Function> loaded = new ArrayList<>();
        List<Declared> declared = declareAll(file, prepared.nodes(), errors);
        Map<String, Function> own = new LinkedHashMap<>();
        for (Declared entry : declared) {
            own.put(entry.function().name(), entry.function());
        }
        fileFunctions = own;
        try {
            for (Declared entry : declared) {
                try {
                    entry.function().define(parseFunctionBody(file, entry.node(), entry.function()));
                    loaded.add(entry.function());
                } catch (Failure failure) {
                    errors.add(new ParseError(file, failure.line, failure.getMessage()));
                }
            }
            for (Node node : prepared.nodes()) {
                if (isFunctionHeader(node) || isOptions(node)) {
                    continue;
                }
                try {
                    triggers.add(parseTrigger(file, node));
                } catch (Failure failure) {
                    errors.add(new ParseError(file, failure.line, failure.getMessage()));
                }
            }
        } finally {
            fileFunctions = Map.of();
        }
        errors.sort((a, b) -> Integer.compare(a.line(), b.line()));
        return new ParsedScript(file, List.copyOf(triggers), List.copyOf(errors), List.copyOf(loaded));
    }

    private Prepared prepare(String file, String source) {
        LexResult lexed = Lexer.lex(file, source);
        List<ParseError> errors = new ArrayList<>(lexed.errors());
        Map<String, String> options = new HashMap<>();
        for (Node node : lexed.nodes()) {
            if (!isOptions(node)) {
                continue;
            }
            for (Node option : node.children()) {
                int colon = option.text().indexOf(':');
                if (option.section() || colon <= 0) {
                    errors.add(new ParseError(file, option.line(), Language.get("parse.option-format")));
                    continue;
                }
                options.put(option.text().substring(0, colon).strip().toLowerCase(Locale.ROOT), option.text().substring(colon + 1).strip());
            }
        }
        List<Node> nodes = new ArrayList<>();
        for (Node node : lexed.nodes()) {
            try {
                nodes.add(isOptions(node) ? node : substitute(node, options));
            } catch (Failure failure) {
                errors.add(new ParseError(file, failure.line, failure.getMessage()));
            }
        }
        return new Prepared(nodes, errors);
    }

    private static Node substitute(Node node, Map<String, String> options) {
        Matcher matcher = OPTION.matcher(node.text());
        StringBuilder text = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group(1).strip().toLowerCase(Locale.ROOT);
            String value = options.get(name);
            if (value == null) {
                throw new Failure(node.line(), Language.format("parse.unknown-option", name));
            }
            matcher.appendReplacement(text, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(text);
        List<Node> children = new ArrayList<>();
        for (Node child : node.children()) {
            children.add(substitute(child, options));
        }
        return new Node(text.toString(), node.line(), node.section(), List.copyOf(children));
    }

    private static boolean isOptions(Node node) {
        return node.section() && node.text().equalsIgnoreCase("options");
    }

    private static boolean isFunctionHeader(Node node) {
        return startsWith(node.text(), "function ") || startsWith(node.text(), "local function ");
    }

    private List<Declared> declareAll(String file, List<Node> nodes, List<ParseError> errors) {
        List<Declared> declared = new ArrayList<>();
        Map<String, Function> seen = new HashMap<>();
        for (Node node : nodes) {
            if (!isFunctionHeader(node)) {
                continue;
            }
            try {
                Function function = declareFunction(file, node);
                if (registry.function(function.name()).isPresent()) {
                    throw new Failure(node.line(), Language.format("parse.function-is-built-in", function.name()));
                }
                Function twin = seen.get(function.name());
                if (twin != null) {
                    throw new Failure(node.line(), Language.format("parse.function-defined-on-line", function.name(), twin.line()));
                }
                if (!function.local()) {
                    Optional<Function> clash = functions.clashWith(function.name(), file);
                    if (clash.isPresent()) {
                        throw new Failure(node.line(), Language.format("parse.function-defined-in-file", function.name(), clash.get().file()));
                    }
                }
                seen.put(function.name(), function);
                declared.add(new Declared(node, function));
            } catch (Failure failure) {
                errors.add(new ParseError(file, failure.line, failure.getMessage()));
            }
        }
        return declared;
    }

    private Function declareFunction(String file, Node node) {
        if (!node.section()) {
            throw new Failure(node.line(), Language.get("parse.function-needs-body"));
        }
        Matcher header = HEADER.matcher(node.text());
        if (!header.matches()) {
            throw new Failure(node.line(), Language.get("parse.function-header-format"));
        }
        String name = header.group(2).toLowerCase(Locale.ROOT);
        if (!NAME.matcher(name).matches()) {
            throw new Failure(node.line(), Language.format("parse.invalid-function-name", header.group(2)));
        }
        ParseScope scope = new ParseScope(file, node.line(), null);
        List<Function.Parameter> parameters = new ArrayList<>();
        String list = header.group(3).strip();
        if (!list.isEmpty()) {
            for (String raw : splitParameters(list)) {
                parameters.add(parameter(node, raw.strip(), parameters, scope));
            }
        }
        SkType returnType = header.group(4) == null ? null : typeNamed(node, header.group(4));
        return new Function(name, file, node.line(), header.group(1) != null, parameters, returnType);
    }

    private Function.Parameter parameter(Node node, String raw, List<Function.Parameter> earlier, ParseScope scope) {
        Matcher matcher = PARAMETER.matcher(raw);
        if (!matcher.matches()) {
            throw new Failure(node.line(), Language.format("parse.parameter-format", raw));
        }
        String name = matcher.group(1).toLowerCase(Locale.ROOT);
        for (Function.Parameter other : earlier) {
            if (other.name().equals(name)) {
                throw new Failure(node.line(), Language.format("parse.parameter-twice", name));
            }
        }
        SkType type = matcher.group(2) == null ? SkType.OBJECT : typeNamed(node, matcher.group(2));
        String fallbackText = matcher.group(3);
        Expression fallback = null;
        if (fallbackText != null) {
            fallback = guarded(node, () -> expressions.parse(tokens(node, fallbackText), List.of(type), scope))
                    .orElseThrow(() -> new Failure(node.line(), Language.format("parse.bad-default-value", fallbackText.strip())));
        } else if (!earlier.isEmpty() && earlier.getLast().fallback() != null) {
            throw new Failure(node.line(), Language.format("parse.parameter-needs-default", name));
        }
        return new Function.Parameter(name, type, fallback);
    }

    private static SkType typeNamed(Node node, String name) {
        SkType type = Pattern.typeNamed(name.strip());
        if (type == null) {
            throw new Failure(node.line(), Language.format("parse.unknown-type", name.strip()));
        }
        return type;
    }

    private static List<String> splitParameters(String list) {
        List<String> parts = new ArrayList<>();
        int depth = 0;
        boolean quoted = false;
        int start = 0;
        for (int i = 0; i < list.length(); i++) {
            char c = list.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (!quoted && c == '(') {
                depth++;
            } else if (!quoted && c == ')') {
                depth--;
            } else if (!quoted && depth == 0 && c == ',') {
                parts.add(list.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(list.substring(start));
        return parts;
    }

    private Block parseFunctionBody(String file, Node node, Function function) {
        current = function;
        try {
            return parseBlock(node.children(), new ParseScope(file, node.line(), null));
        } finally {
            current = null;
        }
    }

    Optional<Expression> functionCall(List<Token> tokens, ParseScope scope) {
        if (!callShaped(tokens)) {
            return Optional.empty();
        }
        Optional<FunctionInfo> builtIn = registry.function(tokens.get(0).text());
        if (builtIn.isPresent()) {
            return Optional.of(nativeCall(builtIn.get(), tokens, scope));
        }
        Optional<Function> found = findFunction(tokens.get(0).text(), scope.file());
        if (found.isEmpty()) {
            return Optional.empty();
        }
        Function function = found.get();
        if (!function.returns()) {
            throw new SyntaxException(Language.format("parse.function-returns-nothing", function.name()));
        }
        return Optional.of(call(function, tokens, scope));
    }

    Optional<Expression> ternary(List<Token> tokens, List<SkType> types, ParseScope scope) {
        int condition = -1;
        int otherwise = -1;
        int depth = 0;
        for (int i = 0; i < tokens.size() && otherwise < 0; i++) {
            Token token = tokens.get(i);
            if (token.is("(")) {
                depth++;
            } else if (token.is(")")) {
                depth--;
            } else if (depth == 0 && condition < 0 && token.is("if")) {
                condition = i;
            } else if (depth == 0 && condition >= 0 && (token.is("else") || token.is("otherwise"))) {
                otherwise = i;
            }
        }
        int conditionEnd = otherwise > 0 && tokens.get(otherwise - 1).is(",") ? otherwise - 1 : otherwise;
        if (condition <= 0 || conditionEnd <= condition + 1 || otherwise >= tokens.size() - 1) {
            return Optional.empty();
        }
        Optional<Condition> test = combine(tokens.subList(condition + 1, conditionEnd), scope);
        if (test.isEmpty()) {
            return Optional.empty();
        }
        Optional<Expression> whenTrue = expressions.parse(tokens.subList(0, condition), types, scope);
        if (whenTrue.isEmpty()) {
            return Optional.empty();
        }
        Optional<Expression> whenFalse = expressions.parse(tokens.subList(otherwise + 1, tokens.size()), types, scope);
        return whenFalse.map(value -> new TernaryExpression(whenTrue.get(), test.get(), value));
    }

    private Optional<Function> findFunction(String name, String file) {
        Function own = fileFunctions.get(name);
        return own != null ? Optional.of(own) : functions.visibleFrom(name, file);
    }

    private FunctionCall call(Function function, List<Token> tokens, ParseScope scope) {
        List<List<Token>> parts = argumentParts(tokens);
        List<Function.Parameter> parameters = function.parameters();
        checkArgumentCount(function.name(), parts.size(), function.requiredParameters(), parameters.size());
        List<Expression> arguments = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            Function.Parameter parameter = parameters.get(i);
            int position = i + 1;
            arguments.add(expressions.parse(parts.get(i), List.of(parameter.type()), scope)
                    .orElseThrow(() -> new SyntaxException(Language.format("parse.argument-type", position, function.name(),
                            Converters.typeName(parameter.type())))));
        }
        return new FunctionCall(function, scope.file(), functions, arguments, scope.line());
    }

    private NativeFunctionCall nativeCall(FunctionInfo function, List<Token> tokens, ParseScope scope) {
        List<List<Token>> parts = argumentParts(tokens);
        List<FunctionParameter> parameters = function.parameters();
        if (parameters.size() == 1 && parameters.get(0).list() && parts.size() > 1) {
            List<Expression> items = new ArrayList<>();
            for (List<Token> part : parts) {
                items.add(listArgument(function, parameters.get(0), part, 1, scope));
            }
            Expression joined = new ListExpression(items, parameters.get(0).type(), false);
            return new NativeFunctionCall(function, List.of(joined), scope.line());
        }
        checkArgumentCount(function.name(), parts.size(), function.requiredParameters(), parameters.size());
        List<Expression> arguments = new ArrayList<>();
        for (int i = 0; i < parts.size(); i++) {
            FunctionParameter parameter = parameters.get(i);
            int position = i + 1;
            if (parameter.list()) {
                arguments.add(listArgument(function, parameter, parts.get(i), position, scope));
                continue;
            }
            Expression argument = expressions.parse(parts.get(i), List.of(parameter.type()), scope)
                    .orElseThrow(() -> new SyntaxException(Language.format("parse.argument-type", position,
                            function.name(), Converters.typeName(parameter.type()))));
            if (argument.isList()) {
                throw new SyntaxException(Language.format("parse.argument-not-list", position, function.name(),
                        Converters.typeName(parameter.type())));
            }
            arguments.add(unconverted(argument));
        }
        return new NativeFunctionCall(function, arguments, scope.line());
    }

    private Expression listArgument(FunctionInfo function, FunctionParameter parameter, List<Token> part, int position,
            ParseScope scope) {
        Optional<Expression> typed = expressions.parse(part, List.of(parameter.type()), scope);
        if (typed.isPresent()) {
            return unconverted(typed.get());
        }
        return expressions.parse(part, List.of(SkType.OBJECT), scope)
                .filter(expression -> expression.type() == SkType.OBJECT)
                .orElseThrow(() -> new SyntaxException(Language.format("parse.argument-type", position,
                        function.name(), Converters.typeName(parameter.type()))));
    }

    private static Expression unconverted(Expression argument) {
        return argument instanceof ConvertedExpression converted ? converted.inner() : argument;
    }

    private static List<List<Token>> argumentParts(List<Token> tokens) {
        List<List<Token>> parts = new ArrayList<>();
        List<Token> inner = tokens.subList(2, tokens.size() - 1);
        if (inner.isEmpty()) {
            return parts;
        }
        int depth = 0;
        int start = 0;
        for (int i = 0; i < inner.size(); i++) {
            Token token = inner.get(i);
            if (token.is("(")) {
                depth++;
            } else if (token.is(")")) {
                depth--;
            } else if (depth == 0 && token.is(",")) {
                parts.add(inner.subList(start, i));
                start = i + 1;
            }
        }
        parts.add(inner.subList(start, inner.size()));
        return parts;
    }

    private static void checkArgumentCount(String name, int given, int required, int total) {
        if (given <= total && given >= required) {
            return;
        }
        String expected = required == total
                ? String.valueOf(total)
                : Language.format("parse.argument-range", required, total);
        throw new SyntaxException(Language.format(total == 1 ? "parse.argument-count-one" : "parse.argument-count-many",
                name, expected, given));
    }

    private String withPositionHint(String message, List<Token> tokens, ParseScope scope) {
        return PositionHint.find(tokens, part -> expressions.parse(part, List.of(SkType.NUMBER), scope)
                        .filter(expression -> !expression.isList()).isPresent())
                .map(numbers -> Language.format("parse.position-hint", message, numbers.get(0), numbers.get(1),
                        numbers.get(2)))
                .orElse(message);
    }

    private static boolean callShaped(List<Token> tokens) {
        if (tokens.size() < 3 || tokens.get(0).quoted() || !NAME.matcher(tokens.get(0).text()).matches() || !tokens.get(1).is("(")) {
            return false;
        }
        return tokens.size() == 3 ? tokens.get(2).is(")") : Arithmetic.wrapped(tokens.subList(1, tokens.size()));
    }

    public ParsedEffect parseEffect(String file, int line, Event event, String text) {
        expressions.clearCache();
        conditionCache.clear();
        String trimmed = text.strip();
        if (trimmed.isEmpty()) {
            return new ParsedEffect(null, new ParseError(file, line, Language.get("parse.nothing-to-run")));
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
            throw new Failure(node.line(), Language.get("parse.expected-event-section"));
        }
        ParseScope scope = new ParseScope(file, node.line(), null);
        Event event = explained(() -> guarded(node, () -> registry.matchFirst(registry.events(),
                tokens(node, node.text()), expressions, scope)
                .orElseThrow(() -> new Failure(node.line(), Language.format("parse.unknown-event", node.text())))));
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
            if (head.equalsIgnoreCase("try")) {
                Block body = parseBlock(node.children(), scope);
                Node next = i + 1 < nodes.size() ? nodes.get(i + 1) : null;
                Block handler = new Block(List.of());
                if (next != null && next.section() && isErrorHandler(next.text())) {
                    handler = parseBlock(next.children(), scope);
                    i++;
                }
                statements.add(new TryStatement(node.line(), body, handler));
                i++;
                continue;
            }
            if (isErrorHandler(head)) {
                throw new Failure(node.line(), Language.format("parse.handler-without-try", head));
            }
            if (startsWith(head, "else if ") || head.equalsIgnoreCase("else")) {
                throw new Failure(node.line(), Language.get("parse.else-without-if"));
            }
            if (head.equalsIgnoreCase("then")) {
                throw new Failure(node.line(), Language.get("parse.then-without-if-all"));
            }
            throw new Failure(node.line(), Language.format("parse.unknown-section", head));
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
                throw new Failure(child.line(), Language.format("parse.no-sections-in", keyword));
            }
            conditions.add(parseCondition(child, child.text(), at(scope, child)));
        }
        if (conditions.size() < 2) {
            throw new Failure(node.line(), Language.format("parse.needs-two-conditions", keyword));
        }
        Node then = index + 1 < nodes.size() ? nodes.get(index + 1) : null;
        if (then == null || !then.section() || !then.text().equalsIgnoreCase("then")) {
            throw new Failure(node.line(), Language.format("parse.needs-then", keyword));
        }
        Condition combined = all ? new AndCondition(List.copyOf(conditions)) : new OrCondition(List.copyOf(conditions));
        branches.add(new IfChain.Branch(combined, parseBlock(then.children(), scope)));
        return index + 2;
    }

    private LoopStatement.Kind parseLoopHeader(Node node, String text, ParseScope scope) {
        return explained(() -> parseLoopHeaderOnce(node, text, scope));
    }

    private LoopStatement.Kind parseLoopHeaderOnce(Node node, String text, ParseScope scope) {
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
                        .orElseThrow(() -> new Failure(node.line(), Language.get("parse.loop-needs-count")));
                if (expression.isList()) {
                    throw new Failure(node.line(), Language.get("parse.loop-needs-count"));
                }
                return new LoopStatement.Times(expression);
            }
        }
        Expression list = guarded(node, () -> expressions.parse(tokens, List.of(SkType.OBJECT), scope))
                .orElseThrow(() -> new Failure(node.line(), Language.get("parse.loop-needs-count")));
        if (!list.isList() && !(list instanceof VariableExpression)) {
            throw new Failure(node.line(), Language.get("parse.loop-needs-count"));
        }
        return new LoopStatement.Over(list);
    }

    private Condition parseCondition(Node node, String text, ParseScope scope) {
        return explained(() -> parseConditionOnce(node, text, scope));
    }

    private Condition parseConditionOnce(Node node, String text, ParseScope scope) {
        List<Token> tokens = tokens(node, text);
        conditionCache.clear();
        return guarded(node, () -> combine(tokens, scope))
                .orElseThrow(() -> new Failure(node.line(),
                        withPositionHint(Language.format("parse.unknown-condition", text.trim()), tokens, scope)));
    }

    Optional<Condition> combine(List<Token> tokens, ParseScope scope) {
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
        Optional<Condition> chain = chain(tokens, scope);
        if (chain.isPresent()) {
            return chain;
        }
        if (tokens.get(0).is("not") && tokens.size() > 1) {
            Optional<Condition> inner = combine(tokens.subList(1, tokens.size()), scope);
            if (inner.isPresent()) {
                Condition negated = inner.get();
                return Optional.of(context -> !negated.test(context));
            }
        }
        if (Arithmetic.wrapped(tokens)) {
            return combine(tokens.subList(1, tokens.size() - 1), scope);
        }
        return truthy(tokens, scope);
    }

    private Optional<Condition> chain(List<Token> tokens, ParseScope scope) {
        List<Integer> operators = new ArrayList<>();
        int depth = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.is("(")) {
                depth++;
            } else if (token.is(")")) {
                depth--;
            } else if (depth == 0 && !token.quoted() && COMPARISONS.contains(token.text())) {
                operators.add(i);
            }
        }
        if (operators.size() < 2) {
            return Optional.empty();
        }
        List<Condition> links = new ArrayList<>();
        int start = 0;
        for (int k = 0; k < operators.size(); k++) {
            int end = k + 1 < operators.size() ? operators.get(k + 1) : tokens.size();
            if (operators.get(k) == start || end == operators.get(k) + 1) {
                return Optional.empty();
            }
            Optional<Condition> link = registry.matchFirst(registry.conditions(), tokens.subList(start, end), expressions, scope);
            if (link.isEmpty()) {
                return Optional.empty();
            }
            links.add(link.get());
            start = operators.get(k) + 1;
        }
        return Optional.of(new AndCondition(List.copyOf(links)));
    }

    private Optional<Condition> truthy(List<Token> tokens, ParseScope scope) {
        Optional<Expression> value = expressions.parse(tokens, List.of(SkType.BOOLEAN), scope);
        if (value.isEmpty() || value.get().isList()) {
            return Optional.empty();
        }
        Expression expression = value.get() instanceof ConvertedExpression converted ? converted.inner() : value.get();
        return Optional.of(context -> Boolean.TRUE.equals(expression.evaluate(context)));
    }

    private Optional<Condition> splitAt(List<Token> tokens, String word, ParseScope scope, java.util.function.Function<List<Condition>, Condition> combinator) {
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
        return explained(() -> parseEffectOnce(node, scope));
    }

    private <T> T explained(Supplier<T> parse) {
        try {
            return parse.get();
        } catch (Failure failure) {
            if (PatternMatcher.exhaustive()) {
                throw failure;
            }
            expressions.clearCache();
            conditionCache.clear();
            try {
                return PatternMatcher.exhaustively(parse);
            } finally {
                expressions.clearCache();
                conditionCache.clear();
            }
        }
    }

    private Statement parseEffectOnce(Node node, ParseScope scope) {
        conditionCache.clear();
        String text = node.text();
        if (startsWith(text, "wait until ") || startsWith(text, "halt until ")) {
            if (!scope.canWait()) {
                throw new Failure(node.line(), Language.get("parse.cannot-wait-here"));
            }
            return new WaitUntil(scope.line(), parseCondition(node, text.substring(11), scope));
        }
        if (text.equalsIgnoreCase("return") || startsWith(text, "return ")) {
            return parseReturn(node, text.substring(6).strip(), scope);
        }
        List<Token> tokens = tokens(node, text);
        if (callShaped(tokens)) {
            Optional<FunctionInfo> builtIn = registry.function(tokens.get(0).text());
            if (builtIn.isPresent()) {
                return guarded(node, () -> nativeCall(builtIn.get(), tokens, scope));
            }
            Optional<Function> function = findFunction(tokens.get(0).text(), scope.file());
            if (function.isPresent()) {
                return guarded(node, () -> call(function.get(), tokens, scope));
            }
        }
        return guarded(node, () -> registry.matchFirst(registry.effects(), tokens, expressions, scope)
                .orElseThrow(() -> new Failure(node.line(), callShaped(tokens)
                        ? Language.format("parse.unknown-function", tokens.get(0).text())
                        : withPositionHint(Language.format("parse.unknown-effect", text), tokens, scope))));
    }

    private Statement parseReturn(Node node, String value, ParseScope scope) {
        if (current == null) {
            throw new Failure(node.line(), Language.get("parse.return-outside-function"));
        }
        if (value.isEmpty()) {
            return new Return(scope.line(), null, null);
        }
        if (!current.returns()) {
            throw new Failure(node.line(), Language.format("parse.function-returns-no-value", current.name()));
        }
        SkType type = current.returnType();
        Expression expression = guarded(node, () -> expressions.parse(tokens(node, value), List.of(type), scope))
                .orElseThrow(() -> new Failure(node.line(), Language.format("parse.unknown-expression", value)));
        return new Return(scope.line(), expression, type);
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

    private static boolean isErrorHandler(String text) {
        return text.equalsIgnoreCase("on error") || text.equalsIgnoreCase("catch");
    }

    private static boolean startsWith(String text, String prefix) {
        return text.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
