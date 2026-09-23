package com.mineskript.game;

import com.mineskript.MineSkriptClient;
import com.mineskript.MineSkriptMessages;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.script.MessageLine;
import com.mineskript.script.Messages;
import com.mineskript.script.OwnChatGuard;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class MinecraftBridge implements GameBridge {
    private final Map<Item, String> itemIds = new IdentityHashMap<>();
    private final Map<Holder<MobEffect>, String> effectIds = new IdentityHashMap<>();
    private final List<String> nameScratch = new ArrayList<>();
    private final Map<String, Integer> effectScratch = new TreeMap<>();
    private final Set<String> heldKeys = new HashSet<>();
    private final OwnChatGuard ownChat = new OwnChatGuard();
    private boolean attackHeld;
    private boolean useHeld;
    private ClientLevel trackedLevel;
    private HitResult staleHit;
    private long modTick;
    private long nearestEntityTick = Long.MIN_VALUE;
    private EntityValue nearestEntityValue;
    private long nearestPlayerTick = Long.MIN_VALUE;
    private EntityValue nearestPlayerValue;
    private long effectsTick = Long.MIN_VALUE;
    private Map<String, Integer> effectsValue = Map.of();
    private long onlineNamesTick = Long.MIN_VALUE;
    private List<String> onlineNamesValue = List.of();
    private ClientLevel dimensionLevel;
    private String dimensionValue = "";

    private static final int PLACE_WINDOW_TICKS = 5;

    private record PendingPlace(int attempt, BlockPos pos, String before, long deadline) {
    }

    private final List<PendingPlace> pendingPlaces = new ArrayList<>();
    private int placeAttempts;

    private static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    private static double exact(float value) {
        return Double.parseDouble(Float.toString(value));
    }

    @Override
    public boolean hasWorld() {
        Minecraft minecraft = minecraft();
        return minecraft.player != null && minecraft.level != null && minecraft.gameMode != null;
    }

    @Override
    public void clickAttack() {
        clickMapping(minecraft().options.keyAttack);
    }

    @Override
    public void clickUse() {
        clickMapping(minecraft().options.keyUse);
    }

    @Override
    public void setAttackHeld(boolean held) {
        attackHeld = held;
        minecraft().options.keyAttack.setDown(held);
    }

    @Override
    public void setUseHeld(boolean held) {
        useHeld = held;
        minecraft().options.keyUse.setDown(held);
    }

    @Override
    public void clickKey(String keyId) {
        KeyMapping.click(InputConstants.getKey(keyId));
    }

    @Override
    public void setKeyHeld(String keyId, boolean held) {
        if (held) {
            heldKeys.add(keyId);
        } else {
            heldKeys.remove(keyId);
        }
        KeyMapping.set(InputConstants.getKey(keyId), held);
    }

    @Override
    public boolean isKeyDown(String keyId) {
        if (minecraft().gui.screen() != null) {
            return false;
        }
        InputConstants.Key key = InputConstants.getKey(keyId);
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(minecraft().getWindow().handle(), key.getValue()) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(minecraft().getWindow(), key.getValue());
    }

    @Override
    public void sendChat(String text) {
        ownChat.around(() -> minecraft().player.connection.sendChat(text));
    }

    @Override
    public boolean sendingOwnChat() {
        return ownChat.sending();
    }

    @Override
    public void sendCommand(String command) {
        minecraft().player.connection.sendCommand(command);
    }

    @Override
    public void showMessage(String text) {
        show(Component.literal(text), text, false);
    }

    @Override
    public void showInfo(String text) {
        show(MineSkriptMessages.component(new MessageLine(MessageLine.Kind.SUCCESS, text)), Messages.PREFIX + " " + text, false);
    }

    @Override
    public void showWarning(String text) {
        show(MineSkriptMessages.component(new MessageLine(MessageLine.Kind.WARNING, text)), Messages.PREFIX + " " + text, true);
    }

    @Override
    public void showError(String text) {
        show(MineSkriptMessages.component(new MessageLine(MessageLine.Kind.ERROR, text)), Messages.PREFIX + " " + text, true);
    }

    @Override
    public double playerHealth() {
        return minecraft().player.getHealth();
    }

    @Override
    public double playerMaxHealth() {
        return minecraft().player.getMaxHealth();
    }

    @Override
    public int playerHunger() {
        return minecraft().player.getFoodData().getFoodLevel();
    }

    @Override
    public String playerName() {
        return minecraft().player.getName().getString();
    }

    @Override
    public double playerX() {
        return minecraft().player.getX();
    }

    @Override
    public double playerY() {
        return minecraft().player.getY();
    }

    @Override
    public double playerZ() {
        return minecraft().player.getZ();
    }

    @Override
    public boolean isSneaking() {
        return minecraft().player.isShiftKeyDown();
    }

    @Override
    public boolean isOnGround() {
        return minecraft().player.onGround();
    }

    @Override
    public boolean isSprinting() {
        return minecraft().player.isSprinting();
    }

    @Override
    public String blockIdAt(int dx, int dy, int dz) {
        Minecraft minecraft = minecraft();
        BlockPos pos = minecraft.player.blockPosition().offset(dx, dy, dz);
        return BuiltInRegistries.BLOCK.getKey(minecraft.level.getBlockState(pos).getBlock()).toString();
    }

    @Override
    public void releaseAll() {
        for (String keyId : heldKeys) {
            KeyMapping.set(InputConstants.getKey(keyId), false);
        }
        heldKeys.clear();
        if (attackHeld) {
            setAttackHeld(false);
        }
        if (useHeld) {
            setUseHeld(false);
        }
    }

    @Override
    public ItemValue heldItem() {
        return item(minecraft().player.getInventory().getSelectedItem());
    }

    @Override
    public ItemValue offhandItem() {
        return item(minecraft().player.getInventory().getItem(Inventory.SLOT_OFFHAND));
    }

    @Override
    public ItemValue itemInSlot(int slot) {
        Inventory inventory = minecraft().player.getInventory();
        if (slot < 0 || slot > Inventory.SLOT_OFFHAND) {
            return ItemValue.empty();
        }
        return item(inventory.getItem(slot));
    }

    @Override
    public int selectedSlot() {
        return minecraft().player.getInventory().getSelectedSlot();
    }

    @Override
    public int inventorySize() {
        return Inventory.INVENTORY_SIZE;
    }

    @Override
    public int freeSlots() {
        Inventory inventory = minecraft().player.getInventory();
        int free = 0;
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (inventory.getItem(slot).isEmpty()) {
                free++;
            }
        }
        return free;
    }

    @Override
    public int countItem(String id) {
        Inventory inventory = minecraft().player.getInventory();
        int count = 0;
        for (int slot = 0; slot <= Inventory.SLOT_OFFHAND; slot++) {
            ItemValue value = item(inventory.getItem(slot));
            if (value.id().equals(id)) {
                count += value.count();
            }
        }
        return count;
    }

    @Override
    public String gamemode() {
        return minecraft().gameMode.getPlayerMode().getName();
    }

    @Override
    public int xpLevel() {
        return minecraft().player.experienceLevel;
    }

    @Override
    public double xpProgress() {
        return exact(minecraft().player.experienceProgress);
    }

    @Override
    public int air() {
        return minecraft().player.getAirSupply();
    }

    @Override
    public int maxAir() {
        return minecraft().player.getMaxAirSupply();
    }

    @Override
    public int armor() {
        return minecraft().player.getArmorValue();
    }

    @Override
    public double yaw() {
        return exact(minecraft().player.getYRot());
    }

    @Override
    public double pitch() {
        return exact(minecraft().player.getXRot());
    }

    @Override
    public double speed() {
        Vec3 velocity = minecraft().player.getDeltaMovement();
        return Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    }

    @Override
    public double fallDistance() {
        return minecraft().player.fallDistance;
    }

    @Override
    public String dimension() {
        Minecraft minecraft = minecraft();
        if (minecraft.level == null) {
            return "";
        }
        if (minecraft.level != dimensionLevel) {
            dimensionLevel = minecraft.level;
            dimensionValue = minecraft.level.dimension().identifier().toString();
        }
        return dimensionValue;
    }

    @Override
    public boolean isInWater() {
        return minecraft().player.isInWater();
    }

    @Override
    public boolean isInLava() {
        return minecraft().player.isInLava();
    }

    @Override
    public boolean isOnFire() {
        return minecraft().player.isOnFire();
    }

    @Override
    public boolean isFlying() {
        return minecraft().player.getAbilities().flying;
    }

    @Override
    public boolean isSleeping() {
        return minecraft().player.isSleeping();
    }

    @Override
    public boolean isBlocking() {
        return minecraft().player.isBlocking();
    }

    @Override
    public boolean isUsingItem() {
        return minecraft().player.isUsingItem();
    }

    @Override
    public boolean isSwimming() {
        return minecraft().player.isSwimming();
    }

    @Override
    public boolean isInvisible() {
        return minecraft().player.isInvisible();
    }

    @Override
    public boolean screenOpen() {
        return minecraft().gui.screen() != null;
    }

    @Override
    public String screenTitle() {
        Screen screen = minecraft().gui.screen();
        return screen == null ? "" : screen.getTitle().getString();
    }

    @Override
    public String screenType() {
        Screen screen = minecraft().gui.screen();
        return screen == null ? "" : screen.getClass().getSimpleName();
    }

    public BlockChange brokenBlock(BlockPos pos, BlockState state) {
        return new BlockChange(BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString(), pos.getX(), pos.getY(), pos.getZ());
    }

    public void notePlaceAttempt(BlockHitResult hit, ItemStack stack) {
        ClientLevel level = minecraft().level;
        if (level == null || !(stack.getItem() instanceof BlockItem)) {
            return;
        }
        long deadline = level.getGameTime() + PLACE_WINDOW_TICKS;
        int attempt = ++placeAttempts;
        for (BlockPos pos : List.of(hit.getBlockPos(), hit.getBlockPos().relative(hit.getDirection()))) {
            pendingPlaces.add(new PendingPlace(attempt, pos.immutable(), blockName(level, pos), deadline));
        }
    }

    public List<BlockChange> drainPlacedBlocks() {
        ClientLevel level = minecraft().level;
        if (level == null) {
            pendingPlaces.clear();
            return List.of();
        }
        List<BlockChange> placed = new ArrayList<>();
        Set<Integer> done = new HashSet<>();
        for (PendingPlace pending : List.copyOf(pendingPlaces)) {
            if (done.contains(pending.attempt())) {
                continue;
            }
            String now = blockName(level, pending.pos());
            if (!now.equals(pending.before()) && !now.equals("minecraft:air")) {
                placed.add(new BlockChange(now, pending.pos().getX(), pending.pos().getY(), pending.pos().getZ()));
                done.add(pending.attempt());
            }
        }
        long time = level.getGameTime();
        pendingPlaces.removeIf(pending -> done.contains(pending.attempt()) || time > pending.deadline());
        return placed;
    }

    private static String blockName(ClientLevel level, BlockPos pos) {
        return BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
    }

    @Override
    public long gameTime() {
        return minecraft().level.getGameTime();
    }

    @Override
    public long dayTime() {
        return minecraft().level.getDefaultClockTime();
    }

    public EntityValue entityValue(Entity target) {
        return entity(target);
    }

    @Override
    public boolean isRaining() {
        return minecraft().level.isRaining();
    }

    @Override
    public boolean isThundering() {
        return minecraft().level.isThundering();
    }

    @Override
    public String difficulty() {
        return minecraft().level.getDifficulty().getSerializedName();
    }

    @Override
    public int playersOnline() {
        return minecraft().level.players().size();
    }

    @Override
    public String targetBlock() {
        Minecraft minecraft = minecraft();
        HitResult hit = freshHit();
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return "minecraft:air";
        }
        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        return BuiltInRegistries.BLOCK.getKey(minecraft.level.getBlockState(pos).getBlock()).toString();
    }

    @Override
    public EntityValue targetEntity() {
        HitResult hit = freshHit();
        if (hit == null || hit.getType() != HitResult.Type.ENTITY) {
            return null;
        }
        return entity(((EntityHitResult) hit).getEntity());
    }

    @Override
    public EntityValue nearestEntity() {
        syncLevel();
        if (modTick != nearestEntityTick) {
            nearestEntityTick = modTick;
            nearestEntityValue = entity(nearest(minecraft().level.entitiesForRendering()));
        }
        return nearestEntityValue;
    }

    @Override
    public EntityValue nearestPlayer() {
        syncLevel();
        if (modTick != nearestPlayerTick) {
            nearestPlayerTick = modTick;
            nearestPlayerValue = entity(nearest(minecraft().level.players()));
        }
        return nearestPlayerValue;
    }

    private static Entity nearest(Iterable<? extends Entity> candidates) {
        LocalPlayer player = minecraft().player;
        Entity nearest = null;
        double best = Double.MAX_VALUE;
        for (Entity candidate : candidates) {
            if (candidate == player) {
                continue;
            }
            double distance = player.distanceToSqr(candidate);
            if (distance < best) {
                best = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    @Override
    public void selectSlot(int slot) {
        minecraft().player.getInventory().setSelectedSlot(slot);
    }

    @Override
    public void swapHands() {
        clickMapping(minecraft().options.keySwapOffhand);
    }

    @Override
    public void dropItem(boolean wholeStack) {
        minecraft().player.drop(wholeStack);
    }

    @Override
    public void lookAt(double x, double y, double z) {
        LocalPlayer player = minecraft().player;
        double dx = x - player.getX();
        double dy = y - (player.getY() + player.getEyeHeight());
        double dz = z - player.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        player.setYRot((float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        player.setXRot((float) -Math.toDegrees(Math.atan2(dy, horizontal)));
    }

    @Override
    public void setYaw(double yaw) {
        minecraft().player.setYRot((float) yaw);
    }

    @Override
    public void setPitch(double pitch) {
        minecraft().player.setXRot((float) pitch);
    }

    @Override
    public void showTitle(String text) {
        Minecraft minecraft = minecraft();
        minecraft.gui.hud.setTimes(10, 70, 20);
        minecraft.gui.hud.setTitle(Component.literal(text));
    }

    @Override
    public void showSubtitle(String text) {
        minecraft().gui.hud.setSubtitle(Component.literal(text));
    }

    @Override
    public void showActionBar(String text) {
        minecraft().gui.hud.setOverlayMessage(Component.literal(text), false);
    }

    @Override
    public void playSound(String id) {
        SoundEvent event = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.tryParse(id));
        if (event == null) {
            return;
        }
        minecraft().level.playPlayerSound(event, SoundSource.MASTER, 1.0f, 1.0f);
    }

    @Override
    public void closeScreen() {
        Screen screen = minecraft().gui.screen();
        if (screen == null) {
            return;
        }
        screen.onClose();
    }

    @Override
    public String biome() {
        Minecraft minecraft = minecraft();
        return minecraft.level.getBiome(minecraft.player.blockPosition()).getRegisteredName();
    }

    @Override
    public int lightLevel() {
        Minecraft minecraft = minecraft();
        return minecraft.level.getBrightness(LightLayer.BLOCK, minecraft.player.blockPosition());
    }

    @Override
    public int skyLight() {
        Minecraft minecraft = minecraft();
        return minecraft.level.getBrightness(LightLayer.SKY, minecraft.player.blockPosition());
    }

    @Override
    public boolean canSeeSky() {
        Minecraft minecraft = minecraft();
        return minecraft.level.canSeeSky(minecraft.player.blockPosition());
    }

    @Override
    public String serverAddress() {
        ServerData server = minecraft().getCurrentServer();
        return server == null ? "singleplayer" : server.ip;
    }

    @Override
    public String serverBrand() {
        Minecraft minecraft = minecraft();
        if (minecraft.player == null) {
            return "";
        }
        String brand = minecraft.player.connection.serverBrand();
        return brand == null ? "" : brand;
    }

    @Override
    public int ping() {
        Minecraft minecraft = minecraft();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return 0;
        }
        PlayerInfo info = minecraft.getConnection().getPlayerInfo(minecraft.player.getUUID());
        return info == null ? 0 : info.getLatency();
    }

    @Override
    public int fps() {
        return minecraft().getFps();
    }

    @Override
    public double saturation() {
        return exact(minecraft().player.getFoodData().getSaturationLevel());
    }

    @Override
    public EntityValue vehicle() {
        return entity(minecraft().player.getVehicle());
    }

    @Override
    public List<String> onlinePlayerNames() {
        if (modTick != onlineNamesTick) {
            onlineNamesTick = modTick;
            onlineNamesValue = readOnlineNames();
        }
        return onlineNamesValue;
    }

    @Override
    public int effectLevel(String name) {
        return activeEffects().getOrDefault(effectId(name), 0);
    }

    @Override
    public Map<String, Integer> activeEffects() {
        if (modTick != effectsTick) {
            effectsTick = modTick;
            effectsValue = readEffects();
        }
        return effectsValue;
    }

    @Override
    public String blockAt(double x, double y, double z) {
        Minecraft minecraft = minecraft();
        BlockPos pos = BlockPos.containing(x, y, z);
        return BuiltInRegistries.BLOCK.getKey(minecraft.level.getBlockState(pos).getBlock()).toString();
    }

    @Override
    public int[] targetBlockPosition() {
        HitResult hit = freshHit();
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos pos = ((BlockHitResult) hit).getBlockPos();
        return new int[] {pos.getX(), pos.getY(), pos.getZ()};
    }

    @Override
    public int totalExperience() {
        return minecraft().player.totalExperience;
    }

    @Override
    public void openInventory() {
        clickMapping(minecraft().options.keyInventory);
    }

    @Override
    public void copyToClipboard(String text) {
        minecraft().keyboardHandler.setClipboard(text);
    }

    @Override
    public void takeScreenshot() {
        Screenshot.grab(minecraft(), false);
    }

    @Override
    public void disconnect() {
        minecraft().disconnectFromWorld(ClientLevel.DEFAULT_QUIT_MESSAGE);
    }

    private List<String> readOnlineNames() {
        nameScratch.clear();
        for (PlayerInfo info : minecraft().getConnection().getListedOnlinePlayers()) {
            nameScratch.add(info.getProfile().name());
        }
        nameScratch.sort(String::compareTo);
        if (nameScratch.equals(onlineNamesValue)) {
            return onlineNamesValue;
        }
        return List.copyOf(nameScratch);
    }

    private Map<String, Integer> readEffects() {
        effectScratch.clear();
        for (MobEffectInstance instance : minecraft().player.getActiveEffects()) {
            effectScratch.put(effectKey(instance.getEffect()), instance.getAmplifier() + 1);
        }
        if (effectScratch.equals(effectsValue)) {
            return effectsValue;
        }
        return Collections.unmodifiableMap(new TreeMap<>(effectScratch));
    }

    private String effectKey(Holder<MobEffect> effect) {
        String id = effectIds.get(effect);
        if (id == null) {
            id = effect.getRegisteredName();
            effectIds.put(effect, id);
        }
        return id;
    }

    private static String effectId(String name) {
        String trimmed = name.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        return trimmed.contains(":") ? trimmed : "minecraft:" + trimmed;
    }

    @Override
    public WorldSnapshot snapshot(SnapshotNeeds needs) {
        modTick++;
        if (!hasWorld()) {
            forgetLevel();
            return WorldSnapshot.empty();
        }
        Minecraft minecraft = minecraft();
        LocalPlayer player = minecraft.player;
        Inventory inventory = player.getInventory();
        List<ItemValue> items = new ArrayList<>();
        if (needs.inventory()) {
            for (int slot = 0; slot <= Inventory.SLOT_OFFHAND; slot++) {
                items.add(item(inventory.getItem(slot)));
            }
        }
        boolean usingItem = player.isUsingItem();
        ItemStack useStack = player.getUseItem();
        return new WorldSnapshot(
                true,
                player.getBlockX(),
                player.getBlockY(),
                player.getBlockZ(),
                player.onGround(),
                player.isShiftKeyDown(),
                player.isSprinting(),
                player.getHealth(),
                player.getFoodData().getFoodLevel(),
                player.experienceLevel,
                inventory.getSelectedSlot(),
                usingItem,
                minecraft.gui.screen() != null,
                minecraft.level.isRaining(),
                minecraft.level.isThundering(),
                minecraft.gameMode.getPlayerMode().getName(),
                player.fallDistance,
                player.getDeltaMovement().y,
                List.copyOf(items),
                needs.heldItem() ? item(inventory.getSelectedItem()) : ItemValue.empty(),
                needs.consume() && usingItem ? item(useStack) : ItemValue.empty(),
                needs.consume() && usingItem && useStack.has(DataComponents.CONSUMABLE),
                needs.consume() ? player.getUseItemRemainingTicks() : 0,
                needs.experience() ? player.totalExperience : 0,
                needs.effects() ? activeEffects() : Map.of(),
                needs.vehicle() ? entity(player.getVehicle()) : null,
                needs.dimension() ? dimension() : "",
                needs.onlineNames() ? onlinePlayerNames() : List.of());
    }

    private ItemValue item(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return ItemValue.empty();
        }
        return new ItemValue(itemId(stack.getItem()), stack.getHoverName().getString(),
                stack.getCount(), stack.getDamageValue(), stack.getMaxDamage());
    }

    private String itemId(Item item) {
        String id = itemIds.get(item);
        if (id == null) {
            id = BuiltInRegistries.ITEM.getKey(item).toString();
            itemIds.put(item, id);
        }
        return id;
    }

    private void syncLevel() {
        Minecraft minecraft = minecraft();
        if (minecraft.level == trackedLevel) {
            return;
        }
        HitResult stale = trackedLevel != null && minecraft.level != null ? minecraft.hitResult : null;
        forgetLevel();
        trackedLevel = minecraft.level;
        staleHit = stale;
    }

    private void forgetLevel() {
        trackedLevel = null;
        staleHit = null;
        dimensionLevel = null;
        dimensionValue = "";
        nearestEntityTick = Long.MIN_VALUE;
        nearestPlayerTick = Long.MIN_VALUE;
        nearestEntityValue = null;
        nearestPlayerValue = null;
    }

    private HitResult freshHit() {
        syncLevel();
        HitResult hit = minecraft().hitResult;
        return hit == staleHit ? null : hit;
    }

    private EntityValue entity(Entity target) {
        if (target == null) {
            return null;
        }
        return new EntityValue(BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString(), target.getName().getString(),
                target.getX(), target.getY(), target.getZ(), minecraft().player.distanceTo(target));
    }

    private static void clickMapping(KeyMapping mapping) {
        if (mapping.isUnbound()) {
            return;
        }
        KeyMapping.click(boundKey(mapping));
    }

    private static InputConstants.Key boundKey(KeyMapping mapping) {
        return InputConstants.getKey(mapping.saveString());
    }

    private static void show(Component component, String plain, boolean error) {
        Minecraft minecraft = minecraft();
        if (minecraft.player == null) {
            if (error) {
                MineSkriptClient.LOGGER.warn(plain);
            } else {
                MineSkriptClient.LOGGER.info(plain);
            }
            return;
        }
        minecraft.gui.hud.getChat().addClientSystemMessage(component);
    }
}
