/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.FormalTypeParameter;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericPlaceholderTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;

public class GenericTypeBinder {
    private final Map<String, JavaTypeInstance> nameToBoundType;

    private GenericTypeBinder(Map<String, JavaTypeInstance> nameToBoundType) {
        this.nameToBoundType = nameToBoundType;
    }

    public static GenericTypeBinder createEmpty() {
        return new GenericTypeBinder(MapFactory.newMap());
    }

    public static GenericTypeBinder bind(List<FormalTypeParameter> methodFormalTypeParameters, ClassSignature classSignature, List<JavaTypeInstance> args, JavaGenericRefTypeInstance boundInstance, List<JavaTypeInstance> boundArgs) {
        int x;
        Map nameToBoundType = MapFactory.newMap();
        if (boundInstance != null) {
            List<JavaTypeInstance> boundParameters;
            List<FormalTypeParameter> unboundParameters;
            if ((boundParameters = boundInstance.getGenericTypes()).size() != (unboundParameters = classSignature.getFormalTypeParameters()).size()) {
                return null;
            }
            for (x = 0; x < boundParameters.size(); ++x) {
                nameToBoundType.put((String)unboundParameters.get(x).getName(), (JavaTypeInstance)boundParameters.get(x));
            }
        }
        List<FormalTypeParameter> classFormalTypeParamters = classSignature.getFormalTypeParameters();
        GenericTypeBinder res = new GenericTypeBinder(nameToBoundType);
        if ((methodFormalTypeParameters == null || methodFormalTypeParameters.isEmpty()) && (classFormalTypeParamters == null || classFormalTypeParamters.isEmpty())) return res;
        if (args.size() != boundArgs.size()) {
            throw new IllegalArgumentException();
        }
        for (x = 0; x < args.size(); ++x) {
            JavaTypeInstance unbound = args.get(x);
            JavaTypeInstance bound = boundArgs.get(x);
            if (unbound instanceof JavaArrayTypeInstance && bound instanceof JavaArrayTypeInstance && unbound.getNumArrayDimensions() == bound.getNumArrayDimensions()) {
                unbound = unbound.getArrayStrippedType();
                bound = bound.getArrayStrippedType();
            }
            if (!(unbound instanceof JavaGenericBaseInstance)) continue;
            JavaGenericBaseInstance unboundGeneric = (JavaGenericBaseInstance)unbound;
            unboundGeneric.tryFindBinding(bound, res);
        }
        return res;
    }

    public static GenericTypeBinder buildIdentityBindings(JavaGenericRefTypeInstance unbound) {
        List<JavaTypeInstance> typeParameters = unbound.getGenericTypes();
        Map unboundNames = MapFactory.newMap();
        int len = typeParameters.size();
        for (int x = 0; x < len; ++x) {
            JavaTypeInstance unboundParam;
            if (!(unboundParam = typeParameters.get(x) instanceof JavaGenericPlaceholderTypeInstance)) {
                throw new ConfusedCFRException("Unbound parameter expected to be placeholder!");
            }
            unboundNames.put((String)unboundParam.getRawName(), (JavaTypeInstance)unboundParam);
        }
        return new GenericTypeBinder(unboundNames);
    }

    public static GenericTypeBinder extractBindings(JavaGenericBaseInstance unbound, JavaTypeInstance maybeBound) {
        Map boundNames = MapFactory.newMap();
        GenericTypeBinder.doBind(boundNames, unbound, maybeBound);
        return new GenericTypeBinder(boundNames);
    }

    private static void doBind(Map<String, JavaTypeInstance> boundNames, JavaGenericBaseInstance unbound, JavaTypeInstance maybeBound) {
        List<JavaTypeInstance> boundTypeParameters;
        JavaGenericBaseInstance bound;
        if (unbound.getClass() == JavaGenericPlaceholderTypeInstance.class) {
            JavaGenericPlaceholderTypeInstance placeholder = (JavaGenericPlaceholderTypeInstance)unbound;
            boundNames.put(placeholder.getRawName(), maybeBound);
            return;
        }
        List<JavaTypeInstance> typeParameters = unbound.getGenericTypes();
        if (!(maybeBound instanceof JavaGenericBaseInstance)) {
            return;
        }
        if (typeParameters.size() != (boundTypeParameters = (bound = (JavaGenericBaseInstance)maybeBound).getGenericTypes()).size()) {
            return;
        }
        int len = typeParameters.size();
        for (int x = 0; x < len; ++x) {
            JavaTypeInstance unboundParam = typeParameters.get(x);
            JavaTypeInstance boundParam = boundTypeParameters.get(x);
            if (!(unboundParam instanceof JavaGenericBaseInstance)) continue;
            GenericTypeBinder.doBind(boundNames, (JavaGenericBaseInstance)unboundParam, boundParam);
        }
    }

    public JavaTypeInstance getBindingFor(JavaTypeInstance maybeUnbound) {
        if (maybeUnbound instanceof JavaGenericPlaceholderTypeInstance) {
            JavaTypeInstance bound;
            JavaGenericPlaceholderTypeInstance placeholder;
            String name;
            if ((bound = this.nameToBoundType.get(name = (placeholder = (JavaGenericPlaceholderTypeInstance)maybeUnbound).getRawName())) == null) return maybeUnbound;
            return bound;
        }
        if (!(maybeUnbound instanceof JavaGenericRefTypeInstance)) return maybeUnbound;
        return ((JavaGenericRefTypeInstance)maybeUnbound).getBoundInstance(this);
    }

    private static boolean isBetterBinding(JavaTypeInstance isBetter, JavaTypeInstance than) {
        if (than == null) {
            return true;
        }
        if (!(isBetter instanceof JavaGenericPlaceholderTypeInstance)) return true;
        return false;
    }

    public void suggestBindingFor(String name, JavaTypeInstance binding) {
        JavaTypeInstance alreadyBound = this.nameToBoundType.get(name);
        if (!GenericTypeBinder.isBetterBinding(binding, alreadyBound)) return;
        this.nameToBoundType.put(name, binding);
    }

    public GenericTypeBinder mergeWith(GenericTypeBinder other, boolean mergeToCommonClass) {
        Set<String> keys = SetFactory.newSet(this.nameToBoundType.keySet());
        keys.addAll(other.nameToBoundType.keySet());
        Map res = MapFactory.newMap();
        for (String key : keys) {
            JavaTypeInstance t1 = this.nameToBoundType.get(key);
            JavaTypeInstance t2 = other.nameToBoundType.get(key);
            if (t1 == null) {
                res.put((String)key, (JavaTypeInstance)t2);
                continue;
            }
            if (t2 == null) {
                res.put((String)key, (JavaTypeInstance)t1);
                continue;
            }
            if (!mergeToCommonClass) return null;
            if (t1.implicitlyCastsTo(t2, other)) {
                res.put((String)key, (JavaTypeInstance)t2);
                continue;
            }
            if (t2.implicitlyCastsTo(t1, other)) {
                res.put((String)key, (JavaTypeInstance)t1);
                continue;
            }
            InferredJavaType clash = InferredJavaType.mkClash(t1, t2);
            clash.collapseTypeClash();
            res.put((String)key, (JavaTypeInstance)clash.getJavaTypeInstance());
        }
        return new GenericTypeBinder(res);
    }
}

