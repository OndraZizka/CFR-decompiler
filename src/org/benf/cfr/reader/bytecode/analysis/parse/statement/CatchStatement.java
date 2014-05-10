/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.CastAction;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class CatchStatement
extends AbstractStatement {
    private final List<ExceptionGroup.Entry> exceptions;
    private BlockIdentifier catchBlockIdent;
    private LValue catching;

    public CatchStatement(List<ExceptionGroup.Entry> exceptions, LValue catching) {
        this.exceptions = exceptions;
        this.catching = catching;
        if (exceptions.isEmpty()) return;
        JavaTypeInstance collapsedCatchType = CatchStatement.determineType(exceptions);
        InferredJavaType catchType = new InferredJavaType(collapsedCatchType, InferredJavaType.Source.EXCEPTION, true);
        this.catching.getInferredJavaType().chain(catchType);
    }

    public static JavaTypeInstance determineType(List<ExceptionGroup.Entry> exceptions) {
        InferredJavaType ijt = new InferredJavaType();
        ijt.chain(new InferredJavaType(exceptions.get(0).getCatchType(), InferredJavaType.Source.EXCEPTION));
        int len = exceptions.size();
        for (int x = 1; x < len; ++x) {
            ijt.chain(new InferredJavaType(exceptions.get(x).getCatchType(), InferredJavaType.Source.EXCEPTION));
        }
        if (!ijt.isClash()) return ijt.getJavaTypeInstance();
        ijt.collapseTypeClash();
        return ijt.getJavaTypeInstance();
    }

    public void removeCatchBlockFor(BlockIdentifier tryBlockIdent) {
        List<ExceptionGroup.Entry> toRemove = Functional.filter(this.exceptions, new Predicate<ExceptionGroup.Entry>(){

            @Override
            public boolean test(ExceptionGroup.Entry in) {
                return in.getTryBlockIdentifier().equals(tryBlockIdent);
            }
        });
        this.exceptions.removeAll(toRemove);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.print("catch ( " + this.exceptions + " ").dump(this.catching).print(" ) {\n");
    }

    public BlockIdentifier getCatchBlockIdent() {
        return this.catchBlockIdent;
    }

    public void setCatchBlockIdent(BlockIdentifier catchBlockIdent) {
        this.catchBlockIdent = catchBlockIdent;
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public void collectLValueAssignments(LValueAssignmentCollector<Statement> lValueAssigmentCollector) {
        if (!(this.catching instanceof LocalVariable)) return;
        lValueAssigmentCollector.collectLocalVariableAssignment((LocalVariable)this.catching, this.getContainer(), null);
    }

    @Override
    public LValue getCreatedLValue() {
        return this.catching;
    }

    public List<ExceptionGroup.Entry> getExceptions() {
        return this.exceptions;
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new UnstructuredCatch(this.exceptions, this.catchBlockIdent, this.catching);
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
        CatchStatement other = (CatchStatement)o;
        if (!constraint.equivalent(this.exceptions, other.exceptions)) {
            return false;
        }
        if (constraint.equivalent(this.catching, other.catching)) return true;
        return false;
    }

}

