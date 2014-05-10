/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;

public interface ExceptionCheck {
    public boolean checkAgainst(Set<? extends JavaTypeInstance> var1);

    public boolean checkAgainst(AbstractFunctionInvokation var1);

    public boolean checkAgainstException(Expression var1);

    public boolean mightCatchUnchecked();
}

