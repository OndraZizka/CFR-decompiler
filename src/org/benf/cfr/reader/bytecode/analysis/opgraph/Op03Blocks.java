/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CatchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.GotoStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.IfStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.TryStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.DecompilerComments;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.SetUtil;
import org.benf.cfr.reader.util.functors.BinaryProcedure;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.graph.GraphVisitor;
import org.benf.cfr.reader.util.graph.GraphVisitorDFS;

public class Op03Blocks {
    private static List<Block3> doTopSort(List<Block3> in) {
        LinkedHashSet allBlocks = new LinkedHashSet();
        allBlocks.addAll(in);
        TreeSet<Block3> ready = new TreeSet<Block3>();
        ready.add(in.get(0));
        List output = ListFactory.newList(in.size());
        Block3 last = null;
        while (!allBlocks.isEmpty()) {
            Block3 next = null;
            if (!ready.isEmpty()) {
                next = (Block3)ready.iterator().next();
                ready.remove(next);
            } else if (next == null) {
                next = (Block3)allBlocks.iterator().next();
            }
            last = next;
            allBlocks.remove((Object)next);
            output.add((Block3)next);
            for (Block3 child : next.targets) {
                child.sources.remove(next);
                if (!child.sources.isEmpty() || !allBlocks.contains((Object)child)) continue;
                ready.add(child);
            }
        }
        return output;
    }

    private static void apply0TargetBlockHeuristic(List<Block3> blocks) {
        for (int idx = blocks.size() - 1; idx >= 0; --idx) {
            Block3 block = blocks.get(idx);
            if (!block.targets.isEmpty()) continue;
            boolean move = false;
            Block3 lastSource = block;
            for (Block3 source : block.sources) {
                if (lastSource.compareTo(source) >= 0) continue;
                move = true;
                lastSource = source;
            }
            if (!move) continue;
            block.startIndex = lastSource.startIndex.justAfter();
            blocks.add(blocks.indexOf(lastSource) + 1, block);
            blocks.remove(idx);
        }
    }

    private static void removeAliases(Set<BlockIdentifier> in, Map<BlockIdentifier, BlockIdentifier> aliases) {
        Set toRemove = SetFactory.newSet();
        Iterator<BlockIdentifier> i$ = in.iterator();
        while (i$.hasNext()) {
            BlockIdentifier alias;
            BlockIdentifier i;
            if ((alias = aliases.get(i = i$.next())) == null || !in.contains(alias)) continue;
            toRemove.add((BlockIdentifier)i);
            toRemove.add((BlockIdentifier)alias);
        }
        in.removeAll(toRemove);
    }

    private static Map<BlockIdentifier, BlockIdentifier> getTryBlockAliases(List<Op03SimpleStatement> statements) {
        Map tryBlockAliases = MapFactory.newMap();
        List<Op03SimpleStatement> catchStatements = Functional.filter(statements, new Op03SimpleStatement.TypeFilter(CatchStatement.class));
        Iterator<Op03SimpleStatement> i$ = catchStatements.iterator();
        block0 : while (i$.hasNext()) {
            CatchStatement catchStatement;
            Op03SimpleStatement catchStatementCtr;
            List<ExceptionGroup.Entry> caught;
            if ((caught = (catchStatement = (CatchStatement)(catchStatementCtr = i$.next()).getStatement()).getExceptions()).isEmpty()) continue;
            ExceptionGroup.Entry first = caught.get(0);
            JavaRefTypeInstance catchType = first.getCatchType();
            BlockIdentifier tryBlockMain = first.getTryBlockIdentifier();
            List possibleAliases = ListFactory.newList();
            int len = caught.size();
            for (int x = 1; x < len; ++x) {
                ExceptionGroup.Entry entry;
                if (!(entry = caught.get(x)).getCatchType().equals(catchType)) continue block0;
                BlockIdentifier tryBlockIdent = entry.getTryBlockIdentifier();
                possibleAliases.add((BlockIdentifier)tryBlockIdent);
            }
            for (Op03SimpleStatement source : catchStatementCtr.getSources()) {
                if (!source.getBlockIdentifiers().contains(tryBlockMain)) continue;
                continue block0;
            }
            Iterator<Op03SimpleStatement> i$2 = possibleAliases.iterator();
            while (i$2.hasNext()) {
                BlockIdentifier last;
                BlockIdentifier alias;
                if ((last = tryBlockAliases.put((BlockIdentifier)(alias = (BlockIdentifier)i$2.next()), (BlockIdentifier)tryBlockMain)) == null || last == tryBlockMain) continue;
                boolean a = true;
            }
        }
        return tryBlockAliases;
    }

