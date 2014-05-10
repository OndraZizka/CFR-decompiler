/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;

public class ExceptionCheckSimple
implements ExceptionCheck {
    @Override
    public boolean checkAgainst(Set<? extends JavaTypeInstance> thrown) {
        return true;
    }

    @Override
    public boolean checkAgainst(AbstractFunctionInvokation functionInvokation) {
        return true;
    }

    @Override
    public boolean checkAgainstException(Expression expression) {
        return true;
    }

    @Override
    public boolean mightCatchUnchecked() {
        return true;
    }
}

