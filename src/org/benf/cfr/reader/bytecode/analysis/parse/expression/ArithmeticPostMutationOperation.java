/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMutatingAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ArithmeticPostMutationOperation
extends AbstractMutatingAssignmentExpression {
    private LValue mutated;
    private final ArithOp op;

    public ArithmeticPostMutationOperation(LValue mutated, ArithOp op) {
        super(mutated.getInferredJavaType());
        this.mutated = mutated;
        this.op = op;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ArithmeticPostMutationOperation(cloneHelper.replaceOrClone(this.mutated), this.op);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.UNARY_POST;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return d.dump(this.mutated).print(this.op == ArithOp.PLUS ? "++" : "--");
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return this;
    }

    @Override
    public ArithmeticPostMutationOperation getPostMutation() {
        throw new IllegalStateException();
    }

    @Override
    public boolean isSelfMutatingOp1(LValue lValue, ArithOp arithOp) {
        return this.mutated.equals(lValue) && this.op == arithOp;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArithmeticPostMutationOperation)) {
            return false;
        }
        ArithmeticPostMutationOperation other = (ArithmeticPostMutationOperation)o;
        return this.mutated.equals(other.mutated) && this.op.equals((Object)other.op);
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArithmeticPostMutationOperation)) {
            return false;
        }
        ArithmeticPostMutationOperation other = (ArithmeticPostMutationOperation)o;
        if (!constraint.equivalent(this.mutated, other.mutated)) {
            return false;
        }
        if (constraint.equivalent((Object)this.op, (Object)other.op)) return true;
        return false;
    }
}

