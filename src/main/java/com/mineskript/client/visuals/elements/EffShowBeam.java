package com.mineskript.client.visuals.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.game.GameBridge;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;
import java.util.OptionalInt;

@Name("Show Beam")
@Description({"Shows a beacon beam going straight up from the block at a location, drawn by your game only: nobody else sees it and no beacon is needed. Decimal coordinates are rounded down to the block. A beam already at that block is replaced.",
        "The colour is optional and written as text: one of the 16 dye colours (white, orange, magenta, light blue, yellow, lime, pink, gray, light gray, cyan, purple, blue, brown, green, red, black) or any colour as \"#rrggbb\". Without one the beam is white. An unknown colour stops the line with an error.",
        "A beam goes straight up through any blocks above it, like a beacon beam, and is drawn up to 512 blocks away (measured flat, ignoring height), even over chunks that are not loaded. At most 64 beams can be shown at once. They disappear when you leave the world or change dimension, and when the script that made them is reloaded, so a location in another dimension than yours stops the line with an error."})
@Examples({"on key press of \"b\":",
        "	show a \"red\" beam at location(100, 64, -200)",
        "	show beam at player",
        "",
        "on key press of \"n\":",
        "	show a \"#00ffaa\" beam at location(0, 70, 0)"})
@Since("1.0.0-alpha.9, 1.0.0-alpha.10 (locations)")
public final class EffShowBeam implements Statement {
    public static final int LIMIT = 64;

    private final int line;
    private final Expression color;
    private final Expression target;

    private EffShowBeam(int line, Expression color, Expression target) {
        this.line = line;
        this.color = color;
        this.target = target;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffShowBeam::create, "show [a] [%-string%] beam at %location%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0) != null && match.slot(0).isList() || match.slot(1).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffShowBeam(scope.line(), match.slot(0), match.slot(1)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        GameBridge game = context.world();
        int rgb = BeamColors.WHITE;
        if (color != null) {
            String name = (String) color.evaluate(context);
            OptionalInt found = BeamColors.rgb(name);
            if (found.isEmpty()) {
                throw new ScriptError("\"" + name + "\" is not a beam colour, use a dye colour such as red or"
                        + " light blue, or #rrggbb");
            }
            rgb = found.getAsInt();
        }
        Location block = Locations.here(target, context).blockCorner();
        int blockX = (int) block.x();
        int blockY = (int) block.y();
        int blockZ = (int) block.z();
        if (game.beamCount() >= LIMIT) {
            if (!game.removeBeam(blockX, blockY, blockZ)) {
                throw new ScriptError("there are already " + LIMIT + " beams, remove some first");
            }
        }
        game.showBeam(blockX, blockY, blockZ, rgb, context.triggerFile());
        return Flow.CONTINUE;
    }
}
