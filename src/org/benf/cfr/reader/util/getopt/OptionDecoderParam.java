/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import org.benf.cfr.reader.util.functors.BinaryFunction;

public interface OptionDecoderParam<T, ARG>
extends BinaryFunction<String, ARG, T> {
    public String getRangeDescription();

    public String getDefaultValue();
}