    private static void applyKnownBlocksHeuristic(Method method, List<Block3> blocks, Map<BlockIdentifier, BlockIdentifier> tryBlockAliases) {
        Block3 linPrev = null;
        for (Block3 block : blocks) {
            Block3 source;
            Set<BlockIdentifier> endIdents;
            Op03SimpleStatement start = block.getStart();
            Set<BlockIdentifier> startIdents = start.getBlockIdentifiers();
            boolean needLinPrev = false;
            if (block.sources.contains(linPrev) && !(endIdents = (source = linPrev).getEnd().getBlockIdentifiers()).equals(startIdents)) {
                Set<BlockIdentifier> diffs = SetUtil.difference(endIdents, startIdents);
                BlockIdentifier newTryBlock = null;
                if (block.getStart().getStatement() instanceof TryStatement && !diffs.add(newTryBlock = ((TryStatement)block.getStart().getStatement()).getBlockIdentifier())) {
                    newTryBlock = null;
                }
                Op03Blocks.removeAliases(diffs, tryBlockAliases);
                for (BlockIdentifier blk : diffs) {
                    if (blk.getBlockType() == BlockType.CASE) continue;
                    if (blk.getBlockType() == BlockType.SWITCH) continue;
                    if (blk == newTryBlock) continue;
                    needLinPrev = true;
                }
            }
            if (needLinPrev) {
                block.addSource(linPrev);
            }
            linPrev = block;
        }
    }

