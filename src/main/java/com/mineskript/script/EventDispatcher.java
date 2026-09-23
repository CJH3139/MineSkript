package com.mineskript.script;

import com.mineskript.game.BlockChange;
import com.mineskript.game.GameBridge;
import com.mineskript.game.GameSignals;
import com.mineskript.game.SnapshotNeeds;
import com.mineskript.game.WorldSnapshot;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Event;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.Trigger;
import com.mineskript.lang.ast.WaitUntil;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Execution;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.ScriptControl;
import com.mineskript.lang.runtime.ScriptError;
import com.mineskript.lang.runtime.Variables;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

public final class EventDispatcher {
    public static final int SAVE_DELAY_TICKS = 60;

    private static final int TICKS_PER_SECOND = 20;

    private static final int TIME_JUMP_TICKS = 20;

    private static final Set<String> INVENTORY_EVENTS = Set.of("inventory", "held", "use start", "use stop");

    private static final Set<String> HELD_ITEM_EVENTS = Set.of("item break", "durability");

    private static final Set<String> CONSUME_EVENTS = Set.of("consume");

    private static final Set<String> EXPERIENCE_EVENTS = Set.of("xp");

    private static final Set<String> EFFECT_EVENTS = Set.of("effect gain", "effect lose");

    private static final Set<String> VEHICLE_EVENTS = Set.of("mount", "dismount");

    private static final Set<String> DIMENSION_EVENTS = Set.of("dimension");

    private static final Set<String> ONLINE_NAME_EVENTS = Set.of("player join", "player leave");

    private final ScriptRegistry registry;
    private final GameBridge game;
    private final Interpreter interpreter;
    private final Scheduler scheduler;
    private final Variables variables;
    private final Runnable saver;
    private final Runnable onWorldJoin;
    private record Parked(Execution execution, Condition condition, long parkTick, long deadlineTick, int line, long generation) {
    }

    private record Waiting(long resumeTick, long generation) {
    }

    private final ScriptControl control = new ScriptControl() {
        @Override
        public void stopAll() {
            for (ParsedScript script : registry.scripts()) {
                fileGeneration.merge(script.file(), 1L, Long::sum);
            }
            dropParked();
            game.releaseAll();
        }

        @Override
        public void stopScript(String file) {
            for (ParsedScript script : registry.scripts()) {
                if (script.file().equalsIgnoreCase(file)) {
                    dropFrames(script.file());
                }
            }
        }
    };

    private final Map<String, Boolean> keyState = new HashMap<>();
    private final List<Parked> parked = new ArrayList<>();
    private final Map<Execution, Waiting> scheduled = new IdentityHashMap<>();
    private final Map<String, Long> fileGeneration = new HashMap<>();
    private WorldSnapshot previous = WorldSnapshot.empty();
    private long ticks;
    private int lastSeenVersion;
    private long lastChangeTick;
    private boolean pendingSave;
    private boolean chatSending;
    private boolean commandSending;
    private SnapshotNeeds needed = SnapshotNeeds.nothing();
    private SnapshotNeeds previousNeeds = SnapshotNeeds.nothing();
    private SnapshotNeeds justEnabled = SnapshotNeeds.nothing();
    private int neededGeneration = -1;
    private long parkedEpoch;
    private boolean worldExpected;
    private boolean onlineNamesSeen;
    private Set<String> eventNames = Set.of();
    private long lastDayTime = Long.MIN_VALUE;

    public EventDispatcher(ScriptRegistry registry, GameBridge game, Interpreter interpreter, Scheduler scheduler) {
        this(registry, game, interpreter, scheduler, new Variables(), () -> {
        });
    }

    public EventDispatcher(ScriptRegistry registry, GameBridge game, Interpreter interpreter, Scheduler scheduler, Variables variables, Runnable saver) {
        this(registry, game, interpreter, scheduler, variables, saver, () -> {
        });
    }

    public EventDispatcher(ScriptRegistry registry, GameBridge game, Interpreter interpreter, Scheduler scheduler, Variables variables, Runnable saver, Runnable onWorldJoin) {
        this.registry = registry;
        this.game = game;
        this.interpreter = interpreter;
        this.scheduler = scheduler;
        this.variables = variables;
        this.saver = saver;
        this.onWorldJoin = onWorldJoin;
    }

    public long ticks() {
        return ticks;
    }

