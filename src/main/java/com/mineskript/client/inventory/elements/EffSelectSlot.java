package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Select Hotbar Slot")
@Description("Selects a hotbar slot, changing what you hold. Slots are numbered 0 to 8 from left to right, so select slot 0 picks the first slot. Decimals are rounded, and numbers outside 0 to 8 are clamped to the nearest end.")
@Examples({"on key press of \"r\":",
        "	select hotbar slot 0",
        "	click use",
        "",
        "on mouse scroll:",
        "	if key \"left alt\" is held:",
        "		select slot 8"})
@Since("1.0.0-alpha.2")
public final class EffSelectSlot implements Statement {
    private static final int FIRST_SLOT = 0;
    private static final int LAST_SLOT = 8;

    private final int line;
    private final Expression slot;

    private EffSelectSlot(int line, Expression slot) {
        this.line = line;
        this.slot = slot;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> match.slot(0).isList()
                        ? Optional.empty()
                        : Optional.of(new EffSelectSlot(scope.line(), match.slot(0))),
                "select [hotbar] slot %number%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        int index = (int) Math.round((Double) slot.evaluate(context));
        game.selectSlot(Math.max(FIRST_SLOT, Math.min(LAST_SLOT, index)));
        return Flow.CONTINUE;
    }
}
