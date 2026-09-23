package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;
import java.util.function.Function;

public final class ExprEntityProperty implements Expression {
    private static final String AXES = "(x:x-coordinate|x:x-coord|x:x coordinate|x:x coord|y:y-coordinate|y:y-coord|y:y coordinate|y:y coord|z:z-coordinate|z:z-coord|z:z coordinate|z:z coord)";

    private final SkType type;
    private final Function<EntityValue, Object> reader;
    private final Expression entity;

    private ExprEntityProperty(SkType type, Function<EntityValue, Object> reader, Expression entity) {
        this.type = type;
        this.reader = reader;
        this.entity = entity;
    }

    public static void register(SyntaxRegistry registry) {
        property(registry, "name", SkType.TEXT, EntityValue::name);
        property(registry, "id", SkType.TEXT, EntityValue::id);
        registry.addExpression(SkType.NUMBER, Tier.PROPERTY, ExprEntityProperty::createCoordinate,
                "[the] " + AXES + " of %entity%",
                "%entity%'s " + AXES);
        property(registry, "distance", SkType.NUMBER, EntityValue::distance);
    }

    private static void property(SyntaxRegistry registry, String words, SkType type, Function<EntityValue, Object> reader) {
        registry.addExpression(type, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprEntityProperty(type, reader, match.slot(0))),
                "[the] " + words + " of %entity%",
                "%entity%'s " + words);
    }

    private static Optional<Expression> createCoordinate(Match match, ParseScope scope) {
        Function<EntityValue, Object> reader;
        if (match.has("x")) {
            reader = EntityValue::x;
        } else if (match.has("y")) {
            reader = EntityValue::y;
        } else {
            reader = EntityValue::z;
        }
        return Optional.of(new ExprEntityProperty(SkType.NUMBER, reader, match.slot(0)));
    }

    @Override
    public SkType type() {
        return type;
    }

    @Override
    public Object evaluate(Context context) {
        Object value = entity.evaluate(context);
        if (!(value instanceof EntityValue found)) {
            throw new ScriptError("there is no entity");
        }
        return reader.apply(found);
    }
}
