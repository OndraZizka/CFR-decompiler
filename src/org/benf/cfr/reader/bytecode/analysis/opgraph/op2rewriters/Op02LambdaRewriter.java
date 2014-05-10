/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op2rewriters;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op02WithProcessedDataAndRefs;
import org.benf.cfr.reader.bytecode.analysis.types.DynamicInvokeType;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.attributes.AttributeBootstrapMethods;
import org.benf.cfr.reader.entities.bootstrap.BootstrapMethodInfo;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryInvokeDynamic;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;

public class Op02LambdaRewriter {
    private static Op02WithProcessedDataAndRefs getSinglePrev(Op02WithProcessedDataAndRefs item) {
        Op02WithProcessedDataAndRefs prev;
        if (item.getSources().size() != 1) {
            return null;
        }
        if ((prev = item.getSources().get(0)).getTargets().size() == 1) return prev;
        return null;
    }

    private static void tryRemove(ClassFile classFile, Op02WithProcessedDataAndRefs item) {
        Op02WithProcessedDataAndRefs getClass;
        Op02WithProcessedDataAndRefs dup;
        Op02WithProcessedDataAndRefs pop = Op02LambdaRewriter.getSinglePrev(item);
        if (pop == null) {
            return;
        }
        if (!Op02LambdaRewriter.isLambda(classFile, item)) {
            return;
        }
        if (pop.getInstr() != JVMInstr.POP) {
            return;
        }
        if ((getClass = Op02LambdaRewriter.getSinglePrev(pop)) == null) {
            return;
        }
        if (!Op02LambdaRewriter.isGetClass(getClass)) {
            return;
        }
        if ((dup = Op02LambdaRewriter.getSinglePrev(getClass)) == null) {
            return;
        }
        if (dup.getInstr() != JVMInstr.DUP) {
            return;
        }
        dup.nop();
        getClass.nop();
        pop.nop();
    }

    private static boolean isGetClass(Op02WithProcessedDataAndRefs item) {
        ConstantPoolEntry[] cpEntries = item.getCpEntries();
        ConstantPoolEntryMethodRef function = (ConstantPoolEntryMethodRef)cpEntries[0];
        MethodPrototype methodPrototype = function.getMethodPrototype();
        if (!methodPrototype.getName().equals("getClass")) {
            return false;
        }
        if (methodPrototype.getArgs().size() != 0) {
            return false;
        }
        if (methodPrototype.getReturnType().getDeGenerifiedType().getRawName().equals("java.lang.Class")) return true;
        return false;
    }

    private static boolean isLambda(ClassFile classFile, Op02WithProcessedDataAndRefs item) {
        ConstantPoolEntry[] cpEntries = item.getCpEntries();
        ConstantPoolEntryInvokeDynamic invokeDynamic = (ConstantPoolEntryInvokeDynamic)cpEntries[0];
        short idx = invokeDynamic.getBootstrapMethodAttrIndex();
        BootstrapMethodInfo bootstrapMethodInfo = classFile.getBootstrapMethods().getBootStrapMethodInfo(idx);
        ConstantPoolEntryMethodRef methodRef = bootstrapMethodInfo.getConstantPoolEntryMethodRef();
        String methodName = methodRef.getName();
        DynamicInvokeType dynamicInvokeType = DynamicInvokeType.lookup(methodName);
        if (dynamicInvokeType != DynamicInvokeType.UNKNOWN) return true;
        return false;
    }

    public static void removeInvokeGetClass(ClassFile classFile, List<Op02WithProcessedDataAndRefs> op02list) {
        for (Op02WithProcessedDataAndRefs item : op02list) {
            if (item.getInstr() != JVMInstr.INVOKEDYNAMIC) continue;
            Op02LambdaRewriter.tryRemove(classFile, item);
        }
    }
}

