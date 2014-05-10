/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.FormalTypeParameter;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericPlaceholderTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;

public class ClassSignature
implements TypeUsageCollectable {
    private final List<FormalTypeParameter> formalTypeParameters;
    private final JavaTypeInstance superClass;
    private final List<JavaTypeInstance> interfaces;

    public ClassSignature(List<FormalTypeParameter> formalTypeParameters, JavaTypeInstance superClass, List<JavaTypeInstance> interfaces) {
        this.formalTypeParameters = formalTypeParameters;
        this.superClass = superClass;
        this.interfaces = interfaces;
    }

    public List<FormalTypeParameter> getFormalTypeParameters() {
        return this.formalTypeParameters;
    }

    public JavaTypeInstance getSuperClass() {
        return this.superClass;
    }

    public List<JavaTypeInstance> getInterfaces() {
        return this.interfaces;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.superClass);
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.formalTypeParameters);
        collector.collect((Collection<? extends JavaTypeInstance>)this.interfaces);
    }

    public JavaTypeInstance getThisGeneralTypeClass(JavaTypeInstance nonGenericInstance, ConstantPool cp) {
        if (nonGenericInstance instanceof JavaGenericBaseInstance) {
            return nonGenericInstance;
        }
        if (this.formalTypeParameters == null || this.formalTypeParameters.isEmpty()) {
            return nonGenericInstance;
        }
        List typeParameterNames = ListFactory.newList();
        for (FormalTypeParameter formalTypeParameter : this.formalTypeParameters) {
            typeParameterNames.add((JavaGenericPlaceholderTypeInstance)new JavaGenericPlaceholderTypeInstance(formalTypeParameter.getName(), cp));
        }
        JavaGenericRefTypeInstance res = new JavaGenericRefTypeInstance(nonGenericInstance, typeParameterNames);
        return res;
    }
}

