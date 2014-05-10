/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.benf.cfr.reader.entities.attributes.Attribute;

public enum AccessFlag {
    ACC_PUBLIC("public"),
    ACC_PRIVATE("private"),
    ACC_PROTECTED("protected"),
    ACC_STATIC("static"),
    ACC_FINAL("final"),
    ACC_SUPER("super"),
    ACC_VOLATILE("volatile"),
    ACC_TRANSIENT("transient"),
    ACC_INTERFACE("interface"),
    ACC_ABSTRACT("abstract"),
    ACC_STRICT("strictfp"),
    ACC_SYNTHETIC("/* synthetic */"),
    ACC_ANNOTATION("/* annotation */"),
    ACC_ENUM("/* enum */");
    
    public final String name;

    private AccessFlag(String name) {
        this.name = name;
    }

    public static Set<AccessFlag> build(int raw) {
        TreeSet<AccessFlag> res = new TreeSet<AccessFlag>();
        if (0 != (raw & 1)) {
            res.add(AccessFlag.ACC_PUBLIC);
        }
        if (0 != (raw & 2)) {
            res.add(AccessFlag.ACC_PRIVATE);
        }
        if (0 != (raw & 4)) {
            res.add(AccessFlag.ACC_PROTECTED);
        }
        if (0 != (raw & 8)) {
            res.add(AccessFlag.ACC_STATIC);
        }
        if (0 != (raw & 16)) {
            res.add(AccessFlag.ACC_FINAL);
        }
        if (0 != (raw & 32)) {
            res.add(AccessFlag.ACC_SUPER);
        }
        if (0 != (raw & 64)) {
            res.add(AccessFlag.ACC_VOLATILE);
        }
        if (0 != (raw & 128)) {
            res.add(AccessFlag.ACC_TRANSIENT);
        }
        if (0 != (raw & 512)) {
            res.add(AccessFlag.ACC_INTERFACE);
        }
        if (0 != (raw & 1024)) {
            res.add(AccessFlag.ACC_ABSTRACT);
        }
        if (0 != (raw & 4096)) {
            res.add(AccessFlag.ACC_SYNTHETIC);
        }
        if (0 != (raw & 8192)) {
            res.add(AccessFlag.ACC_ANNOTATION);
        }
        if (0 != (raw & 16384)) {
            res.add(AccessFlag.ACC_ENUM);
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

    public static void applyAttributes(Map<String, Attribute> attributeMap, Set<AccessFlag> accessFlagSet) {
        if (!attributeMap.containsKey("Synthetic")) return;
        accessFlagSet.add(AccessFlag.ACC_SYNTHETIC);
    }
}

