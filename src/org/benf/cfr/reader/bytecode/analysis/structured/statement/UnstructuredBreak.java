/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredBreak
extends AbstractUnStructuredStatement {
    private final Set<BlockIdentifier> blocksEnding;

    public UnstructuredBreak(Set<BlockIdentifier> blocksEnding) {
        this.blocksEnding = blocksEnding;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("** break;\n");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }

    @Override
    public StructuredStatement informBlockHeirachy(Vector<BlockIdentifier> blockIdentifiers) {
        BlockIdentifier outermostBreakable;
        int index = Integer.MAX_VALUE;
        BlockIdentifier bestBlock = null;
        Iterator<BlockIdentifier> i$ = this.blocksEnding.iterator();
        while (i$.hasNext()) {
            BlockIdentifier block;
            int posn;
            if ((posn = blockIdentifiers.indexOf(block = i$.next())) < 0 || index <= posn) continue;
            index = posn;
            bestBlock = block;
        }
        if (bestBlock == null) {
            return null;
        }
        boolean localBreak = false;
        if ((outermostBreakable = BlockIdentifier.getInnermostBreakable(blockIdentifiers)) == bestBlock) {
            localBreak = true;
        } else {
            bestBlock.addForeignRef();
        }
        return new StructuredBreak(bestBlock, localBreak);
    }
}

