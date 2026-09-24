package com.mineskript.syntax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.ScriptRunner;
import com.mineskript.client.visuals.elements.EffSetHologramText;
import com.mineskript.game.ClientEntityKind;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.TextColors;
import com.mineskript.script.EventDispatcher;
import com.mineskript.script.ScriptRegistry;
import org.junit.jupiter.api.Test;
import java.util.List;

class ClientVisualsTest {
    private final ScriptRunner runner = new ScriptRunner();
    private final FakeGameBridge game = runner.game;

    @Test
    void spawnsAHologramWithColouredTextAndGivesItsNumber() {
        runner.run("on load:\n    spawn a hologram with text \"&6home\" at location(0.5, 66, 0.5)\n"
                + "    set {h} to last spawned client entity\n");
        assertEquals(1.0, runner.global("h"));
        FakeGameBridge.ClientEntity hologram = game.clientEntities.get(1);
        assertEquals(new FakeGameBridge.ClientEntity(ClientEntityKind.HOLOGRAM, "§6home", 0.5, 66, 0.5, "t.ms"), hologram);
    }

    @Test
    void lastSpawnedIsNoneUntilSomethingIsSpawned() {
        runner.run("on load:\n    set {h} to last spawned client entity\n");
        assertEquals(None.NONE, runner.global("h"));
    }

    @Test
    void spawnsItemAndBlockDisplays() {
        game.setSlot(0, new ItemValue("minecraft:diamond", "Diamond", 3, 0, 0));
        runner.run("on load:\n    spawn an item display of diamond at location(1, 2, 3)\n"
                + "    spawn client item display with held item at location(4, 5, 6)\n"
                + "    spawn a block display of gold block at location(7, 8, 9)\n"
                + "    spawn hologram \"plain\" at location(0, 0, 0)\n");
        assertEquals(List.of(
                new FakeGameBridge.ClientEntity(ClientEntityKind.ITEM, "minecraft:diamond", 1, 2, 3, "t.ms"),
                new FakeGameBridge.ClientEntity(ClientEntityKind.ITEM, "minecraft:diamond", 4, 5, 6, "t.ms"),
                new FakeGameBridge.ClientEntity(ClientEntityKind.BLOCK, "minecraft:gold_block", 7, 8, 9, "t.ms"),
                new FakeGameBridge.ClientEntity(ClientEntityKind.HOLOGRAM, "plain", 0, 0, 0, "t.ms")),
                List.copyOf(game.clientEntities.values()));
    }

    @Test
    void anUnknownItemStopsTheLine() {
        runner.run("on load:\n    spawn an item display of unobtainium at location(1, 2, 3)\n    send \"after\"\n");
        assertEquals(1, game.errors.size());
        assertTrue(game.errors.get(0).contains("no item called unobtainium"), game.errors.get(0));
        assertTrue(game.messages.isEmpty());
    }

    @Test
    void movesRetextsAndRemovesByNumber() {
        runner.run("on load:\n    spawn a hologram \"a\" at location(0, 0, 0)\n    set {h} to last spawned client entity\n"
                + "    spawn a hologram \"b\" at location(0, 0, 0)\n    set {g} to last spawned hologram\n"
                + "    move client entity {h} to location(1, 2, 3)\n"
                + "    set text of hologram {h} to \"&cnew\"\n"
                + "    remove client entity {g}\n"
                + "    remove client entity 99\n"
                + "    move hologram 99 to location(1, 1, 1)\n");
        assertEquals(List.of(), game.errors);
        assertEquals(1, game.clientEntities.size());
        assertEquals(new FakeGameBridge.ClientEntity(ClientEntityKind.HOLOGRAM, "§cnew", 1, 2, 3, "t.ms"),
                game.clientEntities.get(1));
        runner.run("on load:\n    remove all client entities\n");
        assertTrue(game.clientEntities.isEmpty());
    }

    @Test
    void setTextOfHologramWinsOverTheGeneralSetEffect() {
        Event load = new Event.Load();
        assertInstanceOf(EffSetHologramText.class, SyntaxTestSupport.effect("set text of hologram 1 to \"x\"", load));
        assertFalse(SyntaxTestSupport.effect("set {text} to \"x\"", load) instanceof EffSetHologramText);
    }

