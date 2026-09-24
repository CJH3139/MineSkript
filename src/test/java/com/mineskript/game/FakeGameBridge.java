package com.mineskript.game;

import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemDetails;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.script.OwnChatGuard;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

public final class FakeGameBridge implements GameBridge {
    public static final int MAIN_SLOTS = 36;
    public static final int OFFHAND_SLOT = 40;
    public static final int CONTAINER_SLOTS = 43;

    public final List<String> calls = new ArrayList<>();
    public final List<String> messages = new ArrayList<>();
    public final List<String> sentChat = new ArrayList<>();
    public final List<String> warnings = new ArrayList<>();
    public final List<String> errors = new ArrayList<>();
    public Predicate<String> chatHook;
    public final List<String> infos = new ArrayList<>();
    public final List<String> shown = new ArrayList<>();
    public final Set<String> keysDown = new HashSet<>();
    public final Map<String, String> blocks = new HashMap<>();
    public boolean hasWorld = true;
    private final OwnChatGuard ownChat = new OwnChatGuard();
    public double health = 20;
    public double maxHealth = 20;
    public int hunger = 20;
    public String name = "Steve";
    public double x = 0.5;
    public double y = 64;
    public double z = -3.5;
    public boolean sneaking;
    public boolean onGround = true;
    public boolean sprinting;
    public final ItemValue[] slots = new ItemValue[CONTAINER_SLOTS];
    public int inventoryScans;
    public int onlineNameScans;
    public ItemValue offhand = ItemValue.empty();
    public int selected;
    public String gamemode = "survival";
    public int xpLevel;
    public double xpProgress;
    public int air = 300;
    public int maxAir = 300;
    public int armor;
    public double yaw;
    public String clipboard = "";
    public double pitch;
    public double speed;
    public double fallDistance;
    public double velocityY;
    public String dimension = "minecraft:overworld";
    public boolean inWater;
    public boolean inLava;
    public boolean onFire;
    public boolean flying;
    public boolean sleeping;
    public boolean blocking;
    public boolean usingItem;
    public boolean swimming;
    public boolean invisible;
    public boolean screenOpen;
    public String screenTitle = "";
    public String screenType = "";
    public long gameTime;
    public long dayTime;
    public boolean raining;
    public boolean thundering;
    public String difficulty = "normal";
    public int playersOnline = 1;
    public String targetBlock = "minecraft:air";
    public EntityValue targetEntity;
    public EntityValue nearestEntity;
    public EntityValue nearestPlayer;
    public String biome = "minecraft:plains";
    public int lightLevel = 15;
    public int skyLight = 15;
    public boolean canSeeSky = true;
    public String serverAddress = "play.example.com";
    public String serverBrand = "vanilla";
    public int ping = 42;
    public int fps = 120;
    public double saturation = 5;
    public EntityValue vehicle;
    public final List<String> onlineNames = new ArrayList<>();
    public final Map<String, Integer> effects = new LinkedHashMap<>();
    public int totalExperience;
    public int[] targetBlockPosition;
    public final Map<String, String> absoluteBlocks = new HashMap<>();
    public ItemValue useItem = ItemValue.empty();
    public boolean consumingItem;
    public int useItemRemaining;

    public record ClientEntity(ClientEntityKind kind, String content, double x, double y, double z, String owner) {
    }

    public record Beam(int x, int y, int z, int rgb, String owner) {
    }

    public record Details(String renamedTo, List<String> lore, Map<String, Integer> enchantments,
            List<Double> customModelData, Map<String, String> components) implements ItemDetails {
        public static Details named(String renamedTo) {
            return new Details(renamedTo, List.of(), Map.of(), List.of(), Map.of());
        }

        @Override
        public Optional<String> customName() {
            return Optional.ofNullable(renamedTo);
        }

        @Override
        public Map<String, Integer> enchantments() {
            return Collections.unmodifiableMap(new TreeMap<>(enchantments));
        }

        @Override
        public Optional<String> component(String id) {
            return Optional.ofNullable(components.get(id));
        }
    }

