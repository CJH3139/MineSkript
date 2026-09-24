package com.mineskript.client.inventory.elements;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Condition;
import com.mineskript.lang.ast.EnchantmentType;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.ItemValue;
import com.mineskript.lang.parse.ListExpression;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Name("Is Enchanted")
@Description({
        "Checks whether an item is enchanted, like Skript's is enchanted. With an enchantment it checks for that one: held item is enchanted with sharpness is true at any level, and held item is enchanted with sharpness 3 needs exactly level 3, unless or better (also greater, higher, above) or or worse (also lesser, lower, below) follows. Enchantments are written out, such as fire aspect 2 or luck of the sea; enchantments from other mods need their namespace.",
        "Several enchantments joined by and must all be there; joined by or, one is enough. Enchanted books count the enchantments stored in them. Items read back from saved variables have no enchantments."
})
@Examples({
        "on key press of \"e\":",
        "	if held item is enchanted:",
        "		send \"%enchantments of held item%\"",
        "",
        "on key press of \"e\":",
        "	if tool of player is enchanted with efficiency 4 or better:",
        "		send \"fast pickaxe\"",
        "",
        "on key press of \"e\":",
        "	if held item is not enchanted with mending:",
        "		send \"this will not repair itself\""
})
@Since("1.0.0-alpha.11")
public final class CondIsEnchanted implements Condition {
    private static final String WITH = " [with %-enchantmenttypes% [or (better:(better|greater|higher|above)"
            + "|worse:(worse|lesser|lower|below))]]";

    private enum Bound {
        EXACT,
        AT_LEAST,
        AT_MOST
    }

    private final Expression items;
    private final Expression enchantments;
    private final Bound bound;
    private final boolean negate;

    private CondIsEnchanted(Expression items, Expression enchantments, Bound bound, boolean negate) {
        this.items = items;
        this.enchantments = enchantments;
        this.bound = bound;
        this.negate = negate;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addCondition(CondIsEnchanted::create,
                "%items% (is|are) enchanted" + WITH,
                "%items% (isn't|is not|aren't|are not) enchanted" + WITH);
    }

    private static Optional<Condition> create(Match match, ParseScope scope) {
        Bound bound = match.has("better") ? Bound.AT_LEAST : match.has("worse") ? Bound.AT_MOST : Bound.EXACT;
        return Optional.of(new CondIsEnchanted(match.slot(0), match.slot(1), bound, match.patternIndex() == 1));
    }

    @Override
    public boolean test(Context context) {
        List<?> found = asList(items.evaluate(context));
        boolean result = !found.isEmpty() && found.stream().allMatch(item -> enchanted(item, context));
        return negate != result;
    }

    private boolean enchanted(Object item, Context context) {
        if (!(item instanceof ItemValue value)) {
            throw new ScriptError("there is no item");
        }
        Map<String, Integer> present = value.details().enchantments();
        if (enchantments == null) {
            return !present.isEmpty();
        }
        List<?> wanted = asList(enchantments.evaluate(context));
        boolean any = enchantments instanceof ListExpression list && list.disjunctive();
        return any
                ? wanted.stream().anyMatch(type -> has(present, type))
                : !wanted.isEmpty() && wanted.stream().allMatch(type -> has(present, type));
    }

    private boolean has(Map<String, Integer> present, Object wanted) {
        if (!(wanted instanceof EnchantmentType type)) {
            return false;
        }
        Integer level = present.get(type.enchantment().id());
        if (level == null) {
            return false;
        }
        if (type.anyLevel()) {
            return true;
        }
        return switch (bound) {
            case EXACT -> level == type.level();
            case AT_LEAST -> level >= type.level();
            case AT_MOST -> level <= type.level();
        };
    }

    private static List<?> asList(Object value) {
        return value instanceof List<?> list ? list : List.of(value);
    }
}
