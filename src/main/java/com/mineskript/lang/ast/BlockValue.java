package com.mineskript.lang.ast;

public record BlockValue(String id) {
    public BlockType type() {
        return new BlockType(id);
    }

    @Override
    public String toString() {
        return type().path();
    }
}
