package com.mineskript.client.hud.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Name("Clipboard")
@Description("The text on your system clipboard, or empty text when it holds none. Works without a world. Setting it puts the value on the clipboard as text, the same as Copy To Clipboard.")
@Examples({
        "on key press of \"v\":",
        "\tsend \"clipboard: %clipboard%\"",
        "",
        "on key press of \"c\":",
        "\tset clipboard to \"%player's x-coordinate% %player's y-coordinate% %player's z-coordinate%\""
})
@Since("1.0.0-alpha.8")
public final class ExprClipboard implements Expression, Changeable {
    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprClipboard()), "[the] clipboard");
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public Object evaluate(Context context) {
        return context.game().clipboard();
    }

    @Override
    public String changeName() {
        return "the clipboard";
    }

    @Override
    public Set<ChangeMode> changeModes() {
        return EnumSet.of(ChangeMode.SET);
    }

    @Override
    public SkType changeType(ChangeMode mode) {
        return SkType.TEXT;
    }

    @Override
    public void change(Context context, ChangeMode mode, Object value) {
        context.game().copyToClipboard(Converters.toText(value, context));
    }
}
