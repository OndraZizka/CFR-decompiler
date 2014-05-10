/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryDouble;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryFieldRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryFloat;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryInteger;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryInvokeDynamic;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLong;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryString;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.bytestream.OffsettingByteData;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.output.LoggerFactory;

public class ConstantPool {
    private static final Logger logger = LoggerFactory.create(ConstantPool.class);
    private final long length;
    private final List<ConstantPoolEntry> entries;
    private final Options options;
    private final DCCommonState dcCommonState;
    private final ClassCache classCache;
    private final ClassFile classFile;
    private String comparisonKey;
    private boolean isLoaded = false;
    private final int idx = ConstantPool.sidx++;
    private static int sidx = 0;

    public ConstantPool(ClassFile classFile, DCCommonState dcCommonState, ByteData raw, short count) {
        this.classFile = classFile;
        this.options = dcCommonState.getOptions();
        ArrayList<ConstantPoolEntry> res = new ArrayList<ConstantPoolEntry>();
        count = (short)(count - 1);
        res.ensureCapacity(count);
        this.length = this.processRaw(raw, count, (List<ConstantPoolEntry>)res);
        this.entries = res;
        this.dcCommonState = dcCommonState;
        this.classCache = dcCommonState.getClassCache();
        this.isLoaded = true;
    }

    public DCCommonState getDCCommonState() {
        return this.dcCommonState;
    }

    public boolean isLoaded() {
        return this.isLoaded;
    }

    private long processRaw(ByteData raw, short count, List<ConstantPoolEntry> tgt) {
        OffsettingByteData data = raw.getOffsettingOffsetData(0);
        ConstantPool.logger.info("Processing " + count + " constpool entries.");
        short x = 0;
        while (x < count) {
            ConstantPoolEntry cpe;
            ntry$Type type = ntry$Type.get(data.getS1At(0));
            switch (type) {
                case CPT_NameAndType: {
                    cpe = new ConstantPoolEntryNameAndType(this, data);
                    break;
                }
                case CPT_String: {
                    cpe = new ConstantPoolEntryString(this, data);
                    break;
                }
                case CPT_FieldRef: {
                    cpe = new ConstantPoolEntryFieldRef(this, data);
                    break;
                }
                case CPT_MethodRef: {
                    cpe = new ConstantPoolEntryMethodRef(this, data, false);
                    break;
                }
                case CPT_InterfaceMethodRef: {
                    cpe = new ConstantPoolEntryMethodRef(this, data, true);
                    break;
                }
                case CPT_Class: {
                    cpe = new ConstantPoolEntryClass(this, data);
                    break;
                }
                case CPT_Double: {
                    cpe = new ConstantPoolEntryDouble(this, data);
                    break;
                }
                case CPT_Float: {
                    cpe = new ConstantPoolEntryFloat(this, data);
                    break;
                }
                case CPT_Long: {
                    cpe = new ConstantPoolEntryLong(this, data);
                    break;
                }
                case CPT_Integer: {
                    cpe = new ConstantPoolEntryInteger(this, data);
                    break;
                }
                case CPT_UTF8: {
                    cpe = new ConstantPoolEntryUTF8(this, data, this.options);
                    break;
                }
                case CPT_MethodHandle: {
                    cpe = new ConstantPoolEntryMethodHandle(this, data);
                    break;
                }
                case CPT_MethodType: {
                    cpe = new ConstantPoolEntryMethodType(this, data);
                    break;
                }
                case CPT_InvokeDynamic: {
                    cpe = new ConstantPoolEntryInvokeDynamic(this, data);
                    break;
                }
                default: {
                    throw new ConfusedCFRException("Invalid constant pool entry : " + (Object)type);
                }
            }
            ConstantPool.logger.info("" + (x + 1) + " : " + cpe);
            tgt.add((ConstantPoolEntryNameAndType)cpe);
            switch (1.$SwitchMap$org$benf$cfr$reader$entities$constantpool$ConstantPoolEntry$Type[type.ordinal()]) {
                case 7: 
                case 9: {
                    tgt.add(null);
                    x = (short)(x + 1);
                }
            }
            long size = cpe.getRawByteLength();
            data.advance(size);
            x = (short)(x + 1);
        }
        return data.getOffset();
    }

    public long getRawByteLength() {
        return this.length;
    }

    public ConstantPoolEntry getEntry(int index) {
        if (index != 0) return this.entries.get(index - 1);
        throw new ConfusedCFRException("Attempt to fetch element 0 from constant pool");
    }

    public ConstantPoolEntryUTF8 getUTF8Entry(int index) {
        return (ConstantPoolEntryUTF8)this.getEntry(index);
    }

    public ConstantPoolEntryNameAndType getNameAndTypeEntry(int index) {
        return (ConstantPoolEntryNameAndType)this.getEntry(index);
    }

    public ConstantPoolEntryMethodHandle getMethodHandleEntry(int index) {
        return (ConstantPoolEntryMethodHandle)this.getEntry(index);
    }

    public ConstantPoolEntryMethodRef getMethodRefEntry(int index) {
        return (ConstantPoolEntryMethodRef)this.getEntry(index);
    }

    public ConstantPoolEntryClass getClassEntry(int index) {
        return (ConstantPoolEntryClass)this.getEntry(index);
    }

    public ClassCache getClassCache() {
        return this.classCache;
    }

    public boolean equals(Object o) {
        this.getComparisonKey();
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ConstantPool that = (ConstantPool)o;
        if (this.comparisonKey.equals(that.comparisonKey)) return true;
        return false;
    }

    public String toString() {
        return this.getComparisonKey() + "[" + this.idx + "]";
    }

    public int hashCode() {
        return this.getComparisonKey().hashCode();
    }

    private String getComparisonKey() {
        if (this.comparisonKey != null) return this.comparisonKey;
        this.comparisonKey = this.classFile.getFilePath();
        return this.comparisonKey;
    }

}

