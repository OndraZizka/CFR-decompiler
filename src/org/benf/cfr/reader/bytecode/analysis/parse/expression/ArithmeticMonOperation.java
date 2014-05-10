/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
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
import org.benf.cfr.reader.util.output.Dumper;

public class ArithmeticMonOperation
extends AbstractExpression {
    private Expression lhs;
    private final ArithOp op;

    public ArithmeticMonOperation(Expression lhs, ArithOp op) {
        super(lhs.getInferredJavaType());
        this.lhs = lhs;
        this.op = op;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.lhs.collectTypeUsages(collector);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ArithmeticMonOperation(cloneHelper.replaceOrClone(this.lhs), this.op);
    }

    @Override
    public Precedence getPrecedence() {
        return this.op.getPrecedence();
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.print(this.op.getShowAs() + " ");
        this.lhs.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.lhs = this.lhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.lhs = expressionRewriter.rewriteExpression(this.lhs, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.lhs.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ArithmeticMonOperation that = (ArithmeticMonOperation)o;
        if (this.lhs != null ? !this.lhs.equals(that.lhs) : that.lhs != null) {
            return false;
        }
        if (this.op == that.op) return true;
        return false;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ArithmeticMonOperation other = (ArithmeticMonOperation)o;
        if (!constraint.equivalent(this.lhs, other.lhs)) {
            return false;
        }
        if (constraint.equivalent((Object)this.op, (Object)other.op)) return true;
        return false;
    }
}

