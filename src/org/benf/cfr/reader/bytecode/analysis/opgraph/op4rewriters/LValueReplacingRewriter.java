/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AbstractExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;

public class LValueReplacingRewriter
extends AbstractExpressionRewriter {
    private final Map<LValue, LValue> replacements;

    public LValueReplacingRewriter(Map<LValue, LValue> replacements) {
        this.replacements = replacements;
    }

    @Override
    public LValue rewriteExpression(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        LValue replacement = this.replacements.get(lValue);
        if (replacement == null) return lValue.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        return replacement;
    }
}

