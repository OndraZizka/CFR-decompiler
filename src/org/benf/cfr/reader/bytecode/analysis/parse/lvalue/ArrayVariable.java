/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.lvalue;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public class ArrayVariable
extends AbstractLValue {
    private ArrayIndex arrayIndex;

    public ArrayVariable(ArrayIndex arrayIndex) {
        super(arrayIndex.getInferredJavaType());
        this.arrayIndex = arrayIndex;
    }

    @Override
    public LValue deepClone(CloneHelper cloneHelper) {
        return new ArrayVariable((ArrayIndex)cloneHelper.replaceOrClone(this.arrayIndex));
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.arrayIndex.collectTypeUsages(collector);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.arrayIndex.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public int getNumberOfCreators() {
        throw new ConfusedCFRException("NYI");
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return this.arrayIndex.dump(d);
    }

    public ArrayIndex getArrayIndex() {
        return this.arrayIndex;
    }

    public void collectLValueAssignments(Expression assignedTo, StatementContainer statementContainer, LValueAssignmentCollector lValueAssigmentCollector) {
    }

    @Override
    public LValue replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.arrayIndex = (ArrayIndex)this.arrayIndex.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public LValue applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.arrayIndex = (ArrayIndex)this.arrayIndex.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, statementContainer, ExpressionRewriterFlags.RVALUE);
        return this;
    }

    @Override
    public SSAIdentifiers<LValue> collectVariableMutation(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        return new SSAIdentifiers();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArrayVariable)) {
            return false;
        }
        ArrayVariable other = (ArrayVariable)o;
        return this.arrayIndex.equals(other.arrayIndex);
    }
}

