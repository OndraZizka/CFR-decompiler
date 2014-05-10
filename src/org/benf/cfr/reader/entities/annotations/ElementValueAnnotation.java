/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.annotations;

import org.benf.cfr.reader.entities.annotations.AnnotationTableEntry;
import org.benf.cfr.reader.entities.annotations.ElementValue;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class ElementValueAnnotation
implements ElementValue {
    private final AnnotationTableEntry annotationTableEntry;

    public ElementValueAnnotation(AnnotationTableEntry annotationTableEntry) {
        this.annotationTableEntry = annotationTableEntry;
    }

    @Override
    public Dumper dump(Dumper d) {
        return this.annotationTableEntry.dump(d);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.annotationTableEntry.collectTypeUsages(collector);
    }
}

