/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entityfactories;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.util.KnowsRawName;
import org.benf.cfr.reader.util.KnowsRawSize;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.bytestream.OffsettingByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class ContiguousEntityFactory {
    public static <X extends KnowsRawSize> long build(ByteData raw, short count, List<X> tgt, UnaryFunction<ByteData, X> func) {
        OffsettingByteData data = raw.getOffsettingOffsetData(0);
        short x = 0;
        while (x < count) {
            KnowsRawSize tmp = (KnowsRawSize)func.invoke(data);
            tgt.add((X)tmp);
            data.advance(tmp.getRawByteLength());
            x = (short)(x + 1);
        }
        return data.getOffset();
    }

    public static <X> long buildSized(ByteData raw, short count, int itemLength, List<X> tgt, UnaryFunction<ByteData, X> func) {
        OffsettingByteData data = raw.getOffsettingOffsetData(0);
        short x = 0;
        while (x < count) {
            X tmp = func.invoke(data);
            tgt.add(tmp);
            data.advance(itemLength);
            x = (short)(x + 1);
        }
        return data.getOffset();
    }

    public static <X extends KnowsRawName> Map<String, X> addToMap(Map<String, X> tgt, List<X> source) {
        for (KnowsRawName item : source) {
            tgt.put(item.getRawName(), (X)item);
        }
        return tgt;
    }
}