    int scheduledSize() {
        return scheduled.size();
    }

    int parkedSize() {
        return parked.size();
    }

    public void tick() {
        SnapshotNeeds needs = needs();
        WorldSnapshot current = game.snapshot(needs);
        justEnabled = newlyEnabled(previousNeeds, needs);
        previousNeeds = needs;
        if (game.hasWorld()) {
            ticks++;
        }
        diff(previous, current);
        previous = current;
        if (!game.hasWorld()) {
            dropParked();
            return;
        }
        for (Execution execution : scheduler.drain(ticks)) {
            Waiting waiting = scheduled.remove(execution);
            if (waiting == null || waiting.generation() != generationOf(execution)) {
                continue;
            }
            runSafely(execution);
            if (!game.hasWorld()) {
                return;
            }
        }
        resumeParked();
        if (!game.hasWorld()) {
            return;
        }
        for (Trigger trigger : registry.triggers()) {
            if (trigger.event() instanceof Event.Periodic periodic && ticks % periodic.intervalTicks() == 0) {
                start(trigger, Map.of());
                if (!game.hasWorld()) {
                    return;
                }
            }
        }
        pollKeys();
        if (!game.hasWorld()) {
            return;
        }
        trackTime();
        if (!game.hasWorld()) {
            return;
        }
        if (eventNames.contains("client tick")) {
            fireAll(trigger -> trigger.event() instanceof Event.State state && state.name().equals("client tick"), Map.of());
            if (!game.hasWorld()) {
                return;
            }
        }
        trackVariables();
    }

    public void onChat(String message) {
        if (!game.hasWorld()) {
            return;
        }
        fireAll(trigger -> trigger.event() instanceof Event.Chat, Map.of("message", message));
    }

    public boolean onChatSend(String message) {
        if (chatSending) {
            return true;
        }
        chatSending = true;
        try {
            return fireMessage(Event.ChatSend.class, message);
        } finally {
            chatSending = false;
        }
    }

    public boolean onCommandSend(String command) {
        if (commandSending) {
            return true;
        }
        commandSending = true;
        try {
            return fireMessage(Event.CommandSend.class, command);
        } finally {
            commandSending = false;
        }
    }

    public void onSignal(GameSignals.Signal signal) {
        if (signal.event().equals("disconnect")) {
            fireAll(trigger -> trigger.event() instanceof Event.State state && state.name().equals("disconnect"), signal.values());
            return;
        }
        if (!game.hasWorld()) {
            return;
        }
        fireAll(trigger -> trigger.event() instanceof Event.State state && state.name().equals(signal.event()), signal.values());
    }

    public void onFrame() {
        if (!game.hasWorld()) {
            return;
        }
        fireAll(trigger -> trigger.event() instanceof Event.State state && state.name().equals("frame"), Map.of());
    }

    public boolean runOneOff(Trigger trigger) {
        if (!game.hasWorld()) {
            return false;
        }
        boolean outer = worldExpected;
        worldExpected = true;
        try {
            return start(trigger, Map.of());
        } finally {
            worldExpected = outer;
        }
    }

    private boolean fireMessage(Class<? extends Event> type, String message) {
        if (!game.hasWorld()) {
            return true;
        }
        boolean allowed = true;
        boolean outer = worldExpected;
        worldExpected = true;
        try {
            for (Trigger trigger : List.copyOf(registry.triggers())) {
                if (worldEnded()) {
                    return allowed;
                }
                if (type.isInstance(trigger.event())) {
                    Context context = context(trigger, Map.of("message", message));
                    runSafely(new Execution(trigger, context));
                    allowed &= !context.cancelled();
                }
            }
        } finally {
            worldExpected = outer;
        }
        return allowed;
    }

    public void onLoad() {
        fireAll(trigger -> trigger.event() instanceof Event.Load, Map.of());
    }

    public void onLoad(String file) {
        fireAll(trigger -> trigger.event() instanceof Event.Load && trigger.file().equals(file), Map.of());
    }

    public void dropFrames(String file) {
        fileGeneration.merge(file, 1L, Long::sum);
        parked.removeIf(entry -> entry.execution().trigger().file().equals(file));
        List<Execution> pending = scheduler.drain(Long.MAX_VALUE);
        for (Execution execution : pending) {
            Waiting waiting = scheduled.remove(execution);
            if (waiting == null || execution.trigger().file().equals(file)) {
                continue;
            }
            scheduler.schedule(execution, waiting.resumeTick());
            scheduled.put(execution, waiting);
        }
    }

