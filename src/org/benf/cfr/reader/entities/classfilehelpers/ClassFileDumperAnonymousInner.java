/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.classfilehelpers;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.classfilehelpers.AbstractClassFileDumper;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ClassFileDumperAnonymousInner
extends AbstractClassFileDumper {
    public ClassFileDumperAnonymousInner() {
        super(null);
    }

    @Override
    public Dumper dump(ClassFile classFile, boolean innerClass, Dumper d) {
        return this.dumpWithArgs(classFile, null, ListFactory.newList(), false, d);
    }

    public Dumper dumpWithArgs(ClassFile classFile, MethodPrototype usedMethod, List<Expression> args, boolean isEnum, Dumper d) {
        List<Method> methods;
        if (classFile == null) {
            d.print("/* Unavailable Anonymous Inner Class!! */");
            return d;
        }
        if (!d.canEmitClass(classFile.getClassType())) {
            return d;
        }
        if (!isEnum) {
            ClassSignature signature;
            if ((signature = classFile.getClassSignature()).getInterfaces().isEmpty()) {
                JavaTypeInstance superclass = signature.getSuperClass();
                d.dump(superclass);
            } else {
                JavaTypeInstance interfaceType = signature.getInterfaces().get(0);
                d.dump(interfaceType);
            }
        }
        if (!(isEnum && args.isEmpty())) {
            d.print("(");
            boolean first = true;
            int len = args.size();
            for (int i = 0; i < len; ++i) {
                if (usedMethod != null && usedMethod.isHiddenArg(i)) continue;
                Expression arg = args.get(i);
                first = CommaHelp.comma(first, d);
                d.dump(arg);
            }
            d.print(")");
        }
        d.print("{\n");
        d.indent(1);
        List<ClassFileField> fields = classFile.getFields();
        for (ClassFileField field : fields) {
            if (field.shouldNotDisplay()) continue;
            field.dump(d);
        }
        if (!(methods = classFile.getMethods()).isEmpty()) {
            for (Method method : methods) {
                if (method.isHiddenFromDisplay()) continue;
                if (method.isConstructor()) continue;
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
    }
}

