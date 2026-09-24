package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Drop Item")
@Description("Drops what you are holding in your main hand, like pressing Q. Drop item throws one item; drop the stack or drop the whole stack throws the entire stack.")
@Examples({"on inventory change:",
        "\tif held item is rotten flesh:",
        "\t\tdrop the whole stack"})
@Since("1.0.0-alpha.2")
public final class EffDrop implements Statement {
    private final int line;
    private final boolean wholeStack;

    private EffDrop(int line, boolean wholeStack) {
        this.line = line;
        this.wholeStack = wholeStack;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffDrop(scope.line(), match.patternIndex() == 1)),
                "drop [the] item",
                "drop [the] (stack|whole stack)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().dropItem(wholeStack);
        return Flow.CONTINUE;
    }
}
