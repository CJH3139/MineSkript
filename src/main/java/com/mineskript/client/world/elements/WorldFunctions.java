package com.mineskript.client.world.elements;

import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.function.FunctionParameter;
import com.mineskript.lang.parse.SyntaxRegistry;
import com.mineskript.lang.runtime.Converters;
import java.util.List;

public final class WorldFunctions {
    private WorldFunctions() {
    }

    public static void register(SyntaxRegistry registry) {
        registry.addFunction("location", SkType.LOCATION, (arguments, context) -> location(arguments),
                        FunctionParameter.of("x", SkType.NUMBER),
                        FunctionParameter.of("y", SkType.NUMBER),
                        FunctionParameter.of("z", SkType.NUMBER),
                        FunctionParameter.optional("dimension", SkType.TEXT, "the dimension you are in",
                                Converters::currentDimension))
                .description("Makes a location from x, y and z coordinates and a dimension. Leave the dimension out"
                                + " to use the one you are in (or minecraft:overworld when you are not in a world).",
                        "The dimension is an id such as \"minecraft:the_nether\"; a name without minecraft: in front,"
                                + " such as \"the_end\", gets it added. Unlike Skript's location function there is"
                                + " no yaw and pitch, because MineSkript locations are only a position.")
                .examples("on key press of \"l\":",
                        "	look at location(0.5, 64.5, 0.5)",
                        "",
                        "on key press of \"h\":",
                        "	set {nether portal} to location(10, 70, -4, \"minecraft:the_nether\")",
                        "	send \"saved %{nether portal}%\"")
                .since("1.0.0-alpha.10");
    }

    private static Location location(List<Object> arguments) {
        return new Location((Double) arguments.get(0), (Double) arguments.get(1), (Double) arguments.get(2),
                (String) arguments.get(3));
    }
}
