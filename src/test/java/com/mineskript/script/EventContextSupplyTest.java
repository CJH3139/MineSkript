package com.mineskript.script;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mineskript.game.BlockChange;
import com.mineskript.game.FakeGameBridge;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.parse.EventInfo;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.syntax.DefaultSyntax;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;

class EventContextSupplyTest {
    private static final Set<String> SUPPLIED_BY_GAME_HOOKS = Set.of("Action Bar", "Title", "Subtitle", "Boss Bar",
            "Sound", "Particle", "Entity Spawn", "Entity Despawn", "Entity Death", "Chunk Load", "Chunk Unload",
            "Scroll", "Toast", "Advancement", "Disconnect");

    private final FakeGameBridge game = new FakeGameBridge();
    private final ScriptRegistry registry = new ScriptRegistry();
    private final EventDispatcher dispatcher = new EventDispatcher(registry, game, new Interpreter(10_000),
            new Scheduler(), new Variables(), () -> {
            });
    private final Map<Integer, String> eventAtLine = new HashMap<>();
    private final Set<String> fired = new TreeSet<>();
    private final List<String> mismatches = new ArrayList<>();

    @Test
    void everyFiredEventSuppliesExactlyTheValuesItDeclares() {
        SyntaxRegistry syntax = DefaultSyntax.registry();
        loadATriggerForEveryEvent(syntax);
        dispatcher.onFiring(this::check);

        driveEveryPolledEvent();

        assertEquals(List.of(), mismatches);
        List<String> missed = new ArrayList<>();
        for (EventInfo info : syntax.eventInfos()) {
            boolean hasValues = !info.context().values().isEmpty();
            if (hasValues && !fired.contains(info.name()) && !SUPPLIED_BY_GAME_HOOKS.contains(info.name())) {
                missed.add(info.name());
            }
        }
        assertEquals(List.of(), missed, "events with values that this test never fired");
    }

    private void loadATriggerForEveryEvent(SyntaxRegistry syntax) {
        StringBuilder source = new StringBuilder();
        int line = 1;
        for (EventInfo info : syntax.eventInfos()) {
            String header = info.examples().get(0);
            source.append(header).append("\n    stop\n");
            eventAtLine.put(line, info.name());
            line += 2;
        }
        ParsedScript script = new Parser(syntax).parse("t.ms", source.toString());
        assertEquals(List.of(), script.errors());
        registry.replace(List.of(script));
    }

    private void check(Trigger trigger, Map<String, Object> values) {
        String event = eventAtLine.get(trigger.line());
        fired.add(event);
        Set<String> declared = new TreeSet<>(trigger.event().context().names());
        Set<String> supplied = new TreeSet<>(values.keySet());
        if (!declared.equals(supplied)) {
            mismatches.add(event + " declares " + declared + " but was given " + supplied);
        }
    }

    private void driveEveryPolledEvent() {
        game.setSlot(0, new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        game.setSlot(1, new ItemValue("minecraft:dirt", "dirt", 1, 0, 0));
        game.onlineNames.add("Alex");
        tick();
        game.x = 5.5;
        tick();
        game.onGround = false;
        game.velocityY = 0.42;
        tick();
        game.fallDistance = 3;
        game.onGround = true;
        tick();
        game.sneaking = true;
        game.sprinting = true;
        tick();
        game.sneaking = false;
        game.sprinting = false;
        tick();
        game.health = 14;
        tick();
        game.health = 18;
        tick();
        game.health = 0;
        tick();
        game.health = 20;
        tick();
        game.hunger = 18;
        game.xpLevel = 1;
        tick();
        game.totalExperience = 30;
        tick();
        game.selected = 1;
        tick();
        game.setSlot(4, new ItemValue("minecraft:dirt", "dirt", 3, 0, 0));
        tick();
        driveItemUse();
        driveHeldItemWear();
        game.effects.put("minecraft:speed", 1);
        game.vehicle = new EntityValue("minecraft:horse", "Horse", 1, 2, 3, 0.5);
        game.dimension = "minecraft:the_nether";
        game.onlineNames.add("Bob");
        game.screenOpen = true;
        game.screenTitle = "Chest";
        game.screenType = "ContainerScreen";
        game.gamemode = "creative";
        game.raining = true;
        tick();
        game.effects.clear();
        game.vehicle = null;
        game.onlineNames.remove("Bob");
        game.screenOpen = false;
        game.dayTime = 5000;
        tick();
        dispatcher.onBlockBreak(new BlockChange("minecraft:dirt", 1, 64, -2));
        dispatcher.onBlockPlace(new BlockChange("minecraft:stone", 0, 0, 0));
        dispatcher.onChat("hello");
        dispatcher.onChatSend("hello");
        dispatcher.onCommandSend("home");
        dispatcher.onFrame();
        dispatcher.onTooltip(new ItemValue("minecraft:stone", "stone", 1, 0, 0));
        game.hasWorld = false;
        tick();
    }

    private void driveItemUse() {
        ItemValue bread = new ItemValue("minecraft:bread", "bread", 3, 0, 0);
        game.usingItem = true;
        game.consumingItem = true;
        game.useItem = bread;
        game.useItemRemaining = 2;
        tick();
        game.usingItem = false;
        game.consumingItem = false;
        game.useItem = ItemValue.empty();
        game.useItemRemaining = 0;
        tick();
    }

    private void driveHeldItemWear() {
        game.setSlot(1, new ItemValue("minecraft:iron_pickaxe", "iron pickaxe", 1, 240, 250));
        tick();
        game.setSlot(1, new ItemValue("minecraft:iron_pickaxe", "iron pickaxe", 1, 249, 250));
        tick();
        game.setSlot(1, null);
        tick();
    }

    private void tick() {
        dispatcher.tick();
    }
}
