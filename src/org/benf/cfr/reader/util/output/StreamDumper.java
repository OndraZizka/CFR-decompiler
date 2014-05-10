/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public abstract class StreamDumper
implements Dumper {
    private final TypeUsageInformation typeUsageInformation;
    private int indent;
    private boolean atStart = true;
    private boolean pendingCR = false;
    private final Set<JavaTypeInstance> emitted = SetFactory.newSet();

    public StreamDumper(TypeUsageInformation typeUsageInformation) {
        this.typeUsageInformation = typeUsageInformation;
    }

    @Override
    public TypeUsageInformation getTypeUsageInformation() {
        return this.typeUsageInformation;
    }

    protected abstract void write(String var1);

    @Override
    public void printLabel(String s) {
        this.processPendingCR();
        this.write(s + ":\n");
        this.atStart = true;
    }

    @Override
    public void enqueuePendingCarriageReturn() {
        this.pendingCR = true;
    }

    @Override
    public Dumper removePendingCarriageReturn() {
        this.pendingCR = false;
        this.atStart = false;
        return this;
    }

    private void processPendingCR() {
        if (!this.pendingCR) return;
        this.write("\n");
        this.atStart = true;
        this.pendingCR = false;
    }

    @Override
    public Dumper print(String s) {
        this.processPendingCR();
        this.doIndent();
        boolean doNewLn = false;
        if (s.endsWith("\n")) {
            s = s.substring(0, s.length() - 1);
            doNewLn = true;
        }
        this.write(s);
        this.atStart = false;
        if (!doNewLn) return this;
        this.newln();
        return this;
    }

    @Override
    public Dumper print(char c) {
        return this.print("" + c);
    }

    @Override
    public Dumper newln() {
        if (this.pendingCR) {
            this.write("\n");
        }
        this.pendingCR = true;
        this.atStart = true;
        return this;
    }

    @Override
    public Dumper endCodeln() {
        this.write(";");
        this.pendingCR = true;
        this.atStart = true;
        return this;
    }

    private void doIndent() {
        if (!this.atStart) {
            return;
        }
        String indents = "    ";
        for (int x = 0; x < this.indent; ++x) {
            this.write(indents);
        }
        this.atStart = false;
    }

    @Override
    public void line() {
        this.write("\n// -------------------");
        this.atStart = true;
    }

    @Override
    public int getIndent() {
        return this.indent;
    }

    @Override
    public void indent(int diff) {
        this.indent+=diff;
    }

    @Override
    public void dump(List<? extends Dumpable> d) {
        for (Dumpable dumpable : d) {
            dumpable.dump(this);
        }
    }

    @Override
    public Dumper dump(JavaTypeInstance javaTypeInstance) {
        javaTypeInstance.dumpInto(this, this.typeUsageInformation);
        return this;
    }

    @Override
    public Dumper dump(Dumpable d) {
        if (d != null) return d.dump(this);
        return this.print("null");
    }

    @Override
    public boolean canEmitClass(JavaTypeInstance type) {
        return this.emitted.add(type);
    }
}

