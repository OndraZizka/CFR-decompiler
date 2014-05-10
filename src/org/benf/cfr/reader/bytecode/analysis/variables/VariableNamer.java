/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.variables.Ident;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;

public interface VariableNamer {
    public NamedVariable getName(int var1, Ident var2, long var3);

    public List<NamedVariable> getNamedVariables();

    public void mutatingRenameUnClash(NamedVariable var1);

    public void forceName(Ident var1, long var2, String var4);
}

