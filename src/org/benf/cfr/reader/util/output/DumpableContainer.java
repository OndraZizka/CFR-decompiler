/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.util.List;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class DumpableContainer
implements Dumpable {
    private final List<? extends Dumpable> dumpables;

    public DumpableContainer(List<? extends Dumpable> dumpables) {
        this.dumpables = dumpables;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        for (Dumpable dumpable : this.dumpables) {
            dumpable.dump(dumper);
        }
        return dumper;
    }
}

