/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
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
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class InstanceOfExpression
extends AbstractExpression {
    private Expression lhs;
    private JavaTypeInstance typeInstance;

    public InstanceOfExpression(Expression lhs, ConstantPoolEntry cpe) {
        super(new InferredJavaType(RawJavaType.BOOLEAN, InferredJavaType.Source.EXPRESSION));
        this.lhs = lhs;
        ConstantPoolEntryClass cpec = (ConstantPoolEntryClass)cpe;
        this.typeInstance = cpec.getTypeInstance();
    }

    private InstanceOfExpression(InferredJavaType inferredJavaType, Expression lhs, JavaTypeInstance typeInstance) {
        super(inferredJavaType);
        this.lhs = lhs;
        this.typeInstance = typeInstance;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.lhs.collectTypeUsages(collector);
        collector.collect(this.typeInstance);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new InstanceOfExpression(this.getInferredJavaType(), cloneHelper.replaceOrClone(this.lhs), this.typeInstance);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.REL_CMP_INSTANCEOF;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return d.dump(this.lhs).print(" instanceof ").dump(this.typeInstance);
    }

    @Override
    public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.lhs = this.lhs.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.lhs = expressionRewriter.rewriteExpression(this.lhs, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    @Override
    public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
        this.lhs.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof InstanceOfExpression)) {
            return false;
        }
        InstanceOfExpression other = (InstanceOfExpression)o;
        if (!this.lhs.equals(other.lhs)) {
            return false;
        }
        if (this.typeInstance.equals(other.typeInstance)) return true;
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
        InstanceOfExpression other = (InstanceOfExpression)o;
        if (!constraint.equivalent(this.lhs, other.lhs)) {
            return false;
        }
        if (constraint.equivalent(this.typeInstance, other.typeInstance)) return true;
        return false;
    }
}

