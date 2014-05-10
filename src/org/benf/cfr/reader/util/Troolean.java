/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

public enum Troolean {
    NEITHER,
    TRUE,
    FALSE;
    

    private Troolean() {
    }

    public static Troolean get(Boolean a) {
        if (a == null) {
            return Troolean.NEITHER;
        }
        return a.booleanValue() ? Troolean.TRUE : Troolean.FALSE;
    }
}

