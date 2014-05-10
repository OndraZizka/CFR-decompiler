/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryInvokeDynamic
extends AbstractConstantPoolEntry {
    private static final long OFFSET_OF_BOOTSTRAP_METHOD_ATTR_INDEX = 1;
    private static final long OFFSET_OF_NAME_AND_TYPE_INDEX = 3;
    private final short bootstrapMethodAttrIndex;
    private final short nameAndTypeIndex;

    public ConstantPoolEntryInvokeDynamic(ConstantPool cp, ByteData data) {
        super(cp);
        this.bootstrapMethodAttrIndex = data.getS2At(1);
        this.nameAndTypeIndex = data.getS2At(3);
    }

    @Override
    public long getRawByteLength() {
        return 5;
    }

    @Override
    public void dump(Dumper d) {
        d.print(this.toString());
    }

    public short getBootstrapMethodAttrIndex() {
        return this.bootstrapMethodAttrIndex;
    }

    public ConstantPoolEntryNameAndType getNameAndTypeEntry() {
        return this.getCp().getNameAndTypeEntry(this.nameAndTypeIndex);
    }

    public String toString() {
        return "InvokeDynamic value=" + this.bootstrapMethodAttrIndex + "," + this.nameAndTypeIndex;
    }
}

