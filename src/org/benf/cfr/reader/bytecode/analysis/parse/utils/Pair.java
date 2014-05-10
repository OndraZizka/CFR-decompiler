/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

public class Pair<X, Y> {
    private final X x;
    private final Y y;

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public X getFirst() {
        return this.x;
    }

    public Y getSecond() {
        return this.y;
    }

    public static <A, B> Pair<A, B> make(A a, B b) {
        return new Pair<A, B>(a, b);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof Pair)) {
            return false;
        }
        Pair other = (Pair)o;
        if (this.x == null) {
            if (other.x != null) {
                return false;
            }
        } else if (!this.x.equals(other.x)) {
            return false;
        }
        if (this.y == null) {
            if (other.y == null) return true;
            return false;
        }
        if (this.y.equals(other.y)) return true;
        return false;
    }

    public int hashCode() {
        int hashCode = 1;
        if (this.x != null) {
            hashCode = this.x.hashCode();
        }
        if (this.y == null) return hashCode;
        hashCode = hashCode * 31 + this.y.hashCode();
        return hashCode;
    }
}

