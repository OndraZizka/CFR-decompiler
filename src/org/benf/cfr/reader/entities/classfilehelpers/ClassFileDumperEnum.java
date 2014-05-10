/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.classfilehelpers;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractConstructorInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnoynmousInner;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
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
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ClassFileDumperEnum
extends AbstractClassFileDumper {
    private static final AccessFlag[] dumpableAccessFlagsEnum = new AccessFlag[]{AccessFlag.ACC_PUBLIC, AccessFlag.ACC_PRIVATE, AccessFlag.ACC_PROTECTED, AccessFlag.ACC_STRICT, AccessFlag.ACC_STATIC};
    private final List<Pair<StaticVariable, AbstractConstructorInvokation>> entries;

    public ClassFileDumperEnum(DCCommonState dcCommonState, List<Pair<StaticVariable, AbstractConstructorInvokation>> entries) {
        super(dcCommonState);
        this.entries = entries;
    }

    private static void dumpHeader(ClassFile c, Dumper d) {
        d.print(ClassFileDumperEnum.getAccessFlagsString(c.getAccessFlags(), (AccessFlag[])ClassFileDumperEnum.dumpableAccessFlagsEnum));
        d.print("enum ").dump(c.getThisClassConstpoolEntry().getTypeInstance()).print(" ");
        ClassSignature signature = c.getClassSignature();
        List<JavaTypeInstance> interfaces = signature.getInterfaces();
        if (interfaces.isEmpty()) return;
        d.print("implements ");
        int size = interfaces.size();
        for (int x = 0; x < size; ++x) {
            JavaTypeInstance iface = interfaces.get(x);
            d.dump(iface).print(x < size - 1 ? ",\n" : "\n");
        }
    }

    private static void dumpEntry(Dumper d, Pair<StaticVariable, AbstractConstructorInvokation> entry, boolean last) {
        StaticVariable staticVariable = entry.getFirst();
        AbstractConstructorInvokation constructorInvokation = entry.getSecond();
        d.print(staticVariable.getVarName());
        if (constructorInvokation instanceof ConstructorInvokationSimple) {
            List<Expression> args;
            if ((args = constructorInvokation.getArgs()).size() > 2) {
                d.print('(');
                int len = args.size();
                for (int x = 2; x < len; ++x) {
                    if (x > 2) {
                        d.print(", ");
                    }
                    d.dump(args.get(x));
                }
                d.print(')');
            }
        } else if (constructorInvokation instanceof ConstructorInvokationAnoynmousInner) {
            ((ConstructorInvokationAnoynmousInner)constructorInvokation).dumpForEnum(d);
        }
        if (last) {
            d.endCodeln();
        } else {
            d.print(",\n");
        }
    }

    @Override
    public Dumper dump(ClassFile classFile, boolean innerClass, Dumper d) {
        List<Method> methods;
        if (!innerClass) {
            this.dumpTopHeader(classFile, d);
            this.dumpImports(d, classFile);
        }
        this.dumpAnnotations(classFile, d);
        ClassFileDumperEnum.dumpHeader(classFile, d);
        d.print("{\n");
        d.indent(1);
        int len = this.entries.size();
        for (int x = 0; x < len; ++x) {
            ClassFileDumperEnum.dumpEntry(d, this.entries.get(x), x == len - 1);
        }
        d.print("\n");
        List<ClassFileField> fields = classFile.getFields();
        for (ClassFileField field : fields) {
            if (field.shouldNotDisplay()) continue;
            field.dump(d);
        }
        if (!(methods = classFile.getMethods()).isEmpty()) {
            for (Method method : methods) {
                if (method.isHiddenFromDisplay()) continue;
                d.newln();
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
        for (Pair<StaticVariable, AbstractConstructorInvokation> entry : this.entries) {
            collector.collectFrom(entry.getFirst());
            collector.collectFrom(entry.getSecond());
        }
    }
}

