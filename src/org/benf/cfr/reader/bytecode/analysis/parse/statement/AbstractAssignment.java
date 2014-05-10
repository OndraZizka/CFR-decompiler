/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;

public abstract class AbstractAssignment
extends AbstractStatement {
    public abstract boolean isSelfMutatingOperation();

    public abstract boolean isSelfMutatingOp1(LValue var1, ArithOp var2);

    public abstract Expression getPostMutation();

    public abstract AbstractAssignmentExpression getInliningExpression();
}

