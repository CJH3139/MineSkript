package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.lang.ast.EntityValue;
import java.util.List;
import org.junit.jupiter.api.Test;

class SpellingCoverageTest {
    private final ScriptRunner runner = new ScriptRunner();

    @Test
    void spacedCaseSpellings() {
        runner.run("""
                on load:
                    set {greeting} to "hello there"
                    set {shout} to "HELLO THERE"
                    send "%upper case {greeting}%"
                    send "%lower case {shout}%"
                """);
        assertEquals(List.of("HELLO THERE", "hello there"), runner.game.messages);
    }

    @Test
    void singularCharacterSpelling() {
        runner.run("""
                on load:
                    set {word} to "minecraft"
                    send "%last 1 character of {word}%"
                """);
        assertEquals(List.of("t"), runner.game.messages);
    }

    @Test
    void ceilAndSingularPlaceSpellings() {
        runner.run("""
                on load:
                    send "%ceil 2.1%"
                    send "%3.14159 rounded to 1 place%"
                """);
        assertEquals(List.of("3", "3.1"), runner.game.messages);
    }

    @Test
    void containsSpellings() {
        runner.run("""
                on load:
                    if "minecraft" contain "craft":
                        send "contain"
                    if "minecraft" contain "zzz":
                        send "never contain"
                    if "minecraft" doesn't contain "zzz":
                        send "doesn't contain"
                    if "minecraft" doesn't contain "craft":
                        send "never doesn't contain"
                    if "minecraft" don't contain "zzz":
                        send "don't contain"
                    if "minecraft" don't contain "craft":
                        send "never don't contain"
                    if "minecraft" do not contain "zzz":
                        send "do not contain"
                    if "minecraft" do not contain "craft":
                        send "never do not contain"
                """);
        assertEquals(List.of("contain", "doesn't contain", "don't contain", "do not contain"), runner.game.messages);
    }

    @Test
    void startsWithSpellings() {
        runner.run("""
                on load:
                    if "minecraft" start with "mine":
                        send "start with"
                    if "minecraft" start with "craft":
                        send "never start with"
                    if "minecraft" does not start with "craft":
                        send "does not start with"
                    if "minecraft" does not start with "mine":
                        send "never does not start with"
                    if "minecraft" don't start with "craft":
                        send "don't start with"
                    if "minecraft" don't start with "mine":
                        send "never don't start with"
                    if "minecraft" do not start with "craft":
                        send "do not start with"
                    if "minecraft" do not start with "mine":
                        send "never do not start with"
                """);
        assertEquals(List.of("start with", "does not start with", "don't start with", "do not start with"),
                runner.game.messages);
    }

    @Test
    void endsWithSpellings() {
        runner.run("""
                on load:
                    if "minecraft" end with "craft":
                        send "end with"
                    if "minecraft" end with "mine":
                        send "never end with"
                    if "minecraft" does not end with "mine":
                        send "does not end with"
                    if "minecraft" does not end with "craft":
                        send "never does not end with"
                    if "minecraft" don't end with "mine":
                        send "don't end with"
                    if "minecraft" don't end with "craft":
                        send "never don't end with"
                    if "minecraft" do not end with "mine":
                        send "do not end with"
                    if "minecraft" do not end with "craft":
                        send "never do not end with"
                """);
        assertEquals(List.of("end with", "does not end with", "don't end with", "do not end with"),
                runner.game.messages);
    }

    @Test
    void ignoringCaseSpellings() {
        runner.run("""
                on load:
                    if "Hello" are "hello" ignoring case:
                        send "are"
                    if "Hello" are "world" ignoring case:
                        send "never are"
                    if "Hello" isn't "world" ignoring case:
                        send "isn't"
                    if "Hello" isn't "hello" ignoring case:
                        send "never isn't"
                    if "Hello" aren't "world" ignoring case:
                        send "aren't"
                    if "Hello" aren't "hello" ignoring case:
                        send "never aren't"
                    if "Hello" are not "world" ignoring case:
                        send "are not"
                    if "Hello" are not "hello" ignoring case:
                        send "never are not"
                """);
        assertEquals(List.of("are", "isn't", "aren't", "are not"), runner.game.messages);
    }

