package com.mineskript.game;

import com.mineskript.MineSkriptClient;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class MinecraftBridge implements GameBridge {
    private final Set<String> heldKeys = new HashSet<>();
    private boolean attackHeld;
    private boolean useHeld;

    private static Minecraft minecraft() {
        return Minecraft.getInstance();
    }

    @Override
    public boolean hasWorld() {
        Minecraft minecraft = minecraft();
        return minecraft.player != null && minecraft.level != null;
    }

    @Override
    public void clickAttack() {
        KeyMapping.click(boundKey(minecraft().options.keyAttack));
    }

    @Override
    public void clickUse() {
        KeyMapping.click(boundKey(minecraft().options.keyUse));
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
        minecraft().player.connection.sendChat(text);
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
    public void showError(String text) {
        show(Component.literal("mineskript: " + text).withStyle(ChatFormatting.RED), "mineskript: " + text, true);
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
