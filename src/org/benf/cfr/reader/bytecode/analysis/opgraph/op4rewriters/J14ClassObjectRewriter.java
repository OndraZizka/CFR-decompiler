/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.ExpressionWildcardReplacingRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.ExpressionRewriterTransformer;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CompOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ComparisonOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.TernaryExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.QuotingUtils;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredThrow;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredTry;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.TypeConstants;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.AccessFlagMethod;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.NonaryFunction;

public class J14ClassObjectRewriter {
    private final ClassFile classFile;
    private final DCCommonState state;
    private static final JavaRefTypeInstance CLASSNOTFOUND_EXCEPTION = JavaRefTypeInstance.createTypeConstant("java.lang.ClassNotFoundException", "ClassNotFoundException", new JavaRefTypeInstance[0]);
    private static final JavaRefTypeInstance NOCLASSDEFFOUND_ERROR = JavaRefTypeInstance.createTypeConstant("java.lang.NoClassDefFoundError", "NoClassDefFoundError", new JavaRefTypeInstance[0]);
    private static final JavaRefTypeInstance CLASS = JavaRefTypeInstance.createTypeConstant("java.lang.Class", "Class", new JavaRefTypeInstance[0]);

    public J14ClassObjectRewriter(ClassFile classFile, DCCommonState state) {
        this.classFile = classFile;
        this.state = state;
    }

    public void rewrite() {
        Method method = this.classFile.getSingleMethodByNameOrNull("class$");
        JavaTypeInstance classType = this.classFile.getClassType();
        if (!this.methodIsClassLookup(method)) {
            return;
        }
        method.hideSynthetic();
        WildcardMatch wcm = new WildcardMatch();
        WildcardMatch.StaticVariableWildcard staticVariable = wcm.getStaticVariable("classVar", classType, new InferredJavaType(J14ClassObjectRewriter.CLASS, InferredJavaType.Source.TEST));
        LValueExpression staticExpression = new LValueExpression(staticVariable);
        TernaryExpression test = new TernaryExpression(new ComparisonOperation(staticExpression, Literal.NULL, CompOp.EQ), new AssignmentExpression(staticVariable, StaticFunctionInvokation.createMatcher(classType, new InferredJavaType(classType, InferredJavaType.Source.TEST), ListFactory.newList(wcm.getExpressionWildCard("classString"))), true), staticExpression);
        Set hideThese = SetFactory.newSet();
        ExpressionWildcardReplacingRewriter expressionRewriter = new ExpressionWildcardReplacingRewriter(wcm, test, new NonaryFunction<Expression>(){

            @Override
            public Expression invoke() {
                TypedLiteral literal;
                Expression string = wcm.getExpressionWildCard("classString").getMatch();
                if (!(string instanceof Literal)) {
                    return null;
                }
                if ((literal = ((Literal)string).getValue()).getType() != TypedLiteral.LiteralType.String) {
                    return null;
                }
                Literal res = new Literal(TypedLiteral.getClass(J14ClassObjectRewriter.this.state.getClassCache().getRefClassFor(QuotingUtils.unquoteString((String)literal.getValue()))));
                String hideThis = staticVariable.getMatch().getVarName();
                hideThese.add(hideThis);
                return res;
            }
        });
        ExpressionRewriterTransformer transformer = new ExpressionRewriterTransformer(expressionRewriter);
        for (ClassFileField field : this.classFile.getFields()) {
            Expression initialValue = field.getInitialValue();
            field.setInitialValue(expressionRewriter.rewriteExpression(initialValue, (SSAIdentifiers)null, (StatementContainer)null, ExpressionRewriterFlags.RVALUE));
        }
        Iterator<ClassFileField> i$ = this.classFile.getMethods().iterator();
        while (i$.hasNext()) {
            Method testMethod;
            if (!(testMethod = (Method)i$.next()).hasCodeAttribute()) continue;
            testMethod.getAnalysis().transform(transformer, new StructuredScope());
        }
        for (String hideThis : hideThese) {
            try {
                ClassFileField fileField = this.classFile.getFieldByName(hideThis);
                fileField.markHidden();
            }
            catch (NoSuchFieldException e) {}
        }
    }

    private boolean methodIsClassLookup(Method method) {
        List<LocalVariable> args;
        List<StructuredStatement> statements;
        LocalVariable arg;
        if (method == null) {
            return false;
        }
        if (!method.getAccessFlags().contains((Object)AccessFlagMethod.ACC_SYNTHETIC)) {
            return false;
        }
        if (!method.hasCodeAttribute()) {
            return false;
        }
        if ((statements = MiscStatementTools.linearise(method.getAnalysis())) == null) {
            return false;
        }
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(statements);
        WildcardMatch wcm1 = new WildcardMatch();
        if ((args = method.getMethodPrototype().getComputedParameters()).size() != 1) {
            return false;
        }
        if (!TypeConstants.STRING.equals((arg = args.get(0)).getInferredJavaType().getJavaTypeInstance())) {
            return false;
        }
        MatchSequence m = new MatchSequence(new BeginBlock(null), (Matcher<StructuredStatement>)new StructuredTry(null, null, null), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new StructuredReturn(wcm1.getStaticFunction("forName", (JavaTypeInstance)J14ClassObjectRewriter.CLASS, "forName", new LValueExpression(arg)), J14ClassObjectRewriter.CLASS), (Matcher<StructuredStatement>)new EndBlock(null), (Matcher<StructuredStatement>)new StructuredCatch(null, null, null, null), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new StructuredThrow(wcm1.getMemberFunction("initCause", "initCause", wcm1.getConstructorSimpleWildcard("nocd", J14ClassObjectRewriter.NOCLASSDEFFOUND_ERROR), wcm1.getExpressionWildCard("throwable"))), (Matcher<StructuredStatement>)new EndBlock(null), (Matcher<StructuredStatement>)new EndBlock(null));
        mi.advance();
        return m.match(mi, null);
    }

}

