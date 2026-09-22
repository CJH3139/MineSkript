package com.mineskript.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FakeGameBridge implements GameBridge {
    public final List<String> calls = new ArrayList<>();
    public final List<String> messages = new ArrayList<>();
    public final List<String> errors = new ArrayList<>();
    public final Set<String> keysDown = new HashSet<>();
    public final Map<String, String> blocks = new HashMap<>();
    public boolean hasWorld = true;
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

    public void setBlock(int dx, int dy, int dz, String id) {
        blocks.put(dx + "," + dy + "," + dz, id);
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
    }

    @Override
    public void sendCommand(String command) {
        calls.add("command:" + command);
    }

    @Override
    public void showMessage(String text) {
        messages.add(text);
    }

    @Override
    public void showError(String text) {
        errors.add(text);
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
}
