package com.mineskript.common.elements.functions;

import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.function.FunctionInfo;
import com.mineskript.lang.function.FunctionParameter;
import com.mineskript.lang.parse.SyntaxRegistry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

public final class MathFunctions {
    private static final String SINCE = "1.0.0-alpha.10";
    private static final double EPSILON = 1e-10;
    private static final int FACTORIAL_LIMIT = 170;

    private MathFunctions() {
    }

    public static void register(SyntaxRegistry registry) {
        registerRounding(registry);
        registerArithmetic(registry);
        registerTrigonometry(registry);
        registerLists(registry);
        registerCounting(registry);
    }

    private static void registerRounding(SyntaxRegistry registry) {
        unary(registry, "floor", MathFunctions::floor)
                .description("Rounds a number down: the closest whole number that is smaller than or equal to it.")
                .examples(script("floor(2.99)"))
                .since(SINCE);
        registry.addFunction("round", SkType.NUMBER, (arguments, context) -> round(number(arguments, 0),
                        number(arguments, 1)),
                FunctionParameter.of("n", SkType.NUMBER), FunctionParameter.optional("d", SkType.NUMBER, 0.0))
                .description("Rounds a number to the closest whole number, halves going up (round(2.5) is 3).",
                        "The second number sets how many decimal places to keep: round(3.14159, 2) is 3.14. A negative"
                                + " number of places rounds to tens, hundreds and so on: round(1234, -2) is 1200.")
                .examples(script("round(3.14159, 2)"))
                .since(SINCE);
        unary(registry, "ceil", MathFunctions::ceil)
                .description("Rounds a number up: the closest whole number that is larger than or equal to it.")
                .examples(script("ceil(2.01)"))
                .since(SINCE);
        unary(registry, "ceiling", MathFunctions::ceil)
                .description("The same as ceil: rounds a number up.")
                .examples(script("ceiling(2.01)"))
                .since(SINCE);
        unary(registry, "abs", Math::abs)
                .description("The absolute value of a number: the number without its minus sign, so abs(-2) is 2.")
                .examples(script("abs(-2)"))
                .since(SINCE);
    }

    private static void registerArithmetic(SyntaxRegistry registry) {
        binary(registry, "mod", "d", "m", MathFunctions::mod)
                .description("The remainder of dividing d by m, like Skript's mod: the result always has the same sign"
                                + " as m, so mod(-1, 10) is 9 and mod(1, -10) is -9.",
                        "Useful for going round in a circle, such as the next hotbar slot. Dividing by 0 gives NaN"
                                + " (not a number).")
                .examples(script("mod(-1, 10)"))
                .since(SINCE);
        unary(registry, "exp", Math::exp)
                .description("The exponential function: e (about 2.718) to the power of the number.")
                .examples(script("exp(1)"))
                .since(SINCE);
        unary(registry, "ln", Math::log)
                .description("The natural logarithm: the power e must be raised to, to give the number. It is NaN (not"
                        + " a number) for negative numbers.")
                .examples(script("ln(10)"))
                .since(SINCE);
        registry.addFunction("log", SkType.NUMBER,
                        (arguments, context) -> Math.log10(number(arguments, 0)) / Math.log10(number(arguments, 1)),
                        FunctionParameter.of("n", SkType.NUMBER),
                        FunctionParameter.optional("base", SkType.NUMBER, 10.0))
                .description("The logarithm of a number: the power the base must be raised to, to give the number."
                                + " The base is 10 when left out, so log(100) is 2 and log(16, 2) is 4.",
                        "It is NaN (not a number) when either number is negative.")
                .examples(script("log(16, 2)"))
                .since(SINCE);
        unary(registry, "sqrt", Math::sqrt)
                .description("The square root of a number, so sqrt(9) is 3. It is NaN (not a number) for negative"
                        + " numbers.")
                .examples(script("sqrt(9)"))
                .since(SINCE);
        binary(registry, "root", "n", "number", MathFunctions::root)
                .description("The nth root of a number, so root(3, 27) is 3 and root(2, 16) is the same as"
                        + " sqrt(16). The 0th root has no value.")
                .examples(script("root(3, 27)"))
                .since(SINCE);
    }