    private static List<Block3> buildBasicBlocks(Method method, List<Op03SimpleStatement> statements) {
        List blocks = ListFactory.newList();
        Map starts = MapFactory.newMap();
        Map ends = MapFactory.newMap();
        GraphVisitorDFS<Op03SimpleStatement> gv = new GraphVisitorDFS<Op03SimpleStatement>(statements.get(0), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(starts, blocks, ends){
            final /* synthetic */ Map val$starts;
            final /* synthetic */ List val$blocks;
            final /* synthetic */ Map val$ends;

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                Op03SimpleStatement next;
                Block3 block = new Block3(arg1);
                this.val$starts.put(arg1, block);
                while (arg1.getTargets().size() == 1 && (next = arg1.getTargets().get(0)).getSources().size() == 1 && arg1.getBlockIdentifiers().equals(next.getBlockIdentifiers())) {
                    arg1 = next;
                    block.append(arg1);
                }
                this.val$blocks.add(block);
                this.val$ends.put(arg1, block);
                arg2.enqueue(arg1.getTargets());
            }
        });
        gv.process();
        Collections.sort(blocks);
        for (Block3 block : blocks) {
            Op03SimpleStatement start = block.getStart();
            List<Op03SimpleStatement> prevList = start.getSources();
            List prevBlocks = ListFactory.newList(prevList.size());
            Iterator<Op03SimpleStatement> i$ = prevList.iterator();
            while (i$.hasNext()) {
                Block3 prevEnd;
                Op03SimpleStatement prev;
                if ((prevEnd = (Block3)ends.get(prev = i$.next())) == null) {
                    throw new IllegalStateException("Topological sort failed, explicitly disable");
                }
                prevBlocks.add((Block3)prevEnd);
            }
            Op03SimpleStatement end = block.getEnd();
            List<Op03SimpleStatement> afterList = end.getTargets();
            List postBlocks = ListFactory.newList(afterList.size());
            for (Op03SimpleStatement after : afterList) {
                postBlocks.add(starts.get(after));
            }
            block.addSources(prevBlocks);
            block.addTargets(postBlocks);
            if (!(end.getStatement() instanceof TryStatement)) continue;
            List depends = ListFactory.newList();
            for (Block3 tgt : postBlocks) {
                tgt.addSources(depends);
                for (Block3 depend : depends) {
                    depend.addTarget(tgt);
                }
                depends.add((Block3)tgt);
            }
        }
        return blocks;
    }

    private static boolean detectMoves(List<Block3> blocks) {
        Map idxLut = MapFactory.newIdentityMap();
        int len = blocks.size();
        for (int i = 0; i < len; ++i) {
            idxLut.put((Block3)blocks.get(i), i);
        }
        BlockIdentifierFactory blockIdentifierFactory = new BlockIdentifierFactory();
        List blockMembers = ListFactory.newList();
        int len2 = blocks.size();
        for (int i2 = 0; i2 < len2; ++i2) {
            blockMembers.add(SetFactory.newSet());
        }
        Map firstByBlock = MapFactory.newMap();
        Map lastByBlock = MapFactory.newMap();
        int len3 = blocks.size();
        for (int i3 = 0; i3 < len3; ++i3) {
            Block3 block;
            Block3 lastBackJump;
            if ((lastBackJump = (block = blocks.get(i3)).getLastUnconditionalBackjumpToHere(idxLut)) == null) continue;
            BlockIdentifier bid = blockIdentifierFactory.getNextBlockIdentifier(BlockType.DOLOOP);
            int last = (Integer)idxLut.get(lastBackJump);
            for (int x = i3 + 1; x <= last; ++x) {
                ((Set)blockMembers.get(x)).add(bid);
            }
            firstByBlock.put((BlockIdentifier)bid, (Block3)block);
            lastByBlock.put((BlockIdentifier)bid, (Block3)lastBackJump);
        }
        boolean effect = false;
        int len4 = blocks.size();
        for (int i4 = 0; i4 < len4; ++i4) {
            Set inThese;
            Block3 block = blocks.get(i4);
            if (!block.targets.isEmpty()) continue;
            if ((inThese = (Set)blockMembers.get(i4)).isEmpty()) continue;
            Iterator<Block3> i$ = block.originalSources.iterator();
            while (i$.hasNext()) {
                Set sourceInThese;
                int j;
                Block3 source;
                if ((j = ((Integer)idxLut.get(source = i$.next())).intValue()) >= i4 || (sourceInThese = (Set)blockMembers.get(j)).containsAll(inThese)) continue;
                Set tmp = SetFactory.newSet(inThese);
                tmp.removeAll(sourceInThese);
                List newSources = ListFactory.newList();
                Iterator i$2 = tmp.iterator();
                while (i$2.hasNext()) {
                    BlockIdentifier jumpedInto;
                    if (firstByBlock.get(jumpedInto = (BlockIdentifier)i$2.next()) == block) continue;
                    newSources.add(lastByBlock.get(jumpedInto));
                }
                if (newSources.isEmpty()) continue;
                block.addSources(newSources);
                effect = true;
            }
        }
        if (!effect) {
            return false;
        }
        for (Block3 block : blocks) {
            block.copySources();
        }
        return true;
    }

    private static void stripTryBlockAliases(List<Op03SimpleStatement> out, Map<BlockIdentifier, BlockIdentifier> tryBlockAliases) {
        List remove = ListFactory.newList();
        int len = out.size();
        for (int x = 1; x < len; ++x) {
            BlockIdentifier alias;
            Op03SimpleStatement s;
            if ((s = out.get(x)).getStatement().getClass() != TryStatement.class) continue;
            TryStatement tryStatement = (TryStatement)s.getStatement();
            BlockIdentifier tryBlock = tryStatement.getBlockIdentifier();
            Op03SimpleStatement prev = out.get(x - 1);
            if ((alias = tryBlockAliases.get(tryBlock)) == null) continue;
            if (!prev.getBlockIdentifiers().contains(alias)) continue;
            remove.add((Op03SimpleStatement)s);
        }
        if (remove.isEmpty()) {
            return;
        }
        for (Op03SimpleStatement removeThis : remove) {
            TryStatement removeTry = (TryStatement)removeThis.getStatement();
            BlockIdentifier blockIdentifier = removeTry.getBlockIdentifier();
            BlockIdentifier alias = tryBlockAliases.get(blockIdentifier);
            List<Op03SimpleStatement> targets = removeThis.getTargets();
            Op03SimpleStatement naturalTarget = targets.get(0);
            for (Op03SimpleStatement target : targets) {
                target.removeSource(removeThis);
            }
            for (Op03SimpleStatement source : removeThis.getSources()) {
                source.replaceTarget(removeThis, naturalTarget);
                naturalTarget.addSource(source);
            }
            removeThis.clear();
            for (Op03SimpleStatement statement : out) {
                statement.replaceBlockIfIn(blockIdentifier, alias);
            }
        }
    }

    public static List<Op03SimpleStatement> combineTryBlocks(Method method, List<Op03SimpleStatement> statements) {
        Map<BlockIdentifier, BlockIdentifier> tryBlockAliases = Op03Blocks.getTryBlockAliases(statements);
        Op03Blocks.stripTryBlockAliases(statements, tryBlockAliases);
        return Op03SimpleStatement.removeUnreachableCode(statements, true);
    }

    public static List<Op03SimpleStatement> topologicalSort(Method method, List<Op03SimpleStatement> statements, DecompilerComments comments, Options options) {
        List<Block3> blocks = Op03Blocks.buildBasicBlocks(method, statements);
        Op03Blocks.apply0TargetBlockHeuristic(blocks);
        Map<BlockIdentifier, BlockIdentifier> tryBlockAliases = Op03Blocks.getTryBlockAliases(statements);
        Op03Blocks.applyKnownBlocksHeuristic(method, blocks, tryBlockAliases);
        blocks = Op03Blocks.doTopSort(blocks);
        if (Op03Blocks.detectMoves(blocks)) {
            Collections.sort(blocks);
            blocks = Op03Blocks.doTopSort(blocks);
        }
        int len = blocks.size();
        for (int i = 0; i < len - 1; ++i) {
            Block3 thisBlock = blocks.get(i);
            Block3 nextBlock = blocks.get(i + 1);
            Op03Blocks.patch(thisBlock, nextBlock);
        }
        Op03Blocks.patch(blocks.get(blocks.size() - 1), null);
        List outStatements = ListFactory.newList();
        for (Block3 outBlock : blocks) {
            outStatements.addAll(outBlock.getContent());
        }
        int newIndex = 0;
        for (Op03SimpleStatement statement : outStatements) {
            statement.setIndex(new InstrIndex(newIndex++));
        }
        boolean patched = false;
        int origLen = outStatements.size() - 1;
        for (int x = 0; x < origLen; ++x) {
            Op03SimpleStatement stm;
            Op03SimpleStatement next;
            List<Op03SimpleStatement> targets;
            if ((stm = (Op03SimpleStatement)outStatements.get(x)).getStatement().getClass() != IfStatement.class) continue;
            if ((targets = stm.getTargets()).get(0) == (next = (Op03SimpleStatement)outStatements.get(x + 1))) continue;
            if (targets.get(1) == next) {
                IfStatement ifStatement = (IfStatement)stm.getStatement();
                ifStatement.setCondition(ifStatement.getCondition().getNegated().simplify());
                Op03SimpleStatement a = targets.get(0);
                Op03SimpleStatement b = targets.get(1);
                targets.set(0, b);
                targets.set(1, a);
                continue;
            }
            patched = true;
            Op03SimpleStatement extra = new Op03SimpleStatement(stm.getBlockIdentifiers(), new GotoStatement(), stm.getSSAIdentifiers(), stm.getIndex().justAfter());
            Op03SimpleStatement target0 = targets.get(0);
            extra.addSource(stm);
            extra.addTarget(target0);
            stm.replaceTarget(target0, extra);
            target0.replaceSource(stm, extra);
            outStatements.add((Op03SimpleStatement)extra);
        }
        if (patched) {
            outStatements = Op03SimpleStatement.renumber(outStatements);
        }
        Op03Blocks.stripTryBlockAliases(outStatements, tryBlockAliases);
        if (!((Boolean)options.getOption(OptionsImpl.ALLOW_CORRECTING)).booleanValue() || !Op03Blocks.stripBackExceptions(outStatements)) return Op03SimpleStatement.removeUnreachableCode(outStatements, true);
        comments.addComment(DecompilerComment.TRY_BACKEDGE_REMOVED);
        return Op03SimpleStatement.removeUnreachableCode(outStatements, true);
    }

    private static boolean stripBackExceptions(List<Op03SimpleStatement> statements) {
        boolean res = false;
        List<Op03SimpleStatement> tryStatements = Functional.filter(statements, new Op03SimpleStatement.ExactTypeFilter(TryStatement.class));
        for (Op03SimpleStatement statement : tryStatements) {
            List<Op03SimpleStatement> remainingTargets;
            TryStatement tryStatement = (TryStatement)statement.getStatement();
            if (statement.getTargets().isEmpty()) continue;
            Op03SimpleStatement fallThrough = statement.getTargets().get(0);
            List<Op03SimpleStatement> backTargets = Functional.filter(statement.getTargets(), new Op03SimpleStatement.IsForwardJumpTo(statement.getIndex()));
            boolean thisRes = false;
            Iterator<Op03SimpleStatement> i$ = backTargets.iterator();
            while (i$.hasNext()) {
                Op03SimpleStatement backTarget;
                Statement backTargetStatement;
                if ((backTargetStatement = (backTarget = i$.next()).getStatement()).getClass() != CatchStatement.class) continue;
                CatchStatement catchStatement = (CatchStatement)backTargetStatement;
                catchStatement.getExceptions().removeAll(tryStatement.getEntries());
                backTarget.removeSource(statement);
                statement.removeTarget(backTarget);
                thisRes = true;
            }
            if (!thisRes) continue;
            res = true;
            if ((remainingTargets = statement.getTargets()).size() != 1 || remainingTargets.get(0) != fallThrough) continue;
            statement.nopOut();
        }
        return res;
    }

    private static void patch(Block3 a, Block3 b) {
        List<Op03SimpleStatement> content = a.content;
        Op03SimpleStatement last = content.get(content.size() - 1);
        Statement statement = last.getStatement();
        if (last.getTargets().isEmpty() || !statement.fallsToNext()) {
            return;
        }
        Op03SimpleStatement fallThroughTarget = last.getTargets().get(0);
        if (b != null && fallThroughTarget == b.getStart()) {
            return;
        }
        Op03SimpleStatement newGoto = new Op03SimpleStatement(last.getBlockIdentifiers(), new GotoStatement(), last.getIndex().justAfter());
        a.append(newGoto);
        last.replaceTarget(fallThroughTarget, newGoto);
        newGoto.addSource(last);
        newGoto.addTarget(fallThroughTarget);
        fallThroughTarget.replaceSource(last, newGoto);
    }

    static class Block3
    implements Comparable<Block3> {
        InstrIndex startIndex;
        List<Op03SimpleStatement> content = ListFactory.newList();
        Set<Block3> sources = new LinkedHashSet<Block3>();
        Set<Block3> originalSources = new LinkedHashSet<Block3>();
        Set<Block3> targets = new LinkedHashSet<Block3>();

        public Block3(Op03SimpleStatement s) {
            this.startIndex = s.getIndex();
            this.content.add(s);
        }

        public void append(Op03SimpleStatement s) {
            this.content.add(s);
        }

        public Op03SimpleStatement getStart() {
            return this.content.get(0);
        }

        public Op03SimpleStatement getEnd() {
            return this.content.get(this.content.size() - 1);
        }

        public void addSources(List<Block3> sources) {
            for (Block3 source : sources) {
                if (source != null) continue;
                throw new IllegalStateException();
            }
            this.sources.addAll(sources);
            this.originalSources.addAll(sources);
        }

        public void addSource(Block3 source) {
            this.sources.add(source);
            this.originalSources.add(source);
        }

        public void setTargets(List<Block3> targets) {
            this.targets.clear();
            this.targets.addAll(targets);
        }

        public void addTargets(List<Block3> targets) {
            for (Block3 source : targets) {
                if (source != null) continue;
                throw new IllegalStateException();
            }
            this.targets.addAll(targets);
        }

        public void addTarget(Block3 source) {
            this.targets.add(source);
        }

        @Override
        public int compareTo(Block3 other) {
            return this.startIndex.compareTo(other.startIndex);
        }

        public String toString() {
            return "(" + this.content.size() + ")[" + this.sources.size() + "/" + this.originalSources.size() + "," + this.targets.size() + "] " + this.getStart().toString();
        }

        private List<Op03SimpleStatement> getContent() {
            return this.content;
        }

        public void copySources() {
            this.sources.clear();
            this.sources.addAll(this.originalSources);
        }

        public Block3 getLastUnconditionalBackjumpToHere(Map<Block3, Integer> idxLut) {
            int thisIdx = idxLut.get(this);
            int best = -1;
            Block3 bestSource = null;
            for (Block3 source : this.originalSources) {
                int idxSource;
                if (source.getEnd().getStatement().getClass() != GotoStatement.class || (idxSource = idxLut.get(source).intValue()) <= best || idxSource <= thisIdx) continue;
                bestSource = source;
                best = idxSource;
            }
            return bestSource;
        }
    }

}

