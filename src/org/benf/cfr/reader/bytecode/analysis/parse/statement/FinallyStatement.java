/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredFinally;
import org.benf.cfr.reader.util.output.Dumper;

public class FinallyStatement
extends AbstractStatement {
    private BlockIdentifier finallyBlockIdent;

    public FinallyStatement(BlockIdentifier finallyBlockIdent) {
        this.finallyBlockIdent = finallyBlockIdent;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("finally {\n");
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public LValue getCreatedLValue() {
        return null;
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new UnstructuredFinally(this.finallyBlockIdent);
    }

    public BlockIdentifier getFinallyBlockIdent() {
        return this.finallyBlockIdent;
    }

    @Override
    public final boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        FinallyStatement other = (FinallyStatement)o;
        return true;
    }
}

