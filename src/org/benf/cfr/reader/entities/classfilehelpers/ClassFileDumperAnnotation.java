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

public class ClassFileDumperAnnotation
extends AbstractClassFileDumper {
    private static final AccessFlag[] dumpableAccessFlagsInterface = new AccessFlag[]{AccessFlag.ACC_PUBLIC, AccessFlag.ACC_PRIVATE, AccessFlag.ACC_PROTECTED, AccessFlag.ACC_STATIC, AccessFlag.ACC_FINAL};

    public ClassFileDumperAnnotation(DCCommonState dcCommonState) {
        super(dcCommonState);
    }

    private void dumpHeader(ClassFile c, Dumper d) {
        d.print(ClassFileDumperAnnotation.getAccessFlagsString(c.getAccessFlags(), (AccessFlag[])ClassFileDumperAnnotation.dumpableAccessFlagsInterface));
        ClassSignature signature = c.getClassSignature();
        d.print("@interface ").dump(c.getThisClassConstpoolEntry().getTypeInstance());
        ClassFileDumperAnnotation.getFormalParametersText((ClassSignature)signature, (Dumper)d);
        d.print("\n");
        d.removePendingCarriageReturn().print(" ");
    }

    @Override
    public Dumper dump(ClassFile classFile, boolean innerClass, Dumper d) {
        List<Method> methods;
        if (!innerClass) {
            this.dumpTopHeader(classFile, d);
            this.dumpImports(d, classFile);
        }
        boolean first = true;
        this.dumpAnnotations(classFile, d);
        this.dumpHeader(classFile, d);
        d.print("{\n");
        d.indent(1);
        List<ClassFileField> fields = classFile.getFields();
        for (ClassFileField field : fields) {
            field.dump(d);
            first = false;
        }
        if (!(methods = classFile.getMethods()).isEmpty()) {
            for (Method meth : methods) {
                if (!first) {
                    d.newln();
                }
                first = false;
                meth.dump(d, false);
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

