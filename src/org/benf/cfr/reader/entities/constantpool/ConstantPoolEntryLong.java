/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLiteral;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryLong
extends AbstractConstantPoolEntry
implements ConstantPoolEntryLiteral {
    private final long value;

    public ConstantPoolEntryLong(ConstantPool cp, ByteData data) {
        super(cp);
        this.value = data.getLongAt(1);
    }

    @Override
    public long getRawByteLength() {
        return 9;
    }

    @Override
    public void dump(Dumper d) {
        d.print("CONSTANT_Long " + this.value);
    }

    public String toString() {
        return "CONSTANT_Long[" + this.value + "]";
    }

    public long getValue() {
        return this.value;
    }

    @Override
    public StackType getStackType() {
        return StackType.LONG;
    }
}

