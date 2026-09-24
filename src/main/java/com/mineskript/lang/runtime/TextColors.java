package com.mineskript.lang.runtime;

public final class TextColors {
    private static final String CODES = "0123456789abcdefklmnor";
    private static final char SECTION = '§';

    private TextColors() {
    }

    public static String colored(String text) {
        if (text.indexOf('&') < 0) {
            return text;
        }
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '&' && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(i + 1));
                if (CODES.indexOf(code) >= 0) {
                    out.append(SECTION).append(code);
                    i++;
                    continue;
                }
            }
            out.append(current);
        }
        return out.toString();
    }
}
