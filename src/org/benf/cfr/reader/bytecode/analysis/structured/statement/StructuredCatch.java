/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredThrow;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredCatch
extends AbstractStructuredStatement {
    private final List<JavaRefTypeInstance> catchTypes;
    private final Op04StructuredStatement catchBlock;
    private final LValue catching;
    private final Set<BlockIdentifier> possibleTryBlocks;

    public StructuredCatch(Collection<JavaRefTypeInstance> catchTypes, Op04StructuredStatement catchBlock, LValue catching, Set<BlockIdentifier> possibleTryBlocks) {
        this.catchTypes = catchTypes == null ? null : ListFactory.newList(catchTypes);
        this.catchBlock = catchBlock;
        this.catching = catching;
        this.possibleTryBlocks = possibleTryBlocks;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect((Collection<? extends JavaTypeInstance>)this.catchTypes);
        this.catchBlock.collectTypeUsages(collector);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        boolean first = true;
        dumper.print("catch (");
        for (JavaRefTypeInstance catchType : this.catchTypes) {
            if (!first) {
                dumper.print(" | ");
            }
            dumper.dump(catchType);
            first = false;
        }
        dumper.print(" ").dump(this.catching).print(") ");
        this.catchBlock.dump(dumper);
        return dumper;
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

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredCatch)) {
            return false;
        }
        StructuredCatch other = (StructuredCatch)o;
        matchIterator.advance();
        return true;
    }

    public boolean isRethrow() {
        Block block;
        StructuredStatement statement = this.catchBlock.getStatement();
        if (!(statement instanceof Block)) {
            return false;
        }
        if (!(block = (Block)statement).isJustOneStatement()) {
            return false;
        }
        StructuredStatement inBlock = block.getSingleStatement().getStatement();
        StructuredThrow test = new StructuredThrow(new LValueExpression(this.catching));
        return test.equals(inBlock);
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        if (this.catching instanceof LocalVariable) {
            scopeDiscoverer.collectLocalVariableAssignment((LocalVariable)this.catching, this.getContainer(), null);
        }
        this.catchBlock.traceLocalVariableScope(scopeDiscoverer);
    }

    @Override
    public List<LValue> findCreatedHere() {
        return ListFactory.newList(this.catching);
    }

    @Override
    public void markCreator(LValue scopedEntity) {
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
    }

    public Set<BlockIdentifier> getPossibleTryBlocks() {
        return this.possibleTryBlocks;
    }

    @Override
    public boolean isRecursivelyStructured() {
        return this.catchBlock.isFullyStructured();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        StructuredCatch that = (StructuredCatch)o;
        if (!(this.catching != null ? !this.catching.equals(that.catching) : that.catching != null)) return true;
        return false;
    }
}

