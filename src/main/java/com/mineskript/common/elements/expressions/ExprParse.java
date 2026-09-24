package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.TextPattern;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Name("Parse")
@Description({
        "Reads text as a type, or pulls several values out of text with a pattern, like Skript's parsed as. If the text does not fit, the result is none and parse error says why.",
        "With a pattern, write it in quotes with a %type% wherever a value should be read: \"%number% coins\", \"buying %itemtype% for %number%\". The result is a list of every value in order, so save it in a list variable: set {_parts::*} to message parsed as \"...\". A plural type such as %numbers% reads a list like 1, 2 and 3, and its values are added to the result one by one. [optional parts] and (either|or) choices work as in syntax patterns, letters are matched ignoring capitals, and a \\ makes the next character plain text.",
        "The types that can be read are text (or string), number, integer, boolean, timespan, itemtype, gamemode, potioneffecttype, enchantmenttype, entitytype and weathertype. An item type is read from any item name, without checking that the item exists. Like Skript, a %type% takes as little text as it can and leaves the rest to what follows, so \"%number%%string%\" reads 3x Booster as 3 and x Booster.",
        "Without a pattern, name the type: parsed as gamemode, item type, entity type, potion effect type, enchantment type, weather type or timespan. Parsed As Number, Parsed As Integer and Parsed As Boolean cover the rest."
})
@Examples({
        "on boss bar update:",
        "	if event-bossbar change is \"name\":",
        "		set {_parts::*} to event-text parsed as \"%number%x %string%\"",
        "		if {_parts::1} > 3:",
        "			send \"big %{_parts::2}%!\"",
        "",
        "on chat:",
        "	set {_trade::*} to message parsed as \"%string% is selling %itemtype% for %number% coins\"",
        "	if parse error is set:",
        "		stop",
        "	send \"%{_trade::1}% sells %{_trade::2}% for %{_trade::3}%\"",
        "",
        "on chat send:",
        "	set {_mode} to message parsed as gamemode",
        "	if {_mode} is set:",
        "		send \"that is a gamemode\""
})
@Since("1.0.0-alpha.13")
public final class ExprParse implements Expression {
    static final String ERROR = "parse error";

    private final Expression text;
    private final TextPattern pattern;
    private final TextPattern.Kind kind;

    private ExprParse(Expression text, TextPattern pattern, TextPattern.Kind kind) {
        this.text = text;
        this.pattern = pattern;
        this.kind = kind;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.OBJECT, Tier.COMBINED, ExprParse::create,
                "%string% parsed as %\"text\"%",
                "%string% parsed as [(a|an)] (gamemode:(gamemode|game mode)|itemtype:(itemtype|item type)"
                        + "|entitytype:(entitytype|entity type)|potioneffecttype:(potioneffecttype|potion effect [type])"
                        + "|enchantmenttype:(enchantmenttype|enchantment [type])|weathertype:(weathertype|weather [type])"
                        + "|timespan:(timespan|time span))");
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        if (match.slot(0).isList()) {
            return Optional.empty();
        }
        if (match.patternIndex() == 1) {
            for (TextPattern.Kind kind : TextPattern.Kind.values()) {
                if (match.has(kind.name().toLowerCase(Locale.ROOT))) {
                    return Optional.of(new ExprParse(match.slot(0), null, kind));
                }
            }
            return Optional.empty();
        }
        String source = (String) ((ConstantExpression) match.slot(1)).value();
        try {
            return Optional.of(new ExprParse(match.slot(0), TextPattern.compile(source), null));
        } catch (IllegalArgumentException error) {
            throw new SyntaxException("can't parse with the pattern \"" + source + "\": " + error.getMessage());
        }
    }

    @Override
    public SkType type() {
        if (kind != null) {
            return kind.type();
        }
        SkType type = pattern.slots().get(0).kind().type();
        for (TextPattern.Slot slot : pattern.slots()) {
            if (slot.kind().type() != type) {
                return SkType.OBJECT;
            }
        }
        return type;
    }

    @Override
    public boolean isList() {
        return pattern != null && (pattern.slots().size() > 1 || pattern.slots().get(0).plural());
    }

    @Override
    public Object evaluate(Context context) {
        String value = Converters.toText(text.evaluate(context), context);
        if (pattern == null) {
            Optional<Object> parsed = TextPattern.parseValue(value, kind);
            context.setEventValue(ERROR, parsed.isPresent() ? None.NONE
                    : value + " could not be parsed as " + describe(kind));
            return parsed.orElse(None.NONE);
        }
        Optional<List<Object>> parsed = pattern.match(value);
        context.setEventValue(ERROR, parsed.isPresent() ? None.NONE
                : value + " could not be parsed as \"" + pattern.source() + "\"");
        if (!isList()) {
            return parsed.filter(values -> !values.isEmpty()).<Object>map(values -> values.get(0)).orElse(None.NONE);
        }
        return parsed.orElse(List.of());
    }

    private static String describe(TextPattern.Kind kind) {
        return switch (kind) {
            case ITEMTYPE -> "an item type";
            case ENTITYTYPE -> "an entity type";
            case ENCHANTMENTTYPE -> "an enchantment type";
            case INTEGER -> "an integer";
            case POTIONEFFECTTYPE -> "a potion effect type";
            case WEATHERTYPE -> "a weather type";
            default -> "a " + kind.name().toLowerCase(Locale.ROOT);
        };
    }
}
