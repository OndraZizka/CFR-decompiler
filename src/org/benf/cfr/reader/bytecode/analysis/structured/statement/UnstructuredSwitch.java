/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.List;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredSwitch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredCase;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredSwitch
extends AbstractUnStructuredStatement {
    private Expression switchOn;
    private final BlockIdentifier blockIdentifier;

    public UnstructuredSwitch(Expression switchOn, BlockIdentifier blockIdentifier) {
        this.switchOn = switchOn;
        this.blockIdentifier = blockIdentifier;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("** switch (").dump(this.switchOn).print(")\n");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.switchOn);
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        List<Op04StructuredStatement> statements;
        Op04StructuredStatement last;
        Block block;
        if (blockIdentifier != this.blockIdentifier) {
            throw new ConfusedCFRException("Unstructured switch being asked to claim wrong block. [" + blockIdentifier + " != " + this.blockIdentifier + "]");
        }
        if (!(innerBlock.getStatement() instanceof Block) || !((last = (statements = (block = (Block)innerBlock.getStatement()).getBlockStatements()).get((statements = (block = (Block)innerBlock.getStatement()).getBlockStatements()).size() - 1)).getStatement() instanceof UnstructuredCase)) return new StructuredSwitch(this.switchOn, innerBlock, blockIdentifier);
        UnstructuredCase caseStatement = (UnstructuredCase)last.getStatement();
        last.replaceContainedStatement(caseStatement.getEmptyStructuredCase());
        return new StructuredSwitch(this.switchOn, innerBlock, blockIdentifier);
    }
}

