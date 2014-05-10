/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDeltaImpl;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryCPEntry;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLiteral;
import org.benf.cfr.reader.util.ConfusedCFRException;

public class OperationFactoryLDC
extends OperationFactoryCPEntry {
    @Override
    public StackDelta getStackDelta(JVMInstr instr, byte[] data, ConstantPoolEntry[] cpEntries, StackSim stackSim, Method method) {
        StackType stackType;
        int requiredComputationCategory;
        ConstantPoolEntryLiteral constantPoolEntryLiteral = (ConstantPoolEntryLiteral)cpEntries[0];
        if (constantPoolEntryLiteral == null) {
            throw new ConfusedCFRException("Expecting ConstantPoolEntryLiteral");
        }
        if ((stackType = constantPoolEntryLiteral.getStackType()).getComputationCategory() == (requiredComputationCategory = 1)) return new StackDeltaImpl(StackTypes.EMPTY, stackType.asList());
        throw new ConfusedCFRException("Got a literal, but expected a different category");
    }
}

