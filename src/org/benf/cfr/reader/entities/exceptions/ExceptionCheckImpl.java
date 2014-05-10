/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.exceptions;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationSimple;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.SetUtil;

public class ExceptionCheckImpl
implements ExceptionCheck {
    private final Set<JavaRefTypeInstance> caughtChecked = SetFactory.newSet();
    private final Set<JavaRefTypeInstance> caughtUnchecked = SetFactory.newSet();
    private final boolean mightUseUnchecked;
    private final boolean missingInfo;
    private final DCCommonState dcCommonState;
    private final JavaRefTypeInstance runtimeExceptionType;

    public ExceptionCheckImpl(DCCommonState dcCommonState, Set<JavaRefTypeInstance> caught) {
        this.dcCommonState = dcCommonState;
        this.runtimeExceptionType = dcCommonState.getClassTypeOrNull("java/lang/RuntimeException.class");
        if (this.runtimeExceptionType == null) {
            this.mightUseUnchecked = true;
            this.missingInfo = true;
            return;
        }
        boolean lmightUseUnchecked = false;
        boolean lmissinginfo = false;
        Iterator<JavaRefTypeInstance> i$ = caught.iterator();
        while (i$.hasNext()) {
            BindingSuperContainer superContainer;
            Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> supers;
            JavaRefTypeInstance ref;
            if ((superContainer = (ref = i$.next()).getBindingSupers()) == null) {
                lmightUseUnchecked = true;
                lmissinginfo = true;
                continue;
            }
            if ((supers = superContainer.getBoundSuperClasses()) == null) {
                lmightUseUnchecked = true;
                lmissinginfo = true;
                continue;
            }
            if (supers.containsKey(this.runtimeExceptionType)) {
                lmightUseUnchecked = true;
                this.caughtUnchecked.add(ref);
                continue;
            }
            this.caughtChecked.add(ref);
        }
        this.mightUseUnchecked = lmightUseUnchecked;
        this.missingInfo = lmissinginfo;
    }

    private boolean checkAgainstInternal(Set<? extends JavaTypeInstance> thrown) {
        if (thrown.isEmpty()) {
            return false;
        }
        for (JavaTypeInstance thrownType : thrown) {
            try {
                Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSuperClasses;
                BindingSuperContainer bindingSuperContainer;
                ClassFile thrownClassFile = this.dcCommonState.getClassFile(thrownType);
                if (thrownClassFile == null) {
                    return true;
                }
                if ((bindingSuperContainer = thrownClassFile.getBindingSupers()) == null) {
                    return true;
                }
                if ((boundSuperClasses = bindingSuperContainer.getBoundSuperClasses()) == null) {
                    return true;
                }
                if (!SetUtil.hasIntersection(this.caughtChecked, boundSuperClasses.keySet())) continue;
                return true;
            }
            catch (CannotLoadClassException e) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkAgainst(Set<? extends JavaTypeInstance> thrown) {
        try {
            return this.checkAgainstInternal(thrown);
        }
        catch (Exception e) {
            return true;
        }
    }

    @Override
    public boolean checkAgainst(AbstractFunctionInvokation functionInvokation) {
        if (this.mightUseUnchecked) {
            return true;
        }
        JavaTypeInstance type = functionInvokation.getClassTypeInstance();
        try {
            ClassFile classFile = this.dcCommonState.getClassFile(type);
            Method method = classFile.getMethodByPrototype(functionInvokation.getMethodPrototype());
            return this.checkAgainstInternal((Set<? extends JavaTypeInstance>)method.getThrownTypes());
        }
        catch (NoSuchMethodException e) {
            return true;
        }
        catch (CannotLoadClassException e) {
            return true;
        }
    }

    @Override
    public boolean checkAgainstException(Expression expression) {
        Set<JavaRefTypeInstance> throwingBases;
        if (this.missingInfo) {
            return true;
        }
        if (!(expression instanceof ConstructorInvokationSimple)) {
            return true;
        }
        ConstructorInvokationSimple constructorInvokation = (ConstructorInvokationSimple)expression;
        JavaTypeInstance type = constructorInvokation.getTypeInstance();
        Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSuperClasses = null;
        try {
            BindingSuperContainer bindingSuperContainer;
            ClassFile classFile = this.dcCommonState.getClassFile(type);
            if (classFile == null) {
                return true;
            }
            if ((bindingSuperContainer = classFile.getBindingSupers()) == null) {
                return true;
            }
            if ((boundSuperClasses = bindingSuperContainer.getBoundSuperClasses()) == null) {
                return true;
            }
        }
        catch (CannotLoadClassException e) {
            return true;
        }
        if (SetUtil.hasIntersection(this.caughtChecked, throwingBases = boundSuperClasses.keySet())) {
            return true;
        }
        if (!SetUtil.hasIntersection(this.caughtUnchecked, throwingBases)) return false;
        return true;
    }

    @Override
    public boolean mightCatchUnchecked() {
        return this.mightUseUnchecked;
    }
}

