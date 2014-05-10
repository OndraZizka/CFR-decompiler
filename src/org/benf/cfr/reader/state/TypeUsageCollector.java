/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageInformationImpl;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;

public class TypeUsageCollector {
    private final JavaRefTypeInstance analysisType;
    private final Set<JavaRefTypeInstance> typeInstanceSet = SetFactory.newSet();
    private final Set<JavaTypeInstance> seen = SetFactory.newSet();

    public TypeUsageCollector(ClassFile analysisClass) {
        this.analysisType = (JavaRefTypeInstance)analysisClass.getClassType().getDeGenerifiedType();
    }

    public void collectRefType(JavaRefTypeInstance type) {
        this.typeInstanceSet.add(type);
    }

    public void collect(JavaTypeInstance type) {
        if (type == null) {
            return;
        }
        if (!this.seen.add(type)) return;
        type.collectInto(this);
        InnerClassInfo innerClassInfo = type.getInnerClassHereInfo();
        if (!innerClassInfo.isInnerClass()) return;
        this.collect(innerClassInfo.getOuterClass());
    }

    public void collect(Collection<? extends JavaTypeInstance> types) {
        if (types == null) {
            return;
        }
        for (JavaTypeInstance type : types) {
            this.collect(type);
        }
    }

    public void collectFrom(TypeUsageCollectable collectable) {
        if (collectable == null) return;
        collectable.collectTypeUsages(this);
    }

    public void collectFrom(Collection<? extends TypeUsageCollectable> collectables) {
        if (collectables == null) return;
        Iterator<? extends TypeUsageCollectable> i$ = collectables.iterator();
        while (i$.hasNext()) {
            TypeUsageCollectable collectable;
            if ((collectable = (TypeUsageCollectable)i$.next()) == null) continue;
            collectable.collectTypeUsages(this);
        }
    }

    public TypeUsageInformation getTypeUsageInformation() {
        return new TypeUsageInformationImpl(this.analysisType, this.typeInstanceSet);
    }
}

