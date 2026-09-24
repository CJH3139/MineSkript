package com.mineskript.lang.runtime;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextMatching {
    private TextMatching() {
    }

    public static String replace(String text, String needle, String replacement, boolean caseSensitive,
            boolean firstOnly) {
        if (caseSensitive && !firstOnly) {
            return text.replace(needle, replacement);
        }
        Matcher matcher = literal(needle, caseSensitive).matcher(text);
        String quoted = Matcher.quoteReplacement(replacement);
        return firstOnly ? matcher.replaceFirst(quoted) : matcher.replaceAll(quoted);
    }

    public static List<String> split(String text, String delimiter, boolean caseSensitive, boolean keepTrailing) {
        if (text.isEmpty()) {
            return List.of();
        }
        if (delimiter.isEmpty()) {
            return text.codePoints().mapToObj(Character::toString).toList();
        }
        return List.of(literal(delimiter, caseSensitive).split(text, keepTrailing ? -1 : 0));
    }

    private static Pattern literal(String text, boolean caseSensitive) {
        int flags = caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        return Pattern.compile(Pattern.quote(text), flags);
    }
}
