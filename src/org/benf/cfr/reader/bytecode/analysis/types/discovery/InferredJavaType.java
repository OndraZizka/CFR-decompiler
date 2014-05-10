/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types.discovery;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericPlaceholderTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.CastAction;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.util.BoolPair;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.MiscUtils;
import org.benf.cfr.reader.util.SetFactory;

public class InferredJavaType {
    private static int global_id = 0;
    private IJTInternal value;
    public static final InferredJavaType IGNORE = new InferredJavaType();

    public InferredJavaType() {
        this.value = new IJTInternal_Impl(RawJavaType.VOID, Source.UNKNOWN, false, null);
    }

    public InferredJavaType(JavaTypeInstance type, Source source) {
        this.value = new IJTInternal_Impl(type, source, false, null);
    }

    public InferredJavaType(JavaTypeInstance type, Source source, boolean locked) {
        this.value = new IJTInternal_Impl(type, source, locked, null);
    }

    private InferredJavaType(IJTInternal_Clash clash) {
        this.value = clash;
    }

    public static /* varargs */ InferredJavaType mkClash(JavaTypeInstance ... types) {
        List ints = ListFactory.newList();
        for (JavaTypeInstance type : types) {
            ints.add((IJTInternal_Impl)new IJTInternal_Impl(type, Source.UNKNOWN, false, null));
        }
        return new InferredJavaType(new IJTInternal_Clash(ints, null));
    }

    public Source getSource() {
        return this.value.getSource();
    }

    private void mergeGenericInfo(JavaGenericRefTypeInstance otherTypeInstance) {
        JavaGenericRefTypeInstance thisType;
        ClassFile degenerifiedThisClassFile;
        if (this.value.isLocked()) {
            return;
        }
        if (!(thisType = (JavaGenericRefTypeInstance)this.value.getJavaTypeInstance()).hasUnbound()) {
            return;
        }
        if ((degenerifiedThisClassFile = thisType.getDeGenerifiedType().getClassFile()) == null) {
            return;
        }
        JavaTypeInstance boundThisType = degenerifiedThisClassFile.getBindingSupers().getBoundAssignable(thisType, otherTypeInstance);
        if (boundThisType.equals(thisType)) return;
        InferredJavaType.mkDelegate(this.value, new IJTInternal_Impl(boundThisType, Source.GENERICCALL, true, null));
    }

    public void noteUseAs(JavaTypeInstance type) {
        BindingSuperContainer bindingSuperContainer;
        if (this.value.getClashState() != ClashState.Clash || !(bindingSuperContainer = this.getJavaTypeInstance().getBindingSupers()).containsBase(type.getDeGenerifiedType())) return;
        this.value.forceType(type, false);
        this.value.markClashState(ClashState.Resolved);
    }

    public boolean isClash() {
        return this.value.getClashState() == ClashState.Clash;
    }

    public void collapseTypeClash() {
        this.value.collapseTypeClash();
    }

    public int getLocalId() {
        return this.value.getLocalId();
    }

    private boolean checkBaseCompatibility(JavaTypeInstance otherType) {
        BindingSuperContainer otherSupers;
        JavaTypeInstance thisStripped = this.getJavaTypeInstance().getDeGenerifiedType();
        JavaTypeInstance otherStripped = otherType.getDeGenerifiedType();
        if (thisStripped.equals(otherStripped)) {
            return true;
        }
        if ((otherSupers = otherType.getBindingSupers()) != null) return otherSupers.containsBase(thisStripped);
        return true;
    }

