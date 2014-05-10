/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.AbstractMatchResultIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.CollectMatch;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchOneOf;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BoolOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NotOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssert;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredThrow;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.entities.Method;

public class AssertRewriter {
    private final ClassFile classFile;
    private ClassFileField assertionsDisabledField = null;
    private StaticVariable assertionStatic = null;

    public AssertRewriter(ClassFile classFile) {
        this.classFile = classFile;
    }

    public void sugarAsserts(Method staticInit) {
        JavaRefTypeInstance nextClass;
        List<StructuredStatement> statements;
        if (!staticInit.hasCodeAttribute()) {
            return;
        }
        if ((statements = MiscStatementTools.linearise(staticInit.getAnalysis())) == null) {
            return;
        }
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(statements);
        WildcardMatch wcm1 = new WildcardMatch();
        JavaTypeInstance topClassType = this.classFile.getClassType();
        InnerClassInfo innerClassInfo = topClassType.getInnerClassHereInfo();
        JavaTypeInstance classType = topClassType;
        while (innerClassInfo != InnerClassInfo.NOT && (nextClass = innerClassInfo.getOuterClass()) != null) {
            if (nextClass.equals(classType)) break;
            classType = nextClass;
            innerClassInfo = classType.getInnerClassHereInfo();
        }
        ResetAfterTest m = new ResetAfterTest(wcm1, new CollectMatch("ass1", new StructuredAssignment(wcm1.getStaticVariable("assertbool", topClassType, new InferredJavaType(RawJavaType.BOOLEAN, InferredJavaType.Source.TEST)), new NotOperation(new BooleanExpression(wcm1.getMemberFunction("assertmeth", "desiredAssertionStatus", (Expression)new Literal(TypedLiteral.getClass(classType))))))));
        AssertVarCollector matchResultCollector = new AssertVarCollector(wcm1, null);
        while (mi.hasNext()) {
            mi.advance();
            matchResultCollector.clear();
            if (!m.match(mi, matchResultCollector) || !matchResultCollector.matched()) continue;
        }
        if (!matchResultCollector.matched()) {
            return;
        }
        this.assertionsDisabledField = matchResultCollector.assertField;
        this.assertionStatic = matchResultCollector.assertStatic;
        this.rewriteMethods();
    }

    private void rewriteMethods() {
        List<Method> methods = this.classFile.getMethods();
        WildcardMatch wcm1 = new WildcardMatch();
        ResetAfterTest m = new ResetAfterTest(wcm1, new MatchOneOf(new CollectMatch("ass1", new MatchSequence(new StructuredIf(new BooleanOperation(new NotOperation(new BooleanExpression(new LValueExpression(this.assertionStatic))), wcm1.getConditionalExpressionWildcard("condition"), BoolOp.AND), null), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new StructuredThrow(wcm1.getConstructorSimpleWildcard("exception", TypeConstants.ASSERTION_ERROR)), (Matcher<StructuredStatement>)new EndBlock(null))), new CollectMatch("ass1b", new MatchSequence(new StructuredIf(new NotOperation(new BooleanOperation(new BooleanExpression(new LValueExpression(this.assertionStatic)), wcm1.getConditionalExpressionWildcard("condition"), BoolOp.OR)), null), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new StructuredThrow(wcm1.getConstructorSimpleWildcard("exception", TypeConstants.ASSERTION_ERROR)), (Matcher<StructuredStatement>)new EndBlock(null))), new CollectMatch("ass2", new MatchSequence(new MatchOneOf(new StructuredIf(new BooleanOperation(new BooleanExpression(new LValueExpression(this.assertionStatic)), wcm1.getConditionalExpressionWildcard("condition2"), BoolOp.OR), null), new StructuredIf(new BooleanExpression(new LValueExpression(this.assertionStatic)), null)), (Matcher<StructuredStatement>)new BeginBlock(wcm1.getBlockWildcard("condBlock")), (Matcher<StructuredStatement>)new MatchOneOf(new StructuredReturn(null, null), new StructuredReturn(wcm1.getExpressionWildCard("retval"), null), new StructuredBreak(wcm1.getBlockIdentifier("breakblock"), false)), (Matcher<StructuredStatement>)new EndBlock(wcm1.getBlockWildcard("condBlock")), (Matcher<StructuredStatement>)new CollectMatch("ass2throw", new StructuredThrow(wcm1.getConstructorSimpleWildcard("exception2", TypeConstants.ASSERTION_ERROR)))))));
        AssertUseCollector collector = new AssertUseCollector(wcm1, null);
        for (Method method : methods) {
            List<StructuredStatement> statements;
            if (!method.hasCodeAttribute()) continue;
            if ((statements = MiscStatementTools.linearise(method.getAnalysis())) == null) continue;
            MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(statements);
            while (mi.hasNext()) {
                mi.advance();
                m.match(mi, collector);
            }
        }
    }

