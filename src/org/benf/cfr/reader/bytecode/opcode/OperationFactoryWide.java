/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDefault;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class OperationFactoryWide
extends OperationFactoryDefault {
    private static JVMInstr getWideInstrVersion(JVMInstr instr) {
        switch (instr) {
            case IINC: {
                return JVMInstr.IINC_WIDE;
            }
            case ILOAD: {
                return JVMInstr.ILOAD_WIDE;
            }
            case FLOAD: {
                return JVMInstr.FLOAD_WIDE;
            }
            case ALOAD: {
                return JVMInstr.ALOAD_WIDE;
            }
            case LLOAD: {
                return JVMInstr.LLOAD_WIDE;
            }
            case DLOAD: {
                return JVMInstr.DLOAD_WIDE;
            }
            case ISTORE: {
                return JVMInstr.ISTORE_WIDE;
            }
            case FSTORE: {
                return JVMInstr.FSTORE_WIDE;
            }
            case ASTORE: {
                return JVMInstr.ASTORE_WIDE;
            }
            case LSTORE: {
                return JVMInstr.LSTORE_WIDE;
            }
            case DSTORE: {
                return JVMInstr.DSTORE_WIDE;
            }
            case RET: {
                return JVMInstr.RET_WIDE;
            }
        }
        throw new ConfusedCFRException("Wide is not defined for instr " + (Object)instr);
    }

    @Override
    public Op01WithProcessedDataAndByteJumps createOperation(JVMInstr instr, ByteData bd, ConstantPool cp, int offset) {
        JVMInstr widenedInstr = OperationFactoryWide.getWideInstrVersion(JVMInstr.find(bd.getS1At(1)));
        return widenedInstr.createOperation(bd, cp, offset);
    }

}

