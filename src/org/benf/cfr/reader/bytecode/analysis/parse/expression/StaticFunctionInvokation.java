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
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.FunctionProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StaticFunctionInvokation
extends AbstractExpression
implements FunctionProcessor,
BoxingProcessor {
    private final ConstantPoolEntryMethodRef function;
    private final List<Expression> args;
    private final JavaTypeInstance clazz;

    private static InferredJavaType getTypeForFunction(ConstantPoolEntryMethodRef function, List<Expression> args) {
        InferredJavaType res = new InferredJavaType(function.getMethodPrototype().getReturnType(function.getClassEntry().getTypeInstance(), args), InferredJavaType.Source.FUNCTION, true);
        return res;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new StaticFunctionInvokation(this.function, cloneHelper.replaceOrClone(this.args));
    }

    public StaticFunctionInvokation(ConstantPoolEntryMethodRef function, List<Expression> args) {
        super(StaticFunctionInvokation.getTypeForFunction(function, args));
        this.function = function;
        this.args = args;
        this.clazz = function.getClassEntry().getTypeInstance();
    }

    private StaticFunctionInvokation(JavaTypeInstance clazz, InferredJavaType res, List<Expression> args) {
        super(res);
        this.function = null;
        this.args = args;
        this.clazz = clazz;
    }

    public static StaticFunctionInvokation createMatcher(JavaTypeInstance clazz, InferredJavaType res, List<Expression> args) {
        return new StaticFunctionInvokation(clazz, res, args);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.clazz);
        for (Expression arg : this.args) {
            arg.collectTypeUsages(collector);
        }
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

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.dump(this.clazz).print(".");
        ConstantPoolEntryNameAndType nameAndType = this.function.getNameAndTypeEntry();
        d.print(nameAndType.getName().getValue() + "(");
        boolean first = true;
        for (Expression arg : this.args) {
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
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        for (Expression expression : this.args) {
            expression.collectUsedLValues(lValueUsageCollector);
        }
    }

    public String getName() {
        ConstantPoolEntryNameAndType nameAndType = this.function.getNameAndTypeEntry();
        return nameAndType.getName().getValue();
    }

    public JavaTypeInstance getClazz() {
        return this.clazz;
    }

    public List<Expression> getArgs() {
        return this.args;
    }

    public ConstantPoolEntryMethodRef getFunction() {
        return this.function;
    }

    @Override
    public void rewriteVarArgs(VarArgsRewriter varArgsRewriter) {
        OverloadMethodSet overloadMethodSet;
        MethodPrototype methodPrototype = this.function.getMethodPrototype();
        if (!methodPrototype.isVarArgs()) {
            return;
        }
        if ((overloadMethodSet = this.function.getOverloadMethodSet()) == null) {
            return;
        }
        GenericTypeBinder gtb = methodPrototype.getTypeBinderFor(this.args);
        varArgsRewriter.rewriteVarArgsArg(overloadMethodSet, methodPrototype, this.getArgs(), gtb);
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        OverloadMethodSet overloadMethodSet = this.function.getOverloadMethodSet();
        if (overloadMethodSet == null) {
            return false;
        }
        for (int x = 0; x < this.args.size(); ++x) {
            Expression arg = this.args.get(x);
            arg = boxingRewriter.rewriteExpression(arg, (SSAIdentifiers)null, (StatementContainer)null, (ExpressionRewriterFlags)null);
            this.args.set(x, boxingRewriter.sugarParameterBoxing(arg, x, overloadMethodSet, null, this.function.getMethodPrototype()));
        }
        return true;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof StaticFunctionInvokation)) {
            return false;
        }
        StaticFunctionInvokation other = (StaticFunctionInvokation)o;
        if (!this.clazz.equals(other.clazz)) {
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
        if (!(o instanceof StaticFunctionInvokation)) {
            return false;
        }
        StaticFunctionInvokation other = (StaticFunctionInvokation)o;
        if (!constraint.equivalent(this.clazz, other.clazz)) {
            return false;
        }
        if (constraint.equivalent(this.args, other.args)) return true;
        return false;
    }
}

