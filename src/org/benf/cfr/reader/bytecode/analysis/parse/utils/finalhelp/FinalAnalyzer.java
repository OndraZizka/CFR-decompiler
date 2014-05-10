/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.IndexedStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CatchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CommentStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.FinallyStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.GotoStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.IfStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.JumpingStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.Nop;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ReturnStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ReturnValueStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ThrowStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.TryStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.FinallyCatchBody;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.FinallyGraphHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.PeerTries;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.Result;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.BinaryProcedure;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.graph.GraphVisitor;
import org.benf.cfr.reader.util.graph.GraphVisitorDFS;

public class FinalAnalyzer {
    public static boolean identifyFinally(Method method, Op03SimpleStatement in, List<Op03SimpleStatement> allStatements, BlockIdentifierFactory blockIdentifierFactory, Set<Op03SimpleStatement> analysedTries) {
        if (!(in.getStatement() instanceof TryStatement)) {
            return true;
        }
        analysedTries.add(in);
        TryStatement tryStatement = (TryStatement)in.getStatement();
        BlockIdentifier tryBlockIdentifier = tryStatement.getBlockIdentifier();
        List<Op03SimpleStatement> targets = in.getTargets();
        List<Op03SimpleStatement> catchStarts = Functional.filter(targets, new Op03SimpleStatement.TypeFilter(CatchStatement.class));
        Set possibleCatches = SetFactory.newOrderedSet();
        for (Op03SimpleStatement catchS : catchStarts) {
            CatchStatement catchStatement = (CatchStatement)catchS.getStatement();
            List<ExceptionGroup.Entry> exceptions = catchStatement.getExceptions();
            for (ExceptionGroup.Entry exception : exceptions) {
                JavaRefTypeInstance catchType;
                if (exception.getExceptionGroup().getTryBlockIdentifier() != tryBlockIdentifier || !"java.lang.Throwable".equals((catchType = exception.getCatchType()).getRawName())) continue;
                possibleCatches.add((Op03SimpleStatement)catchS);
            }
        }
        if (possibleCatches.isEmpty()) {
            return false;
        }
        Set exitPaths = SetFactory.newOrderedSet();
        GraphVisitorDFS<Op03SimpleStatement> gv = new GraphVisitorDFS<Op03SimpleStatement>(in.getTargets().get(0), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(tryBlockIdentifier, exitPaths){
            final /* synthetic */ BlockIdentifier val$tryBlockIdentifier;
            final /* synthetic */ Set val$exitPaths;

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                if (arg1.getBlockIdentifiers().contains(this.val$tryBlockIdentifier)) {
                    arg2.enqueue(arg1.getTargets());
                } else {
                    this.val$exitPaths.add(arg1);
                }
            }
        });
        gv.process();
        Op03SimpleStatement possibleFinallyCatch = FinalAnalyzer.findPossibleFinallyCatch(possibleCatches, allStatements);
        FinallyCatchBody finallyCatchBody = FinallyCatchBody.build(possibleFinallyCatch, allStatements);
        if (finallyCatchBody == null) {
            return false;
        }
        FinallyGraphHelper finallyGraphHelper = new FinallyGraphHelper(finallyCatchBody);
        PeerTries peerTries = new PeerTries(finallyGraphHelper, possibleFinallyCatch);
        peerTries.add(in);
        Set results = SetFactory.newOrderedSet();
        Set peerTrySeen = SetFactory.newSet();
        while (peerTries.hasNext()) {
            Op03SimpleStatement tryS;
            if (!peerTrySeen.add((Op03SimpleStatement)(tryS = peerTries.removeNext()))) continue;
            if (FinalAnalyzer.identifyFinally2(tryS, allStatements, peerTries, finallyGraphHelper, results)) continue;
            return false;
        }
        if (results.isEmpty()) {
            return false;
        }
        if (results.size() == 1) {
            return false;
        }
        List<Op03SimpleStatement> originalTryTargets = ListFactory.newList(SetFactory.newOrderedSet(in.getTargets()));
        Collections.sort(originalTryTargets, new Op03SimpleStatement.CompareByIndex());
        Op03SimpleStatement lastCatch = originalTryTargets.get(originalTryTargets.size() - 1);
        if (!(lastCatch.getStatement() instanceof CatchStatement)) {
            return false;
        }
        List<PeerTries.PeerTrySet> triesByLevel = peerTries.getPeerTryGroups();
        Set catchBlocksToNop = SetFactory.newOrderedSet();
        Set blocksToRemoveCompletely = SetFactory.newSet();
        PeerTries.PeerTrySet originalTryGroupPeers = triesByLevel.get(0);
        for (PeerTries.PeerTrySet peerSet : triesByLevel) {
            boolean firstTryInBlock = true;
            boolean artificalTry = true;
            for (Op03SimpleStatement peerTry : peerSet.getPeerTries()) {
                if (peerTry == in) {
                    peerTry.removeTarget(possibleFinallyCatch);
                    possibleFinallyCatch.removeSource(peerTry);
                    firstTryInBlock = false;
                    continue;
                }
                TryStatement peerTryStmt = (TryStatement)peerTry.getStatement();
                BlockIdentifier oldBlockIdent = peerTryStmt.getBlockIdentifier();
                List<Op03SimpleStatement> handlers = ListFactory.newList(peerTry.getTargets());
                int len = handlers.size();
                for (int x = 1; x < len; ++x) {
                    Op03SimpleStatement tgt = handlers.get(x);
                    tgt.removeSource(peerTry);
                    peerTry.removeTarget(tgt);
                    CatchStatement catchStatement = (CatchStatement)tgt.getStatement();
                    BlockIdentifier catchBlockIdent = catchStatement.getCatchBlockIdent();
                    catchStatement.removeCatchBlockFor(oldBlockIdent);
                    List<Op03SimpleStatement> catchSources = tgt.getSources();
                    Set unionBlocks = SetFactory.newSet();
                    for (Op03SimpleStatement catchSource : catchSources) {
                        unionBlocks.addAll(catchSource.getBlockIdentifiers());
                    }
                    Set<BlockIdentifier> previousTgtBlocks = SetFactory.newSet(tgt.getBlockIdentifiers());
                    previousTgtBlocks.removeAll(unionBlocks);
                    tgt.getBlockIdentifiers().removeAll(previousTgtBlocks);
                    if (!previousTgtBlocks.isEmpty()) {
                        tgt.getBlockIdentifiers().removeAll(previousTgtBlocks);
                        GraphVisitorDFS<Op03SimpleStatement> gv2 = new GraphVisitorDFS<Op03SimpleStatement>(tgt.getTargets(), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(catchBlockIdent, previousTgtBlocks){
                            final /* synthetic */ BlockIdentifier val$catchBlockIdent;
                            final /* synthetic */ Set val$previousTgtBlocks;

                            @Override
                            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                                if (!arg1.getBlockIdentifiers().contains(this.val$catchBlockIdent)) return;
                                arg1.getBlockIdentifiers().removeAll(this.val$previousTgtBlocks);
                                arg2.enqueue(arg1.getTargets());
                            }
                        });
                        gv2.process();
                    }
                    if (!tgt.getSources().isEmpty()) continue;
                    catchBlocksToNop.add((Op03SimpleStatement)tgt);
                }
                peerTry.nopOut();
                if (peerSet.equals(originalTryGroupPeers)) {
                    peerTry.getBlockIdentifiers().add(tryBlockIdentifier);
                }
                GraphVisitorDFS<Op03SimpleStatement> gvpeer = new GraphVisitorDFS<Op03SimpleStatement>(handlers.get(0), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(oldBlockIdent, peerSet, originalTryGroupPeers, tryBlockIdentifier){
                    final /* synthetic */ BlockIdentifier val$oldBlockIdent;
                    final /* synthetic */ PeerTries.PeerTrySet val$peerSet;
                    final /* synthetic */ PeerTries.PeerTrySet val$originalTryGroupPeers;
                    final /* synthetic */ BlockIdentifier val$tryBlockIdentifier;

                    @Override
                    public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                        Set<BlockIdentifier> blockIdentifiers = arg1.getBlockIdentifiers();
                        if (!blockIdentifiers.remove(this.val$oldBlockIdent)) return;
                        if (this.val$peerSet == this.val$originalTryGroupPeers) {
                            blockIdentifiers.add(this.val$tryBlockIdentifier);
                        }
                        arg2.enqueue(arg1.getTargets());
                    }
                });
                gvpeer.process();
                blocksToRemoveCompletely.add((BlockIdentifier)oldBlockIdent);
            }
        }
        CatchStatement catchStatement = (CatchStatement)lastCatch.getStatement();
        BlockIdentifier lastCatchIdent = catchStatement.getCatchBlockIdent();
        int found = -1;
        for (int x = allStatements.size() - 1; x >= 0; --x) {
            if (!allStatements.get(x).getBlockIdentifiers().contains(lastCatchIdent)) continue;
            found = x;
            break;
        }
        if (found == -1) {
            throw new IllegalStateException("Last catch has completely empty body");
        }
        Op03SimpleStatement lastCatchContentStatement = allStatements.get(found);
        InstrIndex newIdx = lastCatchContentStatement.getIndex().justAfter();
        Result cloneThis = (Result)results.iterator().next();
        List<Op03SimpleStatement> oldFinallyBody = ListFactory.newList(cloneThis.getToRemove());
        Collections.sort(oldFinallyBody, new Op03SimpleStatement.CompareByIndex());
        List newFinallyBody = ListFactory.newList();
        Set<BlockIdentifier> oldStartBlocks = SetFactory.newOrderedSet(oldFinallyBody.get(0).getBlockIdentifiers());
        Set<BlockIdentifier> extraBlocks = SetFactory.newOrderedSet(in.getBlockIdentifiers());
        BlockIdentifier finallyBlock = blockIdentifierFactory.getNextBlockIdentifier(BlockType.CATCHBLOCK);
        FinallyStatement finallyStatement = new FinallyStatement(finallyBlock);
        Op03SimpleStatement finallyOp = new Op03SimpleStatement(extraBlocks, finallyStatement, newIdx);
        newIdx = newIdx.justAfter();
        newFinallyBody.add((Op03SimpleStatement)finallyOp);
        extraBlocks.add(finallyBlock);
        Map old2new = MapFactory.newMap();
        for (Op03SimpleStatement old : oldFinallyBody) {
            Statement statement = old.getStatement();
            Set<BlockIdentifier> newblocks = SetFactory.newOrderedSet(old.getBlockIdentifiers());
            newblocks.removeAll(oldStartBlocks);
            newblocks.addAll(extraBlocks);
            Op03SimpleStatement newOp = new Op03SimpleStatement(newblocks, statement, old.getSSAIdentifiers(), newIdx);
            newFinallyBody.add((Op03SimpleStatement)newOp);
            newIdx = newIdx.justAfter();
            old2new.put((Op03SimpleStatement)old, (Op03SimpleStatement)newOp);
        }
        if (newFinallyBody.size() > 1) {
            ((Op03SimpleStatement)newFinallyBody.get(1)).markFirstStatementInBlock(finallyBlock);
        }
        Op03SimpleStatement endRewrite = null;
        Iterator i$ = results.iterator();
        while (i$.hasNext()) {
            Result r;
            Op03SimpleStatement rAfterEnd;
            if ((rAfterEnd = (r = (Result)i$.next()).getAfterEnd()) == null || !rAfterEnd.getIndex().isBackJumpFrom(r.getStart())) continue;
            endRewrite = new Op03SimpleStatement(extraBlocks, new GotoStatement(), newIdx);
            endRewrite.addTarget(rAfterEnd);
            rAfterEnd.addSource(endRewrite);
        }
        if (endRewrite == null) {
            endRewrite = new Op03SimpleStatement(extraBlocks, new CommentStatement(""), newIdx);
        }
        newFinallyBody.add(endRewrite);
        for (Op03SimpleStatement old2 : oldFinallyBody) {
            Op03SimpleStatement newOp = (Op03SimpleStatement)old2new.get(old2);
            Iterator<Op03SimpleStatement> i$2 = old2.getSources().iterator();
            while (i$2.hasNext()) {
                Op03SimpleStatement newSrc;
                Op03SimpleStatement src;
                if ((newSrc = (Op03SimpleStatement)old2new.get(src = i$2.next())) == null) continue;
                newOp.addSource(newSrc);
            }
            i$2 = old2.getTargets().iterator();
            while (i$2.hasNext()) {
                Op03SimpleStatement newTgt;
                Op03SimpleStatement tgt;
                if ((newTgt = (Op03SimpleStatement)old2new.get(tgt = i$2.next())) == null) {
                    if (Op03SimpleStatement.followNopGotoChain(tgt, false, false) != cloneThis.getAfterEnd()) {
                        if (!(newOp.getStatement() instanceof JumpingStatement)) continue;
                        if (!tgt.getIndex().isBackJumpFrom(endRewrite)) {
                            endRewrite.addSource(newOp);
                            newOp.addTarget(endRewrite);
                            if (endRewrite.getTargets().contains(tgt)) continue;
                            endRewrite.addTarget(tgt);
                            tgt.addSource(endRewrite);
                            continue;
                        }
                    } else {
                        endRewrite.addSource(newOp);
                        newTgt = endRewrite;
                    }
                    newTgt = tgt;
                    tgt.addSource(newOp);
                }
                newOp.addTarget(newTgt);
            }
        }
        if (newFinallyBody.size() >= 2) {
            Op03SimpleStatement startFinallyCopy = (Op03SimpleStatement)newFinallyBody.get(1);
            startFinallyCopy.addSource(finallyOp);
            finallyOp.addTarget(startFinallyCopy);
        }
        for (Result result : results) {
            Op03SimpleStatement start = result.getStart();
            Set<Op03SimpleStatement> toRemove = result.getToRemove();
            Op03SimpleStatement afterEnd = result.getAfterEnd();
            List<Op03SimpleStatement> startSources = ListFactory.newList(start.getSources());
            for (Op03SimpleStatement source : startSources) {
                JavaTypeInstance returnType;
                Statement sourceStatement;
                if (toRemove.contains(source)) continue;
                if (afterEnd != null) {
                    boolean canDirect = source.getStatement() instanceof JumpingStatement || source.getIndex().isBackJumpFrom(afterEnd);
                    if (canDirect && source.getStatement().getClass() == IfStatement.class && start == source.getTargets().get(0)) {
                        canDirect = false;
                    }
                    if (canDirect) {
                        source.replaceTarget(start, afterEnd);
                        afterEnd.addSource(source);
                        continue;
                    }
                    Op03SimpleStatement afterSource = new Op03SimpleStatement(source.getBlockIdentifiers(), new GotoStatement(), source.getIndex().justAfter());
                    afterEnd.addSource(afterSource);
                    afterSource.addTarget(afterEnd);
                    afterSource.addSource(source);
                    source.replaceTarget(start, afterSource);
                    allStatements.add(afterSource);
                    continue;
                }
                if ((sourceStatement = source.getStatement()).getClass() == GotoStatement.class) {
                    source.replaceStatement(new Nop());
                    source.removeTarget(start);
                    continue;
                }
                if (sourceStatement.getClass() == IfStatement.class) {
                    IfStatement ifStatement;
                    boolean flip = (ifStatement = (IfStatement)sourceStatement).getJumpTarget().getContainer() == start;
                    if (!flip) {
                        throw new IllegalStateException("If jumping OVER finally body.");
                    }
                    source.replaceTarget(start, endRewrite);
                    endRewrite.addSource(source);
                    continue;
                }
                if ((returnType = method.getMethodPrototype().getReturnType()) == RawJavaType.VOID) {
                    source.removeTarget(start);
                    continue;
                }
                if (sourceStatement instanceof AssignmentSimple) {
                    AssignmentSimple sourceAssignment;
                    LValue lValue;
                    JavaTypeInstance lValueType;
                    if ((lValueType = (lValue = (sourceAssignment = (AssignmentSimple)sourceStatement).getCreatedLValue()).getInferredJavaType().getJavaTypeInstance()).implicitlyCastsTo(lValueType = (lValue = (sourceAssignment = (AssignmentSimple)sourceStatement).getCreatedLValue()).getInferredJavaType().getJavaTypeInstance(), null)) {
                        Op03SimpleStatement afterSource = new Op03SimpleStatement(source.getBlockIdentifiers(), new ReturnValueStatement(new LValueExpression(lValue), returnType), source.getIndex().justAfter());
                        source.replaceTarget(start, afterSource);
                        afterSource.addSource(source);
                        allStatements.add(afterSource);
                        continue;
                    }
                    source.removeTarget(start);
                    continue;
                }
                source.removeTarget(start);
            }
            for (Op03SimpleStatement remove : toRemove) {
                for (Op03SimpleStatement source2 : remove.getSources()) {
                    source2.getTargets().remove(remove);
                }
                for (Op03SimpleStatement target : remove.getTargets()) {
                    target.getSources().remove(remove);
                }
                remove.getSources().clear();
                remove.getTargets().clear();
                remove.nopOut();
            }
            if (afterEnd == null) continue;
            List<Op03SimpleStatement> endSources = ListFactory.newList(afterEnd.getSources());
            for (Op03SimpleStatement source3 : endSources) {
                if (!toRemove.contains(source3)) continue;
                afterEnd.removeSource(source3);
            }
        }
        i$ = originalTryGroupPeers.getPeerTries().iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement topTry;
            Statement topStatement;
            if (!(topStatement = (topTry = (Op03SimpleStatement)i$.next()).getStatement() instanceof TryStatement)) continue;
            TryStatement topTryStatement = (TryStatement)topStatement;
            BlockIdentifier topTryIdent = topTryStatement.getBlockIdentifier();
            Set peerTryExits = SetFactory.newOrderedSet();
            GraphVisitorDFS<Op03SimpleStatement> gv2 = new GraphVisitorDFS<Op03SimpleStatement>(topTry.getTargets().get(0), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(topTryIdent, peerTryExits){
                final /* synthetic */ BlockIdentifier val$topTryIdent;
                final /* synthetic */ Set val$peerTryExits;

                @Override
                public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                    if (arg1.getBlockIdentifiers().contains(this.val$topTryIdent)) {
                        arg2.enqueue(arg1.getTargets());
                    } else {
                        this.val$peerTryExits.add(arg1);
                    }
                }
            });
            gv2.process();
            block20 : for (Op03SimpleStatement peerTryExit : peerTryExits) {
                for (Op03SimpleStatement source : peerTryExit.getSources()) {
                    if (source.getBlockIdentifiers().contains(topTryIdent)) continue;
                    continue block20;
                }
                if (!peerTryExit.getIndex().isBackJumpFrom(finallyOp)) continue;
                peerTryExit.getBlockIdentifiers().add(topTryIdent);
            }
        }
        for (Op03SimpleStatement stm : allStatements) {
            stm.getBlockIdentifiers().removeAll(blocksToRemoveCompletely);
        }
        in.addTarget(finallyOp);
        finallyOp.addSource(in);
        allStatements.addAll(newFinallyBody);
        return true;
    }

    public static boolean identifyFinally2(Op03SimpleStatement in, List<Op03SimpleStatement> allStatements, PeerTries peerTries, FinallyGraphHelper finallyGraphHelper, Set<Result> results) {
        if (!(in.getStatement() instanceof TryStatement)) {
            return false;
        }
        TryStatement tryStatement = (TryStatement)in.getStatement();
        BlockIdentifier tryBlockIdentifier = tryStatement.getBlockIdentifier();
        List<Op03SimpleStatement> targets = in.getTargets();
        List<Op03SimpleStatement> catchStarts = Functional.filter(targets, new Op03SimpleStatement.TypeFilter(CatchStatement.class));
        Set possibleCatches = SetFactory.newOrderedSet();
        Set recTries = SetFactory.newSet();
        for (Op03SimpleStatement catchS : catchStarts) {
            CatchStatement catchStatement = (CatchStatement)catchS.getStatement();
            List<ExceptionGroup.Entry> exceptions = catchStatement.getExceptions();
            for (ExceptionGroup.Entry exception : exceptions) {
                Op03SimpleStatement catchTgt;
                JavaRefTypeInstance catchType;
                if (exception.getExceptionGroup().getTryBlockIdentifier() != tryBlockIdentifier) continue;
                if ("java.lang.Throwable".equals((catchType = exception.getCatchType()).getRawName())) {
                    possibleCatches.add((Op03SimpleStatement)catchS);
                    continue;
                }
                if ((catchTgt = catchS.getTargets().get(0)).getStatement().getClass() != TryStatement.class) continue;
                recTries.add((Op03SimpleStatement)catchTgt);
            }
        }
        if (possibleCatches.isEmpty()) {
            return false;
        }
        boolean result = false;
        for (Op03SimpleStatement recTry : recTries) {
            result|=FinalAnalyzer.identifyFinally2(recTry, allStatements, peerTries, finallyGraphHelper, results);
        }
        Set exitPaths = SetFactory.newOrderedSet();
        GraphVisitorDFS<Op03SimpleStatement> gv = new GraphVisitorDFS<Op03SimpleStatement>(in.getTargets().get(0), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(tryBlockIdentifier, exitPaths){
            final /* synthetic */ BlockIdentifier val$tryBlockIdentifier;
            final /* synthetic */ Set val$exitPaths;

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                if (arg1.getBlockIdentifiers().contains(this.val$tryBlockIdentifier)) {
                    arg2.enqueue(arg1.getTargets());
                } else {
                    this.val$exitPaths.add(arg1);
                }
            }
        });
        gv.process();
        FinalAnalyzer.filterPeerTries(exitPaths, peerTries);
        Iterator i$ = exitPaths.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement legitExitStart;
            Result legitExitResult;
            if ((legitExitResult = finallyGraphHelper.match(legitExitStart = (Op03SimpleStatement)i$.next())).isFail()) {
                return result;
            }
            results.add(legitExitResult);
        }
        List<Op03SimpleStatement> tryTargets = in.getTargets();
        Set seen = SetFactory.newOrderedSet();
        int len = tryTargets.size();
        for (int x = 1; x < len; ++x) {
            Op03SimpleStatement tryCatch;
            if (FinalAnalyzer.verifyCatchFinally(tryCatch = tryTargets.get(x), finallyGraphHelper, peerTries, results)) continue;
            return result;
        }
        return true;
    }

    private static void filterPeerTries(Collection<Op03SimpleStatement> possibleFinally, PeerTries peerTries) {
        Set res = SetFactory.newOrderedSet();
        for (Op03SimpleStatement possible : possibleFinally) {
            if (possible.getStatement() instanceof TryStatement && possible.getTargets().contains(peerTries.getOriginalFinally())) {
                peerTries.add(possible);
                continue;
            }
            res.add((Op03SimpleStatement)possible);
        }
        possibleFinally.clear();
        possibleFinally.addAll(res);
    }

    private static boolean verifyCatchFinally(Op03SimpleStatement in, FinallyGraphHelper finallyGraphHelper, PeerTries peerTries, Set<Result> results) {
        Op03SimpleStatement finallyCodeStart;
        if (!(in.getStatement() instanceof CatchStatement)) {
            return false;
        }
        if (in.getTargets().size() != 1) {
            return false;
        }
        CatchStatement catchStatement = (CatchStatement)in.getStatement();
        BlockIdentifier catchBlockIdent = catchStatement.getCatchBlockIdent();
        Op03SimpleStatement firstStatementInCatch = in.getTargets().get(0);
        List statementsInCatch = ListFactory.newList();
        Set targetsOutsideCatch = SetFactory.newOrderedSet();
        Set directExitsFromCatch = SetFactory.newOrderedSet();
        Map exitParents = MapFactory.newLazyMap(new UnaryFunction<Op03SimpleStatement, Set<Op03SimpleStatement>>(){

            @Override
            public Set<Op03SimpleStatement> invoke(Op03SimpleStatement arg) {
                return SetFactory.newOrderedSet();
            }
        });
        GraphVisitorDFS<Op03SimpleStatement> gv = new GraphVisitorDFS<Op03SimpleStatement>(firstStatementInCatch, (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(catchBlockIdent, statementsInCatch, exitParents, directExitsFromCatch, targetsOutsideCatch){
            final /* synthetic */ BlockIdentifier val$catchBlockIdent;
            final /* synthetic */ List val$statementsInCatch;
            final /* synthetic */ Map val$exitParents;
            final /* synthetic */ Set val$directExitsFromCatch;
            final /* synthetic */ Set val$targetsOutsideCatch;

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                if (arg1.getBlockIdentifiers().contains(this.val$catchBlockIdent)) {
                    Statement statement;
                    this.val$statementsInCatch.add(arg1);
                    arg2.enqueue(arg1.getTargets());
                    for (Op03SimpleStatement tgt : arg1.getTargets()) {
                        ((Set)this.val$exitParents.get(tgt)).add(arg1);
                    }
                    if (!(statement = arg1.getStatement() instanceof ReturnStatement)) return;
                    this.val$directExitsFromCatch.add(arg1);
                } else {
                    this.val$targetsOutsideCatch.add(arg1);
                }
            }
        });
        gv.process();
        for (Op03SimpleStatement outsideCatch : targetsOutsideCatch) {
            directExitsFromCatch.addAll((Collection)exitParents.get(outsideCatch));
        }
        if ((finallyCodeStart = finallyGraphHelper.getFinallyCatchBody().getCatchCodeStart()) == null) {
            return false;
        }
        Statement finallyStartStatement = finallyCodeStart.getStatement();
        List possibleFinalStarts = Functional.filter(statementsInCatch, new Predicate<Op03SimpleStatement>(finallyStartStatement){
            final /* synthetic */ Statement val$finallyStartStatement;

            @Override
            public boolean test(Op03SimpleStatement in) {
                return in.getStatement().getClass() == this.val$finallyStartStatement.getClass();
            }
        });
        List possibleFinallyBlocks = ListFactory.newList();
        Iterator i$ = possibleFinalStarts.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement possibleFinallyStart;
            Result res;
            if ((res = finallyGraphHelper.match(possibleFinallyStart = (Op03SimpleStatement)i$.next())).isFail()) continue;
            possibleFinallyBlocks.add((Result)res);
        }
        Map matchedFinallyBlockMap = MapFactory.newMap();
        for (Result res : possibleFinallyBlocks) {
            for (Op03SimpleStatement b : res.getToRemove()) {
                matchedFinallyBlockMap.put((Op03SimpleStatement)b, (Result)res);
            }
        }
        List tryStatements = Functional.filter(statementsInCatch, new Op03SimpleStatement.TypeFilter(TryStatement.class));
        FinalAnalyzer.filterPeerTries(tryStatements, peerTries);
        List matchedFinallyClones = ListFactory.newList();
        if (finallyGraphHelper.getFinallyCatchBody().hasThrowOp()) {
            for (Op03SimpleStatement exit : directExitsFromCatch) {
                Iterator<Op03SimpleStatement> i$2 = exit.getSources().iterator();
                while (i$2.hasNext()) {
                    Result res2;
                    Op03SimpleStatement source;
                    if ((res2 = (Result)matchedFinallyBlockMap.get(source = i$2.next())) == null) {
                        if (exit.getStatement() instanceof ThrowStatement) continue;
                        return false;
                    }
                    results.add(res2);
                }
            }
        } else {
            Iterator<Op03SimpleStatement> i$3 = directExitsFromCatch.iterator();
            while (i$3.hasNext()) {
                Op03SimpleStatement exit;
                Result res3;
                if ((res3 = (Result)matchedFinallyBlockMap.get(exit = i$3.next())) == null) {
                    if (exit.getStatement() instanceof ThrowStatement) continue;
                    return false;
                }
                results.add(res3);
            }
        }
        return true;
    }

    private static Op03SimpleStatement findPossibleFinallyCatch(Set<Op03SimpleStatement> possibleCatches, List<Op03SimpleStatement> allStatements) {
        List<Op03SimpleStatement> tmp = ListFactory.newList(possibleCatches);
        Collections.sort(tmp, new Op03SimpleStatement.CompareByIndex());
        Op03SimpleStatement catchS = tmp.get(tmp.size() - 1);
        return catchS;
    }

}

