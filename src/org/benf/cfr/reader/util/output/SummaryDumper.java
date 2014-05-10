/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.Method;

public interface SummaryDumper {
    public void notify(String var1);

    public void notifyError(JavaTypeInstance var1, Method var2, String var3);

    public void close();
}

