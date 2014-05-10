/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredCase;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class CaseStatement
extends AbstractStatement {
    private List<Expression> values;
    private final BlockIdentifier switchBlock;
    private final BlockIdentifier caseBlock;
    private final InferredJavaType caseType;

    public CaseStatement(List<Expression> values, InferredJavaType caseType, BlockIdentifier switchBlock, BlockIdentifier caseBlock) {
        this.values = values;
        this.caseType = caseType;
        this.switchBlock = switchBlock;
        this.caseBlock = caseBlock;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.values.isEmpty()) {
            dumper.print("default:\n");
        } else {
            for (Expression value : this.values) {
                dumper.print("case ").dump(value).print(":\n");
            }
        }
        return dumper;
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        for (int x = 0; x < this.values.size(); ++x) {
            this.values.set(x, this.values.get(x).replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer()));
        }
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        for (int x = 0; x < this.values.size(); ++x) {
            this.values.set(x, expressionRewriter.rewriteExpression(this.values.get(x), ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE));
        }
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
    }

    public BlockIdentifier getSwitchBlock() {
        return this.switchBlock;
    }

    public boolean isDefault() {
        return this.values.isEmpty();
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new UnstructuredCase(this.values, this.caseType, this.caseBlock);
    }

    public BlockIdentifier getCaseBlock() {
        return this.caseBlock;
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
        CaseStatement other = (CaseStatement)o;
        if (constraint.equivalent(this.values, other.values)) return true;
        return false;
    }
}

