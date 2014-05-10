/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.checker;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.checker.Op04Checker;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredTry;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.DecompilerComments;

public class LooseCatchChecker
implements Op04Checker {
    private boolean looseCatch = false;

    @Override
    public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
        StructuredStatement outer;
        if (this.looseCatch) {
            return in;
        }
        if (in instanceof StructuredCatch && !(outer = scope.getInnermost() instanceof StructuredTry)) {
            this.looseCatch = true;
            return in;
        }
        in.transformStructuredChildren(this, scope);
        return in;
    }

    @Override
    public void commentInto(DecompilerComments comments) {
        if (!this.looseCatch) return;
        comments.addComment(DecompilerComment.LOOSE_CATCH_BLOCK);
    }
}

