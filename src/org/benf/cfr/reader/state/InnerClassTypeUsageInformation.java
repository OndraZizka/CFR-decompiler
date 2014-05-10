/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageUtils;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;

public class InnerClassTypeUsageInformation
implements TypeUsageInformation {
    private final TypeUsageInformation delegate;
    private final JavaRefTypeInstance analysisInnerClass;
    private final Map<JavaRefTypeInstance, String> localTypeNames = MapFactory.newMap();
    private final Set<String> usedLocalTypeNames = SetFactory.newSet();
    private final Set<JavaRefTypeInstance> usedInnerClassTypes = SetFactory.newSet();

    public InnerClassTypeUsageInformation(TypeUsageInformation delegate, JavaRefTypeInstance analysisInnerClass) {
        this.delegate = delegate;
        this.analysisInnerClass = analysisInnerClass;
        this.initializeFrom();
    }

    private void initializeFrom() {
        Set<JavaRefTypeInstance> outerInners = this.delegate.getUsedInnerClassTypes();
        for (JavaRefTypeInstance outerInner : outerInners) {
            String name;
            if (!outerInner.getInnerClassHereInfo().isTransitiveInnerClassOf(this.analysisInnerClass)) continue;
            this.usedInnerClassTypes.add(outerInner);
            if (this.usedLocalTypeNames.contains(name = TypeUsageUtils.generateInnerClassShortName(outerInner, this.analysisInnerClass))) continue;
            this.localTypeNames.put(outerInner, name);
            this.usedLocalTypeNames.add(name);
        }
    }

    @Override
    public Set<JavaRefTypeInstance> getUsedClassTypes() {
        return this.delegate.getUsedClassTypes();
    }

    @Override
    public Set<JavaRefTypeInstance> getUsedInnerClassTypes() {
        return this.usedInnerClassTypes;
    }

    @Override
    public String getName(JavaTypeInstance type) {
        String res;
        String local = this.localTypeNames.get(type);
        if (local != null) {
            return local;
        }
        if (!this.usedLocalTypeNames.contains(res = this.delegate.getName(type))) return res;
        return type.getRawName();
    }

    @Override
    public String generateInnerClassShortName(JavaRefTypeInstance clazz) {
        return this.delegate.generateInnerClassShortName(clazz);
    }
}

