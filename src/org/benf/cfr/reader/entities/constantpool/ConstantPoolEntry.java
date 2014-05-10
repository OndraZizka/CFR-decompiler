/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public interface ConstantPoolEntry {
    public long getRawByteLength();

    public void dump(Dumper var1);

    public static enum Type {
        CPT_UTF8,
        CPT_Integer,
        CPT_Float,
        CPT_Long,
        CPT_Double,
        CPT_Class,
        CPT_String,
        CPT_FieldRef,
        CPT_MethodRef,
        CPT_InterfaceMethodRef,
        CPT_NameAndType,
        CPT_MethodHandle,
        CPT_MethodType,
        CPT_InvokeDynamic;
        
        private static final byte VAL_UTF8 = 1;
        private static final byte VAL_Integer = 3;
        private static final byte VAL_Float = 4;
        private static final byte VAL_Long = 5;
        private static final byte VAL_Double = 6;
        private static final byte VAL_Class = 7;
        private static final byte VAL_String = 8;
        private static final byte VAL_FieldRef = 9;
        private static final byte VAL_MethodRef = 10;
        private static final byte VAL_InterfaceMethodRef = 11;
        private static final byte VAL_NameAndType = 12;
        private static final byte VAL_MethodHandle = 15;
        private static final byte VAL_MethodType = 16;
        private static final byte VAL_InvokeDynamic = 18;

        private Type() {
        }

        public static Type get(byte val) {
            switch (val) {
                case 1: {
                    return Type.CPT_UTF8;
                }
                case 3: {
                    return Type.CPT_Integer;
                }
                case 4: {
                    return Type.CPT_Float;
                }
                case 5: {
                    return Type.CPT_Long;
                }
                case 6: {
                    return Type.CPT_Double;
                }
                case 7: {
                    return Type.CPT_Class;
                }
                case 8: {
                    return Type.CPT_String;
                }
                case 9: {
                    return Type.CPT_FieldRef;
                }
                case 10: {
                    return Type.CPT_MethodRef;
                }
                case 11: {
                    return Type.CPT_InterfaceMethodRef;
                }
                case 12: {
                    return Type.CPT_NameAndType;
                }
                case 15: {
                    return Type.CPT_MethodHandle;
                }
                case 16: {
                    return Type.CPT_MethodType;
                }
                case 18: {
                    return Type.CPT_InvokeDynamic;
                }
            }
            throw new ConfusedCFRException("Invalid constant pool entry type : " + val);
        }
    }

}

