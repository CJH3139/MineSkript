package com.mineskript.lang.ast;

public record ItemValue(String id, String name, int count, int damage, int maxDamage) {
    public static ItemValue empty() {
        return new ItemValue("minecraft:air", "air", 0, 0, 0);
    }

    public boolean isEmpty() {
        return count == 0 || id.equals("minecraft:air");
    }

    public BlockType type() {
        return new BlockType(id);
    }
}
