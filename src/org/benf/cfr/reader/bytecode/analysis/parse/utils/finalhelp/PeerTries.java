/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.TryStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.CompositeBlockIdentifierKey;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.FinallyGraphHelper;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class PeerTries {
    private final FinallyGraphHelper finallyGraphHelper;
    private final Op03SimpleStatement possibleFinallyCatch;
    private final Set<Op03SimpleStatement> seenEver = SetFactory.newOrderedSet();
    private final LinkedList<Op03SimpleStatement> toProcess = ListFactory.newLinkedList();
    private int nextIdx;
    private final Map<CompositeBlockIdentifierKey, PeerTrySet> triesByLevel;

    public PeerTries(FinallyGraphHelper finallyGraphHelper, Op03SimpleStatement possibleFinallyCatch) {
        this.triesByLevel = MapFactory.newLazyMap(new TreeMap(), new UnaryFunction<CompositeBlockIdentifierKey, PeerTrySet>(){

            @Override
            public PeerTrySet invoke(CompositeBlockIdentifierKey arg) {
                return new PeerTrySet(PeerTries.this.nextIdx++, null);
            }
        });
        this.finallyGraphHelper = finallyGraphHelper;
        this.possibleFinallyCatch = possibleFinallyCatch;
    }

    public Op03SimpleStatement getOriginalFinally() {
        return this.possibleFinallyCatch;
    }

    public void add(Op03SimpleStatement tryStatement) {
        if (!(tryStatement.getStatement() instanceof TryStatement)) {
            throw new IllegalStateException();
        }
        if (this.seenEver.contains(tryStatement)) {
            return;
        }
        this.toProcess.add(tryStatement);
        this.triesByLevel.get(new CompositeBlockIdentifierKey(tryStatement)).add(tryStatement);
    }

    public boolean hasNext() {
        return !this.toProcess.isEmpty();
    }

    public Op03SimpleStatement removeNext() {
        return this.toProcess.removeFirst();
    }

    public List<PeerTrySet> getPeerTryGroups() {
        return ListFactory.newList(this.triesByLevel.values());
    }

    public static final class PeerTrySet {
        private final Set<Op03SimpleStatement> content = SetFactory.newOrderedSet();
        private final int idx;

        private PeerTrySet(int idx) {
            this.idx = idx;
        }

        public void add(Op03SimpleStatement op) {
            this.content.add(op);
        }

        public Collection<Op03SimpleStatement> getPeerTries() {
            return this.content;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            PeerTrySet that = (PeerTrySet)o;
            if (this.idx == that.idx) return true;
            return false;
        }

        public int hashCode() {
            return this.idx;
        }

        /* synthetic */ PeerTrySet(int x0,  x1) {
            this(x0);
        }
    }

}

