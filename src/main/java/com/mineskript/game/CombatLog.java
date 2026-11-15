package com.mineskript.game;

/**
 * Holds what the server last said about hits on you and your death, until the health poll that notices them takes
 * it. The damage packet and the health change it causes can land a tick or so apart, so each report is kept for a few
 * ticks and then dropped, so a stale one is never given to a later, unrelated change.
 */
public final class CombatLog {
    private static final int HIT_TICKS = 5;
    private static final int DEATH_TICKS = 20;

    private static String damageType;
    private static int attackerId = -1;
    private static int hitAge;
    private static String deathMessage;
    private static int deathAge;

    private CombatLog() {
    }

    public static void hit(String type, int attacker) {
        damageType = type;
        attackerId = attacker;
        hitAge = 0;
    }

    public static void died(String message) {
        deathMessage = message;
        deathAge = 0;
    }

    public static void tick() {
        if (damageType != null && ++hitAge > HIT_TICKS) {
            damageType = null;
        }
        if (deathMessage != null && ++deathAge > DEATH_TICKS) {
            deathMessage = null;
        }
    }

    static String takeDamageType() {
        String type = damageType;
        damageType = null;
        return type;
    }

    static int attackerId() {
        return attackerId;
    }

    static String takeDeathMessage() {
        String message = deathMessage;
        deathMessage = null;
        return message;
    }
}
