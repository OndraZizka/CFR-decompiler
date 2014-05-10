/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.DefaultEquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericPlaceholderTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public class JavaGenericRefTypeInstance
implements JavaGenericBaseInstance,
ComparableUnderEC {
    private static final WildcardConstraint WILDCARD_CONSTRAINT = new WildcardConstraint();
    private final JavaRefTypeInstance typeInstance;
    private final List<JavaTypeInstance> genericTypes;
    private final boolean hasUnbound;

    public JavaGenericRefTypeInstance(JavaTypeInstance typeInstance, List<JavaTypeInstance> genericTypes) {
        if (!(typeInstance instanceof JavaRefTypeInstance)) {
            throw new IllegalStateException("Generic sitting on top of non reftype");
        }
        this.typeInstance = (JavaRefTypeInstance)typeInstance;
        this.genericTypes = genericTypes;
        boolean unbound = false;
        for (JavaTypeInstance type : genericTypes) {
            if (!(type instanceof JavaGenericBaseInstance) || !((JavaGenericBaseInstance)type).hasUnbound()) continue;
            unbound = true;
        }
        this.hasUnbound = unbound;
    }

    @Override
    public void collectInto(TypeUsageCollector typeUsageCollector) {
        typeUsageCollector.collectRefType(this.typeInstance);
        for (JavaTypeInstance genericType : this.genericTypes) {
            typeUsageCollector.collect(genericType);
        }
    }

    @Override
    public boolean hasUnbound() {
        return this.hasUnbound;
    }

    @Override
    public boolean hasForeignUnbound(ConstantPool cp) {
        if (!this.hasUnbound) {
            return false;
        }
        for (JavaTypeInstance type : this.genericTypes) {
            if (!(type instanceof JavaGenericBaseInstance) || !((JavaGenericBaseInstance)type).hasForeignUnbound(cp)) continue;
            return true;
        }
        return false;
    }

    @Override
    public JavaGenericRefTypeInstance getBoundInstance(GenericTypeBinder genericTypeBinder) {
        if (genericTypeBinder == null) {
            return this;
        }
        List res = ListFactory.newList();
        for (JavaTypeInstance genericType : this.genericTypes) {
            res.add((JavaTypeInstance)genericTypeBinder.getBindingFor(genericType));
        }
        return new JavaGenericRefTypeInstance(this.typeInstance, res);
    }

    @Override
    public boolean tryFindBinding(JavaTypeInstance other, GenericTypeBinder target) {
        boolean res = false;
        if (!(other instanceof JavaGenericRefTypeInstance)) return res;
        JavaGenericRefTypeInstance otherJavaGenericRef = (JavaGenericRefTypeInstance)other;
        if (this.genericTypes.size() != otherJavaGenericRef.genericTypes.size()) return res;
        for (int x = 0; x < this.genericTypes.size(); ++x) {
            JavaTypeInstance genericType;
            if (!(genericType = this.genericTypes.get(x) instanceof JavaGenericBaseInstance)) continue;
            JavaGenericBaseInstance genericBaseInstance = (JavaGenericBaseInstance)genericType;
            res|=genericBaseInstance.tryFindBinding(otherJavaGenericRef.genericTypes.get(x), target);
        }
        return res;
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }

    @Override
    public void dumpInto(Dumper d, TypeUsageInformation typeUsageInformation) {
        d.dump(this.typeInstance).print('<');
        boolean first = true;
        for (JavaTypeInstance type : this.genericTypes) {
            first = CommaHelp.comma(first, d);
            d.dump(type);
        }
        d.print('>');
    }

    public String toString() {
        return new ToStringDumper().dump(this).toString();
    }

    @Override
    public JavaTypeInstance getArrayStrippedType() {
        return this;
    }

    @Override
    public List<JavaTypeInstance> getGenericTypes() {
        return this.genericTypes;
    }

    @Override
    public JavaRefTypeInstance getDeGenerifiedType() {
        return this.typeInstance;
    }

    @Override
    public int getNumArrayDimensions() {
        return 0;
    }

    public int hashCode() {
        int hash = 31 + this.typeInstance.hashCode();
        return hash;
    }

    @Override
    public String getRawName() {
        return new ToStringDumper().dump(this).toString();
    }

    @Override
    public InnerClassInfo getInnerClassHereInfo() {
        return this.typeInstance.getInnerClassHereInfo();
    }

    public JavaTypeInstance getTypeInstance() {
        return this.typeInstance;
    }

    @Override
    public BindingSuperContainer getBindingSupers() {
        return this.typeInstance.getBindingSupers();
    }

    public boolean equals(Object o) {
        return this.equivalentUnder(o, DefaultEquivalenceConstraint.INSTANCE);
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof JavaGenericRefTypeInstance)) {
            return false;
        }
        JavaGenericRefTypeInstance other = (JavaGenericRefTypeInstance)o;
        if (!constraint.equivalent(this.typeInstance, other.typeInstance)) {
            return false;
        }
        if (constraint.equivalent(this.genericTypes, other.genericTypes)) return true;
        return false;
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
        return this;
    }

    @Override
    public RawJavaType getRawTypeOfSimpleType() {
        return RawJavaType.REF;
    }

    @Override
    public boolean implicitlyCastsTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        BindingSuperContainer bindingSuperContainer;
        JavaRefTypeInstance degenerifiedThis;
        JavaGenericRefTypeInstance boundBase;
        if (other == TypeConstants.OBJECT) {
            return true;
        }
        if (this.equivalentUnder(other, JavaGenericRefTypeInstance.WILDCARD_CONSTRAINT)) {
            return true;
        }
        if ((bindingSuperContainer = this.getBindingSupers()) == null) {
            return false;
        }
        JavaTypeInstance degenerifiedOther = other.getDeGenerifiedType();
        if ((degenerifiedThis = this.getDeGenerifiedType()).equals(other)) {
            return true;
        }
        if (!bindingSuperContainer.containsBase(degenerifiedOther)) {
            return false;
        }
        if (other.equals(boundBase = bindingSuperContainer.getBoundSuperForBase(degenerifiedOther))) {
            return true;
        }
        if (!degenerifiedOther.equals(other)) return false;
        return true;
    }

    @Override
    public boolean canCastTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        return true;
    }

    @Override
    public String suggestVarName() {
        return this.typeInstance.suggestVarName();
    }

    public static class WildcardConstraint
    extends DefaultEquivalenceConstraint {
        @Override
        public boolean equivalent(Object o1, Object o2) {
            if (!(o2 instanceof JavaGenericPlaceholderTypeInstance) || !((JavaGenericPlaceholderTypeInstance)o2).getRawName().equals("?")) return super.equivalent(o1, o2);
            return true;
        }
    }

}

