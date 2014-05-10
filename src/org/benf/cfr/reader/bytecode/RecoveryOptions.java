/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.BytecodeMeta;
import org.benf.cfr.reader.bytecode.RecoveryOption;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.getopt.MutableOptions;
import org.benf.cfr.reader.util.getopt.Options;

public class RecoveryOptions {
    private final List<RecoveryOption<?>> recoveryOptions;

    public /* varargs */ RecoveryOptions(RecoveryOption<?> ... recoveryOptions) {
        this.recoveryOptions = ListFactory.newList(recoveryOptions);
    }

    public /* varargs */ RecoveryOptions(RecoveryOptions prev, RecoveryOption<?> ... recoveryOptions) {
        List recoveryOptionList = ListFactory.newList(recoveryOptions);
        this.recoveryOptions = ListFactory.newList();
        this.recoveryOptions.addAll(prev.recoveryOptions);
        this.recoveryOptions.addAll(recoveryOptionList);
    }

    public Applied apply(DCCommonState commonState, Options originalOptions, BytecodeMeta bytecodeMeta) {
        MutableOptions mutableOptions = new MutableOptions(originalOptions);
        List appliedComments = ListFactory.newList();
        boolean hadEffect = false;
        for (RecoveryOption option : this.recoveryOptions) {
            if (!option.apply(mutableOptions, appliedComments, bytecodeMeta)) continue;
            hadEffect = true;
        }
        return new Applied(mutableOptions, appliedComments, hadEffect);
    }

    public static class Applied {
        public Options options;
        public List<DecompilerComment> comments;
        public boolean valid;

        public Applied(Options options, List<DecompilerComment> comments, boolean valid) {
            this.options = options;
            this.comments = comments;
            this.valid = valid;
        }
    }

}

