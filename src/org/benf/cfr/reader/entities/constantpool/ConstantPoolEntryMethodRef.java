/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamerDefault;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryMethodRef
extends AbstractConstantPoolEntry {
    private final long OFFSET_OF_CLASS_INDEX = 1;
    private final long OFFSET_OF_NAME_AND_TYPE_INDEX = 3;
    private final boolean interfaceMethod;
    private static final VariableNamer fakeNamer = new VariableNamerDefault();
    private MethodPrototype methodPrototype = null;
    private OverloadMethodSet overloadMethodSet = null;
    private final short classIndex;
    private final short nameAndTypeIndex;

    public ConstantPoolEntryMethodRef(ConstantPool cp, ByteData data, boolean interfaceMethod) {
        super(cp);
        this.classIndex = data.getS2At(1);
        this.nameAndTypeIndex = data.getS2At(3);
        this.interfaceMethod = interfaceMethod;
    }

    @Override
    public long getRawByteLength() {
        return 5;
    }

    @Override
    public void dump(Dumper d) {
        ConstantPool cp = this.getCp();
        d.print("Method " + cp.getNameAndTypeEntry(this.nameAndTypeIndex).getName().getValue() + ":" + cp.getNameAndTypeEntry(this.nameAndTypeIndex).getDescriptor().getValue());
    }

    @Override
    public ConstantPool getCp() {
        return super.getCp();
    }

    public String toString() {
        return "Method classIndex " + this.classIndex + " nameAndTypeIndex " + this.nameAndTypeIndex;
    }

    public ConstantPoolEntryClass getClassEntry() {
        return this.getCp().getClassEntry(this.classIndex);
    }

    public ConstantPoolEntryNameAndType getNameAndTypeEntry() {
        return this.getCp().getNameAndTypeEntry(this.nameAndTypeIndex);
    }

    public MethodPrototype getMethodPrototype() {
        if (this.methodPrototype != null) return this.methodPrototype;
        ConstantPool cp = this.getCp();
        JavaTypeInstance classType = cp.getClassEntry(this.classIndex).getTypeInstance();
        ConstantPoolEntryNameAndType nameAndType = cp.getNameAndTypeEntry(this.nameAndTypeIndex);
        ConstantPoolEntryUTF8 descriptor = nameAndType.getDescriptor();
        MethodPrototype basePrototype = ConstantPoolUtils.parseJavaMethodPrototype(null, classType, this.getName(), this.interfaceMethod, Method.MethodConstructor.NOT, descriptor, cp, false, ConstantPoolEntryMethodRef.fakeNamer);
        try {
            JavaTypeInstance loadType = classType.getArrayStrippedType().getDeGenerifiedType();
            ClassFile classFile = cp.getDCCommonState().getClassFile(loadType);
            MethodPrototype replacement = classFile.getMethodByPrototype(basePrototype).getMethodPrototype();
            this.overloadMethodSet = classFile.getOverloadMethodSet(replacement);
            basePrototype = replacement;
        }
        catch (NoSuchMethodException ignore) {
            boolean x = true;
        }
        catch (CannotLoadClassException ignore) {
            boolean x = true;
        }
        this.methodPrototype = basePrototype;
        return this.methodPrototype;
    }

    public OverloadMethodSet getOverloadMethodSet() {
        return this.overloadMethodSet;
    }

    public String getName() {
        return this.getCp().getNameAndTypeEntry(this.nameAndTypeIndex).getName().getValue();
    }

    public boolean isInitMethod() {
        String name = this.getName();
        return "<init>".equals(name);
    }
}

