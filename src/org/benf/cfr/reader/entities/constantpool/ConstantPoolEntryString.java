/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import org.benf.cfr.reader.bytecode.analysis.parse.utils.QuotingUtils;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryLiteral;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryString
extends AbstractConstantPoolEntry
implements ConstantPoolEntryLiteral {
    private final long OFFSET_OF_STRING_INDEX = 1;
    private final long stringIndex;
    private transient String string;

    public ConstantPoolEntryString(ConstantPool cp, ByteData data) {
        super(cp);
        this.stringIndex = data.getS2At(1);
    }

    @Override
    public long getRawByteLength() {
        return 3;
    }

    @Override
    public void dump(Dumper d) {
        d.print("String " + this.getValue());
    }

    public String getValue() {
        if (this.string != null) return this.string;
        this.string = QuotingUtils.enquoteString(this.getCp().getUTF8Entry((int)this.stringIndex).getRawValue());
        return this.string;
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }
}

