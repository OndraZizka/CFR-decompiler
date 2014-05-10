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
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumper;

public class MemberFunctionInvokation
extends AbstractFunctionInvokation {
    private final String name;
    private final boolean special;
    private final boolean isInitMethod;

    public MemberFunctionInvokation(ConstantPool cp, ConstantPoolEntryMethodRef function, MethodPrototype methodPrototype, Expression object, boolean special, List<Expression> args) {
        super(cp, function, methodPrototype, object, args);
        ConstantPoolEntryNameAndType nameAndType = function.getNameAndTypeEntry();
        String funcName = nameAndType.getName().getValue();
        this.isInitMethod = function.isInitMethod();
        this.name = funcName;
        this.special = special;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new MemberFunctionInvokation(this.getCp(), this.getFunction(), this.getMethodPrototype(), cloneHelper.replaceOrClone(this.getObject()), this.special, cloneHelper.replaceOrClone(this.getArgs()));
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        super.collectTypeUsages(collector);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        Object comment = null;
        this.getObject().dumpWithOuterPrecedence(d, this.getPrecedence());
        MethodPrototype methodPrototype = this.getMethodPrototype();
        if (!this.isInitMethod) {
            d.print("." + this.name);
        }
        d.print("(");
        List args = this.getArgs();
        boolean first = true;
        for (int x = 0; x < args.size(); ++x) {
            if (methodPrototype.isHiddenArg(x)) continue;
            Expression arg = (Expression)args.get(x);
            first = CommaHelp.comma(first, d);
            methodPrototype.dumpAppropriatelyCastedArgumentString(arg, x, d);
        }
        d.print(")");
        if (comment == null) return d;
        d.print(comment);
        return d;
    }

    public boolean isInitMethod() {
        return this.isInitMethod;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof MemberFunctionInvokation) return this.name.equals(((MemberFunctionInvokation)o).name);
        return false;
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (!super.equivalentUnder(o, constraint)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof MemberFunctionInvokation)) {
            return false;
        }
        MemberFunctionInvokation other = (MemberFunctionInvokation)o;
        return constraint.equivalent(this.name, other.name);
    }
}

