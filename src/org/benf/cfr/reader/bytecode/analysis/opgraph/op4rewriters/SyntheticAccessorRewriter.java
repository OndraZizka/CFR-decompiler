/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.Op04Rewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.AbstractMatchResultIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchOneOf;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMutatingAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPostMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPreMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StackValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.AccessFlagMethod;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryMethodRef;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.MapFactory;

public class SyntheticAccessorRewriter
implements Op04Rewriter,
ExpressionRewriter {
    private final DCCommonState state;
    private final JavaTypeInstance thisClassType;
    private static final String RETURN_LVALUE = "returnlvalue";
    private static final String MUTATION1 = "mutation1";
    private static final String MUTATION2 = "mutation2";
    private static final String PRE_INC = "preinc";
    private static final String POST_INC = "postinc";
    private static final String PRE_DEC = "predec";
    private static final String POST_DEC = "postdec";
    private static final String FUNCCALL1 = "funccall1";
    private static final String FUNCCALL2 = "funccall2";
    private final String MEM_SUB1 = "msub1";
    private final String STA_SUB1 = "ssub1";
    private final String MEM_FUN1 = "mfun1";
    private final String STA_FUN1 = "sfun1";

    public SyntheticAccessorRewriter(DCCommonState state, JavaTypeInstance thisClassType) {
        this.state = state;
        this.thisClassType = thisClassType;
    }

    @Override
    public void rewrite(Op04StructuredStatement root) {
        List<StructuredStatement> structuredStatements = MiscStatementTools.linearise(root);
        if (structuredStatements == null) {
            return;
        }
        for (StructuredStatement statement : structuredStatements) {
            statement.rewriteExpressions(this);
        }
    }

    @Override
    public void handleStatement(StatementContainer statementContainer) {
    }

    @Override
    public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        expression = expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        if (!(expression instanceof StaticFunctionInvokation)) return expression;
        return this.rewriteFunctionExpression((StaticFunctionInvokation)expression);
    }

    @Override
    public ConditionalExpression rewriteExpression(ConditionalExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression res = expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
        return (ConditionalExpression)res;
    }

    @Override
    public AbstractAssignmentExpression rewriteExpression(AbstractAssignmentExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        Expression res = expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
        return (AbstractAssignmentExpression)res;
    }

    @Override
    public LValue rewriteExpression(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return lValue;
    }

    @Override
    public StackSSALabel rewriteExpression(StackSSALabel lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return lValue;
    }

    private Expression rewriteFunctionExpression(StaticFunctionInvokation functionInvokation) {
        Expression res = this.rewriteFunctionExpression2(functionInvokation);
        if (res != null) return res;
        return functionInvokation;
    }

    private Expression rewriteFunctionExpression2(StaticFunctionInvokation functionInvokation) {
        List<LocalVariable> methodArgs;
        Op04StructuredStatement otherCode;
        List<StructuredStatement> structuredStatements;
        Expression res;
        JavaTypeInstance tgtType = functionInvokation.getClazz();
        boolean child = this.thisClassType.getInnerClassHereInfo().isTransitiveInnerClassOf(tgtType);
        boolean parent = tgtType.getInnerClassHereInfo().isTransitiveInnerClassOf(this.thisClassType);
        if (!(child || parent)) {
            return null;
        }
        ClassFile otherClass = this.state.getClassFile(tgtType);
        JavaTypeInstance otherType = otherClass.getClassType();
        MethodPrototype otherPrototype = functionInvokation.getFunction().getMethodPrototype();
        List<Expression> appliedArgs = functionInvokation.getArgs();
        Method otherMethod = null;
        try {
            otherMethod = otherClass.getMethodByPrototype(otherPrototype);
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        if (!otherMethod.testAccessFlag(AccessFlagMethod.ACC_STATIC)) {
            return null;
        }
        if (!otherMethod.testAccessFlag(AccessFlagMethod.ACC_SYNTHETIC)) {
            return null;
        }
        if (!otherMethod.hasCodeAttribute()) {
            return null;
        }
        if ((otherCode = otherMethod.getAnalysis()) == null) {
            return null;
        }
        if ((res = this.tryRewriteAccessor(structuredStatements = MiscStatementTools.linearise(otherCode), otherType, appliedArgs, methodArgs = otherMethod.getMethodPrototype().getComputedParameters())) != null) {
            otherMethod.hideSynthetic();
            return res;
        }
        if ((res = this.tryRewriteFunctionCall(structuredStatements, otherType, appliedArgs, methodArgs)) == null) return null;
        otherMethod.hideSynthetic();
        return res;
    }

    private Expression tryRewriteAccessor(List<StructuredStatement> structuredStatements, JavaTypeInstance otherType, List<Expression> appliedArgs, List<LocalVariable> methodArgs) {
        boolean isStatic;
        StaticVariable staticVariable;
        WildcardMatch wcm = new WildcardMatch();
        MatchSequence matcher = new MatchSequence(new BeginBlock(null), (Matcher<StructuredStatement>)new MatchOneOf(new ResetAfterTest(wcm, "returnlvalue", new StructuredReturn(new LValueExpression(wcm.getLValueWildCard("lvalue")), null)), new ResetAfterTest(wcm, "mutation1", new MatchSequence(new StructuredAssignment(wcm.getLValueWildCard("lvalue"), wcm.getExpressionWildCard("rvalue")), (Matcher<StructuredStatement>)new StructuredReturn(new LValueExpression(wcm.getLValueWildCard("lvalue")), null))), new ResetAfterTest(wcm, "mutation2", new MatchSequence(new StructuredAssignment(wcm.getLValueWildCard("lvalue"), wcm.getExpressionWildCard("rvalue")), (Matcher<StructuredStatement>)new StructuredReturn(wcm.getExpressionWildCard("rvalue"), null))), new ResetAfterTest(wcm, "preinc", new StructuredReturn(new ArithmeticPreMutationOperation(wcm.getLValueWildCard("lvalue"), ArithOp.PLUS), null)), new ResetAfterTest(wcm, "predec", new StructuredReturn(new ArithmeticPreMutationOperation(wcm.getLValueWildCard("lvalue"), ArithOp.MINUS), null)), new ResetAfterTest(wcm, "postinc", new StructuredReturn(new ArithmeticPostMutationOperation(wcm.getLValueWildCard("lvalue"), ArithOp.PLUS), null)), new ResetAfterTest(wcm, "postinc", new MatchSequence(new StructuredExpressionStatement(new ArithmeticPostMutationOperation(wcm.getLValueWildCard("lvalue"), ArithOp.PLUS), false), (Matcher<StructuredStatement>)new StructuredReturn(new LValueExpression(wcm.getLValueWildCard("lvalue")), null))), new ResetAfterTest(wcm, "postinc", new MatchSequence(new StructuredAssignment(wcm.getStackLabelWildcard("tmp"), new LValueExpression(wcm.getLValueWildCard("lvalue"))), (Matcher<StructuredStatement>)new StructuredAssignment(wcm.getLValueWildCard("lvalue"), new ArithmeticOperation(new StackValue(wcm.getStackLabelWildcard("tmp")), new Literal(TypedLiteral.getInt(1)), ArithOp.PLUS)), (Matcher<StructuredStatement>)new StructuredReturn(new StackValue(wcm.getStackLabelWildcard("tmp")), null))), new ResetAfterTest(wcm, "postdec", new MatchSequence(new StructuredExpressionStatement(new ArithmeticPostMutationOperation(wcm.getLValueWildCard("lvalue"), ArithOp.MINUS), false), (Matcher<StructuredStatement>)new StructuredReturn(new LValueExpression(wcm.getLValueWildCard("lvalue")), null))), new ResetAfterTest(wcm, "postdec", new StructuredReturn(new ArithmeticPostMutationOperation(wcm.getLValueWildCard("lvalue"), ArithOp.MINUS), null))), (Matcher<StructuredStatement>)new EndBlock(null));
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(structuredStatements);
        AccessorMatchCollector accessorMatchCollector = new AccessorMatchCollector(null);
        mi.advance();
        if (!matcher.match(mi, accessorMatchCollector)) {
            return null;
        }
        if (accessorMatchCollector.matchType == null) {
            return null;
        }
        if ((isStatic = accessorMatchCollector.lValue instanceof StaticVariable) && !otherType.equals((staticVariable = (StaticVariable)accessorMatchCollector.lValue).getOwningClassTypeInstance())) {
            return null;
        }
        String matchType = accessorMatchCollector.matchType;
        Map lValueReplacements = MapFactory.newMap();
        Map expressionReplacements = MapFactory.newMap();
        for (int x = 0; x < methodArgs.size(); ++x) {
            Expression appliedArg;
            LocalVariable methodArg = methodArgs.get(x);
            if (appliedArg = appliedArgs.get(x) instanceof LValueExpression) {
                LValue appliedLvalue = ((LValueExpression)appliedArg).getLValue();
                lValueReplacements.put((LocalVariable)methodArg, (LValue)appliedLvalue);
            }
            expressionReplacements.put((LValueExpression)new LValueExpression(methodArg), (Expression)appliedArg);
        }
        CloneHelper cloneHelper = new CloneHelper(expressionReplacements, lValueReplacements);
        if (matchType.equals("mutation1") || matchType.equals("mutation2")) {
            AssignmentExpression assignmentExpression = new AssignmentExpression(accessorMatchCollector.lValue, accessorMatchCollector.rValue, true);
            return cloneHelper.replaceOrClone(assignmentExpression);
        }
        if (matchType.equals("returnlvalue")) {
            return cloneHelper.replaceOrClone(new LValueExpression(accessorMatchCollector.lValue));
        }
        if (matchType.equals("predec")) {
            res = new ArithmeticPreMutationOperation(accessorMatchCollector.lValue, ArithOp.MINUS);
            return cloneHelper.replaceOrClone(res);
        }
        if (matchType.equals("preinc")) {
            res = new ArithmeticPreMutationOperation(accessorMatchCollector.lValue, ArithOp.PLUS);
            return cloneHelper.replaceOrClone(res);
        }
        if (matchType.equals("postdec")) {
            res = new ArithmeticPostMutationOperation(accessorMatchCollector.lValue, ArithOp.MINUS);
            return cloneHelper.replaceOrClone(res);
        }
        if (!matchType.equals("postinc")) throw new IllegalStateException();
        AbstractMutatingAssignmentExpression res = new ArithmeticPostMutationOperation(accessorMatchCollector.lValue, ArithOp.PLUS);
        return cloneHelper.replaceOrClone(res);
    }

    private Expression tryRewriteFunctionCall(List<StructuredStatement> structuredStatements, JavaTypeInstance otherType, List<Expression> appliedArgs, List<LocalVariable> methodArgs) {
        WildcardMatch wcm = new WildcardMatch();
        MatchSequence matcher = new MatchSequence(new BeginBlock(null), (Matcher<StructuredStatement>)new MatchOneOf(new ResetAfterTest(wcm, "msub1", new StructuredExpressionStatement(wcm.getMemberFunction("func", (String)null, false, (Expression)new LValueExpression(wcm.getLValueWildCard("lvalue")), null), false)), new ResetAfterTest(wcm, "ssub1", new StructuredExpressionStatement(wcm.getStaticFunction("func", otherType, (String)null, (List)null), false)), new ResetAfterTest(wcm, "mfun1", new StructuredReturn(wcm.getMemberFunction("func", (String)null, false, (Expression)new LValueExpression(wcm.getLValueWildCard("lvalue")), null), null)), new ResetAfterTest(wcm, "sfun1", new StructuredReturn(wcm.getStaticFunction("func", otherType, (String)null, (List)null), null))), (Matcher<StructuredStatement>)new EndBlock(null));
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(structuredStatements);
        FuncMatchCollector funcMatchCollector = new FuncMatchCollector(null);
        mi.advance();
        if (!matcher.match(mi, funcMatchCollector)) {
            return null;
        }
        if (funcMatchCollector.matchType == null) {
            return null;
        }
        Map lValueReplacements = MapFactory.newMap();
        Map expressionReplacements = MapFactory.newMap();
        for (int x = 0; x < methodArgs.size(); ++x) {
            Expression appliedArg;
            LocalVariable methodArg = methodArgs.get(x);
            if (appliedArg = appliedArgs.get(x) instanceof LValueExpression) {
                LValue appliedLvalue = ((LValueExpression)appliedArg).getLValue();
                lValueReplacements.put((LocalVariable)methodArg, (LValue)appliedLvalue);
            }
            expressionReplacements.put((LValueExpression)new LValueExpression(methodArg), (Expression)appliedArg);
        }
        CloneHelper cloneHelper = new CloneHelper(expressionReplacements, lValueReplacements);
        return cloneHelper.replaceOrClone(funcMatchCollector.functionInvokation);
    }

    class 1 {
    }

    class FuncMatchCollector
    extends AbstractMatchResultIterator {
        String matchType;
        LValue lValue;
        StaticFunctionInvokation staticFunctionInvokation;
        MemberFunctionInvokation memberFunctionInvokation;
        Expression functionInvokation;
        private boolean isStatic;

        private FuncMatchCollector() {
        }

        @Override
        public void clear() {
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
            this.matchType = name;
            if (this.matchType.equals("sfun1") || this.matchType.endsWith("ssub1")) {
                this.functionInvokation = this.staticFunctionInvokation = wcm.getStaticFunction("func").getMatch();
                this.isStatic = true;
            } else {
                this.functionInvokation = this.memberFunctionInvokation = wcm.getMemberFunction("func").getMatch();
                this.lValue = wcm.getLValueWildCard("lvalue").getMatch();
                this.isStatic = false;
            }
        }

        /* synthetic */ FuncMatchCollector(SyntheticAccessorRewriter x0, 1 x1) {
            this();
        }
    }

    class AccessorMatchCollector
    extends AbstractMatchResultIterator {
        String matchType;
        LValue lValue;
        Expression rValue;

        private AccessorMatchCollector() {
        }

        @Override
        public void clear() {
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
            this.matchType = name;
            this.lValue = wcm.getLValueWildCard("lvalue").getMatch();
            if (!this.matchType.equals("mutation1") && !this.matchType.equals("mutation2")) return;
            this.rValue = wcm.getExpressionWildCard("rvalue").getMatch();
        }

        /* synthetic */ AccessorMatchCollector(SyntheticAccessorRewriter x0, 1 x1) {
            this();
        }
    }

}

