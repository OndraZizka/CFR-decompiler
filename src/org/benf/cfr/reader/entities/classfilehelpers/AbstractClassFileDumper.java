/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.classfilehelpers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.FormalTypeParameter;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfoUtils;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeInvisibleAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeVisibleAnnotations;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumper;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public abstract class AbstractClassFileDumper
implements ClassFileDumper {
    private final DCCommonState dcCommonState;

    protected static String getAccessFlagsString(Set<AccessFlag> accessFlags, AccessFlag[] dumpableAccessFlags) {
        StringBuilder sb = new StringBuilder();
        for (AccessFlag accessFlag : dumpableAccessFlags) {
            if (!accessFlags.contains((Object)accessFlag)) continue;
            sb.append((Object)accessFlag).append(' ');
        }
        return sb.toString();
    }

    public AbstractClassFileDumper(DCCommonState dcCommonState) {
        this.dcCommonState = dcCommonState;
    }

    protected void dumpTopHeader(ClassFile classFile, Dumper d) {
        Options options;
        if (this.dcCommonState == null) {
            return;
        }
        String header = "Decompiled with CFR" + (((Boolean)(options = this.dcCommonState.getOptions()).getOption(OptionsImpl.SHOW_CFR_VERSION)).booleanValue() ? " 0_78" : "") + ".";
        d.print("/*").newln();
        d.print(" * ").print(header).newln();
        if (((Boolean)options.getOption(OptionsImpl.DECOMPILER_COMMENTS)).booleanValue()) {
            TypeUsageInformation typeUsageInformation = d.getTypeUsageInformation();
            List couldNotLoad = ListFactory.newList();
            for (JavaTypeInstance type2 : typeUsageInformation.getUsedClassTypes()) {
                if (!(type2 instanceof JavaRefTypeInstance)) continue;
                ClassFile loadedClass = null;
                try {
                    loadedClass = this.dcCommonState.getClassFile(type2);
                }
                catch (CannotLoadClassException e) {
                    // empty catch block
                }
                if (loadedClass != null) continue;
                couldNotLoad.add((JavaRefTypeInstance)type2);
            }
            if (!couldNotLoad.isEmpty()) {
                d.print(" * ").newln();
                d.print(" * Could not load the following classes:").newln();
                for (JavaTypeInstance type2 : couldNotLoad) {
                    d.print(" *  ").print(type2.getRawName()).newln();
                }
            }
        }
        d.print(" */").newln();
        String packageName = classFile.getThisClassConstpoolEntry().getPackageName();
        if (packageName.isEmpty()) return;
        d.print("package ").print(packageName).endCodeln().newln();
    }

    protected static void getFormalParametersText(ClassSignature signature, Dumper d) {
        List<FormalTypeParameter> formalTypeParameters = signature.getFormalTypeParameters();
        if (formalTypeParameters == null || formalTypeParameters.isEmpty()) {
            return;
        }
        d.print('<');
        boolean first = true;
        for (FormalTypeParameter formalTypeParameter : formalTypeParameters) {
            first = CommaHelp.comma(first, d);
            d.dump(formalTypeParameter);
        }
        d.print('>');
    }

    public void dumpImports(Dumper d, ClassFile classFile) {
        Options options;
        List<JavaTypeInstance> classTypes = classFile.getAllClassTypes();
        Set<JavaRefTypeInstance> types = d.getTypeUsageInformation().getUsedClassTypes();
        types.removeAll(classTypes);
        List<JavaRefTypeInstance> inners = Functional.filter(types, new Predicate<JavaRefTypeInstance>(){

            @Override
            public boolean test(JavaRefTypeInstance in) {
                return in.getInnerClassHereInfo().isInnerClass();
            }
        });
        types.removeAll(inners);
        for (JavaRefTypeInstance inner : inners) {
            types.add(InnerClassInfoUtils.getTransitiveOuterClass(inner));
        }
        List names = Functional.map(types, new UnaryFunction<JavaRefTypeInstance, String>(){

            @Override
            public String invoke(JavaRefTypeInstance arg) {
                if (!arg.getInnerClassHereInfo().isInnerClass()) return arg.getRawName();
                String name = arg.getRawName();
                return name.replace('$', '.');
            }
        });
        if (((Boolean)(options = this.dcCommonState.getOptions()).getOption(OptionsImpl.HIDE_LANG_IMPORTS)).booleanValue()) {
            names = Functional.filter(names, new Predicate<String>(){

                @Override
                public boolean test(String in) {
                    return !in.startsWith("java.lang");
                }
            });
        }
        if (names.isEmpty()) {
            return;
        }
        Collections.sort(names);
        for (String name : names) {
            d.print("import " + name + ";\n");
        }
        d.print("\n");
    }

    protected void dumpAnnotations(ClassFile classFile, Dumper d) {
        AttributeRuntimeVisibleAnnotations runtimeVisibleAnnotations = (AttributeRuntimeVisibleAnnotations)classFile.getAttributeByName("RuntimeVisibleAnnotations");
        AttributeRuntimeInvisibleAnnotations runtimeInvisibleAnnotations = (AttributeRuntimeInvisibleAnnotations)classFile.getAttributeByName("RuntimeInvisibleAnnotations");
        if (runtimeVisibleAnnotations != null) {
            runtimeVisibleAnnotations.dump(d);
        }
        if (runtimeInvisibleAnnotations == null) return;
        runtimeInvisibleAnnotations.dump(d);
    }

}

