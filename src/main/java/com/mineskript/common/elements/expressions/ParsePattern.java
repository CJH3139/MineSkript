package com.mineskript.common.elements.expressions;

import com.mineskript.lang.ast.NamedValues;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Literals;
import com.mineskript.lang.parse.Token;
import com.mineskript.lang.parse.TokenizeException;
import com.mineskript.lang.parse.Tokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

final class ParsePattern {
    enum Kind {
        TEXT(SkType.TEXT),
        NUMBER(SkType.NUMBER),
        INTEGER(SkType.NUMBER),
        BOOLEAN(SkType.BOOLEAN),
        TIMESPAN(SkType.TIMESPAN),
        ITEMTYPE(SkType.BLOCKTYPE),
        GAMEMODE(SkType.GAMEMODE),
        POTIONEFFECTTYPE(SkType.POTIONEFFECTTYPE),
        ENCHANTMENTTYPE(SkType.ENCHANTMENTTYPE),
        ENTITYTYPE(SkType.ENTITYTYPE),
        WEATHERTYPE(SkType.WEATHERTYPE);

        private final SkType type;

        Kind(SkType type) {
            this.type = type;
        }

        SkType type() {
            return type;
        }
    }

    record Slot(Kind kind, boolean plural) {
    }

    private sealed interface Element {
    }

    private record Literal(String text) implements Element {
    }

    private record Sequence(List<Element> elements) implements Element {
    }

    private record Optionally(Element element) implements Element {
    }

    private record Choice(List<Element> branches) implements Element {
    }

    private record Hole(int index) implements Element {
    }

    private interface Continuation {
        boolean run(int position);
    }

    private static final Map<String, Kind> KINDS = Map.ofEntries(
            Map.entry("string", Kind.TEXT),
            Map.entry("text", Kind.TEXT),
            Map.entry("number", Kind.NUMBER),
            Map.entry("integer", Kind.INTEGER),
            Map.entry("long", Kind.INTEGER),
            Map.entry("boolean", Kind.BOOLEAN),
            Map.entry("timespan", Kind.TIMESPAN),
            Map.entry("itemtype", Kind.ITEMTYPE),
            Map.entry("blocktype", Kind.ITEMTYPE),
            Map.entry("gamemode", Kind.GAMEMODE),
            Map.entry("potioneffecttype", Kind.POTIONEFFECTTYPE),
            Map.entry("enchantmenttype", Kind.ENCHANTMENTTYPE),
            Map.entry("entitytype", Kind.ENTITYTYPE),
            Map.entry("entitydata", Kind.ENTITYTYPE),
            Map.entry("weathertype", Kind.WEATHERTYPE));

    private static final Pattern LIST_SEPARATOR =
            Pattern.compile("\\s*,\\s*(?:(?:and|or)\\s+)?|\\s+(?:and|or|n)\\s+", Pattern.CASE_INSENSITIVE);

    private final String source;
    private final Element root;
    private final List<Slot> slots;

    private ParsePattern(String source, Element root, List<Slot> slots) {
        this.source = source;
        this.root = root;
        this.slots = List.copyOf(slots);
    }

    static ParsePattern compile(String source) {
        Compiler compiler = new Compiler(source);
        Element root = compiler.sequence();
        if (compiler.position != source.length()) {
            throw new IllegalArgumentException("unexpected '" + source.charAt(compiler.position) + "' in the pattern");
        }
        if (compiler.slots.isEmpty()) {
            throw new IllegalArgumentException("the pattern has no %type% in it, so there is nothing to parse");
        }
        return new ParsePattern(source, root, compiler.slots);
    }

    String source() {
        return source;
    }

    List<Slot> slots() {
        return slots;
    }

    Optional<List<Object>> match(String text) {
        Matcher matcher = new Matcher(text);
        if (!matcher.match(root, 0, position -> position == text.length())) {
            return Optional.empty();
        }
        List<Object> values = new ArrayList<>();
        for (Object value : matcher.values) {
            if (value instanceof List<?> list) {
                values.addAll(list);
            } else if (value != null) {
                values.add(value);
            }
        }
        return Optional.of(values);
    }

