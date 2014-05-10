/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetFactory {
    public static <X> Set<X> newSet() {
        return new HashSet();
    }

    public static <X> Set<X> newSortedSet() {
        return new TreeSet();
    }

    public static <X extends Enum<X>> EnumSet<X> newSet(EnumSet<X> content) {
        return EnumSet.copyOf(content);
    }

    public static <X> Set<X> newSet(Collection<X> content) {
        return new HashSet<X>(content);
    }

    public static /* varargs */ <X> Set<X> newSet(X ... content) {
        HashSet res = new HashSet();
        Collections.addAll(res, content);
        return res;
    }

    public static <X> Set<X> newOrderedSet() {
        return new LinkedHashSet();
    }

    public static <X> Set<X> newOrderedSet(Collection<X> content) {
        return new LinkedHashSet<X>(content);
    }

    public static /* varargs */ <X> Set<X> newOrderedSet(X ... content) {
        LinkedHashSet res = new LinkedHashSet();
        Collections.addAll(res, content);
        return res;
    }
}

