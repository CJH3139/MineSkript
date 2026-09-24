package com.mineskript.client.visuals.elements;

import com.mineskript.client.Locations;
import com.mineskript.client.TextColors;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.ClientEntityKind;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.BlockType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;
import java.util.OptionalInt;

@Name("Spawn Client Entity")
@Description({"Spawns an entity that exists only in your game, at a location: a hologram (a floating line of text that always faces you), an item display (a floating item) or a block display (a block drawn without being placed, with its corner at the location). Nobody else sees it, the server knows nothing about it, and you cannot hit or touch it. Other entity syntax, such as nearest entity and on entity spawn, ignores it.",
        "Get its number with last spawned client entity to move it, change a hologram's text or remove it later. Hologram text can use & colour codes, such as &c for red. The item or block is written like elsewhere, such as diamond or gold block, or taken from an item.",
        "Client entities are removed when you leave the world or change dimension, and when the script that made them is reloaded, so a location in another dimension than yours stops the line with an error. At most 256 can exist at once; spawning more stops the line with an error. An unknown item or block also stops the line with an error."})
@Examples({"on key press of \"h\":",
        "\tspawn a hologram with text \"&6home\" at location(0.5, 66, 0.5)",
        "\tset {-home hologram} to last spawned client entity",
        "",
        "on key press of \"j\":",
        "\tspawn an item display of diamond at 2 above player",
        "\tspawn a block display of gold block at location(10, 64, 10)"})
@Since("1.0.0-alpha.9, 1.0.0-alpha.10 (locations)")
public final class EffSpawnClientEntity implements Statement {
    public static final int LIMIT = 256;
    public static final String LAST_SPAWNED = "last spawned client entity";

    private static final ClientEntityKind[] KINDS = {
        ClientEntityKind.HOLOGRAM, ClientEntityKind.ITEM, ClientEntityKind.BLOCK
    };

    private final int line;
    private final ClientEntityKind kind;
    private final Expression content;
    private final Expression target;

    private EffSpawnClientEntity(int line, ClientEntityKind kind, Expression content, Expression target) {
        this.line = line;
        this.kind = kind;
        this.content = content;
        this.target = target;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffSpawnClientEntity::create,
                "spawn [a] [client] hologram [(with text|saying)] %string% at %location%",
                "spawn [(a|an)] [client] item display (of|with) %itemtype% at %location%",
                "spawn [a] [client] block display (of|with) %blocktype% at %location%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffSpawnClientEntity(scope.line(), KINDS[match.patternIndex()], match.slot(0),
                match.slot(1)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        if (game.clientEntityCount() >= LIMIT) {
            throw new ScriptError("there are already " + LIMIT + " client entities, remove some first");
        }
        Location location = Locations.here(target, context);
        Object value = content.evaluate(context);
        String text = kind == ClientEntityKind.HOLOGRAM
                ? TextColors.colored(Converters.toText(value, context))
                : ((BlockType) value).id();
        OptionalInt handle = game.spawnClientEntity(kind, text, location.x(), location.y(), location.z(),
                context.triggerFile());
        if (handle.isEmpty()) {
            throw new ScriptError("there is no " + (kind == ClientEntityKind.ITEM ? "item" : "block") + " called "
                    + ((BlockType) value).path());
        }
        context.setEventValue(LAST_SPAWNED, (double) handle.getAsInt());
        return Flow.CONTINUE;
    }
}
