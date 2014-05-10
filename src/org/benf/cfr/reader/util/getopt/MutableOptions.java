/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import java.util.Map;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Troolean;
import org.benf.cfr.reader.util.getopt.OptionDecoderParam;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class MutableOptions
implements Options {
    private final Options delegate;
    private Map<String, String> overrides = MapFactory.newMap();

    public MutableOptions(Options delegate) {
        this.delegate = delegate;
    }

    public boolean override(PermittedOptionProvider.ArgumentParam<Troolean, Void> argument, Troolean value) {
        Troolean originalValue = this.delegate.getOption(argument);
        if (originalValue != Troolean.NEITHER) return false;
        this.overrides.put(argument.getName(), value.toString());
        return true;
    }

    public boolean override(PermittedOptionProvider.ArgumentParam<Boolean, Void> argument, boolean value) {
        Boolean originalValue = this.delegate.getOption(argument);
        if (originalValue == value) return false;
        this.overrides.put(argument.getName(), Boolean.toString(value));
        return true;
    }

    @Override
    public boolean optionIsSet(PermittedOptionProvider.ArgumentParam<?, ?> option) {
        if (!this.overrides.containsKey(option.getName())) return this.delegate.optionIsSet(option);
        return true;
    }

    @Override
    public String getFileName() {
        return this.delegate.getFileName();
    }

    @Override
    public String getMethodName() {
        return this.delegate.getMethodName();
    }

    @Override
    public <T> T getOption(PermittedOptionProvider.ArgumentParam<T, Void> option) {
        String override = this.overrides.get(option.getName());
        if (override == null) return this.delegate.getOption(option);
        return option.getFn().invoke((Object)override, (Object)null);
    }

    @Override
    public <T, A> T getOption(PermittedOptionProvider.ArgumentParam<T, A> option, A arg) {
        String override = this.overrides.get(option.getName());
        if (override == null) return this.delegate.getOption(option, arg);
        return option.getFn().invoke((Object)override, arg);
    }
}

