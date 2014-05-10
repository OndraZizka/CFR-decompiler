/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import java.util.List;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitchEntry;

public interface DecodedSwitch {
    public int getDefaultTarget();

    public List<DecodedSwitchEntry> getJumpTargets();
}

