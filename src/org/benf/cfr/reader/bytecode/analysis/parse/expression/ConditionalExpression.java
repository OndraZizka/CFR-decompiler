/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;

public interface ConditionalExpression
extends Expression {
    public ConditionalExpression getNegated();

    public int getSize();

    public ConditionalExpression getDemorganApplied(boolean var1);

    public Set<LValue> getLoopLValues();

    public ConditionalExpression optimiseForType();

    public ConditionalExpression simplify();
}

