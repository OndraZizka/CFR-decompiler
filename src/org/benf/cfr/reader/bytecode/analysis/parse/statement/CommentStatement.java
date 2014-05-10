/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class CommentStatement
extends AbstractStatement {
    private final Expression text;

    public CommentStatement(String text) {
        this.text = new Literal(TypedLiteral.getString(text));
    }

    public CommentStatement(Expression expression) {
        this.text = expression;
    }

    public CommentStatement(Statement statement) {
        this.text = new StatementExpression(statement, null);
    }

    @Override
    public Dumper dump(Dumper dumper) {
        return dumper.dump(this.text);
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        this.text.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.text.applyExpressionRewriter(expressionRewriter, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.text.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        return new StructuredComment(this.text);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && this.getClass() == o.getClass()) return true;
        return false;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (this == o) {
            return true;
        }
        if (o != null && this.getClass() == o.getClass()) return true;
        return false;
    }

    class 1 {
    }

    static class StatementExpression
    extends AbstractExpression {
        private Statement statement;
        private static InferredJavaType javaType = new InferredJavaType(RawJavaType.VOID, InferredJavaType.Source.EXPRESSION);

        private StatementExpression(Statement statement) {
            super(StatementExpression.javaType);
            this.statement = statement;
        }

        @Override
        public boolean equals(Object o) {
            return false;
        }

        @Override
        public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
            this.statement.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers);
            return this;
        }

        @Override
        public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            this.statement.rewriteExpressions(expressionRewriter, ssaIdentifiers);
            return this;
        }

        @Override
        public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
            this.statement.collectLValueUsage(lValueUsageCollector);
        }

        @Override
        public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
            return false;
        }

        @Override
        public Expression deepClone(CloneHelper cloneHelper) {
            return this;
        }

        @Override
        public Precedence getPrecedence() {
            return Precedence.WEAKEST;
        }

        @Override
        public Dumper dumpInner(Dumper d) {
            return d.dump(this.statement);
        }

        /* synthetic */ StatementExpression(Statement x0, 1 x1) {
            this(x0);
        }
    }

}

