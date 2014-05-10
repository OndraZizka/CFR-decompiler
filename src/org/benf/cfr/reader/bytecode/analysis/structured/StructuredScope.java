/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.SetFactory;

public class StructuredScope {
    private final LinkedList<AtLevel> scope = ListFactory.newLinkedList();

    public void add(StructuredStatement statement) {
        this.scope.addFirst(new AtLevel(statement, null));
    }

    public void remove(StructuredStatement statement) {
        AtLevel old = this.scope.removeFirst();
        if (statement == old.statement) return;
        throw new IllegalStateException();
    }

    public StructuredStatement getInnermost() {
        if (!this.scope.isEmpty()) return this.scope.getFirst().statement;
        return null;
    }

    public void setNextAtThisLevel(StructuredStatement statement, int next) {
        AtLevel atLevel = this.scope.getFirst();
        if (atLevel.statement != statement) {
            throw new IllegalStateException();
        }
        atLevel.next = next;
    }

    public Set<Op04StructuredStatement> getNextFallThrough(StructuredStatement structuredStatement) {
        Op04StructuredStatement current = structuredStatement.getContainer();
        Set res = SetFactory.newSet();
        for (AtLevel atLevel : this.scope) {
            if (atLevel.statement instanceof Block) {
                if (atLevel.next != -1) {
                    res.addAll(((Block)atLevel.statement).getNextAfter(atLevel.next));
                }
                if (!((Block)atLevel.statement).statementIsLast(current)) return res;
                current = atLevel.statement.getContainer();
                continue;
            }
            if (!atLevel.statement.fallsNopToNext()) return res;
            current = atLevel.statement.getContainer();
        }
        return res;
    }

    public boolean statementIsLast(StructuredStatement statement) {
        AtLevel atLevel = this.scope.getFirst();
        boolean x = true;
        StructuredStatement s = atLevel.statement;
        if (s instanceof Block) {
            return ((Block)s).statementIsLast(statement.getContainer());
        }
        return statement == s;
    }

    class 1 {
    }

    public static class AtLevel {
        StructuredStatement statement;
        int next;

        private AtLevel(StructuredStatement statement) {
            this.statement = statement;
            this.next = 0;
        }

        public String toString() {
            return this.statement.toString();
        }

        /* synthetic */ AtLevel(StructuredStatement x0, 1 x1) {
            this(x0);
        }
    }

}

