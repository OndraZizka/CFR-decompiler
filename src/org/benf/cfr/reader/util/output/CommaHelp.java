/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.util.output.Dumper;

public class CommaHelp {
    public static boolean dot(boolean first, StringBuilder sb) {
        if (first) return false;
        sb.append(".");
        return false;
    }

    public static boolean comma(boolean first, StringBuilder sb) {
        if (first) return false;
        sb.append(", ");
        return false;
    }

    public static boolean comma(boolean first, Dumper d) {
        if (first) return false;
        d.print(", ");
        return false;
    }
}

