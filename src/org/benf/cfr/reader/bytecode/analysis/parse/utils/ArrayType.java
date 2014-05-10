/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.util.ConfusedCFRException;

public enum ArrayType {
    T_BOOLEAN(4, "boolean", RawJavaType.BOOLEAN),
    T_CHAR(5, "char", RawJavaType.CHAR),
    T_FLOAT(6, "float", RawJavaType.FLOAT),
    T_DOUBLE(7, "double", RawJavaType.DOUBLE),
    T_BYTE(8, "byte", RawJavaType.BYTE),
    T_SHORT(9, "short", RawJavaType.SHORT),
    T_INT(10, "int", RawJavaType.INT),
    T_LONG(11, "long", RawJavaType.LONG);
    
    private final int spec;
    private final String name;
    private final JavaTypeInstance javaTypeInstance;

    private ArrayType(int spec, String name, JavaTypeInstance javaTypeInstance) {
        this.spec = spec;
        this.name = name;
        this.javaTypeInstance = javaTypeInstance;
    }

    public static ArrayType getArrayType(int id) {
        switch (id) {
            case 4: {
                return ArrayType.T_BOOLEAN;
            }
            case 5: {
                return ArrayType.T_CHAR;
            }
            case 6: {
                return ArrayType.T_FLOAT;
            }
            case 7: {
                return ArrayType.T_DOUBLE;
            }
            case 8: {
                return ArrayType.T_BYTE;
            }
            case 9: {
                return ArrayType.T_SHORT;
            }
            case 10: {
                return ArrayType.T_INT;
            }
            case 11: {
                return ArrayType.T_LONG;
            }
        }
        throw new ConfusedCFRException("No such primitive array type " + id);
    }

    public String toString() {
        return this.name;
    }

    public JavaTypeInstance getJavaTypeInstance() {
        return this.javaTypeInstance;
    }
}

