/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class MatchIterator<T> {
    private final List<T> data;
    private int idx;

    public MatchIterator(List<T> data) {
        this.data = data;
        this.idx = -1;
    }

    private MatchIterator(List<T> data, int idx) {
        this.data = data;
        this.idx = idx;
    }

    public T getCurrent() {
        if (this.idx < 0) {
            throw new IllegalStateException("Accessed before being advanced.");
        }
        if (this.idx < this.data.size()) return this.data.get(this.idx);
        throw new IllegalStateException("Out of range");
    }

    public MatchIterator<T> copy() {
        return new MatchIterator<T>(this.data, this.idx);
    }

    public void advanceTo(MatchIterator<StructuredStatement> other) {
        if (this.data != other.data) {
            throw new IllegalStateException();
        }
        this.idx = other.idx;
    }

    public boolean hasNext() {
        return this.idx < this.data.size() - 1;
    }

    public boolean isFinished() {
        return this.idx >= this.data.size();
    }

    public int getRemaining() {
        return this.data.size() - this.idx;
    }

    public boolean advance() {
        if (!this.isFinished()) {
            ++this.idx;
        }
        return !this.isFinished();
    }

    public void rewind1() {
        if (this.idx <= 0) return;
        --this.idx;
    }

    public String toString() {
        T t;
        if (this.isFinished()) {
            return "Finished";
        }
        return (t = this.data.get(this.idx)) == null ? "null" : t.toString();
    }
}

