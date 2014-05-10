/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.EnumSuperRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.AbstractMatchResultIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.CollectMatch;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleenePlus;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchOneOf;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractConstructorInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnoynmousInner;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumper;
import org.benf.cfr.reader.entities.classfilehelpers.ClassFileDumperEnum;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class EnumClassRewriter {
    private final ClassFile classFile;
    private final JavaTypeInstance classType;
    private final DCCommonState state;
    private final InferredJavaType clazzIJT;

    public static void rewriteEnumClass(ClassFile classFile, DCCommonState state) {
        List<JavaTypeInstance> boundTypes;
        JavaGenericRefTypeInstance genericBaseType;
        Options options = state.getOptions();
        ClassFileVersion classFileVersion = classFile.getClassFileVersion();
        if (!options.getOption(OptionsImpl.ENUM_SUGAR, classFileVersion).booleanValue()) {
            return;
        }
        JavaTypeInstance classType = classFile.getClassType();
        JavaTypeInstance baseType = classFile.getBaseClassType();
        JavaRefTypeInstance enumType = TypeConstants.ENUM;
        if (!(baseType instanceof JavaGenericRefTypeInstance)) {
            return;
        }
        if (!(genericBaseType = (JavaGenericRefTypeInstance)baseType).getDeGenerifiedType().equals(enumType)) {
            return;
        }
        if ((boundTypes = genericBaseType.getGenericTypes()) == null || boundTypes.size() != 1) {
            return;
        }
        if (!boundTypes.get(0).equals(classType)) {
            return;
        }
        EnumClassRewriter c = new EnumClassRewriter(classFile, classType, state);
        c.rewrite();
    }

    public EnumClassRewriter(ClassFile classFile, JavaTypeInstance classType, DCCommonState state) {
        this.classFile = classFile;
        this.classType = classType;
        this.state = state;
        this.clazzIJT = new InferredJavaType(classType, InferredJavaType.Source.UNKNOWN, true);
    }

    private boolean rewrite() {
        EnumInitMatchCollector initMatchCollector;
        Method staticInit = null;
        try {
            staticInit = this.classFile.getMethodByName("<clinit>").get(0);
        }
        catch (NoSuchMethodException e) {
            throw new ConfusedCFRException("No static init method on enum");
        }
        Op04StructuredStatement staticInitCode = staticInit.getAnalysis();
        if (!staticInitCode.isFullyStructured()) {
            return false;
        }
        if ((initMatchCollector = this.analyseStaticMethod(staticInitCode)) == null) {
            return false;
        }
        Method valueOf = null;
        Method values = null;
        try {
            valueOf = this.classFile.getMethodByName("valueOf").get(0);
            values = this.classFile.getMethodByName("values").get(0);
        }
        catch (NoSuchMethodException e) {
            return false;
        }
        valueOf.hideSynthetic();
        values.hideSynthetic();
        for (ClassFileField field : initMatchCollector.getMatchedHideTheseFields()) {
            field.markHidden();
        }
        Map entryMap = initMatchCollector.getEntryMap();
        CollectedEnumData matchedArray = initMatchCollector.getMatchedArray();
        for (CollectedEnumData entry : entryMap.values()) {
            entry.getContainer().nopOut();
        }
        matchedArray.getContainer().nopOut();
        List<Method> constructors = this.classFile.getConstructors();
        EnumSuperRewriter enumSuperRewriter = new EnumSuperRewriter();
        for (Method constructor : constructors) {
            enumSuperRewriter.rewrite(constructor.getAnalysis());
        }
        List entries = ListFactory.newList();
        for (Map.Entry entry2 : entryMap.entrySet()) {
            entries.add(Pair.make(entry2.getKey(), ((CollectedEnumData)entry2.getValue()).getData()));
        }
        this.classFile.setDumpHelper(new ClassFileDumperEnum(this.state, entries));
        return true;
    }

    private EnumInitMatchCollector analyseStaticMethod(Op04StructuredStatement statement) {
        List statements = ListFactory.newList();
        statement.linearizeStatementsInto(statements);
        statements = Functional.filter(statements, new Predicate<StructuredStatement>(){

            @Override
            public boolean test(StructuredStatement in) {
                return !(in instanceof StructuredComment);
            }
        });
        WildcardMatch wcm = new WildcardMatch();
        InferredJavaType clazzIJT = new InferredJavaType(this.classType, InferredJavaType.Source.UNKNOWN, true);
        JavaArrayTypeInstance arrayType = new JavaArrayTypeInstance(1, this.classType);
        InferredJavaType clazzAIJT = new InferredJavaType(arrayType, InferredJavaType.Source.UNKNOWN, true);
        MatchSequence matcher = new MatchSequence(new BeginBlock(null), (Matcher<StructuredStatement>)new KleenePlus((Matcher<StructuredStatement>)new MatchOneOf(new ResetAfterTest(wcm, new CollectMatch("entry", new StructuredAssignment(wcm.getStaticVariable("e", this.classType, clazzIJT), wcm.getConstructorSimpleWildcard("c", this.classType)))), new ResetAfterTest(wcm, new CollectMatch("entryderived", new StructuredAssignment(wcm.getStaticVariable("e2", this.classType, clazzIJT, false), wcm.getConstructorAnonymousWildcard("c2", null)))))), (Matcher<StructuredStatement>)new ResetAfterTest(wcm, new CollectMatch("values", new StructuredAssignment(wcm.getStaticVariable("v", this.classType, clazzAIJT), wcm.getNewArrayWildCard("v", 0, 1)))));
        MatchIterator mi = new MatchIterator(statements);
        EnumInitMatchCollector matchCollector = new EnumInitMatchCollector(wcm, null);
        mi.advance();
        if (!matcher.match(mi, matchCollector)) {
            return null;
        }
        if (matchCollector.isValid()) return matchCollector;
        return null;
    }

    class EnumInitMatchCollector
    extends AbstractMatchResultIterator {
        private final WildcardMatch wcm;
        private final Map<StaticVariable, CollectedEnumData<? extends AbstractConstructorInvokation>> entryMap;
        private CollectedEnumData<NewAnonymousArray> matchedArray;
        private List<ClassFileField> matchedHideTheseFields;

        private EnumInitMatchCollector(WildcardMatch wcm) {
            this.entryMap = MapFactory.newLinkedMap();
            this.matchedHideTheseFields = ListFactory.newList();
            this.wcm = wcm;
        }

        @Override
        public void clear() {
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            AbstractNewArray abstractNewArray;
            if (name.equals("entry")) {
                StaticVariable staticVariable = this.wcm.getStaticVariable("e").getMatch();
                ConstructorInvokationSimple constructorInvokation = this.wcm.getConstructorSimpleWildcard("c").getMatch();
                this.entryMap.put(staticVariable, new CollectedEnumData(statement.getContainer(), constructorInvokation, null));
                return;
            }
            if (name.equals("entryderived")) {
                StaticVariable staticVariable = this.wcm.getStaticVariable("e2").getMatch();
                ConstructorInvokationAnoynmousInner constructorInvokation = this.wcm.getConstructorAnonymousWildcard("c2").getMatch();
                this.entryMap.put(staticVariable, new CollectedEnumData(statement.getContainer(), constructorInvokation, null));
                return;
            }
            if (!name.equals("values")) return;
            if (!(abstractNewArray = this.wcm.getNewArrayWildCard("v").getMatch() instanceof NewAnonymousArray)) return;
            this.matchedArray = new CollectedEnumData<NewAnonymousArray>(statement.getContainer(), (NewAnonymousArray)abstractNewArray, null);
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
        }

        boolean isValid() {
            LValue valuesArray;
            List<Expression> values;
            List<ClassFileField> fields = this$0.classFile.getFields();
            ConstantPool cp = this$0.classFile.getConstantPool();
            for (ClassFileField classFileField : fields) {
                Field field = classFileField.getField();
                JavaTypeInstance fieldType = field.getJavaTypeInstance();
                boolean isStatic = field.testAccessFlag(AccessFlag.ACC_STATIC);
                boolean isEnum = field.testAccessFlag(AccessFlag.ACC_ENUM);
                boolean expected = isStatic && isEnum && fieldType.equals(this$0.classType);
                StaticVariable tmp = new StaticVariable(this$0.clazzIJT, this$0.classType, field.getFieldName());
                if (expected != this.entryMap.containsKey(tmp)) {
                    return false;
                }
                if (!expected) continue;
                this.matchedHideTheseFields.add(classFileField);
            }
            if ((values = ((NewAnonymousArray)CollectedEnumData.access$400(this.matchedArray)).getValues()).size() != this.entryMap.size()) {
                return false;
            }
            for (Expression value : values) {
                LValueExpression lValueExpression;
                LValue lvalue;
                StaticVariable staticVariable;
                if (!(value instanceof LValueExpression)) {
                    return false;
                }
                if (!(lvalue = (lValueExpression = (LValueExpression)value).getLValue() instanceof StaticVariable)) {
                    return false;
                }
                if (this.entryMap.containsKey(staticVariable = (StaticVariable)lvalue)) continue;
                return false;
            }
            if (!(valuesArray = ((StructuredAssignment)CollectedEnumData.access$300(this.matchedArray).getStatement()).getLvalue() instanceof StaticVariable)) {
                return false;
            }
            StaticVariable valuesArrayStatic = (StaticVariable)valuesArray;
            try {
                ClassFileField valuesField = this$0.classFile.getFieldByName(valuesArrayStatic.getVarName());
                if (!valuesField.getField().testAccessFlag(AccessFlag.ACC_STATIC)) {
                    return false;
                }
                this.matchedHideTheseFields.add(valuesField);
            }
            catch (NoSuchFieldException e) {
                return false;
            }
            return true;
        }

        private List<ClassFileField> getMatchedHideTheseFields() {
            return this.matchedHideTheseFields;
        }

        private Map<StaticVariable, CollectedEnumData<? extends AbstractConstructorInvokation>> getEntryMap() {
            return this.entryMap;
        }

        private CollectedEnumData<NewAnonymousArray> getMatchedArray() {
            return this.matchedArray;
        }

        /* synthetic */ EnumInitMatchCollector(EnumClassRewriter x0, WildcardMatch x1,  x2) {
            this(x1);
        }
    }

    static class CollectedEnumData<T> {
        private final Op04StructuredStatement container;
        private final T data;

        private CollectedEnumData(Op04StructuredStatement container, T data) {
            this.container = container;
            this.data = data;
        }

        private Op04StructuredStatement getContainer() {
            return this.container;
        }

        private T getData() {
            return this.data;
        }

        /* synthetic */ CollectedEnumData(Op04StructuredStatement x0, Object x1,  x2) {
            this(x0, x1);
        }
    }

}

