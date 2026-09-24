package com.mineskript.client.inventory;

import com.mineskript.client.inventory.elements.CondHasItem;
import com.mineskript.client.inventory.elements.CondInventoryFull;
import com.mineskript.client.inventory.elements.CondIsHolding;
import com.mineskript.client.inventory.elements.CondSlotEmpty;
import com.mineskript.client.inventory.elements.EffDrop;
import com.mineskript.client.inventory.elements.EffEat;
import com.mineskript.client.inventory.elements.EffOpenInventory;
import com.mineskript.client.inventory.elements.EffSelectSlot;
import com.mineskript.client.inventory.elements.EffSwapHands;
import com.mineskript.client.inventory.elements.ExprFreeSlots;
import com.mineskript.client.inventory.elements.ExprHeldItem;
import com.mineskript.client.inventory.elements.ExprInventoryCount;
import com.mineskript.client.inventory.elements.ExprItemCount;
import com.mineskript.client.inventory.elements.ExprItemDamage;
import com.mineskript.client.inventory.elements.ExprItemId;
import com.mineskript.client.inventory.elements.ExprItemInSlot;
import com.mineskript.client.inventory.elements.ExprItemMaxDamage;
import com.mineskript.client.inventory.elements.ExprItemName;
import com.mineskript.client.inventory.elements.ExprOffhandItem;
import com.mineskript.client.inventory.elements.ExprSelectedSlot;
import com.mineskript.client.inventory.elements.ExprUsedSlots;
import com.mineskript.client.inventory.elements.InventoryEvents;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

/** Your inventory and the items in it. */
public final class InventoryModule implements SyntaxModule {
    @Override
    public String name() {
        return "inventory";
    }

    @Override
    public void register(SyntaxRegistry registry) {
        InventoryEvents.register(registry);

        ExprHeldItem.register(registry);
        ExprOffhandItem.register(registry);
        ExprSelectedSlot.register(registry);
        ExprFreeSlots.register(registry);
        ExprUsedSlots.register(registry);
        ExprItemName.register(registry);
        ExprItemId.register(registry);
        ExprItemCount.register(registry);
        ExprItemDamage.register(registry);
        ExprItemMaxDamage.register(registry);
        ExprItemInSlot.register(registry);
        ExprInventoryCount.register(registry);

        CondHasItem.register(registry);
        CondIsHolding.register(registry);
        CondInventoryFull.register(registry);
        CondSlotEmpty.register(registry);

        EffSelectSlot.register(registry);
        EffSwapHands.register(registry);
        EffDrop.register(registry);
        EffEat.register(registry);
        EffOpenInventory.register(registry);
    }
}
