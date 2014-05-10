/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public interface Options {
    public String getFileName();

    public String getMethodName();

    public boolean optionIsSet(PermittedOptionProvider.ArgumentParam<?, ?> var1);

    public <T> T getOption(PermittedOptionProvider.ArgumentParam<T, Void> var1);

    public <T, A> T getOption(PermittedOptionProvider.ArgumentParam<T, A> var1, A var2);
}

