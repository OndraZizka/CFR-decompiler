/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
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
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ArrayIndex
extends AbstractExpression
implements BoxingProcessor {
    private Expression array;
    private Expression index;

    public ArrayIndex(Expression array, Expression index) {
        super(new InferredJavaType(array.getInferredJavaType().getJavaTypeInstance().removeAnArrayIndirection(), InferredJavaType.Source.OPERATION));
        this.array = array;
        this.index = index;
        index.getInferredJavaType().useAsWithoutCasting(RawJavaType.INT);
    }

    private ArrayIndex(InferredJavaType inferredJavaType, Expression array, Expression index) {
        super(inferredJavaType);
        this.array = array;
        this.index = index;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.array.collectTypeUsages(collector);
        this.index.collectTypeUsages(collector);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ArrayIndex(this.getInferredJavaType(), cloneHelper.replaceOrClone(this.array), cloneHelper.replaceOrClone(this.index));
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        this.array.dumpWithOuterPrecedence(d, this.getPrecedence());
        d.print("[").dump(this.index).print("]");
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.array = this.array.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        this.index = this.index.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.array = expressionRewriter.rewriteExpression(this.array, ssaIdentifiers, statementContainer, flags);
        this.index = expressionRewriter.rewriteExpression(this.index, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.array.collectUsedLValues(lValueUsageCollector);
        this.index.collectUsedLValues(lValueUsageCollector);
    }

    public Expression getArray() {
        return this.array;
    }

    public Expression getIndex() {
        return this.index;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArrayIndex)) {
            return false;
        }
        ArrayIndex other = (ArrayIndex)o;
        return this.array.equals(other.array) && this.index.equals(other.index);
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArrayIndex)) {
            return false;
        }
        ArrayIndex other = (ArrayIndex)o;
        if (!constraint.equivalent(this.array, other.array)) {
            return false;
        }
        if (constraint.equivalent(this.index, other.index)) return true;
        return false;
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        this.index = boxingRewriter.sugarUnboxing(this.index);
        return false;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }
}

