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
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
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
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class NotOperation
extends AbstractExpression
implements ConditionalExpression {
    private ConditionalExpression inner;

    public NotOperation(ConditionalExpression lhs) {
        super(lhs.getInferredJavaType());
        this.inner = lhs;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.inner.collectTypeUsages(collector);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new NotOperation((ConditionalExpression)cloneHelper.replaceOrClone(this.inner));
    }

    @Override
    public int getSize() {
        return 1 + this.inner.getSize();
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.inner = expressionRewriter.rewriteExpression(this.inner, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.UNARY_OTHER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.print("!");
        this.inner.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d;
    }

    @Override
    public ConditionalExpression getNegated() {
        return this.inner;
    }

    @Override
    public ConditionalExpression getDemorganApplied(boolean amNegating) {
        return this.inner.getDemorganApplied(!amNegating);
    }

    @Override
    public Set<LValue> getLoopLValues() {
        return this.inner.getLoopLValues();
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
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NotOperation)) {
            return false;
        }
        NotOperation other = (NotOperation)obj;
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
        NotOperation other = (NotOperation)o;
        if (constraint.equivalent(this.inner, other.inner)) return true;
        return false;
    }

    @Override
    public Literal getComputedLiteral(Map<LValue, Literal> display) {
        Boolean boolVal;
        TypedLiteral typedLiteral;
        Literal lv = this.inner.getComputedLiteral(display);
        if (lv == null) {
            return null;
        }
        if ((boolVal = (typedLiteral = lv.getValue()).getMaybeBoolValue()) == null) {
            return null;
        }
        return boolVal.booleanValue() ? Literal.TRUE : Literal.FALSE;
    }
}

