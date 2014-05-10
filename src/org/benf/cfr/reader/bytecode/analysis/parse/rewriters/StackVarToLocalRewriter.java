/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.rewriters;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StackValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.MapFactory;

public class StackVarToLocalRewriter
implements ExpressionRewriter {
    private final Map<StackSSALabel, LocalVariable> replacements = MapFactory.newMap();
    private int idx = 0;

    @Override
    public void handleStatement(StatementContainer statementContainer) {
    }

    @Override
    public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        if (!(expression instanceof StackValue)) return expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        StackValue stackValue = (StackValue)expression;
        return new LValueExpression(this.getReplacement(stackValue.getStackValue()));
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
        if (!(lValue instanceof StackSSALabel)) return lValue.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        return this.getReplacement((StackSSALabel)lValue);
    }

    @Override
    public StackSSALabel rewriteExpression(StackSSALabel lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        throw new UnsupportedOperationException();
    }

    private LocalVariable getReplacement(StackSSALabel stackSSALabel) {
        LocalVariable res = this.replacements.get(stackSSALabel);
        if (res != null) {
            return res;
        }
        res = new LocalVariable("v" + this.idx++, stackSSALabel.getInferredJavaType());
        this.replacements.put(stackSSALabel, res);
        return res;
    }
}

