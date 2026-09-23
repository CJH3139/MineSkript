package com.mineskript.syntax.expressions;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.Optional;
import java.util.function.Function;

public final class ExprItemProperty implements Expression {
    private final SkType type;
    private final Function<ItemValue, Object> reader;
    private final Expression item;

    private ExprItemProperty(SkType type, Function<ItemValue, Object> reader, Expression item) {
        this.type = type;
        this.reader = reader;
        this.item = item;
    }

    public static void register(SyntaxRegistry registry) {
        property(registry, "name", SkType.TEXT, ItemValue::name);
        property(registry, "id", SkType.TEXT, ItemValue::id);
        property(registry, "(count|amount)", SkType.NUMBER, item -> (double) item.count());
        property(registry, "damage", SkType.NUMBER, item -> (double) item.damage());
        property(registry, "max damage", SkType.NUMBER, item -> (double) item.maxDamage());
    }

    private static void property(SyntaxRegistry registry, String words, SkType type, Function<ItemValue, Object> reader) {
        registry.addExpression(type, Tier.PROPERTY,
                (match, scope) -> Optional.of(new ExprItemProperty(type, reader, match.slot(0))),
                "[the] " + words + " of %item%",
                "%item%'s " + words);
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
