package com.mineskript.common.elements.expressions;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Match;
import com.mineskript.lang.parse.ParseScope;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.parse.Tier;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import com.mineskript.lang.runtime.TextMatching;
import java.util.Optional;

@Name("Join & Split")
@Description({
        "Joins several texts into one with a separator between them, or splits a text into a list of pieces at every occurrence of a separator, like Skript's join and split.",
        "join X with Y (also concat, concatenate, and using or by instead of with) puts Y between the texts; without a separator they are joined with nothing in between. This is not the concat(...) function, which is written with brackets.",
        "split X at Y, also written X split at Y, with at, using, by or on, matches the separator literally and ignores capitals, like Skript: add with case sensitivity to only split where the capitals match too. Empty pieces are kept, so a,,b gives three pieces; add without trailing text to drop the empty pieces at the end. An empty separator splits into single characters, and empty text gives an empty list.",
        "The result of split is a list, so use it with loop, contains or is in, or keep its pieces in a list variable with set {_pieces::*} to X split at Y, which stores them under the indices 1, 2, 3 and so on."
})
@Examples({
        "on key press of \"l\":",
        "	loop \"a,b,c\" split at \",\":",
        "		send loop-value",
        "",
        "on chat:",
        "	loop split message by \" \":",
        "		if loop-value is \"hello\":",
        "			send \"someone said hello\"",
        "",
        "on key press of \"j\":",
        "	set {_names::*} to \"Alex\", \"Steve\" and \"Notch\"",
        "	send join {_names::*} with \" | \"",
        "",
        "on key press of \"k\":",
        "	set {_parts::*} to split \"aXbxc\" at \"x\" with case sensitivity",
        "	send \"%size of {_parts::*}% pieces\""
})
@Since({"1.0.0-alpha.4", "1.0.0-alpha.11"})
public final class ExprJoinSplit implements Expression {
    private static final String CASE = "[case:with case sensitivity]";
    private static final String TRAILING = "[trailing:without [the] trailing [empty] (string|text)]";

    private final boolean join;
    private final Expression text;
    private final Expression delimiter;
    private final boolean caseSensitive;
    private final boolean keepTrailing;

    private ExprJoinSplit(boolean join, Expression text, Expression delimiter, boolean caseSensitive,
            boolean keepTrailing) {
        this.join = join;
        this.text = text;
        this.delimiter = delimiter;
        this.caseSensitive = caseSensitive;
        this.keepTrailing = keepTrailing;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addExpression(SkType.TEXT, Tier.COMBINED, ExprJoinSplit::create,
                "(concat|concatenate|join) %strings% [(with|using|by) [[the] delimiter] %-string%]",
                "split %string% (at|using|by|on) [[the] delimiter] %string% " + CASE + " " + TRAILING,
                "%string% split (at|using|by|on) [[the] delimiter] %string% " + CASE + " " + TRAILING);
    }

    private static Optional<Expression> create(Match match, ParseScope scope) {
        boolean join = match.patternIndex() == 0;
        Expression delimiter = match.slot(1);
        if ((!join && match.slot(0).isList()) || (delimiter != null && delimiter.isList())) {
            return Optional.empty();
        }
        return Optional.of(new ExprJoinSplit(join, match.slot(0), delimiter, match.has("case"),
                !match.has("trailing")));
    }

    @Override
    public SkType type() {
        return SkType.TEXT;
    }

    @Override
    public boolean isList() {
        return !join;
    }

    @Override
    public Object evaluate(Context context) {
        String separator = delimiter == null ? "" : Converters.toText(delimiter.evaluate(context), context);
        if (join) {
            return String.join(separator, TextHelper.texts(text.evaluate(context), context));
        }
        String whole = Converters.toText(text.evaluate(context), context);
        return TextMatching.split(whole, separator, caseSensitive, keepTrailing);
    }
}
