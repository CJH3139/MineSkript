package com.mineskript.lang.parse;

import java.util.Optional;

@FunctionalInterface
public interface SyntaxFactory<T> {
    Optional<T> create(Match match, ParseScope scope);
}
