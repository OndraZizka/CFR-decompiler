/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.List;
import org.benf.cfr.reader.entities.annotations.AnnotationTableEntry;
import org.benf.cfr.reader.entities.attributes.AttributeParameterAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeInvisibleParameterAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeVisibleParameterAnnotations;
import org.benf.cfr.reader.util.output.Dumper;

public class MethodPrototypeAnnotationsHelper {
    private final AttributeRuntimeVisibleParameterAnnotations runtimeVisibleParameterAnnotations;
    private final AttributeRuntimeInvisibleParameterAnnotations runtimeInvisibleParameterAnnotations;

    public MethodPrototypeAnnotationsHelper(AttributeRuntimeVisibleParameterAnnotations runtimeVisibleParameterAnnotations, AttributeRuntimeInvisibleParameterAnnotations runtimeInvisibleParameterAnnotations) {
        this.runtimeVisibleParameterAnnotations = runtimeVisibleParameterAnnotations;
        this.runtimeInvisibleParameterAnnotations = runtimeInvisibleParameterAnnotations;
    }

    private static void addAnnotation(AttributeParameterAnnotations annotations, int idx, Dumper d) {
        List<AnnotationTableEntry> annotationTableEntries;
        if (annotations == null) {
            return;
        }
        if ((annotationTableEntries = annotations.getAnnotationsForParamIdx(idx)) == null || annotationTableEntries.isEmpty()) {
            return;
        }
        for (AnnotationTableEntry annotationTableEntry : annotationTableEntries) {
            annotationTableEntry.dump(d);
            d.print(' ');
        }
    }

    public void addAnnotationTextForParameterInto(int idx, Dumper d) {
        MethodPrototypeAnnotationsHelper.addAnnotation(this.runtimeVisibleParameterAnnotations, idx, d);
        MethodPrototypeAnnotationsHelper.addAnnotation(this.runtimeInvisibleParameterAnnotations, idx, d);
    }
}

