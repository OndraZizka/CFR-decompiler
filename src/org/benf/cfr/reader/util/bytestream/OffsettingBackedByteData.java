/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.bytestream;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import org.benf.cfr.reader.util.bytestream.AbstractBackedByteData;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.bytestream.OffsetBackedByteData;
import org.benf.cfr.reader.util.bytestream.OffsettingByteData;

public class OffsettingBackedByteData
extends AbstractBackedByteData
implements OffsettingByteData {
    final byte[] data;
    final int originalOffset;
    int mutableOffset;

    public OffsettingBackedByteData(byte[] data, long offset) {
        this.data = data;
        this.originalOffset = (int)offset;
        this.mutableOffset = 0;
    }

    @Override
    public void advance(long offset) {
        this.mutableOffset = (int)((long)this.mutableOffset + offset);
    }

    @Override
    public void rewind(long offset) {
        this.mutableOffset = (int)((long)this.mutableOffset - offset);
    }

    @Override
    public long getOffset() {
        return this.mutableOffset;
    }

    @Override
    public DataInputStream rawDataAsStream(int start, int len) {
        return new DataInputStream(new ByteArrayInputStream(this.data, start + this.originalOffset + this.mutableOffset, len));
    }

    @Override
    public ByteData getOffsetData(long offset) {
        return new OffsetBackedByteData(this.data, (long)(this.originalOffset + this.mutableOffset) + offset);
    }

    @Override
    public OffsettingByteData getOffsettingOffsetData(long offset) {
        return new OffsettingBackedByteData(this.data, (long)(this.originalOffset + this.mutableOffset) + offset);
    }

    @Override
    public byte getS1At(long o) {
        return this.data[(int)((long)(this.originalOffset + this.mutableOffset) + o)];
    }

    @Override
    public byte[] getBytesAt(int count, long offset) {
        byte[] res = new byte[count];
        System.arraycopy(this.data, (int)((long)(this.originalOffset + this.mutableOffset) + offset), res, 0, count);
        return res;
    }
}

