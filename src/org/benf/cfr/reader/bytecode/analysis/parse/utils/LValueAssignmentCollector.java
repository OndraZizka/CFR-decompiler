/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;

public interface LValueAssignmentCollector<T> {
    public void collect(StackSSALabel var1, StatementContainer<T> var2, Expression var3);

    public void collectMultiUse(StackSSALabel var1, StatementContainer<T> var2, Expression var3);

    public void collectMutatedLValue(LValue var1, StatementContainer<T> var2, Expression var3);

    public void collectLocalVariableAssignment(LocalVariable var1, StatementContainer<T> var2, Expression var3);
}

