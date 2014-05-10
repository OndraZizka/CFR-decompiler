/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.util.MapFactory;

public class BoundSuperCollector {
    private final ClassFile classFile;
    private final Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSupers;
    private final Map<JavaRefTypeInstance, BindingSuperContainer.Route> boundSuperRoute;

    public BoundSuperCollector(ClassFile classFile) {
        this.classFile = classFile;
        this.boundSupers = MapFactory.newMap();
        this.boundSuperRoute = MapFactory.newMap();
    }

    public BindingSuperContainer getBoundSupers() {
        return new BindingSuperContainer(this.classFile, this.boundSupers, this.boundSuperRoute);
    }

    public void collect(JavaGenericRefTypeInstance boundBase, BindingSuperContainer.Route route) {
        JavaRefTypeInstance key = boundBase.getDeGenerifiedType();
        JavaGenericRefTypeInstance prev = this.boundSupers.put(key, boundBase);
        this.boundSuperRoute.put(key, route);
    }

    public void collect(JavaRefTypeInstance boundBase, BindingSuperContainer.Route route) {
        JavaGenericRefTypeInstance prev = this.boundSupers.put(boundBase, null);
        this.boundSuperRoute.put(boundBase, route);
    }
}

