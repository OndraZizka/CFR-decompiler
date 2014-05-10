/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryNameAndType
extends AbstractConstantPoolEntry {
    private final long OFFSET_OF_NAME_INDEX = 1;
    private final long OFFSET_OF_DESCRIPTOR_INDEX = 3;
    private final short nameIndex;
    private final short descriptorIndex;
    private StackDelta[] stackDelta = new StackDelta[2];

    public ConstantPoolEntryNameAndType(ConstantPool cp, ByteData data) {
        super(cp);
        this.nameIndex = data.getS2At(1);
        this.descriptorIndex = data.getS2At(3);
    }

    @Override
    public long getRawByteLength() {
        return 5;
    }

    @Override
    public void dump(Dumper d) {
        d.print("CONSTANT_NameAndType nameIndex=" + this.nameIndex + ", descriptorIndex=" + this.descriptorIndex);
    }

    public String toString() {
        return "CONSTANT_NameAndType nameIndex=" + this.nameIndex + ", descriptorIndex=" + this.descriptorIndex;
    }

    public ConstantPoolEntryUTF8 getName() {
        return this.getCp().getUTF8Entry(this.nameIndex);
    }

    public ConstantPoolEntryUTF8 getDescriptor() {
        return this.getCp().getUTF8Entry(this.descriptorIndex);
    }

    public StackDelta getStackDelta(boolean member) {
        int idx = member ? 1 : 0;
        ConstantPool cp = this.getCp();
        if (this.stackDelta[idx] != null) return this.stackDelta[idx];
        this.stackDelta[idx] = ConstantPoolUtils.parseMethodPrototype(member, cp.getUTF8Entry(this.descriptorIndex), cp);
        return this.stackDelta[idx];
    }
}

