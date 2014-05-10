/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CatchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.Nop;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ThrowStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.SetFactory;

public class FinallyCatchBody {
    final Op03SimpleStatement throwOp;
    final boolean isEmpty;
    final Op03SimpleStatement catchCodeStart;
    final List<Op03SimpleStatement> body;
    final Set<Op03SimpleStatement> bodySet;

    protected FinallyCatchBody(Op03SimpleStatement throwOp, boolean isEmpty, Op03SimpleStatement catchCodeStart, List<Op03SimpleStatement> body) {
        this.throwOp = throwOp;
        this.isEmpty = isEmpty;
        this.catchCodeStart = catchCodeStart;
        this.body = body;
        this.bodySet = SetFactory.newOrderedSet(body);
    }

    public static FinallyCatchBody build(Op03SimpleStatement catchStart, List<Op03SimpleStatement> allStatements) {
        List<Op03SimpleStatement> targets = catchStart.getTargets();
        if (targets.size() != 1) {
            return null;
        }
        if (!(catchStart.getStatement() instanceof CatchStatement)) {
            return null;
        }
        CatchStatement catchStatement = (CatchStatement)catchStart.getStatement();
        BlockIdentifier catchBlockIdentifier = catchStatement.getCatchBlockIdent();
        LinkedList catchBody = ListFactory.newLinkedList();
        int len = allStatements.size();
        for (int idx = allStatements.indexOf((Object)catchStart) + 1; idx < len; ++idx) {
            Op03SimpleStatement stm = allStatements.get(idx);
            boolean isNop = stm.getStatement() instanceof Nop;
            boolean contained = stm.getBlockIdentifiers().contains(catchBlockIdentifier);
            if (!isNop && !contained) break;
            if (!contained) continue;
            catchBody.add((Op03SimpleStatement)stm);
        }
        if (catchBody.isEmpty()) {
            return new FinallyCatchBody(null, true, null, catchBody);
        }
        ThrowStatement testThrow = new ThrowStatement(new LValueExpression(catchStatement.getCreatedLValue()));
        boolean hasThrow = false;
        Op03SimpleStatement throwOp = null;
        if (!testThrow.equals(((Op03SimpleStatement)catchBody.getLast()).getStatement())) return new FinallyCatchBody(throwOp, false, targets.get(0), catchBody);
        hasThrow = true;
        throwOp = (Op03SimpleStatement)catchBody.removeLast();
        return new FinallyCatchBody(throwOp, false, targets.get(0), catchBody);
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public int getSize() {
        return this.body.size();
    }

    public Op03SimpleStatement getCatchCodeStart() {
        return this.catchCodeStart;
    }

    public Op03SimpleStatement getThrowOp() {
        return this.throwOp;
    }

    public boolean hasThrowOp() {
        return this.throwOp != null;
    }

    public boolean contains(Op03SimpleStatement stm) {
        return this.bodySet.contains(stm);
    }
}

