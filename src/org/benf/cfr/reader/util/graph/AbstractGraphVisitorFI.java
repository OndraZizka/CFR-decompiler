/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.BinaryProcedure;
import org.benf.cfr.reader.util.graph.GraphVisitor;

public abstract class AbstractGraphVisitorFI<T>
implements GraphVisitor<T> {
    protected final LinkedList<T> toVisit = ListFactory.newLinkedList();
    private final Set<T> visited = SetFactory.newSet();
    private final BinaryProcedure<T, GraphVisitor<T>> callee;
    private boolean aborted = false;

    public AbstractGraphVisitorFI(T first, BinaryProcedure<T, GraphVisitor<T>> callee) {
        this.add(first);
        this.callee = callee;
    }

    protected abstract void internalAdd(T var1);

    private void add(T next) {
        if (this.visited.contains(next)) return;
        this.toVisit.add(next);
        this.visited.add(next);
    }

    @Override
    public void abort() {
        this.toVisit.clear();
        this.aborted = true;
    }

    @Override
    public boolean wasAborted() {
        return this.aborted;
    }

    @Override
    public Collection<T> getVisitedNodes() {
        return this.visited;
    }

    @Override
    public void enqueue(T next) {
        this.add(next);
    }

    @Override
    public void enqueue(Collection<? extends T> next) {
        for (? extends T t : next) {
            this.enqueue((T)t);
        }
    }

    @Override
    public void process() {
        do {
            T next = this.toVisit.removeFirst();
            this.callee.call(next, this);
        } while (!this.toVisit.isEmpty());
    }
}

