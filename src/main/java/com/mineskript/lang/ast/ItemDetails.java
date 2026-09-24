package com.mineskript.lang.ast;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ItemDetails {
    ItemDetails NONE = new ItemDetails() {
    };

    default Optional<String> customName() {
        return Optional.empty();
    }

    default List<String> lore() {
        return List.of();
    }

    default Map<String, Integer> enchantments() {
        return Map.of();
    }

    default List<Double> customModelData() {
        return List.of();
    }

    default Optional<String> component(String id) {
        return Optional.empty();
    }
}
