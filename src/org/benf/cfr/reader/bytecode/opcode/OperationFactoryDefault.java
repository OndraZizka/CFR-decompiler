/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDeltaImpl;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactory;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class OperationFactoryDefault
implements OperationFactory {
    @Override
    public StackDelta getStackDelta(JVMInstr instr, byte[] data, ConstantPoolEntry[] cpEntries, StackSim stackSim, Method method) {
        return new StackDeltaImpl(instr.getRawStackPopped(), instr.getRawStackPushed());
    }

    @Override
    public Op01WithProcessedDataAndByteJumps createOperation(JVMInstr instr, ByteData bd, ConstantPool cp, int offset) {
        Object args = instr.getRawLength() == 0 ? null : (Object)bd.getBytesAt(instr.getRawLength(), 1);
        Object targetOffsets = null;
        return new Op01WithProcessedDataAndByteJumps(instr, args, targetOffsets, offset);
    }

    protected static /* varargs */ StackTypes getStackTypes(StackSim stackSim, Integer ... indexes) {
        if (indexes.length == 1) {
            return stackSim.getEntry(indexes[0]).getType().asList();
        }
        List stackTypes = ListFactory.newList();
        for (Integer index : indexes) {
            stackTypes.add((StackType)stackSim.getEntry(index).getType());
        }
        return new StackTypes(stackTypes);
    }

    protected static int getCat(StackSim stackSim, int index) {
        return stackSim.getEntry(index).getType().getComputationCategory();
    }

    protected static void checkCat(StackSim stackSim, int index, int category) {
        if (OperationFactoryDefault.getCat(stackSim, index) == category) return;
        throw new ConfusedCFRException("Expected category " + category + " at index " + index);
    }

    public static enum Handler {
        INSTANCE(new OperationFactoryDefault());
        
        private final OperationFactoryDefault h;

        private Handler(OperationFactoryDefault h) {
            this.h = h;
        }

        public OperationFactory getHandler() {
            return this.h;
        }
    }

}

