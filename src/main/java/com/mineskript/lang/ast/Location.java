package com.mineskript.lang.ast;

import java.util.Locale;

public record Location(double x, double y, double z, String dimension) {
    public static final String DEFAULT_DIMENSION = "minecraft:overworld";

    public Location {
        x = x + 0.0;
        y = y + 0.0;
        z = z + 0.0;
        dimension = dimensionId(dimension);
    }

    public static String dimensionId(String name) {
        String id = name == null ? "" : name.strip().toLowerCase(Locale.ROOT);
        if (id.isEmpty()) {
            return DEFAULT_DIMENSION;
        }
        return id.contains(":") ? id : "minecraft:" + id;
    }

    public Location offset(double dx, double dy, double dz) {
        return new Location(x + dx, y + dy, z + dz, dimension);
    }

    public Location blockCorner() {
        return new Location(Math.floor(x), Math.floor(y), Math.floor(z), dimension);
    }

    public boolean sameDimension(Location other) {
        return dimension.equals(other.dimension);
    }

    public double distance(Location other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double dz = z - other.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
