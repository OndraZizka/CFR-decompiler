/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.innerclass;

import com.sun.istack.internal.Nullable;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.AccessFlag;

public class InnerClassAttributeInfo {
    @Nullable
    private final JavaTypeInstance innerClassInfo;
    @Nullable
    private final JavaTypeInstance outerClassInfo;
    @Nullable
    private final String innerName;
    private final Set<AccessFlag> accessFlags;

    public InnerClassAttributeInfo(JavaTypeInstance innerClassInfo, JavaTypeInstance outerClassInfo, String innerName, Set<AccessFlag> accessFlags) {
        this.innerClassInfo = innerClassInfo;
        this.outerClassInfo = outerClassInfo;
        this.innerName = innerName;
        this.accessFlags = accessFlags;
    }

    public JavaTypeInstance getInnerClassInfo() {
        return this.innerClassInfo;
    }

    public JavaTypeInstance getOuterClassInfo() {
        return this.outerClassInfo;
    }

    public String getInnerName() {
        return this.innerName;
    }

    public Set<AccessFlag> getAccessFlags() {
        return this.accessFlags;
    }
}

