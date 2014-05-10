/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import org.benf.cfr.reader.bytecode.BytecodeMeta;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Graph;
import org.benf.cfr.reader.bytecode.analysis.opgraph.GraphConversionHelper;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticMonOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayLength;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CompOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ComparisonOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.DynamicInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.InstanceOfExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewObject;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewObjectArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewPrimitiveArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StackValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.SuperFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.ArrayVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.FieldVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CatchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CompoundStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ConstructorStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ExpressionStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.GotoStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.IfStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.JSRCallStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.JSRRetStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.MonitorEnterStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.MonitorExitStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.Nop;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.RawSwitchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ReturnNothingStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ReturnValueStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ThrowStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.TryStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdent;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntryHolder;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.DynamicInvokeType;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.CastAction;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.Ident;
import org.benf.cfr.reader.bytecode.analysis.variables.Slot;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableFactory;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamerDefault;
import org.benf.cfr.reader.bytecode.opcode.DecodedLookupSwitch;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitch;
import org.benf.cfr.reader.bytecode.opcode.DecodedTableSwitch;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.entities.AccessFlagMethod;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.attributes.AttributeBootstrapMethods;
import org.benf.cfr.reader.entities.bootstrap.BootstrapMethodInfo;
import org.benf.cfr.reader.entities.bootstrap.MethodHandleBehaviour;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryInvokeDynamic;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.entities.exceptions.ExceptionAggregator;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.BinaryPredicate;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.DecompilerComments;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.SetUtil;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.functors.BinaryProcedure;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.graph.GraphVisitor;
import org.benf.cfr.reader.util.graph.GraphVisitorDFS;
import org.benf.cfr.reader.util.graph.GraphVisitorFIFO;
import org.benf.cfr.reader.util.lambda.LambdaUtils;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.LoggerFactory;
import org.benf.cfr.reader.util.output.ToStringDumper;

