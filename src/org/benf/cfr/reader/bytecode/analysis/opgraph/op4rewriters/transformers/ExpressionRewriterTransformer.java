/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class ExpressionRewriterTransformer
implements StructuredStatementTransformer {
    private final ExpressionRewriter expressionRewriter;

    public ExpressionRewriterTransformer(ExpressionRewriter expressionRewriter) {
        this.expressionRewriter = expressionRewriter;
    }

    @Override
    public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
        in.rewriteExpressions(this.expressionRewriter);
        in.transformStructuredChildren(this, scope);
        return in;
    }
}

