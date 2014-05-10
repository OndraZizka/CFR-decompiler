/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.stack;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.SetFactory;

public class StackEntry {
    private static long sid = 0;
    private final long id0 = StackEntry.sid++;
    private final Set<Long> ids = SetFactory.newSet();
    private int artificalSourceCount = 0;
    private final StackSSALabel lValue;
    private long usageCount = 0;
    private final StackType stackType;
    private final InferredJavaType inferredJavaType = new InferredJavaType();

    public StackEntry(StackType stackType) {
        this.ids.add(this.id0);
        this.lValue = new StackSSALabel(this.id0, this);
        this.stackType = stackType;
    }

    public long incrementUsage() {
        return ++this.usageCount;
    }

    public long decrementUsage() {
        return --this.usageCount;
    }

    public long forceUsageCount(long newCount) {
        this.usageCount = newCount;
        return this.usageCount;
    }

    public boolean mergeWith(StackEntry other) {
        if (other.stackType != this.stackType) {
            return false;
        }
        this.ids.addAll(other.ids);
        this.usageCount+=other.usageCount;
        return true;
    }

    public long getUsageCount() {
        return this.usageCount;
    }

    public int getSourceCount() {
        return this.ids.size() + this.artificalSourceCount;
    }

    public void incSourceCount() {
        ++this.artificalSourceCount;
    }

    public void decSourceCount() {
        --this.artificalSourceCount;
    }

    public List<Long> getSources() {
        return ListFactory.newList(this.ids);
    }

    public void removeSource(long x) {
        if (this.ids.remove(x)) return;
        throw new ConfusedCFRException("Attempt to remove non existent id");
    }

    public String toString() {
        return "" + this.id0;
    }

    public StackSSALabel getLValue() {
        return this.lValue;
    }

    public StackType getType() {
        return this.stackType;
    }

    public InferredJavaType getInferredJavaType() {
        return this.inferredJavaType;
    }

    public int hashCode() {
        return (int)this.id0;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof StackEntry)) {
            return false;
        }
        return this.id0 == ((StackEntry)o).id0;
    }
}

