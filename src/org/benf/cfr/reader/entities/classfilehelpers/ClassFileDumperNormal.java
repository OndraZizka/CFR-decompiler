/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.classfilehelpers;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.classfilehelpers.AbstractClassFileDumper;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class ClassFileDumperNormal
extends AbstractClassFileDumper {
    private static final AccessFlag[] dumpableAccessFlagsClass = new AccessFlag[]{AccessFlag.ACC_PUBLIC, AccessFlag.ACC_PRIVATE, AccessFlag.ACC_PROTECTED, AccessFlag.ACC_STRICT, AccessFlag.ACC_STATIC, AccessFlag.ACC_FINAL, AccessFlag.ACC_ABSTRACT};

    public ClassFileDumperNormal(DCCommonState dcCommonState) {
        super(dcCommonState);
    }

    private void dumpHeader(ClassFile c, Dumper d) {
        List<JavaTypeInstance> interfaces;
        d.print(ClassFileDumperNormal.getAccessFlagsString(c.getAccessFlags(), (AccessFlag[])ClassFileDumperNormal.dumpableAccessFlagsClass));
        ClassSignature signature = c.getClassSignature();
        d.print("class ").dump(c.getThisClassConstpoolEntry().getTypeInstance());
        ClassFileDumperNormal.getFormalParametersText((ClassSignature)signature, (Dumper)d);
        d.print("\n");
        JavaTypeInstance superClass = signature.getSuperClass();
        if (!(superClass == null || superClass.getRawName().equals("java.lang.Object"))) {
            d.print("extends ").dump(superClass).print("\n");
        }
        if (!(interfaces = signature.getInterfaces()).isEmpty()) {
            d.print("implements ");
            int size = interfaces.size();
            for (int x = 0; x < size; ++x) {
                JavaTypeInstance iface = interfaces.get(x);
                d.dump(iface).print(x < size - 1 ? ",\n" : "\n");
            }
        }
        d.removePendingCarriageReturn().print(" ");
    }

    @Override
    public Dumper dump(ClassFile classFile, boolean innerClass, Dumper d) {
        List<Method> methods;
        if (!d.canEmitClass(classFile.getClassType())) {
            return d;
        }
        if (!innerClass) {
            this.dumpTopHeader(classFile, d);
            this.dumpImports(d, classFile);
        }
        this.dumpAnnotations(classFile, d);
        this.dumpHeader(classFile, d);
        d.print("{\n");
        d.indent(1);
        boolean first = true;
        List<ClassFileField> fields = classFile.getFields();
        for (ClassFileField field : fields) {
            if (field.shouldNotDisplay()) continue;
            field.dump(d);
            first = false;
        }
        if (!(methods = classFile.getMethods()).isEmpty()) {
            for (Method method : methods) {
                if (method.isHiddenFromDisplay()) continue;
                if (!first) {
                    d.newln();
                }
                first = false;
                method.dump(d, true);
            }
        }
        classFile.dumpNamedInnerClasses(d);
        d.indent(-1);
        d.print("}\n");
        return d;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }
}