    public final Set<String> knownIds = new HashSet<>(Set.of("minecraft:diamond", "minecraft:stone", "minecraft:gold_block"));
    public final Set<String> itemIds = new HashSet<>(Set.of("minecraft:diamond", "minecraft:stone",
            "minecraft:cobblestone", "minecraft:torch", "minecraft:bricks", "minecraft:brick", "minecraft:glass",
            "minecraft:potato", "minecraft:sweet_berries", "minecraft:oak_log", "minecraft:ender_pearl"));
    public final Map<String, String> biomes = new HashMap<>();
    public final Map<String, Integer> blockLight = new HashMap<>();
    public final Map<String, Integer> skyLightAt = new HashMap<>();
    public final Map<Integer, ClientEntity> clientEntities = new LinkedHashMap<>();
    public final List<Beam> beams = new ArrayList<>();
    private int nextHandle = 1;

    public void setBlock(int dx, int dy, int dz, String id) {
        blocks.put(dx + "," + dy + "," + dz, id);
    }

    public void setBlockAt(int x, int y, int z, String id) {
        absoluteBlocks.put(x + "," + y + "," + z, id);
    }

    public void setSlot(int slot, ItemValue item) {
        slots[slot] = item;
    }

    private ItemValue slot(int index) {
        if (index < 0 || index >= slots.length || slots[index] == null) {
            return ItemValue.empty();
        }
        return slots[index];
    }

    @Override
    public boolean hasWorld() {
        return hasWorld;
    }

    @Override
    public void clickAttack() {
        calls.add("clickAttack");
    }

    @Override
    public void clickUse() {
        calls.add("clickUse");
    }

    @Override
    public void setAttackHeld(boolean held) {
        calls.add("attack=" + held);
    }

    @Override
    public void setUseHeld(boolean held) {
        calls.add("use=" + held);
    }

    @Override
    public void clickKey(String keyId) {
        calls.add("click:" + keyId);
    }

    @Override
    public void setKeyHeld(String keyId, boolean held) {
        calls.add(keyId + "=" + held);
    }

    @Override
    public boolean isKeyDown(String keyId) {
        return keysDown.contains(keyId);
    }

    @Override
    public void sendChat(String text) {
        calls.add("chat:" + text);
        ownChat.around(() -> {
            if (chatHook == null || chatHook.test(text)) {
                sentChat.add(text);
            }
        });
    }

    @Override
    public boolean sendingOwnChat() {
        return ownChat.sending();
    }

    @Override
    public void sendCommand(String command) {
        calls.add("command:" + command);
    }

    @Override
    public void showMessage(String text) {
        messages.add(text);
        shown.add("message:" + text);
    }

    @Override
    public void showInfo(String text) {
        infos.add(text);
        shown.add("info:" + text);
    }

    @Override
    public void showWarning(String text) {
        warnings.add(text);
        shown.add("warning:" + text);
    }

    @Override
    public void showError(String text) {
        errors.add(text);
        shown.add("error:" + text);
    }

    @Override
    public double playerHealth() {
        return health;
    }

    @Override
    public double playerMaxHealth() {
        return maxHealth;
    }

    @Override
    public int playerHunger() {
        return hunger;
    }

    @Override
    public String playerName() {
        return name;
    }

    @Override
    public double playerX() {
        return x;
    }

    @Override
    public double playerY() {
        return y;
    }

    @Override
    public double playerZ() {
        return z;
    }

    @Override
    public boolean isSneaking() {
        return sneaking;
    }

    @Override
    public boolean isOnGround() {
        return onGround;
    }

    @Override
    public boolean isSprinting() {
        return sprinting;
    }

    @Override
    public String blockIdAt(int dx, int dy, int dz) {
        return blocks.getOrDefault(dx + "," + dy + "," + dz, "minecraft:air");
    }

