/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NotOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
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
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class BooleanExpression
extends AbstractExpression
implements ConditionalExpression {
    private Expression inner;

    public BooleanExpression(Expression inner) {
        super(new InferredJavaType(RawJavaType.BOOLEAN, InferredJavaType.Source.EXPRESSION));
        this.inner = inner;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.inner.collectTypeUsages(collector);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new BooleanExpression(cloneHelper.replaceOrClone(this.inner));
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.inner = this.inner.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.inner = expressionRewriter.rewriteExpression(this.inner, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public Precedence getPrecedence() {
        return this.inner.getPrecedence();
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return this.inner.dump(d);
    }

    @Override
    public ConditionalExpression getNegated() {
        return new NotOperation(this);
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
        this.addIfLValue(this.inner, res);
        return res;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.inner.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public ConditionalExpression optimiseForType() {
        return this;
    }

    @Override
    public ConditionalExpression simplify() {
        return ConditionalUtils.simplify(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof BooleanExpression)) {
            return false;
        }
        BooleanExpression other = (BooleanExpression)o;
        return this.inner.equals(other.inner);
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
        BooleanExpression other = (BooleanExpression)o;
        if (constraint.equivalent(this.inner, other.inner)) return true;
        return false;
    }

    @Override
    public Literal getComputedLiteral(Map<LValue, Literal> display) {
        return this.inner.getComputedLiteral(display);
    }
}

