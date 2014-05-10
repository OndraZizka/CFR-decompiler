/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.util.output.SummaryDumper;

public class NopSummaryDumper
implements SummaryDumper {
    @Override
    public void notify(String message) {
    }

    @Override
    public void notifyError(JavaTypeInstance controllingType, Method method, String error) {
    }

    @Override
    public void close() {
    }
}

