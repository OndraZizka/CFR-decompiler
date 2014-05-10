/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.CodeAnalyserWholeClass;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnoynmousInner;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Triplet;
import org.benf.cfr.reader.bytecode.analysis.types.BindingSuperContainer;
import org.benf.cfr.reader.bytecode.analysis.types.BoundSuperCollector;
import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.FormalTypeParameter;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.AccessFlagMethod;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.attributes.AttributeBootstrapMethods;
import org.benf.cfr.reader.entities.attributes.AttributeInnerClasses;
import org.benf.cfr.reader.entities.attributes.AttributeSignature;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumper;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperAnnotation;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperInterface;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperNormal;
import org.benf.cfr.reader.entities.classfilehelpers.OverloadMethodSet;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.entities.innerclass.InnerClassAttributeInfo;
import org.benf.cfr.reader.entityfactories.AttributeFactory;
import org.benf.cfr.reader.entityfactories.ContiguousEntityFactory;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.InnerClassTypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.DecompilerComments;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.TypeOverridingDumper;

public class ClassFile
implements Dumpable,
TypeUsageCollectable {
    private final long OFFSET_OF_MAGIC = 0;
    private final long OFFSET_OF_MINOR = 4;
    private final long OFFSET_OF_MAJOR = 6;
    private final long OFFSET_OF_CONSTANT_POOL_COUNT = 8;
    private final long OFFSET_OF_CONSTANT_POOL = 10;
    private final short minorVer;
    private final short majorVer;
    private final ConstantPool constantPool;
    private final Set<AccessFlag> accessFlags;
    private final List<ClassFileField> fields;
    private Map<String, ClassFileField> fieldsByName;
    private final List<Method> methods;
    private Map<String, List<Method>> methodsByName;
    private final Map<JavaTypeInstance, Pair<InnerClassAttributeInfo, ClassFile>> innerClassesByTypeInfo;
    private final Map<String, Attribute> attributes;
    private final ConstantPoolEntryClass thisClass;
    private final ConstantPoolEntryClass rawSuperClass;
    private final List<ConstantPoolEntryClass> rawInterfaces;
    private final ClassSignature classSignature;
    private final ClassFileVersion classFileVersion;
    private DecompilerComments decompilerComments;
    private boolean begunAnalysis;
    private boolean hiddenInnerClass;
    private BindingSuperContainer boundSuperClasses;
    private ClassFileDumper dumpHelper;
    private final String usePath;
    private List<ConstructorInvokationAnoynmousInner> anonymousUsages;

    public ClassFile(ByteData data, String usePath, DCCommonState dcCommonState) {
        JavaRefTypeInstance typeInstance;
        this.OFFSET_OF_MAGIC = 0;
        this.OFFSET_OF_MINOR = 4;
        this.OFFSET_OF_MAJOR = 6;
        this.OFFSET_OF_CONSTANT_POOL_COUNT = 8;
        this.OFFSET_OF_CONSTANT_POOL = 10;
        this.anonymousUsages = ListFactory.newList();
        this.usePath = usePath;
        int magic = data.getS4At(0);
        if (magic != -889275714) {
            throw new ConfusedCFRException("Magic != Cafebabe");
        }
        this.minorVer = data.getS2At(4);
        this.majorVer = data.getS2At(6);
        short constantPoolCount = data.getS2At(8);
        this.constantPool = new ConstantPool(this, dcCommonState, data.getOffsetData(10), constantPoolCount);
        long OFFSET_OF_ACCESS_FLAGS = 10 + this.constantPool.getRawByteLength();
        long OFFSET_OF_THIS_CLASS = OFFSET_OF_ACCESS_FLAGS + 2;
        long OFFSET_OF_SUPER_CLASS = OFFSET_OF_THIS_CLASS + 2;
        long OFFSET_OF_INTERFACES_COUNT = OFFSET_OF_SUPER_CLASS + 2;
        long OFFSET_OF_INTERFACES = OFFSET_OF_INTERFACES_COUNT + 2;
        short numInterfaces = data.getS2At(OFFSET_OF_INTERFACES_COUNT);
        ArrayList<ConstantPoolEntryClass> tmpInterfaces = new ArrayList<ConstantPoolEntryClass>();
        long interfacesLength = ContiguousEntityFactory.buildSized(data.getOffsetData(OFFSET_OF_INTERFACES), numInterfaces, 2, tmpInterfaces, new UnaryFunction<ByteData, ConstantPoolEntryClass>(){

            @Override
            public ConstantPoolEntryClass invoke(ByteData arg) {
                return (ConstantPoolEntryClass)ClassFile.this.constantPool.getEntry(arg.getS2At(0));
            }
        });
        this.thisClass = (ConstantPoolEntryClass)this.constantPool.getEntry(data.getS2At(OFFSET_OF_THIS_CLASS));
        this.rawInterfaces = tmpInterfaces;
        this.accessFlags = AccessFlag.build(data.getS2At(OFFSET_OF_ACCESS_FLAGS));
        long OFFSET_OF_FIELDS_COUNT = OFFSET_OF_INTERFACES + (long)(2 * numInterfaces);
        long OFFSET_OF_FIELDS = OFFSET_OF_FIELDS_COUNT + 2;
        short numFields = data.getS2At(OFFSET_OF_FIELDS_COUNT);
        List tmpFields = ListFactory.newList();
        long fieldsLength = ContiguousEntityFactory.build(data.getOffsetData(OFFSET_OF_FIELDS), numFields, tmpFields, new UnaryFunction<ByteData, Field>(){

            @Override
            public Field invoke(ByteData arg) {
                return new Field(arg, ClassFile.this.constantPool);
            }
        });
        this.fields = ListFactory.newList();
        for (Field tmpField : tmpFields) {
            this.fields.add(new ClassFileField(tmpField));
        }
        long OFFSET_OF_METHODS_COUNT = OFFSET_OF_FIELDS + fieldsLength;
        long OFFSET_OF_METHODS = OFFSET_OF_METHODS_COUNT + 2;
        short numMethods = data.getS2At(OFFSET_OF_METHODS_COUNT);
        ArrayList<Method> tmpMethods = new ArrayList<Method>();
        tmpMethods.ensureCapacity(numMethods);
        long methodsLength = ContiguousEntityFactory.build(data.getOffsetData(OFFSET_OF_METHODS), numMethods, tmpMethods, new UnaryFunction<ByteData, Method>(){

            @Override
            public Method invoke(ByteData arg) {
                return new Method(arg, ClassFile.this, ClassFile.this.constantPool, dcCommonState);
            }
        });
        this.methods = tmpMethods;
        if (this.accessFlags.contains((Object)AccessFlag.ACC_STRICT)) {
            for (Method method : tmpMethods) {
                method.getAccessFlags().remove((Object)AccessFlagMethod.ACC_STRICT);
            }
        }
        long OFFSET_OF_ATTRIBUTES_COUNT = OFFSET_OF_METHODS + methodsLength;
        long OFFSET_OF_ATTRIBUTES = OFFSET_OF_ATTRIBUTES_COUNT + 2;
        short numAttributes = data.getS2At(OFFSET_OF_ATTRIBUTES_COUNT);
        ArrayList tmpAttributes = new ArrayList();
        tmpAttributes.ensureCapacity(numAttributes);
        ContiguousEntityFactory.build(data.getOffsetData(OFFSET_OF_ATTRIBUTES), numAttributes, tmpAttributes, new UnaryFunction<ByteData, Attribute>(){

            @Override
            public Attribute invoke(ByteData arg) {
                return AttributeFactory.build(arg, ClassFile.this.constantPool);
            }
        });
        this.attributes = ContiguousEntityFactory.addToMap(new HashMap(), tmpAttributes);
        AccessFlag.applyAttributes(this.attributes, this.accessFlags);
        short superClassIndex = data.getS2At(OFFSET_OF_SUPER_CLASS);
        this.rawSuperClass = superClassIndex == 0 ? null : (ConstantPoolEntryClass)this.constantPool.getEntry(superClassIndex);
        this.classSignature = this.getSignature(this.constantPool, this.rawSuperClass, this.rawInterfaces);
        this.innerClassesByTypeInfo = new LinkedHashMap<JavaTypeInstance, Pair<InnerClassAttributeInfo, ClassFile>>();
        boolean isInterface = this.accessFlags.contains((Object)AccessFlag.ACC_INTERFACE);
        boolean isAnnotation = this.accessFlags.contains((Object)AccessFlag.ACC_ANNOTATION);
        this.dumpHelper = isInterface ? (isAnnotation ? new ClassFileDumperAnnotation(dcCommonState) : new ClassFileDumperInterface(dcCommonState)) : new ClassFileDumperNormal(dcCommonState);
        ClassFileVersion classFileVersion = new ClassFileVersion(this.majorVer, this.minorVer);
        if (classFileVersion.before(ClassFileVersion.JAVA_6)) {
            boolean hasSignature = false;
            if (null != this.getAttributeByName("Signature")) {
                hasSignature = true;
            }
            if (!hasSignature) {
                for (Method method : this.methods) {
                    if (null == method.getSignatureAttribute()) continue;
                    hasSignature = true;
                }
            }
            if (hasSignature) {
                this.addComment("This class specifies class file version " + classFileVersion + " but uses Java 6 signatures.  Assumed Java 6.");
                classFileVersion = ClassFileVersion.JAVA_6;
            }
        }
        this.classFileVersion = classFileVersion;
        AttributeInnerClasses attributeInnerClasses = (AttributeInnerClasses)this.getAttributeByName("InnerClasses");
        if (!(typeInstance = (JavaRefTypeInstance)this.thisClass.getTypeInstance()).getInnerClassHereInfo().isInnerClass()) return;
        ClassFile.checkInnerClassAssumption(attributeInnerClasses, typeInstance);
    }

    private static void checkInnerClassAssumption(AttributeInnerClasses attributeInnerClasses, JavaRefTypeInstance typeInstance) {
        if (attributeInnerClasses != null) {
            for (InnerClassAttributeInfo innerClassAttributeInfo : attributeInnerClasses.getInnerClassAttributeInfoList()) {
                if (!innerClassAttributeInfo.getInnerClassInfo().equals(typeInstance)) continue;
                return;
            }
        }
        typeInstance.markNotInner();
    }

    public String getUsePath() {
        return this.usePath;
    }

    private void addComment(String comment) {
        if (this.decompilerComments == null) {
            this.decompilerComments = new DecompilerComments();
        }
        this.decompilerComments.addComment(comment);
    }

    private void addComment(String comment, Exception e) {
        if (this.decompilerComments == null) {
            this.decompilerComments = new DecompilerComments();
        }
        this.decompilerComments.addComment(new DecompilerComment(comment, e));
    }

    public List<ConstantPool> getAllCps() {
        Set res = SetFactory.newSet();
        this.getAllCps(res);
        return ListFactory.newList(res);
    }

    private void getAllCps(Set<ConstantPool> tgt) {
        tgt.add(this.constantPool);
        for (Pair<InnerClassAttributeInfo, ClassFile> pair : this.innerClassesByTypeInfo.values()) {
            pair.getSecond().getAllCps(tgt);
        }
    }

    public List<JavaTypeInstance> getAllClassTypes() {
        List res = ListFactory.newList();
        this.getAllClassTypes(res);
        return res;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        if (this.thisClass != null) {
            collector.collect(this.thisClass.getTypeInstance());
        }
        collector.collectFrom(this.classSignature);
        for (ClassFileField field : this.fields) {
            collector.collectFrom(field.getField());
            collector.collectFrom(field.getInitialValue());
        }
        collector.collectFrom((Collection<? extends TypeUsageCollectable>)this.methods);
        for (Map.Entry innerClassByTypeInfo : this.innerClassesByTypeInfo.entrySet()) {
            collector.collect((JavaTypeInstance)innerClassByTypeInfo.getKey());
            ClassFile innerClassFile = (ClassFile)((Pair)innerClassByTypeInfo.getValue()).getSecond();
            innerClassFile.collectTypeUsages(collector);
        }
        collector.collectFrom(this.dumpHelper);
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeVisibleAnnotations"));
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeInvisibleAnnotations"));
    }

    private void getAllClassTypes(List<JavaTypeInstance> tgt) {
        tgt.add(this.getClassType());
        for (Pair<InnerClassAttributeInfo, ClassFile> pair : this.innerClassesByTypeInfo.values()) {
            pair.getSecond().getAllClassTypes(tgt);
        }
    }

    public void setDumpHelper(ClassFileDumper dumpHelper) {
        this.dumpHelper = dumpHelper;
    }

    public void markHiddenInnerClass() {
        this.hiddenInnerClass = true;
    }

    public ClassFileVersion getClassFileVersion() {
        return this.classFileVersion;
    }

    public boolean isInnerClass() {
        if (this.thisClass != null) return this.thisClass.getTypeInstance().getInnerClassHereInfo().isInnerClass();
        return false;
    }

    public ConstantPool getConstantPool() {
        return this.constantPool;
    }

    public boolean testAccessFlag(AccessFlag accessFlag) {
        return this.accessFlags.contains((Object)accessFlag);
    }

    private void markAsStatic() {
        this.accessFlags.add(AccessFlag.ACC_STATIC);
    }

    public boolean hasFormalTypeParameters() {
        List<FormalTypeParameter> formalTypeParameters = this.classSignature.getFormalTypeParameters();
        return !(formalTypeParameters == null || formalTypeParameters.isEmpty());
    }

    public ClassFileField getFieldByName(String name) throws NoSuchFieldException {
        ClassFileField field;
        if (this.fieldsByName == null) {
            this.fieldsByName = MapFactory.newMap();
            for (ClassFileField field2 : this.fields) {
                this.fieldsByName.put(field2.getField().getFieldName(), field2);
            }
        }
        if ((field = this.fieldsByName.get(name)) != null) return field;
        throw new NoSuchFieldException(name);
    }

    public List<ClassFileField> getFields() {
        return this.fields;
    }

    public List<Method> getMethods() {
        return this.methods;
    }

    public void removePointlessMethod(Method method) {
        this.methodsByName.remove(method.getName());
        this.methods.remove(method);
    }

    private List<Method> getMethodsWithMatchingName(MethodPrototype prototype) {
        List<Method> named = Functional.filter(this.methods, new Predicate<Method>(){

            @Override
            public boolean test(Method in) {
                return in.getName().equals(prototype.getName());
            }
        });
        return named;
    }

    public OverloadMethodSet getOverloadMethodSet(MethodPrototype prototype) {
        List<Method> named = this.getMethodsWithMatchingName(prototype);
        boolean isInstance = prototype.isInstanceMethod();
        int numArgs = prototype.getArgs().size();
        boolean isVarArgs = prototype.isVarArgs();
        named = Functional.filter(named, new Predicate<Method>(){

            @Override
            public boolean test(Method in) {
                MethodPrototype other = in.getMethodPrototype();
                if (other.isInstanceMethod() != isInstance) {
                    return false;
                }
                boolean otherIsVarargs = other.isVarArgs();
                if (isVarArgs) {
                    if (otherIsVarargs) {
                        return true;
                    }
                    return other.getArgs().size() >= numArgs;
                }
                if (otherIsVarargs) {
                    return other.getArgs().size() <= numArgs;
                }
                return other.getArgs().size() == numArgs;
            }
        });
        List prototypes = Functional.map(named, new UnaryFunction<Method, MethodPrototype>(){

            @Override
            public MethodPrototype invoke(Method arg) {
                return arg.getMethodPrototype();
            }
        });
        List out = ListFactory.newList();
        Set matched = SetFactory.newSet();
        out.add((MethodPrototype)prototype);
        matched.add((String)prototype.getComparableString());
        Iterator i$ = prototypes.iterator();
        while (i$.hasNext()) {
            MethodPrototype other;
            if (!matched.add((String)(other = (MethodPrototype)i$.next()).getComparableString())) continue;
            out.add((MethodPrototype)other);
        }
        return new OverloadMethodSet(this, prototype, out);
    }

    public Method getMethodByPrototype(MethodPrototype prototype) throws NoSuchMethodException {
        List<Method> named = this.getMethodsWithMatchingName(prototype);
        Method methodMatch = null;
        Iterator<Method> i$ = named.iterator();
        while (i$.hasNext()) {
            MethodPrototype tgt;
            Method method;
            if ((tgt = (method = i$.next()).getMethodPrototype()).equalsMatch(prototype)) {
                return method;
            }
            if (!tgt.equalsGeneric(prototype)) continue;
            methodMatch = method;
        }
        if (methodMatch == null) throw new NoSuchMethodException();
        return methodMatch;
    }

    public Method getMethodByPrototype(MethodPrototype prototype, GenericTypeBinder binder) throws NoSuchMethodException {
        List<Method> named = this.getMethodsWithMatchingName(prototype);
        Method methodMatch = null;
        Iterator<Method> i$ = named.iterator();
        while (i$.hasNext()) {
            Method method;
            MethodPrototype tgt;
            if ((tgt = (method = i$.next()).getMethodPrototype()).equalsMatch(prototype)) {
                return method;
            }
            if (binder == null || !tgt.equalsGeneric(prototype, binder)) continue;
            methodMatch = method;
        }
        if (methodMatch == null) throw new NoSuchMethodException();
        return methodMatch;
    }

    public Method getSingleMethodByNameOrNull(String name) {
        List<Method> methodList = this.getMethodsByNameOrNull(name);
        if (methodList != null && methodList.size() == 1) return methodList.get(0);
        return null;
    }

    public List<Method> getMethodsByNameOrNull(String name) {
        if (this.methodsByName != null) return this.methodsByName.get(name);
        this.methodsByName = MapFactory.newMap();
        Iterator<Method> i$ = this.methods.iterator();
        while (i$.hasNext()) {
            List<Method> list;
            Method method;
            if ((list = this.methodsByName.get((method = i$.next()).getName())) == null) {
                list = ListFactory.newList();
                this.methodsByName.put(method.getName(), list);
            }
            list.add(method);
        }
        return this.methodsByName.get(name);
    }

    public List<Method> getMethodByName(String name) throws NoSuchMethodException {
        List<Method> methods = this.getMethodsByNameOrNull(name);
        if (methods != null) return methods;
        throw new NoSuchMethodException(name);
    }

    public List<Method> getConstructors() {
        List res = ListFactory.newList();
        for (Method method : this.methods) {
            if (!method.isConstructor()) continue;
            res.add((Method)method);
        }
        return res;
    }

    public <X extends Attribute> X getAttributeByName(String name) {
        Attribute attribute = this.attributes.get(name);
        if (attribute == null) {
            return null;
        }
        Attribute tmp = attribute;
        return tmp;
    }

    public AttributeBootstrapMethods getBootstrapMethods() {
        return (AttributeBootstrapMethods)this.getAttributeByName("BootstrapMethods");
    }

    public ConstantPoolEntryClass getThisClassConstpoolEntry() {
        return this.thisClass;
    }

    private void markInnerClassAsStatic(Options options, ClassFile innerClass, JavaTypeInstance thisType) {
        List<Method> constructors = innerClass.getConstructors();
        InnerClassInfo innerClassInfo = innerClass.getClassType().getInnerClassHereInfo();
        if (!innerClassInfo.isInnerClass()) {
            return;
        }
        Iterator<Method> i$ = constructors.iterator();
        while (i$.hasNext()) {
            Method constructor;
            List<JavaTypeInstance> params;
            if ((params = (constructor = i$.next()).getMethodPrototype().getArgs()) != null && !params.isEmpty() && params.get(0).equals(thisType)) continue;
            innerClass.markAsStatic();
            return;
        }
        if (!((Boolean)options.getOption(OptionsImpl.REMOVE_INNER_CLASS_SYNTHETICS)).booleanValue()) return;
        innerClassInfo.setHideSyntheticThis();
    }

    public void loadInnerClasses(DCCommonState dcCommonState) {
        Options options = dcCommonState.getOptions();
        AttributeInnerClasses attributeInnerClasses = (AttributeInnerClasses)this.getAttributeByName("InnerClasses");
        if (attributeInnerClasses == null) {
            return;
        }
        List<InnerClassAttributeInfo> innerClassAttributeInfoList = attributeInnerClasses.getInnerClassAttributeInfoList();
        JavaTypeInstance thisType = this.thisClass.getTypeInstance();
        Iterator<InnerClassAttributeInfo> i$ = innerClassAttributeInfoList.iterator();
        while (i$.hasNext()) {
            JavaTypeInstance innerType;
            InnerClassAttributeInfo innerClassAttributeInfo;
            if ((innerType = (innerClassAttributeInfo = i$.next()).getInnerClassInfo()) == null) continue;
            if (!innerType.getInnerClassHereInfo().isInnerClassOf(thisType)) continue;
            try {
                ClassFile innerClass = dcCommonState.getClassFile(innerType);
                innerClass.loadInnerClasses(dcCommonState);
                this.markInnerClassAsStatic(options, innerClass, thisType);
                this.innerClassesByTypeInfo.put(innerType, new Pair<InnerClassAttributeInfo, ClassFile>(innerClassAttributeInfo, innerClass));
            }
            catch (CannotLoadClassException e) {}
        }
    }

    private void analyseInnerClassesPass1(DCCommonState state) {
        if (this.innerClassesByTypeInfo == null) {
            return;
        }
        for (Pair<InnerClassAttributeInfo, ClassFile> innerClassInfoClassFilePair : this.innerClassesByTypeInfo.values()) {
            ClassFile classFile = innerClassInfoClassFilePair.getSecond();
            classFile.analyseMid(state);
        }
    }

    private void analysePassOuterFirst(DCCommonState state) {
        try {
            CodeAnalyserWholeClass.wholeClassAnalysisPass2(this, state);
        }
        catch (RuntimeException e) {
            this.addComment("Exception performing whole class analysis ignored.", e);
        }
        if (this.innerClassesByTypeInfo == null) {
            return;
        }
        for (Pair<InnerClassAttributeInfo, ClassFile> innerClassInfoClassFilePair : this.innerClassesByTypeInfo.values()) {
            ClassFile classFile = innerClassInfoClassFilePair.getSecond();
            classFile.analysePassOuterFirst(state);
        }
    }

    public void analyseTop(DCCommonState dcCommonState) {
        this.analyseMid(dcCommonState);
        this.analysePassOuterFirst(dcCommonState);
    }

    private void analyseOverrides() {
        try {
            BindingSuperContainer bindingSuperContainer = this.getBindingSupers();
            Map<JavaRefTypeInstance, JavaGenericRefTypeInstance> boundSupers = bindingSuperContainer.getBoundSuperClasses();
            List bindTesters = ListFactory.newList();
            Iterator<Map.Entry<JavaRefTypeInstance, JavaGenericRefTypeInstance>> i$ = boundSupers.entrySet().iterator();
            while (i$.hasNext()) {
                Map.Entry<JavaRefTypeInstance, JavaGenericRefTypeInstance> entry;
                JavaRefTypeInstance superC;
                if ((superC = (entry = i$.next()).getKey()).equals(this.getClassType())) continue;
                ClassFile superClsFile = null;
                try {
                    superClsFile = superC.getClassFile();
                }
                catch (CannotLoadClassException e) {
                    // empty catch block
                }
                if (superClsFile == null) continue;
                if (superClsFile == this) continue;
                JavaGenericRefTypeInstance boundSuperC = entry.getValue();
                GenericTypeBinder binder = null;
                if (boundSuperC != null) {
                    binder = superClsFile.getGenericTypeBinder(boundSuperC);
                }
                bindTesters.add(Triplet.make(superC, superClsFile, binder));
            }
            i$ = this.methods.iterator();
            while (i$.hasNext()) {
                Method method;
                if ((method = (Method)i$.next()).isConstructor()) continue;
                MethodPrototype prototype = method.getMethodPrototype();
                Method baseMethod = null;
                for (Triplet bindTester : bindTesters) {
                    JavaRefTypeInstance refType = (JavaRefTypeInstance)bindTester.getFirst();
                    ClassFile classFile = (ClassFile)bindTester.getSecond();
                    GenericTypeBinder genericTypeBinder = (GenericTypeBinder)bindTester.getThird();
                    try {
                        baseMethod = classFile.getMethodByPrototype(prototype, genericTypeBinder);
                    }
                    catch (NoSuchMethodException e) {
                        // empty catch block
                    }
                    if (baseMethod == null) continue;
                }
                if (baseMethod == null) continue;
                method.markOverride();
            }
        }
        catch (RuntimeException e) {
            this.addComment("Failed to analyse overrides", e);
        }
    }

    public void analyseMid(DCCommonState state) {
        Options options = state.getOptions();
        if (this.begunAnalysis) {
            return;
        }
        this.begunAnalysis = true;
        if (((Boolean)options.getOption(OptionsImpl.DECOMPILE_INNER_CLASSES)).booleanValue()) {
            this.analyseInnerClassesPass1(state);
        }
        for (Method method : this.methods) {
            method.analyse();
        }
        try {
            if (options.getOption(OptionsImpl.OVERRIDES, this.classFileVersion).booleanValue()) {
                this.analyseOverrides();
            }
            CodeAnalyserWholeClass.wholeClassAnalysisPass1(this, state);
        }
        catch (RuntimeException e) {
            this.addComment("Exception performing whole class analysis.");
        }
    }

    public JavaTypeInstance getClassType() {
        return this.thisClass.getTypeInstance();
    }

    public JavaTypeInstance getBaseClassType() {
        return this.classSignature.getSuperClass();
    }

    public ClassSignature getClassSignature() {
        return this.classSignature;
    }

    public Set<AccessFlag> getAccessFlags() {
        return this.accessFlags;
    }

    private ClassSignature getSignature(ConstantPool cp, ConstantPoolEntryClass rawSuperClass, List<ConstantPoolEntryClass> rawInterfaces) {
        AttributeSignature signatureAttribute = (AttributeSignature)this.getAttributeByName("Signature");
        if (signatureAttribute != null) return ConstantPoolUtils.parseClassSignature(signatureAttribute.getSignature(), cp);
        List interfaces = ListFactory.newList();
        for (ConstantPoolEntryClass rawInterface : rawInterfaces) {
            interfaces.add((JavaTypeInstance)rawInterface.getTypeInstance());
        }
        return new ClassSignature(null, rawSuperClass == null ? null : rawSuperClass.getTypeInstance(), interfaces);
    }

    public void dumpNamedInnerClasses(Dumper d) {
        if (this.innerClassesByTypeInfo == null || this.innerClassesByTypeInfo.isEmpty()) {
            return;
        }
        d.newln();
        Iterator<Pair<InnerClassAttributeInfo, ClassFile>> i$ = this.innerClassesByTypeInfo.values().iterator();
        while (i$.hasNext()) {
            InnerClassInfo innerClassInfo;
            Pair<InnerClassAttributeInfo, ClassFile> innerClassEntry;
            if ((innerClassInfo = (innerClassEntry = i$.next()).getFirst().getInnerClassInfo().getInnerClassHereInfo()).isMethodScopedClass()) continue;
            ClassFile classFile = innerClassEntry.getSecond();
            if (classFile.hiddenInnerClass) continue;
            TypeUsageInformation typeUsageInformation = d.getTypeUsageInformation();
            InnerClassTypeUsageInformation innerclassTypeUsageInformation = new InnerClassTypeUsageInformation(typeUsageInformation, (JavaRefTypeInstance)classFile.getClassType());
            TypeOverridingDumper d2 = new TypeOverridingDumper(d, innerclassTypeUsageInformation);
            classFile.dumpHelper.dump(classFile, true, d2);
            d.newln();
        }
    }

    @Override
    public Dumper dump(Dumper d) {
        return this.dumpHelper.dump(this, false, d);
    }

    public Dumper dumpAsInnerClass(Dumper d) {
        return this.dumpHelper.dump(this, true, d);
    }

    public String getFilePath() {
        return this.thisClass.getFilePath();
    }

    public String toString() {
        return this.thisClass.getTextPath();
    }

    public BindingSuperContainer getBindingSupers() {
        if (this.boundSuperClasses != null) return this.boundSuperClasses;
        this.boundSuperClasses = this.generateBoundSuperClasses();
        return this.boundSuperClasses;
    }

    private BindingSuperContainer generateBoundSuperClasses() {
        GenericTypeBinder genericTypeBinder;
        BoundSuperCollector boundSuperCollector = new BoundSuperCollector(this);
        JavaTypeInstance thisType = this.getClassSignature().getThisGeneralTypeClass(this.getClassType(), this.getConstantPool());
        if (thisType instanceof JavaGenericRefTypeInstance) {
            JavaGenericRefTypeInstance genericThisType = (JavaGenericRefTypeInstance)thisType;
            genericTypeBinder = GenericTypeBinder.buildIdentityBindings(genericThisType);
            boundSuperCollector.collect(genericThisType, BindingSuperContainer.Route.IDENTITY);
        } else {
            genericTypeBinder = null;
            boundSuperCollector.collect((JavaRefTypeInstance)thisType, BindingSuperContainer.Route.IDENTITY);
        }
        this.getBoundSuperClasses2(this.classSignature.getSuperClass(), genericTypeBinder, boundSuperCollector, BindingSuperContainer.Route.EXTENSION);
        for (JavaTypeInstance interfaceBase : this.classSignature.getInterfaces()) {
            this.getBoundSuperClasses2(interfaceBase, genericTypeBinder, boundSuperCollector, BindingSuperContainer.Route.INTERFACE);
        }
        return boundSuperCollector.getBoundSupers();
    }

    public void getBoundSuperClasses(JavaTypeInstance boundGeneric, BoundSuperCollector boundSuperCollector, BindingSuperContainer.Route route) {
        GenericTypeBinder genericTypeBinder;
        JavaTypeInstance thisType = this.getClassSignature().getThisGeneralTypeClass(this.getClassType(), this.getConstantPool());
        if (!(thisType instanceof JavaGenericRefTypeInstance)) {
            genericTypeBinder = null;
        } else {
            JavaGenericRefTypeInstance genericThisType = (JavaGenericRefTypeInstance)thisType;
            genericTypeBinder = boundGeneric instanceof JavaGenericRefTypeInstance ? GenericTypeBinder.extractBindings(genericThisType, (JavaGenericRefTypeInstance)boundGeneric) : null;
        }
        this.getBoundSuperClasses2(this.classSignature.getSuperClass(), genericTypeBinder, boundSuperCollector, route);
        for (JavaTypeInstance interfaceBase : this.classSignature.getInterfaces()) {
            this.getBoundSuperClasses2(interfaceBase, genericTypeBinder, boundSuperCollector, BindingSuperContainer.Route.INTERFACE);
        }
    }

    public GenericTypeBinder getGenericTypeBinder(JavaGenericRefTypeInstance boundGeneric) {
        JavaTypeInstance thisType = this.getClassSignature().getThisGeneralTypeClass(this.getClassType(), this.getConstantPool());
        if (!(thisType instanceof JavaGenericRefTypeInstance)) {
            return null;
        }
        JavaGenericRefTypeInstance genericThisType = (JavaGenericRefTypeInstance)thisType;
        return GenericTypeBinder.extractBindings(genericThisType, boundGeneric);
    }

    private void getBoundSuperClasses2(JavaTypeInstance base, GenericTypeBinder genericTypeBinder, BoundSuperCollector boundSuperCollector, BindingSuperContainer.Route route) {
        if (base instanceof JavaRefTypeInstance) {
            boundSuperCollector.collect((JavaRefTypeInstance)base, route);
            ClassFile classFile = ((JavaRefTypeInstance)base).getClassFile();
            if (classFile == null) return;
            classFile.getBoundSuperClasses(base, boundSuperCollector, route);
            return;
        }
        if (!(base instanceof JavaGenericRefTypeInstance)) {
            throw new IllegalStateException("Base class is not generic");
        }
        JavaGenericRefTypeInstance genericBase = (JavaGenericRefTypeInstance)base;
        JavaGenericRefTypeInstance boundBase = genericBase.getBoundInstance(genericTypeBinder);
        boundSuperCollector.collect(boundBase, route);
        ClassFile classFile = null;
        try {
            classFile = genericBase.getDeGenerifiedType().getClassFile();
        }
        catch (CannotLoadClassException e) {
            // empty catch block
        }
        if (classFile == null) {
            return;
        }
        classFile.getBoundSuperClasses(boundBase, boundSuperCollector, route);
    }

    public void noteAnonymousUse(ConstructorInvokationAnoynmousInner anoynmousInner) {
        this.anonymousUsages.add(anoynmousInner);
    }

    public List<ConstructorInvokationAnoynmousInner> getAnonymousUsages() {
        return this.anonymousUsages;
    }

}

