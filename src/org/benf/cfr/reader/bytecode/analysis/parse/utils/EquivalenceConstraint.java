/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.Collection;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;

public interface EquivalenceConstraint {
    public boolean equivalent(Object var1, Object var2);

    public boolean equivalent(Collection var1, Collection var2);

    public boolean equivalent(ComparableUnderEC var1, ComparableUnderEC var2);
}

