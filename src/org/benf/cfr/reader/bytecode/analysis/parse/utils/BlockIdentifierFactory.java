/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;

public class BlockIdentifierFactory {
    int idx = 0;

    public BlockIdentifier getNextBlockIdentifier(BlockType blockType) {
        return new BlockIdentifier(this.idx++, blockType);
    }
}

