/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import org.benf.cfr.reader.bytecode.opcode.OperationFactoryLDCW;

public class OperationFactoryLDC2W
extends OperationFactoryLDCW {
    @Override
    protected int getRequiredComputationCategory() {
        return 2;
    }
}

