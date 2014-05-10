/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.classfilehelpers;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class OverloadMethodSet {
    private final ClassFile classFile;
    private final MethodData actualPrototype;
    private final List<MethodData> allPrototypes;

    public OverloadMethodSet(ClassFile classFile, MethodPrototype actualPrototype, List<MethodPrototype> allPrototypes) {
        this.classFile = classFile;
         mk = new UnaryFunction<MethodPrototype, MethodData>(){

            @Override
            public MethodData invoke(MethodPrototype arg) {
                return new MethodData(arg, arg.getArgs(), null);
            }
        };
        this.actualPrototype = (MethodData)mk.invoke(actualPrototype);
        this.allPrototypes = Functional.map(allPrototypes, mk);
    }

    private OverloadMethodSet(ClassFile classFile, MethodData actualPrototype, List<MethodData> allPrototypes) {
        this.classFile = classFile;
        this.actualPrototype = actualPrototype;
        this.allPrototypes = allPrototypes;
    }

    public OverloadMethodSet specialiseTo(JavaGenericRefTypeInstance type) {
        GenericTypeBinder genericTypeBinder = this.classFile.getGenericTypeBinder(type);
        if (genericTypeBinder == null) {
            return null;
        }
         mk = new UnaryFunction<MethodData, MethodData>(){

            @Override
            public MethodData invoke(MethodData arg) {
                return MethodData.access$100(arg, genericTypeBinder);
            }
        };
        return new OverloadMethodSet(this.classFile, (MethodData)mk.invoke(this.actualPrototype), Functional.map(this.allPrototypes, mk));
    }

    public JavaTypeInstance getArgType(int idx, JavaTypeInstance used) {
        return this.actualPrototype.getArgType(idx, used);
    }

    public boolean callsCorrectEntireMethod(List<Expression> args, GenericTypeBinder gtb) {
        int argCount = args.size();
        Set<MethodData> possibleMatches = SetFactory.newSet(Functional.filter(this.allPrototypes, new Predicate<MethodData>(){

            @Override
            public boolean test(MethodData in) {
                return MethodData.access$300(in).size() <= argCount;
            }
        }));
        int len = args.size();
        for (int x = 0; x < len; ++x) {
            Expression arg = args.get(x);
            JavaTypeInstance actual = arg.getInferredJavaType().getJavaTypeInstance();
            Iterator<MethodData> possiter = possibleMatches.iterator();
            while (possiter.hasNext()) {
                JavaTypeInstance argType;
                MethodData prototype;
                if ((argType = (prototype = possiter.next()).getArgType(x, actual)) == null) {
                    possiter.remove();
                    continue;
                }
                if (actual.implicitlyCastsTo(argType, gtb) && actual.canCastTo(argType, gtb)) continue;
                possiter.remove();
            }
        }
        if (possibleMatches.isEmpty()) {
            return false;
        }
        if (possibleMatches.size() != 1) return false;
        MethodData methodData = possibleMatches.iterator().next();
        return methodData.methodPrototype.equals(this.actualPrototype.methodPrototype);
    }

    public boolean callsCorrectMethod(Expression newArg, int idx, GenericTypeBinder gtb) {
        JavaTypeInstance expectedArgType;
        JavaTypeInstance newArgType = newArg.getInferredJavaType().getJavaTypeInstance();
        Set exactMatches = SetFactory.newSet();
        Iterator<MethodData> i$ = this.allPrototypes.iterator();
        while (i$.hasNext()) {
            MethodData prototype;
            JavaTypeInstance type;
            if ((type = (prototype = i$.next()).getArgType(idx, newArgType)) == null || !type.equals(newArgType)) continue;
            exactMatches.add((MethodPrototype)prototype.methodPrototype);
        }
        if (exactMatches.contains(this.actualPrototype.methodPrototype)) {
            return true;
        }
        if (!(expectedArgType = this.actualPrototype.getArgType(idx, newArgType) instanceof RawJavaType)) return this.callsCorrectApproxObjMethod(newArg, newArgType, idx, gtb);
        return this.callsCorrectApproxRawMethod(newArg, newArgType, idx, gtb);
    }

    public boolean callsCorrectApproxRawMethod(Expression newArg, JavaTypeInstance actual, int idx, GenericTypeBinder gtb) {
        List matches = ListFactory.newList();
        for (MethodData prototype : this.allPrototypes) {
            JavaTypeInstance arg = prototype.getArgType(idx, actual);
            if (!actual.implicitlyCastsTo(arg, null) || !actual.canCastTo(arg, gtb)) continue;
            matches.add((MethodData)prototype);
        }
        if (matches.isEmpty()) {
            return false;
        }
        if (matches.size() == 1 && ((MethodData)matches.get(0)).is(this.actualPrototype)) {
            return true;
        }
        boolean boxingFirst = !(actual instanceof RawJavaType);
        MethodData lowest = (MethodData)matches.get(0);
        JavaTypeInstance lowestType = lowest.getArgType(idx, actual);
        for (int x = 1; x < matches.size(); ++x) {
            JavaTypeInstance nextType;
            MethodData next;
            if (!(nextType = (next = (MethodData)matches.get(x)).getArgType(idx, actual)).implicitlyCastsTo(lowestType, null)) continue;
            lowest = next;
            lowestType = nextType;
        }
        if (!lowest.is(this.actualPrototype)) return false;
        return true;
    }

    public boolean callsCorrectApproxObjMethod(Expression newArg, JavaTypeInstance actual, int idx, GenericTypeBinder gtb) {
        Literal nullLit;
        boolean isPOD;
        List matches = ListFactory.newList();
        boolean podMatchExists = false;
        boolean nonPodMatchExists = false;
        Iterator<MethodData> i$ = this.allPrototypes.iterator();
        while (i$.hasNext()) {
            MethodData prototype;
            JavaTypeInstance arg;
            if ((arg = (prototype = i$.next()).getArgType(idx, actual)) == null || !actual.implicitlyCastsTo(arg, null) || !actual.canCastTo(arg, gtb)) continue;
            if (arg instanceof RawJavaType) {
                podMatchExists = true;
            } else {
                nonPodMatchExists = true;
            }
            matches.add((MethodData)prototype);
        }
        if (matches.isEmpty()) {
            return false;
        }
        if (matches.size() == 1 && ((MethodData)matches.get(0)).is(this.actualPrototype)) {
            return true;
        }
        if (newArg.equals(nullLit = new Literal(TypedLiteral.getNull())) && actual == RawJavaType.NULL) {
            MethodData best = null;
            JavaTypeInstance bestType = null;
            Iterator i$2 = matches.iterator();
            while (i$2.hasNext()) {
                JavaTypeInstance arg;
                MethodData match;
                if ((arg = (match = (MethodData)i$2.next()).getArgType(idx, actual)).equals(TypeConstants.OBJECT)) continue;
                if (best == null) {
                    best = match;
                    bestType = arg;
                    continue;
                }
                if (arg.implicitlyCastsTo(bestType, null)) {
                    best = match;
                    bestType = arg;
                    continue;
                }
                if (bestType.implicitlyCastsTo(arg, null)) continue;
                return false;
            }
            if (best != null) {
                return best.is(this.actualPrototype);
            }
        }
        boolean onlyMatchPod = (isPOD = actual instanceof RawJavaType) && podMatchExists;
        if (onlyMatchPod) {
            matches = Functional.filter(matches, new Predicate<MethodData>(){

                @Override
                public boolean test(MethodData in) {
                    return MethodData.access$200(in, idx, actual) instanceof RawJavaType;
                }
            });
        }
        if (!isPOD) {
            Pair partition = Functional.partition(matches, new Predicate<MethodData>(){

                @Override
                public boolean test(MethodData in) {
                    return !(MethodData.access$200(in, idx, actual) instanceof RawJavaType);
                }
            });
            matches.clear();
            matches.addAll(partition.getFirst());
            if (!nonPodMatchExists) {
                matches.addAll(partition.getSecond());
            }
        }
        if (matches.isEmpty()) {
            return false;
        }
        MethodData lowest = (MethodData)matches.get(0);
        JavaTypeInstance lowestType = lowest.getArgType(idx, actual);
        for (int x = 0; x < matches.size(); ++x) {
            JavaTypeInstance nextType;
            MethodData next;
            if (!(nextType = (next = (MethodData)matches.get(x)).getArgType(idx, actual)).implicitlyCastsTo(lowestType, null)) continue;
            lowest = next;
            lowestType = nextType;
        }
        if (!lowest.is(this.actualPrototype)) return false;
        return true;
    }

    static class MethodData {
        private final MethodPrototype methodPrototype;
        private final List<JavaTypeInstance> methodArgs;
        private final int size;

        private MethodData(MethodPrototype methodPrototype, List<JavaTypeInstance> methodArgs) {
            this.methodPrototype = methodPrototype;
            this.methodArgs = methodArgs;
            this.size = methodArgs.size();
        }

        private JavaTypeInstance getArgType(int idx, JavaTypeInstance used) {
            if (idx >= this.size - 1 && this.methodPrototype.isVarArgs()) {
                JavaTypeInstance res;
                if ((res = this.methodArgs.get(this.size - 1)).getNumArrayDimensions() != used.getNumArrayDimensions() + 1) return res;
                return res.removeAnArrayIndirection();
            }
            if (idx < this.size) return this.methodArgs.get(idx);
            return null;
        }

        public boolean isVararg(int idx) {
            return idx >= this.size - 1 && this.methodPrototype.isVarArgs();
        }

        public boolean is(MethodData other) {
            return this.methodPrototype == other.methodPrototype;
        }

        public String toString() {
            return this.methodPrototype.toString();
        }

        private MethodData getBoundVersion(GenericTypeBinder genericTypeBinder) {
            List rebound = Functional.map(this.methodArgs, new UnaryFunction<JavaTypeInstance, JavaTypeInstance>(){

                @Override
                public JavaTypeInstance invoke(JavaTypeInstance arg) {
                    if (!(arg instanceof JavaGenericBaseInstance)) return arg;
                    return ((JavaGenericBaseInstance)arg).getBoundInstance(genericTypeBinder);
                }
            });
            return new MethodData(this.methodPrototype, rebound);
        }

        /* synthetic */ MethodData(MethodPrototype x0, List x1, org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet$1 x2) {
            this(x0, x1);
        }

        static /* synthetic */ MethodData access$100(MethodData x0, GenericTypeBinder x1) {
            return x0.getBoundVersion(x1);
        }

        static /* synthetic */ List access$300(MethodData x0) {
            return x0.methodArgs;
        }

    }

}

