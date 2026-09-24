package com.mineskript.client;

import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.runtime.Context;
import com.mineskript.lang.runtime.ScriptError;

public final class Locations {
    private Locations() {
    }

    public static Location read(Expression expression, Context context) {
        if (!(expression.evaluate(context) instanceof Location location)) {
            throw new ScriptError("there is no location");
        }
        return location;
    }

    public static boolean isHere(Location location, Context context) {
        return location.dimension().equals(context.world().dimension());
    }

    public static Location here(Expression expression, Context context) {
        Location location = read(expression, context);
        String current = context.world().dimension();
        if (!location.dimension().equals(current)) {
            throw new ScriptError("that location is in " + location.dimension() + ", but you are in " + current);
        }
        return location;
    }
}