public class Op02WithProcessedDataAndRefs
implements Dumpable,
Graph<Op02WithProcessedDataAndRefs> {
    private static final Logger logger = LoggerFactory.create(Op02WithProcessedDataAndRefs.class);
    private InstrIndex index;
    private JVMInstr instr;
    private final int originalRawOffset;
    private final byte[] rawData;
    private List<BlockIdentifier> containedInTheseBlocks = ListFactory.newList();
    private List<ExceptionGroup> exceptionGroups = ListFactory.newList();
    private List<ExceptionGroup.Entry> catchExceptionGroups = ListFactory.newList();
    private final List<Op02WithProcessedDataAndRefs> targets = ListFactory.newList();
    private final List<Op02WithProcessedDataAndRefs> sources = ListFactory.newList();
    private final ConstantPool cp;
    private final ConstantPoolEntry[] cpEntries;
    private long stackDepthBeforeExecution = -1;
    private long stackDepthAfterExecution;
    private final List<StackEntryHolder> stackConsumed = ListFactory.newList();
    private final List<StackEntryHolder> stackProduced = ListFactory.newList();
    private StackSim unconsumedJoinedStack = null;
    private SSAIdentifiers<Slot> ssaIdentifiers;
    private Map<Integer, Ident> localVariablesBySlot = MapFactory.newLinkedMap();

    private Op02WithProcessedDataAndRefs(Op02WithProcessedDataAndRefs other) {
        this.instr = other.instr;
        this.rawData = other.rawData;
        this.index = null;
        this.cp = other.cp;
        this.cpEntries = other.cpEntries;
        this.originalRawOffset = other.originalRawOffset;
    }

    public Op02WithProcessedDataAndRefs(JVMInstr instr, byte[] rawData, int index, ConstantPool cp, ConstantPoolEntry[] cpEntries, int originalRawOffset) {
        this(instr, rawData, new InstrIndex(index), cp, cpEntries, originalRawOffset);
    }

    public Op02WithProcessedDataAndRefs(JVMInstr instr, byte[] rawData, InstrIndex index, ConstantPool cp, ConstantPoolEntry[] cpEntries, int originalRawOffset) {
        this.instr = instr;
        this.rawData = rawData;
        this.index = index;
        this.cp = cp;
        this.cpEntries = cpEntries;
        this.originalRawOffset = originalRawOffset;
    }

    public void resetStackInfo() {
        this.stackDepthBeforeExecution = -1;
        this.stackDepthAfterExecution = -1;
        this.stackConsumed.clear();
        this.stackProduced.clear();
        this.unconsumedJoinedStack = null;
    }

    public InstrIndex getIndex() {
        return this.index;
    }

    public void setIndex(InstrIndex index) {
        this.index = index;
    }

    public void addTarget(Op02WithProcessedDataAndRefs node) {
        this.targets.add(node);
    }

    public void removeTarget(Op02WithProcessedDataAndRefs node) {
        if (this.targets.remove(node)) return;
        throw new ConfusedCFRException("Invalid target, tried to remove " + node + "\nfrom " + this + "\nbut was not a target.");
    }

    public void addSource(Op02WithProcessedDataAndRefs node) {
        this.sources.add(node);
    }

    public JVMInstr getInstr() {
        return this.instr;
    }

    public void replaceTarget(Op02WithProcessedDataAndRefs oldTarget, Op02WithProcessedDataAndRefs newTarget) {
        int index = this.targets.indexOf(oldTarget);
        if (index == -1) {
            throw new ConfusedCFRException("Invalid target");
        }
        this.targets.set(index, newTarget);
    }

    public void replaceSource(Op02WithProcessedDataAndRefs oldSource, Op02WithProcessedDataAndRefs newSource) {
        int index = this.sources.indexOf(oldSource);
        if (index == -1) {
            throw new ConfusedCFRException("Invalid source");
        }
        this.sources.set(index, newSource);
    }

    public void removeSource(Op02WithProcessedDataAndRefs oldSource) {
        if (this.sources.remove(oldSource)) return;
        throw new ConfusedCFRException("Invalid source");
    }

    public void clearSources() {
        this.sources.clear();
    }

    private int getInstrArgByte(int index) {
        return this.rawData[index];
    }

    private int getInstrArgShort(int index) {
        BaseByteData tmp = new BaseByteData(this.rawData);
        return tmp.getS2At((long)index);
    }

    @Override
    public List<Op02WithProcessedDataAndRefs> getTargets() {
        return this.targets;
    }

    @Override
    public List<Op02WithProcessedDataAndRefs> getSources() {
        return this.sources;
    }

    public ConstantPoolEntry[] getCpEntries() {
        return this.cpEntries;
    }

    public void populateStackInfo(StackSim stackSim, Method method, LinkedList<Pair<StackSim, Op02WithProcessedDataAndRefs>> next) {
        StackDelta stackDelta = this.instr.getStackDelta(this.rawData, this.cpEntries, stackSim, method);
        if (this.stackDepthBeforeExecution != -1) {
            if (this.instr == JVMInstr.FAKE_CATCH) {
                return;
            }
            if (stackSim.getDepth() != this.stackDepthBeforeExecution) {
                throw new ConfusedCFRException("Invalid stack depths @ " + this + " : trying to set " + stackSim.getDepth() + " previously set to " + this.stackDepthBeforeExecution);
            }
            List alsoConsumed = ListFactory.newList();
            List alsoProduced = ListFactory.newList();
            StackSim newStackSim = stackSim.getChange(stackDelta, alsoConsumed, alsoProduced);
            if (alsoConsumed.size() != this.stackConsumed.size()) {
                throw new ConfusedCFRException("Unexpected stack sizes on merge");
            }
            for (int i = 0; i < this.stackConsumed.size(); ++i) {
                this.stackConsumed.get(i).mergeWith((StackEntryHolder)alsoConsumed.get(i));
            }
            if (this.unconsumedJoinedStack == null) return;
            long depth = this.unconsumedJoinedStack.getDepth() - (long)alsoProduced.size();
            List<StackEntryHolder> unconsumedEntriesOld = this.unconsumedJoinedStack.getHolders(alsoProduced.size(), depth);
            List<StackEntryHolder> unconsumedEntriesNew = newStackSim.getHolders(alsoProduced.size(), depth);
            for (int i2 = 0; i2 < unconsumedEntriesOld.size(); ++i2) {
                unconsumedEntriesOld.get(i2).mergeWith(unconsumedEntriesNew.get(i2));
            }
        } else {
            this.stackDepthBeforeExecution = this.instr == JVMInstr.FAKE_CATCH ? 0 : stackSim.getDepth();
            this.stackDepthAfterExecution = this.stackDepthBeforeExecution + stackDelta.getChange();
            StackSim newStackSim = stackSim.getChange(stackDelta, this.stackConsumed, this.stackProduced);
            if (this.sources.size() > 1 && newStackSim.getDepth() > (long)this.stackProduced.size()) {
                this.unconsumedJoinedStack = newStackSim;
            }
            for (int i = this.targets.size() - 1; i >= 0; --i) {
                next.addFirst(Pair.make(newStackSim, this.targets.get(i)));
            }
        }
    }

    public ExceptionGroup getSingleExceptionGroup() {
        if (this.exceptionGroups.size() == 1) return this.exceptionGroups.iterator().next();
        throw new ConfusedCFRException("Only expecting statement to be tagged with 1 exceptionGroup");
    }

    @Override
    public Dumper dump(Dumper d) {
        for (BlockIdentifier blockIdentifier : this.containedInTheseBlocks) {
            d.print(" " + blockIdentifier);
        }
        d.print(" " + this.index + " (" + this.originalRawOffset + ") : " + (Object)this.instr + "\t Stack:" + this.stackDepthBeforeExecution + "\t");
        d.print("Consumes:[");
        for (StackEntryHolder stackEntryHolder2 : this.stackConsumed) {
            d.print(stackEntryHolder2.toString() + " ");
        }
        d.print("] Produces:[");
        for (StackEntryHolder stackEntryHolder2 : this.stackProduced) {
            d.print(stackEntryHolder2.toString() + " ");
        }
        d.print("] sources ");
        for (Op02WithProcessedDataAndRefs source : this.sources) {
            d.print(" " + source.index);
        }
        d.print(" targets ");
        for (Op02WithProcessedDataAndRefs target : this.targets) {
            d.print(" " + target.index);
        }
        d.print("\n");
        return d;
    }

    private Statement buildInvoke(Method thisCallerMethod) {
        JavaTypeInstance type;
        ConstantPoolEntryMethodRef function = (ConstantPoolEntryMethodRef)this.cpEntries[0];
        StackValue object = this.getStackRValue(this.stackConsumed.size() - 1);
        boolean special = false;
        boolean isSuper = false;
        if (this.instr == JVMInstr.INVOKESPECIAL) {
            special = true;
            if (!thisCallerMethod.testAccessFlag(AccessFlagMethod.ACC_STATIC)) {
                JavaTypeInstance objType = object.getInferredJavaType().getJavaTypeInstance();
                JavaTypeInstance callType = function.getClassEntry().getTypeInstance();
                ConstantPoolEntryNameAndType nameAndType = function.getNameAndTypeEntry();
                String funcName = nameAndType.getName().getValue();
                boolean typesMatch = callType.equals(objType);
                if (funcName.equals("<init>")) {
                    if (!(typesMatch || objType.getRawName().equals("java.lang.Object"))) {
                        isSuper = true;
                    }
                } else if (!typesMatch) {
                    isSuper = true;
                }
            }
        }
        MethodPrototype methodPrototype = function.getMethodPrototype();
        List<Expression> args = this.getNStackRValuesAsExpressions(this.stackConsumed.size() - 1);
        methodPrototype.tightenArgs(object, args);
        methodPrototype.addExplicitCasts(object, args);
        AbstractFunctionInvokation funcCall = isSuper ? new SuperFunctionInvokation(this.cp, function, methodPrototype, object, args) : new MemberFunctionInvokation(this.cp, function, methodPrototype, object, special, args);
        if (object.getInferredJavaType().getJavaTypeInstance() == RawJavaType.NULL && (type = methodPrototype.getClassType()) != null) {
            object.getInferredJavaType().chain(new InferredJavaType(type, InferredJavaType.Source.FUNCTION));
        }
        if (!isSuper && function.isInitMethod()) {
            return new ConstructorStatement((MemberFunctionInvokation)funcCall);
        }
        if (this.stackProduced.size() != 0) return new AssignmentSimple(this.getStackLValue(0), funcCall);
        return new ExpressionStatement(funcCall);
    }

    private Statement buildInvokeDynamic(Method method, DCCommonState dcCommonState) {
        List<Expression> callargs;
        ConstantPoolEntryInvokeDynamic invokeDynamic = (ConstantPoolEntryInvokeDynamic)this.cpEntries[0];
        ConstantPoolEntryNameAndType nameAndType = invokeDynamic.getNameAndTypeEntry();
        ConstantPoolEntryUTF8 descriptor = nameAndType.getDescriptor();
        MethodPrototype dynamicPrototype = ConstantPoolUtils.parseJavaMethodPrototype(null, null, "", false, Method.MethodConstructor.NOT, descriptor, this.cp, false, new VariableNamerDefault());
        short idx = invokeDynamic.getBootstrapMethodAttrIndex();
        BootstrapMethodInfo bootstrapMethodInfo = method.getClassFile().getBootstrapMethods().getBootStrapMethodInfo(idx);
        ConstantPoolEntryMethodRef methodRef = bootstrapMethodInfo.getConstantPoolEntryMethodRef();
        MethodPrototype prototype = methodRef.getMethodPrototype();
        MethodHandleBehaviour bootstrapBehaviour = bootstrapMethodInfo.getMethodHandleBehaviour();
        String methodName = methodRef.getName();
        DynamicInvokeType dynamicInvokeType = DynamicInvokeType.lookup(methodName);
        if (dynamicInvokeType == DynamicInvokeType.UNKNOWN) {
            throw new IllegalStateException("MetaFactory usage [" + methodName + "] not recognised.");
        }
        switch (dynamicInvokeType) {
            case METAFACTORY_1: 
            case METAFACTORY_2: {
                callargs = this.buildInvokeDynamicMetaFactoryArgs(prototype, dynamicPrototype, bootstrapBehaviour, bootstrapMethodInfo, methodRef);
                break;
            }
            case ALTMETAFACTORY_1: 
            case ALTMETAFACTORY_2: {
                callargs = this.buildInvokeDynamicAltMetaFactoryArgs(prototype, dynamicPrototype, bootstrapBehaviour, bootstrapMethodInfo, methodRef);
                break;
            }
            default: {
                throw new IllegalStateException();
            }
        }
        Expression strippedType = callargs.get(3);
        Expression instantiatedType = callargs.get(5);
        JavaTypeInstance callSiteReturnType = dynamicPrototype.getReturnType();
        callSiteReturnType = this.determineDynamicGeneric(callSiteReturnType, dynamicPrototype, strippedType, instantiatedType, dcCommonState);
        List<Expression> dynamicArgs = this.getNStackRValuesAsExpressions(this.stackConsumed.size());
        dynamicPrototype.tightenArgs(null, dynamicArgs);
        dynamicPrototype.addExplicitCasts(null, dynamicArgs);
        AbstractExpression funcCall = null;
        switch (15.$SwitchMap$org$benf$cfr$reader$entities$bootstrap$MethodHandleBehaviour[bootstrapBehaviour.ordinal()]) {
            case 1: {
                funcCall = new StaticFunctionInvokation(methodRef, callargs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Only static invoke dynamic calls supported currently. This is " + (Object)bootstrapBehaviour);
            }
        }
        funcCall = new DynamicInvokation(new InferredJavaType(callSiteReturnType, InferredJavaType.Source.OPERATION), funcCall, dynamicArgs);
        if (this.stackProduced.size() != 0) return new AssignmentSimple(this.getStackLValue(0), funcCall);
        return new ExpressionStatement(funcCall);
    }

    private JavaTypeInstance determineDynamicGeneric(JavaTypeInstance callsiteReturn, MethodPrototype proto, Expression stripped, Expression instantiated, DCCommonState dcCommonState) {
        BindingSuperContainer b;
        List<Method> methods;
        ClassFile classFile = null;
        try {
            classFile = dcCommonState.getClassFile(proto.getReturnType());
        }
        catch (CannotLoadClassException e) {
            // empty catch block
        }
        if (classFile == null) {
            return callsiteReturn;
        }
        if ((methods = Functional.filter(classFile.getMethods(), new Predicate<Method>(){

            @Override
            public boolean test(Method in) {
                return !in.hasCodeAttribute();
            }
        })).size() != 1) {
            return callsiteReturn;
        }
        Method method = methods.get(0);
        MethodPrototype genericProto = method.getMethodPrototype();
        MethodPrototype boundProto = LambdaUtils.getLiteralProto(instantiated);
        GenericTypeBinder gtb = genericProto.getTypeBinderForTypes(boundProto.getArgs());
        JavaTypeInstance unboundReturn = genericProto.getReturnType();
        JavaTypeInstance boundReturn = boundProto.getReturnType();
        if (unboundReturn instanceof JavaGenericBaseInstance) {
            GenericTypeBinder gtb2 = GenericTypeBinder.extractBindings((JavaGenericBaseInstance)unboundReturn, boundReturn);
            gtb = gtb.mergeWith(gtb2, true);
        }
        JavaTypeInstance classType = classFile.getClassType();
        if ((classType = (b = classFile.getBindingSupers()).getBoundSuperForBase(classType)) == null) {
            return callsiteReturn;
        }
        if (!callsiteReturn.getDeGenerifiedType().equals(classType.getDeGenerifiedType())) {
            return callsiteReturn;
        }
        JavaTypeInstance alternateCallSite = gtb.getBindingFor(classType);
        return alternateCallSite;
    }

    private static TypedLiteral getBootstrapArg(ConstantPoolEntry[] bootstrapArguments, int x, ConstantPool cp) {
        ConstantPoolEntry entry = bootstrapArguments[x];
        TypedLiteral typedLiteral = TypedLiteral.getConstantPoolEntry(cp, entry);
        return typedLiteral;
    }

    private List<Expression> buildInvokeDynamicAltMetaFactoryArgs(MethodPrototype prototype, MethodPrototype dynamicPrototype, MethodHandleBehaviour bootstrapBehaviour, BootstrapMethodInfo bootstrapMethodInfo, ConstantPoolEntryMethodRef methodRef) {
        List<JavaTypeInstance> argTypes = prototype.getArgs();
        ConstantPoolEntry[] bootstrapArguments = bootstrapMethodInfo.getBootstrapArguments();
        if (bootstrapArguments.length < 4) {
            throw new IllegalStateException("Dynamic invoke arg count mismatch ");
        }
        List callargs = ListFactory.newList();
        Literal nullExp = new Literal(TypedLiteral.getNull());
        callargs.add((Literal)nullExp);
        callargs.add((Literal)nullExp);
        callargs.add((Literal)nullExp);
        TypedLiteral tlMethodType = Op02WithProcessedDataAndRefs.getBootstrapArg(bootstrapArguments, 0, this.cp);
        TypedLiteral tlImplMethod = Op02WithProcessedDataAndRefs.getBootstrapArg(bootstrapArguments, 1, this.cp);
        TypedLiteral tlInstantiatedMethodType = Op02WithProcessedDataAndRefs.getBootstrapArg(bootstrapArguments, 2, this.cp);
        TypedLiteral flags = Op02WithProcessedDataAndRefs.getBootstrapArg(bootstrapArguments, 3, this.cp);
        callargs.add((Literal)new Literal(tlMethodType));
        callargs.add((Literal)new Literal(tlImplMethod));
        callargs.add((Literal)new Literal(tlInstantiatedMethodType));
        return callargs;
    }

    private List<Expression> buildInvokeDynamicMetaFactoryArgs(MethodPrototype prototype, MethodPrototype dynamicPrototype, MethodHandleBehaviour bootstrapBehaviour, BootstrapMethodInfo bootstrapMethodInfo, ConstantPoolEntryMethodRef methodRef) {
        int ARG_OFFSET = 3;
        List<JavaTypeInstance> argTypes = prototype.getArgs();
        ConstantPoolEntry[] bootstrapArguments = bootstrapMethodInfo.getBootstrapArguments();
        if (bootstrapArguments.length + 3 != argTypes.size()) {
            throw new IllegalStateException("Dynamic invoke arg count mismatch " + bootstrapArguments.length + "(+3) vs " + argTypes.size());
        }
        List callargs = ListFactory.newList();
        Literal nullExp = new Literal(TypedLiteral.getNull());
        callargs.add((Literal)nullExp);
        callargs.add((Literal)nullExp);
        callargs.add((Literal)nullExp);
        for (int x = 0; x < bootstrapArguments.length; ++x) {
            TypedLiteral typedLiteral;
            JavaTypeInstance expected = argTypes.get(3 + x);
            if (!expected.equals((typedLiteral = Op02WithProcessedDataAndRefs.getBootstrapArg(bootstrapArguments, x, this.cp)).getInferredJavaType().getJavaTypeInstance())) {
                throw new IllegalStateException("Dynamic invoke Expected " + expected + ", got " + typedLiteral);
            }
            callargs.add((Literal)new Literal(typedLiteral));
        }
        return callargs;
    }

    private Pair<JavaTypeInstance, Integer> getRetrieveType() {
        RawJavaType type = null;
        switch (this.instr) {
            case ALOAD: 
            case ALOAD_0: 
            case ALOAD_1: 
            case ALOAD_2: 
            case ALOAD_3: 
            case ALOAD_WIDE: {
                type = RawJavaType.REF;
                break;
            }
            case ILOAD: 
            case ILOAD_0: 
            case ILOAD_1: 
            case ILOAD_2: 
            case ILOAD_3: 
            case ILOAD_WIDE: 
            case IINC: 
            case IINC_WIDE: {
                type = RawJavaType.INT;
                break;
            }
            case LLOAD: 
            case LLOAD_0: 
            case LLOAD_1: 
            case LLOAD_2: 
            case LLOAD_3: 
            case LLOAD_WIDE: {
                type = RawJavaType.LONG;
                break;
            }
            case DLOAD: 
            case DLOAD_0: 
            case DLOAD_1: 
            case DLOAD_2: 
            case DLOAD_3: 
            case DLOAD_WIDE: {
                type = RawJavaType.DOUBLE;
                break;
            }
            case FLOAD: 
            case FLOAD_0: 
            case FLOAD_1: 
            case FLOAD_2: 
            case FLOAD_3: 
            case FLOAD_WIDE: {
                type = RawJavaType.FLOAT;
                break;
            }
            default: {
                return null;
            }
        }
        int idx = 0;
        switch (15.$SwitchMap$org$benf$cfr$reader$bytecode$opcode$JVMInstr[this.instr.ordinal()]) {
            case 1: 
            case 7: 
            case 13: 
            case 15: 
            case 21: 
            case 27: {
                idx = this.getInstrArgByte(0);
                break;
            }
            case 2: 
            case 8: 
            case 16: 
            case 22: 
            case 28: {
                idx = 0;
                break;
            }
            case 3: 
            case 9: 
            case 17: 
            case 23: 
            case 29: {
                idx = 1;
                break;
            }
            case 4: 
            case 10: 
            case 18: 
            case 24: 
            case 30: {
                idx = 2;
                break;
            }
            case 5: 
            case 11: 
            case 19: 
            case 25: 
            case 31: {
                idx = 3;
                break;
            }
            case 6: 
            case 12: 
            case 20: 
            case 26: 
            case 32: {
                throw new UnsupportedOperationException("LOAD_WIDE");
            }
            default: {
                return null;
            }
        }
        return Pair.make(type, idx);
    }

    private Pair<JavaTypeInstance, Integer> getStorageType() {
        RawJavaType type = null;
        switch (this.instr) {
            case ASTORE: 
            case ASTORE_0: 
            case ASTORE_1: 
            case ASTORE_2: 
            case ASTORE_3: 
            case ASTORE_WIDE: {
                type = RawJavaType.REF;
                break;
            }
            case IINC: 
            case IINC_WIDE: 
            case ISTORE: 
            case ISTORE_0: 
            case ISTORE_1: 
            case ISTORE_2: 
            case ISTORE_3: 
            case ISTORE_WIDE: {
                type = RawJavaType.INT;
                break;
            }
            case LSTORE: 
            case LSTORE_0: 
            case LSTORE_1: 
            case LSTORE_2: 
            case LSTORE_3: 
            case LSTORE_WIDE: {
                type = RawJavaType.LONG;
                break;
            }
            case DSTORE: 
            case DSTORE_0: 
            case DSTORE_1: 
            case DSTORE_2: 
            case DSTORE_3: 
            case DSTORE_WIDE: {
                type = RawJavaType.DOUBLE;
                break;
            }
            case FSTORE: 
            case FSTORE_0: 
            case FSTORE_1: 
            case FSTORE_2: 
            case FSTORE_3: 
            case FSTORE_WIDE: {
                type = RawJavaType.FLOAT;
                break;
            }
            default: {
                return null;
            }
        }
        int idx = 0;
        switch (15.$SwitchMap$org$benf$cfr$reader$bytecode$opcode$JVMInstr[this.instr.ordinal()]) {
            case 13: 
            case 33: 
            case 39: 
            case 45: 
            case 51: 
            case 57: {
                idx = this.getInstrArgByte(0);
                break;
            }
            case 34: 
            case 40: 
            case 46: 
            case 52: 
            case 58: {
                idx = 0;
                break;
            }
            case 35: 
            case 41: 
            case 47: 
            case 53: 
            case 59: {
                idx = 1;
                break;
            }
            case 36: 
            case 42: 
            case 48: 
            case 54: 
            case 60: {
                idx = 2;
                break;
            }
            case 37: 
            case 43: 
            case 49: 
            case 55: 
            case 61: {
                idx = 3;
                break;
            }
            case 14: {
                idx = this.getInstrArgShort(1);
                break;
            }
            case 38: 
            case 44: 
            case 50: 
            case 56: 
            case 62: {
                throw new UnsupportedOperationException("STORE_WIDE");
            }
            default: {
                return null;
            }
        }
        return Pair.make(type, idx);
    }

    private Statement mkAssign(VariableFactory variableFactory) {
        Pair<JavaTypeInstance, Integer> storageTypeAndIdx = this.getStorageType();
        int slot = storageTypeAndIdx.getSecond();
        Ident ident = this.localVariablesBySlot.get(slot);
        SSAIdent ssaIdent = this.ssaIdentifiers.getSSAIdent(new Slot(storageTypeAndIdx.getFirst(), slot));
        AssignmentSimple res = new AssignmentSimple(variableFactory.localVariable(slot, ident, this.originalRawOffset, ssaIdent.card() == 1), this.getStackRValue(0));
        if (!this.ssaIdentifiers.isInitialAssign()) return res;
        res.setInitialAssign(true);
        return res;
    }

    private Statement mkRetrieve(VariableFactory variableFactory) {
        Pair<JavaTypeInstance, Integer> storageTypeAndIdx = this.getRetrieveType();
        int slot = storageTypeAndIdx.getSecond();
        Ident ident = this.localVariablesBySlot.get(slot);
        SSAIdent ssaIdent = this.ssaIdentifiers.getSSAIdent(new Slot(storageTypeAndIdx.getFirst(), slot));
        return new AssignmentSimple(this.getStackLValue(0), new LValueExpression(variableFactory.localVariable(slot, ident, this.originalRawOffset, ssaIdent.card() == 1)));
    }

    public Statement createStatement(Method method, VariableFactory variableFactory, BlockIdentifierFactory blockIdentifierFactory, DCCommonState dcCommonState) {
        switch (this.instr) {
            case ALOAD: 
            case ALOAD_0: 
            case ALOAD_1: 
            case ALOAD_2: 
            case ALOAD_3: 
            case ILOAD: 
            case ILOAD_0: 
            case ILOAD_1: 
            case ILOAD_2: 
            case ILOAD_3: 
            case LLOAD: 
            case LLOAD_0: 
            case LLOAD_1: 
            case LLOAD_2: 
            case LLOAD_3: 
            case DLOAD: 
            case DLOAD_0: 
            case DLOAD_1: 
            case DLOAD_2: 
            case DLOAD_3: 
            case FLOAD: 
            case FLOAD_0: 
            case FLOAD_1: 
            case FLOAD_2: 
            case FLOAD_3: {
                return this.mkRetrieve(variableFactory);
            }
            case ACONST_NULL: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getNull()));
            }
            case ICONST_M1: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(-1)));
            }
            case ICONST_0: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getBoolean(0)));
            }
            case ICONST_1: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getBoolean(1)));
            }
            case ICONST_2: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(2)));
            }
            case ICONST_3: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(3)));
            }
            case ICONST_4: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(4)));
            }
            case ICONST_5: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(5)));
            }
            case LCONST_0: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getLong(0)));
            }
            case LCONST_1: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getLong(1)));
            }
            case FCONST_0: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getFloat(0.0f)));
            }
            case DCONST_0: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getDouble(0.0)));
            }
            case FCONST_1: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getFloat(1.0f)));
            }
            case DCONST_1: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getDouble(1.0)));
            }
            case FCONST_2: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getFloat(2.0f)));
            }
            case BIPUSH: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(this.rawData[0])));
            }
            case SIPUSH: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(this.getInstrArgShort(0))));
            }
            case ASTORE: 
            case ASTORE_0: 
            case ASTORE_1: 
            case ASTORE_2: 
            case ASTORE_3: 
            case ISTORE: 
            case ISTORE_0: 
            case ISTORE_1: 
            case ISTORE_2: 
            case ISTORE_3: 
            case LSTORE: 
            case LSTORE_0: 
            case LSTORE_1: 
            case LSTORE_2: 
            case LSTORE_3: 
            case DSTORE: 
            case DSTORE_0: 
            case DSTORE_1: 
            case DSTORE_2: 
            case DSTORE_3: 
            case FSTORE: 
            case FSTORE_0: 
            case FSTORE_1: 
            case FSTORE_2: 
            case FSTORE_3: {
                return this.mkAssign(variableFactory);
            }
            case NEW: {
                return new AssignmentSimple(this.getStackLValue(0), new NewObject(this.cpEntries[0]));
            }
            case NEWARRAY: {
                return new AssignmentSimple(this.getStackLValue(0), new NewPrimitiveArray((Expression)this.getStackRValue(0), this.rawData[0]));
            }
            case ANEWARRAY: {
                List tmp = ListFactory.newList();
                tmp.add((StackValue)this.getStackRValue(0));
                ConstantPoolEntryClass clazz = (ConstantPoolEntryClass)this.cpEntries[0];
                JavaTypeInstance innerInstance = clazz.getTypeInstance();
                JavaArrayTypeInstance resultInstance = new JavaArrayTypeInstance(1, innerInstance);
                return new AssignmentSimple(this.getStackLValue(0), new NewObjectArray(tmp, resultInstance));
            }
            case MULTIANEWARRAY: {
                JavaTypeInstance innerInstance;
                byte numDims = this.rawData[2];
                ConstantPoolEntryClass clazz = (ConstantPoolEntryClass)this.cpEntries[0];
                JavaTypeInstance resultInstance = innerInstance = clazz.getTypeInstance();
                return new AssignmentSimple(this.getStackLValue(0), new NewObjectArray(this.getNStackRValuesAsExpressions(numDims), resultInstance));
            }
            case ARRAYLENGTH: {
                return new AssignmentSimple(this.getStackLValue(0), new ArrayLength(this.getStackRValue(0)));
            }
            case AALOAD: 
            case IALOAD: 
            case BALOAD: 
            case CALOAD: 
            case FALOAD: 
            case LALOAD: 
            case DALOAD: 
            case SALOAD: {
                return new AssignmentSimple(this.getStackLValue(0), new ArrayIndex(this.getStackRValue(1), this.getStackRValue(0)));
            }
            case AASTORE: 
            case IASTORE: 
            case BASTORE: 
            case CASTORE: 
            case FASTORE: 
            case LASTORE: 
            case DASTORE: 
            case SASTORE: {
                return new AssignmentSimple(new ArrayVariable(new ArrayIndex(this.getStackRValue(2), this.getStackRValue(1))), this.getStackRValue(0));
            }
            case LCMP: 
            case DCMPG: 
            case DCMPL: 
            case FCMPG: 
            case FCMPL: 
            case LSUB: 
            case LADD: 
            case IADD: 
            case FADD: 
            case DADD: 
            case ISUB: 
            case DSUB: 
            case FSUB: 
            case IREM: 
            case FREM: 
            case LREM: 
            case DREM: 
            case IDIV: 
            case FDIV: 
            case DDIV: 
            case IMUL: 
            case DMUL: 
            case FMUL: 
            case LMUL: 
            case LAND: 
            case LDIV: 
            case LOR: 
            case LXOR: 
            case ISHR: 
            case ISHL: 
            case LSHL: 
            case LSHR: 
            case IUSHR: 
            case LUSHR: {
                ArithmeticOperation op = new ArithmeticOperation(this.getStackRValue(1), this.getStackRValue(0), ArithOp.getOpFor(this.instr));
                return new AssignmentSimple(this.getStackLValue(0), op);
            }
            case IOR: 
            case IAND: 
            case IXOR: {
                StackValue lhs = this.getStackRValue(1);
                StackValue rhs = this.getStackRValue(0);
                if (lhs.getInferredJavaType().getJavaTypeInstance() == RawJavaType.BOOLEAN && rhs.getInferredJavaType().getJavaTypeInstance() == RawJavaType.BOOLEAN) {
                    ArithmeticOperation op = new ArithmeticOperation(lhs, rhs, ArithOp.getOpFor(this.instr));
                    return new AssignmentSimple(this.getStackLValue(0), op);
                }
                ArithOp arithop = ArithOp.getOpFor(this.instr);
                InferredJavaType.useInArithOp(lhs.getInferredJavaType(), rhs.getInferredJavaType(), arithop);
                ArithmeticOperation op = new ArithmeticOperation(new InferredJavaType(RawJavaType.INT, InferredJavaType.Source.EXPRESSION, true), lhs, rhs, arithop);
                return new AssignmentSimple(this.getStackLValue(0), op);
            }
            case I2B: 
            case I2C: 
            case I2D: 
            case I2F: 
            case I2L: 
            case I2S: 
            case L2D: 
            case L2F: 
            case L2I: 
            case F2D: 
            case F2I: 
            case F2L: 
            case D2F: 
            case D2I: 
            case D2L: {
                LValue lValue = this.getStackLValue(0);
                lValue.getInferredJavaType().useAsWithCast(this.instr.getRawJavaType());
                return new AssignmentSimple(lValue, this.getStackRValue(0));
            }
            case INSTANCEOF: {
                return new AssignmentSimple(this.getStackLValue(0), new InstanceOfExpression(this.getStackRValue(0), this.cpEntries[0]));
            }
            case CHECKCAST: {
                ConstantPoolEntryClass castTarget = (ConstantPoolEntryClass)this.cpEntries[0];
                JavaTypeInstance tgtJavaType = castTarget.getTypeInstance();
                JavaTypeInstance alreadyJavaType = this.getStackRValue(0).getInferredJavaType().getJavaTypeInstance();
                if (tgtJavaType.equals(alreadyJavaType.getDeGenerifiedType())) {
                    return new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                }
                InferredJavaType castType = new InferredJavaType(tgtJavaType, InferredJavaType.Source.EXPRESSION, true);
                return new AssignmentSimple(this.getStackLValue(0), new CastExpression(castType, this.getStackRValue(0)));
            }
            case INVOKESTATIC: {
                ConstantPoolEntryMethodRef function = (ConstantPoolEntryMethodRef)this.cpEntries[0];
                MethodPrototype methodPrototype = function.getMethodPrototype();
                List<Expression> args = this.getNStackRValuesAsExpressions(this.stackConsumed.size());
                methodPrototype.tightenArgs(null, args);
                methodPrototype.addExplicitCasts(null, args);
                StaticFunctionInvokation funcCall = new StaticFunctionInvokation(function, args);
                if (this.stackProduced.size() != 0) return new AssignmentSimple(this.getStackLValue(0), funcCall);
                return new ExpressionStatement(funcCall);
            }
            case INVOKEDYNAMIC: {
                return this.buildInvokeDynamic(method, dcCommonState);
            }
            case INVOKESPECIAL: 
            case INVOKEVIRTUAL: 
            case INVOKEINTERFACE: {
                return this.buildInvoke(method);
            }
            case RETURN: {
                return new ReturnNothingStatement();
            }
            case IF_ACMPEQ: 
            case IF_ACMPNE: 
            case IF_ICMPLT: 
            case IF_ICMPGE: 
            case IF_ICMPGT: 
            case IF_ICMPNE: 
            case IF_ICMPEQ: 
            case IF_ICMPLE: {
                ComparisonOperation conditionalExpression = new ComparisonOperation(this.getStackRValue(1), this.getStackRValue(0), CompOp.getOpFor(this.instr));
                return new IfStatement(conditionalExpression);
            }
            case IFNONNULL: {
                ComparisonOperation conditionalExpression = new ComparisonOperation(this.getStackRValue(0), new Literal(TypedLiteral.getNull()), CompOp.NE);
                return new IfStatement(conditionalExpression);
            }
            case IFNULL: {
                ComparisonOperation conditionalExpression = new ComparisonOperation(this.getStackRValue(0), new Literal(TypedLiteral.getNull()), CompOp.EQ);
                return new IfStatement(conditionalExpression);
            }
            case IFEQ: 
            case IFNE: {
                ComparisonOperation conditionalExpression = new ComparisonOperation(this.getStackRValue(0), new Literal(TypedLiteral.getBoolean(0)), CompOp.getOpFor(this.instr));
                return new IfStatement(conditionalExpression);
            }
            case IFLE: 
            case IFLT: 
            case IFGT: 
            case IFGE: {
                ComparisonOperation conditionalExpression = new ComparisonOperation(this.getStackRValue(0), new Literal(TypedLiteral.getInt(0)), CompOp.getOpFor(this.instr));
                return new IfStatement(conditionalExpression);
            }
            case JSR_W: 
            case JSR: {
                return new CompoundStatement(new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getInt(this.originalRawOffset))), new JSRCallStatement());
            }
            case RET: {
                int slot = this.getInstrArgByte(0);
                LValueExpression retVal = new LValueExpression(variableFactory.localVariable(slot, this.localVariablesBySlot.get(slot), this.originalRawOffset, false));
                return new JSRRetStatement(retVal);
            }
            case GOTO: 
            case GOTO_W: {
                return new GotoStatement();
            }
            case ATHROW: {
                return new ThrowStatement(this.getStackRValue(0));
            }
            case IRETURN: 
            case ARETURN: 
            case LRETURN: 
            case DRETURN: 
            case FRETURN: {
                StackValue retVal = this.getStackRValue(0);
                JavaTypeInstance tgtType = variableFactory.getReturn();
                if (!(tgtType instanceof RawJavaType)) return new ReturnValueStatement(retVal, tgtType);
                retVal.getInferredJavaType().useAsWithoutCasting((RawJavaType)tgtType);
                return new ReturnValueStatement(retVal, tgtType);
            }
            case GETFIELD: {
                LValueExpression fieldExpression = new LValueExpression(new FieldVariable(this.getStackRValue(0), method.getClassFile(), this.cpEntries[0]));
                return new AssignmentSimple(this.getStackLValue(0), fieldExpression);
            }
            case GETSTATIC: {
                return new AssignmentSimple(this.getStackLValue(0), new LValueExpression(new StaticVariable(method.getClassFile(), this.cp, this.cpEntries[0])));
            }
            case PUTSTATIC: {
                return new AssignmentSimple(new StaticVariable(method.getClassFile(), this.cp, this.cpEntries[0]), this.getStackRValue(0));
            }
            case PUTFIELD: {
                return new AssignmentSimple(new FieldVariable(this.getStackRValue(1), method.getClassFile(), this.cpEntries[0]), this.getStackRValue(0));
            }
            case SWAP: {
                AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                return new CompoundStatement(s1, s2);
            }
            case DUP: {
                AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(0));
                return new CompoundStatement(s1, s2);
            }
            case DUP_X1: {
                AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(0));
                return new CompoundStatement(s1, s2, s3);
            }
            case DUP_X2: {
                if (this.stackConsumed.get(1).getStackEntry().getType().getComputationCategory() == 2) {
                    AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                    AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                    AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(0));
                    return new CompoundStatement(s1, s2, s3);
                }
                AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(2));
                AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(1));
                AssignmentSimple s4 = new AssignmentSimple(this.getStackLValue(3), this.getStackRValue(0));
                return new CompoundStatement(s1, s2, s3, s4);
            }
            case DUP2: {
                if (this.stackConsumed.get(0).getStackEntry().getType().getComputationCategory() == 2) {
                    AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                    AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(0));
                    return new CompoundStatement(s1, s2);
                }
                AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(0));
                AssignmentSimple s4 = new AssignmentSimple(this.getStackLValue(3), this.getStackRValue(1));
                return new CompoundStatement(s1, s2, s3, s4);
            }
            case DUP2_X1: {
                if (this.stackConsumed.get(0).getStackEntry().getType().getComputationCategory() == 2) {
                    AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                    AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                    AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(0));
                    return new CompoundStatement(s1, s2, s3);
                }
                AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(1));
                AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(0));
                AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(2));
                AssignmentSimple s4 = new AssignmentSimple(this.getStackLValue(3), this.getStackRValue(1));
                AssignmentSimple s5 = new AssignmentSimple(this.getStackLValue(4), this.getStackRValue(0));
                return new CompoundStatement(s1, s2, s3, s4, s5);
            }
            case DUP2_X2: {
                if (this.stackConsumed.get(0).getStackEntry().getType().getComputationCategory() == 2) {
                    if (this.stackConsumed.get(1).getStackEntry().getType().getComputationCategory() == 2) {
                        AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                        AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                        AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(0));
                        return new CompoundStatement(s1, s2, s3);
                    }
                    AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                    AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                    AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(2));
                    AssignmentSimple s4 = new AssignmentSimple(this.getStackLValue(3), this.getStackRValue(0));
                    return new CompoundStatement(s1, s2, s3, s4);
                }
                if (this.stackConsumed.get(2).getStackEntry().getType().getComputationCategory() == 2) {
                    AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                    AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                    AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(2));
                    AssignmentSimple s4 = new AssignmentSimple(this.getStackLValue(3), this.getStackRValue(0));
                    AssignmentSimple s5 = new AssignmentSimple(this.getStackLValue(4), this.getStackRValue(1));
                    return new CompoundStatement(s1, s2, s3, s4, s5);
                }
                AssignmentSimple s1 = new AssignmentSimple(this.getStackLValue(0), this.getStackRValue(0));
                AssignmentSimple s2 = new AssignmentSimple(this.getStackLValue(1), this.getStackRValue(1));
                AssignmentSimple s3 = new AssignmentSimple(this.getStackLValue(2), this.getStackRValue(2));
                AssignmentSimple s4 = new AssignmentSimple(this.getStackLValue(3), this.getStackRValue(3));
                AssignmentSimple s5 = new AssignmentSimple(this.getStackLValue(4), this.getStackRValue(0));
                AssignmentSimple s6 = new AssignmentSimple(this.getStackLValue(5), this.getStackRValue(1));
                return new CompoundStatement(s1, s2, s3, s4, s5, s6);
            }
            case LDC: 
            case LDC_W: 
            case LDC2_W: {
                return new AssignmentSimple(this.getStackLValue(0), new Literal(TypedLiteral.getConstantPoolEntry(this.cp, this.cpEntries[0])));
            }
            case MONITORENTER: {
                return new MonitorEnterStatement(this.getStackRValue(0), blockIdentifierFactory.getNextBlockIdentifier(BlockType.MONITOR));
            }
            case MONITOREXIT: {
                return new MonitorExitStatement(this.getStackRValue(0));
            }
            case FAKE_TRY: {
                return new TryStatement(this.getSingleExceptionGroup());
            }
            case FAKE_CATCH: {
                return new CatchStatement(this.catchExceptionGroups, this.getStackLValue(0));
            }
            case NOP: {
                return new Nop();
            }
            case POP: {
                return new ExpressionStatement(this.getStackRValue(0));
            }
            case POP2: {
                if (this.stackConsumed.get(0).getStackEntry().getType().getComputationCategory() == 2) {
                    return new ExpressionStatement(this.getStackRValue(0));
                }
                ExpressionStatement s1 = new ExpressionStatement(this.getStackRValue(0));
                ExpressionStatement s2 = new ExpressionStatement(this.getStackRValue(1));
                return new CompoundStatement(s1, s2);
            }
            case TABLESWITCH: {
                return new RawSwitchStatement(this.getStackRValue(0), new DecodedTableSwitch(this.rawData, this.originalRawOffset));
            }
            case LOOKUPSWITCH: {
                return new RawSwitchStatement(this.getStackRValue(0), new DecodedLookupSwitch(this.rawData, this.originalRawOffset));
            }
            case IINC: {
                int variableIndex = this.getInstrArgByte(0);
                int incrAmount = this.getInstrArgByte(1);
                ArithOp op = ArithOp.PLUS;
                if (incrAmount < 0) {
                    incrAmount = - incrAmount;
                    op = ArithOp.MINUS;
                }
                LValue lvalue = variableFactory.localVariable(variableIndex, this.localVariablesBySlot.get(variableIndex), this.originalRawOffset, false);
                return new AssignmentSimple(lvalue, new ArithmeticOperation(new LValueExpression(lvalue), new Literal(TypedLiteral.getInt(incrAmount)), op));
            }
            case IINC_WIDE: {
                int variableIndex = this.getInstrArgShort(1);
                int incrAmount = this.getInstrArgShort(3);
                ArithOp op = ArithOp.PLUS;
                if (incrAmount < 0) {
                    incrAmount = - incrAmount;
                    op = ArithOp.MINUS;
                }
                LValue lvalue = variableFactory.localVariable(variableIndex, this.localVariablesBySlot.get(variableIndex), this.originalRawOffset, false);
                return new AssignmentSimple(lvalue, new ArithmeticOperation(new LValueExpression(lvalue), new Literal(TypedLiteral.getInt(incrAmount)), op));
            }
            case DNEG: 
            case FNEG: 
            case LNEG: 
            case INEG: {
                return new AssignmentSimple(this.getStackLValue(0), new ArithmeticMonOperation(this.getStackRValue(0), ArithOp.MINUS));
            }
        }
        throw new ConfusedCFRException("Not implemented - conversion to statement from " + (Object)this.instr);
    }

    private StackValue getStackRValue(int idx) {
        StackEntryHolder stackEntryHolder = this.stackConsumed.get(idx);
        StackEntry stackEntry = stackEntryHolder.getStackEntry();
        stackEntry.incrementUsage();
        return new StackValue(stackEntry.getLValue());
    }

    private LValue getStackLValue(int idx) {
        StackEntryHolder stackEntryHolder = this.stackProduced.get(idx);
        StackEntry stackEntry = stackEntryHolder.getStackEntry();
        return stackEntry.getLValue();
    }

    private List<Expression> getNStackRValuesAsExpressions(int count) {
        List res = ListFactory.newList();
        for (int i = count - 1; i >= 0; --i) {
            res.add((StackValue)this.getStackRValue(i));
        }
        return res;
    }

    public String toString() {
        return "" + this.index + " : " + (Object)this.instr;
    }

    public static void populateStackInfo(List<Op02WithProcessedDataAndRefs> op2list, Method method) {
        for (Op02WithProcessedDataAndRefs op : op2list) {
            op.resetStackInfo();
        }
        LinkedList toProcess = ListFactory.newLinkedList();
        toProcess.add(Pair.make(new StackSim(), op2list.get(0)));
        try {
            while (!toProcess.isEmpty()) {
                Pair next = (Pair)toProcess.removeFirst();
                Op02WithProcessedDataAndRefs o2 = (Op02WithProcessedDataAndRefs)next.getSecond();
                StackSim stackSim = (StackSim)next.getFirst();
                o2.populateStackInfo(stackSim, method, toProcess);
            }
        }
        catch (ConfusedCFRException e) {
            ToStringDumper dmp = new ToStringDumper();
            dmp.print("----[known stack info]------------\n\n");
            for (Op02WithProcessedDataAndRefs op2 : op2list) {
                op2.dump(dmp);
            }
            System.err.print(dmp.toString());
            throw e;
        }
    }

    public static void unlinkUnreachable(List<Op02WithProcessedDataAndRefs> op2list) {
        Set reached = SetFactory.newSet();
        GraphVisitorDFS<Op02WithProcessedDataAndRefs> reachableVisitor = new GraphVisitorDFS<Op02WithProcessedDataAndRefs>(op2list.get(0), (BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>)new BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>(reached){
            final /* synthetic */ Set val$reached;

            @Override
            public void call(Op02WithProcessedDataAndRefs arg1, GraphVisitor<Op02WithProcessedDataAndRefs> arg2) {
                this.val$reached.add(arg1);
                for (Op02WithProcessedDataAndRefs target : arg1.getTargets()) {
                    arg2.enqueue(target);
                }
            }
        });
        reachableVisitor.process();
        for (Op02WithProcessedDataAndRefs op : op2list) {
            if (reached.contains(op)) continue;
            for (Op02WithProcessedDataAndRefs target : op.targets) {
                target.removeSource(op);
            }
            op.instr = JVMInstr.NOP;
            op.targets.clear();
        }
    }

    public void nop() {
        this.instr = JVMInstr.NOP;
    }

    private void collectLocallyMutatedVariables(SSAIdentifierFactory<Slot> ssaIdentifierFactory) {
        Pair<JavaTypeInstance, Integer> storage = this.getStorageType();
        if (storage != null) {
            this.ssaIdentifiers = new SSAIdentifiers<Slot>(new Slot(storage.getFirst(), storage.getSecond()), ssaIdentifierFactory);
            return;
        }
        this.ssaIdentifiers = new SSAIdentifiers<Slot>();
    }

    private static void assignSSAIdentifiers(SSAIdentifierFactory<Slot> ssaIdentifierFactory, Method method, DecompilerComments comments, List<Op02WithProcessedDataAndRefs> statements, BytecodeMeta bytecodeMeta, Options options) {
        Op02WithProcessedDataAndRefs.assignSSAIdentifiersInner(ssaIdentifierFactory, method, statements, bytecodeMeta, options);
        TreeMap missing = MapFactory.newTreeMap();
        Iterator<Op02WithProcessedDataAndRefs> i$ = statements.iterator();
        while (i$.hasNext()) {
            Pair<JavaTypeInstance, Integer> load;
            SSAIdent ident;
            Op02WithProcessedDataAndRefs op02;
            if ((load = (op02 = i$.next()).getRetrieveType()) == null) continue;
            if ((ident = op02.ssaIdentifiers.getSSAIdent(new Slot(load.getFirst(), load.getSecond()))) != null) continue;
            missing.put((Integer)load.getSecond(), (JavaTypeInstance)load.getFirst());
        }
        if (missing.isEmpty()) {
            return;
        }
        if (!method.getConstructorFlag().isConstructor()) {
            throw new IllegalStateException("Invisible function parameters on a non-constructor");
        }
        method.getMethodPrototype().setSyntheticConstructorParameters(method.getConstructorFlag(), comments, missing);
        Op02WithProcessedDataAndRefs.assignSSAIdentifiersInner(ssaIdentifierFactory, method, statements, bytecodeMeta, options);
    }

    public static void assignSSAIdentifiersInner(SSAIdentifierFactory<Slot> ssaIdentifierFactory, Method method, List<Op02WithProcessedDataAndRefs> statements, BytecodeMeta bytecodeMeta, Options options) {
        Map<Slot, SSAIdent> idents = method.getMethodPrototype().collectInitialSlotUsage(method.getConstructorFlag(), ssaIdentifierFactory);
        for (Op02WithProcessedDataAndRefs statement : statements) {
            statement.collectLocallyMutatedVariables(ssaIdentifierFactory);
        }
        statements.get((int)0).ssaIdentifiers = new SSAIdentifiers<Slot>(idents);
        Set<Integer> livenessClashes = bytecodeMeta.getLivenessClashes();
         testSlot = new BinaryPredicate<Slot, Slot>(livenessClashes){
            final /* synthetic */ Set val$livenessClashes;

            @Override
            public boolean test(Slot a, Slot b) {
                StackType t1 = a.getJavaTypeInstance().getStackType();
                StackType t2 = b.getJavaTypeInstance().getStackType();
                if (t1 != t2) return false;
                if (this.val$livenessClashes.isEmpty()) {
                    return true;
                }
                if (!this.val$livenessClashes.contains(a.getIdx())) return true;
                return false;
            }
        };
        LinkedList toProcess = ListFactory.newLinkedList();
        toProcess.addAll(statements);
        while (!toProcess.isEmpty()) {
            Op02WithProcessedDataAndRefs statement2 = (Op02WithProcessedDataAndRefs)toProcess.remove();
            SSAIdentifiers<Slot> ssaIdentifiers = statement2.ssaIdentifiers;
            boolean changed = false;
            for (Op02WithProcessedDataAndRefs source : statement2.getSources()) {
                if (!ssaIdentifiers.mergeWith(source.ssaIdentifiers, testSlot)) continue;
                changed = true;
            }
            if (!changed) continue;
            toProcess.addAll(statement2.getTargets());
        }
    }

    private static void removeUnusedSSAIdentifiers(SSAIdentifierFactory<Slot> ssaIdentifierFactory, Method method, List<Op02WithProcessedDataAndRefs> op2list) {
        List endPoints = ListFactory.newList();
        GraphVisitorDFS<Op02WithProcessedDataAndRefs> gv = new GraphVisitorDFS<Op02WithProcessedDataAndRefs>(op2list.get(0), (BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>)new BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>(endPoints){
            final /* synthetic */ List val$endPoints;

            @Override
            public void call(Op02WithProcessedDataAndRefs arg1, GraphVisitor<Op02WithProcessedDataAndRefs> arg2) {
                if (arg1.getTargets().isEmpty()) {
                    this.val$endPoints.add(arg1);
                } else {
                    arg2.enqueue(arg1.getTargets());
                }
            }
        });
        gv.process();
        Set seenOnce = SetFactory.newSet();
        Set toProcessContent = SetFactory.newSet();
        LinkedList toProcess = ListFactory.newLinkedList();
        toProcess.addAll(endPoints);
        toProcessContent.addAll(endPoints);
        List storeWithoutRead = ListFactory.newList();
        while (!toProcess.isEmpty()) {
            Op02WithProcessedDataAndRefs node = (Op02WithProcessedDataAndRefs)toProcess.removeFirst();
            toProcessContent.remove(node);
            Pair<JavaTypeInstance, Integer> retrieved = node.getRetrieveType();
            Pair<JavaTypeInstance, Integer> stored = node.getStorageType();
            SSAIdentifiers<Slot> ssaIdents = node.ssaIdentifiers;
            Map<Slot, SSAIdent> idents = ssaIdents.getKnownIdentifiers();
            Iterator<Map.Entry<Slot, SSAIdent>> iterator = idents.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Slot, SSAIdent> entry = iterator.next();
                Slot slot = entry.getKey();
                SSAIdent thisIdent = entry.getValue();
                boolean used = false;
                if (retrieved != null && retrieved.getSecond().intValue() == slot.getIdx()) {
                    used = true;
                }
                if (!used) {
                    for (Op02WithProcessedDataAndRefs target : node.targets) {
                        if (target.ssaIdentifiers.getSSAIdent(slot) == null) continue;
                        used = true;
                    }
                }
                if (!(used || stored == null)) {
                    for (Op02WithProcessedDataAndRefs source : node.sources) {
                        SSAIdent sourceIdent;
                        if ((sourceIdent = source.ssaIdentifiers.getSSAIdent(slot)) == null || !thisIdent.isSuperSet(sourceIdent)) continue;
                        used = true;
                    }
                }
                if (!used) {
                    for (Op02WithProcessedDataAndRefs source : node.sources) {
                        if (toProcessContent.contains(source)) continue;
                        toProcessContent.add((Op02WithProcessedDataAndRefs)source);
                        toProcess.add((Op02WithProcessedDataAndRefs)source);
                        seenOnce.add((Op02WithProcessedDataAndRefs)source);
                    }
                    if (stored != null && stored.getSecond().intValue() == slot.getIdx()) {
                        storeWithoutRead.add((Op02WithProcessedDataAndRefs)node);
                    }
                    iterator.remove();
                    continue;
                }
                for (Op02WithProcessedDataAndRefs source : node.sources) {
                    if (seenOnce.contains(source) || toProcessContent.contains(source)) continue;
                    toProcessContent.add((Op02WithProcessedDataAndRefs)source);
                    toProcess.add((Op02WithProcessedDataAndRefs)source);
                    seenOnce.add((Op02WithProcessedDataAndRefs)source);
                }
            }
        }
        for (Op02WithProcessedDataAndRefs store : storeWithoutRead) {
            Pair<JavaTypeInstance, Integer> storage = store.getStorageType();
            Slot slot = new Slot(storage.getFirst(), storage.getSecond());
            SSAIdent ident = ssaIdentifierFactory.getIdent(slot);
            store.ssaIdentifiers.getKnownIdentifiers().put(slot, ident);
        }
    }

    public static void discoverStorageLiveness(Method method, DecompilerComments comments, List<Op02WithProcessedDataAndRefs> op2list, BytecodeMeta bytecodeMeta, Options options) {
        SSAIdentifierFactory ssaIdentifierFactory = new SSAIdentifierFactory();
        Op02WithProcessedDataAndRefs.assignSSAIdentifiers(ssaIdentifierFactory, method, comments, op2list, bytecodeMeta, options);
        Op02WithProcessedDataAndRefs.removeUnusedSSAIdentifiers(ssaIdentifierFactory, method, op2list);
        Map identChain = MapFactory.newLinkedLazyMap(new UnaryFunction<Slot, Map<SSAIdent, Set<SSAIdent>>>(){

            @Override
            public Map<SSAIdent, Set<SSAIdent>> invoke(Slot arg) {
                return MapFactory.newLinkedLazyMap(new UnaryFunction<SSAIdent, Set<SSAIdent>>(){

                    @Override
                    public Set<SSAIdent> invoke(SSAIdent arg) {
                        return SetFactory.newOrderedSet();
                    }
                });
            }

        });
        Set<Integer> livenessClashes = bytecodeMeta.getLivenessClashes();
        for (Op02WithProcessedDataAndRefs op : op2list) {
            SSAIdentifiers<Slot> identifiers;
            Slot fixedHere;
            if ((fixedHere = (identifiers = op.ssaIdentifiers).getFixedHere()) != null) {
                SSAIdent fixedIdent;
                SSAIdent finalIdent = identifiers.getSSAIdent(fixedHere);
                if ((fixedIdent = identifiers.getValFixedHere()).isFirstIn(finalIdent)) {
                    identifiers.setInitialAssign();
                }
            }
            Map<Slot, SSAIdent> identMap = identifiers.getKnownIdentifiers();
            for (Map.Entry<Slot, SSAIdent> entry : identMap.entrySet()) {
                Slot thisSlot = entry.getKey();
                SSAIdent thisIdents = entry.getValue();
                Map map = (Map)identChain.get(thisSlot);
                Set thisNextSet = (Set)map.get(thisIdents);
                for (Op02WithProcessedDataAndRefs tgt : op.getTargets()) {
                    SSAIdent nextIdents;
                    if ((nextIdents = tgt.ssaIdentifiers.getSSAIdent(thisSlot)) == null || !nextIdents.isSuperSet(thisIdents)) continue;
                    thisNextSet.add(nextIdents);
                }
            }
        }
        Map combinedMap = MapFactory.newLinkedMap();
        IdentFactory identFactory = new IdentFactory(null);
        for (Map.Entry entry : identChain.entrySet()) {
            Slot slot = (Slot)entry.getKey();
            Map downMap = (Map)entry.getValue();
            Map<SSAIdent, Set<SSAIdent>> upMap = Op02WithProcessedDataAndRefs.createReverseMap(downMap);
            Set keys = SetFactory.newOrderedSet();
            keys.addAll(downMap.keySet());
            keys.addAll(upMap.keySet());
            Iterator i$ = keys.iterator();
            while (i$.hasNext()) {
                SSAIdent key;
                Pair<Slot, SSAIdent> slotkey;
                if (combinedMap.containsKey(slotkey = Pair.make(slot, key = (SSAIdent)i$.next()))) continue;
                Ident thisIdent = identFactory.getNextIdent(slot.getIdx());
                GraphVisitorDFS<SSAIdent> gv = new GraphVisitorDFS<SSAIdent>(key, (BinaryProcedure<SSAIdent, GraphVisitor<SSAIdent>>)new BinaryProcedure<SSAIdent, GraphVisitor<SSAIdent>>(slot, livenessClashes, slotkey, combinedMap, thisIdent, downMap, upMap){
                    final /* synthetic */ Slot val$slot;
                    final /* synthetic */ Set val$livenessClashes;
                    final /* synthetic */ Pair val$slotkey;
                    final /* synthetic */ Map val$combinedMap;
                    final /* synthetic */ Ident val$thisIdent;
                    final /* synthetic */ Map val$downMap;
                    final /* synthetic */ Map val$upMap;

                    @Override
                    public void call(SSAIdent arg1, GraphVisitor<SSAIdent> arg2) {
                        Pair<Slot, SSAIdent> innerslotkey = Pair.make(this.val$slot, arg1);
                        if (this.val$livenessClashes.contains(this.val$slot.getIdx()) && !innerslotkey.equals(this.val$slotkey)) {
                            return;
                        }
                        if (this.val$combinedMap.containsKey(innerslotkey)) {
                            return;
                        }
                        this.val$combinedMap.put(innerslotkey, this.val$thisIdent);
                        arg2.enqueue((Collection)this.val$downMap.get(arg1));
                        arg2.enqueue((Collection)this.val$upMap.get(arg1));
                    }
                });
                gv.process();
            }
        }
        for (Op02WithProcessedDataAndRefs op2 : op2list) {
            op2.mapSSASlots(combinedMap);
        }
        method.getMethodPrototype().computeParameters(method.getConstructorFlag(), op2list.get((int)0).localVariablesBySlot);
    }

    private void mapSSASlots(Map<Pair<Slot, SSAIdent>, Ident> identmap) {
        Map<Slot, SSAIdent> knownIdents = this.ssaIdentifiers.getKnownIdentifiers();
        Iterator<Map.Entry<Slot, SSAIdent>> i$ = knownIdents.entrySet().iterator();
        while (i$.hasNext()) {
            Ident ident;
            Map.Entry<Slot, SSAIdent> entry;
            if ((ident = identmap.get(Pair.make((entry = i$.next()).getKey(), (entry = i$.next()).getValue()))) == null) {
                throw new IllegalStateException("Null ident");
            }
            this.localVariablesBySlot.put(entry.getKey().getIdx(), ident);
        }
    }

    private static Map<SSAIdent, Set<SSAIdent>> createReverseMap(Map<SSAIdent, Set<SSAIdent>> downMap) {
        Map res = MapFactory.newLinkedLazyMap(new UnaryFunction<SSAIdent, Set<SSAIdent>>(){

            @Override
            public Set<SSAIdent> invoke(SSAIdent arg) {
                return SetFactory.newOrderedSet();
            }
        });
        for (Map.Entry<SSAIdent, Set<SSAIdent>> entry : downMap.entrySet()) {
            SSAIdent revValue = entry.getKey();
            Set<SSAIdent> revKeys = entry.getValue();
            for (SSAIdent revKey : revKeys) {
                ((Set)res.get(revKey)).add(revValue);
            }
        }
        return res;
    }

    public static List<Op03SimpleStatement> convertToOp03List(List<Op02WithProcessedDataAndRefs> op2list, Method method, VariableFactory variableFactory, BlockIdentifierFactory blockIdentifierFactory, DCCommonState dcCommonState) {
        List op03SimpleParseNodesTmp = ListFactory.newList();
        GraphConversionHelper conversionHelper = new GraphConversionHelper();
        GraphVisitorFIFO<Op02WithProcessedDataAndRefs> o2Converter = new GraphVisitorFIFO<Op02WithProcessedDataAndRefs>(op2list.get(0), new BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>(method, variableFactory, blockIdentifierFactory, dcCommonState, conversionHelper, op03SimpleParseNodesTmp){
            final /* synthetic */ Method val$method;
            final /* synthetic */ VariableFactory val$variableFactory;
            final /* synthetic */ BlockIdentifierFactory val$blockIdentifierFactory;
            final /* synthetic */ DCCommonState val$dcCommonState;
            final /* synthetic */ GraphConversionHelper val$conversionHelper;
            final /* synthetic */ List val$op03SimpleParseNodesTmp;

            @Override
            public void call(Op02WithProcessedDataAndRefs arg1, GraphVisitor<Op02WithProcessedDataAndRefs> arg2) {
                Op03SimpleStatement res = new Op03SimpleStatement(arg1, arg1.createStatement(this.val$method, this.val$variableFactory, this.val$blockIdentifierFactory, this.val$dcCommonState));
                this.val$conversionHelper.registerOriginalAndNew(arg1, res);
                this.val$op03SimpleParseNodesTmp.add(res);
                for (Op02WithProcessedDataAndRefs target : arg1.getTargets()) {
                    arg2.enqueue(target);
                }
            }
        });
        o2Converter.process();
        conversionHelper.patchUpRelations();
        return op03SimpleParseNodesTmp;
    }

    private static Op02WithProcessedDataAndRefs adjustOrdering(Map<InstrIndex, List<ExceptionTempStatement>> insertions, Op02WithProcessedDataAndRefs infrontOf, ExceptionGroup exceptionGroup, Op02WithProcessedDataAndRefs newNode) {
        Op02WithProcessedDataAndRefs afterThis;
        InstrIndex idxInfrontOf = infrontOf.getIndex();
        List<ExceptionTempStatement> collides = insertions.get(idxInfrontOf);
        ExceptionTempStatement exceptionTempStatement = new ExceptionTempStatement(exceptionGroup, newNode, null);
        if (collides.isEmpty()) {
            collides.add(exceptionTempStatement);
            return infrontOf;
        }
        Op02WithProcessedDataAndRefs.logger.finer("Adding " + newNode + " ident " + exceptionGroup.getTryBlockIdentifier());
        Op02WithProcessedDataAndRefs.logger.finer("Already have " + collides);
        int insertionPos = Collections.binarySearch(collides, exceptionTempStatement);
        insertionPos = insertionPos >= 0 ? ++insertionPos : - insertionPos + 1;
        if (insertionPos == 0) {
            collides.add(0, exceptionTempStatement);
            throw new ConfusedCFRException("EEk.");
        }
        Op02WithProcessedDataAndRefs.logger.finer("Insertion position = " + insertionPos);
        if (insertionPos == collides.size()) {
            collides.add(exceptionTempStatement);
            afterThis = infrontOf;
        } else {
            afterThis = collides.get(insertionPos).getOp();
            collides.add(insertionPos, exceptionTempStatement);
        }
        for (ExceptionTempStatement ets : collides) {
            ets.getOp().setIndex(infrontOf.getIndex().justBefore());
        }
        return afterThis;
    }

    private static void tidyMultipleInsertionIdentifiers(Collection<List<ExceptionTempStatement>> etsList) {
        for (List<ExceptionTempStatement> ets : etsList) {
            if (ets.size() <= 1) continue;
            for (int idx = 0; idx < ets.size(); ++idx) {
                ExceptionTempStatement et;
                if (!(et = ets.get(idx)).isTry()) continue;
                BlockIdentifier tryGroup = et.triggeringGroup.getTryBlockIdentifier();
                Op02WithProcessedDataAndRefs.logger.finer("Removing try group identifier " + tryGroup + " idx " + idx);
                for (int idx2 = 0; idx2 < idx; ++idx2) {
                    Op02WithProcessedDataAndRefs.logger.finest("" + ets.get(idx2).getOp());
                    Op02WithProcessedDataAndRefs.logger.finest("" + ets.get((int)idx2).getOp().containedInTheseBlocks + " -->");
                    ets.get((int)idx2).getOp().containedInTheseBlocks.remove(tryGroup);
                    Op02WithProcessedDataAndRefs.logger.finest("" + ets.get((int)idx2).getOp().containedInTheseBlocks);
                }
            }
        }
    }

    private static int getLastIndex(Map<Integer, Integer> lutByOffset, int op2count, long codeLength, int offset) {
        Integer iinclusiveLastIndex = lutByOffset.get(offset);
        if (iinclusiveLastIndex != null) return iinclusiveLastIndex;
        if ((long)offset != codeLength) {
            throw new ConfusedCFRException("Last index of " + offset + " is not a valid entry into the code block");
        }
        iinclusiveLastIndex = op2count - 1;
        return iinclusiveLastIndex;
    }

    private static boolean nextTarget(Op02WithProcessedDataAndRefs op, int idx, List<Op02WithProcessedDataAndRefs> op2list) {
        if (op.getTargets().size() != 1) {
            return false;
        }
        Op02WithProcessedDataAndRefs target = op.getTargets().get(0);
        if (idx + 1 >= op2list.size()) {
            return false;
        }
        if (target == op2list.get(idx + 1)) return true;
        return false;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     */
    public static List<Op02WithProcessedDataAndRefs> insertExceptionBlocks(List<Op02WithProcessedDataAndRefs> op2list, ExceptionAggregator exceptions, Map<Integer, Integer> lutByOffset, ConstantPool cp, long codeLength, DCCommonState dcCommonState, Options options) {
        originalInstrCount = op2list.size();
        if (exceptions.getExceptionsGroups().isEmpty()) {
            return op2list;
        }
        insertions = MapFactory.newLazyMap(new UnaryFunction<InstrIndex, List<ExceptionTempStatement>>(){

            @Override
            public List<ExceptionTempStatement> invoke(InstrIndex ignore) {
                return ListFactory.newList();
            }
        });
        i$ = exceptions.getExceptionsGroups().iterator();
        block3 : do {
            if (!i$.hasNext()) {
                i$ = exceptions.getExceptionsGroups().iterator();
                break;
            }
            exceptionGroup = i$.next();
            tryBlockIdentifier = exceptionGroup.getTryBlockIdentifier();
            originalIndex = lutByOffset.get(Integer.valueOf(exceptionGroup.getBytecodeIndexFrom()));
            exclusiveLastIndex = Op02WithProcessedDataAndRefs.getLastIndex(lutByOffset, originalInstrCount, codeLength, exceptionGroup.getByteCodeIndexTo());
            x = originalIndex;
            do {
                if (x >= exclusiveLastIndex) continue block3;
                op2list.get((int)x).containedInTheseBlocks.add(tryBlockIdentifier);
                ++x;
            } while (true);
            break;
        } while (true);
        block5 : do {
            if (!i$.hasNext()) break;
            exceptionGroup = i$.next();
            rawes = exceptionGroup.getEntries();
            originalIndex = lutByOffset.get(Integer.valueOf(exceptionGroup.getBytecodeIndexFrom()));
            startInstruction = op2list.get(originalIndex);
            inclusiveLastIndex = Op02WithProcessedDataAndRefs.getLastIndex(lutByOffset, originalInstrCount, codeLength, exceptionGroup.getByteCodeIndexTo());
            lastTryInstruction = op2list.get(inclusiveLastIndex);
            handlerTargets = ListFactory.newList();
            i$ = rawes.iterator();
            while (i$.hasNext()) {
                if (!((handlerIndex = lutByOffset.get(Integer.valueOf(handler = (exceptionEntry = i$.next()).getBytecodeIndexHandler())).intValue()) > originalIndex || ((Boolean)options.getOption(OptionsImpl.LENIENT)).booleanValue())) {
                    throw new ConfusedCFRException("Back jump on a try block " + exceptionEntry);
                }
                handerTarget = op2list.get(handlerIndex);
                handlerTargets.add(Pair.make(handerTarget, exceptionEntry));
            }
            tryOp = new Op02WithProcessedDataAndRefs(JVMInstr.FAKE_TRY, (byte[])null, startInstruction.getIndex().justBefore(), cp, (ConstantPoolEntry[])null, -1);
            startInstruction = Op02WithProcessedDataAndRefs.adjustOrdering(insertions, startInstruction, exceptionGroup, tryOp);
            tryOp.containedInTheseBlocks.addAll(startInstruction.containedInTheseBlocks);
            tryOp.containedInTheseBlocks.remove(exceptionGroup.getTryBlockIdentifier());
            tryOp.exceptionGroups.add(exceptionGroup);
            removeThese = ListFactory.newList();
            for (Op02WithProcessedDataAndRefs source : startInstruction.getSources()) {
                if (startInstruction.getIndex().isBackJumpFrom(source.getIndex()) && !lastTryInstruction.getIndex().isBackJumpFrom(source.getIndex())) continue;
                source.replaceTarget(startInstruction, tryOp);
                removeThese.add((Op02WithProcessedDataAndRefs)source);
                tryOp.addSource(source);
            }
            for (Op02WithProcessedDataAndRefs remove : removeThese) {
                startInstruction.removeSource(remove);
            }
            i$ = handlerTargets.iterator();
            do {
                ** if (!i$.hasNext()) goto lbl62
lbl54: // 1 sources:
                catchTargets = (Pair)i$.next();
                tryTarget = (Op02WithProcessedDataAndRefs)catchTargets.getFirst();
                tryTargetSources = tryTarget.getSources();
                preCatchOp = null;
                addFakeCatch = false;
                ** if (!tryTargetSources.isEmpty()) goto lbl68
lbl60: // 1 sources:
                addFakeCatch = true;
                ** GOTO lbl76
lbl62: // 1 sources:
                tryOp.targets.add(0, startInstruction);
                startInstruction.addSource(tryOp);
                op2list.add(tryOp);
                if (!tryOp.sources.isEmpty()) continue block5;
                x = true;
                continue block5;
lbl68: // 3 sources:
                for (Op02WithProcessedDataAndRefs source : tryTargetSources) {
                    if (source.getInstr() == JVMInstr.FAKE_CATCH) {
                        preCatchOp = source;
                        continue;
                    }
                    if (((Boolean)options.getOption(OptionsImpl.LENIENT)).booleanValue()) continue;
                    throw new ConfusedCFRException("non catch before exception catch block");
                }
                if (preCatchOp == null) {
                    addFakeCatch = true;
                }
lbl76: // 4 sources:
                if (addFakeCatch) {
                    entry = (ExceptionGroup.Entry)catchTargets.getSecond();
                    data = null;
                    if (entry.isJustThrowable()) {
                        data = new byte[0];
                    }
                    preCatchOp = new Op02WithProcessedDataAndRefs(JVMInstr.FAKE_CATCH, (byte[])data, tryTarget.getIndex().justBefore(), cp, (ConstantPoolEntry[])null, -1);
                    tryTarget = Op02WithProcessedDataAndRefs.adjustOrdering(insertions, tryTarget, exceptionGroup, preCatchOp);
                    preCatchOp.containedInTheseBlocks.addAll(tryTarget.getContainedInTheseBlocks());
                    preCatchOp.addTarget(tryTarget);
                    tryTarget.addSource(preCatchOp);
                    op2list.add(preCatchOp);
                }
                if (preCatchOp == null) {
                    throw new IllegalStateException("Bad precatch op state.");
                }
                preCatchOp.addSource(tryOp);
                tryOp.addTarget(preCatchOp);
                preCatchOp.catchExceptionGroups.add((ExceptionGroup.Entry)catchTargets.getSecond());
            } while (true);
            break;
        } while (true);
        for (ExceptionGroup exceptionGroup : exceptions.getExceptionsGroups()) {
            tryBlockIdentifier = exceptionGroup.getTryBlockIdentifier();
            beforeLastIndex = Op02WithProcessedDataAndRefs.getLastIndex(lutByOffset, originalInstrCount, codeLength, exceptionGroup.getByteCodeIndexTo()) - 1;
            lastStatement = op2list.get(beforeLastIndex);
            blocks = SetFactory.newSet(lastStatement.containedInTheseBlocks);
            x = beforeLastIndex + 1;
            if (lastStatement.targets.size() != 1 || op2list.get(x) != lastStatement.targets.get(0)) continue;
            next = op2list.get(x);
            bOk = true;
            if (next.sources.size() > 1) {
                for (Op02WithProcessedDataAndRefs source : next.sources) {
                    if (blocks.equals(blocks2 = SetFactory.newSet(source.containedInTheseBlocks))) continue;
                    bOk = false;
                }
            }
            blocksWithoutTry = SetFactory.newSet(blocks);
            blocksWithoutTry.remove(tryBlockIdentifier);
            if (!bOk) continue;
            switch (15.$SwitchMap$org$benf$cfr$reader$bytecode$opcode$JVMInstr[next.instr.ordinal()]) {
                case 160: 
                case 180: 
                case 181: 
                case 183: 
                case 184: 
                case 185: 
                case 186: 
                case 187: {
                    if (!blocksWithoutTry.equals(blocks2 = SetFactory.newSet(next.containedInTheseBlocks))) ** break;
                    next.containedInTheseBlocks.add(tryBlockIdentifier);
                }
            }
        }
        Op02WithProcessedDataAndRefs.tidyMultipleInsertionIdentifiers(insertions.values());
        return op2list;
    }

    public List<BlockIdentifier> getContainedInTheseBlocks() {
        return this.containedInTheseBlocks;
    }

    private static boolean isJSR(Op02WithProcessedDataAndRefs op) {
        JVMInstr instr = op.instr;
        return instr == JVMInstr.JSR || instr == JVMInstr.JSR_W;
    }

    private static boolean isRET(Op02WithProcessedDataAndRefs op) {
        JVMInstr instr = op.instr;
        return instr == JVMInstr.RET || instr == JVMInstr.RET_WIDE;
    }

    public static boolean processJSR(List<Op02WithProcessedDataAndRefs> ops) {
        List<Op02WithProcessedDataAndRefs> jsrInstrs = Functional.filter(ops, new Predicate<Op02WithProcessedDataAndRefs>(){

            @Override
            public boolean test(Op02WithProcessedDataAndRefs in) {
                return Op02WithProcessedDataAndRefs.isJSR(in);
            }
        });
        if (!jsrInstrs.isEmpty()) return Op02WithProcessedDataAndRefs.processJSRs(jsrInstrs, ops);
        return false;
    }

    private static boolean processJSRs(List<Op02WithProcessedDataAndRefs> jsrs, List<Op02WithProcessedDataAndRefs> ops) {
        Op02WithProcessedDataAndRefs jsr;
        Map targets = Functional.groupToMapBy(jsrs, new UnaryFunction<Op02WithProcessedDataAndRefs, Op02WithProcessedDataAndRefs>(){

            @Override
            public Op02WithProcessedDataAndRefs invoke(Op02WithProcessedDataAndRefs arg) {
                return arg.getTargets().get(0);
            }
        });
        boolean result = false;
        Set inlineCandidates = SetFactory.newSet();
        for (Op02WithProcessedDataAndRefs target : targets.keySet()) {
            GraphVisitorDFS<Op02WithProcessedDataAndRefs> gv = new GraphVisitorDFS<Op02WithProcessedDataAndRefs>(target.getTargets(), (BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>)new BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>(){

                @Override
                public void call(Op02WithProcessedDataAndRefs arg1, GraphVisitor<Op02WithProcessedDataAndRefs> arg2) {
                    if (Op02WithProcessedDataAndRefs.isRET(arg1)) {
                        return;
                    }
                    if (arg1 == Op02WithProcessedDataAndRefs.this) {
                        arg2.abort();
                        return;
                    }
                    arg2.enqueue(arg1.getTargets());
                }
            });
            gv.process();
            if (gv.wasAborted()) continue;
            Set<Op02WithProcessedDataAndRefs> nodes = SetFactory.newSet(gv.getVisitedNodes());
            for (Op02WithProcessedDataAndRefs node : nodes) {
                if (nodes.containsAll(node.getSources())) continue;
            }
            nodes.add(target);
            if (SetUtil.hasIntersection(inlineCandidates, nodes)) continue;
            inlineCandidates.addAll(nodes);
            Op02WithProcessedDataAndRefs.inlineJSR(target, nodes, ops);
            result = true;
        }
        Iterator i$ = jsrs.iterator();
        while (i$.hasNext()) {
            Op02WithProcessedDataAndRefs target2;
            List<Op02WithProcessedDataAndRefs> sources;
            if (!Op02WithProcessedDataAndRefs.isJSR(jsr = (Op02WithProcessedDataAndRefs)i$.next())) continue;
            if ((sources = targets.get(target2 = jsr.targets.get(0))) == null) continue;
            if (sources.size() > 1) continue;
            List rets = ListFactory.newList();
            GraphVisitorDFS<Op02WithProcessedDataAndRefs> gv = new GraphVisitorDFS<Op02WithProcessedDataAndRefs>(target2.getTargets(), (BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>)new BinaryProcedure<Op02WithProcessedDataAndRefs, GraphVisitor<Op02WithProcessedDataAndRefs>>(rets, target2){
                final /* synthetic */ List val$rets;
                final /* synthetic */ Op02WithProcessedDataAndRefs val$target;

                @Override
                public void call(Op02WithProcessedDataAndRefs arg1, GraphVisitor<Op02WithProcessedDataAndRefs> arg2) {
                    if (Op02WithProcessedDataAndRefs.isRET(arg1)) {
                        this.val$rets.add(arg1);
                        return;
                    }
                    if (arg1 == this.val$target) {
                        return;
                    }
                    arg2.enqueue(arg1.getTargets());
                }
            });
            gv.process();
            int idx = ops.indexOf(jsr) + 1;
            if (idx >= ops.size()) continue;
            Op02WithProcessedDataAndRefs afterJsr = ops.get(idx);
            for (Op02WithProcessedDataAndRefs ret : rets) {
                ret.instr = JVMInstr.GOTO;
                ret.targets.clear();
                ret.addTarget(afterJsr);
                afterJsr.addSource(ret);
            }
            Op02WithProcessedDataAndRefs.inlineReplaceJSR(jsr, ops);
        }
        i$ = jsrs.iterator();
        while (i$.hasNext()) {
            if (!Op02WithProcessedDataAndRefs.isJSR(jsr = (Op02WithProcessedDataAndRefs)i$.next())) continue;
            Op02WithProcessedDataAndRefs.inlineReplaceJSR(jsr, ops);
        }
        return result;
    }

    private static void inlineReplaceJSR(Op02WithProcessedDataAndRefs jsrCall, List<Op02WithProcessedDataAndRefs> ops) {
        Op02WithProcessedDataAndRefs jsrTarget = jsrCall.getTargets().get(0);
        Op02WithProcessedDataAndRefs newGoto = new Op02WithProcessedDataAndRefs(JVMInstr.GOTO, (byte[])null, jsrCall.getIndex().justAfter(), jsrCall.cp, (ConstantPoolEntry[])null, -1);
        jsrTarget.removeSource(jsrCall);
        jsrCall.removeTarget(jsrTarget);
        newGoto.addTarget(jsrTarget);
        newGoto.addSource(jsrCall);
        jsrCall.addTarget(newGoto);
        jsrTarget.addSource(newGoto);
        jsrCall.instr = JVMInstr.ACONST_NULL;
        int jsrIdx = ops.indexOf(jsrCall);
        ops.add(jsrIdx + 1, newGoto);
    }

    private static void inlineJSR(Op02WithProcessedDataAndRefs start, Set<Op02WithProcessedDataAndRefs> nodes, List<Op02WithProcessedDataAndRefs> ops) {
        List<Op02WithProcessedDataAndRefs> instrs = ListFactory.newList(nodes);
        Collections.sort(instrs, new Comparator<Op02WithProcessedDataAndRefs>(){

            @Override
            public int compare(Op02WithProcessedDataAndRefs o1, Op02WithProcessedDataAndRefs o2) {
                return o1.getIndex().compareTo(o2.getIndex());
            }
        });
        ops.removeAll(instrs);
        List<Op02WithProcessedDataAndRefs> sources = ListFactory.newList(start.getSources());
        Op02WithProcessedDataAndRefs newStart = new Op02WithProcessedDataAndRefs(JVMInstr.ACONST_NULL, (byte[])null, start.getIndex().justBefore(), start.cp, (ConstantPoolEntry[])null, -1);
        instrs.add(0, newStart);
        start.getSources().clear();
        start.addSource(newStart);
        newStart.addTarget(start);
        for (Op02WithProcessedDataAndRefs source : sources) {
            source.removeTarget(start);
            List<Op02WithProcessedDataAndRefs> instrCopy = Op02WithProcessedDataAndRefs.copyBlock(instrs, source.getIndex());
            int idx = ops.indexOf(source) + 1;
            if (idx < ops.size()) {
                Op02WithProcessedDataAndRefs retTgt = ops.get(idx);
                for (Op02WithProcessedDataAndRefs op : instrCopy) {
                    if (!Op02WithProcessedDataAndRefs.isRET(op)) continue;
                    op.instr = JVMInstr.GOTO;
                    op.addTarget(retTgt);
                    retTgt.addSource(op);
                }
            }
            source.instr = JVMInstr.NOP;
            int sourceIdx = ops.indexOf(source);
            ops.addAll(sourceIdx + 1, instrCopy);
            Op02WithProcessedDataAndRefs blockStart = instrCopy.get(0);
            blockStart.addSource(source);
            source.addTarget(blockStart);
        }
    }

    private static List<Op02WithProcessedDataAndRefs> copyBlock(List<Op02WithProcessedDataAndRefs> orig, InstrIndex afterThis) {
        List output = ListFactory.newList(orig.size());
        Map fromTo = MapFactory.newMap();
        for (Op02WithProcessedDataAndRefs in : orig) {
            Op02WithProcessedDataAndRefs copy = new Op02WithProcessedDataAndRefs(in);
            copy.index = afterThis = afterThis.justAfter();
            fromTo.put((Op02WithProcessedDataAndRefs)in, (Op02WithProcessedDataAndRefs)copy);
            output.add((Op02WithProcessedDataAndRefs)copy);
        }
        int len = orig.size();
        for (int x = 0; x < len; ++x) {
            Op02WithProcessedDataAndRefs in2 = orig.get(x);
            Op02WithProcessedDataAndRefs copy = (Op02WithProcessedDataAndRefs)output.get(x);
            copy.exceptionGroups = ListFactory.newList(in2.exceptionGroups);
            copy.containedInTheseBlocks = ListFactory.newList(in2.containedInTheseBlocks);
            copy.catchExceptionGroups = ListFactory.newList(in2.catchExceptionGroups);
            Op02WithProcessedDataAndRefs.tieUpRelations(copy.getSources(), in2.getSources(), fromTo);
            Op02WithProcessedDataAndRefs.tieUpRelations(copy.getTargets(), in2.getTargets(), fromTo);
        }
        return output;
    }

    private static void tieUpRelations(List<Op02WithProcessedDataAndRefs> out, List<Op02WithProcessedDataAndRefs> in, Map<Op02WithProcessedDataAndRefs, Op02WithProcessedDataAndRefs> map) {
        out.clear();
        Iterator<Op02WithProcessedDataAndRefs> i$ = in.iterator();
        while (i$.hasNext()) {
            Op02WithProcessedDataAndRefs mapped;
            Op02WithProcessedDataAndRefs i;
            if ((mapped = map.get(i = i$.next())) == null) {
                throw new ConfusedCFRException("Missing node tying up JSR block");
            }
            out.add(mapped);
        }
    }

    static class ExceptionTempStatement
    implements Comparable<ExceptionTempStatement> {
        private final ExceptionGroup triggeringGroup;
        private final Op02WithProcessedDataAndRefs op;
        private final boolean isTry;

        private ExceptionTempStatement(ExceptionGroup triggeringGroup, Op02WithProcessedDataAndRefs op) {
            this.triggeringGroup = triggeringGroup;
            this.op = op;
            this.isTry = op.instr == JVMInstr.FAKE_TRY;
        }

        public ExceptionGroup getTriggeringGroup() {
            return this.triggeringGroup;
        }

        public Op02WithProcessedDataAndRefs getOp() {
            return this.op;
        }

        public boolean isTry() {
            return this.isTry;
        }

        @Override
        public int compareTo(ExceptionTempStatement other) {
            int startCompare;
            if (other == this) {
                return 0;
            }
            if ((startCompare = this.triggeringGroup.getBytecodeIndexFrom() - other.triggeringGroup.getBytecodeIndexFrom()) != 0) {
                return startCompare;
            }
            int endCompare = this.triggeringGroup.getByteCodeIndexTo() - this.triggeringGroup.getByteCodeIndexTo();
            return 0 - endCompare;
        }

        public String toString() {
            return this.op.toString();
        }

        /* synthetic */ ExceptionTempStatement(ExceptionGroup x0, Op02WithProcessedDataAndRefs x1,  x2) {
            this(x0, x1);
        }
    }

    static class IdentFactory {
        int nextIdx = 0;

        private IdentFactory() {
        }

        public Ident getNextIdent(int slot) {
            return new Ident(slot, this.nextIdx++);
        }

        /* synthetic */ IdentFactory( x0) {
            this();
        }
    }

}

