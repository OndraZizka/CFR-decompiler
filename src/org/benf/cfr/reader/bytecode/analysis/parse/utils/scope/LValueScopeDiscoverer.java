/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils.scope;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public interface LValueScopeDiscoverer
extends LValueUsageCollector,
LValueAssignmentCollector<StructuredStatement> {
    public void enterBlock(StructuredStatement var1);

    public void leaveBlock(StructuredStatement var1);

    @Override
    public void collect(StackSSALabel var1, StatementContainer<StructuredStatement> var2, Expression var3);

    @Override
    public void collectMultiUse(StackSSALabel var1, StatementContainer<StructuredStatement> var2, Expression var3);

    @Override
    public void collectMutatedLValue(LValue var1, StatementContainer<StructuredStatement> var2, Expression var3);

    @Override
    public void collectLocalVariableAssignment(LocalVariable var1, StatementContainer<StructuredStatement> var2, Expression var3);

    @Override
    public void collect(LValue var1);
}

