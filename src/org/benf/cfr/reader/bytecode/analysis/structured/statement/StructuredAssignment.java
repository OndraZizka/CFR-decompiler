/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredAssignment
extends AbstractStructuredStatement
implements BoxingProcessor {
    private LValue lvalue;
    private Expression rvalue;
    boolean isCreator;

    public StructuredAssignment(LValue lvalue, Expression rvalue) {
        this.lvalue = lvalue;
        this.rvalue = rvalue;
        this.isCreator = false;
    }

    public StructuredAssignment(LValue lvalue, Expression rvalue, boolean isCreator) {
        this.lvalue = lvalue;
        this.rvalue = rvalue;
        this.isCreator = isCreator;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.lvalue.collectTypeUsages(collector);
        collector.collectFrom(this.rvalue);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.isCreator) {
            dumper.dump(this.lvalue.getInferredJavaType().getJavaTypeInstance()).print(" ");
        }
        dumper.dump(this.lvalue).print(" = ").dump(this.rvalue).endCodeln();
        return dumper;
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        this.rvalue.collectUsedLValues(scopeDiscoverer);
        this.lvalue.collectLValueAssignments(this.rvalue, this.getContainer(), scopeDiscoverer);
    }

    @Override
    public void markCreator(LValue scopedEntity) {
        InferredJavaType inferredJavaType;
        LocalVariable localVariable;
        if (!(scopedEntity instanceof LocalVariable)) return;
        if (!(localVariable = (LocalVariable)scopedEntity).equals(this.lvalue)) {
            throw new IllegalArgumentException("Being asked to mark creator for wrong variable");
        }
        this.isCreator = true;
        if (!(inferredJavaType = localVariable.getInferredJavaType()).isClash()) return;
        inferredJavaType.collapseTypeClash();
    }

    @Override
    public List<LValue> findCreatedHere() {
        if (!this.isCreator) return null;
        return ListFactory.newList(this.lvalue);
    }

    public LValue getLvalue() {
        return this.lvalue;
    }

    public Expression getRvalue() {
        return this.rvalue;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!this.equals(o)) {
            return false;
        }
        matchIterator.advance();
        return true;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof StructuredAssignment)) {
            return false;
        }
        StructuredAssignment other = (StructuredAssignment)o;
        if (!this.lvalue.equals(other.lvalue)) {
            return false;
        }
        if (this.rvalue.equals(other.rvalue)) return true;
        return false;
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
        expressionRewriter.handleStatement(this.getContainer());
        this.rvalue = expressionRewriter.rewriteExpression(this.rvalue, (SSAIdentifiers)null, (StatementContainer)this.getContainer(), (ExpressionRewriterFlags)null);
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        this.rvalue = boxingRewriter.sugarNonParameterBoxing(this.rvalue, this.lvalue.getInferredJavaType().getJavaTypeInstance());
        return true;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.lvalue = this.lvalue.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, statementContainer, flags);
    }
}

