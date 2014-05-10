/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public class JavaArrayTypeInstance
implements JavaTypeInstance {
    private final int dimensions;
    private final JavaTypeInstance underlyingType;
    private JavaTypeInstance cachedDegenerifiedType;

    public JavaArrayTypeInstance(int dimensions, JavaTypeInstance underlyingType) {
        this.dimensions = dimensions;
        this.underlyingType = underlyingType;
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }

    @Override
    public void dumpInto(Dumper d, TypeUsageInformation typeUsageInformation) {
        this.toCommonString(this.getNumArrayDimensions(), d);
    }

    public String toString() {
        return new ToStringDumper().dump(this).toString();
    }

    private void toCommonString(int numDims, Dumper d) {
        d.dump(this.underlyingType.getArrayStrippedType());
        for (int x = 0; x < numDims; ++x) {
            d.print("[]");
        }
    }

    public void toVarargString(Dumper d) {
        this.toCommonString(this.getNumArrayDimensions() - 1, d);
        d.print(" ...");
    }

    @Override
    public String getRawName() {
        return new ToStringDumper().dump(this).toString();
    }

    @Override
    public InnerClassInfo getInnerClassHereInfo() {
        return InnerClassInfo.NOT;
    }

    @Override
    public BindingSuperContainer getBindingSupers() {
        return null;
    }

    @Override
    public JavaTypeInstance getArrayStrippedType() {
        if (!(this.underlyingType instanceof JavaArrayTypeInstance)) return this.underlyingType;
        return this.underlyingType.getArrayStrippedType();
    }

    @Override
    public int getNumArrayDimensions() {
        return this.dimensions + this.underlyingType.getNumArrayDimensions();
    }

    public int hashCode() {
        return this.dimensions * 31 + this.underlyingType.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavaArrayTypeInstance)) {
            return false;
        }
        JavaArrayTypeInstance other = (JavaArrayTypeInstance)o;
        return other.dimensions == this.dimensions && other.underlyingType.equals(this.underlyingType);
    }

    @Override
    public boolean isComplexType() {
        return true;
    }

    @Override
    public boolean isUsableType() {
        return true;
    }

    @Override
    public JavaTypeInstance removeAnArrayIndirection() {
        if (this.dimensions != 1) return new JavaArrayTypeInstance(this.dimensions - 1, this.underlyingType);
        return this.underlyingType;
    }

    @Override
    public JavaTypeInstance getDeGenerifiedType() {
        if (this.cachedDegenerifiedType != null) return this.cachedDegenerifiedType;
        this.cachedDegenerifiedType = new JavaArrayTypeInstance(this.dimensions, this.underlyingType.getDeGenerifiedType());
        return this.cachedDegenerifiedType;
    }

    @Override
    public RawJavaType getRawTypeOfSimpleType() {
        return this.underlyingType.getRawTypeOfSimpleType();
    }

    @Override
    public void collectInto(TypeUsageCollector typeUsageCollector) {
        typeUsageCollector.collect(this.underlyingType);
    }

    @Override
    public boolean implicitlyCastsTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        JavaArrayTypeInstance arrayOther;
        if (other == TypeConstants.OBJECT) {
            return true;
        }
        if (!(other instanceof JavaArrayTypeInstance)) return false;
        if (this.getNumArrayDimensions() == (arrayOther = (JavaArrayTypeInstance)other).getNumArrayDimensions()) return this.getArrayStrippedType().implicitlyCastsTo(arrayOther.getArrayStrippedType(), gtb);
        return false;
    }

    @Override
    public boolean canCastTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        return true;
    }

    @Override
    public String suggestVarName() {
        return "arr" + this.underlyingType.suggestVarName();
    }
}

