/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.graph;

import java.util.Collection;

public interface GraphVisitor<T> {
    public void enqueue(T var1);

    public void enqueue(Collection<? extends T> var1);

    public void process();

    public void abort();

    public boolean wasAborted();

    public Collection<T> getVisitedNodes();
}