    private CastAction chainFrom(InferredJavaType other) {
        if (this == other) {
            return CastAction.None;
        }
        JavaTypeInstance thisTypeInstance = this.value.getJavaTypeInstance();
        JavaTypeInstance otherTypeInstance = other.value.getJavaTypeInstance();
        if (thisTypeInstance != RawJavaType.VOID) {
            boolean basecast = false;
            if (thisTypeInstance.isComplexType() && otherTypeInstance.isComplexType()) {
                if (!this.checkBaseCompatibility(other.getJavaTypeInstance())) {
                    this.value = IJTInternal_Clash.mkClash(this.value, other.value);
                    return CastAction.None;
                }
                if (this.value.getClashState() == ClashState.Resolved) {
                    return CastAction.None;
                }
                if (thisTypeInstance.getClass() == otherTypeInstance.getClass()) {
                    basecast = true;
                }
            }
            if (otherTypeInstance instanceof JavaGenericRefTypeInstance && thisTypeInstance instanceof JavaGenericRefTypeInstance) {
                other.mergeGenericInfo((JavaGenericRefTypeInstance)thisTypeInstance);
            }
            if (basecast) {
                return CastAction.None;
            }
            if (otherTypeInstance instanceof JavaGenericPlaceholderTypeInstance ^ thisTypeInstance instanceof JavaGenericPlaceholderTypeInstance) {
                return CastAction.InsertExplicit;
            }
        }
        InferredJavaType.mkDelegate(this.value, other.value);
        if (other.value.isLocked()) return CastAction.None;
        this.value = other.value;
        return CastAction.None;
    }

    private static void mkDelegate(IJTInternal a, IJTInternal b) {
        if (a.getFinalId() == b.getFinalId()) return;
        a.mkDelegate(b);
    }

    private CastAction chainIntegralTypes(InferredJavaType other) {
        int pri;
        if (this == other) {
            return CastAction.None;
        }
        if ((pri = this.getRawType().compareTypePriorityTo(other.getRawType())) >= 0) {
            IJTInternal otherLocked;
            if (other.value.isLocked()) {
                if (pri <= 0) return CastAction.None;
                return CastAction.InsertExplicit;
            }
            if (pri > 0 && (otherLocked = other.value.getFirstLocked()) != null && otherLocked.getJavaTypeInstance() == other.getJavaTypeInstance()) {
                return CastAction.InsertExplicit;
            }
            InferredJavaType.mkDelegate(other.value, this.value);
        } else {
            if (this.value.isLocked()) {
                return CastAction.InsertExplicit;
            }
            InferredJavaType.mkDelegate(this.value, other.value);
            this.value = other.value;
        }
        return CastAction.None;
    }

    public static void compareAsWithoutCasting(InferredJavaType a, InferredJavaType b, boolean aLit, boolean bLit) {
        if (a == InferredJavaType.IGNORE) {
            return;
        }
        if (b == InferredJavaType.IGNORE) {
            return;
        }
        RawJavaType art = a.getRawType();
        RawJavaType brt = b.getRawType();
        if (art.getStackType() != StackType.INT || brt.getStackType() != StackType.INT) {
            return;
        }
        InferredJavaType litType = null;
        InferredJavaType betterType = null;
        Object litExp = null;
        BoolPair whichLit = BoolPair.get(a.getSource() == Source.LITERAL, b.getSource() == Source.LITERAL);
        if (whichLit.getCount() != 1) {
            whichLit = BoolPair.get(aLit, bLit);
        }
        if (art == RawJavaType.BOOLEAN && brt.getStackType() == StackType.INT && brt.compareTypePriorityTo(art) > 0) {
            litType = a;
            betterType = b;
        } else if (brt == RawJavaType.BOOLEAN && art.getStackType() == StackType.INT && art.compareTypePriorityTo(brt) > 0) {
            litType = b;
            betterType = a;
        } else {
            switch (whichLit) {
                case FIRST: {
                    litType = a;
                    betterType = b;
                    break;
                }
                case SECOND: {
                    litType = b;
                    betterType = a;
                    break;
                }
                case NEITHER: 
                case BOTH: {
                    return;
                }
            }
        }
        litType.chainFrom(betterType);
    }

    public void useAsWithCast(RawJavaType otherRaw) {
        if (this == InferredJavaType.IGNORE) {
            return;
        }
        this.value = new IJTInternal_Impl(otherRaw, Source.OPERATION, true, null);
    }