    public void onDisconnect() {
        dropParked();
        keyState.clear();
        game.releaseAll();
        pendingSave = false;
        lastSeenVersion = variables.version();
        saver.run();
    }

    public void reset() {
        onDisconnect();
        ticks = 0;
        lastChangeTick = 0;
    }

    private void dropParked() {
        parkedEpoch++;
        parked.clear();
        scheduled.clear();
        scheduler.clear();
    }

    private SnapshotNeeds needs() {
        int generation = registry.generation();
        if (generation != neededGeneration) {
            neededGeneration = generation;
            Set<String> names = new HashSet<>();
            eventNames = names;
            for (Trigger trigger : registry.triggers()) {
                if (trigger.event() instanceof Event.State state) {
                    names.add(state.name());
                } else if (trigger.event() instanceof Event.Durability) {
                    names.add("durability");
                }
            }
            needed = new SnapshotNeeds(
                    wanted(names, INVENTORY_EVENTS),
                    wanted(names, HELD_ITEM_EVENTS),
                    wanted(names, CONSUME_EVENTS),
                    wanted(names, EXPERIENCE_EVENTS),
                    wanted(names, EFFECT_EVENTS),
                    wanted(names, VEHICLE_EVENTS),
                    wanted(names, DIMENSION_EVENTS),
                    wanted(names, ONLINE_NAME_EVENTS));
            GameSignals.listen(names);
        }
        return needed;
    }

    private static SnapshotNeeds newlyEnabled(SnapshotNeeds before, SnapshotNeeds after) {
        return new SnapshotNeeds(
                !before.inventory() && after.inventory(),
                !before.heldItem() && after.heldItem(),
                !before.consume() && after.consume(),
                !before.experience() && after.experience(),
                !before.effects() && after.effects(),
                !before.vehicle() && after.vehicle(),
                !before.dimension() && after.dimension(),
                !before.onlineNames() && after.onlineNames());
    }

    private static boolean wanted(Set<String> loaded, Set<String> events) {
        for (String event : events) {
            if (loaded.contains(event)) {
                return true;
            }
        }
        return false;
    }

    private void trackTime() {
        if (!eventNames.contains("time change")) {
            lastDayTime = Long.MIN_VALUE;
            return;
        }
        long now = game.dayTime();
        long previousTime = lastDayTime;
        lastDayTime = now;
        if (previousTime != Long.MIN_VALUE && Math.abs(now - (previousTime + 1)) > TIME_JUMP_TICKS) {
            fireAll(trigger -> trigger.event() instanceof Event.State state && state.name().equals("time change"),
                    Map.of("time", (double) Math.floorMod(now, 24000L)));
        }
    }

    private void trackVariables() {
        int version = variables.version();
        if (version != lastSeenVersion) {
            lastSeenVersion = version;
            lastChangeTick = ticks;
            pendingSave = true;
        } else if (pendingSave && ticks - lastChangeTick >= SAVE_DELAY_TICKS) {
            pendingSave = false;
            saver.run();
        }
    }

    private void pollKeys() {
        for (String keyId : registry.watchedKeys()) {
            if (!game.hasWorld()) {
                return;
            }
            boolean down = game.isKeyDown(keyId);
            Boolean previous = keyState.put(keyId, down);
            if (previous == null || previous == down) {
                continue;
            }
            for (Trigger trigger : List.copyOf(registry.triggers())) {
                if (!game.hasWorld()) {
                    return;
                }
                if (down && trigger.event() instanceof Event.KeyPress press && press.keyId().equals(keyId) && modifiersHeld(press)) {
                    start(trigger, Map.of());
                } else if (!down && trigger.event() instanceof Event.KeyRelease release && release.keyId().equals(keyId)) {
                    start(trigger, Map.of());
                }
            }
        }
    }

    private boolean modifiersHeld(Event.KeyPress press) {
        for (String modifier : press.modifiers()) {
            boolean held = false;
            for (String keyId : modifier.split("\\|")) {
                held |= game.isKeyDown(keyId);
            }
            if (!held) {
                return false;
            }
        }
        return true;
    }