    @Test
    void everyTargetBlockAxisSpelling() {
        runner.game.targetBlock = "minecraft:diamond_ore";
        runner.game.targetBlockPosition = new int[] {12, 40, -7};
        runner.run("""
                on load:
                    send "%x-coordinate of target block%"
                    send "%x-coord of target block%"
                    send "%x coordinate of the target block%"
                    send "%x coord of the target block%"
                    send "%y-coordinate of target block%"
                    send "%y-coord of target block%"
                    send "%y coordinate of the target block%"
                    send "%y coord of the target block%"
                    send "%z-coordinate of target block%"
                    send "%z-coord of target block%"
                    send "%z coordinate of the target block%"
                    send "%z coord of the target block%"
                """);
        assertEquals(List.of("12", "12", "12", "12", "40", "40", "40", "40", "-7", "-7", "-7", "-7"),
                runner.game.messages);
    }

    @Test
    void totalExperienceSpellings() {
        runner.game.totalExperience = 1234;
        runner.run("""
                on load:
                    send "%total xp%"
                    send "%total experience%"
                    send "%the total xp%"
                """);
        assertEquals(List.of("1234", "1234", "1234"), runner.game.messages);
    }

    @Test
    void effectLevelOfSpelling() {
        runner.game.effects.put("minecraft:speed", 3);
        runner.run("""
                on load:
                    set {present} to "speed"
                    set {absent} to "jump boost"
                    send "%effect level of {present}%"
                    send "%effect level of {absent}%"
                """);
        assertEquals(List.of("3", "0"), runner.game.messages);
    }

    @Test
    void clientReadingSpellings() {
        runner.game.skyLight = 4;
        runner.game.serverAddress = "play.example.com";
        runner.game.fps = 144;
        runner.game.onlineNames.add("Alex");
        runner.run("""
                on load:
                    send "%sky light level%"
                    send "%server ip%"
                    send "%frame rate%"
                    send "%online player name%"
                """);
        assertEquals(List.of("4", "play.example.com", "144", "Alex"), runner.game.messages);
    }

    @Test
    void openGuiSpelling() {
        runner.run("on load:\n    open gui\n    open the gui\n");
        assertEquals(List.of("openInventory", "openInventory"), runner.game.calls);
    }

    @Test
    void everyDisconnectSpelling() {
        runner.run("""
                on load:
                    disconnect
                on load:
                    leave server
                on load:
                    leave the world
                on load:
                    leave the game
                on load:
                    quit the server
                on load:
                    quit world
                on load:
                    quit game
                """);
        assertEquals(List.of("disconnect", "disconnect", "disconnect", "disconnect", "disconnect", "disconnect",
                "disconnect"), runner.game.calls);
    }

    @Test
    void everyCanSeeSkySpelling() {
        runner.game.canSeeSky = true;
        runner.run("""
                on load:
                    if player can see the sky:
                        send "can see"
                    if player sees the sky:
                        send "sees"
                    if player can't see the sky:
                        send "never can't see"
                    if player cannot see the sky:
                        send "never cannot see"
                    if player can not see the sky:
                        send "never can not see"
                    if player doesn't see the sky:
                        send "never doesn't see"
                    if player does not see the sky:
                        send "never does not see"
                """);
        assertEquals(List.of("can see", "sees"), runner.game.messages);
        runner.game.messages.clear();
        runner.game.canSeeSky = false;
        runner.run("""
                on load:
                    if player can't see sky:
                        send "can't see"
                    if player cannot see sky:
                        send "cannot see"
                    if player can not see sky:
                        send "can not see"
                    if player doesn't see sky:
                        send "doesn't see"
                    if player does not see sky:
                        send "does not see"
                    if player can see sky:
                        send "never can see"
                    if player sees sky:
                        send "never sees"
                """);
        assertEquals(List.of("can't see", "cannot see", "can not see", "doesn't see", "does not see"),
                runner.game.messages);
    }

