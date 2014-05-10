/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class ExceptionTableEntry
implements Comparable<ExceptionTableEntry> {
    private static final int OFFSET_INDEX_FROM = 0;
    private static final int OFFSET_INDEX_TO = 2;
    private static final int OFFSET_INDEX_HANDLER = 4;
    private static final int OFFSET_CATCH_TYPE = 6;
    private final short bytecode_index_from;
    private final short bytecode_index_to;
    private final short bytecode_index_handler;
    private final short catch_type;
    private final int priority;

    public ExceptionTableEntry(ByteData raw, int priority) {
        this(raw.getS2At(0), raw.getS2At(2), raw.getS2At(4), raw.getS2At(6), priority);
    }

    public ExceptionTableEntry(short from, short to, short handler, short catchType, int priority) {
        this.bytecode_index_from = from;
        this.bytecode_index_to = to;
        this.bytecode_index_handler = handler;
        this.catch_type = catchType;
        this.priority = priority;
        if (to >= from) return;
        throw new IllegalStateException("Malformed exception block, to < from");
    }

    public JavaRefTypeInstance getCatchType(ConstantPool cp) {
        if (this.catch_type != 0) return (JavaRefTypeInstance)cp.getClassEntry(this.catch_type).getTypeInstance();
        return cp.getClassCache().getRefClassFor("java.lang.Throwable");
    }

    public ExceptionTableEntry copyWithRange(short from, short to) {
        return new ExceptionTableEntry(from, to, this.bytecode_index_handler, this.catch_type, this.priority);
    }

    public short getBytecodeIndexFrom() {
        return this.bytecode_index_from;
    }

    public short getBytecodeIndexTo() {
        return this.bytecode_index_to;
    }

    public short getBytecodeIndexHandler() {
        return this.bytecode_index_handler;
    }

    public short getCatchType() {
        return this.catch_type;
    }

    public int getPriority() {
        return this.priority;
    }

    public ExceptionTableEntry aggregateWith(ExceptionTableEntry later) {
        if (this.bytecode_index_from < later.bytecode_index_from && this.bytecode_index_to == later.bytecode_index_from) return new ExceptionTableEntry(this.bytecode_index_from, later.bytecode_index_to, this.bytecode_index_handler, this.catch_type, this.priority);
        throw new ConfusedCFRException("Can't aggregate exceptionTableEntries");
    }

    public ExceptionTableEntry aggregateWithLenient(ExceptionTableEntry later) {
        if (this.bytecode_index_from < later.bytecode_index_from) return new ExceptionTableEntry(this.bytecode_index_from, later.bytecode_index_to, this.bytecode_index_handler, this.catch_type, this.priority);
        throw new ConfusedCFRException("Can't aggregate exceptionTableEntries");
    }

    public static UnaryFunction<ByteData, ExceptionTableEntry> getBuilder(ConstantPool cp) {
        return new ExceptionTableEntryBuilder(cp);
    }

    @Override
    public int compareTo(ExceptionTableEntry other) {
        int res = this.bytecode_index_from - other.bytecode_index_from;
        if (res != 0) {
            return res;
        }
        if ((res = this.bytecode_index_to - other.bytecode_index_to) != 0) {
            return 0 - res;
        }
        res = this.bytecode_index_handler - other.bytecode_index_handler;
        return res;
    }

    public String toString() {
        return "ExceptionTableEntry " + this.priority + " : [" + this.bytecode_index_from + "->" + this.bytecode_index_to + ") : " + this.bytecode_index_handler;
    }

    static class ExceptionTableEntryBuilder
    implements UnaryFunction<ByteData, ExceptionTableEntry> {
        int idx = 0;

        public ExceptionTableEntryBuilder(ConstantPool cp) {
        }

        @Override
        public ExceptionTableEntry invoke(ByteData arg) {
            return new ExceptionTableEntry(arg, this.idx++);
        }
    }

}

