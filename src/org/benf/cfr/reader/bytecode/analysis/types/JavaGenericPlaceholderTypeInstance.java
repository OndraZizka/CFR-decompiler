/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class JavaGenericPlaceholderTypeInstance
implements JavaGenericBaseInstance {
    private final String className;
    private final ConstantPool cp;

    public JavaGenericPlaceholderTypeInstance(String className, ConstantPool cp) {
        this.className = className;
        this.cp = cp;
    }

    @Override
    public JavaTypeInstance getBoundInstance(GenericTypeBinder genericTypeBinder) {
        return genericTypeBinder.getBindingFor(this);
    }

    @Override
    public boolean hasUnbound() {
        return true;
    }

    @Override
    public List<JavaTypeInstance> getGenericTypes() {
        return ListFactory.newList(this);
    }

    @Override
    public boolean hasForeignUnbound(ConstantPool cp) {
        if (this.className.equals("?")) {
            return true;
        }
        return !cp.equals(this.cp);
    }

    @Override
    public boolean tryFindBinding(JavaTypeInstance other, GenericTypeBinder target) {
        target.suggestBindingFor(this.className, other);
        return true;
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }

    @Override
    public void dumpInto(Dumper d, TypeUsageInformation typeUsageInformation) {
        d.print(this.toString());
    }

    public String toString() {
        return this.className;
    }

    @Override
    public JavaTypeInstance getArrayStrippedType() {
        return this;
    }

    @Override
    public int getNumArrayDimensions() {
        return 0;
    }

    @Override
    public String getRawName() {
        return this.className;
    }

    @Override
    public InnerClassInfo getInnerClassHereInfo() {
        return InnerClassInfo.NOT;
    }

    @Override
    public BindingSuperContainer getBindingSupers() {
        return null;
    }

    public int hashCode() {
        return 31 + this.className.hashCode();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof JavaGenericPlaceholderTypeInstance)) {
            return false;
        }
        JavaGenericPlaceholderTypeInstance other = (JavaGenericPlaceholderTypeInstance)o;
        return other.className.equals(this.className);
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
        throw new UnsupportedOperationException();
    }

    @Override
    public JavaTypeInstance getDeGenerifiedType() {
        return TypeConstants.OBJECT;
    }

    @Override
    public RawJavaType getRawTypeOfSimpleType() {
        return RawJavaType.REF;
    }

    @Override
    public void collectInto(TypeUsageCollector typeUsageCollector) {
    }

    @Override
    public boolean implicitlyCastsTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        if (other == TypeConstants.OBJECT) {
            return true;
        }
        if (!other.equals(this)) return false;
        return true;
    }

    @Override
    public boolean canCastTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        return true;
    }

    @Override
    public String suggestVarName() {
        return this.className;
    }
}

