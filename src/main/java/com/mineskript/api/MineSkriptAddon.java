package com.mineskript.api;

import com.mineskript.lang.module.SyntaxModule;

/**
 * An addon: another Fabric mod that adds syntax to MineSkript. An addon is a {@link SyntaxModule}, organised the same
 * way as MineSkript's own syntax: it registers its own elements in {@link #register} and can split them into child
 * modules with {@link #children()}.
 *
 * <p>Implement this interface and list the class under the {@code mineskript} entrypoint in your
 * {@code fabric.mod.json}:
 *
 * <pre>{@code
 * "entrypoints": {
 *   "mineskript": ["com.example.myaddon.MyAddon"]
 * }
 * }</pre>
 *
 * <p>MineSkript creates one instance of each addon when the game starts and loads it once, after all of its own
 * modules. {@link #name()} is shown by {@code /ms info} and in the generated documentation, and is the addon's module
 * path, so its child modules are {@code My Addon/child}. The same registry is used for script files and for effect
 * commands, so addon syntax works in both. If loading the addon throws, MineSkript logs the error and loads without
 * that addon: nothing it registered is kept. See {@code ADDONS.md} in the MineSkript repository for a full example.
 */
public interface MineSkriptAddon extends SyntaxModule {
}
