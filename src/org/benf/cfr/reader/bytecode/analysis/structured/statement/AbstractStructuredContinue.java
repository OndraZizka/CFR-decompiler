/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;

public abstract class AbstractStructuredContinue
extends AbstractStructuredStatement {
    public abstract BlockIdentifier getContinueTgt();
}

