/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types.discovery;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;

public enum CastAction {
    None{

        @Override
        public Expression performCastAction(Expression orig, InferredJavaType tgtType) {
            return orig;
        }
    }
    ,
    InsertExplicit{

        @Override
        public Expression performCastAction(Expression orig, InferredJavaType tgtType) {
            if (tgtType.getJavaTypeInstance() != RawJavaType.BOOLEAN) return new CastExpression(tgtType, orig);
            return orig;
        }
    };
    

    private CastAction() {
    }

    public abstract Expression performCastAction(Expression var1, InferredJavaType var2);

    /* synthetic */ CastAction(String x2, int n2,  n2) {
        this();
    }

}

