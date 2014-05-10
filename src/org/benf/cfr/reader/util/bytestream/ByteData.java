/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.bytestream;

import org.benf.cfr.reader.util.bytestream.OffsettingByteData;

public interface ByteData {
    public byte getS1At(long var1);

    public short getU1At(long var1);

    public short getS2At(long var1);

    public int getU2At(long var1);

    public int getS4At(long var1);

    public double getDoubleAt(long var1);

    public float getFloatAt(long var1);

    public long getLongAt(long var1);

    public byte[] getBytesAt(int var1, long var2);

    public ByteData getOffsetData(long var1);

    public OffsettingByteData getOffsettingOffsetData(long var1);
}

