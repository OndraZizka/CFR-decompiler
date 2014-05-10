/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredBlockStatement;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredIter
extends AbstractStructuredBlockStatement {
    private final BlockIdentifier block;
    private LValue iterator;
    private Expression list;
    private boolean creator;

    public StructuredIter(BlockIdentifier block, LValue iterator, Expression list, Op04StructuredStatement body) {
        super(body);
        this.block = block;
        this.iterator = iterator;
        this.list = list;
        this.creator = false;
        JavaTypeInstance itertype = iterator.getInferredJavaType().getJavaTypeInstance();
        if (itertype.isUsableType()) return;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.iterator.collectTypeUsages(collector);
        this.list.collectTypeUsages(collector);
        super.collectTypeUsages(collector);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.block.hasForeignReferences()) {
            dumper.print(this.block.getName() + " : ");
        }
        JavaTypeInstance itertype = this.iterator.getInferredJavaType().getJavaTypeInstance();
        dumper.print("for (");
        dumper.dump(itertype).print(" ");
        dumper.dump(this.iterator).print(" : ").dump(this.list).print(") ");
        this.getBody().dump(dumper);
        return dumper;
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
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
        this.getBody().linearizeStatementsInto(out);
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        scopeDiscoverer.enterBlock(this);
        this.list.collectUsedLValues(scopeDiscoverer);
        this.iterator.collectLValueAssignments(null, this.getContainer(), scopeDiscoverer);
        this.getBody().traceLocalVariableScope(scopeDiscoverer);
        scopeDiscoverer.leaveBlock(this);
    }

    @Override
    public void markCreator(LValue scopedEntity) {
        this.creator = true;
    }

    @Override
    public boolean alwaysDefines(LValue scopedEntity) {
        return true;
    }

    @Override
    public List<LValue> findCreatedHere() {
        if (this.iterator instanceof LocalVariable) return ListFactory.newList(this.iterator);
        return null;
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
        this.iterator = expressionRewriter.rewriteExpression(this.iterator, (SSAIdentifiers)null, (StatementContainer)this.getContainer(), (ExpressionRewriterFlags)null);
        this.list = expressionRewriter.rewriteExpression(this.list, (SSAIdentifiers)null, (StatementContainer)this.getContainer(), (ExpressionRewriterFlags)null);
    }
}

