/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

public class QuotingUtils {
    public static String enquoteString(String s) {
        char[] raw = s.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\"");
        block9 : for (char c : raw) {
            switch (c) {
                case '\u000a': {
                    stringBuilder.append("\\n");
                    continue block9;
                }
                case '\u000d': {
                    stringBuilder.append("\\r");
                    continue block9;
                }
                case '\u0009': {
                    stringBuilder.append("\\t");
                    continue block9;
                }
                case '\u0008': {
                    stringBuilder.append("\\b");
                    continue block9;
                }
                case '\u000c': {
                    stringBuilder.append("\\f");
                    continue block9;
                }
                case '\\': {
                    stringBuilder.append("\\\\");
                    continue block9;
                }
                case '\"': {
                    stringBuilder.append("\\\"");
                    continue block9;
                }
                default: {
                    if (c < ' ' || c > '') {
                        stringBuilder.append("\\u").append(String.format("%04x", c));
                        continue block9;
                    }
                    stringBuilder.append(c);
                }
            }
        }
        stringBuilder.append("\"");
        return stringBuilder.toString();
    }

    public static String enquoteIdentifier(String s) {
        char[] raw = s.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : raw) {
            if (c < ' ' || c > '') {
                stringBuilder.append("\\u").append(String.format("%04x", c));
                continue;
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }

    public static String unquoteString(String s) {
        if (!s.startsWith("\"") || !s.endsWith("\"")) return s;
        s = s.substring(1, s.length() - 1);
        return s;
    }
}

