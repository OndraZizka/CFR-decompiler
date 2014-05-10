/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.CommaHelp;

public class ExceptionGroup {
    private short bytecodeIndexFrom;
    private short byteCodeIndexTo;
    private short minHandlerStart = 32767;
    private List<Entry> entries = ListFactory.newList();
    private final BlockIdentifier tryBlockIdentifier;
    private final ConstantPool cp;

    public ExceptionGroup(short bytecodeIndexFrom, BlockIdentifier blockIdentifier, ConstantPool cp) {
        this.bytecodeIndexFrom = bytecodeIndexFrom;
        this.tryBlockIdentifier = blockIdentifier;
        this.cp = cp;
    }

    public void add(ExceptionTableEntry entry) {
        if (entry.getBytecodeIndexHandler() == entry.getBytecodeIndexFrom()) {
            return;
        }
        if (entry.getBytecodeIndexHandler() < this.minHandlerStart) {
            this.minHandlerStart = entry.getBytecodeIndexHandler();
        }
        this.entries.add(new Entry(entry));
        if (entry.getBytecodeIndexTo() <= this.byteCodeIndexTo) return;
        this.byteCodeIndexTo = entry.getBytecodeIndexTo();
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    public void mutateBytecodeIndexFrom(short bytecodeIndexFrom) {
        this.bytecodeIndexFrom = bytecodeIndexFrom;
    }

    public short getBytecodeIndexFrom() {
        return this.bytecodeIndexFrom;
    }

    public short getByteCodeIndexTo() {
        return this.byteCodeIndexTo;
    }

    public BlockIdentifier getTryBlockIdentifier() {
        return this.tryBlockIdentifier;
    }

    public void removeSynchronisedHandlers(Map<Integer, Integer> lutByOffset, Map<Integer, Integer> lutByIdx, List<Op01WithProcessedDataAndByteJumps> instrs) {
        Iterator<Entry> entryIterator = this.entries.iterator();
        while (entryIterator.hasNext()) {
            Entry entry;
            if (!this.isSynchronisedHandler(entry = entryIterator.next(), lutByOffset, lutByIdx, instrs)) continue;
            entryIterator.remove();
        }
    }

    private boolean isSynchronisedHandler(Entry entry, Map<Integer, Integer> lutByOffset, Map<Integer, Integer> lutByIdx, List<Op01WithProcessedDataAndByteJumps> instrs) {
        Integer catchLoad;
        Integer catchStore;
        int idx;
        Op01WithProcessedDataAndByteJumps start;
        ExceptionTableEntry tableEntry = entry.entry;
        Integer offset = lutByOffset.get(Integer.valueOf(tableEntry.getBytecodeIndexHandler()));
        if (offset == null) {
            return false;
        }
        if ((idx = offset.intValue()) >= instrs.size()) {
            return false;
        }
        if ((catchStore = (start = instrs.get(idx)).getAStoreIdx()) == null) {
            return false;
        }
        ++idx;
        int nUnlocks = 0;
        do {
            Op01WithProcessedDataAndByteJumps load;
            Op01WithProcessedDataAndByteJumps next;
            JVMInstr instr;
            Integer loadIdx;
            if (idx + 1 >= instrs.size()) {
                return false;
            }
            if ((loadIdx = (load = instrs.get(idx)).getALoadIdx()) == null && (instr = load.getJVMInstr()) != JVMInstr.LDC) break;
            if ((next = instrs.get(idx + 1)).getJVMInstr() != JVMInstr.MONITOREXIT) break;
            ++nUnlocks;
            idx+=2;
        } while (true);
        if (nUnlocks == 0) {
            return false;
        }
        if (!catchStore.equals(catchLoad = instrs.get(idx).getALoadIdx())) {
            return false;
        }
        if (instrs.get(++idx).getJVMInstr() == JVMInstr.ATHROW) return true;
        return false;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[egrp ").append(this.tryBlockIdentifier).append(" [");
        boolean bfirst = true;
        for (Entry e : this.entries) {
            bfirst = CommaHelp.comma(bfirst, sb);
            sb.append(e.getPriority());
        }
        sb.append(" : ").append(this.bytecodeIndexFrom).append("->").append(this.byteCodeIndexTo).append(")]");
        return sb.toString();
    }

    public class ExtenderKey {
        private final JavaRefTypeInstance type;
        private final short handler;

        public ExtenderKey(JavaRefTypeInstance type, short handler) {
            this.type = type;
            this.handler = handler;
        }

        public JavaRefTypeInstance getType() {
            return this.type;
        }

        public short getHandler() {
            return this.handler;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            ExtenderKey that = (ExtenderKey)o;
            if (this.handler != that.handler) {
                return false;
            }
            if (!(this.type != null ? !this.type.equals(that.type) : that.type != null)) return true;
            return false;
        }

        public int hashCode() {
            int result = this.type != null ? this.type.hashCode() : 0;
            result = 31 * result + this.handler;
            return result;
        }
    }

    public class Entry
    implements ComparableUnderEC {
        private final ExceptionTableEntry entry;
        private final JavaRefTypeInstance refType;

        public Entry(ExceptionTableEntry entry) {
            this.entry = entry;
            this.refType = entry.getCatchType(ExceptionGroup.this.cp);
        }

        public short getBytecodeIndexTo() {
            return this.entry.getBytecodeIndexTo();
        }

        public short getBytecodeIndexHandler() {
            return this.entry.getBytecodeIndexHandler();
        }

        public boolean isJustThrowable() {
            JavaRefTypeInstance type = this.entry.getCatchType(ExceptionGroup.this.cp);
            return type.getRawName().equals("java.lang.Throwable");
        }

        public int getPriority() {
            return this.entry.getPriority();
        }

        public JavaRefTypeInstance getCatchType() {
            return this.refType;
        }

        public ExceptionGroup getExceptionGroup() {
            return ExceptionGroup.this;
        }

        public BlockIdentifier getTryBlockIdentifier() {
            return ExceptionGroup.this.getTryBlockIdentifier();
        }

        public String toString() {
            JavaRefTypeInstance name = this.getCatchType();
            return ExceptionGroup.this.toString() + " " + name.getRawName();
        }

        @Override
        public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
            if (o == null) {
                return false;
            }
            if (o == this) {
                return true;
            }
            if (this.getClass() != o.getClass()) {
                return false;
            }
            Entry other = (Entry)o;
            if (!constraint.equivalent(this.entry, other.entry)) {
                return false;
            }
            if (constraint.equivalent(this.refType, other.refType)) return true;
            return false;
        }

        public ExtenderKey getExtenderKey() {
            return new ExtenderKey(this.refType, this.entry.getBytecodeIndexHandler());
        }
    }

}

