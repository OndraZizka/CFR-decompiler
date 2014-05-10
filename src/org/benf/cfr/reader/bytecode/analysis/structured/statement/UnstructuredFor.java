/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFor;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredFor
extends AbstractUnStructuredStatement {
    private ConditionalExpression condition;
    private BlockIdentifier blockIdentifier;
    private AssignmentSimple initial;
    private AbstractAssignmentExpression assignment;

    public UnstructuredFor(ConditionalExpression condition, BlockIdentifier blockIdentifier, AssignmentSimple initial, AbstractAssignmentExpression assignment) {
        this.condition = condition;
        this.blockIdentifier = blockIdentifier;
        this.initial = initial;
        this.assignment = assignment;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom(this.condition);
        collector.collectFrom(this.assignment);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("** for (").dump(this.initial).print("; ").dump(this.condition).print("; ").dump(this.assignment).print(")\n");
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        if (blockIdentifier != this.blockIdentifier) {
            throw new RuntimeException("For statement claiming wrong block");
        }
        innerBlock.removeLastContinue(blockIdentifier);
        return new StructuredFor(this.condition, this.initial, this.assignment, innerBlock, blockIdentifier);
    }
}