    private void diff(WorldSnapshot before, WorldSnapshot after) {
        boolean outer = worldExpected;
        worldExpected = after.hasWorld();
        try {
            if (!after.hasWorld()) {
                if (before.hasWorld()) {
                    fireState("leave", Map.of());
                }
                return;
            }
            if (!before.hasWorld()) {
                onlineNamesSeen = false;
                onWorldJoin.run();
                fireState("join", Map.of());
                return;
            }
            diffPosition(before, after);
            diffPose(before, after);
            diffHealth(before, after);
            diffStats(before, after);
            diffItems(before, after);
            diffWorldState(before, after);
            diffPresence(before, after);
        } finally {
            worldExpected = outer;
        }
    }

    private void diffPosition(WorldSnapshot before, WorldSnapshot after) {
        if (before.blockX() != after.blockX() || before.blockY() != after.blockY() || before.blockZ() != after.blockZ()) {
            fireState("move", Map.of(
                    "from x", (double) before.blockX(),
                    "from y", (double) before.blockY(),
                    "from z", (double) before.blockZ(),
                    "to x", (double) after.blockX(),
                    "to y", (double) after.blockY(),
                    "to z", (double) after.blockZ()));
        }
        if (before.onGround() && !after.onGround() && after.velocityY() > 0) {
            fireState("jump", Map.of());
        }
        if (!before.onGround() && after.onGround()) {
            fireState("land", Map.of("fall distance", Math.max(before.fallDistance(), after.fallDistance())));
        }
    }

    private void diffPose(WorldSnapshot before, WorldSnapshot after) {
        if (before.sneaking() != after.sneaking()) {
            fireState(after.sneaking() ? "sneak" : "unsneak", Map.of());
        }
        if (before.sprinting() != after.sprinting()) {
            fireState(after.sprinting() ? "sprint" : "unsprint", Map.of());
        }
    }

    private void diffHealth(WorldSnapshot before, WorldSnapshot after) {
        if (after.health() != before.health()) {
            fireState("health", Map.of("health change", after.health() - before.health(), "old health", before.health()));
        }
        if (after.health() <= 0 && before.health() > 0) {
            fireState("death", Map.of());
            return;
        }
        if (after.health() > 0 && before.health() <= 0) {
            fireState("respawn", Map.of());
            return;
        }
        if (after.health() < before.health()) {
            fireState("damage", Map.of("damage", before.health() - after.health()));
        } else if (after.health() > before.health() && before.health() > 0) {
            fireState("heal", Map.of("healed", after.health() - before.health()));
        }
    }

    private void diffStats(WorldSnapshot before, WorldSnapshot after) {
        if (before.hunger() != after.hunger()) {
            fireState("hunger", Map.of("hunger change", (double) (after.hunger() - before.hunger())));
        }
        if (before.xpLevel() != after.xpLevel()) {
            fireState("level", Map.of("level change", (double) (after.xpLevel() - before.xpLevel())));
        }
        if (after.xpLevel() > before.xpLevel()) {
            fireState("level up", Map.of("level change", (double) (after.xpLevel() - before.xpLevel())));
        }
        if (!justEnabled.experience() && before.xpLevel() == after.xpLevel() && before.totalExperience() != after.totalExperience()) {
            fireState("xp", Map.of("xp change", (double) (after.totalExperience() - before.totalExperience())));
        }
    }

