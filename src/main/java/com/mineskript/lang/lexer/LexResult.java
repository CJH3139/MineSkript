package com.mineskript.lang.lexer;

import com.mineskript.lang.ParseError;
import java.util.List;

public record LexResult(List<Node> nodes, List<ParseError> errors) {
}
