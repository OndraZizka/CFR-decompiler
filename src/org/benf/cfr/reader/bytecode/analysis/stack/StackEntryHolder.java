/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.stack;

import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;

public class StackEntryHolder {
    private StackEntry stackEntry;

    public StackEntryHolder(StackType stackType) {
        this.stackEntry = new StackEntry(stackType);
    }

    public void mergeWith(StackEntryHolder other) {
        if (!this.stackEntry.mergeWith(other.stackEntry)) return;
        other.stackEntry = this.stackEntry;
    }

    public String toString() {
        return this.stackEntry.toString();
    }

    public StackEntry getStackEntry() {
        return this.stackEntry;
    }
}

