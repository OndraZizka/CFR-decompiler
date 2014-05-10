/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.lvalue;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.output.Dumper;

public class StackSSALabel
extends AbstractLValue {
    private final long id;
    private final StackEntry stackEntry;

    public StackSSALabel(long id, StackEntry stackEntry) {
        super(stackEntry.getInferredJavaType());
        this.id = id;
        this.stackEntry = stackEntry;
    }

    protected StackSSALabel(InferredJavaType inferredJavaType) {
        super(inferredJavaType);
        this.id = 0;
        this.stackEntry = null;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.HIGHEST;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return d.print("v" + this.id + this.typeToString());
    }

    @Override
    public int getNumberOfCreators() {
        return this.stackEntry.getSourceCount();
    }

    @Override
    public LValue deepClone(CloneHelper cloneHelper) {
        return this;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return false;
    }

    public <Statement> void collectLValueAssignments(Expression rhsAssigned, StatementContainer<Statement> statementContainer, LValueAssignmentCollector<Statement> lValueAssigmentCollector) {
        if (this.getNumberOfCreators() != 1) return;
        if (rhsAssigned.isSimple() || this.stackEntry.getUsageCount() == 1) {
            lValueAssigmentCollector.collect(this, statementContainer, rhsAssigned);
        } else {
            if (this.stackEntry.getUsageCount() <= 1) return;
            lValueAssigmentCollector.collectMultiUse(this, statementContainer, rhsAssigned);
        }
    }

    @Override
    public SSAIdentifiers<LValue> collectVariableMutation(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        return new SSAIdentifiers();
    }

    @Override
    public LValue replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        return this;
    }

    @Override
    public LValue applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return this;
    }

    public StackEntry getStackEntry() {
        return this.stackEntry;
    }

    public int hashCode() {
        return (int)this.id;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof StackSSALabel)) {
            return false;
        }
        return this.id == ((StackSSALabel)o).id;
    }
}