    class 1 {
    }

    class AssertUseCollector
    extends AbstractMatchResultIterator {
        private StructuredStatement ass2throw;
        private final WildcardMatch wcm;

        private AssertUseCollector(WildcardMatch wcm) {
            this.wcm = wcm;
        }

        @Override
        public void clear() {
            this.ass2throw = null;
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            if (name.equals("ass1") || name.equals("ass1b")) {
                StructuredIf ifStatement = (StructuredIf)statement;
                ConditionalExpression condition = this.wcm.getConditionalExpressionWildcard("condition").getMatch();
                if (name.equals("ass1")) {
                    condition = new NotOperation(condition);
                }
                condition = condition.simplify();
                StructuredStatement structuredAssert = ifStatement.convertToAssertion(new StructuredAssert(condition));
                ifStatement.getContainer().replaceContainedStatement(structuredAssert);
            } else if (name.equals("ass2")) {
                WildcardMatch.ConditionalExpressionWildcard wcard;
                ConditionalExpression conditionalExpression;
                if (this.ass2throw == null) {
                    throw new IllegalStateException();
                }
                StructuredIf ifStatement = (StructuredIf)statement;
                if ((conditionalExpression = (wcard = this.wcm.getConditionalExpressionWildcard("condition2")).getMatch()) == null) {
                    conditionalExpression = new BooleanExpression(new Literal(TypedLiteral.getBoolean(0)));
                }
                StructuredAssert structuredAssert = new StructuredAssert(conditionalExpression);
                ifStatement.getContainer().replaceContainedStatement(structuredAssert);
                this.ass2throw.getContainer().replaceContainedStatement(ifStatement.getIfTaken().getStatement());
            } else {
                if (!name.equals("ass2throw")) return;
                this.ass2throw = statement;
            }
        }

        /* synthetic */ AssertUseCollector(AssertRewriter x0, WildcardMatch x1, 1 x2) {
            this(x1);
        }
    }

    class AssertVarCollector
    extends AbstractMatchResultIterator {
        private final WildcardMatch wcm;
        ClassFileField assertField;
        StaticVariable assertStatic;

        private AssertVarCollector(WildcardMatch wcm) {
            this.assertField = null;
            this.assertStatic = null;
            this.wcm = wcm;
        }

        @Override
        public void clear() {
            this.assertField = null;
            this.assertStatic = null;
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            ClassFileField field;
            StaticVariable staticVariable = this.wcm.getStaticVariable("assertbool").getMatch();
            try {
                field = this$0.classFile.getFieldByName(staticVariable.getVarName());
            }
            catch (NoSuchFieldException e) {
                return;
            }
            if (!field.getField().testAccessFlag(AccessFlag.ACC_SYNTHETIC)) {
                return;
            }
            this.assertField = field;
            statement.getContainer().nopOut();
            this.assertField.markHidden();
            this.assertStatic = staticVariable;
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
        }

        public boolean matched() {
            return this.assertField != null;
        }

        /* synthetic */ AssertVarCollector(AssertRewriter x0, WildcardMatch x1, 1 x2) {
            this(x1);
        }
    }

}

