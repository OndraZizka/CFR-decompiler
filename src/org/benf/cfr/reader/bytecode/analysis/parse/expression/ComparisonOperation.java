/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CompOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ConditionalUtils;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class ComparisonOperation
extends AbstractExpression
implements ConditionalExpression,
BoxingProcessor {
    private Expression lhs;
    private Expression rhs;
    private final CompOp op;

    public ComparisonOperation(Expression lhs, Expression rhs, CompOp op) {
        super(new InferredJavaType(RawJavaType.BOOLEAN, InferredJavaType.Source.EXPRESSION));
        this.lhs = lhs;
        this.rhs = rhs;
        boolean lLiteral = lhs instanceof Literal;
        boolean rLiteral = rhs instanceof Literal;
        InferredJavaType.compareAsWithoutCasting(lhs.getInferredJavaType(), rhs.getInferredJavaType(), lLiteral, rLiteral);
        this.op = op;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ComparisonOperation(cloneHelper.replaceOrClone(this.lhs), cloneHelper.replaceOrClone(this.rhs), this.op);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.lhs.collectTypeUsages(collector);
        this.rhs.collectTypeUsages(collector);
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public Precedence getPrecedence() {
        return this.op.getPrecedence();
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        this.lhs.dumpWithOuterPrecedence(d, this.getPrecedence());
        d.print(" " + this.op.getShowAs() + " ");
        this.rhs.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        Expression res;
        this.lhs = this.lhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        this.rhs = this.rhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        if (this.lhs.canPushDownInto()) {
            Expression res2;
            if (this.rhs.canPushDownInto()) {
                throw new ConfusedCFRException("2 sides of a comparison support pushdown?");
            }
            if ((res2 = this.lhs.pushDown(this.rhs, this)) == null) return this;
            return res2;
        }
        if (!this.rhs.canPushDownInto() || (res = this.rhs.pushDown(this.lhs, this.getNegated())) == null) return this;
        return res;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.lhs = expressionRewriter.rewriteExpression(this.lhs, ssaIdentifiers, statementContainer, flags);
        this.rhs = expressionRewriter.rewriteExpression(this.rhs, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public ConditionalExpression getNegated() {
        return new ComparisonOperation(this.lhs, this.rhs, this.op.getInverted());
    }

    public CompOp getOp() {
        return this.op;
    }

    @Override
    public ConditionalExpression getDemorganApplied(boolean amNegating) {
        if (amNegating) return this.getNegated();
        return this;
    }

    protected void addIfLValue(Expression expression, Set<LValue> res) {
        if (!(expression instanceof LValueExpression)) return;
        res.add(((LValueExpression)expression).getLValue());
    }

    @Override
    public Set<LValue> getLoopLValues() {
        Set res = SetFactory.newSet();
        this.addIfLValue(this.lhs, res);
        this.addIfLValue(this.rhs, res);
        return res;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.lhs.collectUsedLValues(lValueUsageCollector);
        this.rhs.collectUsedLValues(lValueUsageCollector);
    }

    private static BooleanComparisonType isBooleanComparison(Expression a, Expression b, CompOp op) {
        TypedLiteral lit;
        int i;
        Literal literal;
        switch (op) {
            case EQ: 
            case NE: {
                break;
            }
            default: {
                return BooleanComparisonType.NOT;
            }
        }
        if (a.getInferredJavaType().getJavaTypeInstance().getRawTypeOfSimpleType() != RawJavaType.BOOLEAN) {
            return BooleanComparisonType.NOT;
        }
        if (!(b instanceof Literal)) {
            return BooleanComparisonType.NOT;
        }
        if ((lit = (literal = (Literal)b).getValue()).getType() != TypedLiteral.LiteralType.Integer) {
            return BooleanComparisonType.NOT;
        }
        if ((i = ((Integer)lit.getValue()).intValue()) < 0 || i > 1) {
            return BooleanComparisonType.NOT;
        }
        if (op == CompOp.NE) {
            i = 1 - i;
        }
        if (i != 0) return BooleanComparisonType.AS_IS;
        return BooleanComparisonType.NEGATED;
    }

    public ConditionalExpression getConditionalExpression(Expression booleanExpression, BooleanComparisonType booleanComparisonType) {
        ConditionalExpression res = null;
        res = booleanExpression instanceof ConditionalExpression ? (ConditionalExpression)booleanExpression : new BooleanExpression(booleanExpression);
        if (booleanComparisonType != BooleanComparisonType.NEGATED) return res;
        res = res.getNegated();
        return res;
    }

    @Override
    public ConditionalExpression optimiseForType() {
        BooleanComparisonType bct = null;
        bct = ComparisonOperation.isBooleanComparison(this.lhs, this.rhs, this.op);
        if (bct.isValid()) {
            return this.getConditionalExpression(this.lhs, bct);
        }
        if (!(bct = ComparisonOperation.isBooleanComparison(this.rhs, this.lhs, this.op)).isValid()) return this;
        return this.getConditionalExpression(this.rhs, bct);
    }

    public Expression getLhs() {
        return this.lhs;
    }

    public Expression getRhs() {
        return this.rhs;
    }

    @Override
    public ConditionalExpression simplify() {
        return ConditionalUtils.simplify(this);
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        switch (this.op) {
            case EQ: 
            case NE: {
                if (boxingRewriter.isUnboxedType(this.lhs)) {
                    this.rhs = boxingRewriter.sugarUnboxing(this.rhs);
                    return false;
                }
                if (!boxingRewriter.isUnboxedType(this.rhs)) return false;
                this.lhs = boxingRewriter.sugarUnboxing(this.lhs);
                return false;
            }
            default: {
                this.lhs = boxingRewriter.sugarUnboxing(this.lhs);
                this.rhs = boxingRewriter.sugarUnboxing(this.rhs);
            }
        }
        return false;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ComparisonOperation)) {
            return false;
        }
        ComparisonOperation other = (ComparisonOperation)o;
        return this.op == other.op && this.lhs.equals(other.lhs) && this.rhs.equals(other.rhs);
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return this.lhs.canThrow(caught) || this.rhs.canThrow(caught);
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
        ComparisonOperation other = (ComparisonOperation)o;
        if (!constraint.equivalent((Object)this.op, (Object)other.op)) {
            return false;
        }
        if (!constraint.equivalent(this.lhs, other.lhs)) {
            return false;
        }
        if (constraint.equivalent(this.rhs, other.rhs)) return true;
        return false;
    }

    @Override
    public Literal getComputedLiteral(Map<LValue, Literal> display) {
        Literal lV = this.lhs.getComputedLiteral(display);
        Literal rV = this.rhs.getComputedLiteral(display);
        if (lV == null || rV == null) {
            return null;
        }
        TypedLiteral l = lV.getValue();
        TypedLiteral r = rV.getValue();
        switch (this.op) {
            case EQ: {
                return l.equals(r) ? Literal.TRUE : Literal.FALSE;
            }
            case NE: {
                return l.equals(r) ? Literal.FALSE : Literal.TRUE;
            }
        }
        return null;
    }

    static enum BooleanComparisonType {
        NOT(false),
        AS_IS(true),
        NEGATED(true);
        
        private final boolean isValid;

        private BooleanComparisonType(boolean isValid) {
            this.isValid = isValid;
        }

        public boolean isValid() {
            return this.isValid;
        }
    }

}

