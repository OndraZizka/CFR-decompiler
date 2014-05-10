/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class FormalTypeParameter
implements Dumpable,
TypeUsageCollectable {
    String name;
    JavaTypeInstance classBound;
    JavaTypeInstance interfaceBound;

    public FormalTypeParameter(String name, JavaTypeInstance classBound, JavaTypeInstance interfaceBound) {
        this.name = name;
        this.classBound = classBound;
        this.interfaceBound = interfaceBound;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.classBound);
        collector.collect(this.interfaceBound);
    }

    @Override
    public Dumper dump(Dumper d) {
        JavaTypeInstance dispInterface = this.classBound == null ? this.interfaceBound : this.classBound;
        d.print(this.name);
        if (dispInterface == null || "java.lang.Object".equals(dispInterface.getRawName())) return d;
        d.print(" extends ").dump(dispInterface);
        return d;
    }

    public String toString() {
        throw new IllegalStateException();
    }
}