    public void useInArithOp(InferredJavaType other, boolean forbidBool) {
        RawJavaType thisRaw;
        RawJavaType otherRaw;
        int cmp;
        if (this == InferredJavaType.IGNORE) {
            return;
        }
        if (other == InferredJavaType.IGNORE) {
            return;
        }
        if ((thisRaw = this.getRawType()).getStackType() != (otherRaw = other.getRawType()).getStackType()) {
            return;
        }
        if (thisRaw.getStackType() != StackType.INT) return;
        if ((cmp = thisRaw.compareTypePriorityTo(otherRaw)) < 0) {
            if (thisRaw != RawJavaType.BOOLEAN || !forbidBool) return;
            this.value.forceType(otherRaw, false);
        } else {
            if (cmp != 0 || thisRaw != RawJavaType.BOOLEAN || !forbidBool) return;
            this.value.forceType(RawJavaType.INT, false);
        }
    }

    public static void useInArithOp(InferredJavaType lhs, InferredJavaType rhs, ArithOp op) {
        boolean forbidBool = true;
        if ((op == ArithOp.OR || op == ArithOp.AND || op == ArithOp.XOR) && lhs.getJavaTypeInstance() == RawJavaType.BOOLEAN && rhs.getJavaTypeInstance() == RawJavaType.BOOLEAN) {
            forbidBool = false;
        }
        lhs.useInArithOp(rhs, forbidBool);
        rhs.useInArithOp(lhs, forbidBool);
    }

    public void useAsWithoutCasting(JavaTypeInstance otherTypeInstance) {
        JavaArrayTypeInstance thisArrayTypeInstance;
        BindingSuperContainer bindingSuperContainer;
        JavaArrayTypeInstance otherArrayTypeInstance;
        JavaTypeInstance thisTypeInstance;
        if (this == InferredJavaType.IGNORE) {
            return;
        }
        if (thisTypeInstance = this.getJavaTypeInstance() instanceof RawJavaType && otherTypeInstance instanceof RawJavaType) {
            RawJavaType otherRaw;
            int cmp;
            RawJavaType thisRaw;
            if ((thisRaw = this.getRawType()).getStackType() != (otherRaw = otherTypeInstance.getRawTypeOfSimpleType()).getStackType()) {
                return;
            }
            if (thisRaw.getStackType() != StackType.INT) return;
            if ((cmp = thisRaw.compareTypePriorityTo(otherRaw)) > 0) {
                this.value.forceType(otherRaw, false);
            } else {
                if (cmp >= 0 || thisRaw != RawJavaType.BOOLEAN) return;
                this.value.forceType(otherRaw, false);
            }
            return;
        }
        if (!(thisTypeInstance instanceof JavaArrayTypeInstance) || !(otherTypeInstance instanceof JavaArrayTypeInstance)) return;
        if ((thisArrayTypeInstance = (JavaArrayTypeInstance)thisTypeInstance).getNumArrayDimensions() != (otherArrayTypeInstance = (JavaArrayTypeInstance)otherTypeInstance).getNumArrayDimensions()) {
            return;
        }
        JavaTypeInstance thisStripped = thisArrayTypeInstance.getArrayStrippedType().getDeGenerifiedType();
        JavaTypeInstance otherArrayStripped = otherArrayTypeInstance.getArrayStrippedType();
        JavaTypeInstance otherStripped = otherArrayStripped.getDeGenerifiedType();
        if (otherArrayStripped instanceof JavaGenericBaseInstance) {
            return;
        }
        if (!(thisStripped instanceof JavaRefTypeInstance) || !(otherStripped instanceof JavaRefTypeInstance)) return;
        JavaRefTypeInstance thisRef = (JavaRefTypeInstance)thisStripped;
        JavaRefTypeInstance otherRef = (JavaRefTypeInstance)otherStripped;
        if ((bindingSuperContainer = thisRef.getBindingSupers()) == null) {
            if (otherRef != TypeConstants.OBJECT) return;
            this.value.forceType(otherTypeInstance, false);
        } else {
            if (!bindingSuperContainer.containsBase(otherRef)) return;
            this.value.forceType(otherTypeInstance, false);
        }
    }

