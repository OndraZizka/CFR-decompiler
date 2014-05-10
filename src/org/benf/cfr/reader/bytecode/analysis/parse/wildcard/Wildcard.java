/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.wildcard;

public interface Wildcard<X> {
    public X getMatch();

    public void resetMatch();
}

