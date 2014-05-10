/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.CastAction;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public class TernaryExpression
extends AbstractExpression
implements BoxingProcessor {
    private ConditionalExpression condition;
    private Expression lhs;
    private Expression rhs;

    public TernaryExpression(ConditionalExpression condition, Expression lhs, Expression rhs) {
        super(TernaryExpression.inferredType(lhs.getInferredJavaType(), rhs.getInferredJavaType()));
        this.condition = condition;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.condition.collectTypeUsages(collector);
        this.lhs.collectTypeUsages(collector);
        this.rhs.collectTypeUsages(collector);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new TernaryExpression((ConditionalExpression)cloneHelper.replaceOrClone(this.condition), cloneHelper.replaceOrClone(this.lhs), cloneHelper.replaceOrClone(this.rhs));
    }

    private static InferredJavaType inferredType(InferredJavaType a, InferredJavaType b) {
        b.chain(a);
        return a;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.CONDITIONAL;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        this.condition.dumpWithOuterPrecedence(d, this.getPrecedence());
        d.print(" ? ");
        this.lhs.dumpWithOuterPrecedence(d, this.getPrecedence());
        d.print(" : ");
        this.rhs.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        Expression replacementCondition = this.condition.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        if (replacementCondition != this.condition) {
            throw new ConfusedCFRException("Can't yet support replacing conditions");
        }
        this.lhs = this.lhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        this.rhs = this.rhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.condition = expressionRewriter.rewriteExpression(this.condition, ssaIdentifiers, statementContainer, flags);
        this.lhs = expressionRewriter.rewriteExpression(this.lhs, ssaIdentifiers, statementContainer, flags);
        this.rhs = expressionRewriter.rewriteExpression(this.rhs, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.condition.collectUsedLValues(lValueUsageCollector);
        this.lhs.collectUsedLValues(lValueUsageCollector);
        this.rhs.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        if (boxingRewriter.isUnboxedType(this.lhs)) {
            this.rhs = boxingRewriter.sugarUnboxing(this.rhs);
            return false;
        }
        if (!boxingRewriter.isUnboxedType(this.rhs)) return false;
        this.lhs = boxingRewriter.sugarUnboxing(this.lhs);
        return false;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TernaryExpression that = (TernaryExpression)o;
        if (this.condition != null ? !this.condition.equals(that.condition) : that.condition != null) {
            return false;
        }
        if (this.lhs != null ? !this.lhs.equals(that.lhs) : that.lhs != null) {
            return false;
        }
        if (!(this.rhs != null ? !this.rhs.equals(that.rhs) : that.rhs != null)) return true;
        return false;
    }

    @Override
    public final boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        TernaryExpression other = (TernaryExpression)o;
        if (!constraint.equivalent(this.condition, other.condition)) {
            return false;
        }
        if (!constraint.equivalent(this.lhs, other.lhs)) {
            return false;
        }
        if (constraint.equivalent(this.rhs, other.rhs)) return true;
        return false;
    }
}

