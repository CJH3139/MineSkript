package com.mineskript.client.world.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockValue;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Target Block")
@Description("The block your crosshair is on, as a block value that prints as its plain name such as oak_log. Written target block, targeted block, target block of player or player's targeted block, like Skript. Returns air when you are not looking at a block (looking at the sky, at an entity, or at something out of reach). Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"i\":",
        "	send \"looking at %target block%\"",
        "",
        "every 1 second:",
        "	if target block is diamond_ore:",
        "		show action bar \"diamonds!\"",
        "",
        "on key press of \"i\":",
        "	send \"%player's targeted block% at %location of player's targeted block%\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprTargetBlock extends GameValueExpression {
    private ExprTargetBlock() {
        super(SkType.BLOCK, ExprTargetBlock::read);
    }

    private static BlockValue read(GameBridge game) {
        int[] position = game.targetBlockPosition();
        if (position == null) {
            return new BlockValue(game.targetBlock());
        }
        Location location = new Location(position[0], position[1], position[2], game.dimension());
        return new BlockValue(game.targetBlock(), location);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.BLOCK, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprTargetBlock()),
                "[the] (target|targeted) block [of %players%]",
                "%players%'s (target|targeted) block");
    }
}
