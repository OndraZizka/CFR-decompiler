/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util;

import com.sun.istack.internal.Nullable;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.util.ListFactory;

public class MiscStatementTools {
    public static List<Op04StructuredStatement> getBlockStatements(Op04StructuredStatement code) {
        StructuredStatement topCode = code.getStatement();
        if (!(topCode instanceof Block)) {
            return null;
        }
        Block block = (Block)topCode;
        List<Op04StructuredStatement> statements = block.getBlockStatements();
        return statements;
    }

    public static boolean isDeadCode(Op04StructuredStatement code) {
        List<Op04StructuredStatement> statements = MiscStatementTools.getBlockStatements(code);
        if (statements == null) {
            return false;
        }
        for (Op04StructuredStatement statement : statements) {
            if (statement.getStatement() instanceof StructuredComment) continue;
            return false;
        }
        return true;
    }

    @Nullable
    public static List<StructuredStatement> linearise(Op04StructuredStatement root) {
        List structuredStatements = ListFactory.newList();
        try {
            root.linearizeStatementsInto(structuredStatements);
        }
        catch (UnsupportedOperationException e) {
            return null;
        }
        return structuredStatements;
    }

    public static void applyExpressionRewriter(Op04StructuredStatement root, ExpressionRewriter expressionRewriter) {
        List<StructuredStatement> statements = MiscStatementTools.linearise(root);
        if (statements == null) {
            return;
        }
        for (StructuredStatement statement : statements) {
            statement.rewriteExpressions(expressionRewriter);
        }
    }
}

