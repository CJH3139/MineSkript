package com.mineskript.common.elements.expressions;

import java.util.concurrent.ThreadLocalRandom;

final class MathHelper {
    private MathHelper() {
    }

    static double places(double value, double digits) {
        int places = Math.max(0, Math.min(15, (int) Math.round(digits)));
        double factor = Math.pow(10, places);
        return Math.round(value * factor) / factor;
    }

    static double random(double from, double to) {
        double low = Math.min(from, to);
        double high = Math.max(from, to);
        if (low == high) {
            return low;
        }
        return ThreadLocalRandom.current().nextDouble(low, high);
    }

    static double randomInteger(double from, double to) {
        long first = Math.round(from);
        long second = Math.round(to);
        long low = Math.min(first, second);
        long high = Math.max(first, second);
        if (low == high) {
            return low;
        }
        if (high == Long.MAX_VALUE) {
            if (low == Long.MIN_VALUE) {
                return ThreadLocalRandom.current().nextLong();
            }
            return ThreadLocalRandom.current().nextLong(low - 1, high) + 1;
        }
        return ThreadLocalRandom.current().nextLong(low, high + 1);
    }
}
