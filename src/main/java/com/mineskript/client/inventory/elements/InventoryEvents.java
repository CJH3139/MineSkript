package com.mineskript.client.inventory.elements;

import static com.mineskript.client.ClientEvents.state;

import com.mineskript.lang.ast.Event;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.Optional;

public final class InventoryEvents {
    private InventoryEvents() {
    }

    public static void register(SyntaxRegistry registry) {
        state(registry, "Held Item Change", "on (held item change|item switch)", "held")
                .values("item", "previous item")
                .description("Fires when you select a different hotbar slot. event-item is the item in the new slot and event-previous item the one in the old slot. Changes to the item inside the same slot do not fire it; use inventory change for that.")
                .examples("on item switch:",
                        "\tsend \"now holding %event-item%\"",
                        "",
                        "on held item change:",
                        "\tif id of event-item is \"minecraft:bow\":",
                        "\t\tsend \"bow ready\"")
                .since("1.0.0-alpha.2");
        state(registry, "Inventory Change", "on inventory change", "inventory")
                .values("item")
                .description("Fires when the item type or count in any of your inventory slots changes, including armour and offhand, checked once per tick. It fires at most once per tick, and event-item is the new content of the first changed slot (air if it was emptied). Durability changes alone do not count.")
                .examples("on inventory change:",
                        "\tif inventory is full:",
                        "\t\tsend \"inventory full\"")
                .since("1.0.0-alpha.2");
        state(registry, "Start Using Item", "on start using item", "use start")
                .values("item")
                .description("Fires when you begin using an item: eating, drinking, drawing a bow, raising a shield and similar. It is polled once per tick.",
                        "event-item is always the item in your main hand, even when the item being used is in the offhand.")
                .examples("on start using item:",
                        "\tsend \"using %event-item%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Stop Using Item", "on stop using item", "use stop")
                .values("item")
                .description("Fires when you stop using an item, whether you finished (ate the food, fired the bow) or let go early. It is polled once per tick.",
                        "event-item is the item in your main hand at that moment, even if the used item was in the offhand.")
                .examples("on stop using item:",
                        "\tif id of event-item is \"minecraft:bow\":",
                        "\t\tsend \"arrow away\"")
                .since("1.0.0-alpha.2");
        state(registry, "Consume", "on (consume|eat|drink)", "consume")
                .values("item")
                .description("Fires when you finish eating or drinking an item, checked once per tick. event-item is the item that was consumed, as it was just before finishing. Stopping early does not fire it.")
                .examples("on eat:",
                        "\tif id of event-item is \"minecraft:golden_apple\":",
                        "\t\tsend \"golden apple eaten\"",
                        "",
                        "on consume:",
                        "\tsend \"consumed %event-item%\"")
                .since("1.0.0-alpha.2");
        state(registry, "Item Break", "on (item break|tool break)", "item break")
                .values("item")
                .description("Fires when the tool in your main hand breaks. It is detected by polling: the held item had one use left and the slot became empty without you changing slots. event-item is the item that broke.",
                        "Because it is inferred, dropping an item with one use left out of your hand is also reported as a break. See on durability below to get a warning before this happens.")
                .examples("on tool break:",
                        "\tsend \"%event-item% broke\"")
                .since("1.0.0-alpha.2");
        state(registry, "Item Tooltip", "on [item] tooltip", "tooltip")
                .values("item")
                .instant()
                .description("Fires while the game builds the tooltip of an item you hover over in an inventory, so you can add your own lines with add to tooltip. The lines are only on your screen: the item itself, and what other players see, does not change. event-item is the hovered item.",
                        "The tooltip is rebuilt every frame while you hover, so this can run many times a second: keep it short. The trigger runs to its end straight away and cannot wait; a wait in it is an error when the script loads. Only lines added before the trigger ends are shown. An error in it is shown once, not every frame.",
                        "It only runs while you are in a world. Tooltips the game builds in the background, such as for the creative inventory search, are left alone.")
                .examples("on item tooltip:",
                        "	add \"&7id: %id of event-item%\" to the tooltip",
                        "",
                        "on tooltip:",
                        "	if custom name of event-item is set:",
                        "		add \"&6renamed item\" to the top of the tooltip")
                .since("1.0.0-alpha.9");
        registry.addEvent("Durability Below", InventoryEvents::durability, "on [held item] durability (below|under|less than) %number%")
                .values("durability", "item")
                .description("Fires when the durability left on the item in your main hand drops below the given number, checked once per tick. It fires only on the crossing, from at least the number to below it, for the same item in the same slot; switching to an item that is already low does not fire it. event-item is the item and event-durability the uses left.",
                        "The number must be written as a fixed value; a variable or expression is rejected when the script loads.")
                .examples("on durability below 10:",
                        "\tsend \"%event-item% has %event-durability% uses left\"",
                        "",
                        "on held item durability under 50:",
                        "\tshow title \"repair soon\"")
                .since("1.0.0-alpha.5");
    }

    private static Optional<Event> durability(Match match, ParseScope scope) {
        if (!(match.slot(0) instanceof ConstantExpression constant) || !(constant.value() instanceof Double threshold)) {
            throw new SyntaxException("\"durability below\" needs a fixed number like \"durability below 10\"");
        }
        return Optional.of(new Event.Durability(threshold));
    }
}
