/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.lvalue;

import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public abstract class AbstractLValue
implements LValue {
    private InferredJavaType inferredJavaType;

    public AbstractLValue(InferredJavaType inferredJavaType) {
        this.inferredJavaType = inferredJavaType;
    }

    protected String typeToString() {
        return this.inferredJavaType.toString();
    }

    @Override
    public InferredJavaType getInferredJavaType() {
        return this.inferredJavaType;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.inferredJavaType.getJavaTypeInstance());
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
    }

    @Override
    public LValue outerDeepClone(CloneHelper cloneHelper) {
        return cloneHelper.replaceOrClone(this);
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return caught.mightCatchUnchecked();
    }

    public final String toString() {
        return ToStringDumper.toString(this);
    }

    @Override
    public final Dumper dump(Dumper d) {
        return this.dumpWithOuterPrecedence(d, Precedence.WEAKEST);
    }

    @Override
    public abstract Precedence getPrecedence();

    public abstract Dumper dumpInner(Dumper var1);

    @Override
    public final Dumper dumpWithOuterPrecedence(Dumper d, Precedence outerP) {
        Precedence innerP = this.getPrecedence();
        int cmp = innerP.compareTo((Enum)outerP);
        if (!(cmp <= 0 && (cmp != 0 || innerP.isLtoR()))) {
            d.print("(");
            this.dumpInner(d);
            d.print(")");
        } else {
            this.dumpInner(d);
        }
        return d;
    }
}

