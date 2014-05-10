/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
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
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class DynamicInvokation
extends AbstractExpression {
    private Expression innerInvokation;
    private List<Expression> dynamicArgs;

    public DynamicInvokation(InferredJavaType castJavaType, Expression innerInvokation, List<Expression> dynamicArgs) {
        super(castJavaType);
        this.innerInvokation = innerInvokation;
        this.dynamicArgs = dynamicArgs;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new DynamicInvokation(this.getInferredJavaType(), cloneHelper.replaceOrClone(this.innerInvokation), cloneHelper.replaceOrClone(this.dynamicArgs));
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.innerInvokation);
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.dynamicArgs);
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.innerInvokation.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        for (int x = 0; x < this.dynamicArgs.size(); ++x) {
            this.dynamicArgs.set(x, this.dynamicArgs.get(x).replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer));
        }
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.innerInvokation.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, statementContainer, flags);
        for (int x = 0; x < this.dynamicArgs.size(); ++x) {
            this.dynamicArgs.set(x, expressionRewriter.rewriteExpression(this.dynamicArgs.get(x), ssaIdentifiers, statementContainer, flags));
        }
        return this;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        d.print("(").dump(this.getInferredJavaType().getJavaTypeInstance()).print(")");
        d.dump(this.innerInvokation);
        d.print("(");
        boolean first = true;
        for (Expression arg : this.dynamicArgs) {
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
        this.innerInvokation.collectUsedLValues(lValueUsageCollector);
        for (Expression expression : this.dynamicArgs) {
            expression.collectUsedLValues(lValueUsageCollector);
        }
    }

    public Expression getInnerInvokation() {
        return this.innerInvokation;
    }

    public List<Expression> getDynamicArgs() {
        return this.dynamicArgs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        DynamicInvokation that = (DynamicInvokation)o;
        if (this.dynamicArgs != null ? !this.dynamicArgs.equals(that.dynamicArgs) : that.dynamicArgs != null) {
            return false;
        }
        if (!(this.innerInvokation != null ? !this.innerInvokation.equals(that.innerInvokation) : that.innerInvokation != null)) return true;
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
        DynamicInvokation other = (DynamicInvokation)o;
        if (!constraint.equivalent(this.innerInvokation, other.innerInvokation)) {
            return false;
        }
        if (constraint.equivalent(this.dynamicArgs, other.dynamicArgs)) return true;
        return false;
    }
}

