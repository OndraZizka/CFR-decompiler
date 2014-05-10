/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;

public interface StatementContainer<T> {
    public T getStatement();

    public T getTargetStatement(int var1);

    public String getLabel();

    public InstrIndex getIndex();

    public void nopOut();

    public void replaceStatement(T var1);

    public void nopOutConditional();

    public SSAIdentifiers getSSAIdentifiers();

    public Set<BlockIdentifier> getBlockIdentifiers();

    public BlockIdentifier getBlockStarted();

    public Set<BlockIdentifier> getBlocksEnded();

    public void copyBlockInformationFrom(StatementContainer<T> var1);
}

