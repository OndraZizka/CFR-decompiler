/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredAnonBreakTarget;
import org.benf.cfr.reader.util.output.Dumper;

public class AnonBreakTarget
extends AbstractStatement {
    private final BlockIdentifier blockIdentifier;

    public AnonBreakTarget(BlockIdentifier blockIdentifier) {
        this.blockIdentifier = blockIdentifier;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper;
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
    public StructuredStatement getStructuredStatement() {
        return new UnstructuredAnonBreakTarget(this.blockIdentifier);
    }

    public BlockIdentifier getBlockIdentifier() {
        return this.blockIdentifier;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && this.getClass() == o.getClass()) return true;
        return false;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (this == o) {
            return true;
        }
        if (o != null && this.getClass() == o.getClass()) return true;
        return false;
    }
}

