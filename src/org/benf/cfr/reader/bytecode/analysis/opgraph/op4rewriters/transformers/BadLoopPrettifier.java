/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredDo;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredWhile;

public class BadLoopPrettifier
implements StructuredStatementTransformer {
    public void transform(Op04StructuredStatement root) {
        StructuredScope structuredScope = new StructuredScope();
        root.transform(this, structuredScope);
    }

    public List<Op04StructuredStatement> getIfBlock(Op04StructuredStatement maybeBlock) {
        StructuredStatement bodyStatement = maybeBlock.getStatement();
        if (!(bodyStatement instanceof Block)) {
            return null;
        }
        Block block = (Block)bodyStatement;
        return block.getBlockStatements();
    }

    @Override
    public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
        Op04StructuredStatement statement1;
        List<Op04StructuredStatement> ifStatements;
        List<Op04StructuredStatement> statements;
        StructuredIf ifStatement;
        in.transformStructuredChildren(this, scope);
        if (!(in instanceof StructuredDo)) {
            return in;
        }
        StructuredDo structuredDo = (StructuredDo)in;
        BlockIdentifier blockIdent = structuredDo.getBlock();
        if (structuredDo.getCondition() != null) {
            return in;
        }
        if ((statements = this.getIfBlock(((StructuredDo)in).getBody())) == null || statements.isEmpty()) {
            return in;
        }
        if (!((statement1 = statements.get(0)).getStatement() instanceof StructuredIf)) {
            return in;
        }
        if ((ifStatement = (StructuredIf)statement1.getStatement()).hasElseBlock()) {
            return in;
        }
        if ((ifStatements = this.getIfBlock(ifStatement.getIfTaken())) == null || ifStatements.size() != 1) {
            return in;
        }
        Op04StructuredStatement exitStatement = ifStatements.get(0);
        StructuredStatement structuredExit = exitStatement.getStatement();
        boolean liftTestBody = false;
        if (structuredExit instanceof StructuredBreak) {
            StructuredBreak breakStatement;
            if (!(breakStatement = (StructuredBreak)structuredExit).getBreakBlock().equals(blockIdent)) {
                return in;
            }
        } else {
            Set<Op04StructuredStatement> fallthrough;
            if (!(structuredExit instanceof StructuredReturn)) return in;
            if (!(fallthrough = scope.getNextFallThrough(in)).isEmpty()) {
                return in;
            }
            liftTestBody = true;
        }
        statements.remove(0);
        ConditionalExpression condition = ifStatement.getConditionalExpression().getNegated().simplify();
        StructuredWhile structuredWhile = new StructuredWhile(condition, structuredDo.getBody(), blockIdent);
        if (!liftTestBody) {
            return structuredWhile;
        }
        Block lifted = Block.getBlockFor(false, structuredWhile, structuredExit);
        return lifted;
    }
}

