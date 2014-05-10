/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredIter;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ForIterStatement
extends AbstractStatement {
    private BlockIdentifier blockIdentifier;
    private LValue iterator;
    private Expression list;

    public ForIterStatement(BlockIdentifier blockIdentifier, LValue iterator, Expression list) {
        this.blockIdentifier = blockIdentifier;
        this.iterator = iterator;
        this.list = list;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("for (").dump(this.iterator).print(" : ").dump(this.list).print(")");
        dumper.print(" // ends " + this.getTargetStatement(1).getContainer().getLabel() + ";\n");
        return dumper;
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        this.iterator.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.iterator = expressionRewriter.rewriteExpression(this.iterator, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
        this.list = expressionRewriter.rewriteExpression(this.list, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.list.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new UnstructuredIter(this.blockIdentifier, this.iterator, this.list);
    }

    public BlockIdentifier getBlockIdentifier() {
        return this.blockIdentifier;
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
        ForIterStatement other = (ForIterStatement)o;
        if (!constraint.equivalent(this.iterator, other.iterator)) {
            return false;
        }
        if (constraint.equivalent(this.list, other.list)) return true;
        return false;
    }
}

