/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CatchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.TryStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.DefaultEquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class FinallyHelper {
    private final Op03SimpleStatement finallyStart;
    private final List<Op03SimpleStatement> inFinallyBlock;
    private final BlockIdentifier finallyIdent;
    private final Op03SimpleStatement finalThrow;
    private final Op03SimpleStatement lastInFinally;
    private final Map<Op03SimpleStatement, Result> cachedResults = MapFactory.newMap();
    private final Op03SimpleStatement guessedFinalCatchBlock;

    public FinallyHelper(Op03SimpleStatement finallyStart, List<Op03SimpleStatement> inFinallyBlock, BlockIdentifier finallyIdent, Op03SimpleStatement finalThrow, Op03SimpleStatement lastInfinally, Op03SimpleStatement guessedFinallyCatchBlock) {
        this.finallyStart = finallyStart;
        this.inFinallyBlock = inFinallyBlock;
        this.finallyIdent = finallyIdent;
        this.finalThrow = finalThrow;
        this.lastInFinally = lastInfinally;
        this.guessedFinalCatchBlock = guessedFinallyCatchBlock;
    }

    public void markAsTestedFrom(Op03SimpleStatement possibleFinallyBlock, Op03SimpleStatement source) {
    }

    public Op03SimpleStatement getLastInFinally() {
        return this.lastInFinally;
    }

    public List<Op03SimpleStatement> getInFinallyBlock() {
        return this.inFinallyBlock;
    }

    public boolean hasFinalThrow() {
        return this.finalThrow != null;
    }

    public Result testEquivalent(Op03SimpleStatement possibleFinallyBlock, TryStatement tryStatement) {
        Result cachedResult = this.cachedResults.get(possibleFinallyBlock);
        if (cachedResult != null) {
            return cachedResult;
        }
        FinallyGraphHelper finallyGraphHelper = new FinallyGraphHelper(new Pair(this.finallyStart, possibleFinallyBlock), this.finallyIdent, this.finalThrow, tryStatement);
        Result res = finallyGraphHelper.match();
        this.cachedResults.put(possibleFinallyBlock, res);
        return res;
    }

    public void linkPeerTries(Set<Op03SimpleStatement> tryStarts, List<Op03SimpleStatement> allStatements) {
        Set tryBlocks = SetFactory.newSet(Functional.map(tryStarts, new UnaryFunction<Op03SimpleStatement, BlockIdentifier>(){

            @Override
            public BlockIdentifier invoke(Op03SimpleStatement arg) {
                return ((TryStatement)arg.getStatement()).getBlockIdentifier();
            }
        }));
        for (Op03SimpleStatement tri : tryStarts) {
            TryStatement thisTry = (TryStatement)tri.getStatement();
            List<Op03SimpleStatement> sources = tri.getSources();
            BlockIdentifier thisblock = thisTry.getBlockIdentifier();
            if (sources.size() != 1) continue;
            Set<BlockIdentifier> callerBlocks = sources.get(0).getBlockIdentifiers();
            Set wanted = SetFactory.newSet(tryBlocks);
            wanted.remove(thisblock);
            wanted.retainAll(callerBlocks);
            if (wanted.size() != 1) continue;
            BlockIdentifier aggregateWith = (BlockIdentifier)wanted.iterator().next();
            Op03SimpleStatement thisTryStm = (Op03SimpleStatement)thisTry.getContainer();
            List<Op03SimpleStatement> thisTriTargets = ListFactory.newList(thisTryStm.getTargets());
            int len = thisTriTargets.size();
            for (x = 1; x < len; ++x) {
                Op03SimpleStatement tgt = thisTriTargets.get(x);
                thisTryStm.removeTarget(tgt);
                tgt.removeSource(thisTryStm);
                Statement shouldBeCatch = tgt.getStatement();
                if (!(shouldBeCatch instanceof CatchStatement)) continue;
                CatchStatement catchStatement = (CatchStatement)shouldBeCatch;
                catchStatement.removeCatchBlockFor(thisblock);
            }
            thisTry.getContainer().nopOut();
            for (Op03SimpleStatement stm : allStatements) {
                if (!stm.getBlockIdentifiers().remove(thisblock)) continue;
                stm.getBlockIdentifiers().add(aggregateWith);
            }
            int x = 1;
        }
    }

    public void unlinkTries(Set<Op03SimpleStatement> wasTries) {
        Iterator<Op03SimpleStatement> i$ = wasTries.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement wasTry;
            Statement statement;
            if (!(statement = (wasTry = i$.next()).getStatement() instanceof TryStatement)) continue;
            wasTry.removeTarget(this.guessedFinalCatchBlock);
            this.guessedFinalCatchBlock.removeSource(wasTry);
        }
    }

    public static class Result {
        private final boolean matched;
        private final Op03SimpleStatement startOfFinallyCopy;
        private final Op03SimpleStatement finalThrowRedirect;
        private final Set<Op03SimpleStatement> toRemove;
        private final Set<Op03SimpleStatement> finalThrowProxySources;
        private final TryStatement tryStatement;
        public static Result FAIL = new Result();

        public Result(boolean matched, Op03SimpleStatement startOfFinallyCopy, Op03SimpleStatement finalThrowRedirect, Set<Op03SimpleStatement> finalThrowProxySources, Set<Op03SimpleStatement> toRemove, TryStatement tryStatement) {
            this.matched = matched;
            this.startOfFinallyCopy = startOfFinallyCopy;
            this.finalThrowRedirect = finalThrowRedirect;
            this.toRemove = toRemove;
            this.finalThrowProxySources = finalThrowProxySources;
            this.tryStatement = tryStatement;
        }

        private Result() {
            this.matched = false;
            this.finalThrowRedirect = null;
            this.toRemove = null;
            this.startOfFinallyCopy = null;
            this.finalThrowProxySources = null;
            this.tryStatement = null;
        }

        public boolean isMatched() {
            return this.matched;
        }

        public Op03SimpleStatement getFinalThrowRedirect() {
            return this.finalThrowRedirect;
        }

        public Set<Op03SimpleStatement> getToRemove() {
            return this.toRemove;
        }

        public Op03SimpleStatement getStartOfFinallyCopy() {
            return this.startOfFinallyCopy;
        }

        public Set<Op03SimpleStatement> getFinalThrowProxySources() {
            return this.finalThrowProxySources;
        }

        public TryStatement getTryStatement() {
            return this.tryStatement;
        }
    }

    static class FinallyGraphHelper {
        private final Pair start;
        private final BlockIdentifier ident;
        private final Op03SimpleStatement finalThrow;
        private final TryStatement tryStatement;
        private final EquivalenceConstraint equivalenceConstraint;
        private final Map<StackSSALabel, StackSSALabel> rhsToLhsMap;

        public FinallyGraphHelper(Pair start, BlockIdentifier ident, Op03SimpleStatement finalThrow, TryStatement tryStatement) {
            this.equivalenceConstraint = new FinallyEquivalenceConstraint(null);
            this.rhsToLhsMap = MapFactory.newMap();
            this.start = start;
            this.ident = ident;
            this.finalThrow = finalThrow;
            this.tryStatement = tryStatement;
        }

        public Result match() {
            Op03SimpleStatement finalThrowProxy = null;
            IdentityHashMap<Op03SimpleStatement, Op03SimpleStatement> matched = new IdentityHashMap<Op03SimpleStatement, Op03SimpleStatement>();
            Set toRemove = SetFactory.newSet();
            LinkedList pending = ListFactory.newLinkedList();
            pending.add((Pair)this.start);
            matched.put(this.start.a, this.start.b);
            Set finalThrowProxySources = SetFactory.newSet();
            while (!pending.isEmpty()) {
                Statement sb;
                List<Op03SimpleStatement> tgtb;
                List<Op03SimpleStatement> tgta;
                Op03SimpleStatement b;
                Statement sa;
                Op03SimpleStatement a;
                Pair p = (Pair)pending.removeFirst();
                if (!(sa = (a = p.a).getStatement()).equivalentUnder(sb = (b = p.b).getStatement(), this.equivalenceConstraint)) {
                    return Result.FAIL;
                }
                if ((tgta = a.getTargets()).size() != (tgtb = b.getTargets()).size()) {
                    return Result.FAIL;
                }
                toRemove.add((Op03SimpleStatement)b);
                int len = tgta.size();
                for (int x = 0; x < len; ++x) {
                    Op03SimpleStatement tgtax;
                    if ((tgtax = tgta.get(x)).getBlockIdentifiers().contains(this.ident) && tgtax != this.finalThrow && !matched.containsKey(tgtax)) {
                        Op03SimpleStatement tgtbx = tgtb.get(x);
                        Pair next = new Pair(tgtax, tgtbx);
                        pending.add((Pair)next);
                        matched.put(tgtax, tgtbx);
                    }
                    if (tgtax != this.finalThrow) continue;
                    finalThrowProxySources.add((Op03SimpleStatement)b);
                    finalThrowProxy = tgtb.get(x);
                }
            }
            return new Result(true, this.start.b, finalThrowProxy, finalThrowProxySources, toRemove, this.tryStatement);
        }

        private StackSSALabel mapSSALabel(StackSSALabel s1, StackSSALabel s2) {
            StackSSALabel r1 = this.rhsToLhsMap.get(s2);
            if (r1 != null) {
                return r1;
            }
            this.rhsToLhsMap.put(s2, s1);
            return s1;
        }

        class FinallyEquivalenceConstraint
        extends DefaultEquivalenceConstraint {
            private FinallyEquivalenceConstraint() {
            }

            @Override
            public boolean equivalent(Object o1, Object o2) {
                if (o1 == null) {
                    return o2 == null;
                }
                if (!(o1 instanceof StackSSALabel) || !(o2 instanceof StackSSALabel)) return super.equivalent(o1, o2);
                o2 = this$0.mapSSALabel((StackSSALabel)o1, (StackSSALabel)o2);
                return super.equivalent(o1, o2);
            }

            /* synthetic */ FinallyEquivalenceConstraint(FinallyGraphHelper x0,  x1) {
                this();
            }
        }

    }

    static class Pair {
        public final Op03SimpleStatement a;
        public final Op03SimpleStatement b;

        public Pair(Op03SimpleStatement a, Op03SimpleStatement b) {
            this.a = a;
            this.b = b;
        }
    }

}