    private static void registerTrigonometry(SyntaxRegistry registry) {
        unary(registry, "sin", degrees -> Math.sin(Math.toRadians(degrees)))
                .description("The sine of an angle in degrees, so sin(90) is 1.")
                .examples(script("sin(90)"))
                .since(SINCE);
        unary(registry, "cos", degrees -> Math.cos(Math.toRadians(degrees)))
                .description("The cosine of an angle in degrees, so cos(0) is 1.")
                .examples(script("cos(0)"))
                .since(SINCE);
        unary(registry, "tan", degrees -> Math.tan(Math.toRadians(degrees)))
                .description("The tangent of an angle in degrees, so tan(45) is 1.")
                .examples(script("tan(45)"))
                .since(SINCE);
        unary(registry, "asin", value -> Math.toDegrees(Math.asin(value)))
                .description("The inverse of sin: the angle in degrees, from -90 to 90, whose sine is the number.")
                .examples(script("asin(1)"))
                .since(SINCE);
        unary(registry, "acos", value -> Math.toDegrees(Math.acos(value)))
                .description("The inverse of cos: the angle in degrees, from 0 to 180, whose cosine is the number.")
                .examples(script("acos(0)"))
                .since(SINCE);
        unary(registry, "atan", value -> Math.toDegrees(Math.atan(value)))
                .description("The inverse of tan: the angle in degrees, from -90 to 90, whose tangent is the number.")
                .examples(script("atan(1)"))
                .since(SINCE);
        binary(registry, "atan2", "x", "y", (x, y) -> (Object) Math.toDegrees(Math.atan2(y, x)))
                .description("The angle in degrees, from -180 to 180, of the point x, y seen from 0, 0, counted"
                        + " anticlockwise from the x axis, so atan2(0, 10) is 90.")
                .examples(script("atan2(0, 10)"))
                .since(SINCE);
    }

    private static void registerLists(SyntaxRegistry registry) {
        numbers(registry, "sum", "ns", values -> values.stream().mapToDouble(Double::doubleValue).sum())
                .description("Adds up a list of numbers.")
                .examples(script("sum(2, 3, 4)"))
                .since(SINCE);
        numbers(registry, "product", "ns", MathFunctions::product)
                .description("Multiplies a list of numbers together.")
                .examples(script("product(2, 3, 4)"))
                .since(SINCE);
        numbers(registry, "max", "ns", values -> extreme(values, true))
                .description("The largest number of a list, such as max(1, 5, 3) or max({scores::*}).")
                .examples(script("max(1, 5, 3)"))
                .since(SINCE);
        numbers(registry, "min", "ns", values -> extreme(values, false))
                .description("The smallest number of a list, such as min(1, 5, 3) or min({scores::*}).")
                .examples(script("min(1, 5, 3)"))
                .since(SINCE);
        registry.addFunction("clamp", SkType.NUMBER, (arguments, context) -> clamp(arguments),
                        FunctionParameter.list("values", SkType.NUMBER), FunctionParameter.of("min", SkType.NUMBER),
                        FunctionParameter.of("max", SkType.NUMBER))
                .returnsList()
                .description("Keeps a number, or each number of a list, between a smallest and a largest value:"
                                + " clamp(15, 0, 10) is 10 and clamp(-3, 0, 10) is 0. If min is larger than max they"
                                + " are swapped.",
                        "Given a list, such as clamp({values::*}, 0, 10) or clamp((5, 20, -1), 0, 10), it gives a"
                                + " list.")
                .examples(script("clamp(15, 0, 10)"))
                .since(SINCE);
        numbers(registry, "mean", "numbers", MathFunctions::mean)
                .description("The mean (average) of a list of numbers. A list with an infinite or NaN (not a number)"
                        + " value has no mean.")
                .examples(script("mean(1, 2, 6)"))
                .since(SINCE);
        numbers(registry, "median", "numbers", MathFunctions::median)
                .description("The middle value of a list of numbers once sorted. With an even count it is the mean of"
                        + " the two middle values. A list with a NaN (not a number) value has no median.")
                .examples(script("median(1, 2, 3, 4)"))
                .since(SINCE);
        unary(registry, "isNaN", "n", SkType.BOOLEAN, value -> Double.isNaN(value))
                .description("Whether a number is NaN (not a number), the result of sums such as 0 / 0 or"
                        + " sqrt(-1).")
                .examples("on load:",
                        "\tif isNaN(sqrt(-1)):",
                        "\t\tsend \"not a number\"")
                .since(SINCE);
    }

