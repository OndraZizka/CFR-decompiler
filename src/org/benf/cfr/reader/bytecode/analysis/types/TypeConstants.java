/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;

public interface TypeConstants {
    public static final JavaRefTypeInstance OBJECT = JavaRefTypeInstance.createTypeConstant("java.lang.Object", "Object", new JavaRefTypeInstance[0]);
    public static final JavaRefTypeInstance ENUM = JavaRefTypeInstance.createTypeConstant("java.lang.Enum", "Enum", TypeConstants.OBJECT);
    public static final JavaRefTypeInstance ASSERTION_ERROR = JavaRefTypeInstance.createTypeConstant("java.lang.AssertionError", "AssertionError", TypeConstants.OBJECT);
    public static final JavaRefTypeInstance STRING = JavaRefTypeInstance.createTypeConstant("java.lang.String", "String", TypeConstants.OBJECT);
    public static final String boxingNameBoolean = "java.lang.Boolean";
    public static final String boxingNameByte = "java.lang.Byte";
    public static final String boxingNameShort = "java.lang.Short";
    public static final String boxingNameChar = "java.lang.Character";
    public static final String boxingNameInt = "java.lang.Integer";
    public static final String boxingNameLong = "java.lang.Long";
    public static final String boxingNameFloat = "java.lang.Float";
    public static final String boxingNameDouble = "java.lang.Double";
    public static final String boxingNameNumber = "java.lang.Number";
    public static final String throwableName = "java.lang.Throwable";
    public static final String stringName = "java.lang.String";
    public static final String charSequenceName = "java.lang.CharSequence";
    public static final String stringBuilderName = "java.lang.StringBuilder";
    public static final String stringBufferName = "java.lang.StringBuffer";
    public static final String className = "java.lang.Class";
    public static final String lambdaMetaFactoryName = "java.lang.invoke.LambdaMetafactory";
    public static final String runtimeExceptionPath = "java/lang/RuntimeException.class";
}

