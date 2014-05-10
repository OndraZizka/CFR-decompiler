/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;

public class IntervalOverlapper {
    private final NavigableMap<Short, Set<ExceptionTableEntry>> starts = MapFactory.newTreeMap();
    private final NavigableMap<Short, Set<ExceptionTableEntry>> ends = MapFactory.newTreeMap();

    public IntervalOverlapper(List<ExceptionTableEntry> entries) {
        this.processEntries(entries);
    }

    private void processEntries(List<ExceptionTableEntry> entries) {
        for (ExceptionTableEntry e : entries) {
            this.processEntry(e);
        }
    }

    private static <X> Set<X> raze(Collection<Set<X>> in) {
        Set res = SetFactory.newSet();
        for (Set<X> i : in) {
            res.addAll(i);
        }
        return res;
    }

    private void processEntry(ExceptionTableEntry e) {
        short from = e.getBytecodeIndexFrom();
        short to = e.getBytecodeIndexTo();
        NavigableMap<Short, Set<ExceptionTableEntry>> startedBeforeStart = this.starts.headMap(from, false);
        NavigableMap<Short, Set<ExceptionTableEntry>> endsBeforeEnd = this.ends.headMap(to, false);
        NavigableMap<Short, Set<ExceptionTableEntry>> endsInside = endsBeforeEnd.tailMap(from, false);
        Set<X> overlapStartsBefore = IntervalOverlapper.raze(endsInside.values());
        overlapStartsBefore.retainAll(IntervalOverlapper.raze(startedBeforeStart.values()));
        NavigableMap<Short, Set<ExceptionTableEntry>> endsAfterEnd = this.ends.tailMap(to, false);
        NavigableMap<Short, Set<ExceptionTableEntry>> startedAfterStart = this.starts.tailMap(from, false);
        NavigableMap<Short, Set<ExceptionTableEntry>> startsInside = startedAfterStart.headMap(to, false);
        Set<X> overlapEndsAfter = IntervalOverlapper.raze(startsInside.values());
        overlapEndsAfter.retainAll(IntervalOverlapper.raze(endsAfterEnd.values()));
        if (overlapEndsAfter.isEmpty() && overlapStartsBefore.isEmpty()) {
            this.addEntry(e);
            return;
        }
        short remainingBlockStart = from;
        short remainingBlockTo = to;
        List output = ListFactory.newList();
        if (!overlapStartsBefore.isEmpty()) {
            TreeSet<Short> blockEnds = new TreeSet<Short>();
            for (ExceptionTableEntry e22 : overlapStartsBefore) {
                blockEnds.add(e22.getBytecodeIndexTo());
                ((Set)this.starts.get((Object)e22.getBytecodeIndexFrom())).remove(e22);
                ((Set)this.ends.get((Object)e22.getBytecodeIndexTo())).remove(e22);
            }
            short currentFrom = from;
            for (Short end : blockEnds) {
                ExceptionTableEntry out = e.copyWithRange(currentFrom, end);
                this.addEntry(out);
                output.add((ExceptionTableEntry)out);
                currentFrom = end;
            }
            remainingBlockStart = currentFrom;
            blockEnds.add(from);
            block2 : for (ExceptionTableEntry e22 : overlapStartsBefore) {
                currentFrom = e22.getBytecodeIndexFrom();
                Iterator i$ = blockEnds.iterator();
                while (i$.hasNext()) {
                    Short end2;
                    if ((end2 = (Short)i$.next()) > e22.getBytecodeIndexTo()) continue block2;
                    ExceptionTableEntry out = e22.copyWithRange(currentFrom, end2);
                    this.addEntry(out);
                    output.add((ExceptionTableEntry)out);
                    currentFrom = end2;
                }
            }
        }
        if (!overlapEndsAfter.isEmpty()) {
            TreeSet<Short> blockStarts = new TreeSet<Short>();
            for (ExceptionTableEntry e22 : overlapStartsBefore) {
                blockStarts.add(e22.getBytecodeIndexFrom());
                ((Set)this.starts.get((Object)e22.getBytecodeIndexFrom())).remove(e22);
                ((Set)this.ends.get((Object)e22.getBytecodeIndexTo())).remove(e22);
            }
            List<Short> revBlockStarts = ListFactory.newList(blockStarts);
            short currentTo = to;
            for (int x = revBlockStarts.size() - 1; x >= 0; --x) {
                Short start = (Short)revBlockStarts.get(x);
                ExceptionTableEntry out = e.copyWithRange(start, currentTo);
                this.addEntry(out);
                output.add((ExceptionTableEntry)out);
                currentTo = start;
            }
            remainingBlockTo = currentTo;
            revBlockStarts.add(to);
            block6 : for (ExceptionTableEntry e2 : overlapStartsBefore) {
                currentTo = e2.getBytecodeIndexTo();
                for (int x2 = revBlockStarts.size() - 1; x2 >= 0; --x2) {
                    Short start;
                    if ((start = (Short)revBlockStarts.get(x2)) <= e2.getBytecodeIndexFrom()) continue block6;
                    ExceptionTableEntry out = e.copyWithRange(start, currentTo);
                    this.addEntry(out);
                    output.add((ExceptionTableEntry)out);
                    currentTo = start;
                }
            }
        }
        ExceptionTableEntry out = e.copyWithRange(remainingBlockStart, remainingBlockTo);
        this.addEntry(out);
        output.add((ExceptionTableEntry)out);
    }

    void addEntry(ExceptionTableEntry e) {
        this.add((NavigableMap<A, Set<B>>)this.starts, e.getBytecodeIndexFrom(), (B)e);
        this.add((NavigableMap<A, Set<B>>)this.ends, e.getBytecodeIndexTo(), (B)e);
    }

    <A, B> void add(NavigableMap<A, Set<B>> m, A k, B v) {
        Set b = (Set)m.get(k);
        if (b == null) {
            b = SetFactory.newSet();
            m.put(k, b);
        }
        b.add(v);
    }

    public List<ExceptionTableEntry> getExceptions() {
        return ListFactory.newList(IntervalOverlapper.raze(this.starts.values()));
    }
}

