package com.mineskript.lang.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mineskript.ScriptRunner;
import com.mineskript.api.MineSkriptAddon;
import com.mineskript.doc.JSONGenerator;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.Parser;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.syntax.DefaultSyntax;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuiltInFunctionsTest {
    private static final double DELTA = 1e-9;

    private final ScriptRunner runner = new ScriptRunner();

    private Object value(String expression) {
        runner.run("on load:\n    set {r} to " + expression + "\n");
        assertEquals(List.of(), runner.game.errors, expression);
        return runner.global("r");
    }

    private double number(String expression) {
        return (Double) value(expression);
    }

    private String text(String expression) {
        runner.game.messages.clear();
        runner.run("on load:\n    send \"%" + expression + "%\"\n");
        return runner.game.messages.getLast();
    }

    @Test
    void floorCeilAbsAndSqrt() {
        assertEquals(2.0, number("floor(2.99)"));
        assertEquals(-3.0, number("floor(-2.1)"));
        assertEquals(3.0, number("floor(2.99999999999999)"));
        assertEquals(3.0, number("ceil(2.01)"));
        assertEquals(2.0, number("ceiling(2)"));
        assertEquals(-2.0, number("ceil(-2.5)"));
        assertEquals(2.0, number("abs(-2)"));
        assertEquals(3.0, number("sqrt(9)"));
        assertTrue(Double.isNaN(number("sqrt(-1)")));
    }

    @Test
    void theFunctionWinsOverTheEnglishFloorExpression() {
        assertEquals(2.0, number("floor 2.99999999999999"));
        assertEquals(3.0, number("floor(2.99999999999999)"));
        assertEquals(3.14, number("round(3.14159, 2)"));
        assertEquals(3.0, number("round 3.14159"));
    }

    @Test
    void roundToPlacesLikeSkript() {
        assertEquals(3.0, number("round(2.5)"));
        assertEquals(-2.0, number("round(-2.5)"));
        assertEquals(2.0, number("round(2)"));
        assertEquals(3.14, number("round(3.14159, 2)"));
        assertEquals(2.68, number("round(2.675, 2)"));
        assertEquals(1200.0, number("round(1234, -2)"));
        assertEquals(1234.6, number("round(1234.56, 1.9)"));
    }

    @Test
    void modAlwaysHasTheSignOfTheDivisor() {
        assertEquals(1.0, number("mod(3, 2)"));
        assertEquals(36.0, number("mod(256436, 100)"));
        assertEquals(9.0, number("mod(-1, 10)"));
        assertEquals(-9.0, number("mod(1, -10)"));
        assertEquals(-1.0, number("mod(-7, -3)"));
        assertEquals(0.5, number("mod(-2.5, 3)"));
        assertTrue(Double.isNaN(number("mod(5, 0)")));
    }

    @Test
    void exponentsAndLogarithms() {
        assertEquals(1.0, number("exp(0)"));
        assertEquals(Math.E, number("exp(1)"), DELTA);
        assertEquals(0.0, number("ln(1)"));
        assertEquals(2.0, number("log(100)"), DELTA);
        assertEquals(4.0, number("log(16, 2)"), DELTA);
        assertEquals(3.0, number("root(3, 27)"), DELTA);
        assertEquals(4.0, number("root(2, 16)"));
        assertEquals(5.0, number("root(1, 5)"));
        assertEquals(0.5, number("root(-4, 16)"), DELTA);
        assertEquals(None.NONE, value("root(0, 5)"));
    }

    @Test
    void trigonometryUsesDegrees() {
        assertEquals(1.0, number("sin(90)"), DELTA);
        assertEquals(1.0, number("cos(0)"), DELTA);
        assertEquals(1.0, number("tan(45)"), DELTA);
        assertEquals(90.0, number("asin(1)"), DELTA);
        assertEquals(90.0, number("acos(0)"), DELTA);
        assertEquals(45.0, number("atan(1)"), DELTA);
        assertEquals(90.0, number("atan2(0, 10)"), DELTA);
        assertEquals(0.0, number("atan2(10, 0)"), DELTA);
        assertEquals(-45.0, number("atan2(5, -5)"), DELTA);
    }

    @Test
    void minMaxSumAndProductTakeValuesAndLists() {
        assertEquals(9.0, number("sum(2, 3, 4)"));
        assertEquals(24.0, number("product(2, 3, 4)"));
        assertEquals(5.0, number("max(1, 5, 3)"));
        assertEquals(1.0, number("min(1, 5, 3)"));
        assertEquals(7.0, number("max(7)"));
        runner.run("on load:\n    set {l::*} to 4, 9 and -2\n    set {max} to max({l::*})\n"
                + "    set {min} to min(3, {l::*}, 1)\n    set {sum} to sum({l::*}, 10)\n"
                + "    set {paren} to max((1, 8, 2))\n");
        assertEquals(9.0, runner.global("max"));
        assertEquals(-2.0, runner.global("min"));
        assertEquals(21.0, runner.global("sum"));
        assertEquals(8.0, runner.global("paren"));
    }

    @Test
    void anEmptyListOrAnUnsetValueGivesNone() {
        assertEquals(None.NONE, value("max({empty::*})"));
        assertEquals(None.NONE, value("round({unset})"));
        assertEquals(1.0, number("min({empty::*}, 1)"));
    }

    @Test
    void clampKeepsValuesInRangeAndGivesAListForAList() {
        assertEquals(10.0, number("clamp(15, 0, 10)"));
        assertEquals(0.0, number("clamp(-3, 10, 0)"));
        assertEquals(5.0, number("clamp(5, 0, 10)"));
        assertEquals("5, 10 and 0", text("clamp((5, 20, -1), 0, 10)"));
        runner.run("on load:\n    set {v::*} to 1, 50 and 7\n    set {c::*} to clamp({v::*}, 2, 10)\n"
                + "    set {n} to size of {c::*}\n");
        assertEquals(3.0, runner.global("n"));
        assertEquals(List.of("2, 10 and 7"), List.of(text("clamp({v::*}, 2, 10)")));
    }

    @Test
    void meanMedianAndIsNaN() {
        assertEquals(3.0, number("mean(1, 2, 6)"));
        assertEquals(298.75, number("mean(13, 97, 376, 709)"));
        assertEquals(2.5, number("median(4, 1, 3, 2)"));
        assertEquals(2.0, number("median(3, 1, 2)"));
        assertEquals(None.NONE, value("median(1, sqrt(-1))"));
        assertEquals(None.NONE, value("mean(1, sqrt(-1))"));
        assertEquals(Boolean.TRUE, value("isNaN(sqrt(-1))"));
        assertEquals(Boolean.FALSE, value("isnan(0)"));
    }

    @Test
    void factorialPermutationsAndCombinations() {
        assertEquals(120.0, number("factorial(5)"));
        assertEquals(1.0, number("factorial(0)"));
        assertEquals(None.NONE, value("factorial(-1)"));
        assertEquals(Double.POSITIVE_INFINITY, number("factorial(171)"));
        assertEquals(90.0, number("permutations(10, 2)"));
        assertEquals(5040.0, number("permutations(10, 4)"));
        assertEquals(1.0, number("permutations(10, 0)"));
        assertEquals(None.NONE, value("permutations(2, 3)"));
        assertEquals(45.0, number("combinations(10, 8)"));
        assertEquals(10.0, number("combinations(5, 3)"));
        assertEquals(1.0, number("combinations(5, 0)"));
        assertEquals(None.NONE, value("combinations(5, -1)"));
    }

    @Test
    void experienceForALevel() {
        assertEquals(0.0, number("calcExperience(0)"));
        assertEquals(160.0, number("calcExperience(10)"));
        assertEquals(352.0, number("calcExperience(16)"));
        assertEquals(1395.0, number("calcExperience(30)"));
        assertEquals(1507.0, number("calcExperience(31)"));
        assertEquals(160.0, number("calcexperience(10.9)"));
    }

    @Test
    void concatAndFormatNumber() {
        assertEquals("level 5!", value("concat(\"level \", 5, \"!\")"));
        assertEquals("ab", value("concat(\"a\", \"b\")"));
        assertEquals("1,234,567", value("formatNumber(1234567)"));
        assertEquals("1,234.57", value("formatNumber(1234.567)"));
        assertEquals("3.1", value("formatNumber(3.14159, \"0.0\")"));
        assertEquals("007", value("formatNumber(7, \"000\")"));
        assertEquals(None.NONE, value("formatNumber(1, \"0.0.0\")"));
    }

    @Test
    void aBuiltInFunctionCanBeCalledOnItsOwnLine() {
        assertEquals(List.of(), runner.errorsOf("on load:\n    round(1)\n    location(1, 2, 3)\n"));
        runner.run("on load:\n    round(1)\n    send \"after\"\n");
        assertEquals(List.of("after"), runner.game.messages);
    }

    @Test
    void aScriptCannotDefineAFunctionWithABuiltInName() {
        assertEquals(List.of("t.ms:1: \"round\" is the name of a built-in function, give your function another name"),
                runner.errorsOf("function round(n: number) :: number:\n    return 1\n"));
        assertEquals(List.of("t.ms:1: \"isnan\" is the name of a built-in function, give your function another name"),
                runner.errorsOf("local function isNaN(n: number) :: boolean:\n    return false\n"));
        assertEquals(List.of("t.ms:1: \"location\" is the name of a built-in function, give your function another"
                + " name"), runner.errorsOf("function location(a: text):\n    send {_a}\n"));
        assertEquals(List.of(), runner.errorsOf("function rounded(n: number) :: number:\n    return round({_n})\n"));
    }

    @Test
    void wrongArgumentsAreParseErrors() {
        assertEquals(List.of("t.ms:2: function \"round\" takes 1 to 2 arguments, not 0"),
                runner.errorsOf("on load:\n    set {x} to round()\n"));
        assertEquals(List.of("t.ms:2: function \"mod\" takes 2 arguments, not 1"),
                runner.errorsOf("on load:\n    set {x} to mod(1)\n"));
        assertEquals(List.of("t.ms:2: function \"sqrt\" takes 1 argument, not 2"),
                runner.errorsOf("on load:\n    set {x} to sqrt(1, 2)\n"));
        assertEquals(List.of("t.ms:2: argument 1 of \"round\" should be number"),
                runner.errorsOf("on load:\n    set {x} to round(\"a\")\n"));
        assertEquals(List.of("t.ms:2: argument 1 of \"round\" should be one number, not a list"),
                runner.errorsOf("on load:\n    set {x} to round((1, 2))\n"));
        assertEquals(List.of("t.ms:2: argument 1 of \"max\" should be number"),
                runner.errorsOf("on load:\n    set {x} to max(1, \"a\")\n"));
    }

    @Test
    void theRegistryFindsFunctionsIgnoringCaseAndRefusesBadOnes() {
        SyntaxRegistry registry = DefaultSyntax.registry();
        assertEquals("isNaN", registry.function("ISNAN").orElseThrow().name());
        assertTrue(registry.function("frobnicate").isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> registry.addFunction("Round", SkType.NUMBER, (arguments, context) -> 1.0));
        assertThrows(IllegalArgumentException.class,
                () -> registry.addFunction("two words", SkType.NUMBER, (arguments, context) -> 1.0));
        assertThrows(IllegalArgumentException.class, () -> registry.addFunction("gap", SkType.NUMBER,
                (arguments, context) -> 1.0, FunctionParameter.optional("a", SkType.NUMBER, 1.0),
                FunctionParameter.of("b", SkType.NUMBER)));
    }

    @Test
    void signaturesShowTypesDefaultsAndLists() {
        SyntaxRegistry registry = DefaultSyntax.registry();
        assertEquals("round(n: number, d: number = 0) :: number", registry.function("round").orElseThrow().signature());
        assertEquals("max(ns: numbers) :: number", registry.function("max").orElseThrow().signature());
        assertEquals("clamp(values: numbers, min: number, max: number) :: numbers",
                registry.function("clamp").orElseThrow().signature());
        assertEquals("formatNumber(number: number, format: text = \"\") :: text",
                registry.function("formatnumber").orElseThrow().signature());
        assertEquals("concat(texts: objects) :: text", registry.function("concat").orElseThrow().signature());
        assertEquals("location(x: number, y: number, z: number, dimension: text = the dimension you are in)"
                + " :: location", registry.function("location").orElseThrow().signature());
    }

    @Test
    void anAddonCanAddAFunction() {
        MineSkriptAddon addon = new MineSkriptAddon() {
            @Override
            public String name() {
                return "Dice";
            }

            @Override
            public void register(SyntaxRegistry registry) {
                registry.addFunction("twice", SkType.NUMBER, (arguments, context) -> 2 * (Double) arguments.get(0),
                                FunctionParameter.of("n", SkType.NUMBER))
                        .description("Doubles a number.")
                        .examples("on load:", "\tsend \"%twice(2)%\"")
                        .since("1.0");
            }
        };
        SyntaxRegistry registry = com.mineskript.api.AddonLoader.load(DefaultSyntax::registry, List.of(addon))
                .registry();
        assertTrue(registry.function("twice").isPresent());
        assertEquals(List.of(), new Parser(registry).parse("t.ms", "on load:\n    send \"%twice(4)%\"\n").errors());
        JsonObject docs = JSONGenerator.generate("test", List.of(addon));
        JsonObject twice = function(docs, "twice");
        assertEquals("Dice", twice.get("addon").getAsString());
        assertEquals("Dice", twice.get("module").getAsString());
    }

    @Test
    void theDocumentationListsEveryFunction() {
        JsonObject docs = JSONGenerator.generate("test");
        List<String> names = new ArrayList<>();
        for (JsonElement element : docs.getAsJsonArray("functions")) {
            names.add(element.getAsJsonObject().get("name").getAsString());
        }
        assertEquals(List.of("floor", "round", "ceil", "ceiling", "abs", "mod", "exp", "ln", "log", "sqrt", "root",
                "sin", "cos", "tan", "asin", "acos", "atan", "atan2", "sum", "product", "max", "min", "clamp", "mean",
                "median", "isNaN", "factorial", "permutations", "combinations", "calcExperience", "concat",
                "formatNumber", "location"), names);
        JsonObject round = function(docs, "round");
        assertEquals("function-round", round.get("id").getAsString());
        assertEquals("round(n: number, d: number = 0) :: number", round.get("signature").getAsString());
        assertEquals("number", round.get("returnType").getAsString());
        assertFalse(round.get("returnsList").getAsBoolean());
        JsonObject places = round.getAsJsonArray("parameters").get(1).getAsJsonObject();
        assertEquals("d", places.get("name").getAsString());
        assertEquals("number", places.get("type").getAsString());
        assertEquals("0", places.get("default").getAsString());
        assertFalse(round.getAsJsonArray("parameters").get(0).getAsJsonObject().has("default"));
        assertEquals("common", round.get("module").getAsString());
        assertEquals("MineSkript", round.get("addon").getAsString());
        assertEquals(List.of("1.0.0-alpha.10"), List.of(round.getAsJsonArray("since").get(0).getAsString()));
        assertEquals("client/world", function(docs, "location").get("module").getAsString());
        assertTrue(function(docs, "max").getAsJsonArray("parameters").get(0).getAsJsonObject().get("list")
                .getAsBoolean());
        assertTrue(function(docs, "clamp").get("returnsList").getAsBoolean());
    }

    private static JsonObject function(JsonObject docs, String name) {
        for (JsonElement element : docs.getAsJsonArray("functions")) {
            if (element.getAsJsonObject().get("name").getAsString().equals(name)) {
                return element.getAsJsonObject();
            }
        }
        throw new AssertionError("no function " + name);
    }
}
