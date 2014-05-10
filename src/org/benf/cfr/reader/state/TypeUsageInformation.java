/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;

public interface TypeUsageInformation {
    public Set<JavaRefTypeInstance> getUsedClassTypes();

    public Set<JavaRefTypeInstance> getUsedInnerClassTypes();

    public String getName(JavaTypeInstance var1);

    public String generateInnerClassShortName(JavaRefTypeInstance var1);
}

