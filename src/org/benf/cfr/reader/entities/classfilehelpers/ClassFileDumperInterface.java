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

public class ClassFileDumperInterface
extends AbstractClassFileDumper {
    private static final AccessFlag[] dumpableAccessFlagsInterface = new AccessFlag[]{AccessFlag.ACC_PUBLIC, AccessFlag.ACC_PRIVATE, AccessFlag.ACC_PROTECTED, AccessFlag.ACC_STRICT, AccessFlag.ACC_STATIC, AccessFlag.ACC_FINAL};

    public ClassFileDumperInterface(DCCommonState dcCommonState) {
        super(dcCommonState);
    }

    private void dumpHeader(ClassFile c, Dumper d) {
        d.print(ClassFileDumperInterface.getAccessFlagsString(c.getAccessFlags(), (AccessFlag[])ClassFileDumperInterface.dumpableAccessFlagsInterface));
        ClassSignature signature = c.getClassSignature();
        d.print("interface ").dump(c.getThisClassConstpoolEntry().getTypeInstance());
        ClassFileDumperInterface.getFormalParametersText((ClassSignature)signature, (Dumper)d);
        d.print("\n");
        List<JavaTypeInstance> interfaces = signature.getInterfaces();
        if (!interfaces.isEmpty()) {
            d.print("extends ");
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
        if (!innerClass) {
            this.dumpTopHeader(classFile, d);
            this.dumpImports(d, classFile);
        }
        this.dumpAnnotations(classFile, d);
        this.dumpHeader(classFile, d);
        boolean first = true;
        d.print("{\n");
        d.indent(1);
        List<ClassFileField> fields = classFile.getFields();
        for (ClassFileField field : fields) {
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
                method.dump(d, false);
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

