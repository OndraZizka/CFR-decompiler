/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.Op04Rewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.DynamicInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpressionFallback;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.expression.StructuredStatementExpression;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
import org.benf.cfr.reader.bytecode.analysis.types.DynamicInvokeType;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.entities.AccessFlagMethod;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.bootstrap.MethodHandleBehaviour;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.lambda.LambdaUtils;

public class LambdaRewriter
implements Op04Rewriter,
ExpressionRewriter {
    private final DCCommonState state;
    private final ClassFile thisClassFile;
    private final JavaTypeInstance typeInstance;

    public LambdaRewriter(DCCommonState state, ClassFile thisClassFile) {
        this.state = state;
        this.thisClassFile = thisClassFile;
        this.typeInstance = thisClassFile.getClassType().getDeGenerifiedType();
    }

    @Override
    public void rewrite(Op04StructuredStatement root) {
        List<StructuredStatement> structuredStatements = MiscStatementTools.linearise(root);
        if (structuredStatements == null) {
            return;
        }
        for (StructuredStatement statement : structuredStatements) {
            statement.rewriteExpressions(this);
        }
    }

    @Override
    public void handleStatement(StatementContainer statementContainer) {
    }

    @Override
    public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression child;
        Expression res;
        if (expression instanceof DynamicInvokation) {
            return this.rewriteDynamicExpression((DynamicInvokation)expression);
        }
        if (!(res = expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags) instanceof CastExpression) || !(child = ((CastExpression)res).getChild() instanceof LambdaExpression) && !(child instanceof LambdaExpressionFallback)) return res;
        return child;
    }

    @Override
    public ConditionalExpression rewriteExpression(ConditionalExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression res = expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
        return (ConditionalExpression)res;
    }

    @Override
    public AbstractAssignmentExpression rewriteExpression(AbstractAssignmentExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression res = expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
        return (AbstractAssignmentExpression)res;
    }

    @Override
    public LValue rewriteExpression(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return lValue;
    }

    @Override
    public StackSSALabel rewriteExpression(StackSSALabel lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return lValue;
    }

    private Expression rewriteDynamicExpression(DynamicInvokation dynamicExpression) {
        List<Expression> curriedArgs = dynamicExpression.getDynamicArgs();
        Expression functionCall = dynamicExpression.getInnerInvokation();
        if (!(functionCall instanceof StaticFunctionInvokation)) return dynamicExpression;
        return this.rewriteDynamicExpression(dynamicExpression, (StaticFunctionInvokation)functionCall, curriedArgs);
    }

    private static LocalVariable getLocalVariable(Expression e) {
        LValueExpression lValueExpression;
        LValue lValue;
        if (!(e instanceof LValueExpression)) {
            throw new CannotDelambaException(null);
        }
        if (lValue = (lValueExpression = (LValueExpression)e).getLValue() instanceof LocalVariable) return (LocalVariable)lValue;
        throw new CannotDelambaException(null);
    }

    private Expression rewriteDynamicExpression(Expression dynamicExpression, StaticFunctionInvokation functionInvokation, List<Expression> curriedArgs) {
        String functionName;
        JavaRefTypeInstance lambdaTypeRefLocation;
        DynamicInvokeType dynamicInvokeType;
        ClassFile classFile;
        List<Expression> metaFactoryArgs;
        JavaTypeInstance typeInstance = functionInvokation.getClazz();
        if (!typeInstance.getRawName().equals("java.lang.invoke.LambdaMetafactory")) {
            return dynamicExpression;
        }
        if ((dynamicInvokeType = DynamicInvokeType.lookup(functionName = functionInvokation.getName())) == DynamicInvokeType.UNKNOWN) {
            return dynamicExpression;
        }
        if ((metaFactoryArgs = functionInvokation.getArgs()).size() != 6) {
            return dynamicExpression;
        }
        Expression arg = metaFactoryArgs.get(3);
        List<JavaTypeInstance> targetFnArgTypes = LambdaUtils.getLiteralProto(arg).getArgs();
        ConstantPoolEntryMethodHandle lambdaFnHandle = LambdaUtils.getHandle(metaFactoryArgs.get(4));
        ConstantPoolEntryMethodRef lambdaMethRef = lambdaFnHandle.getMethodRef();
        JavaTypeInstance lambdaTypeLocation = lambdaMethRef.getClassEntry().getTypeInstance();
        MethodPrototype lambdaFn = lambdaMethRef.getMethodPrototype();
        String lambdaFnName = lambdaFn.getName();
        List<JavaTypeInstance> lambdaFnArgTypes = lambdaFn.getArgs();
        if (!(lambdaTypeLocation instanceof JavaRefTypeInstance)) {
            return dynamicExpression;
        }
        if (!this.typeInstance.equals(lambdaTypeRefLocation = (JavaRefTypeInstance)lambdaTypeLocation)) {
            try {
                classFile = this.state.getClassFile(lambdaTypeRefLocation);
            }
            catch (CannotLoadClassException e) {
                return dynamicExpression;
            }
        } else {
            classFile = this.thisClassFile;
        }
        if (classFile == null) {
            return dynamicExpression;
        }
        boolean instance = false;
        switch (lambdaFnHandle.getReferenceKind()) {
            case INVOKE_INTERFACE: 
            case INVOKE_SPECIAL: 
            case INVOKE_VIRTUAL: {
                instance = true;
            }
        }
        if (curriedArgs.size() + targetFnArgTypes.size() - (instance ? 1 : 0) != lambdaFnArgTypes.size()) {
            throw new IllegalStateException("Bad argument counts!");
        }
        Method lambdaMethod = null;
        try {
            lambdaMethod = classFile.getMethodByPrototype(lambdaFn);
        }
        catch (NoSuchMethodException e) {
            return dynamicExpression;
        }
        int len = curriedArgs.size();
        for (int x = 0; x < len; ++x) {
            curriedArgs.set(x, CastExpression.removeImplicit(curriedArgs.get(x)));
        }
        if (!this.typeInstance.equals(lambdaTypeRefLocation) || !lambdaMethod.testAccessFlag(AccessFlagMethod.ACC_SYNTHETIC)) return new LambdaExpressionFallback(lambdaTypeRefLocation, dynamicExpression.getInferredJavaType(), lambdaFnName, targetFnArgTypes, curriedArgs, instance);
        try {
            List<StructuredStatement> structuredLambdaStatements;
            Op04StructuredStatement lambdaCode = lambdaMethod.getAnalysis();
            int nLambdaArgs = targetFnArgTypes.size();
            List replacementParameters = ListFactory.newList();
            int n = instance ? 1 : 0;
            int m = curriedArgs.size();
            for (int n2 = v23952; n2 < m; ++n2) {
                replacementParameters.add((LocalVariable)LambdaRewriter.getLocalVariable(curriedArgs.get(n2)));
            }
            List anonymousLambdaArgs = ListFactory.newList();
            List<LocalVariable> originalParameters = lambdaMethod.getMethodPrototype().getComputedParameters();
            int offset = replacementParameters.size();
            for (int n3 = 0; n3 < nLambdaArgs; ++n3) {
                LocalVariable original = originalParameters.get(n3 + offset);
                String name = original.getName().getStringName();
                LocalVariable tmp = new LocalVariable(name, new InferredJavaType(targetFnArgTypes.get(n3), InferredJavaType.Source.EXPRESSION));
                anonymousLambdaArgs.add((LocalVariable)tmp);
                replacementParameters.add((LocalVariable)tmp);
            }
            if (originalParameters.size() != replacementParameters.size()) {
                throw new CannotDelambaException(null);
            }
            Map rewrites = MapFactory.newMap();
            for (int x2 = 0; x2 < originalParameters.size(); ++x2) {
                rewrites.put((LocalVariable)originalParameters.get(x2), replacementParameters.get(x2));
            }
            if ((structuredLambdaStatements = MiscStatementTools.linearise(lambdaCode)) == null) {
                throw new CannotDelambaException(null);
            }
            LambdaInternalRewriter variableRenamer = new LambdaInternalRewriter(rewrites);
            for (StructuredStatement lambdaStatement : structuredLambdaStatements) {
                lambdaStatement.rewriteExpressions(variableRenamer);
            }
            StructuredStatement lambdaStatement2 = lambdaCode.getStatement();
            if (structuredLambdaStatements.size() == 3 && structuredLambdaStatements.get(1) instanceof StructuredReturn) {
                StructuredReturn structuredReturn = (StructuredReturn)structuredLambdaStatements.get(1);
                lambdaStatement2 = new StructuredExpressionStatement(structuredReturn.getValue(), true);
            }
            lambdaMethod.hideSynthetic();
            return new LambdaExpression(dynamicExpression.getInferredJavaType(), anonymousLambdaArgs, new StructuredStatementExpression(new InferredJavaType(lambdaMethod.getMethodPrototype().getReturnType(), InferredJavaType.Source.EXPRESSION), lambdaStatement2));
        }
        catch (CannotDelambaException e) {
            // empty catch block
        }
        return new LambdaExpressionFallback(lambdaTypeRefLocation, dynamicExpression.getInferredJavaType(), lambdaFnName, targetFnArgTypes, curriedArgs, instance);
    }

    public static class LambdaInternalRewriter
    implements ExpressionRewriter {
        private final Map<LValue, LValue> rewrites;

        public LambdaInternalRewriter(Map<LValue, LValue> rewrites) {
            this.rewrites = rewrites;
        }

        @Override
        public void handleStatement(StatementContainer statementContainer) {
        }

        @Override
        public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        }

        @Override
        public ConditionalExpression rewriteExpression(ConditionalExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            Expression res = expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
            return (ConditionalExpression)res;
        }

        @Override
        public AbstractAssignmentExpression rewriteExpression(AbstractAssignmentExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            Expression res = expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
            return (AbstractAssignmentExpression)res;
        }

        @Override
        public LValue rewriteExpression(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            LValue replacement = this.rewrites.get(lValue);
            return replacement == null ? lValue : replacement;
        }

        @Override
        public StackSSALabel rewriteExpression(StackSSALabel lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return lValue;
        }
    }

    static class CannotDelambaException
    extends IllegalStateException {
        private CannotDelambaException() {
        }

        /* synthetic */ CannotDelambaException(1 x0) {
            this();
        }
    }

}