    @Override
    public void releaseAll() {
        calls.add("releaseAll");
    }

    @Override
    public ItemValue heldItem() {
        return slot(selected);
    }

    @Override
    public ItemValue offhandItem() {
        return offhand;
    }

    @Override
    public ItemValue itemInSlot(int slot) {
        if (slot < 0 || slot > OFFHAND_SLOT) {
            return ItemValue.empty();
        }
        return slot(slot);
    }

    @Override
    public int selectedSlot() {
        return selected;
    }

    @Override
    public int inventorySize() {
        return MAIN_SLOTS;
    }

    @Override
    public int freeSlots() {
        int free = 0;
        for (int i = 0; i < MAIN_SLOTS; i++) {
            if (slot(i).isEmpty()) {
                free++;
            }
        }
        return free;
    }

    @Override
    public int countItem(String id) {
        int count = 0;
        for (int i = 0; i <= OFFHAND_SLOT; i++) {
            ItemValue item = slot(i);
            if (item.id().equals(id)) {
                count += item.count();
            }
        }
        return count;
    }

    @Override
    public String gamemode() {
        return gamemode;
    }

    @Override
    public int xpLevel() {
        return xpLevel;
    }

    @Override
    public double xpProgress() {
        return xpProgress;
    }

    @Override
    public int air() {
        return air;
    }

    @Override
    public int maxAir() {
        return maxAir;
    }

    @Override
    public int armor() {
        return armor;
    }

    @Override
    public double yaw() {
        return yaw;
    }

    @Override
    public double pitch() {
        return pitch;
    }

    @Override
    public double speed() {
        return speed;
    }

    @Override
    public double fallDistance() {
        return fallDistance;
    }

    @Override
    public String dimension() {
        return dimension;
    }

    @Override
    public boolean isInWater() {
        return inWater;
    }

    @Override
    public boolean isInLava() {
        return inLava;
    }

    @Override
    public boolean isOnFire() {
        return onFire;
    }

    @Override
    public boolean isFlying() {
        return flying;
    }

    @Override
    public boolean isSleeping() {
        return sleeping;
    }

    @Override
    public boolean isBlocking() {
        return blocking;
    }

    @Override
    public boolean isUsingItem() {
        return usingItem;
    }

    @Override
    public boolean isSwimming() {
        return swimming;
    }

    @Override
    public boolean isInvisible() {
        return invisible;
    }

    @Override
    public boolean screenOpen() {
        return screenOpen;
    }

    @Override
    public String screenTitle() {
        return screenOpen ? screenTitle : "";
    }

    @Override
    public String screenType() {
        return screenOpen ? screenType : "";
    }

    @Override
    public long gameTime() {
        return gameTime;
    }

    @Override
    public long dayTime() {
        return dayTime;
    }

    @Override
    public boolean isRaining() {
        return raining;
    }

    @Override
    public boolean isThundering() {
        return thundering;
    }

    @Override
    public String difficulty() {
        return difficulty;
    }

    @Override
    public int playersOnline() {
        return playersOnline;
    }

    @Override
    public String targetBlock() {
        return targetBlock;
    }

    @Override
    public EntityValue targetEntity() {
        return targetEntity;
    }

    @Override
    public EntityValue nearestEntity() {
        return nearestEntity;
    }

    @Override
    public EntityValue nearestPlayer() {
        return nearestPlayer;
    }

    @Override
    public String biome() {
        return biome;
    }

    @Override
    public String biomeAt(double x, double y, double z) {
        return biomes.getOrDefault(blockKey(x, y, z), biome);
    }

    @Override
    public int lightAt(double x, double y, double z, boolean sky) {
        return sky ? skyLightAt.getOrDefault(blockKey(x, y, z), skyLight)
                : blockLight.getOrDefault(blockKey(x, y, z), lightLevel);
    }

