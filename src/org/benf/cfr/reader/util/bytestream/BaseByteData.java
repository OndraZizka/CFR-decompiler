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
import org.benf.cfr.reader.util.bytestream.OffsettingBackedByteData;
import org.benf.cfr.reader.util.bytestream.OffsettingByteData;

public class BaseByteData
extends AbstractBackedByteData {
    private final byte[] data;

    public BaseByteData(byte[] data) {
        this.data = data;
    }

    @Override
    public DataInputStream rawDataAsStream(int start, int len) {
        return new DataInputStream(new ByteArrayInputStream(this.data, start, len));
    }

    @Override
    public ByteData getOffsetData(long offset) {
        return new OffsetBackedByteData(this.data, offset);
    }

    @Override
    public OffsettingByteData getOffsettingOffsetData(long offset) {
        return new OffsettingBackedByteData(this.data, offset);
    }

    @Override
    public byte[] getBytesAt(int count, long offset) {
        byte[] res = new byte[count];
        System.arraycopy(this.data, (int)offset, res, 0, count);
        return res;
    }

    @Override
    public byte getS1At(long o) {
        return this.data[(int)o];
    }
}