    private static void registerCounting(SyntaxRegistry registry) {
        unary(registry, "factorial", "number", SkType.NUMBER, MathFunctions::factorial)
                .description("The factorial of a number: 5 x 4 x 3 x 2 x 1 for factorial(5). A negative number has no"
                        + " factorial, above 21 the result is only approximate, and above 170 it is infinity.")
                .examples(script("factorial(5)"))
                .since(SINCE);
        binary(registry, "permutations", "options", "selected", MathFunctions::permutations)
                .description("How many ordered ways there are to pick a number of things (selected) out of a"
                        + " number of options, so permutations(3, 2) is 6. When selected is more than options or"
                        + " negative there is no result.")
                .examples(script("permutations(10, 2)"))
                .since(SINCE);
        binary(registry, "combinations", "options", "selected", MathFunctions::combinations)
                .description("How many different groups of a number of things (selected) can be picked out of a"
                        + " number of options when order does not matter, so combinations(3, 2) is 3. When selected"
                        + " is more than options or negative there is no result.")
                .examples(script("combinations(5, 3)"))
                .since(SINCE);
        unary(registry, "calcExperience", "level", SkType.NUMBER, MathFunctions::experience)
                .description("The total experience points needed to reach a level from nothing, as Minecraft counts"
                        + " them. Decimal levels are cut down to a whole level first.")
                .examples("on key press of \"x\":",
                        "\tsend \"level 30 takes %calcExperience(30)% points\"")
                .since(SINCE);
    }

    private static String[] script(String call) {
        return new String[] {"on load:", "\tsend \"" + call + " is %" + call + "%\""};
    }

    private static FunctionInfo unary(SyntaxRegistry registry, String name, DoubleUnaryOperator operation) {
        return unary(registry, name, "n", SkType.NUMBER, operation::applyAsDouble);
    }

    private static FunctionInfo unary(SyntaxRegistry registry, String name, String parameter, SkType returnType,
            DoubleFunction<Object> operation) {
        return registry.addFunction(name, returnType,
                (arguments, context) -> operation.apply(number(arguments, 0)),
                FunctionParameter.of(parameter, SkType.NUMBER));
    }

    private static FunctionInfo binary(SyntaxRegistry registry, String name, String first, String second,
            BiFunction<Double, Double, Object> operation) {
        return registry.addFunction(name, SkType.NUMBER,
                (arguments, context) -> operation.apply(number(arguments, 0), number(arguments, 1)),
                FunctionParameter.of(first, SkType.NUMBER), FunctionParameter.of(second, SkType.NUMBER));
    }

    private static FunctionInfo numbers(SyntaxRegistry registry, String name, String parameter,
            Function<List<Double>, Object> operation) {
        return registry.addFunction(name, SkType.NUMBER,
                (arguments, context) -> operation.apply(doubles(arguments.get(0))),
                FunctionParameter.list(parameter, SkType.NUMBER));
    }

    private static double number(List<Object> arguments, int index) {
        return (Double) arguments.get(index);
    }

    private static List<Double> doubles(Object list) {
        List<Double> values = new ArrayList<>();
        for (Object value : (List<?>) list) {
            values.add((Double) value);
        }
        return values;
    }

