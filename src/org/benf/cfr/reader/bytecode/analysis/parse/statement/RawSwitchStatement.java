/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.SwitchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitch;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitchEntry;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class RawSwitchStatement
extends AbstractStatement {
    private Expression switchOn;
    private final DecodedSwitch switchData;

    public RawSwitchStatement(Expression switchOn, DecodedSwitch switchData) {
        this.switchOn = switchOn;
        this.switchData = switchData;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("switch (").dump(this.switchOn).print(") {\n");
        List<DecodedSwitchEntry> targets = this.switchData.getJumpTargets();
        int targetIdx = 1;
        for (DecodedSwitchEntry decodedSwitchEntry : targets) {
            String tgtLbl = this.getTargetStatement(targetIdx++).getContainer().getLabel();
            dumper.print(" case " + decodedSwitchEntry.getValue() + ": goto " + tgtLbl + ";\n");
        }
        dumper.print(" default: goto " + this.getTargetStatement(0).getContainer().getLabel() + ";\n");
        dumper.print("}\n");
        return dumper;
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        this.switchOn = this.switchOn.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.switchOn = expressionRewriter.rewriteExpression(this.switchOn, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.switchOn.collectUsedLValues(lValueUsageCollector);
    }

    public DecodedSwitch getSwitchData() {
        return this.switchData;
    }

    public Expression getSwitchOn() {
        return this.switchOn;
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        throw new RuntimeException("Can't convert a raw switch statement to a structured statement");
    }

    public SwitchStatement getSwitchStatement(BlockIdentifier blockIdentifier) {
        return new SwitchStatement(this.switchOn, blockIdentifier);
    }

    @Override
    public final boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        RawSwitchStatement other = (RawSwitchStatement)o;
        if (constraint.equivalent(this.switchOn, other.switchOn)) return true;
        return false;
    }

    @Override
    public boolean fallsToNext() {
        return false;
    }
}

