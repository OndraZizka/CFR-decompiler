/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredTry;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class TryStatement
extends AbstractStatement {
    private final ExceptionGroup exceptionGroup;
    private final Set<Expression> monitors = SetFactory.newSet();

    public TryStatement(ExceptionGroup exceptionGroup) {
        this.exceptionGroup = exceptionGroup;
    }

    public void addExitMutex(Expression e) {
        this.monitors.add(e);
    }

    public Set<Expression> getMonitors() {
        return this.monitors;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("try { ").print(this.exceptionGroup.getTryBlockIdentifier().toString()).print("\n");
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
        return new UnstructuredTry(this.exceptionGroup);
    }

    public BlockIdentifier getBlockIdentifier() {
        return this.exceptionGroup.getTryBlockIdentifier();
    }

    public List<ExceptionGroup.Entry> getEntries() {
        return this.exceptionGroup.getEntries();
    }

    @Override
    public boolean equivalentUnder(Object other, EquivalenceConstraint constraint) {
        return this.getClass() == other.getClass();
    }
}

