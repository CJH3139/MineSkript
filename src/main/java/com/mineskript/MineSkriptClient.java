package com.mineskript;

import com.mineskript.game.BlockChange;
import com.mineskript.game.GameSignals;
import com.mineskript.game.MinecraftBridge;
import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.runtime.Functions;
import com.mineskript.lang.runtime.Interpreter;
import com.mineskript.lang.runtime.Scheduler;
import com.mineskript.lang.runtime.Variables;
import com.mineskript.script.Config;
import com.mineskript.script.ConfigFile;
import com.mineskript.script.EffectCommands;
import com.mineskript.script.EventDispatcher;
import com.mineskript.script.LoadReport;
import com.mineskript.script.ScriptLoader;
import com.mineskript.script.ScriptRegistry;
import com.mineskript.script.ScriptService;
import com.mineskript.script.VariablePersistence;
import com.mineskript.script.VariableStore;
import com.mineskript.syntax.DefaultSyntax;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.client.player.ClientHotbarScrollEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MineSkriptClient implements ClientModInitializer {
    public static final String MOD_ID = "mineskript";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final int STEP_BUDGET = 10_000;

    private static ScriptService service;

    public static ScriptService service() {
        return service;
    }

    @Override
    public void onInitializeClient() {
        MinecraftBridge bridge = new MinecraftBridge();
        Functions functions = new Functions();
        ScriptRegistry registry = new ScriptRegistry(functions);
        Variables variables = new Variables();
        Path dir = FabricLoader.getInstance().getGameDir().resolve("mineskript");
        VariablePersistence persistence = new VariablePersistence(new VariableStore(), dir.resolve("variables.json"), variables, bridge);
        ConfigFile config = new ConfigFile(dir.resolve(Config.NAME));
        EventDispatcher dispatcher = new EventDispatcher(registry, bridge, new Interpreter(STEP_BUDGET), new Scheduler(), variables, persistence::save, () -> {
            persistence.flushWarning();
            config.flushTo(bridge);
        });
        service = new ScriptService(dir, new ScriptLoader(new Parser(DefaultSyntax.registry(), functions)), registry, dispatcher, persistence, config);
        EffectCommands effects = new EffectCommands(new Parser(DefaultSyntax.registry(), functions), dispatcher, config, bridge);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.level != null) {
                for (BlockChange placed : bridge.drainPlacedBlocks()) {
                    dispatcher.onBlockPlace(placed);
                }
            }
            for (GameSignals.Signal signal : GameSignals.drain()) {
                dispatcher.onSignal(signal);
            }
            dispatcher.tick();
        });
        GameSignals.onFrame(dispatcher::onFrame);
        ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (GameSignals.wants("entity spawn") && entity != Minecraft.getInstance().player) {
                GameSignals.emit("entity spawn", Map.of("entity", bridge.entityValue(entity)));
            }
        });
        ClientEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity == Minecraft.getInstance().player || Minecraft.getInstance().player == null) {
                return;
            }
            boolean died = entity instanceof LivingEntity living && living.isDeadOrDying();
            String event = died ? "entity death" : "entity despawn";
            if (GameSignals.wants(event)) {
                GameSignals.emit(event, Map.of("entity", bridge.entityValue(entity)));
            }
        });
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> GameSignals.emit("chunk load",
                Map.of("chunk x", (double) chunk.getPos().x(), "chunk z", (double) chunk.getPos().z())));
        ClientChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> GameSignals.emit("chunk unload",
                Map.of("chunk x", (double) chunk.getPos().x(), "chunk z", (double) chunk.getPos().z())));
        ClientHotbarScrollEvents.AFTER.register((inventory, currentSlot, newSlot, xOffset, yOffset) ->
                GameSignals.emit("scroll", Map.of("scroll", yOffset)));
        ClientPlayerBlockBreakEvents.AFTER.register((level, player, pos, state) -> dispatcher.onBlockBreak(bridge.brokenBlock(pos, state)));
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (level.isClientSide()) {
                bridge.notePlaceAttempt(hit, player.getItemInHand(hand));
            }
            return InteractionResult.PASS;
        });
        ClientReceiveMessageEvents.CHAT.register((message, signed, profile, params, timestamp) -> dispatcher.onChat(message.getString()));
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                dispatcher.onChat(message.getString());
            }
        });
        ClientSendMessageEvents.ALLOW_CHAT.register(message -> effects.allowChat(message) && dispatcher.onChatSend(message));
        ClientSendMessageEvents.ALLOW_COMMAND.register(dispatcher::onCommandSend);
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> client.execute(dispatcher::onDisconnect));
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            client.getSoundManager().addListener((sound, events, range) -> {
                if (GameSignals.wants("sound")) {
                    GameSignals.emit("sound", Map.of("sound", sound.getIdentifier().toString(),
                            "x", sound.getX(), "y", sound.getY(), "z", sound.getZ()));
                }
            });
            logReport(service.start());
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> service.saveVariables());
        MineSkriptCommand.register(service);
        LOGGER.info("MineSkript loaded, scripts folder {}", dir);
    }

    private static void logReport(LoadReport report) {
        LOGGER.info(report.summary());
        for (ParseError error : report.errors()) {
            LOGGER.warn(error.toString());
        }
    }
}
