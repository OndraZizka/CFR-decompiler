/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.ClassNameUtils;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.MapFactory;

public class ClassCache {
    private final Map<String, JavaRefTypeInstance> refClassTypeCache = MapFactory.newMap();
    private final DCCommonState dcCommonState;

    public ClassCache(DCCommonState dcCommonState) {
        this.dcCommonState = dcCommonState;
        this.refClassTypeCache.put(TypeConstants.ASSERTION_ERROR.getRawName(), TypeConstants.ASSERTION_ERROR);
        this.refClassTypeCache.put(TypeConstants.OBJECT.getRawName(), TypeConstants.OBJECT);
        this.refClassTypeCache.put(TypeConstants.STRING.getRawName(), TypeConstants.STRING);
        this.refClassTypeCache.put(TypeConstants.ENUM.getRawName(), TypeConstants.ENUM);
    }

    public JavaRefTypeInstance getRefClassFor(String rawClassName) {
        String name = ClassNameUtils.convertFromPath(rawClassName);
        JavaRefTypeInstance typeInstance = this.refClassTypeCache.get(name);
        if (typeInstance != null) return typeInstance;
        typeInstance = JavaRefTypeInstance.create(name, this.dcCommonState);
        this.refClassTypeCache.put(name, typeInstance);
        return typeInstance;
    }

    public Pair<JavaRefTypeInstance, JavaRefTypeInstance> getRefClassForInnerOuterPair(String rawInnerName, String rawOuterName) {
        String innerName = ClassNameUtils.convertFromPath(rawInnerName);
        String outerName = ClassNameUtils.convertFromPath(rawOuterName);
        JavaRefTypeInstance inner = this.refClassTypeCache.get(innerName);
        JavaRefTypeInstance outer = this.refClassTypeCache.get(outerName);
        if (inner != null && outer != null) {
            return Pair.make(inner, outer);
        }
        Pair<JavaRefTypeInstance, JavaRefTypeInstance> pair = JavaRefTypeInstance.createKnownInnerOuter(innerName, outerName, outer, this.dcCommonState);
        if (inner == null) {
            this.refClassTypeCache.put(innerName, pair.getFirst());
            inner = pair.getFirst();
        }
        if (outer != null) return Pair.make(inner, outer);
        this.refClassTypeCache.put(outerName, pair.getSecond());
        outer = pair.getSecond();
        return Pair.make(inner, outer);
    }
}

