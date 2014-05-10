/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util;

import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.SetFactory;

public class BoxingHelper {
    private static Set<Pair<String, String>> unboxing = SetFactory.newSet(new Pair[]{Pair.make("java.lang.Integer", "intValue"), Pair.make("java.lang.Long", "longValue"), Pair.make("java.lang.Double", "doubleValue"), Pair.make("java.lang.Short", "shortValue"), Pair.make("java.lang.Boolean", "booleanValue")});
    private static Set<Pair<String, String>> boxing = SetFactory.newSet(new Pair[]{Pair.make("java.lang.Integer", "valueOf"), Pair.make("java.lang.Long", "valueOf"), Pair.make("java.lang.Double", "valueOf"), Pair.make("java.lang.Short", "valueOf"), Pair.make("java.lang.Boolean", "valueOf")});

    public static Expression sugarUnboxing(MemberFunctionInvokation memberFunctionInvokation) {
        String name = memberFunctionInvokation.getName();
        JavaTypeInstance type = memberFunctionInvokation.getObject().getInferredJavaType().getJavaTypeInstance();
        String rawTypeName = type.getRawName();
        Pair<String, String> testPair = Pair.make(rawTypeName, name);
        if (!BoxingHelper.unboxing.contains(testPair)) return memberFunctionInvokation;
        return memberFunctionInvokation.getObject();
    }

    public static Expression sugarBoxing(StaticFunctionInvokation staticFunctionInvokation) {
        Pair<String, String> testPair;
        JavaTypeInstance argType;
        String rawTypeName;
        String name = staticFunctionInvokation.getName();
        JavaTypeInstance type = staticFunctionInvokation.getClazz();
        if (staticFunctionInvokation.getArgs().size() != 1) {
            return staticFunctionInvokation;
        }
        Expression arg1 = staticFunctionInvokation.getArgs().get(0);
        if (!BoxingHelper.boxing.contains(testPair = Pair.make(rawTypeName = type.getRawName(), name)) || !(argType = arg1.getInferredJavaType().getJavaTypeInstance()).implicitlyCastsTo(type, null)) return staticFunctionInvokation;
        return staticFunctionInvokation.getArgs().get(0);
    }
}

