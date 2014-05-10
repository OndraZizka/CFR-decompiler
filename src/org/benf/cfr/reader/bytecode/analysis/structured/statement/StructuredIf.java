/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.CanRemovePointlessBlock;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssert;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.ElseBlock;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredIf
extends AbstractStructuredStatement
implements CanRemovePointlessBlock {
    ConditionalExpression conditionalExpression;
    Op04StructuredStatement ifTaken;
    Op04StructuredStatement elseBlock;

    public StructuredIf(ConditionalExpression conditionalExpression, Op04StructuredStatement ifTaken) {
        this(conditionalExpression, ifTaken, null);
    }

    public StructuredIf(ConditionalExpression conditionalExpression, Op04StructuredStatement ifTaken, Op04StructuredStatement elseBlock) {
        this.conditionalExpression = conditionalExpression;
        this.ifTaken = ifTaken;
        this.elseBlock = elseBlock;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.conditionalExpression.collectTypeUsages(collector);
        collector.collectFrom(this.ifTaken);
        collector.collectFrom(this.elseBlock);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("if (").dump(this.conditionalExpression).print(") ");
        this.ifTaken.dump(dumper);
        if (this.elseBlock == null) return dumper;
        dumper.removePendingCarriageReturn();
        dumper.print(" else ");
        this.elseBlock.dump(dumper);
        return dumper;
    }

    public boolean hasElseBlock() {
        return this.elseBlock != null;
    }

    public ConditionalExpression getConditionalExpression() {
        return this.conditionalExpression;
    }

    public Op04StructuredStatement getIfTaken() {
        return this.ifTaken;
    }

    @Override
    public StructuredStatement informBlockHeirachy(Vector<BlockIdentifier> blockIdentifiers) {
        this.ifTaken.informBlockMembership(blockIdentifiers);
        if (this.elseBlock == null) return null;
        this.elseBlock.informBlockMembership(blockIdentifiers);
        return null;
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
        scope.add(this);
        try {
            this.ifTaken.transform(transformer, scope);
            if (this.elseBlock == null) return;
            this.elseBlock.transform(transformer, scope);
        }
        finally {
            scope.remove(this);
        }
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
        this.ifTaken.linearizeStatementsInto(out);
        if (this.elseBlock == null) return;
        out.add(new ElseBlock());
        this.elseBlock.linearizeStatementsInto(out);
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        this.conditionalExpression.collectUsedLValues((LValueUsageCollector)scopeDiscoverer);
        this.ifTaken.traceLocalVariableScope(scopeDiscoverer);
        if (this.elseBlock == null) return;
        this.elseBlock.traceLocalVariableScope(scopeDiscoverer);
    }

    @Override
    public boolean isRecursivelyStructured() {
        if (!this.ifTaken.isFullyStructured()) {
            return false;
        }
        if (this.elseBlock == null || this.elseBlock.isFullyStructured()) return true;
        return false;
    }

    @Override
    public boolean fallsNopToNext() {
        return true;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredIf)) {
            return false;
        }
        StructuredIf other = (StructuredIf)o;
        if (!this.conditionalExpression.equals(other.conditionalExpression)) {
            return false;
        }
        matchIterator.advance();
        return true;
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
        this.conditionalExpression = expressionRewriter.rewriteExpression(this.conditionalExpression, (SSAIdentifiers)null, (StatementContainer)this.getContainer(), (ExpressionRewriterFlags)null);
    }

    public StructuredStatement convertToAssertion(StructuredAssert structuredAssert) {
        if (this.elseBlock == null) {
            return structuredAssert;
        }
        LinkedList list = ListFactory.newLinkedList();
        list.add((Op04StructuredStatement)new Op04StructuredStatement(structuredAssert));
        list.add((Op04StructuredStatement)this.elseBlock);
        return new Block(list, false);
    }

    @Override
    public void removePointlessBlocks(StructuredScope scope) {
        if (this.elseBlock == null || !this.elseBlock.getStatement().isEffectivelyNOP()) return;
        this.elseBlock = null;
    }
}

