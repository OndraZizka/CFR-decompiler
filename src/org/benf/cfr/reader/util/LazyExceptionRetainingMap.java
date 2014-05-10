/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.Map;
import org.benf.cfr.reader.util.LazyMap;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class LazyExceptionRetainingMap<X, Y>
extends LazyMap<X, Y> {
    private final Map<X, RuntimeException> exceptionMap = MapFactory.newMap();

    public LazyExceptionRetainingMap(Map<X, Y> inner, UnaryFunction<X, Y> factory) {
        super(inner, factory);
    }

    @Override
    public Y get(Object o) {
        RuntimeException exception = this.exceptionMap.get(o);
        if (exception != null) throw exception;
        try {
            return super.get(o);
        }
        catch (RuntimeException e) {
            exception = e;
            this.exceptionMap.put((X)o, e);
        }
        throw exception;
    }
}

