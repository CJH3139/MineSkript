package com.mineskript.client.inventory.elements;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.function.Function;

/** Shared base for the properties of an item ("name of %item%" and friends). Not a syntax element itself. */
abstract class ItemPropertyExpression implements Expression {
    private final SkType type;
    private final Function<ItemValue, Object> reader;
    private final Expression item;

    ItemPropertyExpression(SkType type, Function<ItemValue, Object> reader, Expression item) {
        this.type = type;
        this.reader = reader;
        this.item = item;
    }

    @Override
    public SkType type() {
        return type;
    }

    @Override
    public Object evaluate(Context context) {
        Object value = item.evaluate(context);
        if (!(value instanceof ItemValue found)) {
            throw new ScriptError("there is no item");
        }
        return reader.apply(found);
    }
}
