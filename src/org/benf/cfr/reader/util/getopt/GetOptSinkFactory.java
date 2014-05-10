/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public interface GetOptSinkFactory<T>
extends PermittedOptionProvider {
    public T create(List<String> var1, Map<String, String> var2);
}

