/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.entities.attributes.Attribute;

public enum AccessFlagMethod {
    ACC_PUBLIC("public"),
    ACC_PRIVATE("private"),
    ACC_PROTECTED("protected"),
    ACC_STATIC("static"),
    ACC_FINAL("final"),
    ACC_SYNCHRONISED("synchronized"),
    ACC_BRIDGE("/* bridge */"),
    ACC_VARARGS("/* varargs */"),
    ACC_NATIVE("native"),
    ACC_ABSTRACT("abstract"),
    ACC_STRICT("strictfp"),
    ACC_SYNTHETIC("/* synthetic */");
    
    private final String name;

    private AccessFlagMethod(String name) {
        this.name = name;
    }

    public static EnumSet<AccessFlagMethod> build(int raw) {
        EnumSet res = EnumSet.noneOf(AccessFlagMethod.class);
        if (0 != (raw & 1)) {
            res.add((Object)AccessFlagMethod.ACC_PUBLIC);
        }
        if (0 != (raw & 2)) {
            res.add((Object)AccessFlagMethod.ACC_PRIVATE);
        }
        if (0 != (raw & 4)) {
            res.add((Object)AccessFlagMethod.ACC_PROTECTED);
        }
        if (0 != (raw & 8)) {
            res.add((Object)AccessFlagMethod.ACC_STATIC);
        }
        if (0 != (raw & 16)) {
            res.add((Object)AccessFlagMethod.ACC_FINAL);
        }
        if (0 != (raw & 32)) {
            res.add((Object)AccessFlagMethod.ACC_SYNCHRONISED);
        }
        if (0 != (raw & 64)) {
            res.add((Object)AccessFlagMethod.ACC_BRIDGE);
        }
        if (0 != (raw & 128)) {
            res.add((Object)AccessFlagMethod.ACC_VARARGS);
        }
        if (0 != (raw & 256)) {
            res.add((Object)AccessFlagMethod.ACC_NATIVE);
        }
        if (0 != (raw & 1024)) {
            res.add((Object)AccessFlagMethod.ACC_ABSTRACT);
        }
        if (0 != (raw & 2048)) {
            res.add((Object)AccessFlagMethod.ACC_STRICT);
        }
        if (0 != (raw & 4096)) {
            res.add((Object)AccessFlagMethod.ACC_SYNTHETIC);
        }
        if (res.isEmpty()) {
            return res;
        }
        EnumSet resaf = EnumSet.copyOf(res);
        return resaf;
    }

    public String toString() {
        return this.name;
    }

    public static void applyAttributes(Map<String, Attribute> attributeMap, Set<AccessFlagMethod> accessFlagSet) {
        if (!attributeMap.containsKey("Synthetic")) return;
        accessFlagSet.add(AccessFlagMethod.ACC_SYNTHETIC);
    }
}

