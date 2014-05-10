/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

public class Ident {
    private final int stackpos;
    private final int idx;

    public Ident(int stackpos, int idx) {
        this.stackpos = stackpos;
        this.idx = idx;
    }

    public String toString() {
        if (this.idx != 0) return "" + this.stackpos + "_" + this.idx;
        return "" + this.stackpos;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Ident ident = (Ident)o;
        if (this.idx != ident.idx) {
            return false;
        }
        if (this.stackpos == ident.stackpos) return true;
        return false;
    }

    public int hashCode() {
        int result = this.stackpos;
        result = 31 * result + this.idx;
        return result;
    }
}

