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

public class GraphVisitorDFS<T>
implements GraphVisitor<T> {
    private final Collection<? extends T> start;
    private final Set<T> visited = SetFactory.newSet();
    private final BinaryProcedure<T, GraphVisitor<T>> callee;
    private final LinkedList<T> pending = ListFactory.newLinkedList();
    private final LinkedList<T> enqueued = ListFactory.newLinkedList();
    private boolean aborted = false;

    public GraphVisitorDFS(T first, BinaryProcedure<T, GraphVisitor<T>> callee) {
        this.start = ListFactory.newList(new Object[]{first});
        this.callee = callee;
    }

    public GraphVisitorDFS(Collection<? extends T> first, BinaryProcedure<T, GraphVisitor<T>> callee) {
        this.start = ListFactory.newList(first);
        this.callee = callee;
    }

    @Override
    public void enqueue(T next) {
        this.enqueued.add(next);
    }

    @Override
    public void enqueue(Collection<? extends T> next) {
        for (? extends T t : next) {
            this.enqueue((T)t);
        }
    }

    @Override
    public void abort() {
        this.enqueued.clear();
        this.pending.clear();
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
    public void process() {
        this.pending.clear();
        this.enqueued.clear();
        this.pending.addAll(this.start);
        while (!this.pending.isEmpty()) {
            T current;
            if (this.visited.contains(current = this.pending.removeFirst())) continue;
            this.visited.add(current);
            this.callee.call(current, this);
            while (!this.enqueued.isEmpty()) {
                this.pending.addFirst(this.enqueued.removeLast());
            }
        }
    }
}

