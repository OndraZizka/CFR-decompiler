/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LambdaExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.SentinelLocalClassLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.Keywords;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class VariableNameTidier
implements StructuredStatementTransformer {
    private final Method method;

    public VariableNameTidier(Method method) {
        this.method = method;
    }

    public void transform(Op04StructuredStatement root) {
        StructuredScopeWithVars structuredScopeWithVars = new StructuredScopeWithVars(null);
        structuredScopeWithVars.add(null);
        List<LocalVariable> params = this.method.getMethodPrototype().getComputedParameters();
        for (LocalVariable param : params) {
            structuredScopeWithVars.defineHere(null, param);
        }
        root.transform(this, structuredScopeWithVars);
    }

    @Override
    public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
        StructuredScopeWithVars structuredScopeWithVars = (StructuredScopeWithVars)scope;
        List<LValue> definedHere = in.findCreatedHere();
        if (definedHere != null) {
            for (LValue scopedEntity : definedHere) {
                if (scopedEntity instanceof LocalVariable) {
                    structuredScopeWithVars.defineHere(in, (LocalVariable)scopedEntity);
                }
                if (!(scopedEntity instanceof SentinelLocalClassLValue)) continue;
                structuredScopeWithVars.defineLocalClassHere(in, (SentinelLocalClassLValue)scopedEntity);
            }
        }
        ExpressionNameTidier expressionRewriter = new ExpressionNameTidier(structuredScopeWithVars, null);
        in.transformStructuredChildren(this, scope);
        return in;
    }

    class 1 {
    }

    class StructuredScopeWithVars
    extends StructuredScope {
        private final LinkedList<AtLevel> scope;
        private final Map<String, Integer> nextPostFixed;

        private StructuredScopeWithVars() {
            this.scope = ListFactory.newLinkedList();
            this.nextPostFixed = MapFactory.newLazyMap(new UnaryFunction<String, Integer>(){

                @Override
                public Integer invoke(String arg) {
                    return 2;
                }
            });
        }

        @Override
        public void remove(StructuredStatement statement) {
            super.remove(statement);
            this.scope.removeFirst();
        }

        @Override
        public void add(StructuredStatement statement) {
            super.add(statement);
            this.scope.addFirst(new AtLevel(statement, null));
        }

        private boolean alreadyDefined(String name) {
            Iterator i$ = this.scope.iterator();
            while (i$.hasNext()) {
                AtLevel atLevel;
                if (!(atLevel = (AtLevel)i$.next()).isDefinedHere(name)) continue;
                return true;
            }
            return false;
        }

        private String getNext(String base) {
            int postfix = this.nextPostFixed.get(base);
            this.nextPostFixed.put(base, postfix + 1);
            return base + postfix;
        }

        private String suggestByType(LocalVariable localVariable) {
            JavaTypeInstance type = localVariable.getInferredJavaType().getJavaTypeInstance();
            RawJavaType raw = RawJavaType.getUnboxedTypeFor(type);
            if (raw == null) return type.suggestVarName();
            type = raw;
            return type.suggestVarName();
        }

        private String mkLcMojo(String in) {
            return " class!" + in;
        }

        public void defineLocalClassHere(StructuredStatement statement, SentinelLocalClassLValue localVariable) {
            String postfixedVarName;
            JavaTypeInstance type = localVariable.getLocalClassType();
            String name = type.suggestVarName();
            if (name == null) {
                name = type.getRawName().replace('.', '_');
            }
            char[] chars = name.toCharArray();
            int len = chars.length;
            for (int idx = 0; idx < len; ++idx) {
                char c;
                if ((c = chars[idx]) >= '0' && c <= '9') continue;
                chars[idx] = Character.toUpperCase(chars[idx]);
                name = new String(chars, idx, chars.length - idx);
                break;
            }
            String lcMojo = this.mkLcMojo(name);
            if (!this.alreadyDefined(lcMojo)) {
                this.scope.getFirst().defineHere(lcMojo);
                this$0.method.markUsedLocalClassType(type, name);
                return;
            }
            do {
                postfixedVarName = this.getNext(name);
            } while (this.alreadyDefined(this.mkLcMojo(postfixedVarName)));
            this.scope.getFirst().defineHere(this.mkLcMojo(postfixedVarName));
            this$0.method.markUsedLocalClassType(type, postfixedVarName);
        }

        public void defineHere(StructuredStatement statement, LocalVariable localVariable) {
            NamedVariable namedVariable = localVariable.getName();
            if (!namedVariable.isGoodName()) {
                String suggestion = null;
                if (statement != null) {
                    suggestion = statement.suggestName(localVariable, new Predicate<String>(){

                        @Override
                        public boolean test(String in) {
                            return StructuredScopeWithVars.this.alreadyDefined(in);
                        }
                    });
                }
                if (suggestion == null) {
                    suggestion = this.suggestByType(localVariable);
                }
                if (suggestion != null) {
                    namedVariable.forceName(suggestion);
                }
            }
            if (Keywords.isAKeyword(namedVariable.getStringName())) {
                namedVariable.forceName(namedVariable.getStringName() + "_");
            }
            this.defineHere(localVariable);
        }

        public void defineHere(LocalVariable localVariable) {
            String postfixedVarName;
            NamedVariable namedVariable = localVariable.getName();
            String base = namedVariable.getStringName();
            if (!this.alreadyDefined(base)) {
                this.scope.getFirst().defineHere(base);
                return;
            }
            do {
                postfixedVarName = this.getNext(base);
            } while (this.alreadyDefined(postfixedVarName));
            localVariable.getName().forceName(postfixedVarName);
            this.scope.getFirst().defineHere(postfixedVarName);
        }

        /* synthetic */ StructuredScopeWithVars(VariableNameTidier x0, 1 x1) {
            this();
        }

        public class AtLevel {
            StructuredStatement statement;
            Set<String> definedHere;
            int next;

            private AtLevel(StructuredStatement statement) {
                this.definedHere = SetFactory.newSet();
                this.statement = statement;
                this.next = 0;
            }

            public String toString() {
                return this.statement.toString();
            }

            public boolean isDefinedHere(String name) {
                return this.definedHere.contains(name);
            }

            public void defineHere(String name) {
                this.definedHere.add(name);
            }

            /* synthetic */ AtLevel(StructuredScopeWithVars x0, StructuredStatement x1, 1 x2) {
                this(x1);
            }
        }

    }

    class ExpressionNameTidier
    implements ExpressionRewriter {
        private final StructuredScopeWithVars currentScope;

        private ExpressionNameTidier(StructuredScopeWithVars currentScope) {
            this.currentScope = currentScope;
        }

        @Override
        public Expression rewriteExpression(Expression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            if (!(expression instanceof LambdaExpression)) return expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
            this.currentScope.add(null);
            List<LValue> lValues = ((LambdaExpression)expression).getArgs();
            for (LValue lValue : lValues) {
                if (!(lValue instanceof LocalVariable)) continue;
                this.currentScope.defineHere((LocalVariable)lValue);
            }
            this.currentScope.remove(null);
            return expression.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        }

        @Override
        public ConditionalExpression rewriteExpression(ConditionalExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return (ConditionalExpression)expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
        }

        @Override
        public AbstractAssignmentExpression rewriteExpression(AbstractAssignmentExpression expression, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return (AbstractAssignmentExpression)expression.applyExpressionRewriter((ExpressionRewriter)this, ssaIdentifiers, statementContainer, flags);
        }

        @Override
        public LValue rewriteExpression(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return lValue.applyExpressionRewriter(this, ssaIdentifiers, statementContainer, flags);
        }

        @Override
        public StackSSALabel rewriteExpression(StackSSALabel lValue, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return lValue;
        }

        @Override
        public void handleStatement(StatementContainer statementContainer) {
        }

        /* synthetic */ ExpressionNameTidier(VariableNameTidier x0, StructuredScopeWithVars x1, 1 x2) {
            this(x1);
        }
    }

}

