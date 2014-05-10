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
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ConditionalUtils;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredGoto;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredIf
extends AbstractUnStructuredStatement {
    private ConditionalExpression conditionalExpression;
    private Op04StructuredStatement setIfBlock;
    private BlockIdentifier knownIfBlock;
    private BlockIdentifier knownElseBlock;

    public UnstructuredIf(ConditionalExpression conditionalExpression, BlockIdentifier knownIfBlock, BlockIdentifier knownElseBlock) {
        this.conditionalExpression = conditionalExpression;
        this.knownIfBlock = knownIfBlock;
        this.knownElseBlock = knownElseBlock;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.conditionalExpression);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("** if (").dump(this.conditionalExpression).print(") goto " + this.getContainer().getTargetLabel(1) + "\n");
        if (this.setIfBlock == null) return dumper;
        dumper.dump(this.setIfBlock);
        return dumper;
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        if (blockIdentifier == this.knownIfBlock) {
            if (this.knownElseBlock == null) {
                Op04StructuredStatement fakeElse = new Op04StructuredStatement(new UnstructuredGoto());
                Op04StructuredStatement fakeElseTarget = this.getContainer().getTargets().get(1);
                fakeElse.addTarget(fakeElseTarget);
                fakeElseTarget.addSource(fakeElse);
                LinkedList fakeBlockContent = ListFactory.newLinkedList();
                fakeBlockContent.add((Op04StructuredStatement)fakeElse);
                Op04StructuredStatement fakeElseBlock = new Op04StructuredStatement(new Block(fakeBlockContent, true));
                return new StructuredIf(ConditionalUtils.simplify(this.conditionalExpression.getNegated()), innerBlock, fakeElseBlock);
            }
            this.setIfBlock = innerBlock;
            return this;
        }
        if (blockIdentifier != this.knownElseBlock) return null;
        if (this.setIfBlock == null) {
            throw new ConfusedCFRException("Set else block before setting IF block");
        }
        if (this.knownIfBlock.getBlockType() == BlockType.SIMPLE_IF_TAKEN) {
            this.setIfBlock.removeLastGoto();
        }
        innerBlock = UnstructuredIf.unpackElseIfBlock(innerBlock);
        return new StructuredIf(ConditionalUtils.simplify(this.conditionalExpression.getNegated()), this.setIfBlock, innerBlock);
    }

    private static Op04StructuredStatement unpackElseIfBlock(Op04StructuredStatement elseBlock) {
        Block block;
        Op04StructuredStatement inner;
        StructuredStatement elseStmt = elseBlock.getStatement();
        if (!(elseStmt instanceof Block)) {
            return elseBlock;
        }
        if (!(block = (Block)elseStmt).isJustOneStatement()) {
            return elseBlock;
        }
        if (!((inner = block.getSingleStatement()).getStatement() instanceof StructuredIf)) return elseBlock;
        return inner;
    }
}

