package com.mineskript.syntax;

import com.mineskript.client.ClientModule;
import com.mineskript.common.CommonModule;
import com.mineskript.lang.module.ModuleLoader;
import com.mineskript.lang.module.SyntaxModule;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.List;

public final class DefaultSyntax {
    private DefaultSyntax() {
    }

    public static List<SyntaxModule> modules() {
        return List.of(new CommonModule(), new ClientModule());
    }

    public static void registerAll(SyntaxRegistry registry) {
        ModuleLoader.load(registry, modules());
    }

    public static SyntaxRegistry registry() {
        SyntaxRegistry registry = new SyntaxRegistry();
        registerAll(registry);
        return registry;
    }
}
