/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractUnStructuredStatement;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;

public class UnstructuredGoto
extends AbstractUnStructuredStatement {
    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("** GOTO " + this.getContainer().getTargetLabel(0) + "\n");
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
    }
}

