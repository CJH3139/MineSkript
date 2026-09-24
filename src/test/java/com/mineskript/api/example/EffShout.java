package com.mineskript.api.example;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Locale;
import java.util.Optional;

@Name("Shout")
@Description("Shows a message to you in capital letters. Only you see it.")
@Examples({"on load:",
        "\tshout \"hello\""})
@Since("1.0.0")
public final class EffShout implements Statement {
    private final int line;
    private final Expression text;

    private EffShout(int line, Expression text) {
        this.line = line;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffShout(scope.line(), match.slot(0))), "shout %string%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        String message = Converters.toText(text.evaluate(context), context);
        context.game().showMessage(message.toUpperCase(Locale.ROOT));
        return Flow.CONTINUE;
    }
}
