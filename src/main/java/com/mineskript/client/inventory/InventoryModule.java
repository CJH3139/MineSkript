package com.mineskript.client.inventory;

import com.mineskript.client.inventory.elements.CondCanHold;
import com.mineskript.client.inventory.elements.CondHasItem;
import com.mineskript.client.inventory.elements.CondInventoryFull;
import com.mineskript.client.inventory.elements.CondIsEnchanted;
import com.mineskript.client.inventory.elements.CondIsHolding;
import com.mineskript.client.inventory.elements.CondSlotEmpty;
import com.mineskript.client.inventory.elements.EffAddTooltip;
import com.mineskript.client.inventory.elements.EffClickSlot;
import com.mineskript.client.inventory.elements.EffDrop;
import com.mineskript.client.inventory.elements.EffDropSlot;
import com.mineskript.client.inventory.elements.EffEat;
import com.mineskript.client.inventory.elements.EffMoveSlot;
import com.mineskript.client.inventory.elements.EffOpenInventory;
import com.mineskript.client.inventory.elements.EffQuickMove;
import com.mineskript.client.inventory.elements.EffSelectSlot;
import com.mineskript.client.inventory.elements.EffSwapHands;
import com.mineskript.client.inventory.elements.EffSwapSlot;
import com.mineskript.client.inventory.elements.ExprClicked;
import com.mineskript.client.inventory.elements.ExprCursorItem;
import com.mineskript.client.inventory.elements.ExprCustomModelData;
import com.mineskript.client.inventory.elements.ExprDurability;
import com.mineskript.client.inventory.elements.ExprEnchantmentLevel;
import com.mineskript.client.inventory.elements.ExprFirstEmptySlot;
import com.mineskript.client.inventory.elements.ExprFreeSlots;
import com.mineskript.client.inventory.elements.ExprHeldItem;
import com.mineskript.client.inventory.elements.ExprInventory;
import com.mineskript.client.inventory.elements.ExprInventoryCount;
import com.mineskript.client.inventory.elements.ExprInventoryInfo;
import com.mineskript.client.inventory.elements.ExprItemComponent;
import com.mineskript.client.inventory.elements.ExprItemCount;
import com.mineskript.client.inventory.elements.ExprItemCustomName;
import com.mineskript.client.inventory.elements.ExprItemDamage;
import com.mineskript.client.inventory.elements.ExprItemEnchantments;
import com.mineskript.client.inventory.elements.ExprItemId;
import com.mineskript.client.inventory.elements.ExprItemInSlot;
import com.mineskript.client.inventory.elements.ExprItemLore;
import com.mineskript.client.inventory.elements.ExprItemMaxDamage;
import com.mineskript.client.inventory.elements.ExprItemName;
import com.mineskript.client.inventory.elements.ExprItemsIn;
import com.mineskript.client.inventory.elements.ExprOffhandItem;
import com.mineskript.client.inventory.elements.ExprOpenedInventory;
import com.mineskript.client.inventory.elements.ExprSelectedSlot;
import com.mineskript.client.inventory.elements.ExprUsedSlots;
import com.mineskript.client.inventory.elements.InventoryEvents;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;

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
        ExprDurability.register(registry);
        ExprItemInSlot.register(registry);
        ExprInventoryCount.register(registry);
        ExprInventory.register(registry);
        ExprItemCustomName.register(registry);
        ExprItemLore.register(registry);
        ExprItemEnchantments.register(registry);
        ExprEnchantmentLevel.register(registry);
        ExprCustomModelData.register(registry);
        ExprItemComponent.register(registry);
        ExprOpenedInventory.register(registry);
        ExprItemsIn.register(registry);
        ExprInventoryInfo.register(registry);
        ExprFirstEmptySlot.register(registry);
        ExprCursorItem.register(registry);
        ExprClicked.register(registry);

        CondHasItem.register(registry);
        CondIsHolding.register(registry);
        CondInventoryFull.register(registry);
        CondSlotEmpty.register(registry);
        CondIsEnchanted.register(registry);
        CondCanHold.register(registry);

        EffSelectSlot.register(registry);
        EffSwapHands.register(registry);
        EffDrop.register(registry);
        EffEat.register(registry);
        EffOpenInventory.register(registry);
        EffAddTooltip.register(registry);
        EffClickSlot.register(registry);
        EffDropSlot.register(registry);
        EffSwapSlot.register(registry);
        EffMoveSlot.register(registry);
        EffQuickMove.register(registry);
    }
}
