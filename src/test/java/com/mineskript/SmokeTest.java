package com.mineskript;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SmokeTest {
    @Test
    void modIdIsStable() {
        assertEquals("mineskript", MineSkriptClient.MOD_ID);
    }
}
