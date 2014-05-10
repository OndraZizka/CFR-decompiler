/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import com.sun.istack.internal.Nullable;
import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredBlockStatement;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredCase
extends AbstractStructuredBlockStatement {
    private List<Expression> values;
    private final BlockIdentifier blockIdentifier;
    @Nullable
    private final InferredJavaType inferredJavaTypeOfSwitch;
    private final boolean enumSwitch;

    public StructuredCase(List<Expression> values, InferredJavaType inferredJavaTypeOfSwitch, Op04StructuredStatement body, BlockIdentifier blockIdentifier) {
        this(values, inferredJavaTypeOfSwitch, body, blockIdentifier, false);
    }

    public StructuredCase(List<Expression> values, InferredJavaType inferredJavaTypeOfSwitch, Op04StructuredStatement body, BlockIdentifier blockIdentifier, boolean enumSwitch) {
        super(body);
        this.blockIdentifier = blockIdentifier;
        this.enumSwitch = enumSwitch;
        this.inferredJavaTypeOfSwitch = inferredJavaTypeOfSwitch;
        if (inferredJavaTypeOfSwitch != null && inferredJavaTypeOfSwitch.getJavaTypeInstance() == RawJavaType.CHAR) {
            for (Expression value : values) {
                if (!(value instanceof Literal)) continue;
                TypedLiteral typedLiteral = ((Literal)value).getValue();
                typedLiteral.getInferredJavaType().useAsWithoutCasting(inferredJavaTypeOfSwitch.getJavaTypeInstance());
            }
        }
        this.values = values;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        if (this.inferredJavaTypeOfSwitch != null) {
            collector.collect(this.inferredJavaTypeOfSwitch.getJavaTypeInstance());
        }
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.values);
        super.collectTypeUsages(collector);
    }

    private static StaticVariable getEnumStatic(Expression expression) {
        LValue lValue;
        if (!(expression instanceof LValueExpression)) {
            return null;
        }
        if (lValue = ((LValueExpression)expression).getLValue() instanceof StaticVariable) return (StaticVariable)lValue;
        return null;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.values.isEmpty()) {
            dumper.print("default: ");
        } else {
            int len = this.values.size();
            int last = len - 1;
            for (int x = 0; x < len; ++x) {
                StaticVariable enumStatic;
                Expression value = this.values.get(x);
                if (this.enumSwitch && (enumStatic = StructuredCase.getEnumStatic(value)) != null) {
                    dumper.print("case " + enumStatic.getVarName() + ": ");
                    if (x == last) continue;
                    dumper.newln();
                    continue;
                }
                dumper.print("case ").dump(value).print(": ");
                if (x == last) continue;
                dumper.newln();
            }
        }
        this.getBody().dump(dumper);
        return dumper;
    }

    @Override
    public boolean isProperlyStructured() {
        return true;
    }

    public List<Expression> getValues() {
        return this.values;
    }

    @Override
    public Op04StructuredStatement getBody() {
        return super.getBody();
    }

    public BlockIdentifier getBlockIdentifier() {
        return this.blockIdentifier;
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
        this.getBody().transform(transformer, scope);
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
        this.getBody().linearizeStatementsInto(out);
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        for (Expression expression : this.values) {
            expression.collectUsedLValues(scopeDiscoverer);
        }
        this.getBody().traceLocalVariableScope(scopeDiscoverer);
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredCase)) {
            return false;
        }
        StructuredCase other = (StructuredCase)o;
        if (!this.values.equals(other.values)) {
            return false;
        }
        if (!this.blockIdentifier.equals(other.blockIdentifier)) {
            return false;
        }
        matchIterator.advance();
        return true;
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
    }
}

