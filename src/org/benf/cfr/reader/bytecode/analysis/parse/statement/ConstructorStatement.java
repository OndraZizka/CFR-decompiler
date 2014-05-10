/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.CreationCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.CastAction;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstructorStatement
extends AbstractStatement {
    private MemberFunctionInvokation invokation;

    public ConstructorStatement(MemberFunctionInvokation construction) {
        this.invokation = construction;
        Expression object = this.invokation.getObject();
        object.getInferredJavaType().chain(this.invokation.getInferredJavaType());
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("<init>").dump(this.invokation).print(";\n");
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        this.invokation.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.invokation.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.invokation.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public void collectObjectCreation(CreationCollector creationCollector) {
        Expression object = this.invokation.getObject();
        creationCollector.collectConstruction(object, this.invokation, this.getContainer());
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new StructuredExpressionStatement(this.invokation, false);
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
        ConstructorStatement other = (ConstructorStatement)o;
        if (constraint.equivalent(this.invokation, other.invokation)) return true;
        return false;
    }
}

