/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class NewObjectArray
extends AbstractNewArray {
    private List<Expression> dimSizes;
    private final JavaTypeInstance allocatedType;
    private final JavaTypeInstance resultType;
    private final int numDims;

    public NewObjectArray(List<Expression> dimSizes, JavaTypeInstance resultInstance) {
        super(new InferredJavaType(resultInstance, InferredJavaType.Source.EXPRESSION, true));
        this.dimSizes = dimSizes;
        this.allocatedType = resultInstance.getArrayStrippedType();
        this.resultType = resultInstance;
        this.numDims = resultInstance.getNumArrayDimensions();
        for (Expression size : dimSizes) {
            size.getInferredJavaType().useAsWithoutCasting(RawJavaType.INT);
        }
    }

    private NewObjectArray(InferredJavaType inferredJavaType, JavaTypeInstance resultType, int numDims, JavaTypeInstance allocatedType, List<Expression> dimSizes) {
        super(inferredJavaType);
        this.resultType = resultType;
        this.numDims = numDims;
        this.allocatedType = allocatedType;
        this.dimSizes = dimSizes;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new NewObjectArray(this.getInferredJavaType(), this.resultType, this.numDims, this.allocatedType, cloneHelper.replaceOrClone(this.dimSizes));
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.allocatedType);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.print("new ").dump(this.allocatedType);
        for (Expression dimSize : this.dimSizes) {
            d.print("[").dump(dimSize).print("]");
        }
        for (int x = this.dimSizes.size(); x < this.numDims; ++x) {
            d.print("[]");
        }
        return d;
    }

    @Override
    public int getNumDims() {
        return this.numDims;
    }

    @Override
    public int getNumSizedDims() {
        return this.dimSizes.size();
    }

    @Override
    public Expression getDimSize(int dim) {
        if (dim < this.dimSizes.size()) return this.dimSizes.get(dim);
        throw new ConfusedCFRException("Out of bounds");
    }

    @Override
    public JavaTypeInstance getInnerType() {
        return this.resultType;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        for (int x = 0; x < this.dimSizes.size(); ++x) {
            this.dimSizes.set(x, this.dimSizes.get(x).replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer));
        }
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        for (int x = 0; x < this.dimSizes.size(); ++x) {
            this.dimSizes.set(x, expressionRewriter.rewriteExpression(this.dimSizes.get(x), ssaIdentifiers, statementContainer, flags));
        }
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        NewObjectArray that = (NewObjectArray)o;
        if (this.numDims != that.numDims) {
            return false;
        }
        if (this.allocatedType != null ? !this.allocatedType.equals(that.allocatedType) : that.allocatedType != null) {
            return false;
        }
        if (this.dimSizes != null ? !this.dimSizes.equals(that.dimSizes) : that.dimSizes != null) {
            return false;
        }
        if (!(this.resultType != null ? !this.resultType.equals(that.resultType) : that.resultType != null)) return true;
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
        NewObjectArray other = (NewObjectArray)o;
        if (this.numDims != other.numDims) {
            return false;
        }
        if (!constraint.equivalent(this.dimSizes, other.dimSizes)) {
            return false;
        }
        if (!constraint.equivalent(this.allocatedType, other.allocatedType)) {
            return false;
        }
        if (constraint.equivalent(this.resultType, other.resultType)) return true;
        return false;
    }
}

