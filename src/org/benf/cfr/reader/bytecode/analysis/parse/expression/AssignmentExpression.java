/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPostMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class AssignmentExpression
extends AbstractAssignmentExpression {
    private final LValue lValue;
    private Expression rValue;
    private boolean inlined;

    public AssignmentExpression(LValue lValue, Expression rValue, boolean inlined) {
        super(lValue.getInferredJavaType());
        this.lValue = lValue;
        this.rValue = rValue;
        this.inlined = inlined;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new AssignmentExpression(cloneHelper.replaceOrClone(this.lValue), cloneHelper.replaceOrClone(this.rValue), this.inlined);
    }

    public void setInlined(boolean inlined) {
        this.inlined = inlined;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.lValue.collectTypeUsages(collector);
        collector.collectFrom(this.rValue);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.ASSIGNMENT;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.dump(this.lValue).print(" = ");
        this.rValue.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.rValue = this.rValue.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.rValue = expressionRewriter.rewriteExpression(this.rValue, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public boolean isSelfMutatingOp1(LValue lValue, ArithOp arithOp) {
        return false;
    }

    @Override
    public ArithmeticPostMutationOperation getPostMutation() {
        throw new IllegalStateException();
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        lValueUsageCollector.collect(this.lValue);
        this.rValue.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        AssignmentExpression that = (AssignmentExpression)o;
        if (this.inlined != that.inlined) {
            return false;
        }
        if (this.lValue != null ? !this.lValue.equals(that.lValue) : that.lValue != null) {
            return false;
        }
        if (!(this.rValue != null ? !this.rValue.equals(that.rValue) : that.rValue != null)) return true;
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
        AssignmentExpression other = (AssignmentExpression)o;
        if (this.inlined != other.inlined) {
            return false;
        }
        if (!constraint.equivalent(this.lValue, other.lValue)) {
            return false;
        }
        if (constraint.equivalent(this.rValue, other.rValue)) return true;
        return false;
    }

    @Override
    public Literal getComputedLiteral(Map<LValue, Literal> display) {
        Literal literal;
        if (!(this.lValue instanceof StackSSALabel || this.lValue instanceof LocalVariable)) {
            return null;
        }
        if ((literal = this.rValue.getComputedLiteral(display)) == null) {
            return null;
        }
        display.put(this.lValue, literal);
        return literal;
    }
}

