/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.Op04Rewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.FunctionProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;
import org.benf.cfr.reader.util.ListFactory;

public class VarArgsRewriter
implements Op04Rewriter,
ExpressionRewriter {
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
        if (!(expression instanceof FunctionProcessor)) return expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        ((FunctionProcessor)expression).rewriteVarArgs(this);
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
        return lValue;
    }

    @Override
    public StackSSALabel rewriteExpression(StackSSALabel lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return lValue;
    }

    public void rewriteVarArgsArg(OverloadMethodSet overloadMethodSet, MethodPrototype methodPrototype, List<Expression> args, GenericTypeBinder gtb) {
        Expression lastArg;
        Literal nullLit;
        if (!methodPrototype.isVarArgs()) {
            return;
        }
        if (args.size() != methodPrototype.getArgs().size()) {
            return;
        }
        int last = args.size() - 1;
        if (!(lastArg = args.get(args.size() - 1) instanceof NewAnonymousArray)) {
            return;
        }
        List<Expression> args2 = ListFactory.newList(args);
        args2.remove(last);
        NewAnonymousArray newAnonymousArray = (NewAnonymousArray)lastArg;
        List<Expression> anonVals = newAnonymousArray.getValues();
        if (anonVals.size() == 1 && anonVals.get(0).equals(nullLit = new Literal(TypedLiteral.getNull()))) {
            return;
        }
        args2.addAll(newAnonymousArray.getValues());
        boolean correct = overloadMethodSet.callsCorrectEntireMethod(args2, gtb);
        if (correct) {
            args.clear();
            args.addAll(args2);
        }
        boolean x = true;
    }
}

