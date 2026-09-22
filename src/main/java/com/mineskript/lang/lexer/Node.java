package com.mineskript.lang.lexer;

import java.util.List;

public record Node(String text, int line, boolean section, List<Node> children) {
}
