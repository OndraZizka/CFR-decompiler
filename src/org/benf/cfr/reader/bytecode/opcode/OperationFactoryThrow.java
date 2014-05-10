/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDefault;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class OperationFactoryThrow
extends OperationFactoryDefault {
    @Override
    public Op01WithProcessedDataAndByteJumps createOperation(JVMInstr instr, ByteData bd, ConstantPool cp, int offset) {
        Object args = instr.getRawLength() == 0 ? null : (Object)bd.getBytesAt(instr.getRawLength(), 1);
        int[] targetOffsets = new int[]{};
        return new Op01WithProcessedDataAndByteJumps(instr, args, targetOffsets, offset);
    }
}

