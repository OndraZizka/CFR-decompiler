/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.state.TypeUsageCollector;

public abstract class AbstractStructuredBlockStatement
extends AbstractStructuredStatement {
    private Op04StructuredStatement body;

    public AbstractStructuredBlockStatement(Op04StructuredStatement body) {
        this.body = body;
    }

    public Op04StructuredStatement getBody() {
        return this.body;
    }

    @Override
    public boolean isRecursivelyStructured() {
        return this.body.isFullyStructured();
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.body.collectTypeUsages(collector);
    }
}

