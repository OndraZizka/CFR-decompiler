/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryMethodType
extends AbstractConstantPoolEntry {
    private static final long OFFSET_OF_DESCRIPTOR_INDEX = 1;
    private final short descriptorIndex;

    public ConstantPoolEntryMethodType(ConstantPool cp, ByteData data) {
        super(cp);
        this.descriptorIndex = data.getS2At(1);
    }

    @Override
    public ConstantPool getCp() {
        return super.getCp();
    }

    @Override
    public long getRawByteLength() {
        return 3;
    }

    @Override
    public void dump(Dumper d) {
        d.print(this.toString());
    }

    public ConstantPoolEntryUTF8 getDescriptor() {
        return this.getCp().getUTF8Entry(this.descriptorIndex);
    }

    public String toString() {
        return "MethodType value=" + this.descriptorIndex;
    }
}

