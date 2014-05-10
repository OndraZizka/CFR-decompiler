/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.Collection;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;

public class SSAIdentifierUtils {
    public static boolean isMovableUnder(Collection<LValue> lValues, SSAIdentifiers atTarget, SSAIdentifiers atSource) {
        for (LValue lValue : lValues) {
            if (atTarget.isValidReplacement(lValue, atSource)) continue;
            return false;
        }
        return true;
    }
}

