/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.DumpableWithPrecedence;

public interface LValue
extends DumpableWithPrecedence,
DeepCloneable<LValue>,
TypeUsageCollectable {
    public int getNumberOfCreators();

    public <T> void collectLValueAssignments(Expression var1, StatementContainer<T> var2, LValueAssignmentCollector<T> var3);

    public void collectLValueUsage(LValueUsageCollector var1);

    public SSAIdentifiers<LValue> collectVariableMutation(SSAIdentifierFactory<LValue> var1);

    public LValue replaceSingleUsageLValues(LValueRewriter var1, SSAIdentifiers var2, StatementContainer var3);

    public LValue applyExpressionRewriter(ExpressionRewriter var1, SSAIdentifiers var2, StatementContainer var3, ExpressionRewriterFlags var4);

    public InferredJavaType getInferredJavaType();

    public boolean canThrow(ExceptionCheck var1);
}

