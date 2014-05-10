/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredCatch
extends AbstractUnStructuredStatement {
    private final List<ExceptionGroup.Entry> exceptions;
    private final BlockIdentifier blockIdentifier;
    private final LValue catching;

    public UnstructuredCatch(List<ExceptionGroup.Entry> exceptions, BlockIdentifier blockIdentifier, LValue catching) {
        this.exceptions = exceptions;
        this.blockIdentifier = blockIdentifier;
        this.catching = catching;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("** catch " + this.exceptions + " { \n");
        return dumper;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        for (ExceptionGroup.Entry entry : this.exceptions) {
            collector.collect(entry.getCatchType());
        }
    }

    public StructuredStatement getCatchFor(Op04StructuredStatement innerBlock) {
        TreeMap catchTypes = MapFactory.newTreeMap();
        Set possibleTryBlocks = SetFactory.newSet();
        for (ExceptionGroup.Entry entry : this.exceptions) {
            JavaRefTypeInstance typ = entry.getCatchType();
            catchTypes.put((String)typ.getRawName(), (JavaRefTypeInstance)typ);
            possibleTryBlocks.add((BlockIdentifier)entry.getTryBlockIdentifier());
        }
        return new StructuredCatch(catchTypes.values(), innerBlock, this.catching, possibleTryBlocks);
    }

    public StructuredStatement getCatchForEmpty() {
        return this.getCatchFor(new Op04StructuredStatement(Block.getEmptyBlock(true)));
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        if (blockIdentifier != this.blockIdentifier) return null;
        return this.getCatchFor(innerBlock);
    }
}

