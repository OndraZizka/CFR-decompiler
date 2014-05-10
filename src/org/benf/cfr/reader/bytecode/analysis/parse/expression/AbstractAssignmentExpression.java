/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPostMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;

public abstract class AbstractAssignmentExpression
extends AbstractExpression {
    public AbstractAssignmentExpression(InferredJavaType inferredJavaType) {
        super(inferredJavaType);
    }

    public abstract boolean isSelfMutatingOp1(LValue var1, ArithOp var2);

    public abstract ArithmeticPostMutationOperation getPostMutation();
}

