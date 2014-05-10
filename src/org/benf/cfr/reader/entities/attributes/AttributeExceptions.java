/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeExceptions
extends Attribute {
    public static final String ATTRIBUTE_NAME = "Exceptions";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_NUMBER_OF_EXCEPTIONS = 6;
    private static final long OFFSET_OF_EXCEPTION_TABLE = 8;
    private static final long OFFSET_OF_REMAINDER = 6;
    private final List<ConstantPoolEntryClass> exceptionClassList = ListFactory.newList();
    private final int length;

    public AttributeExceptions(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
        int numExceptions = raw.getS2At(6);
        long offset = 8;
        for (int x = 0; x < numExceptions; ++x) {
            this.exceptionClassList.add(cp.getClassEntry(raw.getS2At(offset)));
            offset+=2;
        }
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        for (ConstantPoolEntryClass exceptionClass : this.exceptionClassList) {
            collector.collect(exceptionClass.getTypeInstance());
        }
    }

    @Override
    public String getRawName() {
        return "Exceptions";
    }

    @Override
    public Dumper dump(Dumper d) {
        return d;
    }

    public List<ConstantPoolEntryClass> getExceptionClassList() {
        return this.exceptionClassList;
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }
}

