/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

public enum WildcardType {
    NONE(""),
    SUPER("super"),
    EXTENDS("extends");
    
    private final String name;

    private WildcardType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}

