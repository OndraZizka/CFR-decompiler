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
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class CastExpression
extends AbstractExpression
implements BoxingProcessor {
    private Expression child;

    public CastExpression(InferredJavaType knownType, Expression child) {
        super(knownType);
        this.child = child;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new CastExpression(this.getInferredJavaType(), cloneHelper.replaceOrClone(this.child));
    }

    public boolean couldBeImplicit(GenericTypeBinder gtb) {
        JavaTypeInstance childType = this.child.getInferredJavaType().getJavaTypeInstance();
        JavaTypeInstance tgtType = this.getInferredJavaType().getJavaTypeInstance();
        return childType.implicitlyCastsTo(tgtType, gtb);
    }

    public boolean couldBeImplicit(JavaTypeInstance tgtType, GenericTypeBinder gtb) {
        JavaTypeInstance childType = this.child.getInferredJavaType().getJavaTypeInstance();
        return childType.implicitlyCastsTo(tgtType, gtb);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.getInferredJavaType().getJavaTypeInstance());
        this.child.collectTypeUsages(collector);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.UNARY_OTHER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        JavaTypeInstance castType = this.getInferredJavaType().getJavaTypeInstance();
        if (!(this.child.getInferredJavaType().getJavaTypeInstance() != RawJavaType.BOOLEAN || RawJavaType.BOOLEAN.implicitlyCastsTo(castType, null))) {
            d.print("(").dump(castType).print(")");
            this.child.dumpWithOuterPrecedence(d, this.getPrecedence());
            d.print(" ? 1 : 0");
        } else if (castType == RawJavaType.NULL) {
            this.child.dumpWithOuterPrecedence(d, this.getPrecedence());
        } else {
            d.print("(").dump(castType).print(")");
            this.child.dumpWithOuterPrecedence(d, this.getPrecedence());
        }
        return d;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.child = this.child.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.child = expressionRewriter.rewriteExpression(this.child, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.child.collectUsedLValues(lValueUsageCollector);
    }

    public Expression getChild() {
        return this.child;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof CastExpression) return this.child.equals(((CastExpression)o).child);
        return false;
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        Expression newchild;
        while (this.child instanceof CastExpression) {
            JavaTypeInstance childType;
            JavaTypeInstance grandChildType;
            CastExpression childCast = (CastExpression)this.child;
            JavaTypeInstance thisType = this.getInferredJavaType().getJavaTypeInstance();
            if ((grandChildType = childCast.child.getInferredJavaType().getJavaTypeInstance()).implicitlyCastsTo(childType = childCast.getInferredJavaType().getJavaTypeInstance(), null) && childType.implicitlyCastsTo(thisType, null)) {
                this.child = childCast.child;
                continue;
            }
            if (!(grandChildType instanceof RawJavaType) || !(childType instanceof RawJavaType) || !(thisType instanceof RawJavaType) || grandChildType.implicitlyCastsTo(childType, null) || childType.implicitlyCastsTo(thisType, null)) break;
            this.child = childCast.child;
        }
        if (!(newchild = boxingRewriter.sugarNonParameterBoxing(this.child, this.getInferredJavaType().getJavaTypeInstance())).getInferredJavaType().getJavaTypeInstance().implicitlyCastsTo(this.child.getInferredJavaType().getJavaTypeInstance(), null)) return false;
        this.child = newchild;
        return false;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        CastExpression other;
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        if (!constraint.equivalent(this.getInferredJavaType().getJavaTypeInstance(), (other = (CastExpression)o).getInferredJavaType().getJavaTypeInstance())) {
            return false;
        }
        if (constraint.equivalent(this.child, other.child)) return true;
        return false;
    }

    public static Expression removeImplicit(Expression e) {
        while (e instanceof CastExpression && ((CastExpression)e).couldBeImplicit(null)) {
            e = ((CastExpression)e).getChild();
        }
        return e;
    }

    public static Expression removeImplicitOuterType(Expression e, GenericTypeBinder gtb, boolean rawArg) {
        JavaTypeInstance t = e.getInferredJavaType().getJavaTypeInstance();
        while (e instanceof CastExpression && ((CastExpression)e).couldBeImplicit(gtb) && ((CastExpression)e).couldBeImplicit(t, gtb)) {
            Expression newE = ((CastExpression)e).getChild();
            if (!rawArg) {
                boolean wasRaw = e.getInferredJavaType().getJavaTypeInstance() instanceof RawJavaType;
                boolean isRaw = newE.getInferredJavaType().getJavaTypeInstance() instanceof RawJavaType;
                if (wasRaw && wasRaw != isRaw) return e;
            }
            e = newE;
        }
        return e;
    }
}

