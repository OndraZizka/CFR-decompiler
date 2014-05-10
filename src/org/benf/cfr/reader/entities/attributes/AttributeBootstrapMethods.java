/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import java.util.List;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.bootstrap.BootstrapMethodInfo;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeBootstrapMethods
extends Attribute {
    public static final String ATTRIBUTE_NAME = "BootstrapMethods";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_REMAINDER = 6;
    private static final long OFFSET_OF_NUM_METHODS = 6;
    private final int length;
    private final List<BootstrapMethodInfo> methodInfoList;

    public AttributeBootstrapMethods(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
        this.methodInfoList = AttributeBootstrapMethods.decodeMethods(raw, cp);
    }

    public BootstrapMethodInfo getBootStrapMethodInfo(int idx) {
        if (idx >= 0 && idx < this.methodInfoList.size()) return this.methodInfoList.get(idx);
        throw new IllegalArgumentException("Invalid bootstrap index.");
    }

    private static List<BootstrapMethodInfo> decodeMethods(ByteData raw, ConstantPool cp) {
        List res = ListFactory.newList();
        int numMethods = raw.getS2At(6);
        long offset = 8;
        for (int x = 0; x < numMethods; ++x) {
            short methodRef = raw.getS2At(offset);
            ConstantPoolEntryMethodHandle methodHandle = cp.getMethodHandleEntry(methodRef);
            int numBootstrapArguments = raw.getS2At(offset+=2);
            offset+=2;
            ConstantPoolEntry[] bootstrapArguments = new ConstantPoolEntry[numBootstrapArguments];
            for (int y = 0; y < numBootstrapArguments; ++y) {
                bootstrapArguments[y] = cp.getEntry(raw.getS2At(offset));
                offset+=2;
            }
            res.add((BootstrapMethodInfo)new BootstrapMethodInfo(methodHandle, bootstrapArguments, cp));
        }
        return res;
    }

    @Override
    public String getRawName() {
        return "BootstrapMethods";
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.print("BootstrapMethods");
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }

    public String toString() {
        return "BootstrapMethods";
    }
}

