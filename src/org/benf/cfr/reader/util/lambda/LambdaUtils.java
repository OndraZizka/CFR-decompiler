/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.lambda;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodHandle;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.util.ConfusedCFRException;

public class LambdaUtils {
    private static TypedLiteral.LiteralType getLiteralType(Expression e) {
        if (!(e instanceof Literal)) {
            throw new IllegalArgumentException("Expecting literal");
        }
        TypedLiteral t = ((Literal)e).getValue();
        return t.getType();
    }

    public static ConstantPoolEntryMethodHandle getHandle(Expression e) {
        TypedLiteral t;
        if (!(e instanceof Literal)) {
            throw new IllegalArgumentException("Expecting literal");
        }
        if ((t = ((Literal)e).getValue()).getType() == TypedLiteral.LiteralType.MethodHandle) return (ConstantPoolEntryMethodHandle)t.getValue();
        throw new IllegalArgumentException("Expecting method handle");
    }

    private static ConstantPoolEntryMethodType getType(Expression e) {
        TypedLiteral t;
        if (!(e instanceof Literal)) {
            throw new IllegalArgumentException("Expecting literal");
        }
        if ((t = ((Literal)e).getValue()).getType() == TypedLiteral.LiteralType.MethodType) return (ConstantPoolEntryMethodType)t.getValue();
        throw new IllegalArgumentException("Expecting method type");
    }

    public static MethodPrototype getLiteralProto(Expression arg) {
        TypedLiteral.LiteralType flavour = LambdaUtils.getLiteralType(arg);
        switch (flavour) {
            case MethodHandle: {
                ConstantPoolEntryMethodHandle targetFnHandle = LambdaUtils.getHandle(arg);
                ConstantPoolEntryMethodRef targetMethRef = targetFnHandle.getMethodRef();
                return targetMethRef.getMethodPrototype();
            }
            case MethodType: {
                ConstantPoolEntryMethodType targetFnType = LambdaUtils.getType(arg);
                ConstantPoolEntryUTF8 descriptor = targetFnType.getDescriptor();
                return ConstantPoolUtils.parseJavaMethodPrototype(null, null, null, false, Method.MethodConstructor.NOT, descriptor, targetFnType.getCp(), false, null);
            }
        }
        throw new ConfusedCFRException("Can't understand this lambda - disable lambdas.");
    }

}

