/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.DumpableWithPrecedence;
import org.benf.cfr.reader.util.output.Dumper;

public interface Expression
extends DumpableWithPrecedence,
DeepCloneable<Expression>,
ComparableUnderEC,
TypeUsageCollectable {
    public Expression replaceSingleUsageLValues(LValueRewriter var1, SSAIdentifiers var2, StatementContainer var3);

    public Expression applyExpressionRewriter(ExpressionRewriter var1, SSAIdentifiers var2, StatementContainer var3, ExpressionRewriterFlags var4);

    public boolean isSimple();

    public void collectUsedLValues(LValueUsageCollector var1);

    public boolean canPushDownInto();

    public Expression pushDown(Expression var1, Expression var2);

    public InferredJavaType getInferredJavaType();

    @Override
    public boolean equivalentUnder(Object var1, EquivalenceConstraint var2);

    public boolean canThrow(ExceptionCheck var1);

    public Literal getComputedLiteral(Map<LValue, Literal> var1);

    @Override
    public Dumper dump(Dumper var1);
}

