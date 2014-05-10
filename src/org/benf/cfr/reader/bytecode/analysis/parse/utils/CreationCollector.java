/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractConstructorInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnoynmousInner;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewObject;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StackValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class CreationCollector {
    private final List<Triple> collectedConstructions = ListFactory.newList();
    private final Map<LValue, List<StatementContainer>> collectedCreations;

    public CreationCollector() {
        this.collectedCreations = MapFactory.newLazyMap(new UnaryFunction<LValue, List<StatementContainer>>(){

            @Override
            public List<StatementContainer> invoke(LValue arg) {
                return ListFactory.newList();
            }
        });
    }

    public void collectCreation(LValue lValue, Expression rValue, StatementContainer container) {
        if (!(rValue instanceof NewObject)) {
            return;
        }
        if (!(lValue instanceof StackSSALabel || lValue instanceof LocalVariable)) {
            return;
        }
        this.collectedCreations.get(lValue).add(container);
    }

    public void collectConstruction(Expression expression, MemberFunctionInvokation rValue, StatementContainer container) {
        if (expression instanceof StackValue) {
            StackSSALabel lValue = ((StackValue)expression).getStackValue();
            this.markConstruction(lValue, rValue, container);
            return;
        }
        if (!(expression instanceof LValueExpression)) return;
        LValue lValue = ((LValueExpression)expression).getLValue();
        this.markConstruction(lValue, rValue, container);
    }

    private void markConstruction(LValue lValue, MemberFunctionInvokation rValue, StatementContainer container) {
        this.collectedConstructions.add(new Triple(lValue, null, new StatementPair(rValue, container, null), null));
    }

    public void condenseConstructions(Method method, DCCommonState dcCommonState) {
        LValue lValue;
        for (Triple construction : this.collectedConstructions) {
            InnerClassInfo innerClassInfo;
            StatementPair constructionValue;
            lValue = construction.getlValue();
            if ((constructionValue = construction.getConstruction()) == null) continue;
            InstrIndex idx = constructionValue.getLocation().getIndex();
            if (!this.collectedCreations.containsKey(lValue)) continue;
            List<StatementContainer> creations = this.collectedCreations.get(lValue);
            boolean found = false;
            for (StatementContainer creation : creations) {
                if (!creation.getIndex().isBackJumpFrom(idx)) continue;
                found = true;
            }
            if (!found) continue;
            MemberFunctionInvokation memberFunctionInvokation = (MemberFunctionInvokation)constructionValue.getValue();
            JavaTypeInstance lValueType = memberFunctionInvokation.getClassTypeInstance();
            InferredJavaType inferredJavaType = lValue.getInferredJavaType();
            AbstractConstructorInvokation constructorInvokation = null;
            if ((innerClassInfo = lValueType.getInnerClassHereInfo()).isMethodScopedClass() && !innerClassInfo.isAnonymousClass()) {
                method.markUsedLocalClassType(lValueType);
            }
            constructorInvokation = innerClassInfo.isAnonymousClass() ? new ConstructorInvokationAnoynmousInner(memberFunctionInvokation, inferredJavaType, memberFunctionInvokation.getArgs(), dcCommonState) : new ConstructorInvokationSimple(memberFunctionInvokation, inferredJavaType, memberFunctionInvokation.getArgs());
            AssignmentSimple replacement = new AssignmentSimple(lValue, constructorInvokation);
            if (lValue instanceof StackSSALabel) {
                StackSSALabel stackSSALabel = (StackSSALabel)lValue;
                StackEntry stackEntry = stackSSALabel.getStackEntry();
                stackEntry.decrementUsage();
                stackEntry.incSourceCount();
            }
            StatementContainer constructionContainer = constructionValue.getLocation();
            constructionContainer.replaceStatement(replacement);
        }
        for (Map.Entry creations : this.collectedCreations.entrySet()) {
            lValue = (LValue)creations.getKey();
            for (StatementContainer statementContainer : (List)creations.getValue()) {
                if (lValue instanceof StackSSALabel) {
                    StackEntry stackEntry = ((StackSSALabel)lValue).getStackEntry();
                    stackEntry.decSourceCount();
                }
                statementContainer.nopOut();
            }
        }
    }

    static class Triple {
        private final LValue lValue;
        private final StatementPair<NewObject> creation;
        private final StatementPair<MemberFunctionInvokation> construction;

        private Triple(LValue lValue, StatementPair<NewObject> creation, StatementPair<MemberFunctionInvokation> construction) {
            this.lValue = lValue;
            this.creation = creation;
            this.construction = construction;
        }

        private LValue getlValue() {
            return this.lValue;
        }

        private StatementPair<NewObject> getCreation() {
            return this.creation;
        }

        private StatementPair<MemberFunctionInvokation> getConstruction() {
            return this.construction;
        }

        /* synthetic */ Triple(LValue x0, StatementPair x1, StatementPair x2,  x3) {
            this(x0, x1, x2);
        }
    }

    static class StatementPair<X> {
        private final X value;
        private final StatementContainer location;

        private StatementPair(X value, StatementContainer location) {
            this.value = value;
            this.location = location;
        }

        private X getValue() {
            return this.value;
        }

        private StatementContainer getLocation() {
            return this.location;
        }

        /* synthetic */ StatementPair(Object x0, StatementContainer x1,  x2) {
            this(x0, x1);
        }
    }

}

