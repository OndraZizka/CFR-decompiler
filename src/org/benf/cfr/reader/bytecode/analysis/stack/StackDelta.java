/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.stack;

import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;

public interface StackDelta {
    public boolean isNoOp();

    public StackTypes getConsumed();

    public StackTypes getProduced();

    public long getChange();
}

