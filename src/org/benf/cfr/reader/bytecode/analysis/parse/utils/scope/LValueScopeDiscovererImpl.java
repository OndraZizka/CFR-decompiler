/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils.scope;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.SentinelLocalClassLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableFactory;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.output.Dumper;

public class LValueScopeDiscovererImpl
implements LValueScopeDiscoverer {
    private final Map<NamedVariable, ScopeDefinition> earliestDefinition = MapFactory.newIdentityMap();
    private final Map<Integer, Map<NamedVariable, Boolean>> earliestDefinitionsByLevel;
    private transient int currentDepth;
    private transient Stack<StatementContainer<StructuredStatement>> currentBlock;
    private final List<ScopeDefinition> discoveredCreations;
    private final VariableFactory variableFactory;

    public LValueScopeDiscovererImpl(MethodPrototype prototype, VariableFactory variableFactory) {
        this.earliestDefinitionsByLevel = MapFactory.newLazyMap(new UnaryFunction<Integer, Map<NamedVariable, Boolean>>(){

            @Override
            public Map<NamedVariable, Boolean> invoke(Integer arg) {
                return MapFactory.newIdentityMap();
            }
        });
        this.currentDepth = 0;
        this.currentBlock = new Stack<StatementContainer<StructuredStatement>>();
        this.discoveredCreations = ListFactory.newList();
        List<LocalVariable> parameters = prototype.getComputedParameters();
        this.variableFactory = variableFactory;
        for (LocalVariable parameter : parameters) {
            JavaTypeInstance type = parameter.getInferredJavaType().getJavaTypeInstance();
            ScopeDefinition prototypeScope = new ScopeDefinition(0, null, null, parameter, type, parameter.getName(), null);
            this.earliestDefinition.put(parameter.getName(), prototypeScope);
        }
    }

    @Override
    public void enterBlock(StructuredStatement structuredStatement) {
        Op04StructuredStatement container = structuredStatement.getContainer();
        this.currentBlock.push(container);
        ++this.currentDepth;
    }

    @Override
    public void leaveBlock(StructuredStatement structuredStatement) {
        for (NamedVariable definedHere : this.earliestDefinitionsByLevel.get(this.currentDepth).keySet()) {
            this.earliestDefinition.remove(definedHere);
        }
        this.earliestDefinitionsByLevel.remove(this.currentDepth);
        StatementContainer<StructuredStatement> oldContainer = this.currentBlock.pop();
        if (structuredStatement.getContainer() != oldContainer) {
            throw new IllegalStateException();
        }
        --this.currentDepth;
    }

    @Override
    public void collect(StackSSALabel lValue, StatementContainer<StructuredStatement> statementContainer, Expression value) {
    }

    @Override
    public void collectMultiUse(StackSSALabel lValue, StatementContainer<StructuredStatement> statementContainer, Expression value) {
    }

    @Override
    public void collectMutatedLValue(LValue lValue, StatementContainer<StructuredStatement> statementContainer, Expression value) {
    }

    @Override
    public void collectLocalVariableAssignment(LocalVariable localVariable, StatementContainer<StructuredStatement> statementContainer, Expression value) {
        JavaTypeInstance newType;
        JavaTypeInstance oldType;
        NamedVariable name = localVariable.getName();
        ScopeDefinition previousDef = this.earliestDefinition.get(name);
        if (previousDef == null) {
            JavaTypeInstance type = localVariable.getInferredJavaType().getJavaTypeInstance();
            ScopeDefinition scopeDefinition = new ScopeDefinition(this.currentDepth, this.currentBlock, statementContainer, localVariable, type, name, null);
            this.earliestDefinition.put(name, scopeDefinition);
            this.earliestDefinitionsByLevel.get(this.currentDepth).put(name, true);
            this.discoveredCreations.add(scopeDefinition);
            return;
        }
        if ((oldType = previousDef.getJavaTypeInstance()).equals(newType = localVariable.getInferredJavaType().getJavaTypeInstance())) return;
        this.earliestDefinitionsByLevel.get(previousDef.getDepth()).remove(previousDef.getName());
        if (previousDef.getDepth() == this.currentDepth) {
            this.variableFactory.mutatingRenameUnClash(localVariable);
            name = localVariable.getName();
        }
        JavaTypeInstance type = localVariable.getInferredJavaType().getJavaTypeInstance();
        ScopeDefinition scopeDefinition = new ScopeDefinition(this.currentDepth, this.currentBlock, statementContainer, localVariable, type, name, null);
        this.earliestDefinition.put(name, scopeDefinition);
        this.earliestDefinitionsByLevel.get(this.currentDepth).put(name, true);
        this.discoveredCreations.add(scopeDefinition);
    }

    public void markDiscoveredCreations() {
        Map definitionsByType = Functional.groupToMapBy(this.discoveredCreations, new UnaryFunction<ScopeDefinition, ScopeKey>(){

            @Override
            public ScopeKey invoke(ScopeDefinition arg) {
                return arg.getScopeKey();
            }
        });
        List finalDefinitions = ListFactory.newList();
        for (Map.Entry entry : definitionsByType.entrySet()) {
            StatementContainer<StructuredStatement> creationContainer;
            LValue scopedEntity;
            block17 : {
                ScopeKey scopeKey = (ScopeKey)entry.getKey();
                List<ScopeDefinition> definitions = entry.getValue();
                List<StatementContainer<StructuredStatement>> commonScope = null;
                ScopeDefinition bestDefn = null;
                scopedEntity = scopeKey.getlValue();
                Iterator<ScopeDefinition> i$ = definitions.iterator();
                while (i$.hasNext()) {
                    StructuredStatement statement;
                    ScopeDefinition definition;
                    List<StatementContainer<StructuredStatement>> scopeList;
                    if ((statement = (definition = i$.next()).getStatementContainer().getStatement()).alwaysDefines(scopedEntity)) {
                        statement.markCreator(scopedEntity);
                        continue;
                    }
                    if ((scopeList = definition.getNestedScope()) == null) {
                        commonScope = null;
                        bestDefn = definition;
                        break;
                    }
                    if (commonScope == null) {
                        commonScope = scopeList;
                        bestDefn = definition;
                        continue;
                    }
                    commonScope = LValueScopeDiscovererImpl.getCommonPrefix(commonScope, scopeList);
                    if (commonScope.size() == scopeList.size()) {
                        bestDefn = definition;
                        continue;
                    }
                    bestDefn = null;
                }
                creationContainer = null;
                if (scopedEntity instanceof SentinelLocalClassLValue) {
                    List<StatementContainer<StructuredStatement>> scope = null;
                    if (bestDefn != null) {
                        scope = bestDefn.getNestedScope();
                    } else if (commonScope != null) {
                        scope = commonScope;
                    }
                    if (scope != null) {
                        for (int i = scope.size() - 1; i >= 0; --i) {
                            StatementContainer<StructuredStatement> thisItem;
                            if (!((thisItem = scope.get(i)).getStatement() instanceof Block)) continue;
                            Block block = (Block)thisItem.getStatement();
                            block.setIndenting(true);
                            creationContainer = thisItem;
                            break block17;
                        }
                    }
                } else if (bestDefn != null) {
                    creationContainer = bestDefn.getStatementContainer();
                } else if (commonScope != null) {
                    creationContainer = (StatementContainer<StructuredStatement>)commonScope.get(commonScope.size() - 1);
                }
            }
            if (creationContainer == null) continue;
            creationContainer.getStatement().markCreator(scopedEntity);
        }
    }

    private static <T> List<T> getCommonPrefix(List<T> a, List<T> b) {
        List<T> la;
        List<T> lb;
        if (a.size() < b.size()) {
            la = a;
            lb = b;
        } else {
            la = b;
            lb = a;
        }
        int maxRes = Math.min(la.size(), lb.size());
        int sameLen = 0;
        for (int x = 0; x < maxRes; ++x) {
            if (!la.get(x).equals(lb.get(x))) break;
            ++sameLen;
        }
        if (sameLen != la.size()) return la.subList(0, sameLen);
        return la;
    }

    @Override
    public void collect(LValue lValue) {
        Class lValueClass = lValue.getClass();
        if (lValueClass == LocalVariable.class) {
            ScopeDefinition previousDef;
            NamedVariable name;
            LocalVariable localVariable;
            if ((name = (localVariable = (LocalVariable)lValue).getName()).getStringName().equals("this")) {
                return;
            }
            if ((previousDef = this.earliestDefinition.get(name)) != null) {
                return;
            }
            JavaTypeInstance type = lValue.getInferredJavaType().getJavaTypeInstance();
            ScopeDefinition scopeDefinition = new ScopeDefinition(this.currentDepth, this.currentBlock, this.currentBlock.peek(), lValue, type, name, null);
            this.earliestDefinition.put(name, scopeDefinition);
            this.earliestDefinitionsByLevel.get(this.currentDepth).put(name, true);
            this.discoveredCreations.add(scopeDefinition);
        } else {
            ScopeDefinition previousDef;
            SentinelLocalClassLValue localClassLValue;
            SentinelNV name;
            if (lValueClass != SentinelLocalClassLValue.class) return;
            if ((previousDef = this.earliestDefinition.get(name = new SentinelNV((localClassLValue = (SentinelLocalClassLValue)lValue).getLocalClassType(), null))) != null) {
                return;
            }
            JavaTypeInstance type = localClassLValue.getLocalClassType();
            ScopeDefinition scopeDefinition = new ScopeDefinition(this.currentDepth, this.currentBlock, this.currentBlock.peek(), lValue, type, name, null);
            this.earliestDefinition.put(name, scopeDefinition);
            this.earliestDefinitionsByLevel.get(this.currentDepth).put(name, true);
            this.discoveredCreations.add(scopeDefinition);
        }
    }

    static class ScopeDefinition {
        private final int depth;
        private final List<StatementContainer<StructuredStatement>> nestedScope;
        private final StatementContainer<StructuredStatement> exactStatement;
        private final LValue lValue;
        private final JavaTypeInstance lValueType;
        private final NamedVariable name;
        private final ScopeKey scopeKey;

        private ScopeDefinition(int depth, Stack<StatementContainer<StructuredStatement>> nestedScope, StatementContainer<StructuredStatement> exactStatement, LValue lValue, JavaTypeInstance type, NamedVariable name) {
            this.depth = depth;
            this.nestedScope = nestedScope == null ? null : ListFactory.newList(nestedScope);
            if (exactStatement == null && depth > 1) {
                boolean x = true;
            }
            this.exactStatement = exactStatement;
            this.lValue = lValue;
            this.lValueType = type;
            this.name = name;
            this.scopeKey = new ScopeKey(lValue, type, null);
        }

        public JavaTypeInstance getJavaTypeInstance() {
            return this.lValueType;
        }

        public StatementContainer<StructuredStatement> getStatementContainer() {
            return this.exactStatement;
        }

        public LValue getlValue() {
            return this.lValue;
        }

        public int getDepth() {
            return this.depth;
        }

        public NamedVariable getName() {
            return this.name;
        }

        public ScopeKey getScopeKey() {
            return this.scopeKey;
        }

        public List<StatementContainer<StructuredStatement>> getNestedScope() {
            return this.nestedScope;
        }

        /* synthetic */ ScopeDefinition(int x0, Stack x1, StatementContainer x2, LValue x3, JavaTypeInstance x4, NamedVariable x5,  x6) {
            this(x0, x1, x2, x3, x4, x5);
        }
    }

    static class SentinelNV
    implements NamedVariable {
        private final JavaTypeInstance typeInstance;

        private SentinelNV(JavaTypeInstance typeInstance) {
            this.typeInstance = typeInstance;
        }

        @Override
        public void forceName(String name) {
        }

        @Override
        public String getStringName() {
            return this.typeInstance.getRawName();
        }

        @Override
        public boolean isGoodName() {
            return true;
        }

        @Override
        public Dumper dump(Dumper d) {
            return null;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            SentinelNV that = (SentinelNV)o;
            if (!(this.typeInstance != null ? !this.typeInstance.equals(that.typeInstance) : that.typeInstance != null)) return true;
            return false;
        }

        public int hashCode() {
            return this.typeInstance != null ? this.typeInstance.hashCode() : 0;
        }

        /* synthetic */ SentinelNV(JavaTypeInstance x0,  x1) {
            this(x0);
        }
    }

    static class ScopeKey {
        private final LValue lValue;
        private final JavaTypeInstance type;

        private ScopeKey(LValue lValue, JavaTypeInstance type) {
            this.lValue = lValue;
            this.type = type;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            ScopeKey scopeKey = (ScopeKey)o;
            if (!this.lValue.equals(scopeKey.lValue)) {
                return false;
            }
            if (this.type.equals(scopeKey.type)) return true;
            return false;
        }

        private LValue getlValue() {
            return this.lValue;
        }

        public int hashCode() {
            int result = this.lValue.hashCode();
            result = 31 * result + this.type.hashCode();
            return result;
        }

        /* synthetic */ ScopeKey(LValue x0, JavaTypeInstance x1,  x2) {
            this(x0, x1);
        }
    }

}

