/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.JumpType;

public abstract class JumpingStatement
extends AbstractStatement {
    public abstract Statement getJumpTarget();

    public abstract JumpType getJumpType();

    public abstract void setJumpType(JumpType var1);

    public abstract boolean isConditional();
}

