/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class LocalClassAwareTypeUsageInformation
implements TypeUsageInformation {
    private final TypeUsageInformation delegate;
    private final Map<JavaTypeInstance, String> localTypeNames;
    private final Set<String> usedLocalTypeNames;

    public LocalClassAwareTypeUsageInformation(Map<JavaRefTypeInstance, String> localClassTypes, TypeUsageInformation delegate) {
        this.delegate = delegate;
        Map lastClassByName = MapFactory.newLazyMap(new UnaryFunction<String, Integer>(){

            @Override
            public Integer invoke(String arg) {
                return 0;
            }
        });
        this.localTypeNames = MapFactory.newMap();
        this.usedLocalTypeNames = SetFactory.newSet();
        for (Map.Entry<JavaRefTypeInstance, String> entry : localClassTypes.entrySet()) {
            String usedName;
            String suggestedName;
            JavaRefTypeInstance localType = entry.getKey();
            if ((suggestedName = entry.getValue()) != null) {
                usedName = suggestedName;
            } else {
                String name = delegate.generateInnerClassShortName(localType);
                int len = name.length();
                for (int idx = 0; idx < len; ++idx) {
                    char c;
                    if ((c = name.charAt(idx)) >= '0' && c <= '9') continue;
                    name = name.substring(idx);
                    break;
                }
                int x = (Integer)lastClassByName.get(name);
                lastClassByName.put((String)name, x + 1);
                usedName = name + (x == 0 ? "" : new StringBuilder().append("_").append(x).toString());
            }
            this.localTypeNames.put(localType, usedName);
            this.usedLocalTypeNames.add(usedName);
        }
    }

    @Override
    public Set<JavaRefTypeInstance> getUsedClassTypes() {
        return this.delegate.getUsedClassTypes();
    }

    @Override
    public Set<JavaRefTypeInstance> getUsedInnerClassTypes() {
        return this.delegate.getUsedInnerClassTypes();
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

