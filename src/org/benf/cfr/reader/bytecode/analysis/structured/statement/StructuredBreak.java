/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredBreak
extends AbstractStructuredStatement {
    private final BlockIdentifier breakBlock;
    private final boolean localBreak;

    public StructuredBreak(BlockIdentifier breakBlock, boolean localBreak) {
        this.breakBlock = breakBlock;
        this.localBreak = localBreak;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.localBreak) {
            dumper.print("break;\n");
        } else {
            dumper.print("break " + this.breakBlock.getName() + ";\n");
        }
        return dumper;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }

    public BlockIdentifier getBreakBlock() {
        return this.breakBlock;
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredBreak)) {
            return false;
        }
        StructuredBreak other = (StructuredBreak)o;
        if (!this.breakBlock.equals(other.breakBlock)) {
            return false;
        }
        matchIterator.advance();
        return true;
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
    }
}

