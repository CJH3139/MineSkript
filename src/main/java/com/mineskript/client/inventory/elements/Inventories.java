package com.mineskript.client.inventory.elements;

import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConvertedExpression;
import java.util.List;

final class Inventories {
    private static final String VANILLA = "minecraft:";

    private Inventories() {
    }

    static boolean isInventory(Expression expression) {
        return expression.type() == SkType.INVENTORY && !(expression instanceof ConvertedExpression);
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
