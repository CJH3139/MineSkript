package com.mineskript.client.visuals.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Last Spawned Client Entity")
@Description({"The number of the client entity (hologram, item display or block display) that spawn last made in this trigger, like Skript's last spawned entity. Keep it in a variable to move the entity, change a hologram's text or remove it later.",
        "It is none until the trigger spawns something. The number stays valid until the entity is removed; using it after that does nothing."})
@Examples({"on key press of \"h\":",
        "\tspawn a hologram \"&ahello\" at location(0.5, 70, 0.5)",
        "\tset {-hello} to last spawned client entity",
        "",
        "on key press of \"g\":",
        "\tremove client entity {-hello}"})
@Since("1.0.0-alpha.9")
public final class ExprLastSpawnedClientEntity implements Expression {
    private ExprLastSpawnedClientEntity() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.NUMBER, Tier.SIMPLE,
                (match, scope) -> Optional.of(new ExprLastSpawnedClientEntity()),
                "[the] last spawned (client entity|hologram)");
    }

    @Override
    public SkType type() {
        return SkType.NUMBER;
    }

    @Override
    public Object evaluate(Context context) {
        return context.eventValueOrNone(EffSpawnClientEntity.LAST_SPAWNED);
    }
}
