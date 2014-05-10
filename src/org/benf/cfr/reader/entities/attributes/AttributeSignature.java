/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeSignature
extends Attribute {
    public static final String ATTRIBUTE_NAME = "Signature";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_REMAINDER = 6;
    private final int length;
    private final ConstantPool cp;
    private final ConstantPoolEntryUTF8 signature;

    public AttributeSignature(ByteData raw, ConstantPool cp) {
        this.length = raw.getS4At(2);
        this.cp = cp;
        this.signature = cp.getUTF8Entry(raw.getS2At(6));
    }

    @Override
    public String getRawName() {
        return "Signature";
    }

    @Override
    public Dumper dump(Dumper d) {
        return d.print("Signature : " + this.signature);
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }

    public ConstantPoolEntryUTF8 getSignature() {
        return this.signature;
    }
}

