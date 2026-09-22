package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import java.util.List;
import java.util.Optional;

public interface SlotResolver {
    Optional<Expression> resolve(List<Token> tokens, List<SkType> types, ParseScope scope);
}
