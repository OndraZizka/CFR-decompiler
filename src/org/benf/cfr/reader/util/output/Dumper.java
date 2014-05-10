/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.output.Dumpable;

public interface Dumper {
    public TypeUsageInformation getTypeUsageInformation();

    public void printLabel(String var1);

    public void enqueuePendingCarriageReturn();

    public Dumper removePendingCarriageReturn();

    public Dumper print(String var1);

    public Dumper print(char var1);

    public Dumper newln();

    public Dumper endCodeln();

    public void line();

    public int getIndent();

    public void indent(int var1);

    public void dump(List<? extends Dumpable> var1);

    public Dumper dump(JavaTypeInstance var1);

    public Dumper dump(Dumpable var1);

    public void close();

    public void addSummaryError(Method var1, String var2);

    public boolean canEmitClass(JavaTypeInstance var1);

    public static class CannotCreate
    extends RuntimeException {
        public CannotCreate(Throwable throwable) {
            super(throwable);
        }

        @Override
        public String toString() {
            return "Cannot create dumper " + super.toString();
        }
    }

}

