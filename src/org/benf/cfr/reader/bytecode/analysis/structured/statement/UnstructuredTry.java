/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredTry;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredTry
extends AbstractUnStructuredStatement {
    private final ExceptionGroup exceptionGroup;

    public UnstructuredTry(ExceptionGroup exceptionGroup) {
        this.exceptionGroup = exceptionGroup;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("** try " + this.exceptionGroup + " { \n");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }

    public StructuredStatement getEmptyTry() {
        return new StructuredTry(this.exceptionGroup, new Op04StructuredStatement(Block.getEmptyBlock(true)), this.exceptionGroup.getTryBlockIdentifier());
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        if (blockIdentifier != this.exceptionGroup.getTryBlockIdentifier()) return null;
        return new StructuredTry(this.exceptionGroup, innerBlock, blockIdentifier);
    }
}

