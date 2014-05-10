/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.innerclass.InnerClassAttributeInfo;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeInnerClasses
extends Attribute {
    public static final String ATTRIBUTE_NAME = "InnerClasses";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_REMAINDER = 6;
    private static final long OFFSET_OF_NUMBER_OF_CLASSES = 6;
    private static final long OFFSET_OF_CLASS_ARRAY = 8;
    private final int length;
    private final List<InnerClassAttributeInfo> innerClassAttributeInfoList = ListFactory.newList();

    private static JavaTypeInstance getOptClass(int idx, ConstantPool cp) {
        if (idx != 0) return cp.getClassEntry(idx).getTypeInstance();
        return null;
    }

    private static String getOptName(int idx, ConstantPool cp) {
        if (idx != 0) return cp.getUTF8Entry(idx).getValue();
        return null;
    }

    private static Pair<JavaTypeInstance, JavaTypeInstance> getInnerOuter(int idxinner, int idxouter, ConstantPool cp) {
        if (idxinner == 0 || idxouter == 0) {
            return Pair.make(AttributeInnerClasses.getOptClass(idxinner, cp), AttributeInnerClasses.getOptClass(idxouter, cp));
        }
        ConstantPoolEntryClass cpecInner = cp.getClassEntry(idxinner);
        ConstantPoolEntryClass cpecOuter = cp.getClassEntry(idxouter);
        JavaTypeInstance innerType = cpecInner.getTypeInstanceKnownOuter(cpecOuter);
        JavaTypeInstance outerType = cpecInner.getTypeInstanceKnownInner(cpecOuter);
        return Pair.make(innerType, outerType);
    }

    public AttributeInnerClasses(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
        int numberInnerClasses = raw.getS2At(6);
        long offset = 8;
        for (int x = 0; x < numberInnerClasses; ++x) {
            JavaTypeInstance outerClassType;
            short innerClassInfoIdx = raw.getS2At(offset);
            short outerClassInfoIdx = raw.getS2At(offset+=2);
            short innerNameIdx = raw.getS2At(offset+=2);
            short innerAccessFlags = raw.getS2At(offset+=2);
            offset+=2;
            Pair<JavaTypeInstance, JavaTypeInstance> innerOuter = AttributeInnerClasses.getInnerOuter(innerClassInfoIdx, outerClassInfoIdx, cp);
            JavaTypeInstance innerClassType = innerOuter.getFirst();
            if ((outerClassType = innerOuter.getSecond()) == null) {
                boolean methodScoped = innerNameIdx == 0;
                innerClassType.getInnerClassHereInfo().markMethodScoped(methodScoped);
            }
            this.innerClassAttributeInfoList.add(new InnerClassAttributeInfo(innerClassType, outerClassType, AttributeInnerClasses.getOptName(innerNameIdx, cp), AccessFlag.build(innerAccessFlags)));
        }
    }

    @Override
    public String getRawName() {
        return "InnerClasses";
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.print("InnerClasses");
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }

    public List<InnerClassAttributeInfo> getInnerClassAttributeInfoList() {
        return this.innerClassAttributeInfoList;
    }

    public String toString() {
        return "InnerClasses";
    }
}