    @Test
    void atMost256ClientEntities() {
        runner.run("on load:\n    loop 300 times:\n        spawn a hologram \"x\" at location(0, 0, 0)\n");
        assertEquals(256, game.clientEntities.size());
        assertEquals(1, game.errors.size());
        assertTrue(game.errors.get(0).contains("256"), game.errors.get(0));
    }

    @Test
    void beamsTakeADyeColourOrHexAndReplaceTheBeamOnTheSameBlock() {
        runner.run("on load:\n    show beam at location(1.7, 64, -2.2)\n    show a \"light blue\" beam at location(5, 70, 5)\n"
                + "    show a \"#FF8800\" beam at location(5, 70, 5)\n    show \"grey\" beam at location(0, 0, 0)\n");
        assertEquals(List.of(
                new FakeGameBridge.Beam(1, 64, -3, 0xF9FFFE, "t.ms"),
                new FakeGameBridge.Beam(5, 70, 5, 0xFF8800, "t.ms"),
                new FakeGameBridge.Beam(0, 0, 0, 0x474F52, "t.ms")), game.beams);
        runner.run("on load:\n    remove beam at location(5.5, 70, 5)\n    hide the beam at location(9, 9, 9)\n");
        assertEquals(2, game.beams.size());
        runner.run("on load:\n    remove all beams\n");
        assertTrue(game.beams.isEmpty());
    }

    @Test
    void anUnknownBeamColourStopsTheLine() {
        runner.run("on load:\n    show a \"sparkly\" beam at location(0, 0, 0)\n");
        assertTrue(game.beams.isEmpty());
        assertEquals(1, game.errors.size());
        assertTrue(game.errors.get(0).contains("not a beam colour"), game.errors.get(0));
    }

    @Test
    void atMost64BeamsButReplacingOneIsAlwaysAllowed() {
        runner.run("on load:\n    loop 70 times:\n        show beam at location(loop-iteration, 64, 0)\n");
        assertEquals(64, game.beams.size());
        assertEquals(1, game.errors.size());
        runner.run("on load:\n    show a \"red\" beam at location(1, 64, 0)\n");
        assertEquals(64, game.beams.size());
        assertEquals(1, game.errors.size());
    }

    @Test
    void unloadingAScriptRemovesOnlyItsVisualsAndLeavingRemovesAll() {
        ScriptRegistry registry = new ScriptRegistry();
        EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000), new Scheduler());
        Parser parser = new Parser(DefaultSyntax.registry());
        ParsedScript first = parser.parse("a.ms", "on load:\n    spawn a hologram \"a\" at location(0, 0, 0)\n    show beam at location(0, 0, 0)\n");
        ParsedScript second = parser.parse("b.ms", "on load:\n    spawn a hologram \"b\" at location(0, 0, 0)\n    show beam at location(1, 0, 0)\n");
        registry.replace(List.of(first, second));
        dispatcher.onLoad();
        assertEquals(2, game.clientEntities.size());
        assertEquals(2, game.beams.size());
        dispatcher.unloadScript("a.ms");
        assertEquals(List.of("b"), game.clientEntities.values().stream().map(FakeGameBridge.ClientEntity::content).toList());
        assertEquals(List.of(new FakeGameBridge.Beam(1, 0, 0, 0xF9FFFE, "b.ms")), game.beams);
        dispatcher.onDisconnect();
        assertTrue(game.clientEntities.isEmpty());
        assertTrue(game.beams.isEmpty());
        dispatcher.onLoad();
        dispatcher.reset();
        assertTrue(game.clientEntities.isEmpty());
        assertTrue(game.beams.isEmpty());
    }

    @Test
    void colourCodesTurnIntoSectionSigns() {
        assertEquals("§c§lhi", TextColors.colored("&c&Lhi"));
        assertEquals("salt & pepper &z", TextColors.colored("salt & pepper &z"));
        assertEquals("§aok", TextColors.colored("§aok"));
        assertEquals("end &", TextColors.colored("end &"));
    }

    @Test
    void theEffectsNeedAWorld() {
        game.hasWorld = false;
        runner.run("on load:\n    spawn a hologram \"x\" at location(0, 0, 0)\n");
        assertEquals(1, game.errors.size());
        assertTrue(game.clientEntities.isEmpty());
    }
}
