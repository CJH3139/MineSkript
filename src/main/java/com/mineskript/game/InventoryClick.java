package com.mineskript.game;

public record InventoryClick(Kind kind, int button) {
    public static final int OFFHAND_BUTTON = 40;

    public enum Kind {
        PICKUP,
        QUICK_MOVE,
        SWAP,
        CLONE,
        THROW,
        PICKUP_ALL
    }

    public static InventoryClick left() {
        return new InventoryClick(Kind.PICKUP, 0);
    }

    public static InventoryClick right() {
        return new InventoryClick(Kind.PICKUP, 1);
    }

    public static InventoryClick shift() {
        return new InventoryClick(Kind.QUICK_MOVE, 0);
    }

    public String typeName(boolean outside) {
        return switch (kind) {
            case PICKUP -> outside
                    ? "window border using " + (button == 1 ? "right" : "left") + " mouse button"
                    : (button == 1 ? "right" : "left") + " mouse button";
            case QUICK_MOVE -> (button == 1 ? "right" : "left") + " mouse button with shift";
            case SWAP -> button == OFFHAND_BUTTON ? "swap offhand" : "number key";
            case CLONE -> "middle mouse button";
            case THROW -> button == 1 ? "drop key with control" : "drop key";
            case PICKUP_ALL -> "double click";
        };
    }
}
