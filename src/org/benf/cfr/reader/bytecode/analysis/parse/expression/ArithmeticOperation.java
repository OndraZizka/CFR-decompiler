/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMutatingAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticMonOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPreMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CompOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ComparisonOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.rewriteinterface.BoxingProcessor;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.BasicExceptions;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public class ArithmeticOperation
extends AbstractExpression
implements BoxingProcessor {
    private Expression lhs;
    private Expression rhs;
    private final ArithOp op;

    public ArithmeticOperation(Expression lhs, Expression rhs, ArithOp op) {
        super(ArithmeticOperation.inferredType(lhs.getInferredJavaType(), rhs.getInferredJavaType(), op));
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    public ArithmeticOperation(InferredJavaType knownType, Expression lhs, Expression rhs, ArithOp op) {
        super(knownType);
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.lhs.collectTypeUsages(collector);
        this.rhs.collectTypeUsages(collector);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ArithmeticOperation(cloneHelper.replaceOrClone(this.lhs), cloneHelper.replaceOrClone(this.rhs), this.op);
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     */
    private static InferredJavaType inferredType(InferredJavaType a, InferredJavaType b, ArithOp op) {
        InferredJavaType.useInArithOp(a, b, op);
        rawJavaType = a.getRawType();
        if (rawJavaType.getStackType().equals((Object)StackType.INT) == false) return new InferredJavaType(rawJavaType, InferredJavaType.Source.OPERATION);
        switch (1.$SwitchMap$org$benf$cfr$reader$bytecode$analysis$parse$expression$ArithOp[op.ordinal()]) {
            case 1: 
            case 2: 
            case 3: {
                if (!rawJavaType.equals((Object)RawJavaType.BOOLEAN)) ** break;
                return new InferredJavaType(rawJavaType, InferredJavaType.Source.OPERATION);
            }
            default: {
                rawJavaType = RawJavaType.INT;
            }
        }
        return new InferredJavaType(rawJavaType, InferredJavaType.Source.OPERATION);
    }

    @Override
    public Precedence getPrecedence() {
        return this.op.getPrecedence();
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        this.lhs.dumpWithOuterPrecedence(d, this.getPrecedence());
        d.print(" " + this.op.getShowAs() + " ");
        this.rhs.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d;
    }

    private boolean isLValueExprFor(LValueExpression expression, LValue lValue) {
        LValue contained = expression.getLValue();
        return lValue.equals(contained);
    }

    public boolean isLiteralFunctionOf(LValue lValue) {
        if (this.lhs instanceof LValueExpression && this.rhs instanceof Literal) {
            return this.isLValueExprFor((LValueExpression)this.lhs, lValue);
        }
        if (!(this.rhs instanceof LValueExpression) || !(this.lhs instanceof Literal)) return false;
        return this.isLValueExprFor((LValueExpression)this.rhs, lValue);
    }

    public boolean isXorM1() {
        return this.op == ArithOp.XOR && this.rhs.equals(Literal.MINUS_ONE);
    }

    public Expression getReplacementXorM1() {
        return new ArithmeticMonOperation(this.lhs, ArithOp.NEG);
    }

    public boolean isMutationOf(LValue lValue) {
        if (!(this.lhs instanceof LValueExpression)) {
            return false;
        }
        if (!this.isLValueExprFor((LValueExpression)this.lhs, lValue)) {
            return false;
        }
        if (!this.op.isTemporary()) return true;
        return false;
    }

    public AbstractMutatingAssignmentExpression getMutationOf(LValue lValue) {
        if (!this.isMutationOf(lValue)) {
            throw new ConfusedCFRException("Can't get a mutation where none exists");
        }
        if (this.lhs.getInferredJavaType().getJavaTypeInstance() == RawJavaType.BOOLEAN || !Literal.equalsAnyOne(this.rhs)) return new ArithmeticMutationOperation(lValue, this.rhs, this.op);
        return new ArithmeticPreMutationOperation(lValue, this.op);
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.lhs = this.lhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        this.rhs = this.rhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.lhs = expressionRewriter.rewriteExpression(this.lhs, ssaIdentifiers, statementContainer, flags);
        this.rhs = expressionRewriter.rewriteExpression(this.rhs, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.lhs.collectUsedLValues(lValueUsageCollector);
        this.rhs.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean canPushDownInto() {
        return this.op.isTemporary();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof ArithmeticOperation)) {
            return false;
        }
        ArithmeticOperation other = (ArithmeticOperation)o;
        if (this.op != other.op) {
            return false;
        }
        if (!this.lhs.equals(other.lhs)) {
            return false;
        }
        if (this.rhs.equals(other.rhs)) return true;
        return false;
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
        ArithmeticOperation other = (ArithmeticOperation)o;
        if (this.op != other.op) {
            return false;
        }
        if (!constraint.equivalent(this.lhs, other.lhs)) {
            return false;
        }
        if (constraint.equivalent(this.rhs, other.rhs)) return true;
        return false;
    }

    private static CompOp rewriteXCMPCompOp(CompOp from, int on) {
        if (on == 0) {
            return from;
        }
        if (on < 0) {
            switch (from) {
                case LT: {
                    throw new IllegalStateException("Bad CMP");
                }
                case LTE: {
                    return CompOp.LT;
                }
                case GTE: {
                    throw new IllegalStateException("Bad CMP");
                }
                case GT: {
                    return CompOp.GTE;
                }
                case EQ: {
                    return CompOp.LT;
                }
                case NE: {
                    return CompOp.GTE;
                }
            }
            throw new IllegalStateException("Unknown enum");
        }
        switch (1.$SwitchMap$org$benf$cfr$reader$bytecode$analysis$parse$expression$CompOp[from.ordinal()]) {
            case 1: {
                return CompOp.LTE;
            }
            case 2: {
                throw new IllegalStateException("Bad CMP");
            }
            case 3: {
                return CompOp.GT;
            }
            case 4: {
                throw new IllegalStateException("Bad CMP");
            }
            case 5: {
                return CompOp.GT;
            }
            case 6: {
                return CompOp.LTE;
            }
        }
        throw new IllegalStateException("Unknown enum");
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return this.lhs.canThrow(caught) || this.rhs.canThrow(caught) || caught.checkAgainst(BasicExceptions.instances);
    }

    @Override
    public boolean rewriteBoxing(PrimitiveBoxingRewriter boxingRewriter) {
        this.lhs = boxingRewriter.sugarUnboxing(this.lhs);
        this.rhs = boxingRewriter.sugarUnboxing(this.rhs);
        return false;
    }

    @Override
    public void applyNonArgExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
    }

    public Expression getLhs() {
        return this.lhs;
    }

    public Expression getRhs() {
        return this.rhs;
    }

    public ArithOp getOp() {
        return this.op;
    }

    @Override
    public Expression pushDown(Expression toPush, Expression parent) {
        Literal literal;
        TypedLiteral typedLiteral;
        if (!(parent instanceof ComparisonOperation)) {
            return null;
        }
        if (!this.op.isTemporary()) {
            return null;
        }
        if (!(toPush instanceof Literal)) {
            throw new ConfusedCFRException("Pushing with a non-literal as pushee.");
        }
        ComparisonOperation comparisonOperation = (ComparisonOperation)parent;
        CompOp compOp = comparisonOperation.getOp();
        if ((typedLiteral = (literal = (Literal)toPush).getValue()).getType() != TypedLiteral.LiteralType.Integer) {
            throw new ConfusedCFRException("<xCMP> , non integer!");
        }
        int litVal = (Integer)typedLiteral.getValue();
        switch (litVal) {
            case -1: 
            case 0: 
            case 1: {
                break;
            }
            default: {
                throw new ConfusedCFRException("Invalid literal value " + litVal + " in xCMP");
            }
        }
        switch (this.op) {
            case DCMPG: 
            case FCMPG: 
            case DCMPL: 
            case FCMPL: 
            case LCMP: {
                break;
            }
            default: {
                throw new ConfusedCFRException("Shouldn't be here.");
            }
        }
        compOp = ArithmeticOperation.rewriteXCMPCompOp(compOp, litVal);
        return new ComparisonOperation(this.lhs, this.rhs, compOp);
    }

}

