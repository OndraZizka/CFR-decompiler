/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;

public interface LValueRewriter<T> {
    public Expression getLValueReplacement(LValue var1, SSAIdentifiers var2, StatementContainer<T> var3);

    public boolean explicitlyReplaceThisLValue(LValue var1);
}

