package com.mineskript.lang.parse;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.SkType;
import java.util.List;

public record ExpressionEntry(List<Pattern> patterns, SkType returnType, Tier tier, SyntaxFactory<Expression> factory,
        Priority priority) {
}
