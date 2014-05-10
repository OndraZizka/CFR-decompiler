/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMutatingAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPostMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
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
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ArithmeticMutationOperation
extends AbstractMutatingAssignmentExpression {
    private LValue mutated;
    private final ArithOp op;
    private Expression mutation;

    public ArithmeticMutationOperation(LValue mutated, Expression mutation, ArithOp op) {
        super(mutated.getInferredJavaType());
        this.mutated = mutated;
        this.op = op;
        this.mutation = mutation;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ArithmeticMutationOperation(cloneHelper.replaceOrClone(this.mutated), cloneHelper.replaceOrClone(this.mutation), this.op);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.mutated.collectTypeUsages(collector);
        this.mutation.collectTypeUsages(collector);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.ASSIGNMENT;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.dump(this.mutated).print(this.op.getShowAs() + "=");
        this.mutation.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.mutation = this.mutation.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.mutation = expressionRewriter.rewriteExpression(this.mutation, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public boolean isSelfMutatingOp1(LValue lValue, ArithOp arithOp) {
        return this.mutated.equals(lValue) && this.op == arithOp && this.mutation.equals(new Literal(TypedLiteral.getInt(1)));
    }

    @Override
    public ArithmeticPostMutationOperation getPostMutation() {
        return new ArithmeticPostMutationOperation(this.mutated, this.op);
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.mutation.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArithmeticMutationOperation)) {
            return false;
        }
        ArithmeticMutationOperation other = (ArithmeticMutationOperation)o;
        return this.mutated.equals(other.mutated) && this.op.equals((Object)other.op) && this.mutation.equals(other.mutation);
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
        ArithmeticMutationOperation other = (ArithmeticMutationOperation)o;
        if (!constraint.equivalent((Object)this.op, (Object)other.op)) {
            return false;
        }
        if (!constraint.equivalent(this.mutated, other.mutated)) {
            return false;
        }
        if (constraint.equivalent(this.mutation, other.mutation)) return true;
        return false;
    }
}

