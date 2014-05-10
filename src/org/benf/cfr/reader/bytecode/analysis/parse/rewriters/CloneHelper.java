/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.rewriters;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;

public class CloneHelper {
    private final Map<Expression, Expression> expressionMap;
    private final Map<LValue, LValue> lValueMap;

    public CloneHelper() {
        this.expressionMap = MapFactory.newMap();
        this.lValueMap = MapFactory.newMap();
    }

    public CloneHelper(Map<Expression, Expression> expressionMap, Map<LValue, LValue> lValueMap) {
        this.expressionMap = expressionMap;
        this.lValueMap = lValueMap;
    }

    public <X extends DeepCloneable<X>> List<X> replaceOrClone(List<X> in) {
        List res = ListFactory.newList();
        for (DeepCloneable i : in) {
            res.add(i.outerDeepClone(this));
        }
        return res;
    }

    public Expression replaceOrClone(Expression source) {
        Expression replacement = this.expressionMap.get(source);
        if (replacement != null) return replacement;
        return (Expression)source.deepClone(this);
    }

    public LValue replaceOrClone(LValue source) {
        LValue replacement = this.lValueMap.get(source);
        if (replacement != null) return replacement;
        return (LValue)source.deepClone(this);
    }
}

