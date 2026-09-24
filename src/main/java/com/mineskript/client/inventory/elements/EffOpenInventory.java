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

@Name("Open Inventory")
@Description({"Opens your inventory screen by pressing your inventory key binding, as if you pressed E.",
        "Does nothing if the inventory control has no key bound. Needs a world."})
@Examples({"on key press of \"i\":",
        "	if player is sneaking:",
        "		open the inventory"})
@Since("1.0.0-alpha.2")
public final class EffOpenInventory implements Statement {
    private final int line;

    private EffOpenInventory(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> Optional.of(new EffOpenInventory(scope.line())),
                "open [the] (inventory|gui)");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        context.world().openInventory();
        return Flow.CONTINUE;
    }
}
