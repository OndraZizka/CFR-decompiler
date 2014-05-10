/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.rewriters;

import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.util.LazyMap;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class AccountingRewriter
implements ExpressionRewriter {
    private final Map<StackSSALabel, Long> count;

    public AccountingRewriter() {
        this.count = new LazyMap<StackSSALabel, Long>(MapFactory.newMap(), new UnaryFunction<StackSSALabel, Long>(){

            @Override
            public Long invoke(StackSSALabel arg) {
                return new Long(0);
            }
        });
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
        if (!(lValue instanceof StackSSALabel)) return lValue.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        return this.rewriteExpression((StackSSALabel)lValue, ssaIdentifiers, statementContainer, flags);
    }

    @Override
    public StackSSALabel rewriteExpression(StackSSALabel lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        if (flags == ExpressionRewriterFlags.LVALUE) return lValue;
        this.count.put(lValue, this.count.get(lValue) + 1);
        return lValue;
    }

    public void flush() {
        for (Map.Entry<StackSSALabel, Long> entry : this.count.entrySet()) {
            StackSSALabel stackSSALabel = entry.getKey();
            stackSSALabel.getStackEntry().forceUsageCount(entry.getValue());
        }
    }

}

