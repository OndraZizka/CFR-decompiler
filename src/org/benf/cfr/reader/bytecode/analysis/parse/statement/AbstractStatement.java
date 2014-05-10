/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.IfStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.CreationCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public abstract class AbstractStatement
implements Statement {
    private StatementContainer<Statement> container;

    @Override
    public void setContainer(StatementContainer<Statement> container) {
        if (container == null) {
            throw new ConfusedCFRException("Trying to setContainer null!");
        }
        this.container = container;
    }

    @Override
    public LValue getCreatedLValue() {
        return null;
    }

    @Override
    public void collectLValueAssignments(LValueAssignmentCollector<Statement> lValueAssigmentCollector) {
    }

    @Override
    public void collectObjectCreation(CreationCollector creationCollector) {
    }

    @Override
    public SSAIdentifiers<LValue> collectLocallyMutatedVariables(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        return new SSAIdentifiers();
    }

    @Override
    public StatementContainer<Statement> getContainer() {
        if (this.container != null) return this.container;
        throw new ConfusedCFRException("Null container!");
    }

    @Override
    public Expression getRValue() {
        throw new ConfusedCFRException("Not appropriate here.");
    }

    protected Statement getTargetStatement(int idx) {
        return this.container.getTargetStatement(idx);
    }

    @Override
    public boolean condenseWithNextConditional() {
        return false;
    }

    @Override
    public boolean condenseWithPriorIfStatement(IfStatement ifStatement) {
        return false;
    }

    @Override
    public boolean isCompound() {
        return false;
    }

    @Override
    public List<Statement> getCompoundParts() {
        throw new ConfusedCFRException("Should not be calling getCompoundParts on this statement");
    }

    public final String toString() {
        ToStringDumper d = new ToStringDumper();
        d.print(this.getClass().getSimpleName()).print(": ").dump(this);
        return d.toString();
    }

    @Override
    public boolean fallsToNext() {
        return true;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return true;
    }
}

