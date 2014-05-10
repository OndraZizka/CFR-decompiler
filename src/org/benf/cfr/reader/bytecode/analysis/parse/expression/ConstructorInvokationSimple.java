/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.VarArgsRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractConstructorInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.FunctionProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.SentinelLocalClassLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstructorInvokationSimple
extends AbstractConstructorInvokation
implements FunctionProcessor {
    private final MemberFunctionInvokation constructorInvokation;

    public ConstructorInvokationSimple(MemberFunctionInvokation constructorInvokation, InferredJavaType inferredJavaType, List<Expression> args) {
        super(inferredJavaType, constructorInvokation.getFunction(), args);
        this.constructorInvokation = constructorInvokation;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ConstructorInvokationSimple(this.constructorInvokation, this.getInferredJavaType(), cloneHelper.replaceOrClone(this.getArgs()));
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        JavaTypeInstance clazz = super.getTypeInstance();
        InnerClassInfo innerClassInfo = clazz.getInnerClassHereInfo();
        List args = this.getArgs();
        d.print("new ").dump(clazz).print("(");
        boolean first = true;
        int n = innerClassInfo.isHideSyntheticThis() ? 1 : 0;
        for (int i = start = (v32461); i < args.size(); ++i) {
            Expression arg = (Expression)args.get(i);
            if (!first) {
                d.print(", ");
            }
            first = false;
            d.dump(arg);
        }
        d.print(")");
        return d;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof ConstructorInvokationSimple) return super.equals(o);
        return false;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        JavaTypeInstance lValueType = this.constructorInvokation.getClassTypeInstance();
        InnerClassInfo innerClassInfo = lValueType.getInnerClassHereInfo();
        if (innerClassInfo.isMethodScopedClass() && !innerClassInfo.isAnonymousClass()) {
            lValueUsageCollector.collect(new SentinelLocalClassLValue(lValueType));
        }
        super.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (!(o instanceof ConstructorInvokationSimple)) {
            return false;
        }
        if (super.equivalentUnder(o, constraint)) return true;
        return false;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return caught.checkAgainst(this.constructorInvokation);
    }

    @Override
    public void rewriteVarArgs(VarArgsRewriter varArgsRewriter) {
        OverloadMethodSet overloadMethodSet;
        MethodPrototype methodPrototype = this.getMethodPrototype();
        if (!methodPrototype.isVarArgs()) {
            return;
        }
        if ((overloadMethodSet = this.getOverloadMethodSet()) == null) {
            return;
        }
        GenericTypeBinder gtb = methodPrototype.getTypeBinderFor(this.getArgs());
        varArgsRewriter.rewriteVarArgsArg(overloadMethodSet, methodPrototype, this.getArgs(), gtb);
    }
}

