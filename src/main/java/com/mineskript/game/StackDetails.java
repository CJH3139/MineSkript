package com.mineskript.game;

import com.mineskript.lang.ast.ItemDetails;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

final class StackDetails implements ItemDetails {
    private final DataComponentMap components;

    StackDetails(DataComponentMap components) {
        this.components = components;
    }

    @Override
    public Optional<String> customName() {
        Component name = components.get(DataComponents.CUSTOM_NAME);
        return name == null ? Optional.empty() : Optional.of(name.getString());
    }

    @Override
    public List<String> lore() {
        ItemLore lore = components.get(DataComponents.LORE);
        if (lore == null) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        for (Component line : lore.lines()) {
            lines.add(line.getString());
        }
        return List.copyOf(lines);
    }

    @Override
    public Map<String, Integer> enchantments() {
        Map<String, Integer> levels = new TreeMap<>();
        addEnchantments(components.get(DataComponents.ENCHANTMENTS), levels);
        addEnchantments(components.get(DataComponents.STORED_ENCHANTMENTS), levels);
        return Collections.unmodifiableMap(levels);
    }

    private static void addEnchantments(ItemEnchantments enchantments, Map<String, Integer> into) {
        if (enchantments == null) {
            return;
        }
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
            into.merge(entry.getKey().getRegisteredName(), entry.getIntValue(), Math::max);
        }
    }

    @Override
    public List<Double> customModelData() {
        CustomModelData data = components.get(DataComponents.CUSTOM_MODEL_DATA);
        if (data == null) {
            return List.of();
        }
        List<Double> numbers = new ArrayList<>();
        for (Float number : data.floats()) {
            numbers.add(Double.parseDouble(Float.toString(number)));
        }
        return List.copyOf(numbers);
    }

    @Override
    public Optional<String> component(String id) {
        Identifier key = Identifier.tryParse(id);
        if (key == null) {
            return Optional.empty();
        }
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(key);
        if (type == null) {
            return Optional.empty();
        }
        return text(type);
    }

    private <T> Optional<String> text(DataComponentType<T> type) {
        T value = components.get(type);
        if (value == null) {
            return Optional.empty();
        }
        Codec<T> codec = type.codec();
        Minecraft minecraft = Minecraft.getInstance();
        if (codec == null || minecraft.level == null) {
            return Optional.of(value.toString());
        }
        Optional<Tag> encoded = codec
                .encodeStart(minecraft.level.registryAccess().createSerializationContext(NbtOps.INSTANCE), value)
                .result();
        if (encoded.isEmpty()) {
            return Optional.of(value.toString());
        }
        Tag tag = encoded.get();
        return Optional.of(tag.asString().orElse(tag.toString()));
    }
}
