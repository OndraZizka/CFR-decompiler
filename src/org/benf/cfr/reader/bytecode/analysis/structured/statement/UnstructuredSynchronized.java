/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredSynchronized;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredSynchronized
extends AbstractUnStructuredStatement {
    private Expression monitor;
    private BlockIdentifier blockIdentifier;

    public UnstructuredSynchronized(Expression monitor, BlockIdentifier blockIdentifier) {
        this.monitor = monitor;
        this.blockIdentifier = blockIdentifier;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.monitor.collectTypeUsages(collector);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("** synchronized (").dump(this.monitor).print(")\n");
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        if (blockIdentifier == this.blockIdentifier) return new StructuredSynchronized(this.monitor, blockIdentifier, innerBlock);
        throw new RuntimeException("MONITOREXIT statement claiming wrong block");
    }
}

