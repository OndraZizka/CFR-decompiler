/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.DeepCloneable;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.util.output.Dumper;

public class SuperFunctionInvokation
extends AbstractFunctionInvokation {
    public SuperFunctionInvokation(ConstantPool cp, ConstantPoolEntryMethodRef function, MethodPrototype methodPrototype, Expression object, List<Expression> args) {
        super(cp, function, methodPrototype, object, args);
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new SuperFunctionInvokation(this.getCp(), this.getFunction(), this.getMethodPrototype(), cloneHelper.replaceOrClone(this.getObject()), cloneHelper.replaceOrClone(this.getArgs()));
    }

    private boolean isSyntheticThisFirstArg() {
        JavaTypeInstance superType = this.getFunction().getClassEntry().getTypeInstance();
        return superType.getInnerClassHereInfo().isHideSyntheticThis();
    }

    public boolean isEmptyIgnoringSynthetics() {
        return this.getArgs().size() == (this.isSyntheticThisFirstArg() ? 1 : 0);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        MethodPrototype methodPrototype = this.getMethodPrototype();
        List args = this.getArgs();
        if (methodPrototype.getName().equals("<init>")) {
            d.print("super(");
        } else {
            d.print("super.").print(methodPrototype.getName()).print("(");
        }
        boolean first = true;
        int n = this.isSyntheticThisFirstArg() ? 1 : 0;
        for (int x = start = (v35284); x < args.size(); ++x) {
            Expression arg = (Expression)args.get(x);
            if (!first) {
                d.print(", ");
            }
            first = false;
            methodPrototype.dumpAppropriatelyCastedArgumentString(arg, x, d);
        }
        d.print(")");
        return d;
    }

    @Override
    public String getName() {
        return "super";
    }
}

