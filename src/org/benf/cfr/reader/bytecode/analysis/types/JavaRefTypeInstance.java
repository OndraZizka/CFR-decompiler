/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import com.sun.istack.internal.Nullable;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericPlaceholderTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public class JavaRefTypeInstance
implements JavaTypeInstance {
    private final String className;
    private transient String shortName;
    private transient String suggestedVarName;
    private transient InnerClassInfo innerClassInfo;
    private final DCCommonState dcCommonState;
    private BindingSuperContainer cachedBindingSupers = BindingSuperContainer.POISON;

    private JavaRefTypeInstance(String className, DCCommonState dcCommonState) {
        this.innerClassInfo = InnerClassInfo.NOT;
        this.dcCommonState = dcCommonState;
        if (className.contains((CharSequence)"$")) {
            String outer = className.substring(0, className.lastIndexOf(36));
            JavaRefTypeInstance outerClassTmp = dcCommonState.getClassCache().getRefClassFor(outer);
            this.innerClassInfo = new RefTypeInnerClassInfo(null);
        }
        this.className = className;
        this.shortName = JavaRefTypeInstance.getShortName(className, this.innerClassInfo);
    }

    private JavaRefTypeInstance(String className, JavaRefTypeInstance knownOuter, DCCommonState dcCommonState) {
        this.className = className;
        this.dcCommonState = dcCommonState;
        String innerSub = className.substring(knownOuter.className.length());
        if (innerSub.charAt(0) == '$') {
            innerSub = innerSub.substring(1);
        }
        this.innerClassInfo = new RefTypeInnerClassInfo(null);
        this.shortName = innerSub;
    }

    public void markNotInner() {
        this.innerClassInfo = InnerClassInfo.NOT;
        this.shortName = JavaRefTypeInstance.getShortName(this.className, this.innerClassInfo);
    }

    @Override
    public String suggestVarName() {
        String displayName;
        if (this.suggestedVarName != null) {
            return this.suggestedVarName;
        }
        if ((displayName = this.shortName).isEmpty()) {
            return null;
        }
        char[] chars = displayName.toCharArray();
        int x2 = 0;
        int len = chars.length;
        for (int x2 = 0; x2 < len && (c = chars[x2]) >= '0' && c <= '9'; ++x2) {
        }
        if (x2 >= len) {
            return null;
        }
        chars[x2] = Character.toLowerCase(chars[x2]);
        displayName = new String(chars, x2, len - x2);
        return displayName;
    }

    private JavaRefTypeInstance(String className, String displayableName, JavaRefTypeInstance[] supers) {
        this.innerClassInfo = InnerClassInfo.NOT;
        this.dcCommonState = null;
        this.className = className;
        this.shortName = displayableName;
        Map tmp = MapFactory.newMap();
        Map routes = MapFactory.newMap();
        for (JavaRefTypeInstance supr : supers) {
            tmp.put((JavaRefTypeInstance)supr, null);
            routes.put((JavaRefTypeInstance)supr, (BindingSuperContainer.Route)BindingSuperContainer.Route.EXTENSION);
        }
        this.cachedBindingSupers = new BindingSuperContainer(null, tmp, routes);
    }

    public static JavaRefTypeInstance create(String rawClassName, DCCommonState dcCommonState) {
        return new JavaRefTypeInstance(rawClassName, dcCommonState);
    }

    public static Pair<JavaRefTypeInstance, JavaRefTypeInstance> createKnownInnerOuter(String inner, String outer, JavaRefTypeInstance outerType, DCCommonState dcCommonState) {
        if (outerType == null) {
            outerType = new JavaRefTypeInstance(outer, dcCommonState);
        }
        JavaRefTypeInstance innerType = !inner.startsWith(outer) ? new JavaRefTypeInstance(inner, dcCommonState) : new JavaRefTypeInstance(inner, outerType, dcCommonState);
        return Pair.make(innerType, outerType);
    }

    public static /* varargs */ JavaRefTypeInstance createTypeConstant(String rawClassName, String displayableName, JavaRefTypeInstance ... supers) {
        return new JavaRefTypeInstance(rawClassName, displayableName, supers);
    }

    @Override
    public StackType getStackType() {
        return StackType.REF;
    }

    @Override
    public void dumpInto(Dumper d, TypeUsageInformation typeUsageInformation) {
        String res = typeUsageInformation.getName(this);
        if (res == null) {
            throw new IllegalStateException();
        }
        d.print(res);
    }

    public String toString() {
        return new ToStringDumper().dump(this).toString();
    }

    @Override
    public JavaTypeInstance getArrayStrippedType() {
        return this;
    }

    @Override
    public int getNumArrayDimensions() {
        return 0;
    }

    @Override
    public String getRawName() {
        return this.className;
    }

    public String getRawShortName() {
        return this.shortName;
    }

    public int hashCode() {
        return 31 + this.className.hashCode();
    }

    @Override
    public InnerClassInfo getInnerClassHereInfo() {
        return this.innerClassInfo;
    }

    @Override
    public BindingSuperContainer getBindingSupers() {
        if (this.cachedBindingSupers != BindingSuperContainer.POISON) {
            return this.cachedBindingSupers;
        }
        try {
            ClassFile classFile = this.getClassFile();
            this.cachedBindingSupers = classFile == null ? null : classFile.getBindingSupers();
        }
        catch (CannotLoadClassException e) {
            this.cachedBindingSupers = null;
        }
        return this.cachedBindingSupers;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof JavaRefTypeInstance)) {
            return false;
        }
        JavaRefTypeInstance other = (JavaRefTypeInstance)o;
        return other.className.equals(this.className);
    }

    @Override
    public boolean isComplexType() {
        return true;
    }

    @Override
    public boolean isUsableType() {
        return true;
    }

    @Override
    public JavaTypeInstance removeAnArrayIndirection() {
        return this;
    }

    @Override
    public JavaTypeInstance getDeGenerifiedType() {
        return this;
    }

    @Override
    public RawJavaType getRawTypeOfSimpleType() {
        return RawJavaType.REF;
    }

    @Override
    public boolean implicitlyCastsTo(JavaTypeInstance other, @Nullable GenericTypeBinder gtb) {
        BindingSuperContainer thisBindingSuper;
        RawJavaType thisAsRaw;
        if (this.equals(other)) {
            return true;
        }
        if (other instanceof RawJavaType && (thisAsRaw = RawJavaType.getUnboxedTypeFor(this)) != null) {
            return thisAsRaw.implicitlyCastsTo(other, gtb);
        }
        if (gtb != null && other instanceof JavaGenericPlaceholderTypeInstance && !(other = gtb.getBindingFor(other) instanceof JavaGenericPlaceholderTypeInstance)) {
            return this.implicitlyCastsTo(other, gtb);
        }
        if (other instanceof JavaGenericPlaceholderTypeInstance) {
            return false;
        }
        JavaTypeInstance otherRaw = other.getDeGenerifiedType();
        if ((thisBindingSuper = this.getBindingSupers()) != null) return thisBindingSuper.containsBase(otherRaw);
        return false;
    }

    @Override
    public boolean canCastTo(JavaTypeInstance other, GenericTypeBinder gtb) {
        RawJavaType thisAsRaw;
        if (!(other instanceof RawJavaType)) return true;
        if ((thisAsRaw = RawJavaType.getUnboxedTypeFor(this)) == null) return true;
        return thisAsRaw.equals((Object)other);
    }

    public ClassFile getClassFile() {
        if (this.dcCommonState == null) {
            return null;
        }
        ClassFile classFile = this.dcCommonState.getClassFile(this);
        return classFile;
    }

    private static String getShortName(String fullClassName, InnerClassInfo innerClassInfo) {
        int idxlast;
        if (innerClassInfo.isInnerClass()) {
            fullClassName = fullClassName.replace('$', '.');
        }
        String partname = (idxlast = fullClassName.lastIndexOf(46)) == -1 ? fullClassName : fullClassName.substring(idxlast + 1);
        return partname;
    }

    @Override
    public void collectInto(TypeUsageCollector typeUsageCollector) {
        typeUsageCollector.collectRefType(this);
    }

    class 1 {
    }

    class RefTypeInnerClassInfo
    implements InnerClassInfo {
        private final JavaRefTypeInstance outerClass;
        private boolean isAnonymous = false;
        private boolean isMethodScoped = false;
        private boolean hideSyntheticThis = false;

        private RefTypeInnerClassInfo(JavaRefTypeInstance outerClass) {
            this.outerClass = outerClass;
        }

        @Override
        public boolean isInnerClass() {
            return true;
        }

        @Override
        public boolean isAnonymousClass() {
            return this.isAnonymous;
        }

        @Override
        public boolean isMethodScopedClass() {
            return this.isMethodScoped;
        }

        @Override
        public void markMethodScoped(boolean isAnonymous) {
            this.isAnonymous = isAnonymous;
            this.isMethodScoped = true;
        }

        @Override
        public boolean isInnerClassOf(JavaTypeInstance possibleParent) {
            if (this.outerClass != null) return possibleParent.equals(this.outerClass);
            return false;
        }

        @Override
        public boolean isTransitiveInnerClassOf(JavaTypeInstance possibleParent) {
            InnerClassInfo upper;
            if (this.outerClass == null) {
                return false;
            }
            if (possibleParent.equals(this.outerClass)) {
                return true;
            }
            if ((upper = this.outerClass.getInnerClassHereInfo()).isInnerClass()) return upper.isTransitiveInnerClassOf(possibleParent);
            return false;
        }

        @Override
        public void setHideSyntheticThis() {
            this.hideSyntheticThis = true;
        }

        @Override
        public JavaRefTypeInstance getOuterClass() {
            return this.outerClass;
        }

        @Override
        public boolean isHideSyntheticThis() {
            return this.hideSyntheticThis;
        }

        /* synthetic */ RefTypeInnerClassInfo(JavaRefTypeInstance x0, 1 x1) {
            this(x0);
        }
    }

}

