/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageInformationEmpty;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ToStringDumper
implements Dumper {
    private int indent;
    private boolean atStart = true;
    private boolean pendingCR = false;
    private final StringBuilder sb = new StringBuilder();
    private final TypeUsageInformation typeUsageInformation = new TypeUsageInformationEmpty();
    private final Set<JavaTypeInstance> emitted = SetFactory.newSet();

    public static String toString(Dumpable d) {
        return new ToStringDumper().dump(d).toString();
    }

    @Override
    public void printLabel(String s) {
        this.processPendingCR();
        this.sb.append(s).append(":\n");
        this.atStart = true;
    }

    @Override
    public void enqueuePendingCarriageReturn() {
        this.pendingCR = true;
    }

    @Override
    public Dumper removePendingCarriageReturn() {
        this.pendingCR = false;
        return this;
    }

    private void processPendingCR() {
        if (!this.pendingCR) return;
        this.sb.append('\u000a');
        this.atStart = true;
        this.pendingCR = false;
    }

    @Override
    public Dumper print(String s) {
        this.processPendingCR();
        this.doIndent();
        this.sb.append(s);
        this.atStart = false;
        if (!s.endsWith("\n")) return this;
        this.atStart = true;
        return this;
    }

    @Override
    public Dumper print(char c) {
        return this.print("" + c);
    }

    @Override
    public Dumper newln() {
        this.sb.append("\n");
        this.atStart = true;
        return this;
    }

    @Override
    public Dumper endCodeln() {
        this.sb.append(";\n");
        this.atStart = true;
        return this;
    }

    private void doIndent() {
        if (!this.atStart) {
            return;
        }
        String indents = "    ";
        for (int x = 0; x < this.indent; ++x) {
            this.sb.append(indents);
        }
        this.atStart = false;
    }

    @Override
    public void line() {
        this.sb.append("\n// -------------------\n");
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
    public Dumper dump(Dumpable d) {
        if (d == null) {
            this.print("null");
            return this;
        }
        d.dump(this);
        return this;
    }

    @Override
    public TypeUsageInformation getTypeUsageInformation() {
        return this.typeUsageInformation;
    }

    @Override
    public Dumper dump(JavaTypeInstance javaTypeInstance) {
        javaTypeInstance.dumpInto(this, this.typeUsageInformation);
        return this;
    }

    public String toString() {
        return this.sb.toString();
    }

    @Override
    public void addSummaryError(Method method, String s) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean canEmitClass(JavaTypeInstance type) {
        return this.emitted.add(type);
    }
}

