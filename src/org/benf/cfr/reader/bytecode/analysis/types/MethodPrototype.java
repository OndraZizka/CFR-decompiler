/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.types;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdent;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.FormalTypeParameter;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototypeAnnotationsHelper;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.Ident;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.bytecode.analysis.variables.Slot;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.DecompilerComments;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class MethodPrototype
implements TypeUsageCollectable {
    private final List<FormalTypeParameter> formalTypeParameters;
    private final List<JavaTypeInstance> args;
    private final Set<Integer> hidden = SetFactory.newSet();
    private JavaTypeInstance result;
    private final VariableNamer variableNamer;
    private final boolean instanceMethod;
    private final boolean varargs;
    private final String name;
    private final ClassFile classFile;
    private final Method.MethodConstructor constructorFlag;
    private final List<Slot> syntheticArgs = ListFactory.newList();
    private transient List<LocalVariable> parameterLValues = null;

    public MethodPrototype(ClassFile classFile, JavaTypeInstance classType, String name, boolean instanceMethod, Method.MethodConstructor constructorFlag, List<FormalTypeParameter> formalTypeParameters, List<JavaTypeInstance> args, JavaTypeInstance result, boolean varargs, VariableNamer variableNamer, ConstantPool cp) {
        this.formalTypeParameters = formalTypeParameters;
        this.instanceMethod = instanceMethod;
        this.constructorFlag = constructorFlag;
        if (constructorFlag.equals((Object)Method.MethodConstructor.ENUM_CONSTRUCTOR)) {
            List args2 = ListFactory.newList();
            args2.add((JavaRefTypeInstance)TypeConstants.STRING);
            args2.add((RawJavaType)RawJavaType.INT);
            args2.addAll(args);
            this.hide(0);
            this.hide(1);
            args = args2;
        }
        this.args = args;
        JavaTypeInstance resultType = "<init>".equals(name) ? (classFile == null ? classType : null) : result;
        this.result = resultType;
        this.varargs = varargs;
        this.variableNamer = variableNamer;
        this.name = name;
        this.classFile = classFile;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.result);
        collector.collect((Collection<? extends JavaTypeInstance>)this.args);
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.formalTypeParameters);
    }

    public void hide(int x) {
        this.hidden.add(x);
    }

    public boolean isHiddenArg(int x) {
        return this.hidden.contains(x);
    }

    public void dumpDeclarationSignature(Dumper d, String methName, Method.MethodConstructor isConstructor, MethodPrototypeAnnotationsHelper annotationsHelper) {
        if (this.formalTypeParameters != null) {
            d.print('<');
            boolean first = true;
            for (FormalTypeParameter formalTypeParameter : this.formalTypeParameters) {
                first = CommaHelp.comma(first, d);
                d.dump(formalTypeParameter);
            }
            d.print("> ");
        }
        if (!isConstructor.isConstructor()) {
            d.dump(this.result).print(" ");
        }
        d.print(methName).print("(");
        List<LocalVariable> parameterLValues = this.getComputedParameters();
        int argssize = this.args.size();
        boolean first = true;
        for (int i = 0; i < argssize; ++i) {
            JavaTypeInstance arg = this.args.get(i);
            if (this.hidden.contains(i)) continue;
            first = CommaHelp.comma(first, d);
            annotationsHelper.addAnnotationTextForParameterInto(i, d);
            if (this.varargs && i == argssize - 1) {
                if (!(arg instanceof JavaArrayTypeInstance)) {
                    throw new ConfusedCFRException("VARARGS method doesn't have an array as last arg!!");
                }
                ((JavaArrayTypeInstance)arg).toVarargString(d);
            } else {
                d.dump(arg);
            }
            d.print(" ").dump(parameterLValues.get(i).getName());
        }
        d.print(")");
    }

    public boolean parametersComputed() {
        return this.parameterLValues != null;
    }

    public List<LocalVariable> getComputedParameters() {
        if (this.parameterLValues != null) return this.parameterLValues;
        throw new IllegalStateException("Parameters not created");
    }

    public void setSyntheticConstructorParameters(Method.MethodConstructor constructorFlag, DecompilerComments comments, Map<Integer, JavaTypeInstance> synthetics) {
        Slot test;
        this.syntheticArgs.clear();
        int offset = 0;
        switch (constructorFlag) {
            case ENUM_CONSTRUCTOR: {
                offset = 3;
                break;
            }
            default: {
                if (!this.isInstanceMethod()) break;
                offset = 1;
            }
        }
        List tmp = ListFactory.newList();
        for (Map.Entry<Integer, JavaTypeInstance> entry : synthetics.entrySet()) {
            tmp.add((Slot)new Slot(entry.getValue(), entry.getKey()));
        }
        if (tmp.isEmpty()) return;
        if (offset != (test = (Slot)tmp.get(0)).getIdx()) {
            List replacements = ListFactory.newList();
            for (Slot synthetic : tmp) {
                JavaTypeInstance type = synthetic.getJavaTypeInstance();
                Slot replacement = new Slot(type, offset);
                offset+=type.getStackType().getComputationCategory();
                replacements.add((Slot)replacement);
            }
            this.syntheticArgs.addAll(replacements);
            comments.addComment(DecompilerComment.PARAMETER_CORRUPTION);
        } else {
            this.syntheticArgs.addAll(tmp);
        }
    }

    public Map<Slot, SSAIdent> collectInitialSlotUsage(Method.MethodConstructor constructorFlag, SSAIdentifierFactory<Slot> ssaIdentifierFactory) {
        Map res = MapFactory.newLinkedMap();
        int offset = 0;
        int n = 1.$SwitchMap$org$benf$cfr$reader$entities$Method$MethodConstructor[constructorFlag.ordinal()];
        if (this.instanceMethod) {
            Slot tgt = new Slot(this.classFile.getClassType(), 0);
            res.put((Slot)tgt, (SSAIdent)ssaIdentifierFactory.getIdent(tgt));
            offset = 1;
        }
        if (!this.syntheticArgs.isEmpty()) {
            for (Slot synthetic : this.syntheticArgs) {
                if (offset != synthetic.getIdx()) {
                    throw new IllegalStateException("Synthetic arg - offset is " + offset + ", but got " + synthetic.getIdx());
                }
                res.put((Slot)synthetic, (SSAIdent)ssaIdentifierFactory.getIdent(synthetic));
                offset+=synthetic.getJavaTypeInstance().getStackType().getComputationCategory();
            }
        }
        for (JavaTypeInstance arg : this.args) {
            Slot tgt = new Slot(arg, offset);
            res.put((Slot)tgt, (SSAIdent)ssaIdentifierFactory.getIdent(tgt));
            offset+=arg.getStackType().getComputationCategory();
        }
        return res;
    }

    public List<LocalVariable> computeParameters(Method.MethodConstructor constructorFlag, Map<Integer, Ident> slotToIdentMap) {
        if (this.parameterLValues != null) {
            return this.parameterLValues;
        }
        this.parameterLValues = ListFactory.newList();
        int offset = 0;
        if (this.instanceMethod) {
            this.variableNamer.forceName(slotToIdentMap.get(0), 0, "this");
            offset = 1;
        }
        if (constructorFlag != Method.MethodConstructor.ENUM_CONSTRUCTOR) {
            for (Slot synthetic : this.syntheticArgs) {
                JavaTypeInstance typeInstance = synthetic.getJavaTypeInstance();
                this.parameterLValues.add(new LocalVariable(offset, slotToIdentMap.get(synthetic.getIdx()), this.variableNamer, 0, new InferredJavaType(typeInstance, InferredJavaType.Source.FIELD, true), false));
                offset+=typeInstance.getStackType().getComputationCategory();
            }
        }
        for (JavaTypeInstance arg : this.args) {
            this.parameterLValues.add(new LocalVariable(offset, slotToIdentMap.get(offset), this.variableNamer, 0, new InferredJavaType(arg, InferredJavaType.Source.FIELD, true), false));
            offset+=arg.getStackType().getComputationCategory();
        }
        return this.parameterLValues;
    }

    public JavaTypeInstance getReturnType() {
        return this.result;
    }

    public String getName() {
        return this.name;
    }

    public boolean hasFormalTypeParameters() {
        return !(this.formalTypeParameters == null || this.formalTypeParameters.isEmpty());
    }

    public JavaTypeInstance getClassType() {
        if (this.classFile != null) return this.classFile.getClassType();
        return null;
    }

    public JavaTypeInstance getReturnType(JavaTypeInstance thisTypeInstance, List<Expression> invokingArgs) {
        if (this.classFile == null) {
            return this.result;
        }
        if (this.result == null) {
            if (!"<init>".equals(this.getName())) {
                throw new IllegalStateException();
            }
            this.result = this.classFile.getClassSignature().getThisGeneralTypeClass(this.classFile.getClassType(), this.classFile.getConstantPool());
        }
        if (!this.hasFormalTypeParameters() && !this.classFile.hasFormalTypeParameters()) return this.result;
        JavaGenericRefTypeInstance genericRefTypeInstance = null;
        if (thisTypeInstance instanceof JavaGenericRefTypeInstance) {
            genericRefTypeInstance = (JavaGenericRefTypeInstance)thisTypeInstance;
        }
        JavaTypeInstance boundResult = this.getResultBoundAccordingly(this.result, genericRefTypeInstance, invokingArgs);
        return boundResult;
    }

    public List<JavaTypeInstance> getArgs() {
        return this.args;
    }

    public int getVisibleArgCount() {
        return this.args.size() - this.hidden.size();
    }

    public boolean isInstanceMethod() {
        return this.instanceMethod;
    }

    public Expression getAppropriatelyCastedArgument(Expression expression, int argidx) {
        RawJavaType providedRawJavaType;
        RawJavaType expectedRawJavaType;
        JavaTypeInstance type = this.args.get(argidx);
        if (type.isComplexType()) {
            return expression;
        }
        if ((expectedRawJavaType = type.getRawTypeOfSimpleType()).compareAllPriorityTo(providedRawJavaType = expression.getInferredJavaType().getRawType()) != 0) return new CastExpression(new InferredJavaType(expectedRawJavaType, InferredJavaType.Source.EXPRESSION, true), expression);
        return expression;
    }

    public Dumper dumpAppropriatelyCastedArgumentString(Expression expression, int argidx, Dumper d) {
        return expression.dump(d);
    }

    public void tightenArgs(Expression object, List<Expression> expressions) {
        if (expressions.size() != this.args.size()) {
            throw new ConfusedCFRException("expr arg size mismatch");
        }
        if (!(object == null || this.classFile == null || "<init>".equals(this.name))) {
            object.getInferredJavaType().noteUseAs(this.classFile.getClassType());
        }
        int length = this.args.size();
        for (int x = 0; x < length; ++x) {
            Expression expression = expressions.get(x);
            JavaTypeInstance type = this.args.get(x);
            expression.getInferredJavaType().useAsWithoutCasting(type);
        }
    }

    public void addExplicitCasts(Expression object, List<Expression> expressions) {
        int length = expressions.size();
        GenericTypeBinder genericTypeBinder = null;
        if (object != null && object.getInferredJavaType().getJavaTypeInstance() instanceof JavaGenericBaseInstance) {
            List invokingTypes = ListFactory.newList();
            for (Expression invokingArg : expressions) {
                invokingTypes.add((JavaTypeInstance)invokingArg.getInferredJavaType().getJavaTypeInstance());
            }
            JavaGenericRefTypeInstance boundInstance = object instanceof JavaGenericRefTypeInstance ? (JavaGenericRefTypeInstance)object : null;
            if (this.classFile != null) {
                genericTypeBinder = GenericTypeBinder.bind(this.formalTypeParameters, this.classFile.getClassSignature(), this.args, boundInstance, invokingTypes);
            }
        }
        for (int x = 0; x < length; ++x) {
            JavaTypeInstance exprType;
            Expression expression = expressions.get(x);
            JavaTypeInstance type = this.args.get(x);
            if (MethodPrototype.isGenericArg(exprType = expression.getInferredJavaType().getJavaTypeInstance())) continue;
            if (genericTypeBinder != null) {
                type = genericTypeBinder.getBindingFor(type);
            }
            if (MethodPrototype.isGenericArg(type)) continue;
            expressions.set(x, new CastExpression(new InferredJavaType(type, InferredJavaType.Source.FUNCTION, true), expression));
        }
    }

    private static boolean isGenericArg(JavaTypeInstance arg) {
        arg = arg.getArrayStrippedType();
        if (!(arg instanceof JavaGenericBaseInstance)) return false;
        return true;
    }

    public String getComparableString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName()).append('(');
        for (JavaTypeInstance arg : this.args) {
            sb.append(arg.getRawName()).append(" ");
        }
        sb.append(')');
        return sb.toString();
    }

    public String toString() {
        return this.getComparableString();
    }

    public boolean equalsGeneric(MethodPrototype other) {
        GenericTypeBinder genericTypeBinder = GenericTypeBinder.createEmpty();
        return this.equalsGeneric(other, genericTypeBinder);
    }

    public boolean equalsGeneric(MethodPrototype other, GenericTypeBinder genericTypeBinder) {
        List<FormalTypeParameter> otherTypeParameters = other.formalTypeParameters;
        List<JavaTypeInstance> otherArgs = other.args;
        if (otherArgs.size() != this.args.size()) {
            return false;
        }
        for (int x = 0; x < this.args.size(); ++x) {
            JavaTypeInstance deGenerifiedLhs;
            JavaTypeInstance deGenerifiedRhs;
            JavaTypeInstance rhs;
            JavaTypeInstance lhs = this.args.get(x);
            if ((deGenerifiedLhs = lhs.getDeGenerifiedType()).equals(deGenerifiedRhs = (rhs = otherArgs.get(x)).getDeGenerifiedType())) continue;
            if (!(lhs instanceof JavaGenericBaseInstance)) return false;
            if (((JavaGenericBaseInstance)lhs).tryFindBinding(rhs, genericTypeBinder)) continue;
            return false;
        }
        return true;
    }

    public GenericTypeBinder getTypeBinderForTypes(List<JavaTypeInstance> invokingArgTypes) {
        if (this.classFile == null) {
            return null;
        }
        if (invokingArgTypes.size() != this.args.size()) {
            return null;
        }
        GenericTypeBinder genericTypeBinder = GenericTypeBinder.bind(this.formalTypeParameters, this.classFile.getClassSignature(), this.args, null, invokingArgTypes);
        return genericTypeBinder;
    }

    public GenericTypeBinder getTypeBinderFor(List<Expression> invokingArgs) {
        List invokingTypes = ListFactory.newList();
        for (Expression invokingArg : invokingArgs) {
            invokingTypes.add((JavaTypeInstance)invokingArg.getInferredJavaType().getJavaTypeInstance());
        }
        return this.getTypeBinderForTypes(invokingTypes);
    }

    private JavaTypeInstance getResultBoundAccordingly(JavaTypeInstance result, JavaGenericRefTypeInstance boundInstance, List<Expression> invokingArgs) {
        if (!(result instanceof JavaArrayTypeInstance)) return this.getResultBoundAccordinglyInner(result, boundInstance, invokingArgs);
        JavaArrayTypeInstance arrayTypeInstance = (JavaArrayTypeInstance)result;
        JavaTypeInstance stripped = result.getArrayStrippedType();
        JavaTypeInstance tmp = this.getResultBoundAccordinglyInner(stripped, boundInstance, invokingArgs);
        if (tmp != stripped) return new JavaArrayTypeInstance(arrayTypeInstance.getNumArrayDimensions(), tmp);
        return result;
    }

    private JavaTypeInstance getResultBoundAccordinglyInner(JavaTypeInstance result, JavaGenericRefTypeInstance boundInstance, List<Expression> invokingArgs) {
        GenericTypeBinder genericTypeBinder;
        if (!(result instanceof JavaGenericBaseInstance)) {
            return result;
        }
        List invokingTypes = ListFactory.newList();
        for (Expression invokingArg : invokingArgs) {
            invokingTypes.add((JavaTypeInstance)invokingArg.getInferredJavaType().getJavaTypeInstance());
        }
        if ((genericTypeBinder = GenericTypeBinder.bind(this.formalTypeParameters, this.classFile.getClassSignature(), this.args, boundInstance, invokingTypes)) == null) {
            return result;
        }
        JavaGenericBaseInstance genericResult = (JavaGenericBaseInstance)result;
        return genericResult.getBoundInstance(genericTypeBinder);
    }

    public boolean isVarArgs() {
        return this.varargs;
    }

    public boolean equalsMatch(MethodPrototype other) {
        List<JavaTypeInstance> otherArgs;
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.args.equals(otherArgs = other.getArgs())) return true;
        return false;
    }

}

