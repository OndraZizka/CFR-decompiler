/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured.statement;

import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.util.output.Dumper;

public class StructuredFakeDecompFailure
extends StructuredComment {
    private Exception e;

    public StructuredFakeDecompFailure(Exception e) {
        super("");
        this.e = e;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("{");
        dumper.indent(1);
        dumper.newln();
        dumper.print("// This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.").newln();
        dumper.print("// ").print(this.e.toString()).newln();
        for (StackTraceElement ste : this.e.getStackTrace()) {
            dumper.print("// ").print(ste.toString()).newln();
        }
        dumper.print("throw new IllegalStateException(\"Decompilation failed\")").endCodeln();
        dumper.indent(-1);
        dumper.print("}");
        dumper.enqueuePendingCarriageReturn();
        return dumper;
    }
}

