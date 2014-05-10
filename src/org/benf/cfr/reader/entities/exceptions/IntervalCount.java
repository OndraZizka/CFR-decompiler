/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.util.MapFactory;

public class IntervalCount {
    private final TreeMap<Short, Boolean> op = MapFactory.newTreeMap();

    public Pair<Short, Short> generateNonIntersection(Short from, Short to) {
        Map.Entry<Short, Boolean> prevEntry;
        if (to < from) {
            return null;
        }
        Boolean previous = (prevEntry = this.op.floorEntry(from)) == null ? null : prevEntry.getValue();
        boolean braOutside = !(previous != null && previous.booleanValue());
        if (!braOutside) {
            Map.Entry<Short, Boolean> nextEntry;
            if ((nextEntry = this.op.ceilingEntry((short)((from = prevEntry.getKey()) + 1))) == null) {
                throw new IllegalStateException("Internal exception pattern invalid");
            }
            if (!(nextEntry.getValue().booleanValue() || nextEntry.getKey() < to)) {
                return null;
            }
        } else {
            this.op.put(from, true);
        }
        NavigableMap<Short, Boolean> afterMap = this.op.tailMap(from, false);
        Set afterSet = afterMap.entrySet();
        Iterator afterIter = afterSet.iterator();
        while (afterIter.hasNext()) {
            Map.Entry next = (Map.Entry)afterIter.next();
            Short end = (Short)next.getKey();
            boolean isKet = Boolean.FALSE == next.getValue();
            if (end > to) {
                if (isKet) {
                    return Pair.make(from, end);
                }
                this.op.put(to, false);
                return Pair.make(from, to);
            }
            if (end.equals(to)) {
                if (isKet) {
                    return Pair.make(from, end);
                }
                afterIter.remove();
                return Pair.make(from, to);
            }
            afterIter.remove();
        }
        this.op.put(to, false);
        return Pair.make(from, to);
    }
}