    private void diffItems(WorldSnapshot before, WorldSnapshot after) {
        if (before.selectedSlot() != after.selectedSlot()) {
            fireState("held", Map.of(
                    "previous item", itemAt(before, before.selectedSlot()),
                    "item", itemAt(after, after.selectedSlot())));
        }
        List<ItemValue> was = before.inventory();
        List<ItemValue> now = after.inventory();
        if (!was.isEmpty() && !now.isEmpty()) {
            int size = Math.max(was.size(), now.size());
            for (int i = 0; i < size; i++) {
                ItemValue old = i < was.size() ? was.get(i) : ItemValue.empty();
                ItemValue fresh = i < now.size() ? now.get(i) : ItemValue.empty();
                if (!old.id().equals(fresh.id()) || old.count() != fresh.count()) {
                    fireState("inventory", Map.of("item", fresh));
                    break;
                }
            }
        }
        if (before.usingItem() != after.usingItem()) {
            fireState(after.usingItem() ? "use start" : "use stop", Map.of("item", itemAt(after, after.selectedSlot())));
        }
        if (before.consumingItem() && !after.consumingItem()
                && before.useItemRemaining() <= 2
                && after.useItemRemaining() == 0) {
            fireState("consume", Map.of("item", before.useItem()));
        }
        ItemValue wasHeld = before.heldItem();
        if (before.selectedSlot() == after.selectedSlot()
                && wasHeld.maxDamage() > 0
                && wasHeld.damage() >= wasHeld.maxDamage() - 1
                && after.heldItem().isEmpty()) {
            fireState("item break", Map.of("item", wasHeld));
        }
        ItemValue nowHeld = after.heldItem();
        if (before.selectedSlot() == after.selectedSlot() && wasHeld.id().equals(nowHeld.id()) && nowHeld.maxDamage() > 0) {
            double wasLeft = wasHeld.maxDamage() - wasHeld.damage();
            double nowLeft = nowHeld.maxDamage() - nowHeld.damage();
            fireWhere(trigger -> trigger.event() instanceof Event.Durability durability
                            && wasLeft >= durability.threshold() && nowLeft < durability.threshold(),
                    Map.of("item", nowHeld, "durability", nowLeft));
        }
    }

    private void diffPresence(WorldSnapshot before, WorldSnapshot after) {
        Map<String, Integer> wasEffects = justEnabled.effects() ? after.effects() : before.effects();
        Set<String> effectIds = new TreeSet<>(wasEffects.keySet());
        effectIds.addAll(after.effects().keySet());
        for (String id : effectIds) {
            Integer was = wasEffects.get(id);
            Integer now = after.effects().get(id);
            if (was == null && now != null) {
                fireState("effect gain", Map.of("effect", effectName(id), "effect level", (double) now));
            } else if (was != null && now == null) {
                fireState("effect lose", Map.of("effect", effectName(id)));
            }
        }
        EntityValue wasVehicle = before.vehicle();
        EntityValue nowVehicle = justEnabled.vehicle() ? wasVehicle : after.vehicle();
        if (wasVehicle != null && (nowVehicle == null || !wasVehicle.id().equals(nowVehicle.id()))) {
            fireState("dismount", Map.of("entity", wasVehicle));
        }
        if (nowVehicle != null && (wasVehicle == null || !wasVehicle.id().equals(nowVehicle.id()))) {
            fireState("mount", Map.of("entity", nowVehicle));
        }
        if (!justEnabled.dimension() && !before.dimension().equals(after.dimension())) {
            fireState("dimension", Map.of("from dimension", before.dimension(), "to dimension", after.dimension()));
        }
        diffOnlineNames(before, after);
    }

    private void diffOnlineNames(WorldSnapshot before, WorldSnapshot after) {
        List<String> was = before.onlineNames();
        List<String> now = after.onlineNames();
        boolean seen = onlineNamesSeen;
        onlineNamesSeen = seen || !was.isEmpty() || !now.isEmpty();
        if (was.equals(now)) {
            return;
        }
        if (was.isEmpty() && (!seen || justEnabled.onlineNames())) {
            return;
        }
        Set<String> wasNames = new HashSet<>(was);
        Set<String> nowNames = new HashSet<>(now);
        Set<String> names = new TreeSet<>(wasNames);
        names.addAll(nowNames);
        for (String name : names) {
            if (!wasNames.contains(name)) {
                fireState("player join", Map.of("player", name));
            } else if (!nowNames.contains(name)) {
                fireState("player leave", Map.of("player", name));
            }
        }
    }

    private static String effectName(String id) {
        int colon = id.indexOf(':');
        return colon < 0 ? id : id.substring(colon + 1);
    }

    private void diffWorldState(WorldSnapshot before, WorldSnapshot after) {
        if (before.screenOpen() != after.screenOpen()) {
            if (after.screenOpen()) {
                fireState("screen open", Map.of("screen title", game.screenTitle(), "screen type", game.screenType()));
            } else {
                fireState("screen close", Map.of());
            }
        }
        if (!before.gamemode().equals(after.gamemode())) {
            fireState("gamemode", Map.of("gamemode", after.gamemode()));
        }
        if (before.raining() != after.raining() || before.thundering() != after.thundering()) {
            fireState("weather", Map.of());
        }
    }

