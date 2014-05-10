/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpressionFallback;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.state.TypeUsageCollector;

public abstract class AbstractConstructorInvokation
extends AbstractExpression
implements BoxingProcessor {
    private final ConstantPoolEntryMethodRef function;
    private final MethodPrototype methodPrototype;
    private final List<Expression> args;

    public AbstractConstructorInvokation(InferredJavaType inferredJavaType, ConstantPoolEntryMethodRef function, List<Expression> args) {
        super(inferredJavaType);
        this.args = args;
        this.function = function;
        this.methodPrototype = function.getMethodPrototype();
    }

    protected AbstractConstructorInvokation(AbstractConstructorInvokation other, CloneHelper cloneHelper) {
        super(other.getInferredJavaType());
        this.args = cloneHelper.replaceOrClone(other.args);
        this.function = other.function;
        this.methodPrototype = other.methodPrototype;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.methodPrototype.collectTypeUsages(collector);
        for (Expression arg : this.args) {
            arg.collectTypeUsages(collector);
        }
        super.collectTypeUsages(collector);
    }

    public List<Expression> getArgs() {
        return this.args;
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        for (int x = 0; x < this.args.size(); ++x) {
            this.args.set(x, this.args.get(x).replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer));
        }
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        for (int x = 0; x < this.args.size(); ++x) {
            this.args.set(x, expressionRewriter.rewriteExpression(this.args.get(x), ssaIdentifiers, statementContainer, flags));
        }
        return this;
    }

    public JavaTypeInstance getTypeInstance() {
        return this.getInferredJavaType().getJavaTypeInstance();
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        for (Expression expression : this.args) {
            expression.collectUsedLValues(lValueUsageCollector);
        }
    }

    @Override
    public boolean equals(Object o) {
        AbstractConstructorInvokation other;
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof AbstractConstructorInvokation)) {
            return false;
        }
        if (!this.getTypeInstance().equals((other = (AbstractConstructorInvokation)o).getTypeInstance())) {
            return false;
        }
        if (this.args.equals(other.args)) return true;
        return false;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        AbstractConstructorInvokation other;
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof AbstractConstructorInvokation)) {
            return false;
        }
        if (!constraint.equivalent(this.getTypeInstance(), (other = (AbstractConstructorInvokation)o).getTypeInstance())) {
            return false;
        }
        if (constraint.equivalent(this.args, other.args)) return true;
        return false;
    }

    protected final OverloadMethodSet getOverloadMethodSet() {
        JavaTypeInstance objectType;
        OverloadMethodSet overloadMethodSet = this.function.getOverloadMethodSet();
        if (overloadMethodSet == null) {
            return null;
        }
        if (!(objectType = this.getInferredJavaType().getJavaTypeInstance() instanceof JavaGenericRefTypeInstance)) return overloadMethodSet;
        JavaGenericRefTypeInstance genericType = (JavaGenericRefTypeInstance)objectType;
        return overloadMethodSet.specialiseTo(genericType);
    }

    protected final MethodPrototype getMethodPrototype() {
        return this.methodPrototype;
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        OverloadMethodSet overloadMethodSet;
        List<Expression> args = this.getArgs();
        if (args.isEmpty()) {
            return false;
        }
        if ((overloadMethodSet = this.getOverloadMethodSet()) == null) {
            boxingRewriter.removeRedundantCastOnly(args);
            return false;
        }
        for (int x = 0; x < args.size(); ++x) {
            Expression arg = args.get(x);
            if (!overloadMethodSet.callsCorrectMethod(arg, x, null)) {
                JavaTypeInstance argType = overloadMethodSet.getArgType(x, arg.getInferredJavaType().getJavaTypeInstance());
                boolean ignore = false;
                if (argType instanceof JavaGenericBaseInstance) {
                    ignore = ((JavaGenericBaseInstance)argType).hasForeignUnbound(this.function.getCp());
                }
                if (!ignore) {
                    ignore|=arg instanceof LambdaExpression;
                    ignore|=arg instanceof LambdaExpressionFallback;
                }
                if (!ignore) {
                    arg = new CastExpression(new InferredJavaType(argType, InferredJavaType.Source.EXPRESSION, true), arg);
                }
            }
            arg = boxingRewriter.rewriteExpression(arg, (SSAIdentifiers)null, (StatementContainer)null, (ExpressionRewriterFlags)null);
            arg = boxingRewriter.sugarParameterBoxing(arg, x, overloadMethodSet, null, this.methodPrototype);
            args.set(x, arg);
        }
        return true;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }
}

