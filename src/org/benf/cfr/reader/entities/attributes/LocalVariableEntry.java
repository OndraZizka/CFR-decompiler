/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

public class LocalVariableEntry {
    private final short startPc;
    private final short length;
    private final short nameIndex;
    private final short descriptorIndex;
    private final short index;

    public LocalVariableEntry(short startPc, short length, short nameIndex, short descriptorIndex, short index) {
        this.startPc = startPc;
        this.length = length;
        this.nameIndex = nameIndex;
        this.descriptorIndex = descriptorIndex;
        this.index = index;
    }

    public short getStartPc() {
        return this.startPc;
    }

    public short getLength() {
        return this.length;
    }

    public short getNameIndex() {
        return this.nameIndex;
    }

    public short getDescriptorIndex() {
        return this.descriptorIndex;
    }

    public short getIndex() {
        return this.index;
    }
}

