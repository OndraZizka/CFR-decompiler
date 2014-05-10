/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.Op04Rewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.AbstractMatchResultIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.CollectMatch;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleenePlus;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayLength;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewPrimitiveArray;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.ArrayVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCase;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredSwitch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredTry;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class SwitchEnumRewriter
implements Op04Rewriter {
    private final DCCommonState dcCommonState;
    private final ClassFileVersion classFileVersion;
    private static final JavaTypeInstance expectedLUTType = new JavaArrayTypeInstance(1, RawJavaType.INT);

    public SwitchEnumRewriter(DCCommonState dcCommonState, ClassFileVersion classFileVersion) {
        this.dcCommonState = dcCommonState;
        this.classFileVersion = classFileVersion;
    }

    @Override
    public void rewrite(Op04StructuredStatement root) {
        List<StructuredStatement> structuredStatements;
        Options options = this.dcCommonState.getOptions();
        if (!options.getOption(OptionsImpl.ENUM_SWITCH, this.classFileVersion).booleanValue()) {
            return;
        }
        if ((structuredStatements = MiscStatementTools.linearise(root)) == null) {
            return;
        }
        List<StructuredStatement> switchStatements = Functional.filter(structuredStatements, new Predicate<StructuredStatement>(){

            @Override
            public boolean test(StructuredStatement in) {
                return in.getClass() == StructuredSwitch.class;
            }
        });
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(switchStatements);
        WildcardMatch wcm = new WildcardMatch();
        ResetAfterTest m = new ResetAfterTest(wcm, new CollectMatch("switch", new StructuredSwitch(new ArrayIndex(new LValueExpression(wcm.getLValueWildCard("lut")), wcm.getMemberFunction("fncall", "ordinal", (Expression)wcm.getExpressionWildCard("object"))), null, wcm.getBlockIdentifier("block"))));
        SwitchEnumMatchResultCollector matchResultCollector = new SwitchEnumMatchResultCollector(wcm, null);
        while (mi.hasNext()) {
            mi.advance();
            matchResultCollector.clear();
            if (!m.match(mi, matchResultCollector)) continue;
            this.tryRewrite(matchResultCollector);
        }
    }

    private void tryRewrite(SwitchEnumMatchResultCollector mrc) {
        StructuredStatement switchBlockStatement;
        ClassFile enumLutClass;
        Field lut;
        Method lutStaticInit;
        Op04StructuredStatement switchBlock;
        StructuredSwitch structuredSwitch = mrc.getStructuredSwitch();
        LValue lookupTable = mrc.getLookupTable();
        Expression enumObject = mrc.getEnumObject();
        if (!(lookupTable instanceof StaticVariable)) {
            return;
        }
        StaticVariable staticLookupTable = (StaticVariable)lookupTable;
        JavaTypeInstance classInfo = staticLookupTable.getOwningClassTypeInstance();
        String varName = staticLookupTable.getVarName();
        try {
            enumLutClass = this.dcCommonState.getClassFile(classInfo);
        }
        catch (CannotLoadClassException e) {
            return;
        }
        ConstantPool classConstantPool = enumLutClass.getConstantPool();
        try {
            lut = enumLutClass.getFieldByName(varName).getField();
        }
        catch (NoSuchFieldException e) {
            return;
        }
        JavaTypeInstance fieldType = lut.getJavaTypeInstance();
        if (!fieldType.equals(SwitchEnumRewriter.expectedLUTType)) {
            return;
        }
        try {
            lutStaticInit = enumLutClass.getMethodByName("<clinit>").get(0);
        }
        catch (NoSuchMethodException e) {
            return;
        }
        Op04StructuredStatement lutStaticInitCode = lutStaticInit.getAnalysis();
        List<StructuredStatement> structuredStatements = MiscStatementTools.linearise(lutStaticInitCode);
        if (structuredStatements == null) {
            return;
        }
        structuredStatements = Functional.filter(structuredStatements, new Predicate<StructuredStatement>(){

            @Override
            public boolean test(StructuredStatement in) {
                return !(in instanceof StructuredComment);
            }
        });
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(structuredStatements);
        WildcardMatch wcm1 = new WildcardMatch();
        WildcardMatch wcm2 = new WildcardMatch();
        ResetAfterTest m = new ResetAfterTest(wcm1, new MatchSequence(new StructuredAssignment(lookupTable, new NewPrimitiveArray((Expression)new ArrayLength(wcm1.getStaticFunction("func", enumObject.getInferredJavaType().getJavaTypeInstance(), "values")), RawJavaType.INT)), (Matcher<StructuredStatement>)new KleenePlus((Matcher<StructuredStatement>)new ResetAfterTest(wcm2, new MatchSequence(new StructuredTry(null, null, null), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new StructuredAssignment(new ArrayVariable(new ArrayIndex(new LValueExpression(lookupTable), wcm2.getMemberFunction("ordinal", "ordinal", (Expression)new LValueExpression(wcm2.getStaticVariable("enumval", enumObject.getInferredJavaType().getJavaTypeInstance(), enumObject.getInferredJavaType()))))), wcm2.getExpressionWildCard("literal")), (Matcher<StructuredStatement>)new EndBlock(null), (Matcher<StructuredStatement>)new StructuredCatch(null, null, null, null), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new EndBlock(null))))));
        SwitchForeignEnumMatchResultCollector matchResultCollector = new SwitchForeignEnumMatchResultCollector(wcm1, wcm2, null);
        boolean matched = false;
        while (mi.hasNext()) {
            mi.advance();
            matchResultCollector.clear();
            if (!m.match(mi, matchResultCollector)) continue;
            matched = true;
        }
        if (!matched) {
            return;
        }
        Map<Integer, StaticVariable> reverseLut = matchResultCollector.getLUT();
        if (!(switchBlockStatement = (switchBlock = structuredSwitch.getBody()).getStatement() instanceof Block)) {
            throw new IllegalStateException("Inside switch should be a block");
        }
        Block block = (Block)switchBlockStatement;
        List<Op04StructuredStatement> caseStatements = block.getBlockStatements();
        LinkedList newBlockContent = ListFactory.newLinkedList();
        InferredJavaType inferredJavaType = enumObject.getInferredJavaType();
        Iterator<Op04StructuredStatement> i$ = caseStatements.iterator();
        while (i$.hasNext()) {
            StructuredStatement caseInner;
            Op04StructuredStatement caseOuter;
            if (!(caseInner = (caseOuter = i$.next()).getStatement() instanceof StructuredCase)) {
                return;
            }
            StructuredCase caseStmt = (StructuredCase)caseInner;
            List<Expression> values = caseStmt.getValues();
            List newValues = ListFactory.newList();
            Iterator<Expression> i$2 = values.iterator();
            while (i$2.hasNext()) {
                Expression value;
                StaticVariable enumVal;
                Integer iVal;
                if ((iVal = this.getIntegerFromLiteralExpression(value = i$2.next())) == null) {
                    return;
                }
                if ((enumVal = reverseLut.get(iVal)) == null) {
                    return;
                }
                newValues.add((LValueExpression)new LValueExpression(enumVal));
            }
            StructuredCase replacement = new StructuredCase(newValues, inferredJavaType, caseStmt.getBody(), caseStmt.getBlockIdentifier(), true);
            newBlockContent.add((Op04StructuredStatement)new Op04StructuredStatement(replacement));
        }
        Block replacementBlock = new Block(newBlockContent, block.isIndenting());
        StructuredSwitch newSwitch = new StructuredSwitch(enumObject, new Op04StructuredStatement(replacementBlock), structuredSwitch.getBlockIdentifier());
        structuredSwitch.getContainer().replaceContainedStatement(newSwitch);
        enumLutClass.markHiddenInnerClass();
    }

    private Integer getIntegerFromLiteralExpression(Expression exp) {
        Literal literal;
        TypedLiteral typedLiteral;
        if (!(exp instanceof Literal)) {
            return null;
        }
        if ((typedLiteral = (literal = (Literal)exp).getValue()).getType() == TypedLiteral.LiteralType.Integer) return (Integer)typedLiteral.getValue();
        return null;
    }

    class SwitchForeignEnumMatchResultCollector
    extends AbstractMatchResultIterator {
        private final WildcardMatch wcmOuter;
        private final WildcardMatch wcmCase;
        private boolean bad;
        private final Map<Integer, StaticVariable> lutValues;

        private SwitchForeignEnumMatchResultCollector(WildcardMatch wcmOuter, WildcardMatch wcmCase) {
            this.lutValues = MapFactory.newMap();
            this.wcmOuter = wcmOuter;
            this.wcmCase = wcmCase;
        }

        public Map<Integer, StaticVariable> getLUT() {
            return this.lutValues;
        }

        @Override
        public void clear() {
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
            Expression exp;
            Integer literalInt;
            if (wcm == this.wcmOuter) return;
            if (wcm != this.wcmCase) return;
            StaticVariable staticVariable = wcm.getStaticVariable("enumval").getMatch();
            if ((literalInt = this$0.getIntegerFromLiteralExpression(exp = wcm.getExpressionWildCard("literal").getMatch())) == null) {
                this.bad = true;
                return;
            }
            this.lutValues.put(literalInt, staticVariable);
        }

        /* synthetic */ SwitchForeignEnumMatchResultCollector(SwitchEnumRewriter x0, WildcardMatch x1, WildcardMatch x2,  x3) {
            this(x1, x2);
        }
    }

    static class SwitchEnumMatchResultCollector
    extends AbstractMatchResultIterator {
        private final WildcardMatch wcm;
        private LValue lookupTable;
        private Expression enumObject;
        private StructuredSwitch structuredSwitch;

        private SwitchEnumMatchResultCollector(WildcardMatch wcm) {
            this.wcm = wcm;
        }

        @Override
        public void clear() {
            this.lookupTable = null;
            this.enumObject = null;
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            if (!name.equals("switch")) return;
            this.structuredSwitch = (StructuredSwitch)statement;
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
            this.lookupTable = wcm.getLValueWildCard("lut").getMatch();
            this.enumObject = wcm.getExpressionWildCard("object").getMatch();
        }

        public LValue getLookupTable() {
            return this.lookupTable;
        }

        public Expression getEnumObject() {
            return this.enumObject;
        }

        public StructuredSwitch getStructuredSwitch() {
            return this.structuredSwitch;
        }

        /* synthetic */ SwitchEnumMatchResultCollector(WildcardMatch x0,  x1) {
            this(x0);
        }
    }

}

