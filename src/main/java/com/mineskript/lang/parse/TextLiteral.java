package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TextLiteral implements Expression {
    private final List<Object> segments;

    private TextLiteral(List<Object> segments) {
        this.segments = List.copyOf(segments);
    }

    public static Optional<Expression> parse(String raw, ExpressionParser parser, ParseScope scope) {
        List<Object> segments = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        boolean interpolated = false;
        int i = 0;
        while (i < raw.length()) {
            char c = raw.charAt(i);
            if (c != '%') {
                text.append(c);
                i++;
                continue;
            }
            if (i + 1 < raw.length() && raw.charAt(i + 1) == '%') {
                text.append('%');
                i += 2;
                continue;
            }
            int end = raw.indexOf('%', i + 1);
            if (end < 0) {
                throw new SyntaxException("unterminated % in string");
            }
            String inner = raw.substring(i + 1, end);
            List<Token> tokens;
            try {
                tokens = Tokenizer.tokenize(inner);
            } catch (TokenizeException error) {
                return Optional.empty();
            }
            Expression expression = parser.parse(tokens, List.of(SkType.OBJECT), scope)
                    .orElseThrow(() -> new SyntaxException("unknown expression \"" + inner + "\" in string"));
            if (!text.isEmpty()) {
                segments.add(text.toString());
                text.setLength(0);
            }
            segments.add(expression);
            interpolated = true;
            i = end + 1;
        }
        if (!text.isEmpty()) {
            segments.add(text.toString());
        }
        if (!interpolated) {
            return Optional.of(new ConstantExpression(SkType.TEXT, text.isEmpty() && segments.isEmpty() ? "" : String.join("", segments.stream().map(String.class::cast).toList())));
        }
        return Optional.of(new TextLiteral(segments));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        StringBuilder result = new StringBuilder();
        for (Object segment : segments) {
            if (segment instanceof Expression expression) {
                result.append(Converters.toText(expression.evaluate(context), context));
            } else {
                result.append(segment);
            }
        }
        return result.toString();
    }
}
