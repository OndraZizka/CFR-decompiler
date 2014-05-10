/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.util.SetFactory;

public class ListFactory {
    public static <X> List<X> newList() {
        return new ArrayList();
    }

    public static /* varargs */ <X> List<X> newList(X ... original) {
        return Arrays.asList(original);
    }

    public static <X> List<X> newList(Collection<X> original) {
        return new ArrayList<X>(original);
    }

    public static <X> List<X> newList(int size) {
        return new ArrayList(size);
    }

    public static <X> LinkedList<X> newLinkedList() {
        return new LinkedList();
    }

    public static <X> List<X> uniqueList(Collection<X> list) {
        List<X> res = ListFactory.newList();
        Set tmp = SetFactory.newSet();
        for (X x : list) {
            if (!tmp.add(x)) continue;
            res.add(x);
        }
        return res;
    }
}