    @Test
    void bareCanIsNotAValidSkyPhrasing() {
        List<String> errors = runner.errorsOf("on load:\n    if player can sky:\n        send \"no\"\n");
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).contains("player can sky"), errors.get(0));
    }

    @Test
    void everyEffectConditionSpelling() {
        runner.game.effects.put("minecraft:speed", 1);
        runner.run("""
                on load:
                    if player have effect "speed":
                        send "have"
                    if player have effect "haste":
                        send "never have"
                    if player does not have effect "haste":
                        send "does not have"
                    if player does not have the effect "speed":
                        send "never does not have"
                    if player don't have effect "haste":
                        send "don't have"
                    if player don't have effect "speed":
                        send "never don't have"
                    if player do not have effect "haste":
                        send "do not have"
                    if player do not have effect "speed":
                        send "never do not have"
                """);
        assertEquals(List.of("have", "does not have", "don't have", "do not have"), runner.game.messages);
    }

    @Test
    void everyPlayerFlagReadsItsOwnValue() {
        runner.game.inWater = true;
        runner.game.inLava = false;
        runner.game.onFire = true;
        runner.game.flying = false;
        runner.game.sleeping = true;
        runner.game.blocking = false;
        runner.game.usingItem = true;
        runner.game.swimming = false;
        runner.game.invisible = true;
        runner.game.vehicle = new EntityValue("minecraft:horse", "Horse", 0, 0, 0, 0);
        runner.run("""
                on load:
                    if player is in the water:
                        send "water"
                    if player is in the lava:
                        send "lava"
                    if player is on fire:
                        send "fire"
                    if player is flying:
                        send "flying"
                    if player is sleeping:
                        send "sleeping"
                    if player is blocking:
                        send "blocking"
                    if player is using an item:
                        send "using"
                    if player is swimming:
                        send "swimming"
                    if player is invisible:
                        send "invisible"
                    if player is riding:
                        send "riding"
                """);
        assertEquals(List.of("water", "fire", "sleeping", "using", "invisible", "riding"), runner.game.messages);
    }

    @Test
    void hugeCharacterCountsClampToTheWholeText() {
        runner.run("""
                on load:
                    set {word} to "hello"
                    send "%first 3000000000 characters of {word}%"
                    send "%last 3000000000 characters of {word}%"
                    send "%{word} from character 1 to 3000000000%"
                """);
        assertEquals(List.of("hello", "hello", "hello"), runner.game.messages);
        assertEquals(List.of(), runner.game.errors);
    }

    @Test
    void randomIntegerSpansTheWholeLongRange() {
        runner.run("""
                on load:
                    set {n} to random integer between 1 and 9223372036854775807
                    if {n} is at least 1:
                        send "drew a number"
                """);
        assertEquals(List.of(), runner.game.errors);
        assertEquals(List.of("drew a number"), runner.game.messages);
    }

    @Test
    void readingsThatDoNotNeedAWorld() {
        runner.game.hasWorld = false;
        runner.game.fps = 144;
        runner.game.ping = 57;
        runner.game.serverAddress = "singleplayer";
        runner.game.serverBrand = "";
        runner.run("""
                on load:
                    send "%fps%"
                    send "%ping%"
                    send "[%server address%]"
                    send "[%server brand%]"
                """);
        assertEquals(List.of("144", "57", "[singleplayer]", "[]"), runner.game.messages);
        assertEquals(List.of(), runner.game.errors);
    }

    @Test
    void readingsThatDoNeedAWorldStillSayNoWorld() {
        runner.game.hasWorld = false;
        runner.run("on load:\n    send \"%biome%\"\n");
        assertEquals(List.of(), runner.game.messages);
        assertEquals(List.of("t.ms:2: no world"), runner.game.errors);
    }

    @Test
    void replacementThatIsNotFoundLeavesTheTextAlone() {
        runner.run("""
                on load:
                    set {word} to "abc"
                    set {missing} to "zz"
                    set {replacement} to "q"
                    send "[%{word} with {missing} replaced with {replacement}%]"
                """);
        assertEquals(List.of("[abc]"), runner.game.messages);
    }

    @Test
    void negativeLengthsAreEmpty() {
        runner.run("""
                on load:
                    set {word} to "abc"
                    send "[%first -2 characters of {word}%]"
                    send "[%last -2 characters of {word}%]"
                """);
        assertEquals(List.of("[]", "[]"), runner.game.messages);
        assertEquals(List.of(), runner.game.errors);
    }

    @Test
    void aStartPastTheEndIsEmpty() {
        runner.run("""
                on load:
                    set {word} to "abc"
                    send "[%{word} from character 9 to 12%]"
                    send "[%{word} from character 4 to 4%]"
                """);
        assertEquals(List.of("[]", "[]"), runner.game.messages);
        assertEquals(List.of(), runner.game.errors);
    }
}
