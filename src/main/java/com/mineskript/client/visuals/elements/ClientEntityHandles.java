package com.mineskript.client.visuals.elements;

final class ClientEntityHandles {
    private ClientEntityHandles() {
    }

    static int handle(Object value) {
        double number = (Double) value;
        return number == Math.rint(number) && number >= 1 && number <= Integer.MAX_VALUE ? (int) number : 0;
    }
}
