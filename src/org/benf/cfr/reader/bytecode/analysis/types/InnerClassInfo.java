/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;

public interface InnerClassInfo {
    public static final InnerClassInfo NOT = new InnerClassInfo(){

        @Override
        public boolean isInnerClass() {
            return false;
        }

        @Override
        public boolean isAnonymousClass() {
            return false;
        }

        @Override
        public boolean isMethodScopedClass() {
            return false;
        }

        @Override
        public void markMethodScoped(boolean isAnonymous) {
        }

        @Override
        public boolean isInnerClassOf(JavaTypeInstance possibleParent) {
            return false;
        }

        @Override
        public boolean isTransitiveInnerClassOf(JavaTypeInstance possibleParent) {
            return false;
        }

        @Override
        public void setHideSyntheticThis() {
            throw new IllegalStateException();
        }

        @Override
        public JavaRefTypeInstance getOuterClass() {
            throw new IllegalStateException();
        }

        @Override
        public boolean isHideSyntheticThis() {
            return false;
        }
    };

    public boolean isInnerClass();

    public boolean isInnerClassOf(JavaTypeInstance var1);

    public boolean isTransitiveInnerClassOf(JavaTypeInstance var1);

    public void setHideSyntheticThis();

    public boolean isHideSyntheticThis();

    public void markMethodScoped(boolean var1);

    public boolean isAnonymousClass();

    public boolean isMethodScopedClass();

    public JavaRefTypeInstance getOuterClass();

}

