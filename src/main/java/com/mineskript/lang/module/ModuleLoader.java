package com.mineskript.lang.module;

import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads module trees into a registry: each module that {@link SyntaxModule#canLoad() can load} registers its own
 * syntax, recorded under its path, and then its children load in order.
 */
public final class ModuleLoader {
    private ModuleLoader() {
    }

    /** Loads each top-level module, in order, with its name as its path. */
    public static void load(SyntaxRegistry registry, List<? extends SyntaxModule> modules) {
        Set<String> names = new HashSet<>();
        for (SyntaxModule module : modules) {
            String name = checkedName(module);
            if (!names.add(name)) {
                throw new IllegalArgumentException("two top-level modules are named " + name);
            }
            load(registry, module, name);
        }
    }

    /**
     * Loads one module tree with the given path for its top module, which is how an addon is loaded under its display
     * name. Children get the path {@code path/childName}.
     */
    public static void load(SyntaxRegistry registry, SyntaxModule module, String path) {
        if (!module.canLoad()) {
            return;
        }
        registry.registerIn(path, module::register);
        Set<String> names = new HashSet<>();
        for (SyntaxModule child : module.children()) {
            String name = checkedName(child);
            if (!names.add(name)) {
                throw new IllegalArgumentException("module " + path + " has two children named " + name);
            }
            load(registry, child, path + "/" + name);
        }
    }

    /** Every module in the trees, depth first in load order, by path, whether or not it can load. */
    public static Map<String, SyntaxModule> tree(List<? extends SyntaxModule> modules) {
        Map<String, SyntaxModule> tree = new LinkedHashMap<>();
        for (SyntaxModule module : modules) {
            collect(module, checkedName(module), tree);
        }
        return tree;
    }

    private static void collect(SyntaxModule module, String path, Map<String, SyntaxModule> tree) {
        if (tree.putIfAbsent(path, module) != null) {
            throw new IllegalArgumentException("two modules have the path " + path);
        }
        for (SyntaxModule child : module.children()) {
            collect(child, path + "/" + checkedName(child), tree);
        }
    }

    private static String checkedName(SyntaxModule module) {
        String name = module.name();
        if (name == null || name.isBlank() || name.contains("/")) {
            throw new IllegalArgumentException("module " + module.getClass().getName() + " has the invalid name \""
                    + name + "\": a module name must not be blank or contain /");
        }
        return name;
    }
}
