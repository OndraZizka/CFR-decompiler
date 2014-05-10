/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import java.nio.charset.Charset;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.QuotingUtils;
import org.benf.cfr.reader.entities.AbstractConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstantPoolEntryUTF8
extends AbstractConstantPoolEntry {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final long OFFSET_OF_LENGTH = 1;
    private static final long OFFSET_OF_DATA = 3;
    private final int length;
    private transient String value;
    private final String rawValue;
    private static int idx;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     */
    public ConstantPoolEntryUTF8(ConstantPool cp, ByteData data, Options options) {
        boolean needsUTF;
        byte[] bytes;
        String tmpValue;
        block8 : {
            super(cp);
            this.length = data.getU2At(1);
            bytes = data.getBytesAt(this.length, 3);
            char[] outchars = new char[bytes.length];
            tmpValue = null;
            int out = 0;
            needsUTF = false;
            try {
            }
            catch (IllegalArgumentException e) {
                break block8;
            }
            catch (IndexOutOfBoundsException e) {
                // empty catch block
                break block8;
            }
            for (int i = 0; i < bytes.length; ++i) {
                char x;
                if (((x = bytes[i]) & 128) == 0) {
                    outchars[out++] = x;
                    continue;
                }
                if ((x & 224) == 192) {
                    y = bytes[++i];
                    if ((y & 192) != 128) throw new IllegalArgumentException();
                    int val = ((x & 31) << 6) + (y & 63);
                    outchars[out++] = (char)val;
                    needsUTF = true;
                    continue;
                }
                if ((x & 240) != 224) throw new IllegalArgumentException();
                byte y = bytes[++i];
                byte z = bytes[++i];
                if ((y & 192) != 128) throw new IllegalArgumentException();
                if ((z & 192) != 128) throw new IllegalArgumentException();
                int val = ((x & 15) << 12) + ((y & 63) << 6) + (z & 63);
                outchars[out++] = (char)val;
                needsUTF = true;
            }
            tmpValue = new String(outchars, 0, out);
        }
        if (tmpValue == null) {
            tmpValue = new String(bytes, ConstantPoolEntryUTF8.UTF8_CHARSET);
        }
        if (tmpValue.length() > 512 && ((Boolean)options.getOption(OptionsImpl.HIDE_LONGSTRINGS)).booleanValue()) {
            tmpValue = "longStr" + ConstantPoolEntryUTF8.idx++ + "[" + tmpValue.substring(0, 10).replace('\u000d', '_').replace('\u000a', '_') + "]";
        }
        this.rawValue = tmpValue;
        if (needsUTF) return;
        this.value = tmpValue;
    }

    @Override
    public long getRawByteLength() {
        return 3 + this.length;
    }

    public String getValue() {
        if (this.value != null) return this.value;
        this.value = QuotingUtils.enquoteIdentifier(this.rawValue);
        return this.value;
    }

    public String getRawValue() {
        return this.rawValue;
    }

    @Override
    public void dump(Dumper d) {
        d.print("CONSTANT_UTF8 value=" + this.value);
    }

    public String toString() {
        return "ConstantUTF8[" + this.value + "]";
    }
}

