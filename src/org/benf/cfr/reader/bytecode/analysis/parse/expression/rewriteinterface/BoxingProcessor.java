/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;

public interface BoxingProcessor {
    public boolean rewriteBoxing(PrimitiveBoxingRewriter var1);

    public void applyNonArgExpressionRewriter(ExpressionRewriter var1, SSAIdentifiers var2, StatementContainer var3, ExpressionRewriterFlags var4);
}

