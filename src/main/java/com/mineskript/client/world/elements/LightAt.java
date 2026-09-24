package com.mineskript.client.world.elements;

import com.mineskript.client.Locations;
import com.mineskript.lang.ast.Expression;
import com.mineskript.lang.ast.Location;
import com.mineskript.lang.ast.None;
import com.mineskript.lang.runtime.Context;

final class LightAt {
    private LightAt() {
    }

    static Object read(Expression location, Context context, boolean sky) {
        Location at = Locations.read(location, context);
        if (!Locations.isHere(at, context)) {
            return None.NONE;
        }
        return (double) context.world().lightAt(at.x(), at.y(), at.z(), sky);
    }

    static Object readCombined(Expression location, Context context) {
        Location at = Locations.read(location, context);
        if (!Locations.isHere(at, context)) {
            return None.NONE;
        }
        return (double) context.world().combinedLightAt(at.x(), at.y(), at.z());
    }
}
