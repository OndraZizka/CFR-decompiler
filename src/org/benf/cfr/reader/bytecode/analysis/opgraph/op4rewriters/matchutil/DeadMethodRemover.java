/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import java.util.Iterator;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;

public class DeadMethodRemover {
    public static void removeDeadMethod(ClassFile classFile, Method method) {
        Op04StructuredStatement code = method.getAnalysis();
        StructuredStatement statement = code.getStatement();
        if (!(statement instanceof Block)) {
            return;
        }
        Block block = (Block)statement;
        Iterator<Op04StructuredStatement> i$ = block.getBlockStatements().iterator();
        while (i$.hasNext()) {
            StructuredStatement innerStatement;
            Op04StructuredStatement inner;
            if (innerStatement = (inner = i$.next()).getStatement() instanceof StructuredComment) continue;
            return;
        }
        classFile.removePointlessMethod(method);
    }
}

