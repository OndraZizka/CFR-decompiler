/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.GotoStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.Nop;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.DefaultEquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.FinallyCatchBody;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.Pair;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.Result;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;

public class FinallyGraphHelper {
    private final FinallyCatchBody finallyCatchBody;

    public FinallyGraphHelper(FinallyCatchBody finallyCatchBody) {
        this.finallyCatchBody = finallyCatchBody;
    }

    public FinallyCatchBody getFinallyCatchBody() {
        return this.finallyCatchBody;
    }

    private List<Op03SimpleStatement> filterFalseNegatives(List<Op03SimpleStatement> in, Set<Op03SimpleStatement> toRemove) {
        List res = ListFactory.newList();
        Iterator<Op03SimpleStatement> i$ = in.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement i = i$.next();
            block5 : while (i != null && i.getStatement() instanceof Nop) {
                switch (i.getTargets().size()) {
                    case 0: {
                        i = null;
                        continue block5;
                    }
                    case 1: {
                        if (toRemove != null) {
                            toRemove.add(i);
                        }
                        i = i.getTargets().get(0);
                        continue block5;
                    }
                }
                throw new IllegalStateException();
            }
            if (i == null) continue;
            res.add((Op03SimpleStatement)i);
        }
        return res;
    }

    public Result match(Op03SimpleStatement test) {
        Set<BlockIdentifier> minBlockSet = SetFactory.newOrderedSet(test.getBlockIdentifiers());
        Op03SimpleStatement finalThrowProxy = null;
        Op03SimpleStatement finalThrow = this.finallyCatchBody.getThrowOp();
        IdentityHashMap<Op03SimpleStatement, Op03SimpleStatement> matched = new IdentityHashMap<Op03SimpleStatement, Op03SimpleStatement>();
        Set toRemove = SetFactory.newOrderedSet();
        LinkedList pending = ListFactory.newLinkedList();
        if (this.finallyCatchBody.isEmpty()) {
            return new Result(toRemove, null, null);
        }
        Pair start = new Pair(test, this.finallyCatchBody.getCatchCodeStart());
        pending.add((Pair)start);
        matched.put(start.b, start.a);
        FinallyEquivalenceConstraint equivalenceConstraint = new FinallyEquivalenceConstraint(null);
        Set finalThrowProxySources = SetFactory.newOrderedSet();
        while (!pending.isEmpty()) {
            Pair p = (Pair)pending.removeFirst();
            Op03SimpleStatement a = p.a;
            Op03SimpleStatement b = p.b;
            Statement sa = a.getStatement();
            Statement sb = b.getStatement();
            sa.collectLValueAssignments(equivalenceConstraint);
            if (!sa.equivalentUnder(sb, equivalenceConstraint)) {
                return Result.FAIL;
            }
            List<Op03SimpleStatement> tgta = ListFactory.newList(a.getTargets());
            List<Op03SimpleStatement> tgtb = ListFactory.newList(b.getTargets());
            tgta = this.filterFalseNegatives(tgta, toRemove);
            if (tgta.size() != (tgtb = this.filterFalseNegatives(tgtb, null)).size() && tgta.size() != tgtb.size()) {
                return Result.FAIL;
            }
            toRemove.add((Op03SimpleStatement)a);
            int len = tgta.size();
            for (int x = 0; x < len; ++x) {
                Set<BlockIdentifier> newBlockIdentifiers;
                Op03SimpleStatement tgttestx = tgta.get(x);
                Op03SimpleStatement tgthayx = tgtb.get(x);
                Op03SimpleStatement tgttestx2 = Op03SimpleStatement.followNopGotoChain(tgttestx, false, false);
                Op03SimpleStatement tgthayx2 = Op03SimpleStatement.followNopGotoChain(tgthayx, false, false);
                Op03SimpleStatement finalyThrowProxy2 = Op03SimpleStatement.followNopGotoChain(finalThrowProxy, false, false);
                if (!(newBlockIdentifiers = tgttestx.getBlockIdentifiers()).containsAll(minBlockSet)) continue;
                if (tgthayx2 == finalThrow) {
                    if (finalThrowProxy != null && finalThrowProxy != tgttestx2 && finalyThrowProxy2 != tgttestx2) {
                        Statement s1 = tgttestx.getStatement();
                        Statement s2 = finalThrowProxy.getStatement();
                        if (!(s1.getClass() == GotoStatement.class && s1.equals(s2))) {
                            return Result.FAIL;
                        }
                        boolean y = true;
                    }
                    if (finalThrowProxy == null) {
                        finalThrowProxy = tgttestx;
                    }
                    finalThrowProxySources.add((Op03SimpleStatement)a);
                }
                if (matched.containsKey(tgthayx) || !this.finallyCatchBody.contains(tgthayx)) continue;
                matched.put(tgthayx, tgttestx);
                pending.add((Pair)new Pair(tgttestx, tgthayx));
            }
        }
        return new Result(toRemove, test, Op03SimpleStatement.followNopGotoChain(finalThrowProxy, false, false));
    }

    class 1 {
    }

    class FinallyEquivalenceConstraint
    extends DefaultEquivalenceConstraint
    implements LValueAssignmentCollector<Statement> {
        private final Map<StackSSALabel, StackSSALabel> rhsToLhsMap;
        private final Map<LocalVariable, LocalVariable> rhsToLhsLVMap;
        private final Set<StackSSALabel> validSSA;
        private final Set<LocalVariable> validLocal;

        private FinallyEquivalenceConstraint() {
            this.rhsToLhsMap = MapFactory.newMap();
            this.rhsToLhsLVMap = MapFactory.newMap();
            this.validSSA = SetFactory.newSet();
            this.validLocal = SetFactory.newSet();
        }

        private StackSSALabel mapSSALabel(StackSSALabel s1, StackSSALabel s2) {
            StackSSALabel r1 = this.rhsToLhsMap.get(s2);
            if (r1 != null) {
                return r1;
            }
            this.rhsToLhsMap.put(s2, s1);
            return s1;
        }

        private LocalVariable mapLocalVariable(LocalVariable s1, LocalVariable s2) {
            LocalVariable r1 = this.rhsToLhsLVMap.get(s2);
            if (r1 != null) {
                return r1;
            }
            this.rhsToLhsLVMap.put(s2, s1);
            return s1;
        }

        @Override
        public boolean equivalent(Object o1, Object o2) {
            boolean x;
            if (o1 == null) {
                return o2 == null;
            }
            if (o1 instanceof Collection && o2 instanceof Collection) {
                return this.equivalent((Collection)o1, (Collection)o2);
            }
            if (o1 instanceof StackSSALabel && o2 instanceof StackSSALabel) {
                if (this.validSSA.contains(o1)) {
                    o2 = this.mapSSALabel((StackSSALabel)o1, (StackSSALabel)o2);
                } else {
                    x = true;
                }
            }
            if (o1 instanceof LocalVariable && o2 instanceof LocalVariable) {
                if (this.validLocal.contains(o1)) {
                    o2 = this.mapLocalVariable((LocalVariable)o1, (LocalVariable)o2);
                } else {
                    x = true;
                }
            }
            if (!(o1 instanceof ExceptionTableEntry) || !(o2 instanceof ExceptionTableEntry)) return super.equivalent(o1, o2);
            return true;
        }

        @Override
        public void collect(StackSSALabel lValue, StatementContainer<Statement> statementContainer, Expression value) {
            this.validSSA.add(lValue);
        }

        @Override
        public void collectMultiUse(StackSSALabel lValue, StatementContainer<Statement> statementContainer, Expression value) {
            this.validSSA.add(lValue);
        }

        @Override
        public void collectMutatedLValue(LValue lValue, StatementContainer<Statement> statementContainer, Expression value) {
            boolean x = true;
        }

        @Override
        public void collectLocalVariableAssignment(LocalVariable localVariable, StatementContainer<Statement> statementContainer, Expression value) {
            this.validLocal.add(localVariable);
        }

        /* synthetic */ FinallyEquivalenceConstraint(FinallyGraphHelper x0, 1 x1) {
            this();
        }
    }

}

