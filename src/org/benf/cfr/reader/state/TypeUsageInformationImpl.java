/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageUtils;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;

public class TypeUsageInformationImpl
implements TypeUsageInformation {
    private final JavaRefTypeInstance analysisType;
    private final Set<JavaRefTypeInstance> usedRefTypes = SetFactory.newOrderedSet();
    private final Set<JavaRefTypeInstance> usedLocalInnerTypes = SetFactory.newOrderedSet();
    private final Map<JavaRefTypeInstance, String> displayName = MapFactory.newMap();
    private final Set<String> shortNames = SetFactory.newSet();

    public TypeUsageInformationImpl(JavaRefTypeInstance analysisType, Set<JavaRefTypeInstance> usedRefTypes) {
        this.analysisType = analysisType;
        this.initialiseFrom(usedRefTypes);
    }

    @Override
    public String generateInnerClassShortName(JavaRefTypeInstance clazz) {
        return TypeUsageUtils.generateInnerClassShortName(clazz, this.analysisType);
    }

    private void initialiseFrom(Set<JavaRefTypeInstance> usedRefTypes) {
        List<JavaRefTypeInstance> usedRefs = ListFactory.newList(usedRefTypes);
        Collections.sort(usedRefs, new Comparator<JavaRefTypeInstance>(){

            @Override
            public int compare(JavaRefTypeInstance a, JavaRefTypeInstance b) {
                return a.getRawName().compareTo(b.getRawName());
            }
        });
        this.usedRefTypes.addAll(usedRefs);
        Pair<List<JavaRefTypeInstance>, List<JavaRefTypeInstance>> types = Functional.partition(usedRefs, new Predicate<JavaRefTypeInstance>(){

            @Override
            public boolean test(JavaRefTypeInstance in) {
                return in.getInnerClassHereInfo().isTransitiveInnerClassOf(TypeUsageInformationImpl.this.analysisType);
            }
        });
        this.addDisplayNames((Collection)types.getFirst());
        this.usedLocalInnerTypes.addAll((Collection)types.getFirst());
        this.addDisplayNames((Collection)types.getSecond());
    }

    private void addDisplayNames(Collection<JavaRefTypeInstance> types) {
        for (JavaRefTypeInstance type : types) {
            this.addDisplayName(type);
        }
    }

    private String addDisplayName(JavaRefTypeInstance type) {
        String already = this.displayName.get(type);
        if (already != null) {
            return already;
        }
        String useName = null;
        if (type.getInnerClassHereInfo().isInnerClass()) {
            useName = this.generateInnerClassShortName(type);
            this.shortNames.add(useName);
        } else {
            String shortName;
            useName = this.shortNames.add(shortName = type.getRawShortName()) ? shortName : type.getRawName();
        }
        this.displayName.put(type, useName);
        return useName;
    }

    @Override
    public Set<JavaRefTypeInstance> getUsedClassTypes() {
        return this.usedRefTypes;
    }

    @Override
    public Set<JavaRefTypeInstance> getUsedInnerClassTypes() {
        return this.usedLocalInnerTypes;
    }

    @Override
    public String getName(JavaTypeInstance type) {
        String res = this.displayName.get(type);
        if (res != null) return res;
        return type.getRawName();
    }

}

