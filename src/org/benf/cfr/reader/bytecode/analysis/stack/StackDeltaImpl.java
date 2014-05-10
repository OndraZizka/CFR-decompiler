/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.stack;

import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.util.ConfusedCFRException;

public class StackDeltaImpl
implements StackDelta {
    private final StackTypes consumed;
    private final StackTypes produced;

    public StackDeltaImpl(StackTypes consumed, StackTypes produced) {
        if (consumed == null || produced == null) {
            throw new ConfusedCFRException("Must not have null stackTypes");
        }
        this.consumed = consumed;
        this.produced = produced;
    }

    @Override
    public boolean isNoOp() {
        return this.consumed.isEmpty() && this.produced.isEmpty();
    }

    @Override
    public StackTypes getConsumed() {
        return this.consumed;
    }

    @Override
    public StackTypes getProduced() {
        return this.produced;
    }

    @Override
    public long getChange() {
        return this.produced.size() - this.consumed.size();
    }

    public String toString() {
        return "Consumes " + this.consumed + ", Produces " + this.produced;
    }
}

