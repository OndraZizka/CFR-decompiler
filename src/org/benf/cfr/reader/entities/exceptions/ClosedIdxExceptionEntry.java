/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;

public class ClosedIdxExceptionEntry {
    private final int start;
    private final int end;
    private final int handler;
    private final short catchType;
    private final int priority;
    private final JavaRefTypeInstance catchRefType;

    public ClosedIdxExceptionEntry(int start, int end, int handler, short catchType, int priority, JavaRefTypeInstance catchRefType) {
        this.start = start;
        this.end = end;
        this.handler = handler;
        this.catchType = catchType;
        this.priority = priority;
        this.catchRefType = catchRefType;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public int getHandler() {
        return this.handler;
    }

    public short getCatchType() {
        return this.catchType;
    }

    public int getPriority() {
        return this.priority;
    }

    public JavaRefTypeInstance getCatchRefType() {
        return this.catchRefType;
    }

    public ClosedIdxExceptionEntry withRange(int newStart, int newEnd) {
        if (this.start != newStart || this.end != newEnd) return new ClosedIdxExceptionEntry(newStart, newEnd, this.handler, this.catchType, this.priority, this.catchRefType);
        return this;
    }

    public ExceptionTableEntry convertToRaw(Map<Integer, Integer> offsetByIdx) {
        return new ExceptionTableEntry((short)offsetByIdx.get(this.start).intValue(), (short)offsetByIdx.get(this.end + 1).intValue(), (short)offsetByIdx.get(this.handler).intValue(), this.catchType, this.priority);
    }
}

