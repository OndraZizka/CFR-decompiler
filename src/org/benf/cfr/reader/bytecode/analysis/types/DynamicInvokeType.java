/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

public enum DynamicInvokeType {
    UNKNOWN("?"),
    METAFACTORY_1("metaFactory"),
    METAFACTORY_2("metafactory"),
    ALTMETAFACTORY_1("altMetaFactory"),
    ALTMETAFACTORY_2("altMetafactory");
    
    private final String constName;

    private DynamicInvokeType(String constName) {
        this.constName = constName;
    }

    public String getConstName() {
        return this.constName;
    }

    public static DynamicInvokeType lookup(String name) {
        if (name.equals(DynamicInvokeType.METAFACTORY_1.constName)) {
            return DynamicInvokeType.METAFACTORY_1;
        }
        if (name.equals(DynamicInvokeType.METAFACTORY_2.constName)) {
            return DynamicInvokeType.METAFACTORY_2;
        }
        if (name.equals(DynamicInvokeType.ALTMETAFACTORY_1.constName)) {
            return DynamicInvokeType.ALTMETAFACTORY_1;
        }
        if (!name.equals(DynamicInvokeType.ALTMETAFACTORY_2.constName)) return DynamicInvokeType.UNKNOWN;
        return DynamicInvokeType.ALTMETAFACTORY_2;
    }
}

