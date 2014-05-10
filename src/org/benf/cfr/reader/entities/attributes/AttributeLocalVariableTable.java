/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import java.util.List;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.attributes.LocalVariableEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeLocalVariableTable
extends Attribute {
    public static final String ATTRIBUTE_NAME = "LocalVariableTable";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_ENTRY_COUNT = 6;
    private static final long OFFSET_OF_ENTRIES = 8;
    private static final long OFFSET_OF_REMAINDER = 6;
    private final List<LocalVariableEntry> localVariableEntryList = ListFactory.newList();
    private final int length;

    public AttributeLocalVariableTable(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
        int numLocalVariables = raw.getS2At(6);
        long offset = 8;
        for (int x = 0; x < numLocalVariables; ++x) {
            short startPc = raw.getS2At(offset + 0);
            short length = raw.getS2At(offset + 2);
            short nameIndex = raw.getS2At(offset + 4);
            short descriptorIndex = raw.getS2At(offset + 6);
            short index = raw.getS2At(offset + 8);
            this.localVariableEntryList.add(new LocalVariableEntry(startPc, length, nameIndex, descriptorIndex, index));
            offset+=10;
        }
    }

    @Override
    public String getRawName() {
        return "LocalVariableTable";
    }

    @Override
    public Dumper dump(Dumper d) {
        return d;
    }

    public List<LocalVariableEntry> getLocalVariableEntryList() {
        return this.localVariableEntryList;
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }
}

