package com.mineskript.lang.parse;

import java.util.List;

public record SyntaxEntry<T>(List<Pattern> patterns, SyntaxFactory<T> factory) {
}
