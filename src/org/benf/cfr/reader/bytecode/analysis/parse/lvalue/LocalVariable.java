/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.lvalue;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.Ident;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariableDefault;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public class LocalVariable
extends AbstractLValue {
    private final NamedVariable name;
    private final int idx;
    private final Ident ident;
    private final boolean guessedFinal;

    public LocalVariable(int stackPosition, Ident ident, VariableNamer variableNamer, int originalRawOffset, InferredJavaType inferredJavaType, boolean guessedFinal) {
        super(inferredJavaType);
        this.name = variableNamer.getName(originalRawOffset, ident, stackPosition);
        this.idx = stackPosition;
        this.ident = ident;
        this.guessedFinal = guessedFinal;
    }

    public LocalVariable(String name, InferredJavaType inferredJavaType) {
        super(inferredJavaType);
        this.name = new NamedVariableDefault(name);
        this.idx = -1;
        this.ident = null;
        this.guessedFinal = false;
    }

    @Override
    public int getNumberOfCreators() {
        throw new ConfusedCFRException("NYI");
    }

    public boolean isGuessedFinal() {
        return this.guessedFinal;
    }

    @Override
    public LValue deepClone(CloneHelper cloneHelper) {
        return this;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.HIGHEST;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return this.name.dump(d).print(this.typeToString());
    }

    public NamedVariable getName() {
        return this.name;
    }

    public int getIdx() {
        return this.idx;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return false;
    }

    @Override
    public <T> void collectLValueAssignments(Expression assignedTo, StatementContainer<T> statementContainer, LValueAssignmentCollector<T> lValueAssigmentCollector) {
        lValueAssigmentCollector.collectLocalVariableAssignment(this, statementContainer, assignedTo);
    }

    @Override
    public LValue replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        return this;
    }

    @Override
    public SSAIdentifiers<LValue> collectVariableMutation(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        return new SSAIdentifiers<LValue>(this, ssaIdentifierFactory);
    }

    @Override
    public LValue applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocalVariable)) {
            return false;
        }
        LocalVariable that = (LocalVariable)o;
        if (!this.name.equals(that.name)) {
            return false;
        }
        if (this.idx != that.idx) {
            return false;
        }
        if (this.ident == null) {
            if (that.ident == null) return true;
            return false;
        }
        if (this.ident.equals(that.ident)) return true;
        return false;
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.idx;
        if (this.ident == null) return result;
        result = 31 * result + this.ident.hashCode();
        return result;
    }
}

