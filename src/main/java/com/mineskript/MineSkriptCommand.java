package com.mineskript;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.parse.ParsedScript;
import com.mineskript.script.LoadReport;
import com.mineskript.script.ScriptService;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class MineSkriptCommand {
    private MineSkriptCommand() {
    }

    public static void register(ScriptService service) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> dispatcher.register(
                ClientCommands.literal("mineskript")
                        .then(ClientCommands.literal("reload").executes(ctx -> {
                            report(ctx.getSource(), service.reload());
                            return 1;
                        }))
                        .then(ClientCommands.literal("list").executes(ctx -> {
                            list(ctx.getSource(), service);
                            return 1;
                        }))));
    }

    private static void report(FabricClientCommandSource source, LoadReport report) {
        source.sendFeedback(Component.literal(report.summary()));
        for (ParseError error : report.errors()) {
            source.sendFeedback(Component.literal(error.toString()).withStyle(ChatFormatting.RED));
        }
    }

    private static void list(FabricClientCommandSource source, ScriptService service) {
        if (service.registry().scripts().isEmpty()) {
            source.sendFeedback(Component.literal("No scripts loaded from " + service.dir()));
            return;
        }
        for (ParsedScript script : service.registry().scripts()) {
            String line = script.file() + ": " + LoadReport.plural(script.triggers().size(), "trigger");
            if (!script.errors().isEmpty()) {
                line += ", " + LoadReport.plural(script.errors().size(), "error");
            }
            source.sendFeedback(Component.literal(line));
        }
    }
}
