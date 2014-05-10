/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericPlaceholderTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumper;

public enum RawJavaType implements JavaTypeInstance
{
    BOOLEAN("boolean", "bl", StackType.INT, true, "java.lang.Boolean", false),
    BYTE("byte", "by", StackType.INT, true, "java.lang.Byte", true),
    CHAR("char", "c", StackType.INT, true, "java.lang.Character", false),
    SHORT("short", "s", StackType.INT, true, "java.lang.Short", true),
    INT("int", "n", StackType.INT, true, "java.lang.Integer", true),
    LONG("long", "l", StackType.LONG, true, "java.lang.Long", true),
    FLOAT("float", "f", StackType.FLOAT, true, "java.lang.Float", true),
    DOUBLE("double", "d", StackType.DOUBLE, true, "java.lang.Double", true),
    VOID("void", null, StackType.VOID, false),
    REF("reference", null, StackType.REF, false),
    RETURNADDRESS("returnaddress", null, StackType.RETURNADDRESS, false),
    RETURNADDRESSORREF("returnaddress or ref", null, StackType.RETURNADDRESSORREF, false),
    NULL("null", null, StackType.REF, false);
    
    private final String name;
    private final String suggestedVarName;
    private final StackType stackType;
    private final boolean usableType;
    private final String boxedName;
    private final boolean isNumber;
    private static final Map<RawJavaType, Set<RawJavaType>> implicitCasts;
    private static final Map<String, RawJavaType> boxingTypes;
    public static Map<String, RawJavaType> rawJavaTypeMap;

    public static RawJavaType getUnboxedTypeFor(JavaTypeInstance type) {
        String rawName = type.getRawName();
        RawJavaType tgt = RawJavaType.boxingTypes.get(rawName);
        return tgt;
    }

    private RawJavaType(String name, String suggestedVarName, StackType stackType, boolean usableType, String boxedName, boolean isNumber) {
        this.name = name;
        this.stackType = stackType;
        this.usableType = usableType;
        this.boxedName = boxedName;
        this.suggestedVarName = suggestedVarName;
        this.isNumber = isNumber;
    }

    private RawJavaType(String name, String suggestedVarName, StackType stackType, boolean usableType) {
        this(name, suggestedVarName, stackType, usableType, null, false);
    }

    public String getName() {
        return this.name;
    }

    @Override
    public StackType getStackType() {
        return this.stackType;
    }

    @Override
    public boolean isComplexType() {
        return false;
    }

    public int compareTypePriorityTo(RawJavaType other) {
        if (this.stackType != StackType.INT) {
            throw new IllegalArgumentException();
        }
        if (other.stackType == StackType.INT) return this.ordinal() - other.ordinal();
        throw new IllegalArgumentException();
    }

    public int compareAllPriorityTo(RawJavaType other) {
        return this.ordinal() - other.ordinal();
    }

    @Override
    public boolean isUsableType() {
        return this.usableType;
    }

    @Override
    public RawJavaType getRawTypeOfSimpleType() {
        return this;
    }

    @Override
    public JavaTypeInstance removeAnArrayIndirection() {
        return RawJavaType.VOID;
    }

    @Override
    public JavaTypeInstance getArrayStrippedType() {
        return this;
    }

    @Override
    public JavaTypeInstance getDeGenerifiedType() {
        return this;
    }

    @Override
    public int getNumArrayDimensions() {
        return 0;
    }

    @Override
    public String getRawName() {
        return this.name;
    }

    @Override
    public InnerClassInfo getInnerClassHereInfo() {
        return InnerClassInfo.NOT;
    }

    @Override
    public BindingSuperContainer getBindingSupers() {
        return null;
    }

    private boolean implicitlyCastsTo(RawJavaType other) {
        Set<RawJavaType> tgt;
        if (other == this) {
            return true;
        }
        if ((tgt = RawJavaType.implicitCasts.get(this)) != null) return tgt.contains(other);
        return false;
    }

