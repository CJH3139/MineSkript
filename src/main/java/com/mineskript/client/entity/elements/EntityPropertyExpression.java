package com.mineskript.client.entity.elements;

import com.mineskript.lang.ast.EntityValue;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.function.Function;

/** Shared base for the properties of an entity ("name of %entity%" and friends). Not a syntax element itself. */
abstract class EntityPropertyExpression implements Expression {
    private final SkType type;
    private final Function<EntityValue, Object> reader;
    private final Expression entity;

    EntityPropertyExpression(SkType type, Function<EntityValue, Object> reader, Expression entity) {
        this.type = type;
        this.reader = reader;
        this.entity = entity;
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
