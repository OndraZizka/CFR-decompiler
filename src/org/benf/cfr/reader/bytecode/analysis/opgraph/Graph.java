/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph;

import java.util.List;

public interface Graph<T> {
    public List<T> getSources();

    public List<T> getTargets();
}

