/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.classfilehelpers;

import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumper;

public interface ClassFileDumper
extends TypeUsageCollectable {
    public Dumper dump(ClassFile var1, boolean var2, Dumper var3);

    @Override
    public void collectTypeUsages(TypeUsageCollector var1);
}

