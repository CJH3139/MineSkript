package com.mineskript.lang.parse;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.Function;
import com.mineskript.lang.ast.Trigger;
import java.util.List;

public record ParsedScript(String file, List<Trigger> triggers, List<ParseError> errors, List<Function> functions) {
    public ParsedScript(String file, List<Trigger> triggers, List<ParseError> errors) {
        this(file, triggers, errors, List.of());
    }
}
