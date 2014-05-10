/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDefault;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class OperationFactoryInvoke
extends OperationFactoryDefault {
    private static final int LENGTH_OF_DATA = 2;
    private static final int OFFSET_OF_METHOD_INDEX = 1;
    private final boolean instance;

    public OperationFactoryInvoke(boolean instance) {
        this.instance = instance;
    }

    @Override
    public Op01WithProcessedDataAndByteJumps createOperation(JVMInstr instr, ByteData bd, ConstantPool cp, int offset) {
        byte[] args = bd.getBytesAt(2, 1);
        java.lang.Object targetOffsets = null;
        ConstantPoolEntry[] cpEntries = new ConstantPoolEntry[]{cp.getEntry(bd.getS2At(1))};
        return new Op01WithProcessedDataAndByteJumps(instr, args, targetOffsets, offset, cpEntries);
    }

    @Override
    public StackDelta getStackDelta(JVMInstr instr, byte[] data, ConstantPoolEntry[] cpEntries, StackSim stackSim, Method method) {
        ConstantPoolEntryMethodRef methodRef = (ConstantPoolEntryMethodRef)cpEntries[0];
        return methodRef.getNameAndTypeEntry().getStackDelta(this.instance);
    }
}

