package com.mineskript.client.hud.elements;

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
import java.util.Optional;

@Name("Copy To Clipboard")
@Description("Puts a value on your system clipboard as text, ready to paste anywhere. Numbers, items and lists are turned into text the same way Send shows them.")
@Examples({"on key press of \"c\":",
        "	if key \"ctrl\" is held:",
        "		copy \"%round player's x-coordinate% %round player's y-coordinate% %round player's z-coordinate%\" to clipboard",
        "		show action bar \"coordinates copied\""})
@Since("1.0.0-alpha.2")
public final class EffClipboard implements Statement {
    private final int line;
    private final Expression text;

    private EffClipboard(int line, Expression text) {
        this.line = line;
        this.text = text;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffClipboard(scope.line(), match.slot(0))),
                "copy %objects% to [the] clipboard");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.game().copyToClipboard(Converters.toText(text.evaluate(context), context));
        return Flow.CONTINUE;
    }
}