    public void deGenerify(JavaTypeInstance other) {
        JavaTypeInstance typeInstanceThis = this.getJavaTypeInstance().getDeGenerifiedType();
        JavaTypeInstance typeInstanceOther = other.getDeGenerifiedType();
        if (!(typeInstanceOther.equals(typeInstanceThis) || TypeConstants.OBJECT == typeInstanceThis)) {
            throw new ConfusedCFRException("Incompatible types : " + typeInstanceThis.getClass() + "[" + typeInstanceThis + "] / " + typeInstanceOther.getClass() + "[" + typeInstanceOther + "]");
        }
        this.value.forceType(other, true);
    }

    public CastAction chain(InferredJavaType other) {
        if (this == InferredJavaType.IGNORE) {
            return CastAction.None;
        }
        if (other == InferredJavaType.IGNORE) {
            return CastAction.None;
        }
        if (other.getRawType() == RawJavaType.VOID) {
            return CastAction.None;
        }
        RawJavaType thisRaw = this.value.getRawType();
        RawJavaType otherRaw = other.getRawType();
        if (thisRaw == RawJavaType.VOID) {
            return this.chainFrom(other);
        }
        if (thisRaw.getStackType() != otherRaw.getStackType()) {
            if (!MiscUtils.xor(thisRaw.getStackType(), otherRaw.getStackType(), StackType.REF)) return CastAction.InsertExplicit;
            this.value = IJTInternal_Clash.mkClash(this.value, other.value);
            return CastAction.InsertExplicit;
        }
        if (thisRaw == otherRaw && thisRaw.getStackType() != StackType.INT) {
            return this.chainFrom(other);
        }
        if (thisRaw == RawJavaType.NULL && (otherRaw == RawJavaType.NULL || otherRaw == RawJavaType.REF)) {
            return this.chainFrom(other);
        }
        if (thisRaw == RawJavaType.REF && otherRaw == RawJavaType.NULL) {
            return CastAction.None;
        }
        if (thisRaw.getStackType() != StackType.INT) throw new ConfusedCFRException("Don't know how to tighten from " + thisRaw + " to " + otherRaw);
        if (otherRaw.getStackType() == StackType.INT) return this.chainIntegralTypes(other);
        throw new IllegalStateException();
    }

    public RawJavaType getRawType() {
        return this.value.getRawType();
    }

    public JavaTypeInstance getJavaTypeInstance() {
        return this.value.getJavaTypeInstance();
    }

    public boolean equals(Object o) {
        throw new UnsupportedOperationException();
    }

    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public String toString() {
        return this.value.getClashState() == ClashState.Clash ? " /* !! */ " : "";
    }

