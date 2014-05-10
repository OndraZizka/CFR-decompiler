/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitchEntry;
import org.benf.cfr.reader.bytecode.opcode.DecodedTableSwitch;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDefault;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class OperationFactoryTableSwitch
extends OperationFactoryDefault {
    private static final int OFFSET_OF_LOWBYTE = 4;
    private static final int OFFSET_OF_HIGHBYTE = 8;
    private static final int OFFSET_OF_OFFSETS = 12;

    @Override
    public Op01WithProcessedDataAndByteJumps createOperation(JVMInstr instr, ByteData bd, ConstantPool cp, int offset) {
        int curoffset = offset + 1;
        int overflow = curoffset % 4;
        overflow = overflow > 0 ? 4 - overflow : 0;
        int startdata = 1 + overflow;
        int lowvalue = bd.getS4At(startdata + 4);
        int highvalue = bd.getS4At(startdata + 8);
        int numoffsets = highvalue - lowvalue + 1;
        int size = overflow + 12 + 4 * numoffsets;
        byte[] rawData = bd.getBytesAt(size, 1);
        DecodedTableSwitch dts = new DecodedTableSwitch(rawData, offset);
        int defaultTarget = dts.getDefaultTarget();
        List<DecodedSwitchEntry> targets = dts.getJumpTargets();
        int[] targetOffsets = new int[targets.size() + 1];
        targetOffsets[0] = defaultTarget;
        int out = 1;
        for (DecodedSwitchEntry target : targets) {
            targetOffsets[out++] = target.getBytecodeTarget();
        }
        return new Op01WithProcessedDataAndByteJumps(instr, rawData, targetOffsets, offset);
    }
}

