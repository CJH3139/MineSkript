package com.mineskript.lang.lexer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class LexerTest {
    private static LexResult lex(String source) {
        return Lexer.lex("test.ms", source);
    }

    @Test
    void buildsNestedSections() {
        LexResult result = lex("""
                on chat:
                    if message is "hi":
                        send "hello"
                    send "seen"
                """);
        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.nodes().size());
        Node event = result.nodes().get(0);
        assertEquals("on chat", event.text());
        assertTrue(event.section());
        assertEquals(1, event.line());
        assertEquals(2, event.children().size());
        Node cond = event.children().get(0);
        assertEquals("if message is \"hi\"", cond.text());
        assertTrue(cond.section());
        assertEquals(List.of("send \"hello\""), cond.children().stream().map(Node::text).toList());
        assertEquals("send \"seen\"", event.children().get(1).text());
        assertFalse(event.children().get(1).section());
        assertEquals(4, event.children().get(1).line());
    }

    @Test
    void stripsCommentsAndBlankLinesButKeepsHashInsideQuotes() {
        LexResult result = lex("""
                # header comment

                on load:   # trailing
                    send "a # b"  # comment
                """);
        assertTrue(result.errors().isEmpty());
        Node event = result.nodes().get(0);
        assertEquals("on load", event.text());
        assertEquals("send \"a # b\"", event.children().get(0).text());
        assertEquals(4, event.children().get(0).line());
    }

    @Test
    void acceptsTabIndentation() {
        LexResult result = lex("on load:\n\tsend \"x\"\n\tif 1 is 1:\n\t\tstop\n");
        assertTrue(result.errors().isEmpty());
        Node event = result.nodes().get(0);
        assertEquals(2, event.children().size());
        assertEquals("stop", event.children().get(1).children().get(0).text());
    }

    @Test
    void reportsMixedIndentationAndDropsTheTrigger() {
        LexResult result = lex("on load:\n \tsend \"x\"\non chat:\n    stop\n");
        assertEquals(1, result.errors().size());
        assertEquals("test.ms:2: mixed tabs and spaces in indentation", result.errors().get(0).toString());
        assertEquals(1, result.nodes().size());
        assertEquals("on chat", result.nodes().get(0).text());
    }

    @Test
    void reportsDanglingIndent() {
        LexResult result = lex("on load:\n    send \"x\"\n        send \"y\"\n");
        assertEquals(1, result.errors().size());
        assertEquals("test.ms:3: unexpected indentation", result.errors().get(0).toString());
        assertTrue(result.nodes().isEmpty());
    }

    @Test
    void reportsIndentedTopLevelLine() {
        LexResult result = lex("    send \"x\"\non load:\n    stop\n");
        assertEquals("test.ms:1: unexpected indentation", result.errors().get(0).toString());
        assertEquals(1, result.nodes().size());
    }

    @Test
    void reportsEmptySection() {
        LexResult result = lex("on load:\non chat:\n    stop\n");
        assertEquals("test.ms:1: empty section", result.errors().get(0).toString());
        assertEquals(1, result.nodes().size());
        assertEquals("on chat", result.nodes().get(0).text());
    }

    @Test
    void reportsEmptyNestedSection() {
        LexResult result = lex("on load:\n    if 1 is 1:\n    stop\n");
        assertEquals("test.ms:2: empty section", result.errors().get(0).toString());
        assertTrue(result.nodes().isEmpty());
    }

    @Test
    void reportsIndentNotMultipleOfUnit() {
        LexResult result = lex("on load:\n    stop\non chat:\n  stop\n");
        assertEquals("test.ms:4: unexpected indentation", result.errors().get(0).toString());
        assertEquals(1, result.nodes().size());
    }

    @Test
    void stripsLeadingByteOrderMark() {
        LexResult result = lex("﻿on load:\n    send \"x\"\n");
        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.nodes().size());
        assertEquals("on load", result.nodes().get(0).text());
    }
}
