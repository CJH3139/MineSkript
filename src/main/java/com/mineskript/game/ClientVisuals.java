package com.mineskript.game;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class ClientVisuals {
    private static final int FIRST_ENTITY_ID = -1_000_000;
    private static final double BEAM_DRAW_DISTANCE = 512;
    private static final float BEAM_SCALE_DISTANCE = 96.0f;
    private static final int BEAM_ANIMATION_TICKS = 40;

    private record Spawned(Entity entity, ClientEntityKind kind, String owner) {
    }

    private record Beam(int x, int y, int z, int argb, String owner) {
    }

    private final Map<Integer, Spawned> byHandle = new LinkedHashMap<>();
    private final Set<Entity> entities = Collections.newSetFromMap(new IdentityHashMap<>());
    private volatile List<Beam> beams = List.of();
    private ClientLevel level;
    private int nextHandle = 1;
    private int nextEntityId = FIRST_ENTITY_ID;

    boolean owns(Entity entity) {
        return !entities.isEmpty() && entities.contains(entity);
    }

    void sync() {
        ClientLevel current = Minecraft.getInstance().level;
        if (current == level) {
            return;
        }
        byHandle.clear();
        entities.clear();
        beams = List.of();
        level = current;
    }

    OptionalInt spawn(ClientEntityKind kind, String content, double x, double y, double z, String owner) {
        sync();
        if (level == null) {
            return OptionalInt.empty();
        }
        Entity entity = create(kind, content);
        if (entity == null) {
            return OptionalInt.empty();
        }
        entity.setId(nextEntityId--);
        entity.snapTo(x, y, z);
        int handle = nextHandle++;
        byHandle.put(handle, new Spawned(entity, kind, owner));
        entities.add(entity);
        level.addEntity(entity);
        return OptionalInt.of(handle);
    }

    private Entity create(ClientEntityKind kind, String content) {
        return switch (kind) {
            case HOLOGRAM -> {
                Display.TextDisplay display = new Display.TextDisplay(EntityTypes.TEXT_DISPLAY, level);
                display.setText(Component.literal(content));
                display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
                yield display;
            }
            case ITEM -> {
                Identifier id = Identifier.tryParse(content);
                if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                    yield null;
                }
                Display.ItemDisplay display = new Display.ItemDisplay(EntityTypes.ITEM_DISPLAY, level);
                display.setItemStack(new ItemStack(BuiltInRegistries.ITEM.getValue(id)));
                display.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);
                yield display;
            }
            case BLOCK -> {
                Identifier id = Identifier.tryParse(content);
                if (id == null || !BuiltInRegistries.BLOCK.containsKey(id)) {
                    yield null;
                }
                Display.BlockDisplay display = new Display.BlockDisplay(EntityTypes.BLOCK_DISPLAY, level);
                display.setBlockState(BuiltInRegistries.BLOCK.getValue(id).defaultBlockState());
                yield display;
            }
        };
    }

    int count() {
        sync();
        return byHandle.size();
    }

    boolean move(int handle, double x, double y, double z) {
        sync();
        Spawned spawned = byHandle.get(handle);
        if (spawned == null) {
            return false;
        }
        spawned.entity().snapTo(x, y, z);
        return true;
    }

    boolean setText(int handle, String text) {
        sync();
        Spawned spawned = byHandle.get(handle);
        if (spawned == null || !(spawned.entity() instanceof Display.TextDisplay display)) {
            return false;
        }
        display.setText(Component.literal(text));
        return true;
    }

    boolean remove(int handle) {
        sync();
        Spawned spawned = byHandle.remove(handle);
        if (spawned == null) {
            return false;
        }
        discard(spawned.entity());
        return true;
    }

    void removeAll() {
        sync();
        for (Spawned spawned : List.copyOf(byHandle.values())) {
            discard(spawned.entity());
        }
        byHandle.clear();
    }

    void removeOwnedBy(String owner) {
        sync();
        byHandle.values().removeIf(spawned -> {
            if (!spawned.owner().equals(owner)) {
                return false;
            }
            discard(spawned.entity());
            return true;
        });
        List<Beam> kept = new ArrayList<>(beams);
        if (kept.removeIf(beam -> beam.owner().equals(owner))) {
            beams = List.copyOf(kept);
        }
    }

    private void discard(Entity entity) {
        int id = entity.getId();
        if (level != null && level.getEntity(id) == entity) {
            level.removeEntity(id, Entity.RemovalReason.DISCARDED);
        }
        entities.remove(entity);
    }

    void showBeam(int x, int y, int z, int rgb, String owner) {
        sync();
        if (level == null) {
            return;
        }
        List<Beam> updated = new ArrayList<>(beams);
        updated.removeIf(beam -> beam.x() == x && beam.y() == y && beam.z() == z);
        updated.add(new Beam(x, y, z, 0xFF000000 | rgb, owner));
        beams = List.copyOf(updated);
    }

    boolean removeBeam(int x, int y, int z) {
        sync();
        List<Beam> updated = new ArrayList<>(beams);
        boolean removed = updated.removeIf(beam -> beam.x() == x && beam.y() == y && beam.z() == z);
        beams = List.copyOf(updated);
        return removed;
    }

    void removeAllBeams() {
        beams = List.of();
    }

    int beamCount() {
        sync();
        return beams.size();
    }

    void submitBeams(LevelRenderContext context) {
        List<Beam> current = beams;
        Minecraft minecraft = Minecraft.getInstance();
        if (current.isEmpty() || minecraft.level == null || minecraft.level != level || minecraft.player == null) {
            return;
        }
        Vec3 camera = context.levelState().cameraRenderState.pos;
        PoseStack pose = context.poseStack();
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float animation = Math.floorMod(level.getGameTime(), BEAM_ANIMATION_TICKS) + partialTick;
        boolean scoping = minecraft.player.isScoping();
        for (Beam beam : current) {
            double dx = beam.x() - camera.x;
            double dz = beam.z() - camera.z;
            double distance = Math.sqrt((dx + 0.5) * (dx + 0.5) + (dz + 0.5) * (dz + 0.5));
            if (distance > BEAM_DRAW_DISTANCE) {
                continue;
            }
            float scale = scoping ? 1.0f : Math.max(1.0f, (float) distance / BEAM_SCALE_DISTANCE);
            pose.pushPose();
            pose.translate(dx, beam.y() - camera.y, dz);
            BeaconRenderer.submitBeaconBeam(pose, context.submitNodeCollector(), BeaconRenderer.BEAM_LOCATION, 1.0f,
                    animation, 0, BeaconRenderer.MAX_RENDER_Y, beam.argb(), BeaconRenderer.SOLID_BEAM_RADIUS * scale,
                    BeaconRenderer.BEAM_GLOW_RADIUS * scale);
            pose.popPose();
        }
    }
}
