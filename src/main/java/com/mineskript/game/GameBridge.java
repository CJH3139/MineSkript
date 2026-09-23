package com.mineskript.game;

import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import java.util.List;
import java.util.Map;

public interface GameBridge {
    boolean hasWorld();

    void clickAttack();

    void clickUse();

    void setAttackHeld(boolean held);

    void setUseHeld(boolean held);

    void clickKey(String keyId);

    void setKeyHeld(String keyId, boolean held);

    boolean isKeyDown(String keyId);

    void sendChat(String text);

    boolean sendingOwnChat();

    void sendCommand(String command);

    void showMessage(String text);

    void showInfo(String text);

    void showWarning(String text);

    void showError(String text);

    double playerHealth();

    double playerMaxHealth();

    int playerHunger();

    String playerName();

    double playerX();

    double playerY();

    double playerZ();

    boolean isSneaking();

    boolean isOnGround();

    boolean isSprinting();

    String blockIdAt(int dx, int dy, int dz);

    void releaseAll();

    ItemValue heldItem();

    ItemValue offhandItem();

    ItemValue itemInSlot(int slot);

    int selectedSlot();

    int inventorySize();

    int freeSlots();

    int countItem(String id);

    String gamemode();

    int xpLevel();

    double xpProgress();

    int air();

    int maxAir();

    int armor();

    double yaw();

    double pitch();

    double speed();

    double fallDistance();

    String dimension();

    boolean isInWater();

    boolean isInLava();

    boolean isOnFire();

    boolean isFlying();

    boolean isSleeping();

    boolean isBlocking();

    boolean isUsingItem();

    boolean isSwimming();

    boolean isInvisible();

    boolean screenOpen();

    String screenTitle();

    String screenType();

    long gameTime();

    long dayTime();

    boolean isRaining();

    boolean isThundering();

    String difficulty();

    int playersOnline();

    String targetBlock();

    EntityValue targetEntity();

    EntityValue nearestEntity();

    EntityValue nearestPlayer();

    void selectSlot(int slot);

    void swapHands();

    void dropItem(boolean wholeStack);

    void lookAt(double x, double y, double z);

    void setYaw(double yaw);

    void setPitch(double pitch);

    void showTitle(String text);

    void showSubtitle(String text);

    void showActionBar(String text);

    void playSound(String id);

    void closeScreen();

    WorldSnapshot snapshot(SnapshotNeeds needs);

    default WorldSnapshot snapshot(boolean collectInventory) {
        return snapshot(SnapshotNeeds.everything(collectInventory));
    }

    String biome();

    int lightLevel();

    int skyLight();

    boolean canSeeSky();

    String serverAddress();

    String serverBrand();

    int ping();

    int fps();

    double saturation();

    EntityValue vehicle();

    List<String> onlinePlayerNames();

    int effectLevel(String name);

    Map<String, Integer> activeEffects();

    String blockAt(double x, double y, double z);

    int[] targetBlockPosition();

    int totalExperience();

    void openInventory();

    void copyToClipboard(String text);

    void takeScreenshot();

    void disconnect();
}
