/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public interface NamedVariable
extends Dumpable {
    public void forceName(String var1);

    public String getStringName();

    public boolean isGoodName();

    @Override
    public Dumper dump(Dumper var1);
}

