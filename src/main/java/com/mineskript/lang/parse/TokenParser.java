package com.mineskript.lang.parse;

import java.util.List;
import java.util.Optional;

/**
 * Reads a whole run of tokens itself, for syntax that a pattern cannot describe. Register one with
 * {@link SyntaxRegistry#addRawExpression} or {@link SyntaxRegistry#addRawEffect}. Raw parsers are tried after every
 * pattern, so they only see lines and values nothing else understood.
 *
 * @param <T> what it makes: an expression or a statement
 */
@FunctionalInterface
public interface TokenParser<T> {
    /**
     * Returns what the tokens mean, or empty when they are not this syntax. Throwing {@link SyntaxException} makes
     * the line a parse error with that message. {@code resolver} parses parts of the tokens as ordinary values.
     */
    Optional<T> parse(List<Token> tokens, SlotResolver resolver, ParseScope scope);
}