    static class IJTInternal_Impl
    implements IJTInternal {
        private boolean isDelegate = false;
        private final boolean locked;
        private JavaTypeInstance type;
        private final Source source;
        private final int id;
        private IJTInternal delegate;

        private IJTInternal_Impl(JavaTypeInstance type, Source source, boolean locked) {
            this.type = type;
            this.source = source;
            this.id = InferredJavaType.global_id++;
            this.locked = locked;
        }

        @Override
        public RawJavaType getRawType() {
            if (!this.isDelegate) return this.type.getRawTypeOfSimpleType();
            return this.delegate.getRawType();
        }

        @Override
        public JavaTypeInstance getJavaTypeInstance() {
            if (!this.isDelegate) return this.type;
            return this.delegate.getJavaTypeInstance();
        }

        @Override
        public Source getSource() {
            if (!this.isDelegate) return this.source;
            return this.delegate.getSource();
        }

        @Override
        public void collapseTypeClash() {
            if (!this.isDelegate) return;
            this.delegate.collapseTypeClash();
        }

        @Override
        public int getFinalId() {
            if (!this.isDelegate) return this.id;
            return this.delegate.getFinalId();
        }

        @Override
        public int getLocalId() {
            return this.id;
        }

        @Override
        public ClashState getClashState() {
            return ClashState.None;
        }

        @Override
        public void mkDelegate(IJTInternal newDelegate) {
            if (this.isDelegate) {
                this.delegate.mkDelegate(newDelegate);
            } else {
                this.isDelegate = true;
                this.delegate = newDelegate;
            }
        }

        @Override
        public void forceType(JavaTypeInstance rawJavaType, boolean ignoreLock) {
            if (!ignoreLock && this.isLocked()) {
                return;
            }
            if (this.isDelegate && this.delegate.isLocked() && !ignoreLock) {
                this.isDelegate = false;
            }
            if (this.isDelegate) {
                this.delegate.forceType(rawJavaType, ignoreLock);
            } else {
                this.type = rawJavaType;
            }
        }

        @Override
        public void markClashState(ClashState newClashState) {
            throw new UnsupportedOperationException();
        }

        public String toString() {
            if (!this.isDelegate) return "#" + this.id + " " + this.type.toString();
            return "#" + this.id + " -> " + this.delegate.toString();
        }

        @Override
        public boolean isLocked() {
            return this.locked;
        }

        @Override
        public IJTInternal getFirstLocked() {
            if (this.locked) {
                return this;
            }
            if (this.delegate == null) return null;
            return this.delegate.getFirstLocked();
        }

        /* synthetic */ IJTInternal_Impl(JavaTypeInstance x0, Source x1, boolean x2, 1 x3) {
            this(x0, x1, x2);
        }
    }

    static class IJTInternal_Clash
    implements IJTInternal {
        private boolean resolved = false;
        private List<IJTInternal> clashes;
        private final int id = InferredJavaType.access$008();
        private JavaTypeInstance type = null;

        private IJTInternal_Clash(Collection<IJTInternal> clashes) {
            this.clashes = ListFactory.newList(clashes);
        }

        private static Map<JavaTypeInstance, JavaGenericRefTypeInstance> getMatches(List<IJTInternal> clashes) {
            Map matches = MapFactory.newMap();
            IJTInternal clash = clashes.get(0);
            JavaTypeInstance clashType = clash.getJavaTypeInstance();
            BindingSuperContainer otherSupers = clashType.getBindingSupers();
            if (otherSupers != null) {
                Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSupers = otherSupers.getBoundSuperClasses();
                matches.putAll(boundSupers);
            }
            int len = clashes.size();
            for (int x = 1; x < len; ++x) {
                JavaTypeInstance clashType2;
                IJTInternal clash2;
                BindingSuperContainer otherSupers2;
                if ((otherSupers2 = (clashType2 = (clash2 = clashes.get(x)).getJavaTypeInstance()).getBindingSupers()) == null) continue;
                Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSupers = otherSupers2.getBoundSuperClasses();
                Iterator iterator = matches.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry;
                    if (boundSupers.containsKey((entry = iterator.next()).getKey())) continue;
                    iterator.remove();
                }
            }
            return matches;
        }

        private static IJTInternal mkClash(IJTInternal delegate1, IJTInternal delegate2) {
            List clashes = ListFactory.newList();
            if (delegate1 instanceof IJTInternal_Clash) {
                for (IJTInternal clash : ((IJTInternal_Clash)delegate1).clashes) {
                    clashes.add((IJTInternal)clash);
                }
            } else {
                clashes.add((IJTInternal)delegate1);
            }
            if (delegate2 instanceof IJTInternal_Clash) {
                for (IJTInternal clash : ((IJTInternal_Clash)delegate2).clashes) {
                    clashes.add((IJTInternal)clash);
                }
            } else {
                clashes.add((IJTInternal)delegate2);
            }
            Map<JavaTypeInstance, JavaGenericRefTypeInstance> matches = IJTInternal_Clash.getMatches(clashes);
            if (matches.isEmpty()) {
                return new IJTInternal_Impl(TypeConstants.OBJECT, Source.UNKNOWN, true, null);
            }
            if (matches.size() != 1) return new IJTInternal_Clash(clashes);
            return new IJTInternal_Impl(matches.keySet().iterator().next(), Source.UNKNOWN, true, null);
        }

