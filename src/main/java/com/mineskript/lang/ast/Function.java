package com.mineskript.lang.ast;

import java.util.List;

public final class Function {
    public record Parameter(String name, SkType type, Expression fallback) {
    }

    private final String name;
    private final String file;
    private final int line;
    private final boolean local;
    private final List<Parameter> parameters;
    private final SkType returnType;
    private Block body;

    public Function(String name, String file, int line, boolean local, List<Parameter> parameters, SkType returnType) {
        this.name = name;
        this.file = file;
        this.line = line;
        this.local = local;
        this.parameters = List.copyOf(parameters);
        this.returnType = returnType;
    }

    public String name() {
        return name;
    }

    public String file() {
        return file;
    }

    public int line() {
        return line;
    }

    public boolean local() {
        return local;
    }

    public List<Parameter> parameters() {
        return parameters;
    }

    public SkType returnType() {
        return returnType;
    }

    public boolean returns() {
        return returnType != null;
    }

    public int requiredParameters() {
        int required = 0;
        for (Parameter parameter : parameters) {
            if (parameter.fallback() == null) {
                required++;
            }
        }
        return required;
    }

    public Block body() {
        return body;
    }

    public void define(Block body) {
        this.body = body;
    }
}
