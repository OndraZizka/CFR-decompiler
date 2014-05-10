/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class CompoundStatement
extends AbstractStatement {
    private List<Statement> statements;

    public /* varargs */ CompoundStatement(Statement ... statements) {
        this.statements = ListFactory.newList(statements);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("{\n");
        for (Statement statement : this.statements) {
            statement.dump(dumper);
        }
        dumper.print("}\n");
        return dumper;
    }

    @Override
    public void collectLValueAssignments(LValueAssignmentCollector<Statement> lValueAssigmentCollector) {
        throw new ConfusedCFRException("Should not be using compound statements here");
    }

    @Override
    public LValue getCreatedLValue() {
        throw new ConfusedCFRException("Should not be using compound statements here");
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        throw new ConfusedCFRException("Should not be using compound statements here");
    }

    @Override
    public Expression getRValue() {
        throw new ConfusedCFRException("Should not be using compound statements here");
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        throw new ConfusedCFRException("Should not be using compound statements here");
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        throw new ConfusedCFRException("Should not be using compound statements here");
    }

    @Override
    public boolean isCompound() {
        return true;
    }

    @Override
    public List<Statement> getCompoundParts() {
        return this.statements;
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        throw new UnsupportedOperationException();
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
        CompoundStatement other = (CompoundStatement)o;
        if (constraint.equivalent(this.statements, other.statements)) return true;
        return false;
    }
}

