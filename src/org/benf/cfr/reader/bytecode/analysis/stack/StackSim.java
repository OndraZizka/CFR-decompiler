/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.stack;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntryHolder;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;

public class StackSim {
    private final StackSim parent;
    private final StackEntryHolder stackEntryHolder;
    private final long depth;

    public StackSim() {
        this.depth = 0;
        this.parent = null;
        this.stackEntryHolder = null;
    }

    private StackSim(StackSim parent, StackType stackType) {
        this.parent = parent;
        this.depth = parent.depth + 1;
        this.stackEntryHolder = new StackEntryHolder(stackType);
    }

    public StackEntry getEntry(int depth) {
        StackSim thisSim = this;
        for (; depth > 0; --depth) {
            thisSim = thisSim.getParent();
        }
        if (thisSim.stackEntryHolder != null) return thisSim.stackEntryHolder.getStackEntry();
        throw new ConfusedCFRException("Underrun type stack");
    }

    public List<StackEntryHolder> getHolders(int offset, long num) {
        StackSim thisSim = this;
        List res = ListFactory.newList();
        while (num > 0) {
            if (offset > 0) {
                --offset;
            } else {
                res.add((StackEntryHolder)thisSim.stackEntryHolder);
                --num;
            }
            thisSim = thisSim.getParent();
        }
        return res;
    }

    public long getDepth() {
        return this.depth;
    }

    public StackSim getChange(StackDelta delta, List<StackEntryHolder> consumed, List<StackEntryHolder> produced) {
        if (delta.isNoOp()) {
            return this;
        }
        StackSim thisSim = this;
        StackTypes consumedStack = delta.getConsumed();
        for (StackType stackType : consumedStack) {
            consumed.add(thisSim.stackEntryHolder);
            thisSim = thisSim.getParent();
        }
        StackTypes producedStack = delta.getProduced();
        for (StackType stackType2 : producedStack) {
            thisSim = new StackSim(thisSim, stackType2);
            produced.add(thisSim.stackEntryHolder);
        }
        return thisSim;
    }

    private StackSim getParent() {
        if (this.parent != null) return this.parent;
        throw new ConfusedCFRException("Stack underflow");
    }
}

