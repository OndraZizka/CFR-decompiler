/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ForStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredWhile;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class WhileStatement
extends AbstractStatement {
    private ConditionalExpression condition;
    private BlockIdentifier blockIdentifier;

    public WhileStatement(ConditionalExpression conditionalExpression, BlockIdentifier blockIdentifier) {
        this.condition = conditionalExpression;
        this.blockIdentifier = blockIdentifier;
    }

    private int getBackJumpIndex() {
        return this.condition == null ? 0 : 1;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("while (");
        if (this.condition == null) {
            dumper.print("true");
        } else {
            dumper.dump(this.condition);
        }
        dumper.print(") ");
        dumper.print(" // ends " + this.getTargetStatement(this.getBackJumpIndex()).getContainer().getLabel() + ";\n");
        return dumper;
    }

    public void replaceWithForLoop(AssignmentSimple initial, AbstractAssignmentExpression assignment) {
        if (this.condition == null) {
            throw new UnsupportedOperationException();
        }
        ForStatement forStatement = new ForStatement(this.condition, this.blockIdentifier, initial, assignment);
        this.getContainer().replaceStatement(forStatement);
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        Expression replacementCondition;
        if (this.condition == null) {
            return;
        }
        if ((replacementCondition = this.condition.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer())) == this.condition) return;
        throw new ConfusedCFRException("Can't yet support replacing conditions");
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        if (this.condition == null) {
            return;
        }
        this.condition = expressionRewriter.rewriteExpression(this.condition, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        if (this.condition == null) return;
        this.condition.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new UnstructuredWhile(this.condition, this.blockIdentifier, this.getTargetStatement(this.getBackJumpIndex()).getContainer().getBlocksEnded());
    }

    public BlockIdentifier getBlockIdentifier() {
        return this.blockIdentifier;
    }

    public ConditionalExpression getCondition() {
        return this.condition;
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
        WhileStatement other = (WhileStatement)o;
        if (constraint.equivalent(this.condition, other.condition)) return true;
        return false;
    }
}

