package com.mineskript.lang.ast;

import com.mineskript.lang.runtime.Context;
import java.util.List;

public record IfChain(int line, List<Branch> branches, Block otherwise) implements Statement {
    public record Branch(Condition condition, Block block) {
    }

    @Override
    public Flow execute(Context context) {
        for (Branch branch : branches) {
            if (branch.condition().test(context)) {
                return new Flow.Enter(branch.block());
            }
        }
        return otherwise == null ? Flow.CONTINUE : new Flow.Enter(otherwise);
    }
}
