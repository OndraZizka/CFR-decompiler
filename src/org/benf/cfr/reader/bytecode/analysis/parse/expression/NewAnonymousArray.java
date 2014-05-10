/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
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
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class NewAnonymousArray
extends AbstractNewArray
implements BoxingProcessor {
    private JavaTypeInstance allocatedType;
    private int numDims;
    private List<Expression> values = ListFactory.newList();
    private boolean isCompletelyAnonymous = false;

    public NewAnonymousArray(InferredJavaType type, int numDims, List<Expression> values, boolean isCompletelyAnonymous) {
        super(type);
        this.numDims = numDims;
        this.allocatedType = type.getJavaTypeInstance().getArrayStrippedType();
        if (this.allocatedType instanceof RawJavaType) {
            for (Expression value : values) {
                value.getInferredJavaType().useAsWithoutCasting((RawJavaType)this.allocatedType);
            }
        }
        for (Expression value : values) {
            if (value instanceof NewAnonymousArray) {
                NewAnonymousArray newAnonymousArrayInner = (NewAnonymousArray)value;
                newAnonymousArrayInner.isCompletelyAnonymous = true;
            }
            this.values.add(value);
        }
        this.isCompletelyAnonymous = isCompletelyAnonymous;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.allocatedType);
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.values);
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        for (int i = 0; i < this.values.size(); ++i) {
            this.values.set(i, boxingRewriter.sugarNonParameterBoxing(this.values.get(i), this.allocatedType));
        }
        return false;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new NewAnonymousArray(this.getInferredJavaType(), this.numDims, cloneHelper.replaceOrClone(this.values), this.isCompletelyAnonymous);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        if (!this.isCompletelyAnonymous) {
            d.print("new ").dump(this.allocatedType);
            for (int x = 0; x < this.numDims; ++x) {
                d.print("[]");
            }
        }
        d.print("{");
        boolean first = true;
        for (Expression value : this.values) {
            first = CommaHelp.comma(first, d);
            d.dump(value);
        }
        d.print("}");
        return d;
    }

    public List<Expression> getValues() {
        return this.values;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        for (int x = 0; x < this.values.size(); ++x) {
            this.values.set(x, this.values.get(x).replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer));
        }
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        for (int x = 0; x < this.values.size(); ++x) {
            this.values.set(x, expressionRewriter.rewriteExpression(this.values.get(x), ssaIdentifiers, statementContainer, flags));
        }
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public int getNumDims() {
        return this.numDims;
    }

    @Override
    public int getNumSizedDims() {
        return 0;
    }

    @Override
    public Expression getDimSize(int dim) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JavaTypeInstance getInnerType() {
        return this.allocatedType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        NewAnonymousArray that = (NewAnonymousArray)o;
        if (this.isCompletelyAnonymous != that.isCompletelyAnonymous) {
            return false;
        }
        if (this.numDims != that.numDims) {
            return false;
        }
        if (this.allocatedType != null ? !this.allocatedType.equals(that.allocatedType) : that.allocatedType != null) {
            return false;
        }
        if (!(this.values != null ? !this.values.equals(that.values) : that.values != null)) return true;
        return false;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        NewAnonymousArray other = (NewAnonymousArray)o;
        if (this.isCompletelyAnonymous != other.isCompletelyAnonymous) {
            return false;
        }
        if (this.numDims != other.numDims) {
            return false;
        }
        if (!constraint.equivalent(this.allocatedType, other.allocatedType)) {
            return false;
        }
        if (constraint.equivalent(this.values, other.values)) return true;
        return false;
    }
}

