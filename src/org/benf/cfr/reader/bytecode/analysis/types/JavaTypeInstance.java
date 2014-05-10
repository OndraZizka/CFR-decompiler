/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.output.Dumper;

public interface JavaTypeInstance {
    public StackType getStackType();

    public boolean isComplexType();

    public boolean isUsableType();

    public RawJavaType getRawTypeOfSimpleType();

    public JavaTypeInstance removeAnArrayIndirection();

    public JavaTypeInstance getArrayStrippedType();

    public JavaTypeInstance getDeGenerifiedType();

    public int getNumArrayDimensions();

    public String getRawName();

    public InnerClassInfo getInnerClassHereInfo();

    public BindingSuperContainer getBindingSupers();

    public boolean implicitlyCastsTo(JavaTypeInstance var1, GenericTypeBinder var2);

    public boolean canCastTo(JavaTypeInstance var1, GenericTypeBinder var2);

    public String suggestVarName();

    public void dumpInto(Dumper var1, TypeUsageInformation var2);

    public void collectInto(TypeUsageCollector var1);
}

