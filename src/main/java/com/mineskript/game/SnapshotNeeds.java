package com.mineskript.game;

public record SnapshotNeeds(
        boolean inventory,
        boolean heldItem,
        boolean consume,
        boolean experience,
        boolean effects,
        boolean vehicle,
        boolean dimension,
        boolean onlineNames) {

    public static SnapshotNeeds nothing() {
        return new SnapshotNeeds(false, false, false, false, false, false, false, false);
    }

    public static SnapshotNeeds everything(boolean inventory) {
        return new SnapshotNeeds(inventory, true, true, true, true, true, true, true);
    }
}
