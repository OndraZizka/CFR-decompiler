/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMutatingAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredBlockStatement;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredFor
extends AbstractStructuredBlockStatement {
    private ConditionalExpression condition;
    private AssignmentSimple initial;
    private AbstractAssignmentExpression assignment;
    private final BlockIdentifier block;
    private boolean isCreator;

    public StructuredFor(ConditionalExpression condition, AssignmentSimple initial, AbstractAssignmentExpression assignment, Op04StructuredStatement body, BlockIdentifier block) {
        super(body);
        this.condition = condition;
        this.initial = initial;
        this.assignment = assignment;
        this.block = block;
        this.isCreator = false;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.condition);
        collector.collectFrom(this.assignment);
        super.collectTypeUsages(collector);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.block.hasForeignReferences()) {
            dumper.print(this.block.getName() + " : ");
        }
        dumper.print("for (");
        if (this.initial != null) {
            if (this.isCreator) {
                dumper.dump(this.initial.getCreatedLValue().getInferredJavaType().getJavaTypeInstance()).print(" ");
            }
            dumper.dump(this.initial);
            dumper.removePendingCarriageReturn();
        } else {
            dumper.print(";");
        }
        dumper.print(" ").dump(this.condition).print("; ").dump(this.assignment).print(") ");
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
        this.assignment.collectUsedLValues((LValueUsageCollector)scopeDiscoverer);
        this.condition.collectUsedLValues((LValueUsageCollector)scopeDiscoverer);
        if (this.initial != null) {
            LValue lValue = this.initial.getCreatedLValue();
            Expression expression = this.initial.getRValue();
            lValue.collectLValueAssignments(expression, this.getContainer(), scopeDiscoverer);
        }
        this.getBody().traceLocalVariableScope(scopeDiscoverer);
        scopeDiscoverer.leaveBlock(this);
    }

    @Override
    public void markCreator(LValue scopedEntity) {
        this.isCreator = true;
    }

    @Override
    public List<LValue> findCreatedHere() {
        LValue created;
        if (!this.isCreator) {
            return null;
        }
        if (this.initial == null) {
            return null;
        }
        if (created = this.initial.getCreatedLValue() instanceof LocalVariable) return ListFactory.newList(created);
        return null;
    }

    @Override
    public String suggestName(LocalVariable createdHere, Predicate<String> testNameUsedFn) {
        JavaTypeInstance loopType = createdHere.getInferredJavaType().getJavaTypeInstance();
        if (!(this.assignment instanceof AbstractMutatingAssignmentExpression)) {
            return null;
        }
        if (!(loopType instanceof RawJavaType)) {
            return null;
        }
        RawJavaType rawJavaType = (RawJavaType)loopType;
        switch (rawJavaType) {
            case INT: 
            case SHORT: 
            case LONG: {
                break;
            }
            default: {
                return null;
            }
        }
        for (String posss : poss = new String[]{"i", "j", "k"}) {
            if (testNameUsedFn.test(posss)) continue;
            return posss;
        }
        return "i";
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
        this.condition = expressionRewriter.rewriteExpression(this.condition, (SSAIdentifiers)null, (StatementContainer)this.getContainer(), (ExpressionRewriterFlags)null);
    }

    public BlockIdentifier getBlock() {
        return this.block;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        StructuredStatement o = matchIterator.getCurrent();
        if (!(o instanceof StructuredFor)) {
            return false;
        }
        StructuredFor other = (StructuredFor)o;
        if (!this.initial.equals(other.initial)) {
            return false;
        }
        if (this.condition == null) {
            if (other.condition != null) {
                return false;
            }
        } else if (!this.condition.equals(other.condition)) {
            return false;
        }
        if (!this.assignment.equals((Object)other.assignment)) {
            return false;
        }
        if (!this.block.equals(other.block)) {
            return false;
        }
        matchIterator.advance();
        return true;
    }

}

