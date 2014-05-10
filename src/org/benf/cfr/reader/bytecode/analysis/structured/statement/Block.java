/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredContinue;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredDefinition;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFinally;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredTry;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredAnonBreakTarget;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredAnonymousBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredGoto;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredTry;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredWhile;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumper;

public class Block
extends AbstractStructuredStatement {
    private LinkedList<Op04StructuredStatement> containedStatements;
    private boolean indenting;
    private BlockIdentifier blockIdentifier;
    private static final LinkedList<Op04StructuredStatement> emptyBlockStatements = ListFactory.newLinkedList();

    public Block(LinkedList<Op04StructuredStatement> containedStatements, boolean indenting) {
        this(containedStatements, indenting, null);
    }

    public Block(LinkedList<Op04StructuredStatement> containedStatements, boolean indenting, BlockIdentifier blockIdentifier) {
        this.containedStatements = containedStatements;
        this.indenting = indenting;
        this.blockIdentifier = blockIdentifier;
    }

    public static Block getEmptyBlock() {
        return new Block(Block.emptyBlockStatements, false);
    }

    public static Block getEmptyBlock(boolean indenting) {
        return new Block(Block.emptyBlockStatements, indenting);
    }

    public static /* varargs */ Block getBlockFor(boolean indenting, StructuredStatement ... statements) {
        LinkedList tmp = ListFactory.newLinkedList();
        for (StructuredStatement statement : statements) {
            tmp.add((Op04StructuredStatement)new Op04StructuredStatement(statement));
        }
        return new Block(tmp, indenting);
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        for (Op04StructuredStatement statement : this.containedStatements) {
            statement.collectTypeUsages(collector);
        }
    }

    public boolean removeLastContinue(BlockIdentifier block) {
        AbstractStructuredContinue structuredContinue;
        StructuredStatement structuredStatement = this.containedStatements.getLast().getStatement();
        if (!(structuredStatement instanceof AbstractStructuredContinue)) return false;
        if ((structuredContinue = (AbstractStructuredContinue)structuredStatement).getContinueTgt() != block) return false;
        Op04StructuredStatement continueStmt = this.containedStatements.getLast();
        continueStmt.replaceStatementWithNOP("");
        return true;
    }

    public boolean removeLastNVReturn() {
        StructuredReturn structuredReturn;
        StructuredStatement structuredStatement = this.containedStatements.getLast().getStatement();
        if (!(structuredStatement instanceof StructuredReturn)) return false;
        Op04StructuredStatement oldReturn = this.containedStatements.getLast();
        if ((structuredReturn = (StructuredReturn)structuredStatement).getValue() != null) return true;
        oldReturn.replaceStatementWithNOP("");
        return true;
    }

    public boolean removeLastGoto() {
        StructuredStatement structuredStatement = this.containedStatements.getLast().getStatement();
        if (!(structuredStatement instanceof UnstructuredGoto)) return false;
        Op04StructuredStatement oldGoto = this.containedStatements.getLast();
        oldGoto.replaceStatementWithNOP("");
        return true;
    }

    public boolean removeLastGoto(Op04StructuredStatement toHere) {
        Op04StructuredStatement oldGoto;
        StructuredStatement structuredStatement = this.containedStatements.getLast().getStatement();
        if (!(structuredStatement instanceof UnstructuredGoto) || (oldGoto = this.containedStatements.getLast()).getTargets().get(0) != toHere) return false;
        oldGoto.replaceStatementWithNOP("");
        return true;
    }

    public UnstructuredWhile removeLastEndWhile() {
        StructuredStatement structuredStatement = this.containedStatements.getLast().getStatement();
        if (!(structuredStatement instanceof UnstructuredWhile)) return null;
        Op04StructuredStatement endWhile = this.containedStatements.getLast();
        endWhile.replaceStatementWithNOP("");
        return (UnstructuredWhile)structuredStatement;
    }

    public boolean isJustOneStatement() {
        int count = 0;
        Iterator i$ = this.containedStatements.iterator();
        while (i$.hasNext()) {
            Op04StructuredStatement statement;
            if ((statement = (Op04StructuredStatement)i$.next()).getStatement() instanceof StructuredComment) continue;
            ++count;
        }
        return count == 1;
    }

    public Op04StructuredStatement getSingleStatement() {
        Iterator i$ = this.containedStatements.iterator();
        while (i$.hasNext()) {
            Op04StructuredStatement statement;
            if ((statement = (Op04StructuredStatement)i$.next()).getStatement() instanceof StructuredComment) continue;
            return statement;
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean inlineable() {
        Iterator i$ = this.containedStatements.iterator();
        while (i$.hasNext()) {
            Op04StructuredStatement in;
            StructuredStatement s;
            Class c;
            if ((c = (s = (in = (Op04StructuredStatement)i$.next()).getStatement()).getClass()) == StructuredReturn.class || c == UnstructuredGoto.class) continue;
            return false;
        }
        return true;
    }

    @Override
    public Op04StructuredStatement getInline() {
        return this.getContainer();
    }

    public void combineInlineable() {
        boolean inline = false;
        Iterator i$ = this.containedStatements.iterator();
        while (i$.hasNext()) {
            Op04StructuredStatement in;
            if (!(in = (Op04StructuredStatement)i$.next()).getStatement().inlineable()) continue;
            inline = true;
        }
        if (!inline) {
            return;
        }
        LinkedList newContained = ListFactory.newLinkedList();
        Iterator i$2 = this.containedStatements.iterator();
        while (i$2.hasNext()) {
            Op04StructuredStatement in;
            StructuredStatement s;
            if ((s = (in = (Op04StructuredStatement)i$2.next()).getStatement()).inlineable()) {
                Op04StructuredStatement inlinedOp;
                StructuredStatement inlined;
                if (inlined = (inlinedOp = s.getInline()).getStatement() instanceof Block) {
                    List<Op04StructuredStatement> inlinedBlocks = ((Block)inlined).getBlockStatements();
                    newContained.addAll(((Block)inlined).getBlockStatements());
                    this.replaceInlineSource(in, inlinedBlocks.get(0));
                    continue;
                }
                newContained.add((Op04StructuredStatement)inlinedOp);
                this.replaceInlineSource(in, inlinedOp);
                continue;
            }
            newContained.add((Op04StructuredStatement)in);
        }
        this.containedStatements = newContained;
    }

    private void replaceInlineSource(Op04StructuredStatement oldS, Op04StructuredStatement newS) {
        for (Op04StructuredStatement src : oldS.getSources()) {
            src.replaceTarget(oldS, newS);
            newS.addSource(src);
        }
        newS.getSources().remove(oldS);
    }

    public void extractAnonymousBlocks() {
        Iterator<Op04StructuredStatement> iterator = this.containedStatements.descendingIterator();
        while (iterator.hasNext()) {
            StructuredStatement statement;
            Op04StructuredStatement stm;
            if ((statement = (stm = iterator.next()).getStatement()).getClass() != UnstructuredAnonBreakTarget.class) continue;
            UnstructuredAnonBreakTarget breakTarget = (UnstructuredAnonBreakTarget)statement;
            BlockIdentifier blockIdentifier = breakTarget.getBlockIdentifier();
            LinkedList inner = ListFactory.newLinkedList();
            iterator.remove();
            while (iterator.hasNext()) {
                inner.addFirst((Op04StructuredStatement)iterator.next());
                iterator.remove();
            }
            Block nested = new Block(inner, true, blockIdentifier);
            Set<BlockIdentifier> outerIdents = this.getContainer().getBlockIdentifiers();
            Set<BlockIdentifier> innerIdents = SetFactory.newSet(outerIdents);
            innerIdents.add(blockIdentifier);
            InstrIndex newIdx = this.getContainer().getIndex().justAfter();
            Op04StructuredStatement newStm = new Op04StructuredStatement(newIdx, (Collection<BlockIdentifier>)innerIdents, nested);
            this.containedStatements.addFirst(newStm);
            List<Op04StructuredStatement> sources = stm.getSources();
            Iterator<Op04StructuredStatement> i$ = sources.iterator();
            while (i$.hasNext()) {
                StructuredStatement maybeBreak;
                Op04StructuredStatement source;
                if ((maybeBreak = (source = i$.next()).getStatement()).getClass() == StructuredIf.class) {
                    StructuredIf structuredIf = (StructuredIf)maybeBreak;
                    source = structuredIf.getIfTaken();
                    maybeBreak = source.getStatement();
                }
                if (maybeBreak.getClass() != UnstructuredAnonymousBreak.class) continue;
                UnstructuredAnonymousBreak unstructuredBreak = (UnstructuredAnonymousBreak)maybeBreak;
                source.replaceStatement(unstructuredBreak.tryExplicitlyPlaceInBlock(blockIdentifier));
            }
            stm.replaceStatement(new StructuredComment(""));
        }
    }

    public void combineTryCatch() {
        Set<Class> skipThese = SetFactory.newSet(StructuredCatch.class, StructuredFinally.class, StructuredTry.class, UnstructuredTry.class);
        int size = this.containedStatements.size();
        boolean finished = false;
        block0 : for (int x = 0; !(x >= size || finished); ++x) {
            StructuredStatement nextStatement;
            Op04StructuredStatement statement;
            StructuredStatement innerStatement;
            if (innerStatement = (statement = this.containedStatements.get(x)).getStatement() instanceof UnstructuredTry) {
                StructuredStatement nextStatement2;
                UnstructuredTry unstructuredTry = (UnstructuredTry)innerStatement;
                if (x < size - 1 && (nextStatement2 = this.containedStatements.get(x + 1).getStatement() instanceof StructuredCatch || nextStatement2 instanceof StructuredFinally)) {
                    Op04StructuredStatement replacement = new Op04StructuredStatement(unstructuredTry.getEmptyTry());
                    Op04StructuredStatement.replaceInTargets(statement, replacement);
                    Op04StructuredStatement.replaceInSources(statement, replacement);
                    statement = replacement;
                    this.containedStatements.set(x, statement);
                    innerStatement = statement.getStatement();
                }
            }
            if (!(innerStatement instanceof StructuredTry)) continue;
            StructuredTry structuredTry = (StructuredTry)innerStatement;
            BlockIdentifier tryBlockIdent = structuredTry.getTryBlockIdentifier();
            Op04StructuredStatement next = ++x < size ? this.containedStatements.get(x) : null;
            if (!(next == null || skipThese.contains((nextStatement = next.getStatement()).getClass()))) {
                for (int y = x + 1; y < size; ++y) {
                    StructuredStatement test;
                    Set<BlockIdentifier> blocks;
                    if (test = this.containedStatements.get(y).getStatement() instanceof StructuredTry) continue block0;
                    if (test instanceof UnstructuredTry) continue block0;
                    if (!(test instanceof StructuredCatch) || !(blocks = ((StructuredCatch)test).getPossibleTryBlocks()).contains(tryBlockIdent)) continue;
                    x = y;
                    next = this.containedStatements.get(y);
                    break;
                }
            }
            for (; x < size && next != null; ++x) {
                if (nextStatement = next.getStatement() instanceof StructuredComment) {
                    next.nopThis();
                    continue;
                }
                if (nextStatement instanceof StructuredCatch) {
                    Set<BlockIdentifier> blocks;
                    if (!(blocks = ((StructuredCatch)nextStatement).getPossibleTryBlocks()).contains(tryBlockIdent)) {
                        --x;
                        break;
                    }
                    structuredTry.addCatch(next.nopThisAndReplace());
                    if (x < size) {
                        next = this.containedStatements.get(x);
                        continue;
                    }
                    next = null;
                    finished = true;
                    continue;
                }
                if (next.getStatement() instanceof StructuredFinally) {
                    structuredTry.addFinally(next.nopThisAndReplace());
                    if (x < size) {
                        next = this.containedStatements.get(x);
                        continue;
                    }
                    next = null;
                    finished = true;
                    continue;
                }
                --x;
            }
            --x;
        }
    }

    @Override
    public void transformStructuredChildren(StructuredStatementTransformer transformer, StructuredScope scope) {
        scope.add(this);
        try {
            int len = this.containedStatements.size();
            for (int x = 0; x < len; ++x) {
                Op04StructuredStatement structuredBlock = this.containedStatements.get(x);
                scope.setNextAtThisLevel(this, x < len - 1 ? x + 1 : -1);
                structuredBlock.transform(transformer, scope);
            }
        }
        finally {
            scope.remove(this);
        }
    }

    public Set<Op04StructuredStatement> getNextAfter(int x) {
        Set res = SetFactory.newSet();
        if (x == -1 || x > this.containedStatements.size()) {
            return res;
        }
        for (; x != -1 && x < this.containedStatements.size(); ++x) {
            Op04StructuredStatement next = this.containedStatements.get(x);
            res.add((Op04StructuredStatement)this.containedStatements.get(x));
            if (!(next.getStatement() instanceof StructuredComment)) return res;
        }
        return res;
    }

    public boolean statementIsLast(Op04StructuredStatement needle) {
        for (int x = this.containedStatements.size() - 1; x >= 0; --x) {
            Op04StructuredStatement statement;
            if ((statement = this.containedStatements.get(x)) == needle) {
                return true;
            }
            if (!(statement.getStatement() instanceof StructuredComment)) return false;
        }
        return false;
    }

    @Override
    public boolean isRecursivelyStructured() {
        Iterator i$ = this.containedStatements.iterator();
        while (i$.hasNext()) {
            Op04StructuredStatement structuredStatement;
            if ((structuredStatement = (Op04StructuredStatement)i$.next()).isFullyStructured()) continue;
            return false;
        }
        return true;
    }

    public List<Op04StructuredStatement> getBlockStatements() {
        return this.containedStatements;
    }

    @Override
    public void linearizeInto(List<StructuredStatement> out) {
        out.add(new BeginBlock(this));
        for (Op04StructuredStatement structuredBlock : this.containedStatements) {
            structuredBlock.linearizeStatementsInto(out);
        }
        out.add(new EndBlock(this));
    }

    @Override
    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        scopeDiscoverer.enterBlock(this);
        for (Op04StructuredStatement item : this.containedStatements) {
            item.traceLocalVariableScope(scopeDiscoverer);
        }
        scopeDiscoverer.leaveBlock(this);
    }

    @Override
    public void markCreator(LValue scopedEntity) {
        this.containedStatements.addFirst(new Op04StructuredStatement(new StructuredDefinition(scopedEntity)));
    }

    @Override
    public boolean alwaysDefines(LValue scopedEntity) {
        return false;
    }

    @Override
    public Dumper dump(Dumper d) {
        boolean isIndenting = this.isIndenting();
        if (this.blockIdentifier != null) {
            if (this.blockIdentifier.hasForeignReferences()) {
                d.print(this.blockIdentifier.getName() + " : ");
                isIndenting = true;
            } else {
                isIndenting = false;
            }
        }
        if (this.containedStatements.isEmpty()) {
            if (isIndenting) {
                d.print("{}\n");
            } else {
                d.print("\n");
            }
            return d;
        }
        try {
            if (isIndenting) {
                d.print("{\n");
                d.indent(1);
            }
            for (Op04StructuredStatement structuredBlock : this.containedStatements) {
                structuredBlock.dump(d);
            }
        }
        finally {
            if (isIndenting) {
                d.indent(-1);
                d.print("}");
                d.enqueuePendingCarriageReturn();
            }
        }
        return d;
    }

    public boolean isIndenting() {
        return this.indenting;
    }

    public void setIndenting(boolean indenting) {
        this.indenting = indenting;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rewriteExpressions(ExpressionRewriter expressionRewriter) {
    }

    @Override
    public boolean isEffectivelyNOP() {
        Iterator i$ = this.containedStatements.iterator();
        while (i$.hasNext()) {
            Op04StructuredStatement statement;
            if ((statement = (Op04StructuredStatement)i$.next()).getStatement().isEffectivelyNOP()) continue;
            return false;
        }
        return true;
    }
}

