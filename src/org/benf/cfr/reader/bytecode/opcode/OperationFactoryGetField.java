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
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryCPEntryW;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryFieldRef;
import org.benf.cfr.reader.util.ConfusedCFRException;

public class OperationFactoryGetField
extends OperationFactoryCPEntryW {
    @Override
    public StackDelta getStackDelta(JVMInstr instr, byte[] data, ConstantPoolEntry[] cpEntries, StackSim stackSim, Method method) {
        ConstantPoolEntryFieldRef fieldRef = (ConstantPoolEntryFieldRef)cpEntries[0];
        if (fieldRef == null) {
            throw new ConfusedCFRException("Expecting fieldRef");
        }
        StackType stackType = fieldRef.getStackType();
        return new StackDeltaImpl(StackType.REF.asList(), stackType.asList());
    }
}

