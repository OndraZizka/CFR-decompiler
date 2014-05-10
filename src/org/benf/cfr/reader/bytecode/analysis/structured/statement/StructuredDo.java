/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
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
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredBlockStatement;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredDo
extends AbstractStructuredBlockStatement {
    private ConditionalExpression condition;
    private final BlockIdentifier block;

    public StructuredDo(ConditionalExpression condition, Op04StructuredStatement body, BlockIdentifier block) {
        super(body);
        this.condition = condition;
        this.block = block;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.block.hasForeignReferences()) {
            dumper.print(this.block.getName() + " : ");
        }
        dumper.print("do ");
        this.getBody().dump(dumper);
        dumper.removePendingCarriageReturn();
        dumper.print(" while (");
        if (this.condition == null) {
            dumper.print("true");
        } else {
            dumper.dump(this.condition);
        }
        return dumper.print(");\n");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.condition);
        super.collectTypeUsages(collector);
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
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        if (this.condition != null) {
            this.condition.collectUsedLValues((LValueUsageCollector)scopeDiscoverer);
        }
        this.getBody().traceLocalVariableScope(scopeDiscoverer);
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(this);
        this.getBody().linearizeStatementsInto(out);
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
        if (this.condition == null) return;
        this.condition = expressionRewriter.rewriteExpression(this.condition, (SSAIdentifiers)null, (StatementContainer)this.getContainer(), (ExpressionRewriterFlags)null);
    }

    public BlockIdentifier getBlock() {
        return this.block;
    }

    public ConditionalExpression getCondition() {
        return this.condition;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredDo)) {
            return false;
        }
        StructuredDo other = (StructuredDo)o;
        if (this.condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!this.condition.equals(other.condition)) {
            return false;
        }
        if (!this.block.equals(other.block)) {
            return false;
        }
        matchIterator.advance();
        return true;
    }
}

