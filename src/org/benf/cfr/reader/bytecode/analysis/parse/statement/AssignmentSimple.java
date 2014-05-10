/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractAssignment;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.CreationCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.CastAction;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class AssignmentSimple
extends AbstractAssignment {
    private LValue lvalue;
    private Expression rvalue;
    private boolean initialAssign = false;

    public AssignmentSimple(LValue lvalue, Expression rvalue) {
        this.lvalue = lvalue;
        this.rvalue = lvalue.getInferredJavaType().chain(rvalue.getInferredJavaType()).performCastAction(rvalue, lvalue.getInferredJavaType());
    }

    public AssignmentSimple(InferredJavaType type, LValue lvalue, Expression rvalue) {
        this.lvalue = lvalue;
        this.rvalue = rvalue;
    }

    public void setInitialAssign(boolean initialAssign) {
        this.initialAssign = initialAssign;
    }

    public boolean isInitialAssign() {
        return this.initialAssign;
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.dump(this.lvalue).print(" = ").dump(this.rvalue).endCodeln();
    }

    @Override
    public void collectLValueAssignments(LValueAssignmentCollector<Statement> lValueAssigmentCollector) {
        this.lvalue.collectLValueAssignments(this.rvalue, this.getContainer(), lValueAssigmentCollector);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.rvalue.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public void collectObjectCreation(CreationCollector creationCollector) {
        creationCollector.collectCreation(this.lvalue, this.rvalue, this.getContainer());
    }

    @Override
    public SSAIdentifiers<LValue> collectLocallyMutatedVariables(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        return this.lvalue.collectVariableMutation(ssaIdentifierFactory);
    }

    @Override
    public LValue getCreatedLValue() {
        return this.lvalue;
    }

    @Override
    public Expression getRValue() {
        return this.rvalue;
    }

    @Override
    public boolean isSelfMutatingOperation() {
        ArithmeticOperation arithmeticOperation;
        if (!(this.rvalue instanceof ArithmeticOperation) || !(arithmeticOperation = (ArithmeticOperation)this.rvalue).isLiteralFunctionOf(this.lvalue)) return false;
        return true;
    }

    @Override
    public boolean isSelfMutatingOp1(LValue lValue, ArithOp arithOp) {
        return false;
    }

    @Override
    public Expression getPostMutation() {
        throw new IllegalStateException();
    }

    @Override
    public AbstractAssignmentExpression getInliningExpression() {
        return new AssignmentExpression(this.getCreatedLValue(), this.getRValue(), true);
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        this.lvalue = this.lvalue.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
        this.rvalue = this.rvalue.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.lvalue = expressionRewriter.rewriteExpression(this.lvalue, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.LVALUE);
        this.rvalue = expressionRewriter.rewriteExpression(this.rvalue, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new StructuredAssignment(this.lvalue, this.rvalue);
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return this.lvalue.canThrow(caught) || this.rvalue.canThrow(caught);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AssignmentSimple)) {
            return false;
        }
        AssignmentSimple other = (AssignmentSimple)o;
        return this.lvalue.equals(other.lvalue) && this.rvalue.equals(other.rvalue);
    }

    @Override
    public final boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        AssignmentSimple other = (AssignmentSimple)o;
        if (!constraint.equivalent(this.lvalue, other.lvalue)) {
            return false;
        }
        if (constraint.equivalent(this.rvalue, other.rvalue)) return true;
        return false;
    }
}

