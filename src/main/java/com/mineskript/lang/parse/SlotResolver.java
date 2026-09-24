package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import java.util.List;
import java.util.Optional;

public interface SlotResolver {
    Optional<Expression> resolve(List<Token> tokens, List<SkType> types, ParseScope scope);

    /**
     * Parses a whole condition for a {@code %condition%} slot, as a boolean expression that tests it. A resolver that
     * cannot parse conditions finds none.
     */
    default Optional<Expression> resolveCondition(List<Token> tokens, ParseScope scope) {
        return Optional.empty();
    }
}
