/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class LValueExpression
extends AbstractExpression {
    private LValue lValue;

    public LValueExpression(LValue lValue) {
        super(lValue.getInferredJavaType());
        this.lValue = lValue;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new LValueExpression(cloneHelper.replaceOrClone(this.lValue));
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.lValue.collectTypeUsages(collector);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        Expression replacement;
        if (lValueRewriter.explicitlyReplaceThisLValue(this.lValue) && (replacement = lValueRewriter.getLValueReplacement(this.lValue, ssaIdentifiers, statementContainer)) != null) {
            return replacement;
        }
        this.lValue = this.lValue.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.lValue = expressionRewriter.rewriteExpression(this.lValue, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return this.lValue.dumpWithOuterPrecedence(d, this.getPrecedence());
    }

    public LValue getLValue() {
        return this.lValue;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        lValueUsageCollector.collect(this.lValue);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof LValueExpression) return this.lValue.equals(((LValueExpression)o).getLValue());
        return false;
    }

    public int hashCode() {
        return this.lValue.hashCode();
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
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
        if (this.getClass() != o.getClass()) {
            return false;
        }
        LValueExpression other = (LValueExpression)o;
        if (constraint.equivalent(this.lValue, other.lValue)) return true;
        return false;
    }

    @Override
    public Literal getComputedLiteral(Map<LValue, Literal> display) {
        return display.get(this.lValue);
    }
}

