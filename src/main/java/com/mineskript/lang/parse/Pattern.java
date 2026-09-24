package com.mineskript.lang.parse;

import com.mineskript.lang.ast.SkType;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class Pattern {
    /** The slot type that takes a whole condition instead of an expression, written {@code %condition%}. */
    public static final String CONDITION = "condition";

    private static final Map<String, SkType> TYPE_NAMES = Map.ofEntries(
            Map.entry("string", SkType.TEXT),
            Map.entry("strings", SkType.TEXT),
            Map.entry("text", SkType.TEXT),
            Map.entry("number", SkType.NUMBER),
            Map.entry("numbers", SkType.NUMBER),
            Map.entry("boolean", SkType.BOOLEAN),
            Map.entry("timespan", SkType.TIMESPAN),
            Map.entry("blocktype", SkType.BLOCKTYPE),
            Map.entry("blocktypes", SkType.BLOCKTYPE),
            Map.entry("block", SkType.BLOCK),
            Map.entry("blocks", SkType.BLOCK),
            Map.entry("player", SkType.PLAYER),
            Map.entry("players", SkType.PLAYER),
            Map.entry("item", SkType.ITEM),
            Map.entry("items", SkType.ITEM),
            Map.entry("entity", SkType.ENTITY),
            Map.entry("entities", SkType.ENTITY),
            Map.entry("object", SkType.OBJECT),
            Map.entry("objects", SkType.OBJECT));

    private final String source;
    private final PatternElement root;
    private final int slotCount;
    private final Map<PatternElement, int[]> suffixMinima = new IdentityHashMap<>();

    private Pattern(String source, PatternElement root, int slotCount) {
        this.source = source;
        this.root = root;
        this.slotCount = slotCount;
        collectSuffixMinima(root, suffixMinima);
    }

    public static Pattern compile(String source) {
        Compiler compiler = new Compiler(source);
        PatternElement root = compiler.sequence();
        if (compiler.position != source.length()) {
            throw new IllegalArgumentException("unexpected '" + source.charAt(compiler.position) + "' in pattern: " + source);
        }
        return new Pattern(source, root, compiler.slotCount);
    }

    public static SkType typeNamed(String name) {
        return TYPE_NAMES.get(name.toLowerCase(Locale.ROOT));
    }

    public String source() {
        return source;
    }

    public PatternElement root() {
        return root;
    }

    public int slotCount() {
        return slotCount;
    }

    int suffixMinimum(PatternElement.Sequence sequence, int index) {
        return suffixMinima.get(sequence)[index];
    }

    private static void collectSuffixMinima(PatternElement element, Map<PatternElement, int[]> into) {
        switch (element) {
            case PatternElement.Sequence sequence -> {
                List<PatternElement> elements = sequence.elements();
                int[] suffix = new int[elements.size() + 1];
                for (int i = elements.size() - 1; i >= 0; i--) {
                    suffix[i] = suffix[i + 1] + PatternElement.minimum(elements.get(i));
                    collectSuffixMinima(elements.get(i), into);
                }
                into.put(sequence, suffix);
            }
            case PatternElement.Optional optional -> collectSuffixMinima(optional.element(), into);
            case PatternElement.Choice choice -> {
                for (PatternElement.Branch branch : choice.branches()) {
                    collectSuffixMinima(branch.element(), into);
                }
            }
            case PatternElement.Literal ignored -> {
            }
            case PatternElement.Slot ignored -> {
            }
        }
    }

    @Override
    public String toString() {
        return source;
    }

    private static final class Compiler {
        private final String source;
        private int position;
        private int slotCount;

        private Compiler(String source) {
            this.source = source;
        }

        private PatternElement sequence() {
            List<PatternElement> elements = new ArrayList<>();
            StringBuilder word = new StringBuilder();
            while (position < source.length()) {
                char c = source.charAt(position);
                if (c == ']' || c == ')' || c == '|') {
                    break;
                }
                if (c == ' ') {
                    flushWord(elements, word);
                    position++;
                } else if (c == '[') {
                    flushWord(elements, word);
                    position++;
                    PatternElement inner = sequence();
                    expect(']');
                    elements.add(new PatternElement.Optional(inner));
                } else if (c == '(') {
                    flushWord(elements, word);
                    position++;
                    elements.add(choice());
                    expect(')');
                } else if (c == '%') {
                    flushWord(elements, word);
                    elements.add(slot());
                } else {
                    word.append(c);
                    position++;
                }
            }
            flushWord(elements, word);
            return new PatternElement.Sequence(List.copyOf(elements));
        }

        private PatternElement choice() {
            List<PatternElement.Branch> branches = new ArrayList<>();
            while (true) {
                String tag = "";
                int colon = tagEnd();
                if (colon > 0) {
                    tag = source.substring(position, colon);
                    position = colon + 1;
                }
                PatternElement element = sequence();
                branches.add(new PatternElement.Branch(tag, element));
                if (position < source.length() && source.charAt(position) == '|') {
                    position++;
                    continue;
                }
                return new PatternElement.Choice(List.copyOf(branches));
            }
        }

        private int tagEnd() {
            int i = position;
            while (i < source.length() && Character.isLetterOrDigit(source.charAt(i))) {
                i++;
            }
            return i < source.length() && source.charAt(i) == ':' && i > position ? i : -1;
        }

        private PatternElement slot() {
            int end = source.indexOf('%', position + 1);
            if (end < 0) {
                throw new IllegalArgumentException("unterminated % in pattern: " + source);
            }
            String body = source.substring(position + 1, end);
            position = end + 1;
            boolean optional = body.startsWith("-");
            if (optional) {
                body = body.substring(1);
            }
            if (body.equalsIgnoreCase(CONDITION)) {
                return new PatternElement.Slot(slotCount++, List.of(SkType.BOOLEAN), optional, true);
            }
            List<SkType> types = new ArrayList<>();
            for (String name : body.split("/")) {
                SkType type = TYPE_NAMES.get(name.toLowerCase(Locale.ROOT));
                if (type == null) {
                    throw new IllegalArgumentException("unknown type '" + name + "' in pattern: " + source);
                }
                types.add(type);
            }
            return new PatternElement.Slot(slotCount++, List.copyOf(types), optional);
        }

        private void flushWord(List<PatternElement> elements, StringBuilder word) {
            if (!word.isEmpty()) {
                elements.add(new PatternElement.Literal(word.toString().toLowerCase(Locale.ROOT)));
                word.setLength(0);
            }
        }

        private void expect(char c) {
            if (position >= source.length() || source.charAt(position) != c) {
                throw new IllegalArgumentException("expected '" + c + "' in pattern: " + source);
            }
            position++;
        }
    }
}
