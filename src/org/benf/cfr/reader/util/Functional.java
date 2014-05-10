/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class Functional {
    public static <X> List<X> filterColl(Collection<X> input, Predicate<X> predicate) {
        return Functional.filter(input, predicate);
    }

    public static <X> List<X> filter(Collection<X> input, Predicate<X> predicate) {
        List result = ListFactory.newList();
        for (X item : input) {
            if (!predicate.test(item)) continue;
            result.add(item);
        }
        return result;
    }

    public static <X> Set<X> filterSet(Collection<X> input, Predicate<X> predicate) {
        Set result = SetFactory.newSet();
        for (X item : input) {
            if (!predicate.test(item)) continue;
            result.add(item);
        }
        return result;
    }

    public static <X> boolean any(Collection<X> input, Predicate<X> predicate) {
        List result = ListFactory.newList();
        for (X item : input) {
            if (!predicate.test(item)) continue;
            return true;
        }
        return false;
    }

    public static <X> boolean all(Collection<X> input, Predicate<X> predicate) {
        List result = ListFactory.newList();
        for (X item : input) {
            if (predicate.test(item)) continue;
            return false;
        }
        return true;
    }

    public static <X> Pair<List<X>, List<X>> partition(Collection<X> input, Predicate<X> predicate) {
        List lTrue = ListFactory.newList();
        List lFalse = ListFactory.newList();
        for (X item : input) {
            if (predicate.test(item)) {
                lTrue.add(item);
                continue;
            }
            lFalse.add(item);
        }
        return new Pair(lTrue, lFalse);
    }

    public static <X, Y> List<Y> map(Collection<X> input, UnaryFunction<X, Y> function) {
        List result = ListFactory.newList();
        for (X item : input) {
            result.add(function.invoke(item));
        }
        return result;
    }

    public static <X> List<X> uniqAll(List<X> input) {
        Set found = SetFactory.newSet();
        List result = ListFactory.newList();
        for (X in : input) {
            if (!found.add(in)) continue;
            result.add(in);
        }
        return result;
    }

    public static <Y, X> Map<Y, List<X>> groupToMapBy(List<X> input, UnaryFunction<X, Y> mapF) {
        Map temp = MapFactory.newMap();
        return Functional.groupToMapBy(input, temp, mapF);
    }

    public static <Y, X> Map<Y, List<X>> groupToMapBy(List<X> input, Map<Y, List<X>> tgt, UnaryFunction<X, Y> mapF) {
        Iterator<X> i$ = input.iterator();
        while (i$.hasNext()) {
            X x;
            List<X> lx;
            Y key;
            if ((lx = tgt.get(key = mapF.invoke(x = i$.next()))) == null) {
                lx = ListFactory.newList();
                tgt.put(key, lx);
            }
            lx.add(x);
        }
        return tgt;
    }

    public static <Y, X> List<Y> groupBy(List<X> input, Comparator<? super X> comparator, UnaryFunction<List<X>, Y> gf) {
        TreeMap<? super X, List> temp = new TreeMap<? super X, List>(comparator);
        Iterator<X> i$ = input.iterator();
        while (i$.hasNext()) {
            List lx;
            X x;
            if ((lx = (List)temp.get(x = i$.next())) == null) {
                lx = ListFactory.newList();
                temp.put((? super X)x, lx);
            }
            lx.add(x);
        }
        List res = ListFactory.newList();
        for (List lx : temp.values()) {
            res.add(gf.invoke(lx));
        }
        return res;
    }
}

