package com.mineskript.lang.parse;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.Statement;

public record ParsedEffect(Statement statement, ParseError error) {
    public boolean failed() {
        return statement == null;
    }
}
