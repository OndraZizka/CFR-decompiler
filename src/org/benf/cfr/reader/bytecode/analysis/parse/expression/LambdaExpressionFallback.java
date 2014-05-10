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

public class LambdaExpressionFallback
extends AbstractExpression {
    private JavaTypeInstance callClassType;
    private String lambdaFnName;
    private List<JavaTypeInstance> targetFnArgTypes;
    private List<Expression> curriedArgs;
    private boolean instance;
    private final boolean colon;

    public LambdaExpressionFallback(JavaTypeInstance callClassType, InferredJavaType castJavaType, String lambdaFnName, List<JavaTypeInstance> targetFnArgTypes, List<Expression> curriedArgs, boolean instance) {
        super(castJavaType);
        this.callClassType = callClassType;
        this.lambdaFnName = lambdaFnName.equals("<init>") ? "new" : lambdaFnName;
        this.targetFnArgTypes = targetFnArgTypes;
        this.curriedArgs = curriedArgs;
        this.instance = instance;
        boolean isColon = false;
        switch (curriedArgs.size()) {
            case 0: {
                isColon = !(targetFnArgTypes.size() > 1 || instance);
                if (!instance) break;
                isColon = true;
                this.instance = false;
                break;
            }
            case 1: {
                isColon = targetFnArgTypes.size() == 1 && instance;
            }
        }
        this.colon = isColon;
    }

    private LambdaExpressionFallback(InferredJavaType inferredJavaType, boolean colon, boolean instance, List<Expression> curriedArgs, List<JavaTypeInstance> targetFnArgTypes, String lambdaFnName, JavaTypeInstance callClassType) {
        super(inferredJavaType);
        this.colon = colon;
        this.instance = instance;
        this.curriedArgs = curriedArgs;
        this.targetFnArgTypes = targetFnArgTypes;
        this.lambdaFnName = lambdaFnName;
        this.callClassType = callClassType;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new LambdaExpressionFallback(this.getInferredJavaType(), this.colon, this.instance, cloneHelper.replaceOrClone(this.curriedArgs), this.targetFnArgTypes, this.lambdaFnName, this.callClassType);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect((Collection<? extends JavaTypeInstance>)this.targetFnArgTypes);
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.curriedArgs);
        collector.collect(this.callClassType);
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        for (int x = 0; x < this.curriedArgs.size(); ++x) {
            this.curriedArgs.set(x, expressionRewriter.rewriteExpression(this.curriedArgs.get(x), ssaIdentifiers, statementContainer, flags));
        }
        return this;
    }

    private boolean comma(boolean first, Dumper d) {
        if (first) return false;
        d.print(", ");
        return false;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        if (this.colon) {
            if (this.instance) {
                d.dump(this.curriedArgs.get(0)).print("::").print(this.lambdaFnName);
            } else {
                d.dump(this.callClassType).print("::").print(this.lambdaFnName);
            }
        } else {
            int n;
            int x;
            boolean multi = (n = this.targetFnArgTypes.size()) != 1;
            if (multi) {
                d.print("(");
            }
            for (int x2 = 0; x2 < n; ++x2) {
                if (x2 > 0) {
                    d.print(", ");
                }
                d.print("arg_" + x2);
            }
            if (multi) {
                d.print(")");
            }
            if (this.instance) {
                d.print(" -> ").dump(this.curriedArgs.get(0)).print('.').print(this.lambdaFnName);
            } else {
                d.print(" -> ").dump(this.callClassType).print('.').print(this.lambdaFnName);
            }
            d.print("(");
            boolean first = true;
            int n2 = this.instance ? 1 : 0;
            int cnt = this.curriedArgs.size();
            for (x = v33334; x < cnt; ++x) {
                Expression c = this.curriedArgs.get(x);
                first = this.comma(first, d);
                d.dump(c);
            }
            for (x = 0; x < n; ++x) {
                first = this.comma(first, d);
                d.print("arg_" + x);
            }
            d.print(")");
        }
        return d;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        LambdaExpressionFallback that = (LambdaExpressionFallback)o;
        if (this.colon != that.colon) {
            return false;
        }
        if (this.instance != that.instance) {
            return false;
        }
        if (this.callClassType != null ? !this.callClassType.equals(that.callClassType) : that.callClassType != null) {
            return false;
        }
        if (this.curriedArgs != null ? !this.curriedArgs.equals(that.curriedArgs) : that.curriedArgs != null) {
            return false;
        }
        if (this.lambdaFnName != null ? !this.lambdaFnName.equals(that.lambdaFnName) : that.lambdaFnName != null) {
            return false;
        }
        if (!(this.targetFnArgTypes != null ? !this.targetFnArgTypes.equals(that.targetFnArgTypes) : that.targetFnArgTypes != null)) return true;
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
        LambdaExpressionFallback other = (LambdaExpressionFallback)o;
        if (this.instance != other.instance) {
            return false;
        }
        if (this.colon != other.colon) {
            return false;
        }
        if (!constraint.equivalent(this.lambdaFnName, other.lambdaFnName)) {
            return false;
        }
        if (constraint.equivalent(this.curriedArgs, other.curriedArgs)) return true;
        return false;
    }
}

