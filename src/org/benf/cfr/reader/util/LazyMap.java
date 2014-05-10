/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class LazyMap<X, Y>
implements Map<X, Y> {
    private final Map<X, Y> inner;
    private final UnaryFunction<X, Y> factory;

    public LazyMap(Map<X, Y> inner, UnaryFunction<X, Y> factory) {
        this.inner = inner;
        this.factory = factory;
    }

    @Override
    public int size() {
        return this.inner.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object o) {
        return this.inner.containsKey(o);
    }

    @Override
    public boolean containsValue(Object o) {
        return this.inner.containsValue(o);
    }

    @Override
    public Y get(Object o) {
        Y res = this.inner.get(o);
        if (res != null) return res;
        res = this.factory.invoke(o);
        this.inner.put((X)o, res);
        return res;
    }

    @Override
    public Y put(X x, Y y) {
        return this.inner.put(x, y);
    }

    @Override
    public Y remove(Object o) {
        return this.inner.remove(o);
    }

    @Override
    public void putAll(Map<? extends X, ? extends Y> map) {
        this.inner.putAll(map);
    }

    @Override
    public void clear() {
        this.inner.clear();
    }

    @Override
    public Set<X> keySet() {
        return this.inner.keySet();
    }

    @Override
    public Collection<Y> values() {
        return this.inner.values();
    }

    @Override
    public Set<Map.Entry<X, Y>> entrySet() {
        return this.inner.entrySet();
    }
}

