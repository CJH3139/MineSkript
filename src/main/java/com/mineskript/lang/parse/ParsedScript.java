package com.mineskript.lang.parse;

import com.mineskript.lang.ParseError;
import com.mineskript.lang.ast.Trigger;
import java.util.List;

public record ParsedScript(String file, List<Trigger> triggers, List<ParseError> errors) {
}
