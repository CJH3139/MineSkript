package com.mineskript.client.movement.elements;

import com.mineskript.client.Locations;
import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Look At")
@Description({"Turns your camera instantly so your eyes point at a location, such as location(0.5, 64.5, 0.5), a saved {home}, an entity or a block. Add 0.5 to block coordinates to aim at a block's centre; a block such as target block is aimed at by its corner.",
        "Needs a world. The turn is a single jump, not a smooth motion. A location in another dimension than yours stops the line with an error."})
@Examples({"on key press of \"l\":",
        "\tlook at location(0.5, 64.5, 0.5)",
        "\tsend \"facing spawn\"",
        "",
        "on key press of \"k\":",
        "\tif nearest player is set:",
        "\t\tlook at nearest player"})
@Since("1.0.0-alpha.2, 1.0.0-alpha.10 (locations)")
public final class EffLookAt implements Statement {
    private final int line;
    private final Expression target;

    private EffLookAt(int line, Expression target) {
        this.line = line;
        this.target = target;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect(EffLookAt::create, "look at %location%");
    }

    private static Optional<Statement> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        return Optional.of(new EffLookAt(scope.line(), match.slot(0)));
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        Location location = Locations.here(target, context);
        context.world().lookAt(location.x(), location.y(), location.z());
        return Flow.CONTINUE;
    }
}
