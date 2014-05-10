/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Collection;
import java.util.List;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCase;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredCase
extends AbstractUnStructuredStatement {
    private final List<Expression> values;
    private final BlockIdentifier blockIdentifier;
    private final InferredJavaType caseType;

    public UnstructuredCase(List<Expression> values, InferredJavaType caseType, BlockIdentifier blockIdentifier) {
        this.values = values;
        this.caseType = caseType;
        this.blockIdentifier = blockIdentifier;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.values.isEmpty()) {
            dumper.print("** default:\n");
        } else {
            for (Expression value : this.values) {
                dumper.print("** case ").dump(value).print(":\n");
            }
        }
        return dumper;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.values);
        collector.collect(this.caseType.getJavaTypeInstance());
    }

    public StructuredStatement getEmptyStructuredCase() {
        Op04StructuredStatement container = this.getContainer();
        return new StructuredCase(this.values, this.caseType, new Op04StructuredStatement(container.getIndex().justAfter(), container.getBlockMembership(), Block.getEmptyBlock()), this.blockIdentifier);
    }

    @Override
    public StructuredStatement claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier blockIdentifier, Vector<BlockIdentifier> blocksCurrentlyIn) {
        if (blockIdentifier == this.blockIdentifier) return new StructuredCase(this.values, this.caseType, innerBlock, blockIdentifier);
        throw new ConfusedCFRException("Unstructured case being asked to claim wrong block. [" + blockIdentifier + " != " + this.blockIdentifier + "]");
    }
}

