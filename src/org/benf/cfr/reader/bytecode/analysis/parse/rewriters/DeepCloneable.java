/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.rewriters;

import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;

public interface DeepCloneable<X> {
    public X deepClone(CloneHelper var1);

    public X outerDeepClone(CloneHelper var1);
}