    static double floor(double value) {
        return Double.isFinite(value) ? Math.floor(value + EPSILON) : value;
    }

    static double ceil(double value) {
        return Double.isFinite(value) ? Math.ceil(value - EPSILON) : value;
    }

    static double round(double value, double places) {
        if (!Double.isFinite(value)) {
            return value;
        }
        if (!Double.isFinite(places) || places >= Integer.MAX_VALUE || places <= Integer.MIN_VALUE) {
            return Double.NaN;
        }
        int digits = (int) places;
        if (digits == 0) {
            return Math.round(value + EPSILON);
        }
        if (digits > 0) {
            return new BigDecimal(Double.toString(value)).setScale(digits, RoundingMode.HALF_UP).doubleValue();
        }
        double factor = Math.pow(10.0, digits);
        return Math.round(Math.round(value + EPSILON) * factor) / factor;
    }

    static Object mod(double value, double divisor) {
        if (divisor == 0) {
            return Double.NaN;
        }
        return (value % divisor + divisor) % divisor;
    }

    private static Object root(double n, double value) {
        if (n == 0) {
            return None.NONE;
        }
        if (n == 1) {
            return value;
        }
        return n == 2 ? Math.sqrt(value) : Math.pow(value, 1 / n);
    }

    private static double product(List<Double> values) {
        double product = 1;
        for (double value : values) {
            product *= value;
        }
        return product;
    }

    private static double extreme(List<Double> values, boolean largest) {
        double result = values.getFirst();
        for (double value : values) {
            if (Double.isNaN(result) || (largest ? value > result : value < result)) {
                result = value;
            }
        }
        return result;
    }

    private static List<Object> clamp(List<Object> arguments) {
        double low = Math.min(number(arguments, 1), number(arguments, 2));
        double high = Math.max(number(arguments, 1), number(arguments, 2));
        List<Object> clamped = new ArrayList<>();
        for (double value : doubles(arguments.get(0))) {
            clamped.add(Math.max(Math.min(value, high), low));
        }
        return clamped;
    }

    private static Object mean(List<Double> values) {
        double total = 0;
        for (double value : values) {
            if (!Double.isFinite(value) || !Double.isFinite(total)) {
                return None.NONE;
            }
            total += value / values.size();
        }
        return total;
    }

    private static Object median(List<Double> values) {
        if (values.stream().anyMatch(value -> Double.isNaN(value))) {
            return None.NONE;
        }
        List<Double> sorted = values.stream().sorted().toList();
        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        }
        return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2;
    }

    private static Object factorial(double number) {
        if (number < 0) {
            return None.NONE;
        }
        if (number <= 1) {
            return 1.0;
        }
        if (number > FACTORIAL_LIMIT) {
            return Double.POSITIVE_INFINITY;
        }
        double result = 1;
        for (double i = number; i > 1 && Double.isFinite(result); i--) {
            result *= i;
        }
        return result;
    }

    private static Object permutations(double options, double selected) {
        if (selected > options || selected < 0) {
            return None.NONE;
        }
        return arrangements(options, selected);
    }

    private static double arrangements(double options, double selected) {
        double result = 1;
        for (double i = options; i > options - selected && Double.isFinite(result); i--) {
            result *= i;
        }
        return result;
    }

    private static Object combinations(double options, double selected) {
        if (selected > options || selected < 0) {
            return None.NONE;
        }
        if (selected == 0) {
            return 1.0;
        }
        double top = arrangements(options, selected);
        double bottom = selected;
        for (double i = selected - 1; i > 1 && Double.isFinite(bottom); i--) {
            bottom *= i;
        }
        return top / bottom;
    }

    private static Object experience(double value) {
        long level = (long) value;
        if (level <= 0) {
            return 0.0;
        }
        if (level <= 15) {
            return (double) (level * level + 6 * level);
        }
        if (level <= 30) {
            return (double) (long) (2.5 * level * level - 40.5 * level + 360);
        }
        return (double) (long) (4.5 * level * level - 162.5 * level + 2220);
    }
}
