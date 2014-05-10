/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.BoxingHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;

public class PrimitiveBoxingRewriter
implements StructuredStatementTransformer,
ExpressionRewriter {
    @Override
    public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
        in.transformStructuredChildren(this, scope);
        in.rewriteExpressions(this);
        return in;
    }

    @Override
    public void handleStatement(StatementContainer statementContainer) {
        Object statement = statementContainer.getStatement();
        if (!(statement instanceof BoxingProcessor)) return;
        ((BoxingProcessor)statement).rewriteBoxing(this);
    }

    @Override
    public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        BoxingProcessor boxingProcessor;
        if (expression instanceof BoxingProcessor && (boxingProcessor = (BoxingProcessor)expression).rewriteBoxing(this)) {
            boxingProcessor.applyNonArgExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
            return expression;
        }
        expression = expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        return expression;
    }

    @Override
    public ConditionalExpression rewriteExpression(ConditionalExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        BoxingProcessor boxingProcessor;
        if (expression instanceof BoxingProcessor && (boxingProcessor = (BoxingProcessor)expression).rewriteBoxing(this)) {
            boxingProcessor.applyNonArgExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
            return expression;
        }
        Expression res = expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
        return (ConditionalExpression)res;
    }

    @Override
    public AbstractAssignmentExpression rewriteExpression(AbstractAssignmentExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        if (expression instanceof BoxingProcessor) {
            ((BoxingProcessor)expression).rewriteBoxing(this);
        }
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

    public Expression sugarParameterBoxing(Expression in, int argIdx, OverloadMethodSet possibleMethods, GenericTypeBinder gtb, MethodPrototype methodPrototype) {
        Expression res = in;
        InferredJavaType outerCastType = null;
        Expression res1 = null;
        if (in instanceof CastExpression) {
            List<JavaTypeInstance> argTypes;
            boolean wasRaw = true;
            if (methodPrototype != null && argIdx <= (argTypes = methodPrototype.getArgs()).size() - 1) {
                wasRaw = argTypes.get(argIdx) instanceof RawJavaType;
            }
            outerCastType = in.getInferredJavaType();
            res1 = res = CastExpression.removeImplicitOuterType(res, gtb, wasRaw);
        }
        if (res instanceof MemberFunctionInvokation) {
            res = BoxingHelper.sugarUnboxing((MemberFunctionInvokation)res);
        } else if (res instanceof StaticFunctionInvokation) {
            res = BoxingHelper.sugarBoxing((StaticFunctionInvokation)res);
        }
        if (res == in) {
            return in;
        }
        if (possibleMethods.callsCorrectMethod(res, argIdx, gtb)) return res;
        if (outerCastType != null && res.getInferredJavaType().getJavaTypeInstance().canCastTo(outerCastType.getJavaTypeInstance(), gtb) && possibleMethods.callsCorrectMethod(res = new CastExpression(outerCastType, res), argIdx, gtb)) {
            return res;
        }
        if (res1 == null || !possibleMethods.callsCorrectMethod(res1, argIdx, gtb)) return in;
        return res1;
    }

    public void removeRedundantCastOnly(List<Expression> mutableIn) {
        int len = mutableIn.size();
        for (int x = 0; x < len; ++x) {
            mutableIn.set(x, this.removeRedundantCastOnly(mutableIn.get(x)));
        }
    }

    public Expression removeRedundantCastOnly(Expression in) {
        JavaTypeInstance childType;
        if (!(in instanceof CastExpression)) return in;
        JavaTypeInstance castType = in.getInferredJavaType().getJavaTypeInstance();
        if (!castType.equals(childType = ((CastExpression)in).getChild().getInferredJavaType().getJavaTypeInstance())) return in;
        return this.removeRedundantCastOnly(((CastExpression)in).getChild());
    }

    public Expression sugarNonParameterBoxing(Expression in, JavaTypeInstance tgtType) {
        CastExpression cast;
        boolean expectingPrim = tgtType instanceof RawJavaType;
        Expression res = in;
        boolean recast = false;
        if (in instanceof CastExpression && ((CastExpression)in).couldBeImplicit(null)) {
            res = ((CastExpression)in).getChild();
            recast = !(tgtType instanceof RawJavaType);
        } else if (in instanceof MemberFunctionInvokation) {
            res = BoxingHelper.sugarUnboxing((MemberFunctionInvokation)in);
        } else if (in instanceof StaticFunctionInvokation) {
            res = BoxingHelper.sugarBoxing((StaticFunctionInvokation)in);
        }
        if (res == in) {
            return in;
        }
        if (!res.getInferredJavaType().getJavaTypeInstance().implicitlyCastsTo(in.getInferredJavaType().getJavaTypeInstance(), null)) {
            return in;
        }
        if (!res.getInferredJavaType().getJavaTypeInstance().canCastTo(tgtType, null)) {
            return in;
        }
        res = this.sugarNonParameterBoxing(res, tgtType);
        if (!recast || !((cast = (CastExpression)in).getInferredJavaType().getJavaTypeInstance() instanceof RawJavaType) || !(res.getInferredJavaType().getJavaTypeInstance() instanceof JavaRefTypeInstance)) return res;
        res = new CastExpression(cast.getInferredJavaType(), res);
        return res;
    }

    public Expression sugarUnboxing(Expression in) {
        if (!(in instanceof MemberFunctionInvokation)) return in;
        return BoxingHelper.sugarUnboxing((MemberFunctionInvokation)in);
    }

    public boolean isUnboxedType(Expression in) {
        JavaTypeInstance type = in.getInferredJavaType().getJavaTypeInstance();
        if (!(type instanceof RawJavaType)) {
            return false;
        }
        if (in instanceof AbstractFunctionInvokation) {
            return false;
        }
        RawJavaType rawJavaType = (RawJavaType)type;
        return rawJavaType.isUsableType();
    }
}

