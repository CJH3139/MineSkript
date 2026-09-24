package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.ast.EntityValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClientEnvironmentTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void sentTextTurnsColourCodesIntoFormatting() {
        runner.run("""
                on load:
                    send "&cred &Lbold &zplain & done"
                    send title "&6gold" with subtitle "&7grey"
                    send action bar "&aok"
                    show title "&btitle"
                """);
        assertEquals(List.of("§cred §lbold &zplain & done"), runner.game.messages);
        assertEquals(List.of("sendTitle:§6gold|§7grey|-1|-1|-1", "actionBar:§aok", "title:§btitle"),
                runner.game.calls);
    }

    @Test
    void environmentReadings() {
        runner.game.biome = "minecraft:desert";
        runner.game.lightLevel = 4;
        runner.game.skyLight = 11;
        runner.game.serverAddress = "play.example.com";
        runner.game.serverBrand = "paper";
        runner.game.ping = 57;
        runner.game.fps = 144;
        runner.game.saturation = 4.5;
        runner.run("""
                on load:
                    send "%biome%"
                    send "%light level% %block light level% %sky light%"
                    send "%server address% %server brand%"
                    send "%ping% %fps%"
                    send "%saturation%"
                """);
        assertEquals(List.of("minecraft:desert", "11 4 11", "play.example.com paper", "57 144", "4.5"), runner.game.messages);
    }

    @Test
    void vehicleReadsAsNoneOnFoot() {
        runner.run("on load:\n    send \"%vehicle%\"\n");
        assertEquals(List.of("<none>"), runner.game.messages);
        runner.game.messages.clear();
        runner.game.vehicle = new EntityValue("minecraft:horse", "Horse", 1, 2, 3, 0.0);
        runner.run("on load:\n    send \"%vehicle% %name of vehicle%\"\n");
        assertEquals(List.of("Horse Horse"), runner.game.messages);
    }

    @Test
    void onlinePlayerNamesIsAList() {
        runner.game.onlineNames.add("Zoe");
        runner.game.onlineNames.add("Alex");
        runner.run("""
                on load:
                    send "%online player names%"
                    loop online player names:
                        send "%loop-value%"
                """);
        assertEquals(List.of("Alex and Zoe", "Alex", "Zoe"), runner.game.messages);
    }

    @Test
    void effectLevelIsZeroWhenAbsent() {
        runner.game.effects.put("minecraft:speed", 2);
        runner.run("""
                on load:
                    set {present} to "speed"
                    set {absent} to "jump boost"
                    send "%level of effect {present}%"
                    send "%level of effect {absent}%"
                """);
        assertEquals(List.of("2", "0"), runner.game.messages);
    }

    @Test
    void blockAtAbsoluteCoordinates() {
        runner.game.setBlockAt(10, 64, 10, "minecraft:stone");
        runner.run("""
                on load:
                    send "%block at location(10, 64, 10)%"
                    send "%block at location(10, 65, 10)%"
                    if block at location(10, 64, 10) is stone:
                        send "is stone"
                    if block at location(10, 64, 10) is not dirt:
                        send "not dirt"
                """);
        assertEquals(List.of("stone", "air", "is stone", "not dirt"), runner.game.messages);
    }

    @Test
    void targetBlockCoordinates() {
        runner.game.targetBlock = "minecraft:diamond_ore";
        runner.game.targetBlockPosition = new int[] {12, 40, -7};
        runner.run("""
                on load:
                    send "%x coordinate of target block%"
                    send "%y coordinate of target block%"
                    send "%z-coord of target block%"
                """);
        assertEquals(List.of("12", "40", "-7"), runner.game.messages);
    }

    @Test
    void targetBlockCoordinatesErrorWithNoTarget() {
        runner.run("on load:\n    send \"%x coordinate of target block%\"\n");
        assertEquals(List.of("t.ms:2: there is no target block"), runner.game.errors);
    }

    @Test
    void effectRidingAndSkyConditions() {
        runner.game.effects.put("minecraft:speed", 1);
        runner.game.vehicle = new EntityValue("minecraft:horse", "Horse", 0, 0, 0, 0);
        runner.game.canSeeSky = true;
        runner.run("""
                on load:
                    if player has effect "speed":
                        send "fast"
                    if player doesn't have effect "haste":
                        send "no haste"
                    if player is riding:
                        send "riding"
                    if player can see sky:
                        send "sky"
                """);
        assertEquals(List.of("fast", "no haste", "riding", "sky"), runner.game.messages);
    }

    @Test
    void negatedRidingAndSky() {
        runner.game.canSeeSky = false;
        runner.run("""
                on load:
                    if player is not riding:
                        send "walking"
                    if player cannot see sky:
                        send "underground"
                """);
        assertEquals(List.of("walking", "underground"), runner.game.messages);
    }

    @Test
    void theNewEffectsReachTheBridge() {
        runner.run("""
                on load:
                    send command "/spawn"
                    open inventory
                    copy "hello" to clipboard
                    take screenshot
                    disconnect
                """);
        assertEquals(List.of("command:spawn", "openInventory", "clipboard:hello", "screenshot", "disconnect"), runner.game.calls);
    }

    @Test
    void sendCommandAndExecuteCommandAreTheSameEffect() {
        runner.run("""
                on load:
                    send command "spawn"
                    execute command "/home"
                    make player execute command "warp shop"
                """);
        assertEquals(List.of("command:spawn", "command:home", "command:warp shop"), runner.game.calls);
    }

    @Test
    void theClipboardAndTheScreenshotDoNotNeedAWorld() {
        runner.game.hasWorld = false;
        runner.run("""
                on load:
                    copy "ready" to clipboard
                    take screenshot
                """);
        assertEquals(List.of("clipboard:ready", "screenshot"), runner.game.calls);
        assertEquals(List.of(), runner.game.errors);
    }

    @Test
    void copyToClipboardAcceptsAnyValue() {
        runner.game.name = "Steve";
        runner.run("on load:\n    copy name of player to clipboard\n    copy \"%biome% at %light level%\" to the clipboard\n");
        assertEquals(List.of("clipboard:Steve", "clipboard:minecraft:plains at 15"), runner.game.calls);
    }
}
