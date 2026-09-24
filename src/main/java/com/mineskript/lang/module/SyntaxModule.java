package com.mineskript.lang.module;

import com.mineskript.lang.parse.SyntaxRegistry;
import java.util.List;

/**
 * A group of syntax elements that belong together, the way Skript organises its syntax into addon modules. MineSkript
 * itself is two module trees, {@code common} (syntax that never touches the game) and {@code client} (one child per
 * game feature, such as {@code client/inventory}), and every addon is a module too.
 *
 * <p>{@link ModuleLoader} loads a module by calling {@link #register} and then loading each of its
 * {@link #children()} in order, so a child may rely on everything its parent registered. Which module registers an
 * element does not decide how scripts parse: conditions and effects are tried by
 * {@link com.mineskript.lang.parse.Priority} and expressions by {@link com.mineskript.lang.parse.Tier} first.
 */
public interface SyntaxModule {
    /**
     * A short name, unique among the module's siblings, such as {@code inventory}. A module's path joins the names
     * from the top-level module down with {@code /}, so a name must not contain {@code /}.
     */
    String name();

    /** Whether this module and its children load at all. Checked once, before {@link #register}. */
    default boolean canLoad() {
        return true;
    }

    /**
     * Registers this module's own events, conditions, effects and expressions. It runs once, while the game is
     * starting and before any world exists, so it must not touch the game.
     */
    void register(SyntaxRegistry registry);

    /** The modules loaded after this one, in order, under this module's path. */
    default List<SyntaxModule> children() {
        return List.of();
    }
}
