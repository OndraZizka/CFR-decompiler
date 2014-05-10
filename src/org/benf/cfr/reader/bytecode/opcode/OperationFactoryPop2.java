/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDeltaImpl;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDefault;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class OperationFactoryPop2
extends OperationFactoryDefault {
    @Override
    public StackDelta getStackDelta(JVMInstr instr, byte[] data, ConstantPoolEntry[] cpEntries, StackSim stackSim, Method method) {
        StackType topStackEntry = stackSim.getEntry(0).getType();
        if (topStackEntry.getComputationCategory() == 2) {
            return new StackDeltaImpl(topStackEntry.asList(), StackTypes.EMPTY);
        }
        StackType nextStackEntry = stackSim.getEntry(1).getType();
        StackTypes stackTypesPopped = new StackTypes(topStackEntry, nextStackEntry);
        return new StackDeltaImpl(stackTypesPopped, StackTypes.EMPTY);
    }

    @Override
    public Op01WithProcessedDataAndByteJumps createOperation(JVMInstr instr, ByteData bd, ConstantPool cp, int offset) {
        java.lang.Object args = null;
        java.lang.Object targetOffsets = null;
        return new Op01WithProcessedDataAndByteJumps(instr, args, targetOffsets, offset);
    }
}

