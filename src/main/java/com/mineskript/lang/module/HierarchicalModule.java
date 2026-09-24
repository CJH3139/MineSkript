package com.mineskript.lang.module;

import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.List;

/**
 * A module made of child modules, like Skript's hierarchical addon modules: it loads its children in the order given
 * and registers nothing of its own unless a subclass overrides {@link #register}.
 */
public abstract class HierarchicalModule implements SyntaxModule {
    private final List<SyntaxModule> children;

    protected HierarchicalModule(SyntaxModule... children) {
        this.children = List.of(children);
    }

    @Override
    public void register(SyntaxRegistry registry) {
    }

    @Override
    public final List<SyntaxModule> children() {
        return children;
    }
}
