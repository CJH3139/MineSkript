package com.mineskript.scripttest;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.NoDoc;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.Converters;
import java.util.Optional;

@Name("Assert")
@Description({"Fails the current script test when the condition does not pass, with the given message or a default one.",
        "Only exists in MineSkript's own tests; the shipped mod does not have it."})
@Examples({"test \"set\":",
        "\tset {x} to 5",
        "\tassert {x} is 5 with \"set did not store 5\""})
@Since("1.0.0-alpha.8")
@NoDoc
public final class EffAssert implements Statement {
    static final String DEFAULT_MESSAGE = "assertion failed";

    private final int line;
    private final Expression condition;
    private final Expression message;
    private final AssertionLog log;

    private EffAssert(int line, Expression condition, Expression message, AssertionLog log) {
        this.line = line;
        this.condition = condition;
        this.message = message;
        this.log = log;
    }

    public static void register(SyntaxRegistry registry, AssertionLog log) {
        registry.addEffect((match, scope) -> Optional.of(new EffAssert(scope.line(), match.slot(0),
                        match.patternIndex() == 0 ? match.slot(1) : null, log)),
                "assert %condition% with %string%",
                "assert %condition%");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        if (!Boolean.TRUE.equals(condition.evaluate(context))) {
            String text = message == null ? DEFAULT_MESSAGE : Converters.toText(message.evaluate(context), context);
            log.fail(context.file(), line, text);
        }
        return Flow.CONTINUE;
    }
}
