package com.mineskript.client.inventory.elements;

import com.mineskript.client.GameValueExpression;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import java.util.Optional;

@Name("Offhand Item")
@Description("The item stack in your offhand. Also written item in offhand, and like Skript off hand tool, offhand item of player or player's offhand item. Returns air when the offhand is empty. Printed in text it shows the display name with the count in front when there is more than one. Needs a world: outside a world the line stops with a \"no world\" error.")
@Examples({
        "on key press of \"o\":",
        "	send \"offhand: %offhand item%\"",
        "",
        "every 5 seconds:",
        "	if offhand item is torch:",
        "		if count of offhand item is less than 5:",
        "			send \"running out of torches\""
})
@Since({"1.0.0-alpha.2", "1.0.0-alpha.11"})
public final class ExprOffhandItem extends GameValueExpression {
    private ExprOffhandItem() {
        super(SkType.ITEM, GameBridge::offhandItem);
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.ITEM, Tier.SIMPLE, (match, scope) -> Optional.of(new ExprOffhandItem()),
                "[the] item in offhand",
                "[the] (offhand|off hand) (item|tool) [of %players%]",
                "%players%'s (offhand|off hand) (item|tool)");
    }
}
