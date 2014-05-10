/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class TypeOverridingDumper
implements Dumper {
    private final Dumper delegate;
    private final TypeUsageInformation typeUsageInformation;

    public TypeOverridingDumper(Dumper delegate, TypeUsageInformation typeUsageInformation) {
        this.delegate = delegate;
        this.typeUsageInformation = typeUsageInformation;
    }

    @Override
    public TypeUsageInformation getTypeUsageInformation() {
        return this.typeUsageInformation;
    }

    @Override
    public void printLabel(String s) {
        this.delegate.printLabel(s);
    }

    @Override
    public void enqueuePendingCarriageReturn() {
        this.delegate.enqueuePendingCarriageReturn();
    }

    @Override
    public Dumper removePendingCarriageReturn() {
        this.delegate.removePendingCarriageReturn();
        return this;
    }

    @Override
    public Dumper print(String s) {
        this.delegate.print(s);
        return this;
    }

    @Override
    public Dumper print(char c) {
        this.delegate.print(c);
        return this;
    }

    @Override
    public Dumper newln() {
        this.delegate.newln();
        return this;
    }

    @Override
    public Dumper endCodeln() {
        this.delegate.endCodeln();
        return this;
    }

    @Override
    public void line() {
        this.delegate.line();
    }

    @Override
    public int getIndent() {
        return this.delegate.getIndent();
    }

    @Override
    public void indent(int diff) {
        this.delegate.indent(diff);
    }

    @Override
    public Dumper dump(JavaTypeInstance javaTypeInstance) {
        javaTypeInstance.dumpInto(this, this.typeUsageInformation);
        return this;
    }

    @Override
    public void dump(List<? extends Dumpable> d) {
        for (Dumpable dumpable : d) {
            dumpable.dump(this);
        }
    }

    @Override
    public Dumper dump(Dumpable d) {
        if (d != null) return d.dump(this);
        return this.print("null");
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    @Override
    public void addSummaryError(Method method, String s) {
        this.delegate.addSummaryError(method, s);
    }

    @Override
    public boolean canEmitClass(JavaTypeInstance type) {
        return this.delegate.canEmitClass(type);
    }
}

