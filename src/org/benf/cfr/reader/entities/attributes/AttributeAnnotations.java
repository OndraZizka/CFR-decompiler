/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.entities.annotations.AnnotationTableEntry;
import org.benf.cfr.reader.entities.attributes.AnnotationHelpers;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public abstract class AttributeAnnotations
extends Attribute
implements TypeUsageCollectable {
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_REMAINDER = 6;
    private static final long OFFSET_OF_NUMBER_OF_ANNOTATIONS = 6;
    private static final long OFFSET_OF_ANNOTATION_TABLE = 8;
    private final List<AnnotationTableEntry> annotationTableEntryList = ListFactory.newList();
    private final int length;

    public AttributeAnnotations(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
        int numAnnotations = raw.getS2At(6);
        long offset = 8;
        for (int x = 0; x < numAnnotations; ++x) {
            Pair<Long, AnnotationTableEntry> ape = AnnotationHelpers.getAnnotation(raw, offset, cp);
            offset = ape.getFirst();
            this.annotationTableEntryList.add(ape.getSecond());
        }
    }

    public List<AnnotationTableEntry> getAnnotationTableEntryList() {
        return this.annotationTableEntryList;
    }

    @Override
    public Dumper dump(Dumper d) {
        for (AnnotationTableEntry annotationTableEntry : this.annotationTableEntryList) {
            annotationTableEntry.dump(d);
            d.newln();
        }
        return d;
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        for (AnnotationTableEntry annotationTableEntry : this.annotationTableEntryList) {
            annotationTableEntry.collectTypeUsages(collector);
        }
    }
}

