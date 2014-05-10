/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractConstructorInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.ComparableUnderEC;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperAnonymousInner;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.output.Dumper;

public class ConstructorInvokationAnoynmousInner
extends AbstractConstructorInvokation {
    private final MemberFunctionInvokation constructorInvokation;
    private final ClassFile classFile;

    public ConstructorInvokationAnoynmousInner(MemberFunctionInvokation constructorInvokation, InferredJavaType inferredJavaType, List<Expression> args, DCCommonState dcCommonState) {
        super(inferredJavaType, constructorInvokation.getFunction(), args);
        this.constructorInvokation = constructorInvokation;
        ClassFile classFile = null;
        try {
            classFile = dcCommonState.getClassFile(constructorInvokation.getMethodPrototype().getReturnType());
        }
        catch (CannotLoadClassException e) {
            // empty catch block
        }
        this.classFile = classFile;
        if (classFile == null) return;
        classFile.noteAnonymousUse(this);
    }

    protected ConstructorInvokationAnoynmousInner(ConstructorInvokationAnoynmousInner other, CloneHelper cloneHelper) {
        super(other, cloneHelper);
        this.constructorInvokation = (MemberFunctionInvokation)cloneHelper.replaceOrClone(other.constructorInvokation);
        this.classFile = other.classFile;
    }

    @Override
    public Expression deepClone(CloneHelper cloneHelper) {
        return new ConstructorInvokationAnoynmousInner(this, cloneHelper);
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        ConstantPool cp = this.constructorInvokation.getCp();
        ClassFile anonymousClassFile = null;
        JavaTypeInstance typeInstance = this.getTypeInstance();
        try {
            anonymousClassFile = cp.getDCCommonState().getClassFile(typeInstance);
        }
        catch (CannotLoadClassException e) {
            anonymousClassFile = this.classFile;
        }
        if (anonymousClassFile != this.classFile) {
            throw new IllegalStateException("Inner class got unexpected class file - revert this change");
        }
        d.print("new ");
        ClassFileDumperAnonymousInner cfd = new ClassFileDumperAnonymousInner();
        List args = this.getArgs();
        MethodPrototype prototype = this.constructorInvokation.getMethodPrototype();
        try {
            if (this.classFile != null) {
                prototype = this.classFile.getMethodByPrototype(prototype).getMethodPrototype();
            }
        }
        catch (NoSuchMethodException e) {
            // empty catch block
        }
        cfd.dumpWithArgs(this.classFile, prototype, args, false, d);
        d.removePendingCarriageReturn();
        return d;
    }

    public Dumper dumpForEnum(Dumper d) {
        ClassFile anonymousClassFile = this.classFile;
        ClassFileDumperAnonymousInner cfd = new ClassFileDumperAnonymousInner();
        List args = this.getArgs();
        return cfd.dumpWithArgs(anonymousClassFile, null, args.subList(2, args.size()), true, d);
    }

    @Override
    public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
        if (!(o instanceof ConstructorInvokationAnoynmousInner)) {
            return false;
        }
        if (!super.equivalentUnder(o, constraint)) {
            return false;
        }
        ConstructorInvokationAnoynmousInner other = (ConstructorInvokationAnoynmousInner)o;
        if (constraint.equivalent(this.constructorInvokation, other.constructorInvokation)) return true;
        return false;
    }
}

