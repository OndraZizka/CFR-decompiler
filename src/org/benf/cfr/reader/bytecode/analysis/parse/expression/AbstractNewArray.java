/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;

public abstract class AbstractNewArray
extends AbstractExpression {
    public AbstractNewArray(InferredJavaType inferredJavaType) {
        super(inferredJavaType);
    }

    public abstract int getNumDims();

    public abstract int getNumSizedDims();

    public abstract Expression getDimSize(int var1);

    public abstract JavaTypeInstance getInnerType();
}

