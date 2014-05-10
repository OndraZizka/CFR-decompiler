/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ArrayType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class NewPrimitiveArray
extends AbstractNewArray {
    private Expression size;
    private final JavaTypeInstance type;

    public NewPrimitiveArray(Expression size, byte type) {
        this(size, ArrayType.getArrayType(type).getJavaTypeInstance());
    }

    public NewPrimitiveArray(Expression size, JavaTypeInstance type) {
        super(new InferredJavaType(new JavaArrayTypeInstance(1, type), InferredJavaType.Source.EXPRESSION));
        this.size = size;
        this.type = type;
        size.getInferredJavaType().useAsWithoutCasting(RawJavaType.INT);
    }

    private NewPrimitiveArray(InferredJavaType inferredJavaType, JavaTypeInstance type, Expression size) {
        super(inferredJavaType);
        this.type = type;
        this.size = size;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.size.collectTypeUsages(collector);
        collector.collect(this.type);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new NewPrimitiveArray(this.getInferredJavaType(), this.type, cloneHelper.replaceOrClone(this.size));
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return d.print("new " + this.type + "[").dump(this.size).print("]");
    }

    @Override
    public int getNumDims() {
        return 1;
    }

    @Override
    public int getNumSizedDims() {
        return 1;
    }

    @Override
    public Expression getDimSize(int dim) {
        if (dim <= 0) return this.size;
        throw new ConfusedCFRException("Only 1 dimension for primitive arrays!");
    }

    @Override
    public JavaTypeInstance getInnerType() {
        return this.type;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.size = this.size.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.size = expressionRewriter.rewriteExpression(this.size, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof NewPrimitiveArray)) {
            return false;
        }
        NewPrimitiveArray other = (NewPrimitiveArray)o;
        if (!this.size.equals(other.size)) {
            return false;
        }
        if (this.type.equals(other.type)) return true;
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
        NewPrimitiveArray other = (NewPrimitiveArray)o;
        if (!constraint.equivalent(this.size, other.size)) {
            return false;
        }
        if (constraint.equivalent(this.type, other.type)) return true;
        return false;
    }
}

