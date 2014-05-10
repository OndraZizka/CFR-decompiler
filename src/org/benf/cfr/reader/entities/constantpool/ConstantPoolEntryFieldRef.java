/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryFieldRef
extends AbstractConstantPoolEntry {
    private final long OFFSET_OF_CLASS_INDEX = 1;
    private final long OFFSET_OF_NAME_AND_TYPE_INDEX = 3;
    final short classIndex;
    final short nameAndTypeIndex;
    JavaTypeInstance cachedDecodedType;

    public ConstantPoolEntryFieldRef(ConstantPool cp, ByteData data) {
        super(cp);
        this.classIndex = data.getS2At(1);
        this.nameAndTypeIndex = data.getS2At(3);
    }

    @Override
    public long getRawByteLength() {
        return 5;
    }

    @Override
    public void dump(Dumper d) {
        ConstantPool cp = this.getCp();
        d.print("Field " + cp.getNameAndTypeEntry(this.nameAndTypeIndex).getName().getValue() + ":" + this.getJavaTypeInstance());
    }

    public ConstantPoolEntryClass getClassEntry() {
        return this.getCp().getClassEntry(this.classIndex);
    }

    public ConstantPoolEntryNameAndType getNameAndTypeEntry() {
        return this.getCp().getNameAndTypeEntry(this.nameAndTypeIndex);
    }

    public String getLocalName() {
        return this.getCp().getNameAndTypeEntry(this.nameAndTypeIndex).getName().getValue();
    }

    public JavaTypeInstance getJavaTypeInstance() {
        if (this.cachedDecodedType != null) return this.cachedDecodedType;
        ConstantPool cp = this.getCp();
        this.cachedDecodedType = ConstantPoolUtils.decodeTypeTok(cp.getNameAndTypeEntry(this.nameAndTypeIndex).getDescriptor().getValue(), cp);
        return this.cachedDecodedType;
    }

    public StackType getStackType() {
        return this.getJavaTypeInstance().getStackType();
    }

    public String toString() {
        return "ConstantPool_FieldRef [classIndex:" + this.classIndex + ", nameAndTypeIndex:" + this.nameAndTypeIndex + "]";
    }
}

