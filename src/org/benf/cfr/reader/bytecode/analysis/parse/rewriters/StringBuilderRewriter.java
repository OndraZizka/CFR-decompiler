/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.rewriters;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ExplicitBraceExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class StringBuilderRewriter
implements ExpressionRewriter {
    private final boolean stringBuilderEnabled;
    private final boolean stringBufferEnabled;

    public StringBuilderRewriter(Options options, ClassFileVersion classFileVersion) {
        this.stringBufferEnabled = options.getOption(OptionsImpl.SUGAR_STRINGBUFFER, classFileVersion);
        this.stringBuilderEnabled = options.getOption(OptionsImpl.SUGAR_STRINGBUILDER, classFileVersion);
    }

    @Override
    public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression lhs;
        MemberFunctionInvokation memberFunctionInvokation;
        Expression result;
        if (!(expression instanceof MemberFunctionInvokation) || !"toString".equals((memberFunctionInvokation = (MemberFunctionInvokation)expression).getName()) || (result = this.testAppendChain(lhs = memberFunctionInvokation.getObject())) == null) return expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        return result;
    }

    @Override
    public void handleStatement(StatementContainer statementContainer) {
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

    private Expression testAppendChain(Expression lhs) {
        List reverseAppendChain = ListFactory.newList();
        do {
            if (lhs instanceof MemberFunctionInvokation) {
                Expression e;
                MemberFunctionInvokation memberFunctionInvokation;
                if (!(memberFunctionInvokation = (MemberFunctionInvokation)lhs).getName().equals("append") || memberFunctionInvokation.getArgs().size() != 1) return null;
                lhs = memberFunctionInvokation.getObject();
                if (e = memberFunctionInvokation.getAppropriatelyCastArgument(0) instanceof CastExpression) {
                    Expression ce = ((CastExpression)e).getChild();
                    if (ce.getInferredJavaType().getJavaTypeInstance().implicitlyCastsTo(e.getInferredJavaType().getJavaTypeInstance(), null)) {
                        e = ce;
                    }
                }
                reverseAppendChain.add((Expression)e);
                continue;
            }
            if (!(lhs instanceof ConstructorInvokationSimple)) return null;
            ConstructorInvokationSimple newObject = (ConstructorInvokationSimple)lhs;
            String rawName = newObject.getTypeInstance().getRawName();
            if ((!this.stringBuilderEnabled || !rawName.equals("java.lang.StringBuilder")) && (!this.stringBufferEnabled || !rawName.equals("java.lang.StringBuffer"))) return null;
            switch (newObject.getArgs().size()) {
                default: {
                    return null;
                }
                case 1: {
                    Expression e = (Expression)newObject.getArgs().get(0);
                    String typeName = e.getInferredJavaType().getJavaTypeInstance().getRawName();
                    if (!typeName.equals("java.lang.String")) return null;
                    if (e instanceof CastExpression) {
                        Expression ce = ((CastExpression)e).getChild();
                        if (ce.getInferredJavaType().getJavaTypeInstance().implicitlyCastsTo(e.getInferredJavaType().getJavaTypeInstance(), null)) {
                            e = ce;
                        }
                    }
                    reverseAppendChain.add((Expression)e);
                    break;
                }
                case 0: 
            }
            return this.genStringConcat(reverseAppendChain);
        } while (lhs != null);
        return null;
    }

    private Expression genStringConcat(List<Expression> revList) {
        int x;
        JavaTypeInstance lastType = revList.get(revList.size() - 1).getInferredJavaType().getJavaTypeInstance();
        if (lastType instanceof RawJavaType) {
            revList.add(new Literal(TypedLiteral.getString("\"\"")));
        }
        if ((x = revList.size() - 1) < 0) {
            return null;
        }
        Expression head = revList.get(x);
        InferredJavaType inferredJavaType = new InferredJavaType(TypeConstants.STRING, InferredJavaType.Source.STRING_TRANSFORM, true);
        --x;
        for (; x >= 0; --x) {
            Expression appendee;
            if (appendee = revList.get(x) instanceof ArithmeticOperation && appendee.getPrecedence().compareTo((Enum)Precedence.ADD_SUB) <= 0) {
                appendee = new ExplicitBraceExpression(appendee);
            }
            head = new ArithmeticOperation(inferredJavaType, head, appendee, ArithOp.PLUS);
        }
        return head;
    }
}

