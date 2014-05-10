/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.statement;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BoolOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NotOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AnonBreakTarget;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.GotoStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.WhileStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ConditionalUtils;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.JumpType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredAnonymousBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredContinue;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredIf;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class IfStatement
extends GotoStatement {
    private static final int JUMP_NOT_TAKEN = 0;
    private static final int JUMP_TAKEN = 1;
    private ConditionalExpression condition;
    private BlockIdentifier knownIfBlock = null;
    private BlockIdentifier knownElseBlock = null;

    public IfStatement(ConditionalExpression conditionalExpression) {
        this.condition = conditionalExpression;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("if (").dump(this.condition).print(") ");
        return super.dump(dumper);
    }

    @Override
    public void replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers) {
        Expression replacementCondition = this.condition.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, this.getContainer());
        if (replacementCondition == this.condition) return;
        this.condition = (ConditionalExpression)replacementCondition;
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers) {
        this.condition = expressionRewriter.rewriteExpression(this.condition, ssaIdentifiers, this.getContainer(), ExpressionRewriterFlags.RVALUE);
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.condition.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public boolean condenseWithNextConditional() {
        Statement nextStatement = this.getTargetStatement(0);
        return nextStatement.condenseWithPriorIfStatement(this);
    }

    public ConditionalExpression getCondition() {
        return this.condition;
    }

    public void setCondition(ConditionalExpression condition) {
        this.condition = condition;
    }

    public void simplifyCondition() {
        this.condition = ConditionalUtils.simplify(this.condition);
    }

    public void negateCondition() {
        this.condition = ConditionalUtils.simplify(this.condition.getNegated());
    }

    @Override
    public boolean condenseWithPriorIfStatement(IfStatement prior) {
        Statement target2;
        Statement fallThrough2 = this.getTargetStatement(0);
        Statement target1 = prior.getTargetStatement(1);
        if (fallThrough2 == target1) {
            this.condition = new BooleanOperation(new NotOperation(prior.getCondition()), this.getCondition(), BoolOp.AND).simplify();
            prior.getContainer().nopOutConditional();
            return true;
        }
        if (target1 != (target2 = this.getTargetStatement(1))) return false;
        this.condition = new BooleanOperation(prior.getCondition(), this.getCondition(), BoolOp.OR).simplify();
        prior.getContainer().nopOutConditional();
        return true;
    }

    public void replaceWithWhileLoopStart(BlockIdentifier blockIdentifier) {
        WhileStatement replacement = new WhileStatement(ConditionalUtils.simplify(this.condition.getNegated()), blockIdentifier);
        this.getContainer().replaceStatement(replacement);
    }

    public void replaceWithWhileLoopEnd(BlockIdentifier blockIdentifier) {
        WhileStatement replacement = new WhileStatement(ConditionalUtils.simplify(this.condition), blockIdentifier);
        this.getContainer().replaceStatement(replacement);
    }

    @Override
    public Statement getJumpTarget() {
        return this.getTargetStatement(1);
    }

    @Override
    public boolean isConditional() {
        return true;
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return this.condition.canThrow(caught);
    }

    @Override
    public StructuredStatement getStructuredStatement() {
        switch (this.getJumpType()) {
            case GOTO: 
            case GOTO_OUT_OF_IF: 
            case GOTO_OUT_OF_TRY: {
                return new UnstructuredIf(this.condition, this.knownIfBlock, this.knownElseBlock);
            }
            case CONTINUE: {
                return new StructuredIf(this.condition, new Op04StructuredStatement(new UnstructuredContinue(this.getTargetStartBlock())));
            }
            case BREAK: {
                return new StructuredIf(this.condition, new Op04StructuredStatement(new UnstructuredBreak(this.getJumpTarget().getContainer().getBlocksEnded())));
            }
            case BREAK_ANONYMOUS: {
                Statement target = this.getJumpTarget();
                if (!(target instanceof AnonBreakTarget)) {
                    throw new IllegalStateException("Target of anonymous break unexpected.");
                }
                AnonBreakTarget anonBreakTarget = (AnonBreakTarget)target;
                BlockIdentifier breakFrom = anonBreakTarget.getBlockIdentifier();
                Op04StructuredStatement unstructuredBreak = new Op04StructuredStatement(new UnstructuredAnonymousBreak(breakFrom));
                return new StructuredIf(this.condition, unstructuredBreak);
            }
        }
        throw new UnsupportedOperationException("Unexpected jump type in if block - " + (Object)this.getJumpType());
    }

    public void setKnownBlocks(BlockIdentifier ifBlock, BlockIdentifier elseBlock) {
        this.knownIfBlock = ifBlock;
        this.knownElseBlock = elseBlock;
    }

    public void optimiseForTypes() {
        this.condition = this.condition.optimiseForType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        IfStatement that = (IfStatement)o;
        if (!(this.condition != null ? !this.condition.equals(that.condition) : that.condition != null)) return true;
        return false;
    }

}

