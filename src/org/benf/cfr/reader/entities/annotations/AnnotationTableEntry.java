/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.annotations;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.annotations.ElementValue;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumper;

public class AnnotationTableEntry
implements TypeUsageCollectable {
    private final JavaTypeInstance clazz;
    private final Map<String, ElementValue> elementValueMap;

    public AnnotationTableEntry(JavaTypeInstance clazz, Map<String, ElementValue> elementValueMap) {
        this.clazz = clazz;
        this.elementValueMap = elementValueMap;
    }

    public Dumper dump(Dumper d) {
        d.print('@').dump(this.clazz);
        if (this.elementValueMap == null || this.elementValueMap.isEmpty()) return d;
        d.print('(');
        boolean first = true;
        for (Map.Entry<String, ElementValue> elementValueEntry : this.elementValueMap.entrySet()) {
            first = CommaHelp.comma(first, d);
            d.print(elementValueEntry.getKey()).print('=');
            elementValueEntry.getValue().dump(d);
        }
        d.print(')');
        return d;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.clazz);
        if (this.elementValueMap == null) return;
        for (ElementValue elementValue : this.elementValueMap.values()) {
            elementValue.collectTypeUsages(collector);
        }
    }
}

