/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredWhile;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredContinue;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredWhile
extends AbstractUnStructuredStatement {
    private ConditionalExpression condition;
    private BlockIdentifier blockIdentifier;
    private Set<BlockIdentifier> blocksEndedAfter;

    public UnstructuredWhile(ConditionalExpression condition, BlockIdentifier blockIdentifier, Set<BlockIdentifier> blocksEndedAfter) {
        this.condition = condition;
        this.blockIdentifier = blockIdentifier;
        this.blocksEndedAfter = blocksEndedAfter;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("** while (");
        if (this.condition == null) {
            dumper.print("true");
        } else {
            dumper.dump(this.condition);
        }
        return dumper.print(")\n");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.condition);
    }

    @Override
    public StructuredStatement informBlockHeirachy(Vector<BlockIdentifier> blockIdentifiers) {
        void res22;
        StructuredStatement resInform;
        UnstructuredContinue res22;
        switch (this.blockIdentifier.getBlockType()) {
            case DOLOOP: 
            case UNCONDITIONALDOLOOP: {
                break;
            }
            default: {
                return null;
            }
        }
        if (blockIdentifiers.isEmpty()) {
            return null;
        }
        if (this.blockIdentifier == blockIdentifiers.get(blockIdentifiers.size() - 1)) return null;
        if ((resInform = (res22 = new UnstructuredContinue(this.blockIdentifier)).informBlockHeirachy(blockIdentifiers)) != null) {
            StructuredStatement res22 = resInform;
        }
        if (this.condition == null) {
            return res22;
        }
        StructuredIf fakeIf = new StructuredIf(this.condition, new Op04StructuredStatement((StructuredStatement)res22));
        return fakeIf;
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        if (blockIdentifier != this.blockIdentifier) {
            throw new RuntimeException("While statement claiming wrong block");
        }
        innerBlock.removeLastContinue(blockIdentifier);
        StructuredWhile whileLoop = new StructuredWhile(this.condition, innerBlock, blockIdentifier);
        BlockIdentifier externalBreak = BlockIdentifier.getOutermostEnding(blocksCurrentlyIn, this.blocksEndedAfter);
        if (externalBreak == null) {
            return whileLoop;
        }
        LinkedList lst = ListFactory.newLinkedList();
        lst.add((Op04StructuredStatement)new Op04StructuredStatement(whileLoop));
        lst.add((Op04StructuredStatement)new Op04StructuredStatement(new StructuredBreak(externalBreak, false)));
        return new Block(lst, false);
    }

    public ConditionalExpression getCondition() {
        return this.condition;
    }

}

