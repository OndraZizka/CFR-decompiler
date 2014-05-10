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
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class StackValue
extends AbstractExpression {
    private StackSSALabel stackValue;

    public StackValue(StackSSALabel stackValue) {
        super(stackValue.getInferredJavaType());
        this.stackValue = stackValue;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.WEAKEST;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return this.stackValue.dump(d);
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.stackValue.collectTypeUsages(collector);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return this;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        Expression replaceMeWith = lValueRewriter.getLValueReplacement(this.stackValue, ssaIdentifiers, statementContainer);
        if (replaceMeWith == null) return this;
        return replaceMeWith;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.stackValue = expressionRewriter.rewriteExpression(this.stackValue, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    public StackSSALabel getStackValue() {
        return this.stackValue;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        lValueUsageCollector.collect(this.stackValue);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StackValue)) {
            return false;
        }
        StackValue other = (StackValue)o;
        return this.stackValue.equals(other.stackValue);
    }

    public int hashCode() {
        return this.stackValue.hashCode();
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof StackValue)) {
            return false;
        }
        StackValue other = (StackValue)o;
        return constraint.equivalent(this.stackValue, other.stackValue);
    }

    @Override
    public Literal getComputedLiteral(Map<LValue, Literal> display) {
        return display.get(this.stackValue);
    }
}

