/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeSourceFile
extends Attribute {
    public static final String ATTRIBUTE_NAME = "SourceFile";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_REMAINDER = 6;
    private final int length;

    public AttributeSourceFile(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
    }

    @Override
    public String getRawName() {
        return "SourceFile";
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.print("SourceFile");
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }

    public String toString() {
        return "SourceFile";
    }
}

