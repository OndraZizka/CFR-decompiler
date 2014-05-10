/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.Collection;
import java.util.Set;
import org.benf.cfr.reader.util.SetFactory;

public class SetUtil {
    public static <X> boolean hasIntersection(Set<? extends X> b, Collection<? extends X> a) {
        for (? extends X x : a) {
            if (!b.contains((Object)x)) continue;
            return true;
        }
        return false;
    }

    public static <X> Set<X> intersectionOrNull(Set<? extends X> a, Set<? extends X> b) {
        if (b.size() < a.size()) {
            Set<? extends X> tmp = a;
            a = b;
            b = tmp;
        }
        Set res = null;
        for (? extends X x : a) {
            if (!b.contains((Object)x)) continue;
            if (res == null) {
                res = SetFactory.newSet();
            }
            res.add(x);
        }
        return res;
    }

    public static <X> Set<X> difference(Set<? extends X> a, Set<? extends X> b) {
        Set res = SetFactory.newSet();
        for (? extends X a1 : a) {
            if (b.contains((Object)a1)) continue;
            res.add(a1);
        }
        for (? extends X b1 : b) {
            if (a.contains((Object)b1)) continue;
            res.add(b1);
        }
        return res;
    }
}

