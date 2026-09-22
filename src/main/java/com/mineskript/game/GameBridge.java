package com.mineskript.game;

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

    void sendCommand(String command);

    void showMessage(String text);

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
}
