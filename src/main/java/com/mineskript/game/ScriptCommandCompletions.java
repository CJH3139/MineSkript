package com.mineskript.game;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.List;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;

public final class ScriptCommandCompletions {
    private static volatile List<String> labels = List.of();

    private ScriptCommandCompletions() {
    }

    static void set(List<String> next) {
        labels = List.copyOf(next);
    }

    public static void addTo(CommandDispatcher<ClientSuggestionProvider> dispatcher) {
        for (String label : labels) {
            dispatcher.register(LiteralArgumentBuilder.<ClientSuggestionProvider>literal(label)
                    .executes(context -> 0)
                    .then(RequiredArgumentBuilder.<ClientSuggestionProvider, String>argument("arguments",
                            StringArgumentType.greedyString()).executes(context -> 0)));
        }
    }
}
