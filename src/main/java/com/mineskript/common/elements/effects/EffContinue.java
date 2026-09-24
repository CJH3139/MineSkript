package com.mineskript.common.elements.effects;

import com.mineskript.doc.Description;
import com.mineskript.doc.Examples;
import com.mineskript.doc.Name;
import com.mineskript.doc.Since;
import com.mineskript.lang.ast.Flow;
import com.mineskript.lang.ast.Statement;
import com.mineskript.lang.parse.SyntaxException;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Context;
import java.util.Optional;

@Name("Continue")
@Description({"Skips the rest of the current pass of the innermost loop and moves on to the next one. Works in loop and while sections.",
        "Only allowed inside a loop: anywhere else it is an error when the script loads."})
@Examples({"on load:",
        "	loop 5 times:",
        "		if loop-value is 3:",
        "			continue",
        "		send \"%loop-value%\""})
@Since("1.0.0-alpha.2")
public final class EffContinue implements Statement {
    private final int line;

    private EffContinue(int line) {
        this.line = line;
    }

    public static void register(SyntaxRegistry registry) {
        registry.addEffect((match, scope) -> {
            if (!scope.inLoop()) {
                throw new SyntaxException("continue is only available inside a loop");
            }
            return Optional.of(new EffContinue(scope.line()));
        }, "continue [(this loop|[the] [current] loop)]");
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public Flow execute(Context context) {
        return Flow.NEXT_ITERATION;
    }
}
