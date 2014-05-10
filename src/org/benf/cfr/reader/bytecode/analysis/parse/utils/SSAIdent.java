/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.BitSet;

public class SSAIdent {
    private final BitSet val;

    public SSAIdent(int idx) {
        this.val = new BitSet();
        this.val.set(idx);
    }

    private SSAIdent(BitSet content) {
        this.val = content;
    }

    public SSAIdent mergeWith(SSAIdent other) {
        BitSet b1 = this.val;
        BitSet b2 = other.val;
        if (b1.equals(b2)) {
            return this;
        }
        b1 = (BitSet)b1.clone();
        b1.or(b2);
        return new SSAIdent(b1);
    }

    public boolean isSuperSet(SSAIdent other) {
        BitSet tmp = (BitSet)this.val.clone();
        tmp.or(other.val);
        if (tmp.cardinality() != this.val.cardinality()) {
            return false;
        }
        tmp.xor(other.val);
        return tmp.cardinality() > 0;
    }

    public int card() {
        return this.val.cardinality();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof SSAIdent)) {
            return false;
        }
        SSAIdent other = (SSAIdent)o;
        return this.val.equals(other.val);
    }

    public int hashCode() {
        return this.val.hashCode();
    }

    public String toString() {
        return this.val.toString();
    }

    public boolean isFirstIn(SSAIdent other) {
        int bit1 = this.val.nextSetBit(0);
        int bit2 = other.val.nextSetBit(0);
        return bit1 == bit2;
    }
}

