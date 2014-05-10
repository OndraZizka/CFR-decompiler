/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public abstract class AbstractExpression
implements Expression {
    private final InferredJavaType inferredJavaType;

    public AbstractExpression(InferredJavaType inferredJavaType) {
        this.inferredJavaType = inferredJavaType;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.inferredJavaType.getJavaTypeInstance());
    }

    @Override
    public boolean canPushDownInto() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public Expression pushDown(Expression toPush, Expression parent) {
        throw new ConfusedCFRException("Push down not supported.");
    }

    @Override
    public InferredJavaType getInferredJavaType() {
        return this.inferredJavaType;
    }

    @Override
    public Expression outerDeepClone(CloneHelper cloneHelper) {
        return cloneHelper.replaceOrClone(this);
    }

    public final String toString() {
        return ToStringDumper.toString(this);
    }

    @Override
    public boolean canThrow(ExceptionCheck caught) {
        return true;
    }

    public abstract boolean equals(Object var1);

    @Override
    public Literal getComputedLiteral(Map<LValue, Literal> display) {
        return null;
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

