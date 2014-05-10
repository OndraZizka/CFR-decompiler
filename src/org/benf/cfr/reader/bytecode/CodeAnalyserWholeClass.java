/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.AssertRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.EnumClassRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.IllegalGenericRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.J14ClassObjectRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.NonStaticLifter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.StaticLifter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.DeadMethodRemover;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.FieldVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.AccessFlagMethod;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class CodeAnalyserWholeClass {
    public static void wholeClassAnalysisPass1(ClassFile classFile, DCCommonState state) {
        Options options = state.getOptions();
        EnumClassRewriter.rewriteEnumClass(classFile, state);
        if (((Boolean)options.getOption(OptionsImpl.REMOVE_BAD_GENERICS)).booleanValue()) {
            CodeAnalyserWholeClass.removeIllegalGenerics(classFile, options);
        }
        if (((Boolean)options.getOption(OptionsImpl.SUGAR_ASSERTS)).booleanValue()) {
            CodeAnalyserWholeClass.resugarAsserts(classFile, options);
        }
        if (((Boolean)options.getOption(OptionsImpl.LIFT_CONSTRUCTOR_INIT)).booleanValue()) {
            CodeAnalyserWholeClass.liftStaticInitialisers(classFile, options);
            CodeAnalyserWholeClass.liftNonStaticInitialisers(classFile, options);
        }
        if (options.getOption(OptionsImpl.JAVA_4_CLASS_OBJECTS, classFile.getClassFileVersion()).booleanValue()) {
            CodeAnalyserWholeClass.resugarJava14classObjects(classFile, state);
        }
        if (!((Boolean)options.getOption(OptionsImpl.REMOVE_BOILERPLATE)).booleanValue()) return;
        CodeAnalyserWholeClass.removeBoilerplateMethods(classFile);
    }

    private static void replaceNestedSyntheticOuterRefs(ClassFile classFile) {
        for (Method method : classFile.getMethods()) {
            if (!method.hasCodeAttribute()) continue;
            Op04StructuredStatement code = method.getAnalysis();
            Op04StructuredStatement.replaceNestedSyntheticOuterRefs(code);
        }
    }

    private static void inlineAccessors(DCCommonState state, ClassFile classFile) {
        for (Method method : classFile.getMethods()) {
            if (!method.hasCodeAttribute()) continue;
            Op04StructuredStatement code = method.getAnalysis();
            Op04StructuredStatement.inlineSyntheticAccessors(state, method, code);
        }
    }

    private static void fixInnerClassConstructors(ClassFile classFile) {
        LValue outerThis;
        if (classFile.testAccessFlag(AccessFlag.ACC_STATIC)) {
            return;
        }
        Set removedLValues = SetFactory.newSet();
        boolean invalid = false;
        Iterator<Method> i$ = classFile.getConstructors().iterator();
        while (i$.hasNext()) {
            LValue removed;
            Method method;
            if ((removed = Op04StructuredStatement.fixInnerClassConstruction(method = i$.next(), (method = i$.next()).getAnalysis())) == null) {
                invalid = true;
                continue;
            }
            removedLValues.add((LValue)removed);
        }
        if (invalid || removedLValues.size() != 1) {
            return;
        }
        if (!(outerThis = (LValue)removedLValues.iterator().next() instanceof FieldVariable)) {
            return;
        }
        FieldVariable fieldVariable = (FieldVariable)outerThis;
        String originalName = fieldVariable.getFieldName();
        JavaTypeInstance fieldType = outerThis.getInferredJavaType().getJavaTypeInstance();
        JavaRefTypeInstance fieldRefType = (JavaRefTypeInstance)fieldType.getDeGenerifiedType();
        String name = fieldRefType.getRawShortName();
        ClassFileField classFileField = fieldVariable.getClassFileField();
        classFileField.overrideName(name + ".this");
        classFileField.markSyntheticOuterRef();
        try {
            ClassFileField localClassFileField = classFile.getFieldByName(originalName);
            localClassFileField.overrideName(name + ".this");
            localClassFileField.markSyntheticOuterRef();
        }
        catch (NoSuchFieldException e) {
            // empty catch block
        }
    }

    private static Method getStaticConstructor(ClassFile classFile) {
        Method staticInit;
        try {
            staticInit = classFile.getMethodByName("<clinit>").get(0);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        return staticInit;
    }

    private static void liftStaticInitialisers(ClassFile classFile, Options state) {
        Method staticInit = CodeAnalyserWholeClass.getStaticConstructor(classFile);
        if (staticInit == null) {
            return;
        }
        new StaticLifter(classFile).liftStatics(staticInit);
    }

    private static void liftNonStaticInitialisers(ClassFile classFile, Options state) {
        new NonStaticLifter(classFile).liftNonStatics();
    }

    private static void removeDeadMethods(ClassFile classFile) {
        Method staticInit = CodeAnalyserWholeClass.getStaticConstructor(classFile);
        if (staticInit != null) {
            DeadMethodRemover.removeDeadMethod(classFile, staticInit);
        }
        CodeAnalyserWholeClass.tryRemoveConstructor(classFile);
    }

    private static void removeBoilerplateMethods(ClassFile classFile) {
        String[] removeThese;
        String[] arr$ = removeThese = new String[]{"$deserializeLambda$"};
        int len$ = arr$.length;
        for (int i$ = 0; i$ < len$; ++i$) {
            List<Method> methods;
            String methName;
            if ((methods = classFile.getMethodsByNameOrNull(methName = arr$[i$])) == null) continue;
            for (Method method : methods) {
                method.hideSynthetic();
            }
        }
    }

    private static void tryRemoveConstructor(ClassFile classFile) {
        Method constructor;
        MethodPrototype methodPrototype;
        List<Method> constructors = classFile.getConstructors();
        if (constructors.size() != 1) {
            return;
        }
        if ((methodPrototype = (constructor = constructors.get(0)).getMethodPrototype()).getVisibleArgCount() > 0) {
            return;
        }
        if (constructor.testAccessFlag(AccessFlagMethod.ACC_FINAL)) {
            return;
        }
        if (!constructor.testAccessFlag(AccessFlagMethod.ACC_PUBLIC)) {
            return;
        }
        if (!MiscStatementTools.isDeadCode(constructor.getAnalysis())) {
            return;
        }
        classFile.removePointlessMethod(constructor);
    }

    private static void removeIllegalGenerics(ClassFile classFile, Options state) {
        ConstantPool cp = classFile.getConstantPool();
        IllegalGenericRewriter r = new IllegalGenericRewriter(cp);
        for (Method m : classFile.getMethods()) {
            List<StructuredStatement> statements;
            Op04StructuredStatement code;
            if (!m.hasCodeAttribute()) {
                return;
            }
            if (!(code = m.getAnalysis()).isFullyStructured()) continue;
            if ((statements = MiscStatementTools.linearise(code)) == null) {
                return;
            }
            for (StructuredStatement statement : statements) {
                statement.rewriteExpressions(r);
            }
            Op04StructuredStatement.removePrimitiveDeconversion(state, m, code);
        }
    }

    private static void resugarAsserts(ClassFile classFile, Options state) {
        Method staticInit = CodeAnalyserWholeClass.getStaticConstructor(classFile);
        if (staticInit == null) return;
        new AssertRewriter(classFile).sugarAsserts(staticInit);
    }

    private static void resugarJava14classObjects(ClassFile classFile, DCCommonState state) {
        new J14ClassObjectRewriter(classFile, state).rewrite();
    }

    public static void wholeClassAnalysisPass2(ClassFile classFile, DCCommonState state) {
        Options options = state.getOptions();
        if (((Boolean)options.getOption(OptionsImpl.REMOVE_INNER_CLASS_SYNTHETICS)).booleanValue()) {
            if (classFile.isInnerClass()) {
                CodeAnalyserWholeClass.fixInnerClassConstructors(classFile);
            }
            CodeAnalyserWholeClass.replaceNestedSyntheticOuterRefs(classFile);
            CodeAnalyserWholeClass.inlineAccessors(state, classFile);
        }
        if (!((Boolean)options.getOption(OptionsImpl.REMOVE_DEAD_METHODS)).booleanValue()) return;
        CodeAnalyserWholeClass.removeDeadMethods(classFile);
    }
}