    static Optional<Object> parseValue(String text, Kind kind) {
        String value = text.strip();
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return switch (kind) {
            case TEXT -> Optional.of(value);
            case NUMBER -> ParsedNumbers.number(value) instanceof Double number ? Optional.of(number) : Optional.empty();
            case INTEGER -> ParsedNumbers.number(value) instanceof Double number && number == Math.rint(number)
                    ? Optional.of(number) : Optional.empty();
            case BOOLEAN -> switch (value.toLowerCase(Locale.ROOT)) {
                case "true", "yes", "on" -> Optional.of(Boolean.TRUE);
                case "false", "no", "off" -> Optional.of(Boolean.FALSE);
                default -> Optional.empty();
            };
            case TIMESPAN -> tokens(value).flatMap(Literals::timespan).map(Object.class::cast);
            case ITEMTYPE -> tokens(value).flatMap(Literals::blockType).map(Object.class::cast);
            default -> NamedValues.parse(value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " "), kind.type());
        };
    }

    private static Optional<List<Token>> tokens(String value) {
        try {
            return Optional.of(Tokenizer.tokenize(value));
        } catch (TokenizeException error) {
            return Optional.empty();
        }
    }

    private static Optional<Object> parseSlot(String text, Slot slot) {
        if (!slot.plural()) {
            return parseValue(text, slot.kind());
        }
        List<Object> values = new ArrayList<>();
        for (String part : LIST_SEPARATOR.split(text.strip(), -1)) {
            Optional<Object> value = parseValue(part, slot.kind());
            if (value.isEmpty()) {
                return Optional.empty();
            }
            values.add(value.get());
        }
        return Optional.of(List.copyOf(values));
    }

    private final class Matcher {
        private final String text;
        private final Object[] values;

        private Matcher(String text) {
            this.text = text;
            this.values = new Object[slots.size()];
        }

        private boolean match(Element element, int position, Continuation next) {
            return switch (element) {
                case Literal literal -> literal(literal.text(), position, next);
                case Sequence sequence -> sequence(sequence.elements(), 0, position, next);
                case Optionally optional -> match(optional.element(), position, next) || next.run(position);
                case Choice choice -> {
                    for (Element branch : choice.branches()) {
                        if (match(branch, position, next)) {
                            yield true;
                        }
                    }
                    yield false;
                }
                case Hole hole -> hole(hole.index(), position, next);
            };
        }

        private boolean sequence(List<Element> elements, int index, int position, Continuation next) {
            if (index == elements.size()) {
                return next.run(position);
            }
            return match(elements.get(index), position, p -> sequence(elements, index + 1, p, next));
        }

        private boolean literal(String literal, int position, Continuation next) {
            if (literal.equals(" ")) {
                int end = position;
                while (end < text.length() && Character.isWhitespace(text.charAt(end))) {
                    end++;
                }
                boolean joined = position == 0 || position == text.length()
                        || Character.isWhitespace(text.charAt(position - 1));
                return (end > position || joined) && next.run(end);
            }
            return text.regionMatches(true, position, literal, 0, literal.length())
                    && next.run(position + literal.length());
        }

        private boolean hole(int index, int position, Continuation next) {
            for (int end = position + 1; end <= text.length(); end++) {
                String part = text.substring(position, end);
                if (Character.isWhitespace(part.charAt(0)) || Character.isWhitespace(part.charAt(part.length() - 1))) {
                    continue;
                }
                Optional<Object> value = parseSlot(part, slots.get(index));
                if (value.isEmpty()) {
                    continue;
                }
                values[index] = value.get();
                if (next.run(end)) {
                    return true;
                }
                values[index] = null;
            }
            return false;
        }
    }

    private static final class Compiler {
        private final String source;
        private final List<Slot> slots = new ArrayList<>();
        private int position;

        private Compiler(String source) {
            this.source = source;
        }

        private Element sequence() {
            List<Element> elements = new ArrayList<>();
            StringBuilder literal = new StringBuilder();
            while (position < source.length()) {
                char c = source.charAt(position);
                if (c == ']' || c == ')' || c == '|') {
                    break;
                }
                if (c == '\\' && position + 1 < source.length()) {
                    literal.append(source.charAt(position + 1));
                    position += 2;
                } else if (c == '[') {
                    flush(elements, literal);
                    position++;
                    Element inner = choice();
                    expect(']');
                    elements.add(new Optionally(inner));
                } else if (c == '(') {
                    flush(elements, literal);
                    position++;
                    Element inner = choice();
                    expect(')');
                    elements.add(inner);
                } else if (c == '%') {
                    flush(elements, literal);
                    elements.add(hole());
                } else if (Character.isWhitespace(c)) {
                    flush(elements, literal);
                    while (position < source.length() && Character.isWhitespace(source.charAt(position))) {
                        position++;
                    }
                    elements.add(new Literal(" "));
                } else {
                    literal.append(c);
                    position++;
                }
            }
            flush(elements, literal);
            return new Sequence(List.copyOf(elements));
        }

        private Element choice() {
            List<Element> branches = new ArrayList<>();
            branches.add(sequence());
            while (position < source.length() && source.charAt(position) == '|') {
                position++;
                branches.add(sequence());
            }
            return branches.size() == 1 ? branches.get(0) : new Choice(List.copyOf(branches));
        }

        private Element hole() {
            int end = source.indexOf('%', position + 1);
            if (end < 0) {
                throw new IllegalArgumentException("a % in the pattern is never closed");
            }
            String name = source.substring(position + 1, end).toLowerCase(Locale.ROOT).replace(" ", "");
            position = end + 1;
            while (name.startsWith("-") || name.startsWith("*") || name.startsWith("~")) {
                name = name.substring(1);
            }
            Kind kind = KINDS.get(name);
            boolean plural = false;
            if (kind == null && name.endsWith("s")) {
                kind = KINDS.get(name.substring(0, name.length() - 1));
                plural = kind != null;
            }
            if (kind == null && name.endsWith("ies")) {
                kind = KINDS.get(name.substring(0, name.length() - 3) + "y");
                plural = kind != null;
            }
            if (kind == null) {
                throw new IllegalArgumentException("text cannot be parsed as %" + name + "%; it can be parsed as "
                        + String.join(", ", Arrays.asList("text", "number", "integer", "boolean", "timespan",
                        "itemtype", "gamemode", "potioneffecttype", "enchantmenttype", "entitytype", "weathertype")));
            }
            slots.add(new Slot(kind, plural));
            return new Hole(slots.size() - 1);
        }

        private void flush(List<Element> elements, StringBuilder literal) {
            if (!literal.isEmpty()) {
                elements.add(new Literal(literal.toString()));
                literal.setLength(0);
            }
        }

        private void expect(char c) {
            if (position >= source.length() || source.charAt(position) != c) {
                throw new IllegalArgumentException("a " + (c == ']' ? "[" : "(") + " in the pattern is never closed");
            }
            position++;
        }
    }
}
