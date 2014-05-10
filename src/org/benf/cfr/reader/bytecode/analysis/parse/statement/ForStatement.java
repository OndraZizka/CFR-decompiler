/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredFor;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ForStatement
extends AbstractStatement {
    private ConditionalExpression condition;
    private BlockIdentifier blockIdentifier;
    private AssignmentSimple initial;
    private AbstractAssignmentExpression assignment;

    public ForStatement(ConditionalExpression conditionalExpression, BlockIdentifier blockIdentifier, AssignmentSimple initial, AbstractAssignmentExpression assignment) {
        this.condition = conditionalExpression;
        this.blockIdentifier = blockIdentifier;
        this.initial = initial;
        this.assignment = assignment;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("for (");
        if (this.initial != null) {
            dumper.dump(this.initial);
        }
        dumper.print("; ").dump(this.condition).print("; ").dump(this.assignment).print(") ");
        dumper.print(" // ends " + this.getTargetStatement(1).getContainer().getLabel() + ";\n");
        return dumper;
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        this.assignment.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.condition = expressionRewriter.rewriteExpression(this.condition, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
        this.assignment = expressionRewriter.rewriteExpression(this.assignment, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.condition.collectUsedLValues(lValueUsageCollector);
        this.assignment.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new UnstructuredFor(this.condition, this.blockIdentifier, this.initial, this.assignment);
    }

    public BlockIdentifier getBlockIdentifier() {
        return this.blockIdentifier;
    }

    public ConditionalExpression getCondition() {
        return this.condition;
    }

    public AssignmentSimple getInitial() {
        return this.initial;
    }

    public AbstractAssignmentExpression getAssignment() {
        return this.assignment;
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
        ForStatement other = (ForStatement)o;
        if (!constraint.equivalent(this.condition, other.condition)) {
            return false;
        }
        if (!constraint.equivalent(this.initial, other.initial)) {
            return false;
        }
        if (constraint.equivalent(this.assignment, other.assignment)) return true;
        return false;
    }
}

