/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;

public class Slot {
    private final JavaTypeInstance javaTypeInstance;
    private final int idx;

    public Slot(JavaTypeInstance javaTypeInstance, int idx) {
        this.javaTypeInstance = javaTypeInstance;
        this.idx = idx;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Slot slot = (Slot)o;
        if (this.idx == slot.idx) return true;
        return false;
    }

    public int getIdx() {
        return this.idx;
    }

    public JavaTypeInstance getJavaTypeInstance() {
        return this.javaTypeInstance;
    }

    public String toString() {
        return "S{" + this.idx + '}';
    }

    public int hashCode() {
        return this.idx;
    }
}

