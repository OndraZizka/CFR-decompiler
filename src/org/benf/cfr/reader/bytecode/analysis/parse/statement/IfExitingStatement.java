/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class IfExitingStatement
extends AbstractStatement {
    private ConditionalExpression condition;
    private Statement statement;

    public IfExitingStatement(ConditionalExpression conditionalExpression, Statement statement) {
        this.condition = conditionalExpression;
        this.statement = statement;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("if (").dump(this.condition).print(") ");
        this.statement.dump(dumper);
        return dumper;
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        Expression replacementCondition = this.condition.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
        if (replacementCondition != this.condition) {
            this.condition = (ConditionalExpression)replacementCondition;
        }
        this.statement.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers);
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.condition = expressionRewriter.rewriteExpression(this.condition, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
        this.statement.rewriteExpressions(expressionRewriter, ssaIdentifiers);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.condition.collectUsedLValues(lValueUsageCollector);
        this.statement.collectLValueUsage(lValueUsageCollector);
    }

    @Override
    public boolean condenseWithNextConditional() {
        return false;
    }

    public ConditionalExpression getCondition() {
        return this.condition;
    }

    public Statement getExitStatement() {
        return this.statement;
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new StructuredIf(this.condition, new Op04StructuredStatement(Block.getBlockFor(false, this.statement.getStructuredStatement())));
    }

    public void optimiseForTypes() {
        this.condition = this.condition.optimiseForType();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        IfExitingStatement that = (IfExitingStatement)o;
        if (this.condition != null ? !this.condition.equals(that.condition) : that.condition != null) {
            return false;
        }
        if (this.statement.equals(that.statement)) return true;
        return false;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        IfExitingStatement other = (IfExitingStatement)o;
        if (!constraint.equivalent(this.condition, other.condition)) {
            return false;
        }
        if (constraint.equivalent(this.statement, other.statement)) return true;
        return false;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return this.condition.canThrow(caught) || this.statement.canThrow(caught);
    }
}

