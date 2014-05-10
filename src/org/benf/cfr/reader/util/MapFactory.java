/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.benf.cfr.reader.util.LazyExceptionRetainingMap;
import org.benf.cfr.reader.util.LazyMap;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class MapFactory {
    public static <X, Y> Map<X, Y> newMap() {
        return new HashMap();
    }

    public static <X, Y> Map<X, Y> newIdentityMap() {
        return new IdentityHashMap();
    }

    public static <X, Y> TreeMap<X, Y> newTreeMap() {
        return new TreeMap();
    }

    public static <X, Y> Map<X, Y> newLinkedMap() {
        return new LinkedHashMap();
    }

    public static <X, Y> Map<X, Y> newLazyMap(UnaryFunction<X, Y> factory) {
        return new LazyMap<X, Y>(MapFactory.newMap(), factory);
    }

    public static <X, Y> Map<X, Y> newLinkedLazyMap(UnaryFunction<X, Y> factory) {
        return new LazyMap<X, Y>(MapFactory.newLinkedMap(), factory);
    }

    public static <X, Y> Map<X, Y> newLazyMap(Map<X, Y> base, UnaryFunction<X, Y> factory) {
        return new LazyMap<X, Y>(base, factory);
    }

    public static <X, Y> Map<X, Y> newExceptionRetainingLazyMap(UnaryFunction<X, Y> factory) {
        return new LazyExceptionRetainingMap<X, Y>(MapFactory.newMap(), factory);
    }

    public static <X, Y> Map<X, Y> newExceptionRetainingLazyMap(Map<X, Y> base, UnaryFunction<X, Y> factory) {
        return new LazyExceptionRetainingMap<X, Y>(base, factory);
    }
}

