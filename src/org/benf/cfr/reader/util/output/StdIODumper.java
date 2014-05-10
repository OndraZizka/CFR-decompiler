/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.io.PrintStream;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.output.StreamDumper;

public class StdIODumper
extends StreamDumper {
    public StdIODumper(TypeUsageInformation typeUsageInformation) {
        super(typeUsageInformation);
    }

    @Override
    protected void write(String s) {
        System.out.print(s);
    }

    @Override
    public void addSummaryError(Method method, String s) {
    }

    @Override
    public void close() {
    }
}

