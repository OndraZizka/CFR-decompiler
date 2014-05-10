/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;

public class BindingSuperContainer {
    public static BindingSuperContainer POISON = new BindingSuperContainer(null, null, null);
    private final ClassFile thisClass;
    private final Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSuperClasses;
    private final Map<JavaRefTypeInstance, Route> boundSuperRoute;

    public BindingSuperContainer(ClassFile thisClass, Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSuperClasses, Map<JavaRefTypeInstance, Route> boundSuperRoute) {
        this.thisClass = thisClass;
        this.boundSuperClasses = boundSuperClasses;
        this.boundSuperRoute = boundSuperRoute;
    }

    public JavaTypeInstance getBoundAssignable(JavaGenericRefTypeInstance assignable, JavaGenericRefTypeInstance superType) {
        JavaRefTypeInstance baseKey = superType.getDeGenerifiedType();
        JavaRefTypeInstance assignableKey = assignable.getDeGenerifiedType();
        JavaGenericRefTypeInstance reboundBase = this.boundSuperClasses.get(baseKey);
        if (reboundBase == null) {
            return assignable;
        }
        GenericTypeBinder genericTypeBinder = GenericTypeBinder.extractBindings(reboundBase, superType);
        JavaGenericRefTypeInstance boundAssignable = assignable.getBoundInstance(genericTypeBinder);
        return boundAssignable;
    }

    public boolean containsBase(JavaTypeInstance possBase) {
        if (possBase instanceof JavaRefTypeInstance) return this.boundSuperClasses.containsKey(possBase);
        return false;
    }

    public Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> getBoundSuperClasses() {
        return this.boundSuperClasses;
    }

    public JavaGenericRefTypeInstance getBoundSuperForBase(JavaTypeInstance possBase) {
        if (possBase instanceof JavaRefTypeInstance) return this.boundSuperClasses.get(possBase);
        return null;
    }

    public Map<JavaRefTypeInstance, Route> getBoundSuperRoute() {
        return this.boundSuperRoute;
    }

    public static enum Route {
        IDENTITY,
        EXTENSION,
        INTERFACE;
        

        private Route() {
        }
    }

}

