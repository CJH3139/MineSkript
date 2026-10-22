package com.mineskript.client.inventory.elements;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.InventoryRef;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConvertedExpression;
import com.mineskript.lang.runtime.Context;
import java.util.ArrayList;
import java.util.List;

final class Inventories {
    static final int STORAGE_SLOTS = 36;
    static final int OWN_SLOTS = 41;
    private static final String VANILLA = "minecraft:";

    private Inventories() {
    }

    static boolean isInventory(Expression expression) {
        return expression.type() == SkType.INVENTORY && !(expression instanceof ConvertedExpression);
    }

    static InventoryRef inventory(Expression expression, Context context) {
        if (expression == null) {
            return InventoryRef.LOCAL;
        }
        return expression.evaluate(context) instanceof InventoryRef inventory ? inventory : InventoryRef.LOCAL;
    }

    static List<ItemValue> items(GameBridge game, InventoryRef inventory) {
        if (inventory == InventoryRef.OPEN) {
            return game.openInventoryItems();
        }
        List<ItemValue> items = new ArrayList<>(OWN_SLOTS);
        for (int slot = 0; slot < OWN_SLOTS; slot++) {
            items.add(game.itemInSlot(slot));
        }
        return items;
    }

    static List<ItemValue> storage(GameBridge game, InventoryRef inventory) {
        List<ItemValue> items = items(game, inventory);
        return inventory == InventoryRef.LOCAL ? items.subList(0, STORAGE_SLOTS) : items;
    }

    static ItemValue item(GameBridge game, InventoryRef inventory, int slot) {
        if (inventory == InventoryRef.LOCAL) {
            return game.itemInSlot(slot);
        }
        List<ItemValue> items = game.openInventoryItems();
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemValue.empty();
    }

    static int slot(Expression number, Context context) {
        return (int) Math.round((Double) number.evaluate(context));
    }

    static int count(GameBridge game, BlockType type) {
        int exact = game.countItem(type.id());
        if (exact > 0 || game.itemExists(type.id())) {
            return exact;
        }
        for (String candidate : singulars(type.id())) {
            if (game.itemExists(candidate)) {
                return game.countItem(candidate);
            }
        }
        return 0;
    }

    static int count(GameBridge game, InventoryRef inventory, BlockType type) {
        if (inventory == InventoryRef.LOCAL) {
            return count(game, type);
        }
        String id = id(game, type);
        int total = 0;
        for (ItemValue item : game.openInventoryItems()) {
            if (item.id().equals(id)) {
                total += item.count();
            }
        }
        return total;
    }

    static String id(GameBridge game, BlockType type) {
        if (game.itemExists(type.id())) {
            return type.id();
        }
        for (String candidate : singulars(type.id())) {
            if (game.itemExists(candidate)) {
                return candidate;
            }
        }
        return type.id();
    }

    private static List<String> singulars(String id) {
        int colon = id.indexOf(':');
        String namespace = colon < 0 ? VANILLA : id.substring(0, colon + 1);
        String path = colon < 0 ? id : id.substring(colon + 1);
        if (!path.endsWith("s")) {
            return List.of();
        }
        String withoutS = path.substring(0, path.length() - 1);
        if (path.endsWith("ies")) {
            return List.of(namespace + withoutS, namespace + path.substring(0, path.length() - 3) + "y");
        }
        if (path.endsWith("es")) {
            return List.of(namespace + withoutS, namespace + path.substring(0, path.length() - 2));
        }
        return List.of(namespace + withoutS);
    }
}
