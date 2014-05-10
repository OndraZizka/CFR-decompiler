/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;

public interface JavaGenericBaseInstance
extends JavaTypeInstance {
    public JavaTypeInstance getBoundInstance(GenericTypeBinder var1);

    public boolean tryFindBinding(JavaTypeInstance var1, GenericTypeBinder var2);

    public boolean hasUnbound();

    public boolean hasForeignUnbound(ConstantPool var1);

    public List<JavaTypeInstance> getGenericTypes();
}

