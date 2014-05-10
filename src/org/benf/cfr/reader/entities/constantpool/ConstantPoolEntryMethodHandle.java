/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.bootstrap.MethodHandleBehaviour;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryMethodHandle
extends AbstractConstantPoolEntry {
    private static final long OFFSET_OF_REFERENCE_KIND = 1;
    private static final long OFFSET_OF_REFERENCE_INDEX = 2;
    private final MethodHandleBehaviour referenceKind;
    private final short referenceIndex;

    public ConstantPoolEntryMethodHandle(ConstantPool cp, ByteData data) {
        super(cp);
        this.referenceKind = MethodHandleBehaviour.decode(data.getS1At(1));
        this.referenceIndex = data.getS2At(2);
    }

    @Override
    public long getRawByteLength() {
        return 4;
    }

    @Override
    public void dump(Dumper d) {
        d.print(this.toString());
    }

    public MethodHandleBehaviour getReferenceKind() {
        return this.referenceKind;
    }

    public ConstantPoolEntryMethodRef getMethodRef() {
        return this.getCp().getMethodRefEntry(this.referenceIndex);
    }

    public String toString() {
        return "MethodHandle value=" + (Object)this.referenceKind + "," + this.referenceIndex;
    }
}

