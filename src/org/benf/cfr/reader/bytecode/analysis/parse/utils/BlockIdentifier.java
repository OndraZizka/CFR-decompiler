/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.Predicate;

public class BlockIdentifier
implements Comparable<BlockIdentifier> {
    private final int index;
    private BlockType blockType;
    private int knownForeignReferences = 0;

    public BlockIdentifier(int index, BlockType blockType) {
        this.index = index;
        this.blockType = blockType;
    }

    public BlockType getBlockType() {
        return this.blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }

    public String getName() {
        return "block" + this.index;
    }

    public int getIndex() {
        return this.index;
    }

    public void addForeignRef() {
        ++this.knownForeignReferences;
    }

    public void releaseForeignRef() {
        --this.knownForeignReferences;
    }

    public boolean hasForeignReferences() {
        return this.knownForeignReferences > 0;
    }

    public String toString() {
        return "" + this.index + "[" + (Object)this.blockType + "]";
    }

    public static boolean blockIsOneOf(BlockIdentifier needle, Set<BlockIdentifier> haystack) {
        return haystack.contains(needle);
    }

    public static BlockIdentifier getOutermostContainedIn(Set<BlockIdentifier> endingBlocks, Set<BlockIdentifier> blocksInAtThisPoint) {
        List<BlockIdentifier> containedIn = Functional.filter(ListFactory.newList(endingBlocks), new Predicate<BlockIdentifier>(blocksInAtThisPoint){
            final /* synthetic */ Set val$blocksInAtThisPoint;

            @Override
            public boolean test(BlockIdentifier in) {
                return this.val$blocksInAtThisPoint.contains(in);
            }
        });
        if (containedIn.isEmpty()) {
            return null;
        }
        Collections.sort(containedIn);
        return containedIn.get(0);
    }

    public static BlockIdentifier getInnermostBreakable(List<BlockIdentifier> blocks) {
        BlockIdentifier res = null;
        for (BlockIdentifier block : blocks) {
            if (!block.blockType.isBreakable()) continue;
            res = block;
        }
        return res;
    }

    public static BlockIdentifier getOutermostEnding(List<BlockIdentifier> blocks, Set<BlockIdentifier> blocksEnding) {
        for (BlockIdentifier blockIdentifier : blocks) {
            if (!blocksEnding.contains(blockIdentifier)) continue;
            return blockIdentifier;
        }
        return null;
    }

    public static boolean isInAllBlocks(Collection<BlockIdentifier> mustBeIn, Collection<BlockIdentifier> isIn) {
        for (BlockIdentifier must : mustBeIn) {
            if (isIn.contains(must)) continue;
            return false;
        }
        return true;
    }

    public static boolean isInAnyBlock(Collection<BlockIdentifier> mustBeInOneOf, Collection<BlockIdentifier> isIn) {
        for (BlockIdentifier block : isIn) {
            if (!mustBeInOneOf.contains(block)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(BlockIdentifier blockIdentifier) {
        return this.index - blockIdentifier.index;
    }

}

