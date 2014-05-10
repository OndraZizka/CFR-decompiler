/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.ClassNameUtils;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLiteral;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryClass
extends AbstractConstantPoolEntry
implements ConstantPoolEntryLiteral {
    private final long OFFSET_OF_NAME_INDEX = 1;
    final short nameIndex;
    transient JavaTypeInstance javaTypeInstance = null;

    public ConstantPoolEntryClass(ConstantPool cp, ByteData data) {
        super(cp);
        this.nameIndex = data.getS2At(1);
    }

    @Override
    public long getRawByteLength() {
        return 3;
    }

    public String toString() {
        return "CONSTANT_Class " + this.nameIndex;
    }

    public String getTextPath() {
        return ClassNameUtils.convertFromPath(this.getCp().getUTF8Entry(this.nameIndex).getValue()) + ".class";
    }

    public String getFilePath() {
        return this.getCp().getUTF8Entry(this.nameIndex).getValue() + ".class";
    }

    @Override
    public void dump(Dumper d) {
        d.print("Class " + this.getCp().getUTF8Entry(this.nameIndex).getValue());
    }

    public String getPackageName() {
        String full = ClassNameUtils.convertFromPath(this.getCp().getUTF8Entry(this.nameIndex).getValue());
        int idx = full.lastIndexOf(46);
        if (idx != -1) return full.substring(0, idx);
        return "";
    }

    public JavaTypeInstance convertFromString(String rawType) {
        if (!rawType.startsWith("[")) return this.getCp().getClassCache().getRefClassFor(ClassNameUtils.convertFromPath(rawType));
        return ConstantPoolUtils.decodeTypeTok(rawType, this.getCp());
    }

    public JavaTypeInstance getTypeInstance() {
        if (this.javaTypeInstance != null) return this.javaTypeInstance;
        String rawType = this.getCp().getUTF8Entry(this.nameIndex).getValue();
        this.javaTypeInstance = this.convertFromString(rawType);
        return this.javaTypeInstance;
    }

    public JavaTypeInstance getTypeInstanceKnownOuter(ConstantPoolEntryClass outer) {
        if (this.javaTypeInstance != null) {
            return this.javaTypeInstance;
        }
        String thisInnerType = this.getCp().getUTF8Entry(this.nameIndex).getValue();
        String thisOuterType = this.getCp().getUTF8Entry(outer.nameIndex).getValue();
        Pair<JavaRefTypeInstance, JavaRefTypeInstance> pair = this.getCp().getClassCache().getRefClassForInnerOuterPair(thisInnerType, thisOuterType);
        this.javaTypeInstance = pair.getFirst();
        return this.javaTypeInstance;
    }

    public JavaTypeInstance getTypeInstanceKnownInner(ConstantPoolEntryClass inner) {
        if (this.javaTypeInstance != null) {
            return this.javaTypeInstance;
        }
        String thisInnerType = this.getCp().getUTF8Entry(inner.nameIndex).getValue();
        String thisOuterType = this.getCp().getUTF8Entry(this.nameIndex).getValue();
        Pair<JavaRefTypeInstance, JavaRefTypeInstance> pair = this.getCp().getClassCache().getRefClassForInnerOuterPair(thisInnerType, thisOuterType);
        this.javaTypeInstance = pair.getSecond();
        return this.javaTypeInstance;
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }
}

