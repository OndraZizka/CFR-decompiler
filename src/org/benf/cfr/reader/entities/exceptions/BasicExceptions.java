/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.util.SetFactory;

public class BasicExceptions {
    public static Set<? extends JavaTypeInstance> instances = SetFactory.newSet(JavaRefTypeInstance.createTypeConstant("java.lang.AbstractMethodError", "AbstractMethodError", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.ArithmeticException", "ArithmeticException", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.ArrayIndexOutOfBoundsException", "ArrayIndexOutOfBoundsException", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.ArrayStoreException", "ArrayStoreException", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.ClassCastException", "ClassCastException", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.IllegalAccessError", "IllegalAccessError", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.IllegalMonitorStateException", "IllegalMonitorStateException", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.IncompatibleClassChangeError", "IncompatibleClassChangeError", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.InstantiationError", "InstantiationError", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.NegativeArraySizeException", "NegativeArraySizeException", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.NullPointerException", "NullPointerException", new JavaRefTypeInstance[0]), JavaRefTypeInstance.createTypeConstant("java.lang.UnsatisfiedLinkError", "UnsatisfiedLinkError", new JavaRefTypeInstance[0]));
}

