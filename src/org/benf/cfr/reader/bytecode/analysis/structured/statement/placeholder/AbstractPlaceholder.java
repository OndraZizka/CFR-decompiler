/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder;

import java.util.List;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.output.Dumper;

public abstract class AbstractPlaceholder
implements StructuredStatement {
    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }

    @Override
    public boolean isProperlyStructured() {
        return false;
    }

    @Override
    public boolean isRecursivelyStructured() {
        return false;
    }

    @Override
    public boolean fallsNopToNext() {
        return false;
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
    }

    @Override
    public StructuredStatement informBlockHeirachy(Vector<BlockIdentifier> blockIdentifiers) {
        throw new UnsupportedOperationException();
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Op04StructuredStatement getContainer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContainer(Op04StructuredStatement container) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void markCreator(LValue scopedEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean alwaysDefines(LValue scopedEntity) {
        return false;
    }

    @Override
    public List<LValue> findCreatedHere() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String suggestName(LocalVariable createdHere, Predicate<String> testNameUsedFn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper;
    }

    @Override
    public boolean inlineable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Op04StructuredStatement getInline() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEffectivelyNOP() {
        throw new UnsupportedOperationException();
    }
}