    @Override
    public int combinedLightAt(double x, double y, double z) {
        return Math.max(lightAt(x, y, z, false), lightAt(x, y, z, true));
    }

    @Override
    public boolean itemExists(String id) {
        if (itemIds.contains(id)) {
            return true;
        }
        for (int i = 0; i <= OFFHAND_SLOT; i++) {
            if (slot(i).id().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static String blockKey(double x, double y, double z) {
        return (int) Math.floor(x) + "," + (int) Math.floor(y) + "," + (int) Math.floor(z);
    }

    @Override
    public int lightLevel() {
        return lightLevel;
    }

    @Override
    public int skyLight() {
        return skyLight;
    }

    @Override
    public int combinedLight() {
        return Math.max(lightLevel, skyLight);
    }

    @Override
    public boolean canSeeSky() {
        return canSeeSky;
    }

    @Override
    public String serverAddress() {
        return serverAddress;
    }

    @Override
    public String serverBrand() {
        return serverBrand;
    }

    @Override
    public int ping() {
        return ping;
    }

    @Override
    public int fps() {
        return fps;
    }

    @Override
    public double saturation() {
        return saturation;
    }

    @Override
    public EntityValue vehicle() {
        return vehicle;
    }

    @Override
    public List<String> onlinePlayerNames() {
        List<String> names = new ArrayList<>(onlineNames);
        names.sort(String::compareTo);
        return List.copyOf(names);
    }

    @Override
    public int effectLevel(String name) {
        return effects.getOrDefault(effectId(name), 0);
    }

    @Override
    public Map<String, Integer> activeEffects() {
        return Collections.unmodifiableMap(new TreeMap<>(effects));
    }

    @Override
    public String blockAt(double x, double y, double z) {
        String key = (int) Math.floor(x) + "," + (int) Math.floor(y) + "," + (int) Math.floor(z);
        return absoluteBlocks.getOrDefault(key, "minecraft:air");
    }

    @Override
    public int[] targetBlockPosition() {
        return targetBlockPosition;
    }

    @Override
    public int totalExperience() {
        return totalExperience;
    }

    @Override
    public void openInventory() {
        calls.add("openInventory");
    }

    @Override
    public void copyToClipboard(String text) {
        clipboard = text;
        calls.add("clipboard:" + text);
    }

    @Override
    public String clipboard() {
        return clipboard;
    }

    @Override
    public void takeScreenshot() {
        calls.add("screenshot");
    }

    @Override
    public void disconnect() {
        calls.add("disconnect");
    }

    @Override
    public OptionalInt spawnClientEntity(ClientEntityKind kind, String content, double x, double y, double z, String owner) {
        if (kind != ClientEntityKind.HOLOGRAM && !knownIds.contains(content)) {
            return OptionalInt.empty();
        }
        int handle = nextHandle++;
        clientEntities.put(handle, new ClientEntity(kind, content, x, y, z, owner));
        return OptionalInt.of(handle);
    }

    @Override
    public int clientEntityCount() {
        return clientEntities.size();
    }

    @Override
    public boolean moveClientEntity(int handle, double x, double y, double z) {
        ClientEntity entity = clientEntities.get(handle);
        if (entity == null) {
            return false;
        }
        clientEntities.put(handle, new ClientEntity(entity.kind(), entity.content(), x, y, z, entity.owner()));
        return true;
    }

    @Override
    public boolean setClientEntityText(int handle, String text) {
        ClientEntity entity = clientEntities.get(handle);
        if (entity == null || entity.kind() != ClientEntityKind.HOLOGRAM) {
            return false;
        }
        clientEntities.put(handle, new ClientEntity(entity.kind(), text, entity.x(), entity.y(), entity.z(), entity.owner()));
        return true;
    }

    @Override
    public boolean removeClientEntity(int handle) {
        return clientEntities.remove(handle) != null;
    }

    @Override
    public void removeAllClientEntities() {
        clientEntities.clear();
    }

    @Override
    public void showBeam(int x, int y, int z, int rgb, String owner) {
        removeBeam(x, y, z);
        beams.add(new Beam(x, y, z, rgb, owner));
    }

    @Override
    public boolean removeBeam(int x, int y, int z) {
        return beams.removeIf(beam -> beam.x() == x && beam.y() == y && beam.z() == z);
    }

    @Override
    public void removeAllBeams() {
        beams.clear();
    }

    @Override
    public int beamCount() {
        return beams.size();
    }

    @Override
    public void removeClientVisuals(String owner) {
        clientEntities.values().removeIf(entity -> entity.owner().equals(owner));
        beams.removeIf(beam -> beam.owner().equals(owner));
    }

    private static String effectId(String name) {
        String trimmed = name.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        return trimmed.contains(":") ? trimmed : "minecraft:" + trimmed;
    }

    @Override
    public void selectSlot(int slot) {
        selected = slot;
        calls.add("selectSlot:" + slot);
    }

    @Override
    public void swapHands() {
        ItemValue temp = offhand;
        offhand = slots[selected];
        slots[selected] = temp;
        calls.add("swapHands");
    }

    @Override
    public void dropItem(boolean wholeStack) {
        calls.add(wholeStack ? "dropStack" : "dropItem");
    }

    @Override
    public void lookAt(double x, double y, double z) {
        calls.add("lookAt:" + x + "," + y + "," + z);
    }

    @Override
    public void setYaw(double yaw) {
        this.yaw = yaw;
        calls.add("yaw:" + yaw);
    }

    @Override
    public void setPitch(double pitch) {
        this.pitch = pitch;
        calls.add("pitch:" + pitch);
    }

    @Override
    public void showTitle(String text) {
        calls.add("title:" + text);
    }

    @Override
    public void showSubtitle(String text) {
        calls.add("subtitle:" + text);
    }

    @Override
    public void showActionBar(String text) {
        calls.add("actionBar:" + text);
    }

    @Override
    public void sendTitle(Optional<String> title, Optional<String> subtitle, int fadeInTicks, int stayTicks,
            int fadeOutTicks) {
        calls.add("sendTitle:" + title.orElse("-") + "|" + subtitle.orElse("-") + "|" + fadeInTicks + "|"
                + stayTicks + "|" + fadeOutTicks);
    }

    @Override
    public void playSound(String id) {
        calls.add("sound:" + id);
    }

    @Override
    public void closeScreen() {
        if (!screenOpen) {
            return;
        }
        calls.add("closeScreen");
        screenOpen = false;
    }

    @Override
    public WorldSnapshot snapshot(SnapshotNeeds needs) {
        if (!hasWorld) {
            return WorldSnapshot.empty();
        }
        List<ItemValue> items = new ArrayList<>();
        if (needs.inventory()) {
            inventoryScans++;
            for (int i = 0; i <= OFFHAND_SLOT; i++) {
                items.add(slot(i));
            }
        }
        if (needs.onlineNames()) {
            onlineNameScans++;
        }
        return new WorldSnapshot(
                true,
                (int) Math.floor(x),
                (int) Math.floor(y),
                (int) Math.floor(z),
                onGround,
                sneaking,
                sprinting,
                health,
                hunger,
                xpLevel,
                selected,
                usingItem,
                screenOpen,
                raining,
                thundering,
                gamemode,
                fallDistance,
                velocityY,
                List.copyOf(items),
                needs.heldItem() ? slot(selected) : ItemValue.empty(),
                needs.consume() ? useItem : ItemValue.empty(),
                needs.consume() && consumingItem,
                needs.consume() ? useItemRemaining : 0,
                needs.experience() ? totalExperience : 0,
                needs.effects() ? activeEffects() : Map.of(),
                needs.vehicle() ? vehicle : null,
                needs.dimension() ? dimension : "",
                needs.onlineNames() ? onlinePlayerNames() : List.of());
    }
}
