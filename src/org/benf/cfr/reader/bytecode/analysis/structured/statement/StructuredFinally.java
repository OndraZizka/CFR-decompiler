/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredFinally
extends AbstractStructuredStatement {
    private final Op04StructuredStatement catchBlock;

    public StructuredFinally(Op04StructuredStatement catchBlock) {
        this.catchBlock = catchBlock;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("finally ");
        this.catchBlock.dump(dumper);
        return dumper;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.catchBlock);
    }

    @Override
    public boolean isProperlyStructured() {
        return true;
    }

    @Override
    public boolean fallsNopToNext() {
        return true;
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
        scope.add(this);
        try {
            this.catchBlock.transform(transformer, scope);
        }
        finally {
            scope.remove(this);
        }
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
        this.catchBlock.linearizeStatementsInto(out);
    }

    public Op04StructuredStatement getCatchBlock() {
        return this.catchBlock;
    }

    @Override
    public boolean isRecursivelyStructured() {
        return this.catchBlock.isFullyStructured();
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredFinally)) {
            return false;
        }
        StructuredFinally other = (StructuredFinally)o;
        matchIterator.advance();
        return true;
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        this.catchBlock.traceLocalVariableScope(scopeDiscoverer);
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
    }
}

