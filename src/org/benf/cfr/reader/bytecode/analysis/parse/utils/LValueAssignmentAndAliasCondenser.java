/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StackValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.ArrayVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdent;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.output.LoggerFactory;

public class LValueAssignmentAndAliasCondenser
implements LValueRewriter<Statement>,
LValueAssignmentCollector<Statement> {
    private static final Logger logger = LoggerFactory.create(LValueAssignmentAndAliasCondenser.class);
    private final Map<StackSSALabel, ExpressionStatement> found = MapFactory.newMap();
    private final Map<StackSSALabel, Expression> aliasReplacements = MapFactory.newMap();
    private final Map<StackSSALabel, ExpressionStatement> multiFound = MapFactory.newMap();
    private final Map<VersionedLValue, ExpressionStatement> mutableFound = MapFactory.newMap();
    Map<Expression, Expression> cache = MapFactory.newMap();

    @Override
    public void collect(StackSSALabel lValue, StatementContainer<Statement> statementContainer, Expression value) {
        this.found.put(lValue, new ExpressionStatement(value, statementContainer, null));
    }

    @Override
    public void collectMultiUse(StackSSALabel lValue, StatementContainer<Statement> statementContainer, Expression value) {
        this.multiFound.put(lValue, new ExpressionStatement(value, statementContainer, null));
    }

    @Override
    public void collectMutatedLValue(LValue lValue, StatementContainer<Statement> statementContainer, Expression value) {
        SSAIdent version = statementContainer.getSSAIdentifiers().getSSAIdent(lValue);
        if (null == this.mutableFound.put(new VersionedLValue(lValue, version, null), new ExpressionStatement(value, statementContainer, null))) return;
        throw new ConfusedCFRException("Duplicate versioned SSA Ident.");
    }

    @Override
    public void collectLocalVariableAssignment(LocalVariable localVariable, StatementContainer<Statement> statementContainer, Expression value) {
    }

    @Override
    public Expression getLValueReplacement(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer<Statement> lvSc) {
        ExpressionStatement pair;
        StackSSALabel stackSSALabel;
        LValue resLValue;
        StatementContainer statementContainer;
        if (!(lValue instanceof StackSSALabel)) {
            return null;
        }
        if (!this.found.containsKey(stackSSALabel = (StackSSALabel)lValue)) {
            return null;
        }
        SSAIdentifiers replacementIdentifiers = (statementContainer = (pair = this.found.get(stackSSALabel)).statementContainer) == null ? null : statementContainer.getSSAIdentifiers();
        Expression res = pair.expression;
        Expression prev = null;
        if (res instanceof LValueExpression && replacementIdentifiers != null && !ssaIdentifiers.isValidReplacement(resLValue = ((LValueExpression)res).getLValue(), replacementIdentifiers)) {
            Statement lvStm;
            if (!(lvStm = lvSc.getStatement() instanceof AssignmentSimple) || !lvStm.getCreatedLValue().equals(resLValue)) return null;
            Op03SimpleStatement lv03 = (Op03SimpleStatement)lvSc;
            for (Op03SimpleStatement source : lv03.getSources()) {
                if (source.getSSAIdentifiers().isValidReplacement(resLValue, replacementIdentifiers)) continue;
                return null;
            }
        }
        if (statementContainer != null) {
            lvSc.copyBlockInformationFrom(statementContainer);
            statementContainer.nopOut();
        }
        stackSSALabel.getStackEntry().decrementUsage();
        if (this.aliasReplacements.containsKey(stackSSALabel)) {
            this.found.put(stackSSALabel, new ExpressionStatement(this.aliasReplacements.get(stackSSALabel), null, null));
            this.aliasReplacements.remove(stackSSALabel);
        }
        do {
            prev = res;
            if (!this.cache.containsKey(res)) continue;
            prev = res = this.cache.get(res);
        } while ((res = res.replaceSingleUsageLValues(this, ssaIdentifiers, lvSc)) != null && res != prev);
        this.cache.put(new StackValue(stackSSALabel), prev);
        return prev;
    }

    @Override
    public boolean explicitlyReplaceThisLValue(LValue lValue) {
        return false;
    }

    public AliasRewriter getAliasRewriter() {
        return new AliasRewriter();
    }

    public MutationRewriterFirstPass getMutationRewriterFirstPass() {
        if (!this.mutableFound.isEmpty()) return new MutationRewriterFirstPass();
        return null;
    }

    class 1 {
    }

    static final class VersionedLValue {
        private final LValue lValue;
        private final SSAIdent ssaIdent;

        private VersionedLValue(LValue lValue, SSAIdent ssaIdent) {
            this.lValue = lValue;
            this.ssaIdent = ssaIdent;
        }

        public int hashCode() {
            return this.lValue.hashCode() + 31 * this.ssaIdent.hashCode();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof VersionedLValue)) {
                return false;
            }
            VersionedLValue other = (VersionedLValue)o;
            return this.lValue.equals(other.lValue) && this.ssaIdent.equals(other.ssaIdent);
        }

        /* synthetic */ VersionedLValue(LValue x0, SSAIdent x1, 1 x2) {
            this(x0, x1);
        }
    }

    static class LValueStatementContainer {
        private final LValue lValue;
        private final StatementContainer statementContainer;

        private LValueStatementContainer(LValue lValue, StatementContainer statementContainer) {
            this.lValue = lValue;
            this.statementContainer = statementContainer;
        }

        /* synthetic */ LValueStatementContainer(LValue x0, StatementContainer x1, 1 x2) {
            this(x0, x1);
        }

        static /* synthetic */ LValue access$600(LValueStatementContainer x0) {
            return x0.lValue;
        }

        static /* synthetic */ StatementContainer access$700(LValueStatementContainer x0) {
            return x0.statementContainer;
        }
    }

    public class MutationRewriterSecondPass
    implements LValueRewriter<Statement> {
        private final Map<VersionedLValue, StatementContainer> mutableReplacable;

        private MutationRewriterSecondPass(Map<VersionedLValue, StatementContainer> mutableReplacable) {
            this.mutableReplacable = mutableReplacable;
        }

        @Override
        public Expression getLValueReplacement(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer<Statement> statementContainer) {
            VersionedLValue versionedLValue;
            StatementContainer canReplaceIn;
            ExpressionStatement replaceWith;
            SSAIdent ssaIdent = ssaIdentifiers.getSSAIdent(lValue);
            if (ssaIdent == null || (canReplaceIn = this.mutableReplacable.get(versionedLValue = new VersionedLValue(lValue, ssaIdent, null))) != statementContainer) return null;
            if (ExpressionStatement.access$200(replaceWith = (ExpressionStatement)this$0.mutableFound.get(versionedLValue)) == statementContainer) {
                return null;
            }
            this.mutableReplacable.remove(versionedLValue);
            ExpressionStatement.access$200(replaceWith).nopOut();
            return ExpressionStatement.access$300(replaceWith);
        }

        @Override
        public boolean explicitlyReplaceThisLValue(LValue lValue) {
            return true;
        }

        /* synthetic */ MutationRewriterSecondPass(LValueAssignmentAndAliasCondenser x0, Map x1, 1 x2) {
            this(x1);
        }
    }

    public class MutationRewriterFirstPass
    implements LValueRewriter<Statement> {
        private final Map<VersionedLValue, Set<StatementContainer>> mutableUseFound;

        public MutationRewriterFirstPass() {
            this.mutableUseFound = MapFactory.newLazyMap(new UnaryFunction<VersionedLValue, Set<StatementContainer>>(){

                @Override
                public Set<StatementContainer> invoke(VersionedLValue arg) {
                    return SetFactory.newSet();
                }
            });
        }

        @Override
        public Expression getLValueReplacement(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer<Statement> statementContainer) {
            VersionedLValue versionedLValue;
            SSAIdent ssaIdent = ssaIdentifiers.getSSAIdent(lValue);
            if (ssaIdent == null || !LValueAssignmentAndAliasCondenser.this.mutableFound.containsKey(versionedLValue = new VersionedLValue(lValue, ssaIdent, null))) return null;
            this.mutableUseFound.get(versionedLValue).add(statementContainer);
            return null;
        }

        @Override
        public boolean explicitlyReplaceThisLValue(LValue lValue) {
            return true;
        }

        private StatementContainer getUniqueParent(StatementContainer start, Set<StatementContainer> seen) {
            Op03SimpleStatement o3current = (Op03SimpleStatement)start;
            while (!seen.contains(o3current)) {
                List<Op03SimpleStatement> targets;
                if ((targets = o3current.getTargets()).size() != 1) {
                    return null;
                }
                o3current = targets.get(0);
            }
            return o3current;
        }

        public MutationRewriterSecondPass getSecondPassRewriter() {
            Map replacableUses = MapFactory.newMap();
            for (Map.Entry<VersionedLValue, Set<StatementContainer>> entry : this.mutableUseFound.entrySet()) {
                StatementContainer uniqueParent;
                ExpressionStatement definition = (ExpressionStatement)LValueAssignmentAndAliasCondenser.this.mutableFound.get(entry.getKey());
                if ((uniqueParent = this.getUniqueParent(ExpressionStatement.access$200(definition), entry.getValue())) == null) continue;
                replacableUses.put((VersionedLValue)entry.getKey(), (StatementContainer)uniqueParent);
            }
            if (!replacableUses.isEmpty()) return new MutationRewriterSecondPass(replacableUses, null);
            return null;
        }

    }

    public class AliasRewriter
    implements LValueRewriter<Statement> {
        private final Map<StackSSALabel, List<StatementContainer>> usages;
        private final Map<StackSSALabel, List<LValueStatementContainer>> possibleAliases;

        public AliasRewriter() {
            this.usages = MapFactory.newLazyMap(new UnaryFunction<StackSSALabel, List<StatementContainer>>(){

                @Override
                public List<StatementContainer> invoke(StackSSALabel ignore) {
                    return ListFactory.newList();
                }
            });
            this.possibleAliases = MapFactory.newLazyMap(new UnaryFunction<StackSSALabel, List<LValueStatementContainer>>(){

                @Override
                public List<LValueStatementContainer> invoke(StackSSALabel ignore) {
                    return ListFactory.newList();
                }
            });
        }

        @Override
        public Expression getLValueReplacement(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer<Statement> statementContainer) {
            Expression rhs;
            AssignmentSimple assignmentSimple;
            if (!(lValue instanceof StackSSALabel)) {
                return null;
            }
            StackSSALabel stackSSALabel = (StackSSALabel)lValue;
            if (!LValueAssignmentAndAliasCondenser.this.multiFound.containsKey(lValue)) {
                return null;
            }
            if (statementContainer.getStatement() instanceof AssignmentSimple && rhs = (assignmentSimple = (AssignmentSimple)statementContainer.getStatement()).getRValue() instanceof StackValue && ((StackValue)rhs).getStackValue().equals(stackSSALabel)) {
                this.possibleAliases.get(stackSSALabel).add(new LValueStatementContainer(assignmentSimple.getCreatedLValue(), statementContainer, null));
            }
            this.usages.get(stackSSALabel).add(statementContainer);
            return null;
        }

        private LValue getAlias(StackSSALabel stackSSALabel) {
            List<LValueStatementContainer> possibleAliasList = this.possibleAliases.get(stackSSALabel);
            if (possibleAliasList.isEmpty()) {
                return null;
            }
            LValue guessAlias = null;
            StatementContainer guessStatement = null;
            for (LValueStatementContainer lValueStatementContainer : possibleAliasList) {
                if (LValueStatementContainer.access$600(lValueStatementContainer) instanceof StackSSALabel) continue;
                guessAlias = LValueStatementContainer.access$600(lValueStatementContainer);
                guessStatement = LValueStatementContainer.access$700(lValueStatementContainer);
            }
            if (guessAlias == null) {
                return null;
            }
            LValue returnGuessAlias = guessAlias;
            List checkThese = ListFactory.newList();
            if (guessAlias instanceof ArrayVariable) {
                Expression array;
                ArrayIndex arrayIndex;
                ArrayVariable arrayVariable;
                if (!(array = (arrayIndex = (arrayVariable = (ArrayVariable)guessAlias).getArrayIndex()).getArray() instanceof LValueExpression)) {
                    return null;
                }
                LValueExpression lValueArrayIndex = (LValueExpression)array;
                checkThese.add((LValue)lValueArrayIndex.getLValue());
                Expression index = arrayIndex.getIndex();
                if (index instanceof LValueExpression) {
                    checkThese.add((LValue)((LValueExpression)index).getLValue());
                } else if (!(index instanceof Literal)) {
                    return null;
                }
            } else {
                checkThese.add((LValue)guessAlias);
            }
            for (StatementContainer verifyStatement : this.usages.get(stackSSALabel)) {
                Iterator i$ = checkThese.iterator();
                while (i$.hasNext()) {
                    LValue checkThis;
                    if (guessStatement.getSSAIdentifiers().isValidReplacement(checkThis = (LValue)i$.next(), verifyStatement.getSSAIdentifiers())) continue;
                    return null;
                }
            }
            return returnGuessAlias;
        }

        public void inferAliases() {
            Iterator i$ = LValueAssignmentAndAliasCondenser.this.multiFound.entrySet().iterator();
            while (i$.hasNext()) {
                LValue alias;
                StackSSALabel stackSSALabel;
                Map.Entry multi;
                if ((alias = this.getAlias(stackSSALabel = (StackSSALabel)(multi = i$.next()).getKey())) == null) continue;
                LValueAssignmentAndAliasCondenser.this.found.put(stackSSALabel, multi.getValue());
                LValueAssignmentAndAliasCondenser.this.aliasReplacements.put(stackSSALabel, new LValueExpression(alias));
            }
        }

        @Override
        public boolean explicitlyReplaceThisLValue(LValue lValue) {
            return false;
        }

    }

    static class ExpressionStatement {
        private final Expression expression;
        private final StatementContainer<Statement> statementContainer;

        private ExpressionStatement(Expression expression, StatementContainer<Statement> statementContainer) {
            this.expression = expression;
            this.statementContainer = statementContainer;
        }

        /* synthetic */ ExpressionStatement(Expression x0, StatementContainer x1, 1 x2) {
            this(x0, x1);
        }
    }

}

