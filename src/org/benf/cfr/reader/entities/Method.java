/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototypeAnnotationsHelper;
import org.benf.cfr.reader.bytecode.analysis.variables.Ident;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamerFactory;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.AccessFlagMethod;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.annotations.ElementValue;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.attributes.AttributeAnnotationDefault;
import org.benf.cfr.reader.entities.attributes.AttributeCode;
import org.benf.cfr.reader.entities.attributes.AttributeExceptions;
import org.benf.cfr.reader.entities.attributes.AttributeLocalVariableTable;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeInvisibleAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeInvisibleParameterAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeVisibleAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeVisibleParameterAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeSignature;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.entityfactories.AttributeFactory;
import org.benf.cfr.reader.entityfactories.ContiguousEntityFactory;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.LocalClassAwareTypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.CollectionUtils;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.DecompilerComments;
import org.benf.cfr.reader.util.KnowsRawSize;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.TypeOverridingDumper;

public class Method
implements KnowsRawSize,
TypeUsageCollectable {
    private static final long OFFSET_OF_ACCESS_FLAGS = 0;
    private static final long OFFSET_OF_NAME_INDEX = 2;
    private static final long OFFSET_OF_DESCRIPTOR_INDEX = 4;
    private static final long OFFSET_OF_ATTRIBUTES_COUNT = 6;
    private static final long OFFSET_OF_ATTRIBUTES = 8;
    private final long length;
    private final EnumSet<AccessFlagMethod> accessFlags;
    private final Map<String, Attribute> attributes;
    private final String name;
    private MethodConstructor isConstructor;
    private final short descriptorIndex;
    private final AttributeCode codeAttribute;
    private final ConstantPool cp;
    private final VariableNamer variableNamer;
    private final MethodPrototype methodPrototype;
    private final ClassFile classFile;
    private boolean hidden;
    private DecompilerComments comments;
    private final Map<JavaRefTypeInstance, String> localClasses;
    private boolean isOverride;
    private transient Set<JavaTypeInstance> thrownTypes;

    public Method(ByteData raw, ClassFile classFile, ConstantPool cp, DCCommonState dcCommonState) {
        Attribute codeAttribute;
        this.localClasses = MapFactory.newLinkedMap();
        this.thrownTypes = null;
        Options options = dcCommonState.getOptions();
        this.cp = cp;
        this.classFile = classFile;
        this.accessFlags = AccessFlagMethod.build(raw.getS2At(0));
        this.descriptorIndex = raw.getS2At(4);
        short nameIndex = raw.getS2At(2);
        short numAttributes = raw.getS2At(6);
        ArrayList tmpAttributes = new ArrayList();
        tmpAttributes.ensureCapacity(numAttributes);
        long attributesLength = ContiguousEntityFactory.build(raw.getOffsetData(8), numAttributes, tmpAttributes, new UnaryFunction<ByteData, Attribute>(){

            @Override
            public Attribute invoke(ByteData arg) {
                return AttributeFactory.build(arg, cp);
            }
        });
        this.attributes = ContiguousEntityFactory.addToMap(new HashMap(), tmpAttributes);
        AccessFlagMethod.applyAttributes(this.attributes, this.accessFlags);
        this.length = 8 + attributesLength;
        this.name = cp.getUTF8Entry(nameIndex).getValue();
        MethodConstructor methodConstructor = MethodConstructor.NOT;
        if (this.name.equals("<init>")) {
            boolean isEnum;
            methodConstructor = (isEnum = classFile.getAccessFlags().contains((Object)AccessFlag.ACC_ENUM)) ? MethodConstructor.ENUM_CONSTRUCTOR : MethodConstructor.CONSTRUCTOR;
        } else if (this.name.equals("<clinit>")) {
            methodConstructor = MethodConstructor.STATIC_CONSTRUCTOR;
        }
        this.isConstructor = methodConstructor;
        if (this.isConstructor() && this.accessFlags.contains((Object)AccessFlagMethod.ACC_STRICT)) {
            this.accessFlags.remove((Object)AccessFlagMethod.ACC_STRICT);
            classFile.getAccessFlags().add(AccessFlag.ACC_STRICT);
        }
        if ((codeAttribute = this.attributes.get("Code")) == null) {
            this.variableNamer = VariableNamerFactory.getNamer(null, cp);
            this.codeAttribute = null;
        } else {
            this.codeAttribute = (AttributeCode)codeAttribute;
            this.variableNamer = VariableNamerFactory.getNamer(this.codeAttribute.getLocalVariableTable(), cp);
            this.codeAttribute.setMethod(this);
        }
        this.methodPrototype = this.generateMethodPrototype();
        if (!this.accessFlags.contains((Object)AccessFlagMethod.ACC_BRIDGE) || !((Boolean)options.getOption(OptionsImpl.HIDE_BRIDGE_METHODS)).booleanValue()) return;
        this.hidden = true;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.methodPrototype.collectTypeUsages(collector);
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeVisibleAnnotations"));
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeInvisibleAnnotations"));
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeVisibleParameterAnnotations"));
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeInvisibleParameterAnnotations"));
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("AnnotationDefault"));
        if (this.codeAttribute != null) {
            this.codeAttribute.analyse().collectTypeUsages(collector);
        }
        collector.collect((Collection<? extends JavaTypeInstance>)this.localClasses.keySet());
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("Exceptions"));
    }

    public Set<AccessFlagMethod> getAccessFlags() {
        return this.accessFlags;
    }

    public void hideSynthetic() {
        this.hidden = true;
    }

    public boolean isHiddenFromDisplay() {
        return this.hidden;
    }

    public boolean testAccessFlag(AccessFlagMethod flag) {
        return this.accessFlags.contains((Object)flag);
    }

    public MethodConstructor getConstructorFlag() {
        return this.isConstructor;
    }

    public AttributeSignature getSignatureAttribute() {
        return (AttributeSignature)this.getAttributeByName("Signature");
    }

    private <T extends Attribute> T getAttributeByName(String name) {
        Attribute attribute = this.attributes.get(name);
        if (attribute == null) {
            return null;
        }
        Attribute tmp = attribute;
        return tmp;
    }

    public VariableNamer getVariableNamer() {
        return this.variableNamer;
    }

    public ClassFile getClassFile() {
        return this.classFile;
    }

    @Override
    public long getRawByteLength() {
        return this.length;
    }

    public String getName() {
        return this.name;
    }

    private MethodPrototype generateMethodPrototype() {
        MethodPrototype descriptorProto;
        AttributeSignature sig = this.getSignatureAttribute();
        ConstantPoolEntryUTF8 signature = sig == null ? null : sig.getSignature();
        ConstantPoolEntryUTF8 descriptor = this.cp.getUTF8Entry(this.descriptorIndex);
        ConstantPoolEntryUTF8 prototype = null;
        prototype = signature == null ? descriptor : signature;
        boolean isInstance = !this.accessFlags.contains((Object)AccessFlagMethod.ACC_STATIC);
        boolean isVarargs = this.accessFlags.contains((Object)AccessFlagMethod.ACC_VARARGS);
        MethodPrototype res = ConstantPoolUtils.parseJavaMethodPrototype(this.classFile, this.classFile.getClassType(), this.getName(), isInstance, this.getConstructorFlag(), prototype, this.cp, isVarargs, this.variableNamer);
        if (!this.classFile.isInnerClass() || signature == null || (descriptorProto = ConstantPoolUtils.parseJavaMethodPrototype(this.classFile, this.classFile.getClassType(), this.getName(), isInstance, this.getConstructorFlag(), descriptor, this.cp, isVarargs, this.variableNamer)).getArgs().size() == res.getArgs().size()) return res;
        res = Method.fixupInnerClassSignature(descriptorProto, res);
        return res;
    }

    private static MethodPrototype fixupInnerClassSignature(MethodPrototype descriptor, MethodPrototype signature) {
        List<JavaTypeInstance> descriptorArgs = descriptor.getArgs();
        List<JavaTypeInstance> signatureArgs = signature.getArgs();
        if (signatureArgs.size() != descriptorArgs.size() - 1) {
            return signature;
        }
        for (int x = 0; x < signatureArgs.size(); ++x) {
            if (descriptorArgs.get(x + 1).equals(signatureArgs.get(x).getDeGenerifiedType())) continue;
            return signature;
        }
        signatureArgs.add(0, descriptorArgs.get(0));
        return signature;
    }

    public MethodPrototype getMethodPrototype() {
        return this.methodPrototype;
    }

    public void markOverride() {
        this.isOverride = true;
    }

    public void markUsedLocalClassType(JavaTypeInstance javaTypeInstance, String suggestedName) {
        javaTypeInstance = javaTypeInstance.getDeGenerifiedType();
        if (!(javaTypeInstance instanceof JavaRefTypeInstance)) {
            throw new IllegalStateException("Bad local class Type " + javaTypeInstance.getRawName());
        }
        this.localClasses.put((JavaRefTypeInstance)javaTypeInstance, suggestedName);
    }

    public void markUsedLocalClassType(JavaTypeInstance javaTypeInstance) {
        this.markUsedLocalClassType(javaTypeInstance, null);
    }

    private void dumpMethodAnnotations(Dumper d) {
        AttributeRuntimeVisibleAnnotations runtimeVisibleAnnotations = (AttributeRuntimeVisibleAnnotations)this.getAttributeByName("RuntimeVisibleAnnotations");
        AttributeRuntimeInvisibleAnnotations runtimeInvisibleAnnotations = (AttributeRuntimeInvisibleAnnotations)this.getAttributeByName("RuntimeInvisibleAnnotations");
        if (runtimeVisibleAnnotations != null) {
            runtimeVisibleAnnotations.dump(d);
        }
        if (runtimeInvisibleAnnotations != null) {
            runtimeInvisibleAnnotations.dump(d);
        }
        if (!this.isOverride) return;
        d.print("@Override\n");
    }

    public Set<JavaTypeInstance> getThrownTypes() {
        AttributeExceptions exceptionsAttribute;
        if (this.thrownTypes != null) return this.thrownTypes;
        this.thrownTypes = SetFactory.newOrderedSet();
        if ((exceptionsAttribute = (AttributeExceptions)this.getAttributeByName("Exceptions")) == null) return this.thrownTypes;
        List<ConstantPoolEntryClass> exceptionClasses = exceptionsAttribute.getExceptionClassList();
        for (ConstantPoolEntryClass exceptionClass : exceptionClasses) {
            JavaTypeInstance typeInstance = exceptionClass.getTypeInstance();
            this.thrownTypes.add(typeInstance);
        }
        return this.thrownTypes;
    }

    public void dumpSignatureText(boolean asClass, Dumper d) {
        String prefix;
        this.dumpMethodAnnotations(d);
        EnumSet<AccessFlagMethod> localAccessFlags = this.accessFlags;
        if (!asClass) {
            if (this.codeAttribute != null) {
                d.print("default ");
            }
            localAccessFlags = SetFactory.newSet(localAccessFlags);
            localAccessFlags.remove((Object)AccessFlagMethod.ACC_ABSTRACT);
        }
        if (!(prefix = CollectionUtils.join(localAccessFlags, " ")).isEmpty()) {
            d.print(prefix);
        }
        if (this.isConstructor == MethodConstructor.STATIC_CONSTRUCTOR) {
            return;
        }
        if (!prefix.isEmpty()) {
            d.print(' ');
        }
        MethodPrototypeAnnotationsHelper paramAnnotationsHelper = new MethodPrototypeAnnotationsHelper((AttributeRuntimeVisibleParameterAnnotations)this.getAttributeByName("RuntimeVisibleParameterAnnotations"), (AttributeRuntimeInvisibleParameterAnnotations)this.getAttributeByName("RuntimeInvisibleParameterAnnotations"));
        String displayName = this.name;
        if (this.isConstructor.isConstructor()) {
            displayName = d.getTypeUsageInformation().getName(this.classFile.getClassType());
        }
        this.getMethodPrototype().dumpDeclarationSignature(d, displayName, this.isConstructor, paramAnnotationsHelper);
        AttributeExceptions exceptionsAttribute = (AttributeExceptions)this.getAttributeByName("Exceptions");
        if (exceptionsAttribute == null) return;
        d.print(" throws ");
        boolean first = true;
        for (JavaTypeInstance typeInstance : this.getThrownTypes()) {
            first = CommaHelp.comma(first, d);
            d.dump(typeInstance);
        }
    }

    public Op04StructuredStatement getAnalysis() {
        if (this.codeAttribute == null) {
            throw new ConfusedCFRException("No code in this method to analyze");
        }
        Op04StructuredStatement analysis = this.codeAttribute.analyse();
        return analysis;
    }

    public boolean isConstructor() {
        return this.isConstructor.isConstructor();
    }

    public void analyse() {
        try {
            if (this.codeAttribute != null) {
                this.codeAttribute.analyse();
            }
            if (this.methodPrototype.parametersComputed()) return;
            Map identMap = MapFactory.newLazyMap(new UnaryFunction<Integer, Ident>(){
                public int x;

                @Override
                public Ident invoke(Integer arg) {
                    return new Ident(arg, 0);
                }
            });
            this.methodPrototype.computeParameters(this.getConstructorFlag(), identMap);
        }
        catch (RuntimeException e) {
            System.out.println("While processing method : " + this.getName());
            throw e;
        }
    }

    public boolean hasCodeAttribute() {
        return this.codeAttribute != null;
    }

    public boolean isInstanceMethod() {
        return !this.accessFlags.contains((Object)AccessFlagMethod.ACC_STATIC);
    }

    public void dumpComments(Dumper d) {
        if (this.comments == null) return;
        this.comments.dump(d);
        List<DecompilerComment> commentList = this.comments.getCommentList();
        Iterator<DecompilerComment> i$ = commentList.iterator();
        while (i$.hasNext()) {
            String string;
            DecompilerComment decompilerComment;
            if ((string = (decompilerComment = i$.next()).getSummaryMessage()) == null) continue;
            d.addSummaryError(this, string);
        }
    }

    public void setComments(DecompilerComments comments) {
        this.comments = comments;
    }

    public void dump(Dumper d, boolean asClass) {
        if (this.codeAttribute != null) {
            this.codeAttribute.analyse();
        }
        this.dumpComments(d);
        this.dumpSignatureText(asClass, d);
        if (this.codeAttribute == null) {
            AttributeAnnotationDefault annotationDefault;
            if ((annotationDefault = (AttributeAnnotationDefault)this.getAttributeByName("AnnotationDefault")) != null) {
                d.print(" default ").dump(annotationDefault.getElementValue());
            }
            d.endCodeln();
        } else {
            if (!this.localClasses.isEmpty()) {
                LocalClassAwareTypeUsageInformation overrides = new LocalClassAwareTypeUsageInformation(this.localClasses, d.getTypeUsageInformation());
                d = new TypeOverridingDumper(d, overrides);
            }
            d.print(' ').dump(this.codeAttribute);
        }
    }

    public String toString() {
        return this.name + ": " + this.methodPrototype;
    }

    public static enum MethodConstructor {
        NOT(false),
        STATIC_CONSTRUCTOR(false),
        CONSTRUCTOR(true),
        ENUM_CONSTRUCTOR(true);
        
        private final boolean isConstructor;

        private MethodConstructor(boolean isConstructor) {
            this.isConstructor = isConstructor;
        }

        public boolean isConstructor() {
            return this.isConstructor;
        }
    }

}