        @Override
        public void collapseTypeClash() {
            Map<JavaTypeInstance, JavaGenericRefTypeInstance> matches;
            if (this.resolved) {
                return;
            }
            if ((matches = IJTInternal_Clash.getMatches(this.clashes)).isEmpty()) {
                this.type = TypeConstants.OBJECT;
                this.resolved = true;
                return;
            }
            List<JavaTypeInstance> poss = ListFactory.newList(matches.keySet());
            boolean effect = true;
            do {
                effect = false;
                for (JavaTypeInstance pos : poss) {
                    Set<JavaRefTypeInstance> supers = SetFactory.newSet(pos.getBindingSupers().getBoundSuperClasses().keySet());
                    supers.remove(pos);
                    if (!poss.removeAll(supers)) continue;
                    effect = true;
                }
            } while (effect);
            JavaTypeInstance oneClash = this.clashes.get(0).getJavaTypeInstance();
            Map<JavaRefTypeInstance, BindingSuperContainer.Route> routes = oneClash.getBindingSupers().getBoundSuperRoute();
            if (poss.isEmpty()) {
                poss = ListFactory.newList(matches.keySet());
            }
            for (JavaTypeInstance pos : poss) {
                if (BindingSuperContainer.Route.EXTENSION != routes.get(pos)) continue;
                this.type = pos;
                this.resolved = true;
                return;
            }
            this.type = poss.get(0);
            this.resolved = true;
        }

        @Override
        public RawJavaType getRawType() {
            if (!this.resolved) return this.clashes.get(0).getRawType();
            return this.type.getRawTypeOfSimpleType();
        }

        @Override
        public JavaTypeInstance getJavaTypeInstance() {
            if (!this.resolved) return this.clashes.get(0).getJavaTypeInstance();
            return this.type;
        }

        @Override
        public Source getSource() {
            return this.clashes.get(0).getSource();
        }

        @Override
        public int getFinalId() {
            return this.id;
        }

        @Override
        public int getLocalId() {
            return this.id;
        }

        @Override
        public ClashState getClashState() {
            if (!this.resolved) return ClashState.Clash;
            return ClashState.Resolved;
        }

        @Override
        public void mkDelegate(IJTInternal newDelegate) {
        }

        @Override
        public void forceType(JavaTypeInstance rawJavaType, boolean ignoreLock) {
            this.type = rawJavaType;
            this.resolved = true;
        }

        @Override
        public void markClashState(ClashState newClashState) {
        }

        @Override
        public boolean isLocked() {
            return this.resolved;
        }

        @Override
        public IJTInternal getFirstLocked() {
            return null;
        }

        public String toString() {
            if (this.resolved) {
                return "#" + this.id + " " + this.type.toString();
            }
            StringBuilder sb = new StringBuilder();
            for (IJTInternal clash : this.clashes) {
                sb.append(this.id).append(" -> ").append(clash.toString()).append(", ");
            }
            return sb.toString();
        }

        /* synthetic */ IJTInternal_Clash(Collection x0, 1 x1) {
            this(x0);
        }
    }

    interface IJTInternal {
        public RawJavaType getRawType();

        public JavaTypeInstance getJavaTypeInstance();

        public Source getSource();

        public int getLocalId();

        public int getFinalId();

        public ClashState getClashState();

        public void collapseTypeClash();

        public void mkDelegate(IJTInternal var1);

        public void forceType(JavaTypeInstance var1, boolean var2);

        public void markClashState(ClashState var1);

        public boolean isLocked();

        public IJTInternal getFirstLocked();
    }

    static enum ClashState {
        None,
        Clash,
        Resolved;
        

        private ClashState() {
        }
    }

    public static enum Source {
        TEST,
        UNKNOWN,
        LITERAL,
        FIELD,
        FUNCTION,
        OPERATION,
        EXPRESSION,
        INSTRUCTION,
        GENERICCALL,
        EXCEPTION,
        STRING_TRANSFORM;
        

        private Source() {
        }
    }

}

