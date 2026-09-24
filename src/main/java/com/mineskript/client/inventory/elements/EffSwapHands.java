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

@Name("Swap Hands")
@Description({"Swaps the items in your main hand and offhand by pressing your swap-offhand key binding, as if you pressed F.",
        "Does nothing if the swap control has no key bound. Needs a world."})
@Examples({"on key press of \"v\":",
        "\tif player is holding totem_of_undying:",
        "\t\tswap hands"})
@Since("1.0.0-alpha.2")
public final class EffSwapHands implements Statement {
    private final int line;

    private EffSwapHands(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffSwapHands(scope.line())),
                "swap hands",
                "swap [the] offhand");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().swapHands();
        return Flow.CONTINUE;
    }
}
