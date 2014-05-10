/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.bootstrap;

import org.benf.cfr.reader.entities.bootstrap.MethodHandleBehaviour;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;

public class BootstrapMethodInfo {
    private final MethodHandleBehaviour methodHandleBehaviour;
    private final ConstantPoolEntryMethodRef constantPoolEntryMethodRef;
    private final ConstantPoolEntry[] bootstrapArguments;

    public BootstrapMethodInfo(ConstantPoolEntryMethodHandle methodHandle, ConstantPoolEntry[] bootstrapArguments, ConstantPool cp) {
        this.methodHandleBehaviour = methodHandle.getReferenceKind();
        if (this.methodHandleBehaviour != MethodHandleBehaviour.INVOKE_STATIC && this.methodHandleBehaviour != MethodHandleBehaviour.NEW_INVOKE_SPECIAL) {
            throw new IllegalArgumentException("Expected INVOKE_STATIC / NEWINVOKE_SPECIAL, got " + (Object)this.methodHandleBehaviour);
        }
        this.constantPoolEntryMethodRef = methodHandle.getMethodRef();
        this.bootstrapArguments = bootstrapArguments;
    }

    public ConstantPoolEntryMethodRef getConstantPoolEntryMethodRef() {
        return this.constantPoolEntryMethodRef;
    }

    public ConstantPoolEntry[] getBootstrapArguments() {
        return this.bootstrapArguments;
    }

    public MethodHandleBehaviour getMethodHandleBehaviour() {
        return this.methodHandleBehaviour;
    }
}

