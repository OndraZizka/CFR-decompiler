/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import java.util.Set;
import org.benf.cfr.reader.util.SetFactory;

public class Keywords {
    private static final Set<String> keywords = SetFactory.newSet("abstract", "continue", "for", "new", "switch", "assert", "default", "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while");

    public static boolean isAKeyword(String string) {
        return Keywords.keywords.contains(string);
    }
}

