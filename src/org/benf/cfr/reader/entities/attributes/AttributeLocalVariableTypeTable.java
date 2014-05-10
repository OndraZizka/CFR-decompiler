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

public class AttributeLocalVariableTypeTable
extends Attribute {
    public static final String ATTRIBUTE_NAME = "LocalVariableTypeTable";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_REMAINDER = 6;
    private final List<LocalVariableEntry> localVariableEntryList = ListFactory.newList();
    private final int length;

    public AttributeLocalVariableTypeTable(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
    }

    @Override
    public String getRawName() {
        return "LocalVariableTypeTable";
    }

    @Override
    public Dumper dump(Dumper d) {
        return d;
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }
}

