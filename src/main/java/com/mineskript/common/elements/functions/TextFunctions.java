package com.mineskript.common.elements.functions;

import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.function.FunctionParameter;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public final class TextFunctions {
    private static final String SINCE = "1.0.0-alpha.10";
    private static final String WHOLE_FORMAT = "###,###";
    private static final String DECIMAL_FORMAT = "###,###.##";

    private TextFunctions() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addFunction("concat", SkType.TEXT, TextFunctions::concat,
                        FunctionParameter.list("texts", SkType.OBJECT))
                .description("Joins texts, and any other values, into one text with nothing in between, such as"
                        + " concat(\"level \", 5) giving level 5.")
                .examples("on key press of \"c\":",
                        "\tsend concat(\"you are at y \", round(player's y-coordinate))")
                .since(SINCE);
        registry.addFunction("formatNumber", SkType.TEXT, TextFunctions::format,
                        FunctionParameter.of("number", SkType.NUMBER),
                        FunctionParameter.optional("format", SkType.TEXT, ""))
                .description("Writes a number as text in an easy to read way. Without a format, whole numbers get a"
                                + " comma between each group of three digits (1234567 gives 1,234,567) and other"
                                + " numbers also keep up to 2 decimal places (1234.567 gives 1,234.57).",
                        "A format is a pattern such as \"#,##0.00\": # is a digit that is left out when it is not"
                                + " needed, 0 is a digit that is always shown, a comma separates groups and a dot"
                                + " starts the decimals. Commas and dots are always written this way, whatever"
                                + " language your game uses. An invalid format gives no value.")
                .examples("on key press of \"m\":",
                        "\tsend \"you have %formatNumber(total experience)% experience points\"",
                        "\tsend \"health: %formatNumber(health of player, \"0.0\")%\"")
                .since(SINCE);
    }

    private static Object concat(List<Object> arguments, Context context) {
        StringBuilder text = new StringBuilder();
        for (Object value : (List<?>) arguments.get(0)) {
            text.append(Converters.toText(value, context));
        }
        return text.toString();
    }

    private static Object format(List<Object> arguments, Context context) {
        double number = (Double) arguments.get(0);
        String format = (String) arguments.get(1);
        if (format.isEmpty()) {
            format = number == Math.rint(number) ? WHOLE_FORMAT : DECIMAL_FORMAT;
        }
        try {
            return new DecimalFormat(format, DecimalFormatSymbols.getInstance(Locale.ROOT)).format(number);
        } catch (IllegalArgumentException invalid) {
            return None.NONE;
        }
    }
}
