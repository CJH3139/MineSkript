package com.mineskript.lang.ast;

import java.util.Objects;

public record ItemValue(String id, String name, int count, int damage, int maxDamage, ItemDetails details) {
    public ItemValue {
        details = details == null ? ItemDetails.NONE : details;
    }

    public ItemValue(String id, String name, int count, int damage, int maxDamage) {
        this(id, name, count, damage, maxDamage, ItemDetails.NONE);
    }

    public static ItemValue empty() {
        return new ItemValue("minecraft:air", "air", 0, 0, 0);
    }

    public boolean isEmpty() {
        return count == 0 || id.equals("minecraft:air");
    }

    public BlockType type() {
        return new BlockType(id);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemValue item && id.equals(item.id) && name.equals(item.name) && count == item.count
                && damage == item.damage && maxDamage == item.maxDamage;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, count, damage, maxDamage);
    }

    @Override
    public String toString() {
        return "ItemValue[id=" + id + ", name=" + name + ", count=" + count + ", damage=" + damage
                + ", maxDamage=" + maxDamage + "]";
    }
}
