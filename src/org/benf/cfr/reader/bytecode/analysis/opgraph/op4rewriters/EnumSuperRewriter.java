/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.RedundantSuperRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.SuperFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.SetFactory;

public class EnumSuperRewriter
extends RedundantSuperRewriter {
    @Override
    protected List<Expression> getSuperArgs(WildcardMatch wcm) {
        List res = ListFactory.newList();
        res.add((WildcardMatch.ExpressionWildcard)wcm.getExpressionWildCard("enum_a"));
        res.add((WildcardMatch.ExpressionWildcard)wcm.getExpressionWildCard("enum_b"));
        return res;
    }

    private static LValue getLValue(WildcardMatch wcm, String name) {
        Expression e = wcm.getExpressionWildCard(name).getMatch();
        while (e instanceof CastExpression) {
            e = ((CastExpression)e).getChild();
        }
        if (e instanceof LValueExpression) return ((LValueExpression)e).getLValue();
        throw new IllegalStateException();
    }

    @Override
    protected Set<LValue> getDeclarationsToNop(WildcardMatch wcm) {
        Set res = SetFactory.newSet();
        res.add((LValue)EnumSuperRewriter.getLValue(wcm, "enum_a"));
        res.add((LValue)EnumSuperRewriter.getLValue(wcm, "enum_b"));
        return res;
    }

    @Override
    protected boolean canBeNopped(SuperFunctionInvokation superInvokation) {
        return true;
    }
}