    @Override
    public boolean implicitlyCastsTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        RawJavaType tgt;
        if (other instanceof RawJavaType) {
            return this.implicitlyCastsTo((RawJavaType)other);
        }
        if (this == RawJavaType.NULL) {
            return true;
        }
        if (this == RawJavaType.REF) {
            return true;
        }
        if (other instanceof JavaGenericPlaceholderTypeInstance) {
            return true;
        }
        if (!(other instanceof JavaRefTypeInstance)) return false;
        if (other == TypeConstants.OBJECT) {
            return true;
        }
        if ((tgt = RawJavaType.getUnboxedTypeFor((JavaRefTypeInstance)other)) != null) return this.equals((Object)tgt);
        if (!other.getRawName().equals("java.lang.Number")) return false;
        return this.isNumber;
    }

    @Override
    public boolean canCastTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        RawJavaType tgt;
        if (this.boxedName == null || !(other instanceof JavaRefTypeInstance)) return true;
        if ((tgt = RawJavaType.getUnboxedTypeFor((JavaRefTypeInstance)other)) == null) {
            if (other == TypeConstants.OBJECT) {
                return true;
            }
            if (!other.getRawName().equals("java.lang.Number")) return false;
            return this.isNumber;
        }
        return this.implicitlyCastsTo(tgt) || tgt.implicitlyCastsTo(this);
    }

    @Override
    public String suggestVarName() {
        return this.suggestedVarName;
    }

    @Override
    public void dumpInto(Dumper d, TypeUsageInformation typeUsageInformation) {
        if (this == RawJavaType.NULL) {
            TypeConstants.OBJECT.dumpInto(d, typeUsageInformation);
            return;
        }
        d.print(this.toString());
    }

    @Override
    public void collectInto(TypeUsageCollector typeUsageCollector) {
    }

    public String toString() {
        return this.name;
    }

    public static RawJavaType getMaximalJavaTypeForStackType(StackType stackType) {
        switch (stackType) {
            case INT: {
                return RawJavaType.INT;
            }
            case FLOAT: {
                return RawJavaType.FLOAT;
            }
            case REF: {
                return RawJavaType.REF;
            }
            case RETURNADDRESS: {
                return RawJavaType.RETURNADDRESS;
            }
            case RETURNADDRESSORREF: {
                return RawJavaType.RETURNADDRESSORREF;
            }
            case LONG: {
                return RawJavaType.LONG;
            }
            case DOUBLE: {
                return RawJavaType.DOUBLE;
            }
        }
        throw new ConfusedCFRException("Unexpected stacktype.");
    }

    public static RawJavaType getByName(String name) {
        RawJavaType res;
        if (RawJavaType.rawJavaTypeMap == null) {
            RawJavaType.rawJavaTypeMap = MapFactory.newMap();
            for (RawJavaType typ : RawJavaType.values()) {
                RawJavaType.rawJavaTypeMap.put(typ.getName(), typ);
            }
        }
        if ((res = RawJavaType.rawJavaTypeMap.get(name)) != null) return res;
        throw new ConfusedCFRException("No RawJavaType '" + name + "'");
    }

    static {
        RawJavaType.implicitCasts = MapFactory.newMap();
        RawJavaType.boxingTypes = MapFactory.newMap();
        RawJavaType.implicitCasts.put(RawJavaType.FLOAT, SetFactory.newSet(RawJavaType.DOUBLE));
        RawJavaType.implicitCasts.put(RawJavaType.LONG, SetFactory.newSet(RawJavaType.FLOAT, RawJavaType.DOUBLE));
        RawJavaType.implicitCasts.put(RawJavaType.INT, SetFactory.newSet(RawJavaType.LONG, RawJavaType.FLOAT, RawJavaType.DOUBLE));
        RawJavaType.implicitCasts.put(RawJavaType.CHAR, SetFactory.newSet(RawJavaType.INT, RawJavaType.LONG, RawJavaType.FLOAT, RawJavaType.DOUBLE));
        RawJavaType.implicitCasts.put(RawJavaType.SHORT, SetFactory.newSet(RawJavaType.INT, RawJavaType.LONG, RawJavaType.FLOAT, RawJavaType.DOUBLE));
        RawJavaType.implicitCasts.put(RawJavaType.BYTE, SetFactory.newSet(RawJavaType.SHORT, RawJavaType.INT, RawJavaType.LONG, RawJavaType.FLOAT, RawJavaType.DOUBLE));
        for (RawJavaType type : RawJavaType.values()) {
            if (type.boxedName == null) continue;
            RawJavaType.boxingTypes.put(type.boxedName, type);
        }
    }

}

