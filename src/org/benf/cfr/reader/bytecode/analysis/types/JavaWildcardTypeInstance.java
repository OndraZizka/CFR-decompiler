/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.WildcardType;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public class JavaWildcardTypeInstance
implements JavaGenericBaseInstance {
    private final WildcardType wildcardType;
    private final JavaTypeInstance underlyingType;

    public JavaWildcardTypeInstance(WildcardType wildcardType, JavaTypeInstance underlyingType) {
        this.wildcardType = wildcardType;
        this.underlyingType = underlyingType;
    }

    @Override
    public JavaTypeInstance getBoundInstance(GenericTypeBinder genericTypeBinder) {
        if (!(this.underlyingType instanceof JavaGenericBaseInstance)) return this.underlyingType;
        return ((JavaGenericBaseInstance)this.underlyingType).getBoundInstance(genericTypeBinder);
    }

    @Override
    public boolean tryFindBinding(JavaTypeInstance other, GenericTypeBinder target) {
        if (!(this.underlyingType instanceof JavaGenericBaseInstance)) return false;
        return ((JavaGenericBaseInstance)this.underlyingType).tryFindBinding(other, target);
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }

    @Override
    public boolean hasUnbound() {
        if (!(this.underlyingType instanceof JavaGenericBaseInstance)) return false;
        return ((JavaGenericBaseInstance)this.underlyingType).hasUnbound();
    }

    @Override
    public boolean hasForeignUnbound(ConstantPool cp) {
        if (!(this.underlyingType instanceof JavaGenericBaseInstance)) return false;
        return ((JavaGenericBaseInstance)this.underlyingType).hasForeignUnbound(cp);
    }

    @Override
    public List<JavaTypeInstance> getGenericTypes() {
        if (!(this.underlyingType instanceof JavaGenericBaseInstance)) return ListFactory.newList();
        return ((JavaGenericBaseInstance)this.underlyingType).getGenericTypes();
    }

    @Override
    public void dumpInto(Dumper d, TypeUsageInformation typeUsageInformation) {
        d.print("? ").print(this.wildcardType.toString()).print(' ');
        d.dump(this.underlyingType);
    }

    public String toString() {
        return new ToStringDumper().dump(this).toString();
    }

    @Override
    public String getRawName() {
        return this.toString();
    }

    @Override
    public void collectInto(TypeUsageCollector typeUsageCollector) {
        this.underlyingType.collectInto(typeUsageCollector);
    }

    @Override
    public InnerClassInfo getInnerClassHereInfo() {
        return this.underlyingType.getInnerClassHereInfo();
    }

    @Override
    public BindingSuperContainer getBindingSupers() {
        return this.underlyingType.getBindingSupers();
    }

    @Override
    public JavaTypeInstance getArrayStrippedType() {
        return this.underlyingType.getArrayStrippedType();
    }

    @Override
    public int getNumArrayDimensions() {
        return this.underlyingType.getNumArrayDimensions();
    }

    public int hashCode() {
        return this.wildcardType.hashCode() * 31 + this.underlyingType.hashCode();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JavaWildcardTypeInstance)) {
            return false;
        }
        JavaWildcardTypeInstance other = (JavaWildcardTypeInstance)o;
        return other.wildcardType == this.wildcardType && other.underlyingType.equals(this.underlyingType);
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
        return this.underlyingType.removeAnArrayIndirection();
    }

    @Override
    public JavaTypeInstance getDeGenerifiedType() {
        return this;
    }

    @Override
    public RawJavaType getRawTypeOfSimpleType() {
        return this.underlyingType.getRawTypeOfSimpleType();
    }

    @Override
    public boolean implicitlyCastsTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        return false;
    }

    @Override
    public boolean canCastTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        return true;
    }

    @Override
    public String suggestVarName() {
        return null;
    }
}

