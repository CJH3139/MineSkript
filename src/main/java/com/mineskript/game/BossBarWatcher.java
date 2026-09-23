package com.mineskript.game;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;

public final class BossBarWatcher implements ClientboundBossEventPacket.Handler {
    public static final BossBarWatcher INSTANCE = new BossBarWatcher();

    private final Map<UUID, String> names = new HashMap<>();

    private BossBarWatcher() {
    }

    @Override
    public void add(UUID id, Component name, float progress, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay,
            boolean darken, boolean music, boolean fog) {
        names.put(id, name.getString());
        emit(id, "add", progress);
    }

    @Override
    public void remove(UUID id) {
        emit(id, "remove", -1);
        names.remove(id);
    }

    @Override
    public void updateProgress(UUID id, float progress) {
        emit(id, "progress", progress);
    }

    @Override
    public void updateName(UUID id, Component name) {
        names.put(id, name.getString());
        emit(id, "name", -1);
    }

    private void emit(UUID id, String change, float progress) {
        if (!GameSignals.wants("bossbar")) {
            return;
        }
        Map<String, Object> values = new HashMap<>();
        values.put("text", names.getOrDefault(id, ""));
        values.put("bossbar change", change);
        if (progress >= 0) {
            values.put("progress", (double) progress * 100.0);
        }
        GameSignals.emit("bossbar", Map.copyOf(values));
    }
}
