/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFinally;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredTry
extends AbstractStructuredStatement {
    private final ExceptionGroup exceptionGroup;
    private Op04StructuredStatement tryBlock;
    private List<Op04StructuredStatement> catchBlocks = ListFactory.newList();
    private Op04StructuredStatement finallyBlock;
    private final BlockIdentifier tryBlockIdentifier;

    public StructuredTry(ExceptionGroup exceptionGroup, Op04StructuredStatement tryBlock, BlockIdentifier tryBlockIdentifier) {
        this.exceptionGroup = exceptionGroup;
        this.tryBlock = tryBlock;
        this.finallyBlock = null;
        this.tryBlockIdentifier = tryBlockIdentifier;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("try ");
        this.tryBlock.dump(dumper);
        for (Op04StructuredStatement catchBlock : this.catchBlocks) {
            catchBlock.dump(dumper);
        }
        if (this.finallyBlock == null) return dumper;
        this.finallyBlock.dump(dumper);
        return dumper;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.tryBlock);
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.catchBlocks);
        collector.collectFrom(this.finallyBlock);
    }

    @Override
    public boolean isProperlyStructured() {
        return true;
    }

    @Override
    public boolean fallsNopToNext() {
        return true;
    }

    public void addCatch(Op04StructuredStatement catchStatement) {
        this.catchBlocks.add(catchStatement);
    }

    public void addFinally(Op04StructuredStatement finallyBlock) {
        this.finallyBlock = finallyBlock;
    }

    public void removeFinalJumpsTo(Op04StructuredStatement after) {
        this.tryBlock.removeLastGoto(after);
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
        scope.add(this);
        try {
            this.tryBlock.transform(transformer, scope);
            for (Op04StructuredStatement catchBlock : this.catchBlocks) {
                catchBlock.getStatement().transformStructuredChildren(transformer, scope);
            }
            if (this.finallyBlock == null) return;
            this.finallyBlock.getStatement().transformStructuredChildren(transformer, scope);
        }
        finally {
            scope.remove(this);
        }
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
        this.tryBlock.linearizeStatementsInto(out);
        for (Op04StructuredStatement catchBlock : this.catchBlocks) {
            catchBlock.linearizeStatementsInto(out);
        }
        if (this.finallyBlock == null) return;
        this.finallyBlock.linearizeStatementsInto(out);
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        this.tryBlock.traceLocalVariableScope(scopeDiscoverer);
        for (Op04StructuredStatement catchBlock : this.catchBlocks) {
            catchBlock.traceLocalVariableScope(scopeDiscoverer);
        }
        if (this.finallyBlock == null) return;
        this.finallyBlock.traceLocalVariableScope(scopeDiscoverer);
    }

    @Override
    public boolean isRecursivelyStructured() {
        if (!this.tryBlock.isFullyStructured()) {
            return false;
        }
        for (Op04StructuredStatement catchBlock : this.catchBlocks) {
            if (catchBlock.isFullyStructured()) continue;
            return false;
        }
        if (this.finallyBlock == null || this.finallyBlock.isFullyStructured()) return true;
        return false;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredTry)) {
            return false;
        }
        StructuredTry other = (StructuredTry)o;
        matchIterator.advance();
        return true;
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
    }

    private boolean isPointlessTry() {
        Block block;
        Op04StructuredStatement finallyCode;
        StructuredFinally structuredFinally;
        if (!this.catchBlocks.isEmpty()) {
            return false;
        }
        if (this.finallyBlock == null) {
            return true;
        }
        if (!(this.finallyBlock.getStatement() instanceof StructuredFinally)) {
            return false;
        }
        if (!((finallyCode = (structuredFinally = (StructuredFinally)this.finallyBlock.getStatement()).getCatchBlock()).getStatement() instanceof Block)) {
            return false;
        }
        if (!(block = (Block)finallyCode.getStatement()).isEffectivelyNOP()) return false;
        return true;
    }

    private boolean isJustTryCatchThrow() {
        StructuredStatement catchS;
        Op04StructuredStatement catchBlock;
        if (this.finallyBlock != null) {
            return false;
        }
        if (this.catchBlocks.size() != 1) {
            return false;
        }
        if (!(catchS = (catchBlock = this.catchBlocks.get(0)).getStatement() instanceof StructuredCatch)) {
            return false;
        }
        StructuredCatch structuredCatch = (StructuredCatch)catchS;
        return structuredCatch.isRethrow();
    }

    @Override
    public boolean inlineable() {
        if (!this.isPointlessTry() && !this.isJustTryCatchThrow()) return false;
        return true;
    }

    public BlockIdentifier getTryBlockIdentifier() {
        return this.tryBlockIdentifier;
    }

    @Override
    public Op04StructuredStatement getInline() {
        return this.tryBlock;
    }
}

