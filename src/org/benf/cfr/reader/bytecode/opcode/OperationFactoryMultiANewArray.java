/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDeltaImpl;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDefault;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class OperationFactoryMultiANewArray
extends OperationFactoryDefault {
    private static final int LENGTH_OF_DATA = 3;
    public static final int OFFSET_OF_DIMS = 2;

    @Override
    public Op01WithProcessedDataAndByteJumps createOperation(JVMInstr instr, ByteData bd, ConstantPool cp, int offset) {
        byte[] args = bd.getBytesAt(3, 1);
        java.lang.Object targetOffsets = null;
        ConstantPoolEntry[] cpEntries = new ConstantPoolEntry[]{cp.getEntry(bd.getS2At(1))};
        return new Op01WithProcessedDataAndByteJumps(instr, args, targetOffsets, offset, cpEntries);
    }

    @Override
    public StackDelta getStackDelta(JVMInstr instr, byte[] data, ConstantPoolEntry[] cpEntries, StackSim stackSim, Method method) {
        int numDims = data[2];
        if (numDims < 0) {
            throw new ConfusedCFRException("NYI : Unsupported num of dims, should be using a short not a byte.");
        }
        List stackTypeList = ListFactory.newList();
        for (int x = 0; x < numDims; ++x) {
            stackTypeList.add((StackType)StackType.INT);
        }
        return new StackDeltaImpl(new StackTypes(stackTypeList), StackType.REF.asList());
    }
}

