/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.literal;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.QuotingUtils;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryDouble;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryFloat;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryInteger;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLong;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryString;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public class TypedLiteral
implements TypeUsageCollectable,
Dumpable {
    private final InferredJavaType inferredJavaType;
    private final LiteralType type;
    private final Object value;

    protected TypedLiteral(LiteralType type, InferredJavaType inferredJavaType, Object value) {
        this.type = type;
        this.value = value;
        this.inferredJavaType = inferredJavaType;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        if (this.type != LiteralType.Class) return;
        collector.collect((JavaTypeInstance)this.value);
    }

    private static String integerName(Object o) {
        if (!(o instanceof Integer)) {
            return o.toString();
        }
        int i = (Integer)o;
        switch (i) {
            case Integer.MAX_VALUE: {
                return "Integer.MAX_VALUE";
            }
            case Integer.MIN_VALUE: {
                return "Integer.MIN_VALUE";
            }
        }
        return o.toString();
    }

    public boolean getBoolValue() {
        Integer i;
        if (this.type != LiteralType.Integer) {
            throw new IllegalStateException("Expecting integral literal");
        }
        return (i = (Integer)this.value) != 0;
    }

    public Boolean getMaybeBoolValue() {
        Integer i;
        if (this.type != LiteralType.Integer) {
            return null;
        }
        return (i = (Integer)this.value) == 0 ? Boolean.FALSE : Boolean.TRUE;
    }

    private static String charName(Object o) {
        int i;
        if (!(o instanceof Integer)) {
            throw new ConfusedCFRException("Expecting char-as-int");
        }
        if ((i = ((Integer)o).intValue()) < 32 || i >= 254) {
            return "'\\u" + String.format("%04x", i) + "'";
        }
        char c = (char)i;
        switch (c) {
            case '\"': {
                return "'\\\"'";
            }
            case '\u000d': {
                return "'\\r'";
            }
            case '\u000a': {
                return "'\\n'";
            }
            case '\u0009': {
                return "'\\t'";
            }
            case '\u0008': {
                return "'\\b'";
            }
            case '\u000c': {
                return "'\\r'";
            }
            case '\\': {
                return "'\\\\'";
            }
            case '\'': {
                return "'\\''";
            }
        }
        return "'" + c + "'";
    }

    private static String boolName(Object o) {
        if (!(o instanceof Integer)) {
            throw new ConfusedCFRException("Expecting boolean-as-int");
        }
        int i = (Integer)o;
        switch (i) {
            case 0: {
                return "false";
            }
            case 1: {
                return "true";
            }
        }
        return "BADBOOL " + i;
    }

    private static String longName(Object o) {
        long l;
        if (!(o instanceof Long)) {
            return o.toString();
        }
        if ((l = ((Long)o).longValue()) == Long.MAX_VALUE) {
            return "Long.MAX_VALUE";
        }
        if (l == Long.MIN_VALUE) {
            return "Long.MIN_VALUE";
        }
        if (l == Integer.MAX_VALUE) {
            return "Integer.MAX_VALUE";
        }
        if (l == Integer.MIN_VALUE) {
            return "Integer.MIN_VALUE";
        }
        String longString = o.toString();
        if (l > 1048575) {
            String hexTest = Long.toHexString(l).toUpperCase();
            byte[] bytes = hexTest.getBytes();
            byte[] count = new byte[16];
            int diff = 0;
            int len = bytes.length;
            for (int i = 0; i < len; ++i) {
                byte b;
                if ((b = bytes[i]) >= 48 && b <= 57) {
                    byte[] arrby = count;
                    int n = bytes[i] - 48;
                    n[arrby] = n[arrby] + true;
                    if (n[arrby] + true != 1) continue;
                    ++diff;
                    continue;
                }
                if (b >= 65 && b <= 70) {
                    byte[] arrby = count;
                    int n = bytes[i] - 65 + 10;
                    n[arrby] = n[arrby] + true;
                    if (n[arrby] + true != 1) continue;
                    ++diff;
                    continue;
                }
                diff = 10;
                break;
            }
            if (diff <= 2) {
                longString = "0x" + hexTest;
            }
        }
        if (l <= Integer.MAX_VALUE && l >= Integer.MIN_VALUE) return longString;
        return longString + "L";
    }

    private static String methodHandleName(Object o) {
        ConstantPoolEntryMethodHandle methodHandle = (ConstantPoolEntryMethodHandle)o;
        ConstantPoolEntryMethodRef methodRef = methodHandle.getMethodRef();
        return methodRef.getMethodPrototype().toString();
    }

    private static String methodTypeName(Object o) {
        ConstantPoolEntryMethodType methodType = (ConstantPoolEntryMethodType)o;
        return methodType.getDescriptor().getValue();
    }

    @Override
    public Dumper dump(Dumper d) {
        switch (this.type) {
            case String: {
                return d.print((String)this.value);
            }
            case NullObject: {
                return d.print("null");
            }
            case Integer: {
                switch (1.$SwitchMap$org$benf$cfr$reader$bytecode$analysis$types$RawJavaType[this.inferredJavaType.getRawType().ordinal()]) {
                    case 1: {
                        return d.print(TypedLiteral.charName(this.value));
                    }
                    case 2: {
                        return d.print(TypedLiteral.boolName(this.value));
                    }
                }
                return d.print(TypedLiteral.integerName(this.value));
            }
            case Long: {
                return d.print(TypedLiteral.longName(this.value));
            }
            case MethodType: {
                return d.print(TypedLiteral.methodTypeName(this.value));
            }
            case MethodHandle: {
                return d.print(TypedLiteral.methodHandleName(this.value));
            }
            case Class: {
                return d.dump((JavaTypeInstance)this.value).print(".class");
            }
            case Float: {
                return d.print(this.value.toString()).print("f");
            }
        }
        return d.print(this.value.toString());
    }

    public String toString() {
        return ToStringDumper.toString(this);
    }

    public static TypedLiteral getLong(long v) {
        return new TypedLiteral(LiteralType.Long, new InferredJavaType(RawJavaType.LONG, InferredJavaType.Source.LITERAL), v);
    }

    public static TypedLiteral getInt(int v) {
        return new TypedLiteral(LiteralType.Integer, new InferredJavaType(RawJavaType.INT, InferredJavaType.Source.LITERAL), v);
    }

    public static TypedLiteral getBoolean(int v) {
        return new TypedLiteral(LiteralType.Integer, new InferredJavaType(RawJavaType.BOOLEAN, InferredJavaType.Source.LITERAL), v);
    }

    public static TypedLiteral getDouble(double v) {
        return new TypedLiteral(LiteralType.Double, new InferredJavaType(RawJavaType.DOUBLE, InferredJavaType.Source.LITERAL), v);
    }

    public static TypedLiteral getFloat(float v) {
        return new TypedLiteral(LiteralType.Float, new InferredJavaType(RawJavaType.FLOAT, InferredJavaType.Source.LITERAL), Float.valueOf(v));
    }

    public static TypedLiteral getClass(JavaTypeInstance v) {
        return new TypedLiteral(LiteralType.Class, new InferredJavaType(RawJavaType.REF, InferredJavaType.Source.LITERAL), v);
    }

    public static TypedLiteral getString(String v) {
        return new TypedLiteral(LiteralType.String, new InferredJavaType(TypeConstants.STRING, InferredJavaType.Source.LITERAL), v);
    }

    public static TypedLiteral getNull() {
        return new TypedLiteral(LiteralType.NullObject, new InferredJavaType(RawJavaType.NULL, InferredJavaType.Source.LITERAL), null);
    }

    public static TypedLiteral getMethodHandle(ConstantPoolEntryMethodHandle methodHandle, ConstantPool cp) {
        JavaRefTypeInstance typeInstance = cp.getClassCache().getRefClassFor("java.lang.invoke.MethodHandle");
        return new TypedLiteral(LiteralType.MethodHandle, new InferredJavaType(typeInstance, InferredJavaType.Source.LITERAL), methodHandle);
    }

    public static TypedLiteral getMethodType(ConstantPoolEntryMethodType methodType, ConstantPool cp) {
        JavaRefTypeInstance typeInstance = cp.getClassCache().getRefClassFor("java.lang.invoke.MethodType");
        return new TypedLiteral(LiteralType.MethodType, new InferredJavaType(typeInstance, InferredJavaType.Source.LITERAL), methodType);
    }

    public static TypedLiteral getConstantPoolEntryUTF8(ConstantPoolEntryUTF8 cpe) {
        return TypedLiteral.getString(QuotingUtils.enquoteString(cpe.getRawValue()));
    }

    public static TypedLiteral getConstantPoolEntry(ConstantPool cp, ConstantPoolEntry cpe) {
        if (cpe instanceof ConstantPoolEntryDouble) {
            return TypedLiteral.getDouble(((ConstantPoolEntryDouble)cpe).getValue());
        }
        if (cpe instanceof ConstantPoolEntryFloat) {
            return TypedLiteral.getFloat(((ConstantPoolEntryFloat)cpe).getValue());
        }
        if (cpe instanceof ConstantPoolEntryLong) {
            return TypedLiteral.getLong(((ConstantPoolEntryLong)cpe).getValue());
        }
        if (cpe instanceof ConstantPoolEntryInteger) {
            return TypedLiteral.getInt(((ConstantPoolEntryInteger)cpe).getValue());
        }
        if (cpe instanceof ConstantPoolEntryString) {
            return TypedLiteral.getString(((ConstantPoolEntryString)cpe).getValue());
        }
        if (cpe instanceof ConstantPoolEntryClass) {
            return TypedLiteral.getClass(((ConstantPoolEntryClass)cpe).getTypeInstance());
        }
        if (cpe instanceof ConstantPoolEntryMethodHandle) {
            return TypedLiteral.getMethodHandle((ConstantPoolEntryMethodHandle)cpe, cp);
        }
        if (!(cpe instanceof ConstantPoolEntryMethodType)) throw new ConfusedCFRException("Can't turn ConstantPoolEntry into Literal - got " + cpe);
        return TypedLiteral.getMethodType((ConstantPoolEntryMethodType)cpe, cp);
    }

    public LiteralType getType() {
        return this.type;
    }

    public Object getValue() {
        return this.value;
    }

    public InferredJavaType getInferredJavaType() {
        return this.inferredJavaType;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof TypedLiteral)) {
            return false;
        }
        TypedLiteral other = (TypedLiteral)o;
        return this.type == other.type && (this.value == null ? other.value == null : this.value.equals(other.value));
    }

    public static enum LiteralType {
        Integer,
        Long,
        Double,
        Float,
        String,
        NullObject,
        Class,
        MethodHandle,
        MethodType;
        

        private LiteralType() {
        }
    }

}

