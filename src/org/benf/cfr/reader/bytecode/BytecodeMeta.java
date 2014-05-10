/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.entities.attributes.AttributeCode;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class BytecodeMeta {
    private final EnumSet<CodeInfoFlag> flags = EnumSet.noneOf(CodeInfoFlag.class);
    private final Set<Integer> livenessClashes = SetFactory.newSet();

    public BytecodeMeta(List<Op01WithProcessedDataAndByteJumps> op1s, AttributeCode code) {
        int flagCount = CodeInfoFlag.values().length;
        if (!code.getExceptionTableEntries().isEmpty()) {
            this.flags.add((Object)CodeInfoFlag.USES_EXCEPTIONS);
        }
        for (Op01WithProcessedDataAndByteJumps op : op1s) {
            switch (op.getJVMInstr()) {
                case MONITOREXIT: 
                case MONITORENTER: {
                    this.flags.add((Object)CodeInfoFlag.USES_MONITORS);
                    break;
                }
                case INVOKEDYNAMIC: {
                    this.flags.add((Object)CodeInfoFlag.USES_INVOKEDYNAMIC);
                }
            }
            if (this.flags.size() != flagCount) continue;
            return;
        }
    }

    public boolean has(CodeInfoFlag flag) {
        return this.flags.contains((Object)flag);
    }

    public void informLivenessClashes(Set<Integer> slots) {
        this.flags.add((Object)CodeInfoFlag.LIVENESS_CLASH);
        this.livenessClashes.addAll(slots);
    }

    public Set<Integer> getLivenessClashes() {
        return this.livenessClashes;
    }

    public static UnaryFunction<BytecodeMeta, Boolean> testFlag(CodeInfoFlag flag) {
        return new FlagTest(flag, null);
    }

    static class FlagTest
    implements UnaryFunction<BytecodeMeta, Boolean> {
        private final CodeInfoFlag flag;

        private FlagTest(CodeInfoFlag flag) {
            this.flag = flag;
        }

        @Override
        public Boolean invoke(BytecodeMeta arg) {
            return arg.has(this.flag);
        }

        /* synthetic */ FlagTest(CodeInfoFlag x0, 1 x1) {
            this(x0);
        }
    }

    public static enum CodeInfoFlag {
        USES_MONITORS,
        USES_EXCEPTIONS,
        USES_INVOKEDYNAMIC,
        LIVENESS_CLASH;
        

        private CodeInfoFlag() {
        }
    }

}

