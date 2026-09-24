package com.mineskript.api;

import com.mineskript.common.elements.expressions.ExprEventValue;
import com.mineskript.lang.ast.EventValue;
import com.mineskript.lang.module.ModuleLoader;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Builds the one syntax registry MineSkript parses with: its own modules, then each addon's module tree, in the order
 * given. An addon whose loading throws is left out entirely, and the registry is rebuilt without it so nothing it
 * registered before failing remains. An addon whose {@link MineSkriptAddon#canLoad()} is false is skipped without
 * being a failure. It takes the addons as a plain list, so it can be tested without Fabric.
 */
public final class AddonLoader {
    /** An addon that could not register its syntax, and what it threw. */
    public record Failure(String addon, Throwable error) {
    }

    /** The finished registry, the names of the addons loaded into it, and the addons that failed. */
    public record Result(SyntaxRegistry registry, List<String> addons, List<Failure> failures) {
    }

    private AddonLoader() {
    }

    /**
     * Registers every addon into a registry made by {@code base}, which must already hold MineSkript's own syntax.
     *
     * @param base makes a fresh registry with the built-in syntax; it is called again after each failing addon
     * @param addons the addons, in the order their syntax should be registered
     */
    public static Result load(Supplier<SyntaxRegistry> base, List<? extends MineSkriptAddon> addons) {
        List<MineSkriptAddon> remaining = new ArrayList<>(addons);
        List<Failure> failures = new ArrayList<>();
        while (true) {
            SyntaxRegistry registry = base.get();
            List<String> loaded = new ArrayList<>();
            MineSkriptAddon failed = null;
            for (MineSkriptAddon addon : remaining) {
                String name = nameOf(addon);
                try {
                    if (!addon.canLoad()) {
                        continue;
                    }
                    registry.registerAs(name, target -> register(target, addon, name));
                    loaded.add(name);
                } catch (Exception | LinkageError | StackOverflowError error) {
                    failures.add(new Failure(name, error));
                    failed = addon;
                    break;
                }
            }
            if (failed == null) {
                return new Result(registry, List.copyOf(loaded), List.copyOf(failures));
            }
            remaining.remove(failed);
        }
    }

    /**
     * Loads the addon's module tree under its name, then adds the generic event-name expression for every event value
     * it defined, the way the built-in values get theirs.
     */
    private static void register(SyntaxRegistry registry, MineSkriptAddon addon, String name) {
        Set<String> before = new HashSet<>();
        registry.eventValues().forEach(value -> before.add(value.name()));
        ModuleLoader.load(registry, addon, name);
        registry.registerIn(name, target -> {
            for (EventValue value : target.eventValues()) {
                if (value.generic() && !before.contains(value.name())) {
                    ExprEventValue.registerValue(target, value);
                }
            }
        });
    }

    /** The addon's name, or its class name when {@link MineSkriptAddon#name()} fails or is blank. */
    public static String nameOf(MineSkriptAddon addon) {
        String name;
        try {
            name = addon.name();
        } catch (Exception | LinkageError error) {
            name = null;
        }
        return name == null || name.isBlank() ? addon.getClass().getName() : name.strip();
    }
}
