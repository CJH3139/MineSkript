package com.mineskript;

import com.mineskript.script.FileReload;
import com.mineskript.script.LoadReport;
import com.mineskript.script.MessageLine;
import com.mineskript.script.Messages;
import com.mineskript.script.ScriptNames;
import com.mineskript.script.ScriptService;
import com.mineskript.script.VariablesReload;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;

public final class MineSkriptCommand {
    private MineSkriptCommand() {
    }

    public static void register(ScriptService service) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
            LiteralCommandNode<FabricClientCommandSource> root = dispatcher.register(tree(service));
            dispatcher.register(ClientCommands.literal("ms").executes(root.getCommand()).redirect(root));
        });
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> tree(ScriptService service) {
        return ClientCommands.literal("mineskript")
                .executes(ctx -> show(ctx.getSource(), Messages.help()))
                .then(ClientCommands.literal("help")
                        .executes(ctx -> show(ctx.getSource(), Messages.help())))
                .then(ClientCommands.literal("reload")
                        .executes(ctx -> reloadScripts(ctx.getSource(), service))
                        .then(ClientCommands.literal("scripts")
                                .executes(ctx -> reloadScripts(ctx.getSource(), service)))
                        .then(ClientCommands.literal("variables")
                                .executes(ctx -> reloadVariables(ctx.getSource(), service)))
                        .then(ClientCommands.literal("all")
                                .executes(ctx -> reloadEverything(ctx.getSource(), service)))
                        .then(ClientCommands.argument("file", StringArgumentType.word())
                                .suggests((ctx, builder) -> suggest(service, builder))
                                .executes(ctx -> reloadOne(ctx.getSource(), service, StringArgumentType.getString(ctx, "file")))))
                .then(ClientCommands.literal("list")
                        .executes(ctx -> show(ctx.getSource(), Messages.list(service.dir(), service.registry().scripts(), service.errors()))))
                .then(ClientCommands.literal("errors")
                        .executes(ctx -> show(ctx.getSource(), Messages.errors("the last load", service.errors(), service.sources()))))
                .then(ClientCommands.literal("info")
                        .executes(ctx -> show(ctx.getSource(), Messages.info(
                                version(),
                                service.dir(),
                                service.registry().scripts().size(),
                                service.registry().triggers().size(),
                                service.dispatcher().ticks()))))
                .then(ClientCommands.argument("rest", StringArgumentType.greedyString())
                        .executes(MineSkriptCommand::unknown));
    }

    private static int unknown(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        if (Messages.typedTheAlias(ctx.getInput())) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand()
                    .createWithContext(new StringReader(ctx.getInput()));
        }
        return show(ctx.getSource(), Messages.unknownBranch());
    }

    private static int reloadScripts(FabricClientCommandSource source, ScriptService service) {
        show(source, Messages.startingScripts());
        return showFullReload(source, service, service.reload());
    }

    private static int reloadEverything(FabricClientCommandSource source, ScriptService service) {
        show(source, Messages.startingEverything());
        return showFullReload(source, service, service.reloadAll());
    }

    private static int showFullReload(FabricClientCommandSource source, ScriptService service, LoadReport report) {
        show(source, Messages.reloaded(report, service.lastMillis()));
        if (report != null && !report.errors().isEmpty()) {
            show(source, Messages.errors("the last load", service.errors(), service.sources()));
        }
        return 1;
    }

    private static int reloadVariables(FabricClientCommandSource source, ScriptService service) {
        show(source, Messages.startingVariables(service.persistence().file()));
        VariablesReload done = service.reloadVariables();
        return show(source, Messages.variablesReloaded(service.persistence().file(), done, service.lastMillis()));
    }

    private static int reloadOne(FabricClientCommandSource source, ScriptService service, String file) {
        String name = service.canonical(file);
        show(source, Messages.startingFile(name));
        FileReload result = service.reloadFile(name);
        show(source, Messages.fileReload(result, service.dir()));
        if (!result.errors().isEmpty()) {
            show(source, Messages.errors(result.file(), result.errors(), service.sources()));
        }
        return 1;
    }

    private static CompletableFuture<Suggestions> suggest(ScriptService service, SuggestionsBuilder builder) {
        for (String name : ScriptNames.matching(service.dir(), service.loadedNames(), builder.getRemaining())) {
            builder.suggest(name);
        }
        return builder.buildFuture();
    }

    private static String version() {
        return FabricLoader.getInstance().getModContainer(MineSkriptClient.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }

    private static int show(FabricClientCommandSource source, List<MessageLine> lines) {
        MineSkriptMessages.send(source, lines);
        return 1;
    }
}
