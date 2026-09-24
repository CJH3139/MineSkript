package com.mineskript.client.inventory.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.ChangeMode;
import com.mineskript.lang.ast.Changeable;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Name("Selected Slot")
@Description({
        "The hotbar slot you have selected, as a whole number from 0 (leftmost) to 8 (rightmost). Needs a world: outside a world the line stops with a \"no world\" error.",
        "It can be set, added to and removed from. Decimals are rounded and the result wraps round like the scroll wheel, so adding 1 to slot 8 selects slot 0 and set selected slot to 10 selects slot 1."
})
@Examples({
        "on item switch:",
        "\tsend \"slot %selected slot%: %held item%\"",
        "",
        "on key press of \"x\":",
        "\tadd 1 to selected slot"
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.8"})
public final class ExprSelectedSlot extends GameValueExpression implements Changeable {
    private static final int HOTBAR_SIZE = 9;

    private ExprSelectedSlot() {
        super(SkType.NUMBER, game -> (double) game.selectedSlot());
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprSelectedSlot()), "[the] selected slot");
    }

    @Override
    public String changeName() {
        return "the selected slot";
    }

    @Override
    public Set<ChangeMode> changeModes() {
        return EnumSet.of(ChangeMode.SET, ChangeMode.ADD, ChangeMode.REMOVE);
    }

    @Override
    public SkType changeType(ChangeMode mode) {
        return SkType.NUMBER;
    }

    @Override
    public void change(Context context, ChangeMode mode, Object value) {
        GameBridge game = context.world();
        long slot = Math.round(changed(mode, game.selectedSlot(), value));
        game.selectSlot((int) Math.floorMod(slot, HOTBAR_SIZE));
    }
}
