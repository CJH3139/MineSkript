package com.mineskript.lang.parse;

/**
 * When a syntax element is tried, relative to the others of its kind, like Skript's syntax priorities. A line is
 * matched against the elements with the lowest priority first, and within one priority in the order they were
 * registered, so which module registers an element first never decides between a specific pattern and a catch-all
 * one. Expressions are ordered by their {@link Tier} first and by priority within a tier.
 *
 * <p>Use the three base priorities, or {@link #before} and {@link #after} to place an element just before or after
 * one of them.
 */
public final class Priority implements Comparable<Priority> {
    private static final int STEP = 1000;

    /** The default: a pattern made mostly of fixed words, such as {@code %player% is sneaking}. */
    public static final Priority SIMPLE = new Priority(STEP);
    /**
     * A pattern whose slots can take words another element needs, such as {@code set %objects% to %objects%} or
     * {@code %player% has %blocktype%}: it is tried after every simple pattern.
     */
    public static final Priority COMBINED = new Priority(2 * STEP);
    /**
     * A pattern that starts with a bare slot taking any value, such as {@code %objects% is %objects%}, and so could
     * match nearly any line with the right word in it: it is tried last.
     */
    public static final Priority PATTERN_MATCHES_EVERYTHING = new Priority(3 * STEP);

    private final int order;

    private Priority(int order) {
        this.order = order;
    }

    /** A priority tried just before {@code other} and after everything tried before it. */
    public static Priority before(Priority other) {
        return new Priority(other.order - 1);
    }

    /** A priority tried just after {@code other} and before everything tried after it. */
    public static Priority after(Priority other) {
        return new Priority(other.order + 1);
    }

    @Override
    public int compareTo(Priority other) {
        return Integer.compare(order, other.order);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Priority priority && priority.order == order;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(order);
    }

    @Override
    public String toString() {
        return switch (order) {
            case STEP -> "SIMPLE";
            case 2 * STEP -> "COMBINED";
            case 3 * STEP -> "PATTERN_MATCHES_EVERYTHING";
            default -> "Priority(" + order + ")";
        };
    }
}
