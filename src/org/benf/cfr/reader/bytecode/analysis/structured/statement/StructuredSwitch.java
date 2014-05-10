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
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredBlockStatement;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredSwitch
extends AbstractStructuredBlockStatement
implements BoxingProcessor {
    private Expression switchOn;
    private final BlockIdentifier blockIdentifier;

    public StructuredSwitch(Expression switchOn, Op04StructuredStatement body, BlockIdentifier blockIdentifier) {
        super(body);
        this.switchOn = switchOn;
        this.blockIdentifier = blockIdentifier;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.switchOn.collectTypeUsages(collector);
        super.collectTypeUsages(collector);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.blockIdentifier.hasForeignReferences()) {
            dumper.print(this.blockIdentifier.getName() + " : ");
        }
        dumper.print("switch (").dump(this.switchOn).print(") ");
        this.getBody().dump(dumper);
        return dumper;
    }

    @Override
    public boolean isProperlyStructured() {
        return true;
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
        scope.add(this);
        try {
            this.getBody().transform(transformer, scope);
        }
        finally {
            scope.remove(this);
        }
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        this.switchOn = boxingRewriter.sugarUnboxing(this.switchOn);
        return true;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }

    public Expression getSwitchOn() {
        return this.switchOn;
    }

    @Override
    public Op04StructuredStatement getBody() {
        return super.getBody();
    }

    public BlockIdentifier getBlockIdentifier() {
        return this.blockIdentifier;
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
        this.getBody().linearizeStatementsInto(out);
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        this.switchOn.collectUsedLValues(scopeDiscoverer);
        this.getBody().traceLocalVariableScope(scopeDiscoverer);
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredSwitch)) {
            return false;
        }
        StructuredSwitch other = (StructuredSwitch)o;
        if (!this.switchOn.equals(other.switchOn)) {
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
        expressionRewriter.handleStatement(this.getContainer());
        this.switchOn = expressionRewriter.rewriteExpression(this.switchOn, (SSAIdentifiers)null, (StatementContainer)this.getContainer(), (ExpressionRewriterFlags)null);
    }
}

