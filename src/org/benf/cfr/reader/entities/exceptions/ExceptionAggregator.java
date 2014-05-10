/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;
import org.benf.cfr.reader.entities.exceptions.IntervalCount;
import org.benf.cfr.reader.entities.exceptions.IntervalOverlapper;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.Troolean;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class ExceptionAggregator {
    private final List<ExceptionGroup> exceptionsByRange = ListFactory.newList();
    private final Method method;
    private final Map<Integer, Integer> lutByOffset;
    private final Map<Integer, Integer> lutByIdx;
    private final List<Op01WithProcessedDataAndByteJumps> instrs;
    private final Options options;
    private final boolean aggressivePrune;
    private final boolean aggressiveAggregate;

    private boolean canExtendTo(ExceptionTableEntry a, ExceptionTableEntry b) {
        short startNext = b.getBytecodeIndexFrom();
        int current = a.getBytecodeIndexTo();
        if (current > startNext) {
            return false;
        }
        block3 : while (current < startNext) {
            JVMInstr instr;
            Op01WithProcessedDataAndByteJumps op;
            Integer idx;
            if ((idx = this.lutByOffset.get(current)) == null) {
                return false;
            }
            if ((instr = (op = this.instrs.get(idx)).getJVMInstr()).isNoThrow()) {
                current+=op.getInstructionLength();
                continue;
            }
            if (!this.aggressivePrune) return false;
            switch (instr) {
                case GETSTATIC: {
                    current+=op.getInstructionLength();
                    continue block3;
                }
            }
            return false;
        }
        return true;
    }

    private static int canExpandTryBy(int idx, List<Op01WithProcessedDataAndByteJumps> statements) {
        Op01WithProcessedDataAndByteJumps op = statements.get(idx);
        JVMInstr instr = op.getJVMInstr();
        switch (instr) {
            case GOTO: 
            case GOTO_W: 
            case RETURN: 
            case ARETURN: 
            case IRETURN: 
            case LRETURN: 
            case DRETURN: 
            case FRETURN: {
                return op.getInstructionLength();
            }
            case ALOAD: 
            case ALOAD_0: 
            case ALOAD_1: 
            case ALOAD_2: 
            case ALOAD_3: {
                Op01WithProcessedDataAndByteJumps op2 = statements.get(idx + 1);
                if (op2.getJVMInstr() != JVMInstr.MONITOREXIT) return 0;
                return op.getInstructionLength() + op2.getInstructionLength();
            }
        }
        return 0;
    }

    public ExceptionAggregator(List<ExceptionTableEntry> rawExceptions, BlockIdentifierFactory blockIdentifierFactory, Map<Integer, Integer> lutByOffset, Map<Integer, Integer> lutByIdx, List<Op01WithProcessedDataAndByteJumps> instrs, Options options, ConstantPool cp, Method method) {
        this.method = method;
        this.lutByIdx = lutByIdx;
        this.lutByOffset = lutByOffset;
        this.instrs = instrs;
        this.options = options;
        this.aggressivePrune = options.getOption(OptionsImpl.FORCE_PRUNE_EXCEPTIONS) == Troolean.TRUE;
        this.aggressiveAggregate = options.getOption(OptionsImpl.FORCE_AGGRESSIVE_EXCEPTION_AGG) == Troolean.TRUE;
        rawExceptions = Functional.filter(rawExceptions, new ValidException(null));
        if (rawExceptions.isEmpty()) {
            return;
        }
        List extended = ListFactory.newList();
        Iterator<ExceptionTableEntry> i$ = rawExceptions.iterator();
        while (i$.hasNext()) {
            short indexFrom;
            ExceptionTableEntry exceptionTableEntry;
            ExceptionTableEntry exceptionTableEntryOrig = exceptionTableEntry = i$.next();
            int indexTo = exceptionTableEntry.getBytecodeIndexTo();
            do {
                int offset;
                Integer tgtIdx;
                exceptionTableEntryOrig = exceptionTableEntry;
                if ((tgtIdx = lutByOffset.get(indexTo)) == null) continue;
                if ((offset = ExceptionAggregator.canExpandTryBy(tgtIdx, instrs)) != 0) {
                    exceptionTableEntry = exceptionTableEntry.copyWithRange(exceptionTableEntry.getBytecodeIndexFrom(), (short)(exceptionTableEntry.getBytecodeIndexTo() + offset));
                }
                indexTo+=offset;
            } while (exceptionTableEntry != exceptionTableEntryOrig);
            short handlerIndex = exceptionTableEntry.getBytecodeIndexHandler();
            indexTo = exceptionTableEntry.getBytecodeIndexTo();
            if ((indexFrom = exceptionTableEntry.getBytecodeIndexFrom()) < handlerIndex && indexTo >= handlerIndex) {
                exceptionTableEntry = exceptionTableEntry.copyWithRange(indexFrom, handlerIndex);
            }
            extended.add((ExceptionTableEntry)exceptionTableEntry);
        }
        rawExceptions = extended;
        Map grouped = Functional.groupToMapBy(rawExceptions, new UnaryFunction<ExceptionTableEntry, Short>(){

            @Override
            public Short invoke(ExceptionTableEntry arg) {
                return arg.getCatchType();
            }
        });
        List processedExceptions = ListFactory.newList(rawExceptions.size());
        for (List<ExceptionTableEntry> list : grouped.values()) {
            IntervalCount intervalCount = new IntervalCount();
            for (ExceptionTableEntry e : list) {
                Pair<Short, Short> res;
                short to;
                short from = e.getBytecodeIndexFrom();
                if ((res = intervalCount.generateNonIntersection(from, to = e.getBytecodeIndexTo())) == null) continue;
                processedExceptions.add((ExceptionTableEntry)new ExceptionTableEntry(res.getFirst(), res.getSecond(), e.getBytecodeIndexHandler(), e.getCatchType(), e.getPriority()));
            }
        }
        List byTargetList = Functional.groupBy(processedExceptions, new Comparator<ExceptionTableEntry>(){

            @Override
            public int compare(ExceptionTableEntry exceptionTableEntry, ExceptionTableEntry exceptionTableEntry1) {
                int hd = exceptionTableEntry.getBytecodeIndexHandler() - exceptionTableEntry1.getBytecodeIndexHandler();
                if (hd == 0) return exceptionTableEntry.getCatchType() - exceptionTableEntry1.getCatchType();
                return hd;
            }
        }, new UnaryFunction<List<ExceptionTableEntry>, ByTarget>(){

            @Override
            public ByTarget invoke(List<ExceptionTableEntry> arg) {
                return new ByTarget(arg);
            }
        });
        rawExceptions = ListFactory.newList();
        Map byTargetMap = MapFactory.newMap();
        for (ByTarget t : byTargetList) {
            byTargetMap.put(((ExceptionTableEntry)t.entries.get(0)).getBytecodeIndexHandler(), (ByTarget)t);
        }
        for (ByTarget byTarget : byTargetList) {
            rawExceptions.addAll(byTarget.getAggregated());
        }
        IntervalOverlapper intervalOverlapper = new IntervalOverlapper(rawExceptions);
        rawExceptions = intervalOverlapper.getExceptions();
        Collections.sort(rawExceptions);
        CompareExceptionTablesByRange compareExceptionTablesByStart = new CompareExceptionTablesByRange(null);
        ExceptionTableEntry prev = null;
        ExceptionGroup currentGroup = null;
        List rawExceptionsByRange = ListFactory.newList();
        for (ExceptionTableEntry e : rawExceptions) {
            if (prev == null || compareExceptionTablesByStart.compare(e, prev) != 0) {
                currentGroup = new ExceptionGroup(e.getBytecodeIndexFrom(), blockIdentifierFactory.getNextBlockIdentifier(BlockType.TRYBLOCK), cp);
                rawExceptionsByRange.add((ExceptionGroup)currentGroup);
                prev = e;
            }
            currentGroup.add(e);
        }
        this.exceptionsByRange.addAll(rawExceptionsByRange);
    }

    public List<ExceptionGroup> getExceptionsGroups() {
        return this.exceptionsByRange;
    }

    public void removeSynchronisedHandlers(Map<Integer, Integer> lutByOffset, Map<Integer, Integer> lutByIdx, List<Op01WithProcessedDataAndByteJumps> instrs) {
        Iterator<ExceptionGroup> groupIterator = this.exceptionsByRange.iterator();
        ExceptionGroup prev = null;
        while (groupIterator.hasNext()) {
            List<ExceptionGroup.Entry> groupEntries;
            List<ExceptionGroup.Entry> prevEntries;
            ExceptionGroup group = groupIterator.next();
            boolean prevSame = false;
            if (prev != null && (groupEntries = group.getEntries()).equals(prevEntries = prev.getEntries())) {
                prevSame = true;
            }
            group.removeSynchronisedHandlers(lutByOffset, lutByIdx, instrs);
            if (group.getEntries().isEmpty()) {
                groupIterator.remove();
                continue;
            }
            prev = group;
        }
    }

    public void aggressivePruning(Map<Integer, Integer> lutByOffset, Map<Integer, Integer> lutByIdx, List<Op01WithProcessedDataAndByteJumps> instrs) {
        Iterator<ExceptionGroup> groupIterator = this.exceptionsByRange.iterator();
        while (groupIterator.hasNext()) {
            ExceptionGroup.Entry entry;
            ExceptionGroup group;
            Integer index;
            Op01WithProcessedDataAndByteJumps handlerStartInstr;
            List<ExceptionGroup.Entry> entries;
            short handler;
            if ((entries = (group = groupIterator.next()).getEntries()).size() != 1) continue;
            if ((index = lutByOffset.get(Integer.valueOf(handler = (entry = entries.get(0)).getBytecodeIndexHandler()))) == null) continue;
            if ((handlerStartInstr = instrs.get(index)).getJVMInstr() != JVMInstr.ATHROW) continue;
            groupIterator.remove();
        }
    }

    static class ValidException
    implements Predicate<ExceptionTableEntry> {
        private ValidException() {
        }

        @Override
        public boolean test(ExceptionTableEntry in) {
            return in.getBytecodeIndexFrom() != in.getBytecodeIndexHandler();
        }

        /* synthetic */ ValidException( x0) {
            this();
        }
    }

    class ByTarget {
        private final List<ExceptionTableEntry> entries;

        public ByTarget(List<ExceptionTableEntry> entries) {
            this.entries = entries;
        }

        public Collection<ExceptionTableEntry> getAggregated() {
            Collections.sort(this.entries, new CompareExceptionTablesByRange(null));
            List res = ListFactory.newList();
            ExceptionTableEntry held = null;
            for (ExceptionTableEntry entry : this.entries) {
                if (held == null) {
                    held = entry;
                    continue;
                }
                if (held.getBytecodeIndexTo() == entry.getBytecodeIndexFrom()) {
                    held = held.aggregateWith(entry);
                    continue;
                }
                if (held.getBytecodeIndexFrom() == entry.getBytecodeIndexFrom() && held.getBytecodeIndexTo() <= entry.getBytecodeIndexTo()) {
                    held = entry;
                    continue;
                }
                if (held.getBytecodeIndexFrom() < entry.getBytecodeIndexFrom() && entry.getBytecodeIndexFrom() < held.getBytecodeIndexTo() && entry.getBytecodeIndexTo() > held.getBytecodeIndexTo()) {
                    held = held.aggregateWithLenient(entry);
                    continue;
                }
                if (ExceptionAggregator.this.aggressiveAggregate && ExceptionAggregator.this.canExtendTo(held, entry)) {
                    held = held.aggregateWithLenient(entry);
                    continue;
                }
                res.add((ExceptionTableEntry)held);
                held = entry;
            }
            if (held == null) return res;
            res.add(held);
            return res;
        }
    }

    static class CompareExceptionTablesByRange
    implements Comparator<ExceptionTableEntry> {
        private CompareExceptionTablesByRange() {
        }

        @Override
        public int compare(ExceptionTableEntry exceptionTableEntry, ExceptionTableEntry exceptionTableEntry1) {
            int res = exceptionTableEntry.getBytecodeIndexFrom() - exceptionTableEntry1.getBytecodeIndexFrom();
            if (res == 0) return exceptionTableEntry.getBytecodeIndexTo() - exceptionTableEntry1.getBytecodeIndexTo();
            return res;
        }

        /* synthetic */ CompareExceptionTablesByRange( x0) {
            this();
        }
    }

}

