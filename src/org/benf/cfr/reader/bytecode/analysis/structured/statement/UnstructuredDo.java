/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredContinue;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredDo;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredWhile;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredDo
extends AbstractUnStructuredStatement {
    private BlockIdentifier blockIdentifier;

    public UnstructuredDo(BlockIdentifier blockIdentifier) {
        this.blockIdentifier = blockIdentifier;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("** do \n");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        Block block;
        UnstructuredWhile lastEndWhile;
        StructuredStatement inner;
        if (blockIdentifier != this.blockIdentifier) {
            throw new RuntimeException("Do statement claiming wrong block");
        }
        if ((lastEndWhile = innerBlock.removeLastEndWhile()) != null) {
            ConditionalExpression condition = lastEndWhile.getCondition();
            return new StructuredDo(condition, innerBlock, blockIdentifier);
        }
        if (!(inner = innerBlock.getStatement() instanceof Block)) {
            LinkedList blockContent = ListFactory.newLinkedList();
            blockContent.add((Op04StructuredStatement)new Op04StructuredStatement(inner));
            inner = new Block(blockContent, true);
            innerBlock.replaceContainedStatement(inner);
        }
        if ((block = (Block)inner).isJustOneStatement()) {
            Op04StructuredStatement singleStatement = block.getSingleStatement();
            StructuredStatement stm = singleStatement.getStatement();
            boolean canRemove = true;
            if (stm instanceof StructuredBreak) {
                StructuredBreak brk;
                if ((brk = (StructuredBreak)stm).getBreakBlock().equals(blockIdentifier)) {
                    canRemove = false;
                }
            } else if (stm instanceof StructuredContinue) {
                StructuredContinue cnt;
                if ((cnt = (StructuredContinue)stm).getContinueTgt().equals(blockIdentifier)) {
                    canRemove = false;
                }
            } else {
                canRemove = false;
            }
            if (canRemove) {
                return stm;
            }
        }
        block.getBlockStatements().add(new Op04StructuredStatement(new StructuredBreak(blockIdentifier, true)));
        return new StructuredDo(null, innerBlock, blockIdentifier);
    }
}

