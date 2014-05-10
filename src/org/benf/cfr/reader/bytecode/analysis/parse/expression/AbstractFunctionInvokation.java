/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.VarArgsRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpressionFallback;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.FunctionProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;

public abstract class AbstractFunctionInvokation
extends AbstractExpression
implements FunctionProcessor,
BoxingProcessor {
    private final ConstantPoolEntryMethodRef function;
    private Expression object;
    private final List<Expression> args;
    private final ConstantPool cp;
    private final MethodPrototype methodPrototype;

    public AbstractFunctionInvokation(ConstantPool cp, ConstantPoolEntryMethodRef function, MethodPrototype methodPrototype, Expression object, List<Expression> args) {
        super(new InferredJavaType(methodPrototype.getReturnType(object.getInferredJavaType().getJavaTypeInstance(), args), InferredJavaType.Source.FUNCTION, true));
        this.function = function;
        this.methodPrototype = methodPrototype;
        this.object = object;
        this.args = args;
        this.cp = cp;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        for (Expression arg : this.args) {
            arg.collectTypeUsages(collector);
        }
        this.methodPrototype.collectTypeUsages(collector);
        collector.collectFrom(this.object);
        super.collectTypeUsages(collector);
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.object = this.object.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        for (int x = 0; x < this.args.size(); ++x) {
            this.args.set(x, this.args.get(x).replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer));
        }
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.object = expressionRewriter.rewriteExpression(this.object, ssaIdentifiers, statementContainer, flags);
        for (int x = 0; x < this.args.size(); ++x) {
            this.args.set(x, expressionRewriter.rewriteExpression(this.args.get(x), ssaIdentifiers, statementContainer, flags));
        }
        return this;
    }

    public Expression getObject() {
        return this.object;
    }

    public ConstantPoolEntryMethodRef getFunction() {
        return this.function;
    }

    public JavaTypeInstance getClassTypeInstance() {
        return this.function.getClassEntry().getTypeInstance();
    }

    public List<Expression> getArgs() {
        return this.args;
    }

    public MethodPrototype getMethodPrototype() {
        return this.methodPrototype;
    }

    public Expression getAppropriatelyCastArgument(int idx) {
        return this.methodPrototype.getAppropriatelyCastedArgument(this.args.get(idx), idx);
    }

    public ConstantPool getCp() {
        return this.cp;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.object.collectUsedLValues(lValueUsageCollector);
        for (Expression expression : this.args) {
            expression.collectUsedLValues(lValueUsageCollector);
        }
    }

    private OverloadMethodSet getOverloadMethodSet() {
        JavaTypeInstance objectType;
        OverloadMethodSet overloadMethodSet = this.function.getOverloadMethodSet();
        if (overloadMethodSet == null) {
            return null;
        }
        if (!(objectType = this.object.getInferredJavaType().getJavaTypeInstance() instanceof JavaGenericRefTypeInstance)) return overloadMethodSet;
        JavaGenericRefTypeInstance genericType = (JavaGenericRefTypeInstance)objectType;
        return overloadMethodSet.specialiseTo(genericType);
    }

    public abstract String getName();

    @Override
    public void rewriteVarArgs(VarArgsRewriter varArgsRewriter) {
        OverloadMethodSet overloadMethodSet;
        if (!this.methodPrototype.isVarArgs()) {
            return;
        }
        if ((overloadMethodSet = this.getOverloadMethodSet()) == null) {
            return;
        }
        GenericTypeBinder gtb = this.methodPrototype.getTypeBinderFor(this.args);
        varArgsRewriter.rewriteVarArgsArg(overloadMethodSet, this.methodPrototype, this.args, gtb);
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        OverloadMethodSet overloadMethodSet;
        if (this.args.isEmpty()) {
            return false;
        }
        if ((overloadMethodSet = this.getOverloadMethodSet()) == null) {
            boxingRewriter.removeRedundantCastOnly(this.args);
            return false;
        }
        BindingSuperContainer bindingSuperContainer = this.object.getInferredJavaType().getJavaTypeInstance().getBindingSupers();
        GenericTypeBinder gtb = this.methodPrototype.getTypeBinderFor(this.args);
        boolean callsCorrectEntireMethod = overloadMethodSet.callsCorrectEntireMethod(this.args, gtb);
        for (int x = 0; x < this.args.size(); ++x) {
            Expression arg = this.args.get(x);
            if (!(callsCorrectEntireMethod || overloadMethodSet.callsCorrectMethod(arg, x, gtb))) {
                JavaTypeInstance argType = overloadMethodSet.getArgType(x, arg.getInferredJavaType().getJavaTypeInstance());
                boolean ignore = false;
                if (argType instanceof JavaGenericBaseInstance) {
                    ignore|=((JavaGenericBaseInstance)argType).hasForeignUnbound(this.cp);
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
            arg = boxingRewriter.sugarParameterBoxing(arg, x, overloadMethodSet, gtb, this.methodPrototype);
            this.args.set(x, arg);
        }
        return true;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.object = expressionRewriter.rewriteExpression(this.object, ssaIdentifiers, statementContainer, flags);
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return caught.checkAgainst(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof AbstractFunctionInvokation)) {
            return false;
        }
        AbstractFunctionInvokation other = (AbstractFunctionInvokation)o;
        if (!this.object.equals(other.object)) {
            return false;
        }
        if (this.args.equals(other.args)) return true;
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
        if (!(o instanceof AbstractFunctionInvokation)) {
            return false;
        }
        AbstractFunctionInvokation other = (AbstractFunctionInvokation)o;
        if (!constraint.equivalent(this.object, other.object)) {
            return false;
        }
        if (constraint.equivalent(this.args, other.args)) return true;
        return false;
    }
}

