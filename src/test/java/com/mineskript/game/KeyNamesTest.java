package com.mineskript.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mineskript.lang.ast.SkType;
import com.mineskript.lang.parse.ConstantExpression;
import com.mineskript.lang.parse.SyntaxException;
import org.junit.jupiter.api.Test;

class KeyNamesTest {
    @Test
    void mapsCommonNamesToMinecraftKeyIds() {
        assertEquals("key.keyboard.r", KeyNames.toKeyId("r").orElseThrow());
        assertEquals("key.keyboard.r", KeyNames.toKeyId("R").orElseThrow());
        assertEquals("key.keyboard.space", KeyNames.toKeyId("space").orElseThrow());
        assertEquals("key.keyboard.f6", KeyNames.toKeyId("f6").orElseThrow());
        assertEquals("key.keyboard.left.shift", KeyNames.toKeyId("left shift").orElseThrow());
        assertEquals("key.keyboard.left.shift", KeyNames.toKeyId("shift").orElseThrow());
        assertEquals("key.keyboard.left.control", KeyNames.toKeyId("ctrl").orElseThrow());
        assertEquals("key.keyboard.keypad.5", KeyNames.toKeyId("keypad 5").orElseThrow());
        assertEquals("key.keyboard.7", KeyNames.toKeyId("7").orElseThrow());
        assertEquals("key.mouse.left", KeyNames.toKeyId("mouse left").orElseThrow());
        assertEquals("key.mouse.4", KeyNames.toKeyId("mouse 4").orElseThrow());
    }

    @Test
    void rejectsUnknownNames() {
        assertTrue(KeyNames.toKeyId("banana").isEmpty());
        assertTrue(KeyNames.toKeyId("").isEmpty());
        assertTrue(KeyNames.toKeyId("f99").isEmpty());
    }

    @Test
    void keyIdOfRequiresAPlainKnownString() {
        assertEquals("key.keyboard.w", KeyNames.keyIdOf(new ConstantExpression(SkType.TEXT, "w")));
        SyntaxException unknown = assertThrows(SyntaxException.class, () -> KeyNames.keyIdOf(new ConstantExpression(SkType.TEXT, "banana")));
        assertEquals("unknown key \"banana\"", unknown.getMessage());
        SyntaxException dynamic = assertThrows(SyntaxException.class, () -> KeyNames.keyIdOf(new ConstantExpression(SkType.NUMBER, 5.0)));
        assertEquals("key name must be a plain string like \"r\"", dynamic.getMessage());
    }
}
