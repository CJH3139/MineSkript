package com.mineskript.lang.lexer;

import com.mineskript.lang.Language;
import com.mineskript.lang.ParseError;
import java.util.ArrayList;
import java.util.List;

public final class Lexer {
    private Lexer() {
    }

    public static LexResult lex(String file, String source) {
        String text = source.startsWith("﻿") ? source.substring(1) : source;
        List<ParseError> errors = new ArrayList<>();
        List<Line> lines = readLines(file, text, errors);
        List<Node> nodes = buildTree(file, lines, errors);
        return new LexResult(nodes, errors);
    }

    private static List<Line> readLines(String file, String source, List<ParseError> errors) {
        List<Line> lines = new ArrayList<>();
        String[] raw = source.split("\r?\n", -1);
        int blockCommentStart = 0;
        StringBuilder continued = null;
        int continuedFrom = 0;
        for (int i = 0; i < raw.length; i++) {
            int number = i + 1;
            if (raw[i].strip().startsWith("###")) {
                blockCommentStart = blockCommentStart == 0 ? number : 0;
                continue;
            }
            if (blockCommentStart != 0) {
                continue;
            }
            String withoutComment = stripComment(raw[i]);
            if (continued != null) {
                continued.append(' ').append(withoutComment.strip());
                withoutComment = continued.toString();
                number = continuedFrom;
                continued = null;
            }
            if (withoutComment.stripTrailing().endsWith("\\")) {
                String head = withoutComment.stripTrailing();
                continued = new StringBuilder(head.substring(0, head.length() - 1).stripTrailing());
                continuedFrom = number;
                continue;
            }
            if (withoutComment.isBlank()) {
                continue;
            }
            int indentEnd = 0;
            boolean tabs = false;
            boolean spaces = false;
            while (indentEnd < withoutComment.length() && (withoutComment.charAt(indentEnd) == ' ' || withoutComment.charAt(indentEnd) == '\t')) {
                if (withoutComment.charAt(indentEnd) == '\t') {
                    tabs = true;
                } else {
                    spaces = true;
                }
                indentEnd++;
            }
            if (tabs && spaces) {
                errors.add(new ParseError(file, number, Language.get("lexer.mixed-indentation")));
                lines.add(new Line(null, number, indentEnd));
                continue;
            }
            lines.add(new Line(withoutComment.substring(indentEnd).stripTrailing(), number, indentEnd));
        }
        if (blockCommentStart != 0) {
            errors.add(new ParseError(file, blockCommentStart, Language.get("lexer.unclosed-block-comment")));
        }
        if (continued != null) {
            errors.add(new ParseError(file, continuedFrom, Language.get("lexer.dangling-continuation")));
        }
        return lines;
    }

    private static String stripComment(String line) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                i = TextScanner.closingQuote(line, i);
                if (i < 0) {
                    return line;
                }
            } else if (c == '#') {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private static List<Node> buildTree(String file, List<Line> lines, List<ParseError> errors) {
        List<Node> nodes = new ArrayList<>();
        int unit = 0;
        int i = 0;
        while (i < lines.size()) {
            Line top = lines.get(i);
            if (top.text() == null) {
                i = skipTrigger(lines, i + 1);
                continue;
            }
            if (top.indent() != 0) {
                errors.add(new ParseError(file, top.number(), Language.get("lexer.unexpected-indentation")));
                i = skipTrigger(lines, i + 1);
                continue;
            }
            if (unit == 0 && i + 1 < lines.size() && lines.get(i + 1).text() != null && lines.get(i + 1).indent() > 0) {
                unit = lines.get(i + 1).indent();
            }
            List<Node> topChildren = new ArrayList<>();
            Node node = new Node(sectionText(top.text()), top.number(), isSection(top.text()), topChildren);
            List<Node> parentsByDepth = new ArrayList<>();
            parentsByDepth.add(node);
            int j = i + 1;
            boolean brokenInLoop = false;
            while (j < lines.size() && (lines.get(j).text() == null || lines.get(j).indent() > 0)) {
                Line line = lines.get(j);
                if (line.text() == null) {
                    brokenInLoop = true;
                    break;
                }
                if (line.indent() % unit != 0) {
                    errors.add(new ParseError(file, line.number(), Language.get("lexer.unexpected-indentation")));
                    brokenInLoop = true;
                    break;
                }
                int depth = line.indent() / unit;
                if (depth > parentsByDepth.size()) {
                    errors.add(new ParseError(file, line.number(), Language.get("lexer.unexpected-indentation")));
                    brokenInLoop = true;
                    break;
                }
                while (parentsByDepth.size() > depth) {
                    parentsByDepth.removeLast();
                }
                Node parent = parentsByDepth.getLast();
                if (!parent.section()) {
                    errors.add(new ParseError(file, line.number(), Language.get("lexer.unexpected-indentation")));
                    brokenInLoop = true;
                    break;
                }
                Node child = new Node(sectionText(line.text()), line.number(), isSection(line.text()), new ArrayList<>());
                parent.children().add(child);
                parentsByDepth.add(child);
                j++;
            }
            boolean dropped = brokenInLoop;
            if (!dropped) {
                Node empty = findEmptySection(node);
                if (empty != null) {
                    errors.add(new ParseError(file, empty.line(), Language.get("lexer.empty-section")));
                    dropped = true;
                }
            }
            if (!dropped) {
                nodes.add(freeze(node));
            }
            i = brokenInLoop ? skipTrigger(lines, j + 1) : j;
        }
        return nodes;
    }

    private static int skipTrigger(List<Line> lines, int from) {
        int i = from;
        while (i < lines.size() && (lines.get(i).text() == null || lines.get(i).indent() > 0)) {
            i++;
        }
        return i;
    }

    private static Node findEmptySection(Node node) {
        if (node.section() && node.children().isEmpty()) {
            return node;
        }
        for (Node child : node.children()) {
            Node empty = findEmptySection(child);
            if (empty != null) {
                return empty;
            }
        }
        return null;
    }

    private static Node freeze(Node node) {
        List<Node> children = node.children().stream().map(Lexer::freeze).toList();
        return new Node(node.text(), node.line(), node.section(), children);
    }

    private static boolean isSection(String text) {
        return text.endsWith(":");
    }

    private static String sectionText(String text) {
        return isSection(text) ? text.substring(0, text.length() - 1).stripTrailing() : text;
    }
}
