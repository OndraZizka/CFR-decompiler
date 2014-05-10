/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class LambdaExpression
extends AbstractExpression {
    private List<LValue> args;
    private Expression result;

    public LambdaExpression(InferredJavaType castJavaType, List<LValue> args, Expression result) {
        super(castJavaType);
        this.args = args;
        this.result = result;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new LambdaExpression(this.getInferredJavaType(), cloneHelper.replaceOrClone(this.args), cloneHelper.replaceOrClone(this.result));
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.args);
        this.result.collectTypeUsages(collector);
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        for (int x = 0; x < this.args.size(); ++x) {
            this.args.set(x, expressionRewriter.rewriteExpression(this.args.get(x), ssaIdentifiers, statementContainer, flags));
        }
        this.result = expressionRewriter.rewriteExpression(this.result, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        boolean multi = this.args.size() != 1;
        boolean first = true;
        if (multi) {
            d.print("(");
        }
        for (LValue lValue : this.args) {
            first = CommaHelp.comma(first, d);
            d.dump(lValue);
        }
        if (!multi) return d.print(" -> ").dump(this.result);
        d.print(")");
        return d.print(" -> ").dump(this.result);
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
    }

    public List<LValue> getArgs() {
        return this.args;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        LambdaExpression that = (LambdaExpression)o;
        if (this.args != null ? !this.args.equals(that.args) : that.args != null) {
            return false;
        }
        if (!(this.result != null ? !this.result.equals(that.result) : that.result != null)) return true;
        return false;
    }

    @Override
    public final boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        LambdaExpression other = (LambdaExpression)o;
        if (!constraint.equivalent(this.args, other.args)) {
            return false;
        }
        if (constraint.equivalent(this.result, other.result)) return true;
        return false;
    }
}

