package com.mineskript.lang.ast;

import java.util.Optional;

public record BlockValue(String id, Optional<Location> location) {
    public BlockValue(String id) {
        this(id, Optional.empty());
    }

    public BlockValue(String id, Location location) {
        this(id, Optional.of(location.blockCorner()));
    }

    public BlockType type() {
        return new BlockType(id);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BlockValue block && id.equals(block.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return type().path();
    }
}
