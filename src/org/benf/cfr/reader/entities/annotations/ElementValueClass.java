/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.annotations;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.annotations.ElementValue;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class ElementValueClass
implements ElementValue {
    private final JavaTypeInstance classType;

    public ElementValueClass(JavaTypeInstance classType) {
        this.classType = classType;
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.dump(this.classType).print(".class");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.classType);
    }
}