    private static ItemValue itemAt(WorldSnapshot snapshot, int slot) {
        List<ItemValue> items = snapshot.inventory();
        return slot >= 0 && slot < items.size() ? items.get(slot) : ItemValue.empty();
    }

    private void fireState(String name, Map<String, Object> values) {
        fireWhere(trigger -> trigger.event() instanceof Event.State state && state.name().equals(name), values);
    }

    private void fireWhere(Predicate<Trigger> match, Map<String, Object> values) {
        for (Trigger trigger : List.copyOf(registry.triggers())) {
            if (worldEnded()) {
                return;
            }
            if (match.test(trigger)) {
                start(trigger, values);
            }
        }
    }

    public void onBlockBreak(BlockChange change) {
        fireBlock("block break", change);
    }

    public void onBlockPlace(BlockChange change) {
        fireBlock("block place", change);
    }

    private void fireBlock(String name, BlockChange change) {
        if (!game.hasWorld()) {
            return;
        }
        fireAll(trigger -> trigger.event() instanceof Event.State state && state.name().equals(name), Map.of(
                "block", new BlockType(change.id()),
                "block x", (double) change.x(),
                "block y", (double) change.y(),
                "block z", (double) change.z()));
    }

    private void fireAll(Predicate<Trigger> match, Map<String, Object> values) {
        boolean outer = worldExpected;
        worldExpected = game.hasWorld();
        try {
            for (Trigger trigger : List.copyOf(registry.triggers())) {
                if (worldEnded()) {
                    return;
                }
                if (match.test(trigger)) {
                    start(trigger, values);
                }
            }
        } finally {
            worldExpected = outer;
        }
    }

    private boolean worldEnded() {
        return worldExpected && !game.hasWorld();
    }

    private boolean start(Trigger trigger, Map<String, Object> values) {
        return runSafely(new Execution(trigger, context(trigger, values)));
    }

    private Context context(Trigger trigger, Map<String, Object> values) {
        return new Context(game, trigger.file(), values, variables).control(control);
    }

    private void resumeParked() {
        if (parked.isEmpty()) {
            return;
        }
        long epoch = parkedEpoch;
        List<Parked> current = List.copyOf(parked);
        parked.clear();
        List<Parked> survivors = new ArrayList<>();
        for (Parked entry : current) {
            if (parkedEpoch != epoch || !game.hasWorld()) {
                return;
            }
            if (entry.generation() != generationOf(entry.execution())) {
                continue;
            }
            if (ticks <= entry.parkTick()) {
                survivors.add(entry);
                continue;
            }
            boolean satisfied;
            try {
                satisfied = entry.condition().test(entry.execution().context());
            } catch (RuntimeException error) {
                String message = error.getMessage();
                game.showError(new ScriptError(entry.execution().context().file(), entry.line(),
                        message == null ? error.getClass().getSimpleName() : message).toString());
                continue;
            }
            if (satisfied) {
                runSafely(entry.execution());
            } else if (ticks >= entry.deadlineTick()) {
                game.showError(new ScriptError(entry.execution().context().file(), entry.line(),
                        "wait until timed out after " + WaitUntil.TIMEOUT_TICKS / TICKS_PER_SECOND + " seconds").toString());
            } else {
                survivors.add(entry);
            }
        }
        if (parkedEpoch != epoch) {
            return;
        }
        for (Parked entry : survivors) {
            if (entry.generation() == generationOf(entry.execution())) {
                parked.add(entry);
            }
        }
    }

    private boolean runSafely(Execution execution) {
        long epoch = parkedEpoch;
        long generation = generationOf(execution);
        try {
            if (interpreter.run(execution) == Interpreter.Outcome.WAITING) {
                if (parkedEpoch != epoch || generationOf(execution) != generation) {
                    return true;
                }
                if (execution.waitCondition() != null) {
                    parked.add(new Parked(execution, execution.waitCondition(), ticks,
                            ticks + execution.waitTimeoutTicks(), execution.waitLine(), generation));
                } else {
                    long resume = ticks + execution.waitTicks();
                    scheduler.schedule(execution, resume);
                    scheduled.put(execution, new Waiting(resume, generation));
                }
            }
        } catch (ScriptError error) {
            game.showError(error.toString());
            return false;
        }
        return true;
    }

    private long generationOf(Execution execution) {
        return fileGeneration.getOrDefault(execution.trigger().file(), 0L);
    }
}
