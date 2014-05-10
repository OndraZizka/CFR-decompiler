/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class BadParametersException
extends IllegalArgumentException {
    private final PermittedOptionProvider permittedOptionProvider;

    public BadParametersException(String s, PermittedOptionProvider permittedOptionProvider) {
        super(s);
        this.permittedOptionProvider = permittedOptionProvider;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getMessage()).append("\n\ncfr filename [ methname ]\n").append(GetOptParser.getHelp(this.permittedOptionProvider));
        return sb.toString();
    }
}

