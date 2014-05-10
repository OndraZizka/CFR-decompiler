/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.benf.cfr.reader.bytecode.BytecodeMeta;
import org.benf.cfr.reader.bytecode.analysis.opgraph.GraphConversionHelper;
import org.benf.cfr.reader.bytecode.analysis.opgraph.IndexedStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.opgraph.MutableGraph;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op02WithProcessedDataAndRefs;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.ExpressionReplacingRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.NOPSearchingExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.Statement;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractMutatingAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPostMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayLength;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CompOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ComparisonOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NewAnonymousArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StackValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.TernaryExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.ArrayVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.AccountingRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.StackVarToLocalRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AbstractAssignment;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AnonBreakTarget;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentPreMutation;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CaseStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CatchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.CommentStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.DoStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ExpressionStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.FinallyStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ForIterStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ForStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.GotoStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.IfExitingStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.IfStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.JSRRetStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.JumpingStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.MonitorEnterStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.MonitorExitStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.MonitorStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.Nop;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.RawSwitchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ReturnNothingStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ReturnStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ReturnValueStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.SwitchStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.ThrowStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.TryStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.WhileStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.CreationCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.JumpType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentAndAliasCondenser;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollectorSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdent;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierUtils;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp.FinalAnalyzer;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.stack.StackEntry;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.types.GenericTypeBinder;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericBaseInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableFactory;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitch;
import org.benf.cfr.reader.bytecode.opcode.DecodedSwitchEntry;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheckImpl;
import org.benf.cfr.reader.entities.exceptions.ExceptionGroup;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.CannotPerformDecode;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.SetUtil;
import org.benf.cfr.reader.util.Troolean;
import org.benf.cfr.reader.util.functors.BinaryProcedure;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.graph.GraphVisitor;
import org.benf.cfr.reader.util.graph.GraphVisitorDFS;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.LoggerFactory;

public class Op03SimpleStatement
implements MutableGraph<Op03SimpleStatement>,
Dumpable,
StatementContainer<Statement>,
IndexedStatement {
    private static final Logger logger = LoggerFactory.create(Op03SimpleStatement.class);
    private final List<Op03SimpleStatement> sources = ListFactory.newList();
    private final List<Op03SimpleStatement> targets = ListFactory.newList();
    private Op03SimpleStatement linearlyPrevious;
    private Op03SimpleStatement linearlyNext;
    private boolean isNop;
    private InstrIndex index;
    private Statement containedStatement;
    private SSAIdentifiers<LValue> ssaIdentifiers;
    private BlockIdentifier thisComparisonBlock;
    private BlockIdentifier firstStatementInThisBlock;
    private final Set<BlockIdentifier> containedInBlocks = SetFactory.newSet();

    public Op03SimpleStatement(Op02WithProcessedDataAndRefs original, Statement statement) {
        this.containedStatement = statement;
        this.isNop = false;
        this.index = original.getIndex();
        this.ssaIdentifiers = new SSAIdentifiers<LValue>();
        this.containedInBlocks.addAll(original.getContainedInTheseBlocks());
        statement.setContainer(this);
    }

    public Op03SimpleStatement(Set<BlockIdentifier> containedIn, Statement statement, InstrIndex index) {
        this.containedStatement = statement;
        this.isNop = false;
        this.index = index;
        this.ssaIdentifiers = new SSAIdentifiers<LValue>();
        this.containedInBlocks.addAll(containedIn);
        statement.setContainer(this);
    }

    public Op03SimpleStatement(Set<BlockIdentifier> containedIn, Statement statement, SSAIdentifiers<LValue> ssaIdentifiers, InstrIndex index) {
        this.containedStatement = statement;
        this.isNop = false;
        this.index = index;
        this.ssaIdentifiers = new SSAIdentifiers<LValue>(ssaIdentifiers);
        this.containedInBlocks.addAll(containedIn);
        statement.setContainer(this);
    }

    @Override
    public List<Op03SimpleStatement> getSources() {
        return this.sources;
    }

    @Override
    public List<Op03SimpleStatement> getTargets() {
        return this.targets;
    }

    @Override
    public void addSource(Op03SimpleStatement source) {
        if (source == null) {
            throw new ConfusedCFRException("Null source being added.");
        }
        this.sources.add(source);
    }

    @Override
    public void addTarget(Op03SimpleStatement target) {
        this.targets.add(target);
    }

    @Override
    public Statement getStatement() {
        return this.containedStatement;
    }

    @Override
    public Statement getTargetStatement(int idx) {
        Statement statement;
        Op03SimpleStatement target;
        if (this.targets.size() <= idx) {
            throw new ConfusedCFRException("Trying to get invalid target " + idx);
        }
        if ((statement = (target = this.targets.get(idx)).getStatement()) != null) return statement;
        throw new ConfusedCFRException("Invalid target statement");
    }

    @Override
    public void replaceStatement(Statement newStatement) {
        newStatement.setContainer(this);
        this.containedStatement = newStatement;
    }

    @Override
    public void nopOut() {
        if (this.isNop) {
            return;
        }
        if (this.targets.isEmpty()) {
            this.containedStatement = new Nop();
            this.isNop = true;
            this.containedStatement.setContainer(this);
            return;
        }
        if (this.targets.size() != 1) {
            throw new ConfusedCFRException("Trying to nopOut a node with multiple targets");
        }
        this.containedStatement = new Nop();
        this.isNop = true;
        this.containedStatement.setContainer(this);
        Op03SimpleStatement target = this.targets.get(0);
        for (Op03SimpleStatement source : this.sources) {
            source.replaceTarget(this, target);
        }
        target.replaceSingleSourceWith(this, this.sources);
        this.sources.clear();
        this.targets.clear();
    }

    @Override
    public void nopOutConditional() {
        this.containedStatement = new Nop();
        this.isNop = true;
        this.containedStatement.setContainer(this);
        for (int i = 1; i < this.targets.size(); ++i) {
            Op03SimpleStatement dropTarget = this.targets.get(i);
            dropTarget.removeSource(this);
        }
        Op03SimpleStatement target = this.targets.get(0);
        this.targets.clear();
        this.targets.add(target);
        for (Op03SimpleStatement source : this.sources) {
            source.replaceTarget(this, target);
        }
        target.replaceSingleSourceWith(this, this.sources);
    }

    public void clear() {
        for (Op03SimpleStatement source : this.sources) {
            if (!source.getTargets().contains(this)) continue;
            source.removeTarget(this);
        }
        this.sources.clear();
        for (Op03SimpleStatement target : this.targets) {
            if (!target.getSources().contains(this)) continue;
            target.removeSource(this);
        }
        this.targets.clear();
        this.nopOut();
    }

    @Override
    public SSAIdentifiers<LValue> getSSAIdentifiers() {
        return this.ssaIdentifiers;
    }

    @Override
    public Set<BlockIdentifier> getBlockIdentifiers() {
        return this.containedInBlocks;
    }

    @Override
    public BlockIdentifier getBlockStarted() {
        return this.firstStatementInThisBlock;
    }

    @Override
    public Set<BlockIdentifier> getBlocksEnded() {
        if (this.linearlyPrevious == null) {
            return SetFactory.newSet();
        }
        Set<BlockIdentifier> in = SetFactory.newSet(this.linearlyPrevious.getBlockIdentifiers());
        in.removeAll(this.getBlockIdentifiers());
        Iterator<BlockIdentifier> iterator = in.iterator();
        while (iterator.hasNext()) {
            BlockIdentifier blockIdentifier;
            if ((blockIdentifier = iterator.next()).getBlockType().isBreakable()) continue;
            iterator.remove();
        }
        return in;
    }

    @Override
    public void copyBlockInformationFrom(StatementContainer other) {
        Op03SimpleStatement other3 = (Op03SimpleStatement)other;
        this.containedInBlocks.addAll(other.getBlockIdentifiers());
        if (this.firstStatementInThisBlock != null) return;
        this.firstStatementInThisBlock = other3.firstStatementInThisBlock;
    }

    private boolean isNop() {
        return this.isNop;
    }

    public void replaceBlockIfIn(BlockIdentifier oldB, BlockIdentifier newB) {
        if (!this.containedInBlocks.remove(oldB)) return;
        this.containedInBlocks.add(newB);
    }

    public void replaceTarget(Op03SimpleStatement oldTarget, Op03SimpleStatement newTarget) {
        int index = this.targets.indexOf(oldTarget);
        if (index == -1) {
            throw new ConfusedCFRException("Invalid target");
        }
        this.targets.set(index, newTarget);
    }

    private void replaceSingleSourceWith(Op03SimpleStatement oldSource, List<Op03SimpleStatement> newSources) {
        if (!this.sources.remove(oldSource)) {
            throw new ConfusedCFRException("Invalid source");
        }
        this.sources.addAll(newSources);
    }

    public void replaceSource(Op03SimpleStatement oldSource, Op03SimpleStatement newSource) {
        int index = this.sources.indexOf(oldSource);
        if (index == -1) {
            throw new ConfusedCFRException("Invalid source");
        }
        this.sources.set(index, newSource);
    }

    public void removeSource(Op03SimpleStatement oldSource) {
        if (this.sources.remove(oldSource)) return;
        throw new ConfusedCFRException("Invalid source, tried to remove " + oldSource + "\nfrom " + this + "\nbut was not a source.");
    }

    public void removeTarget(Op03SimpleStatement oldTarget) {
        if (this.containedStatement instanceof GotoStatement) {
            throw new ConfusedCFRException("Removing goto target");
        }
        if (this.targets.remove(oldTarget)) return;
        throw new ConfusedCFRException("Invalid target, tried to remove " + oldTarget + "\nfrom " + this + "\nbut was not a target.");
    }

    private LValue getCreatedLValue() {
        return this.containedStatement.getCreatedLValue();
    }

    @Override
    public InstrIndex getIndex() {
        return this.index;
    }

    public void setIndex(InstrIndex index) {
        this.index = index;
    }

    private void markBlockStatement(BlockIdentifier blockIdentifier, Op03SimpleStatement lastInBlock, Op03SimpleStatement blockEnd, List<Op03SimpleStatement> statements) {
        if (this.thisComparisonBlock != null) {
            throw new ConfusedCFRException("Statement marked as the start of multiple blocks");
        }
        this.thisComparisonBlock = blockIdentifier;
        switch (blockIdentifier.getBlockType()) {
            case WHILELOOP: {
                IfStatement ifStatement = (IfStatement)this.containedStatement;
                ifStatement.replaceWithWhileLoopStart(blockIdentifier);
                Op03SimpleStatement whileEndTarget = this.targets.get(1);
                boolean pullOutJump = this.index.isBackJumpTo(whileEndTarget);
                if (!(pullOutJump || statements.indexOf(lastInBlock) == statements.indexOf(blockEnd) - 1)) {
                    pullOutJump = true;
                }
                if (!pullOutJump) return;
                Set<BlockIdentifier> backJumpContainedIn = SetFactory.newSet(this.containedInBlocks);
                backJumpContainedIn.remove(blockIdentifier);
                Op03SimpleStatement backJump = new Op03SimpleStatement(backJumpContainedIn, new GotoStatement(), blockEnd.index.justBefore());
                whileEndTarget.replaceSource(this, backJump);
                this.replaceTarget(whileEndTarget, backJump);
                backJump.addSource(this);
                backJump.addTarget(whileEndTarget);
                int insertAfter = statements.indexOf(blockEnd) - 1;
                while (!statements.get((int)insertAfter).containedInBlocks.containsAll(this.containedInBlocks)) {
                    --insertAfter;
                }
                backJump.index = statements.get((int)insertAfter).index.justAfter();
                statements.add(insertAfter + 1, backJump);
                break;
            }
            case UNCONDITIONALDOLOOP: {
                this.containedStatement.getContainer().replaceStatement(new WhileStatement(null, blockIdentifier));
                break;
            }
            case DOLOOP: {
                IfStatement ifStatement = (IfStatement)this.containedStatement;
                ifStatement.replaceWithWhileLoopEnd(blockIdentifier);
                break;
            }
            case SIMPLE_IF_ELSE: 
            case SIMPLE_IF_TAKEN: {
                throw new ConfusedCFRException("Shouldn't be marking the comparison of an IF");
            }
            default: {
                throw new ConfusedCFRException("Don't know how to start a block like this");
            }
        }
    }

    public void markFirstStatementInBlock(BlockIdentifier blockIdentifier) {
        if (this.firstStatementInThisBlock != null && this.firstStatementInThisBlock != blockIdentifier) {
            throw new ConfusedCFRException("Statement already marked as first in another block");
        }
        this.firstStatementInThisBlock = blockIdentifier;
    }

    private void markBlock(BlockIdentifier blockIdentifier) {
        this.containedInBlocks.add(blockIdentifier);
    }

    private void collect(LValueAssignmentAndAliasCondenser lValueAssigmentCollector) {
        this.containedStatement.collectLValueAssignments(lValueAssigmentCollector);
    }

    private void condense(LValueRewriter lValueRewriter) {
        this.containedStatement.replaceSingleUsageLValues(lValueRewriter, this.ssaIdentifiers);
    }

    private void rewrite(ExpressionRewriter expressionRewriter) {
        this.containedStatement.rewriteExpressions(expressionRewriter, this.ssaIdentifiers);
    }

    private void findCreation(CreationCollector creationCollector) {
        this.containedStatement.collectObjectCreation(creationCollector);
    }

    public boolean condenseWithNextConditional() {
        return this.containedStatement.condenseWithNextConditional();
    }

    private void simplifyConditional() {
        if (!(this.containedStatement instanceof IfStatement)) return;
        IfStatement ifStatement = (IfStatement)this.containedStatement;
        ifStatement.simplifyCondition();
    }

    private boolean needsLabel() {
        Op03SimpleStatement source;
        if (this.sources.size() > 1) {
            return true;
        }
        if (this.sources.size() == 0) {
            return false;
        }
        return !(source = this.sources.get(0)).getIndex().directlyPreceeds(this.getIndex());
    }

    @Override
    public String getLabel() {
        return this.getIndex().toString();
    }

    public void dumpInner(Dumper dumper) {
        if (this.needsLabel()) {
            dumper.print(this.getLabel() + ":\n");
        }
        for (BlockIdentifier blockIdentifier : this.containedInBlocks) {
            dumper.print(blockIdentifier + " ");
        }
        this.getStatement().dump(dumper);
    }

    public static void dumpAll(List<Op03SimpleStatement> statements, Dumper dumper) {
        for (Op03SimpleStatement statement : statements) {
            statement.dumpInner(dumper);
        }
    }

    @Override
    public Dumper dump(Dumper dumper) {
        dumper.print("**********\n");
        List reachableNodes = ListFactory.newList();
        GraphVisitorCallee graphVisitorCallee = new GraphVisitorCallee(reachableNodes);
        GraphVisitorDFS<Op03SimpleStatement> visitor = new GraphVisitorDFS<Op03SimpleStatement>(this, (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)graphVisitorCallee);
        visitor.process();
        try {
            Collections.sort(reachableNodes, new CompareByIndex());
        }
        catch (ConfusedCFRException e) {
            dumper.print("CONFUSED!" + e);
        }
        for (Op03SimpleStatement op : reachableNodes) {
            op.dumpInner(dumper);
        }
        dumper.print("**********\n");
        return dumper;
    }

    public Op04StructuredStatement getStructuredStatementPlaceHolder() {
        return new Op04StructuredStatement(this.index, (Collection<BlockIdentifier>)this.containedInBlocks, this.containedStatement.getStructuredStatement());
    }

    private boolean isCompound() {
        return this.containedStatement.isCompound();
    }

    private List<Op03SimpleStatement> splitCompound() {
        List result = ListFactory.newList();
        List<Statement> innerStatements = this.containedStatement.getCompoundParts();
        InstrIndex nextIndex = this.index.justAfter();
        for (Statement statement : innerStatements) {
            result.add((Op03SimpleStatement)new Op03SimpleStatement(this.containedInBlocks, statement, nextIndex));
            nextIndex = nextIndex.justAfter();
        }
        ((Op03SimpleStatement)result.get((int)0)).firstStatementInThisBlock = this.firstStatementInThisBlock;
        Op03SimpleStatement previous = null;
        for (Op03SimpleStatement statement2 : result) {
            if (previous != null) {
                statement2.addSource(previous);
                previous.addTarget(statement2);
            }
            previous = statement2;
        }
        Op03SimpleStatement newStart = (Op03SimpleStatement)result.get(0);
        Op03SimpleStatement newEnd = previous;
        for (Op03SimpleStatement source : this.sources) {
            source.replaceTarget(this, newStart);
            newStart.addSource(source);
        }
        for (Op03SimpleStatement target : this.targets) {
            target.replaceSource(this, newEnd);
            newEnd.addTarget(target);
        }
        this.containedStatement = new Nop();
        this.isNop = true;
        return result;
    }

    public static void flattenCompoundStatements(List<Op03SimpleStatement> statements) {
        List newStatements = ListFactory.newList();
        for (Op03SimpleStatement statement : statements) {
            if (!statement.isCompound()) continue;
            newStatements.addAll(statement.splitCompound());
        }
        statements.addAll(newStatements);
    }

    private void collectLocallyMutatedVariables(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        this.ssaIdentifiers = this.containedStatement.collectLocallyMutatedVariables(ssaIdentifierFactory);
    }

    public static void assignSSAIdentifiers(Method method, List<Op03SimpleStatement> statements) {
        SSAIdentifierFactory<LocalVariable> ssaIdentifierFactory = new SSAIdentifierFactory<LocalVariable>();
        List<LocalVariable> params = method.getMethodPrototype().getComputedParameters();
        Map initialSSAValues = MapFactory.newMap();
        for (LocalVariable param : params) {
            initialSSAValues.put((LocalVariable)param, (SSAIdent)ssaIdentifierFactory.getIdent(param));
        }
        SSAIdentifiers initialIdents = new SSAIdentifiers(initialSSAValues);
        for (Op03SimpleStatement statement : statements) {
            statement.collectLocallyMutatedVariables((SSAIdentifierFactory<LValue>)ssaIdentifierFactory);
        }
        Op03SimpleStatement entry = statements.get(0);
        LinkedList toProcess = ListFactory.newLinkedList();
        toProcess.addAll(statements);
        while (!toProcess.isEmpty()) {
            Op03SimpleStatement statement2 = (Op03SimpleStatement)toProcess.remove();
            SSAIdentifiers<LValue> ssaIdentifiers = statement2.ssaIdentifiers;
            boolean changed = false;
            if (statement2 == entry && ssaIdentifiers.mergeWith(initialIdents)) {
                changed = true;
            }
            for (Op03SimpleStatement source : statement2.getSources()) {
                if (!ssaIdentifiers.mergeWith(source.ssaIdentifiers)) continue;
                changed = true;
            }
            if (!changed) continue;
            toProcess.addAll(statement2.getTargets());
        }
    }

    public static void condenseLValueChain1(List<Op03SimpleStatement> statements) {
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement statement2;
            Statement stm;
            Statement stm2;
            Op03SimpleStatement statement;
            if (!(stm = (statement = i$.next()).getStatement() instanceof AssignmentSimple) || statement.getTargets().size() != 1) continue;
            if ((statement2 = statement.getTargets().get(0)).getSources().size() != 1) continue;
            if (!(stm2 = statement2.getStatement() instanceof AssignmentSimple)) continue;
            Op03SimpleStatement.applyLValueSwap((AssignmentSimple)stm, (AssignmentSimple)stm2, statement, statement2);
        }
    }

    public static void applyLValueSwap(AssignmentSimple a1, AssignmentSimple a2, Op03SimpleStatement stm1, Op03SimpleStatement stm2) {
        Expression r1 = a1.getRValue();
        Expression r2 = a2.getRValue();
        if (!r1.equals(r2)) {
            return;
        }
        LValue l1 = a1.getCreatedLValue();
        LValue l2 = a2.getCreatedLValue();
        if (!(l1 instanceof StackSSALabel) || l2 instanceof StackSSALabel) return;
        stm1.replaceStatement(a2);
        stm2.replaceStatement(new AssignmentSimple(l1, new LValueExpression(l2)));
    }

    public static void condenseLValueChain2(List<Op03SimpleStatement> statements) {
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement statement2;
            Statement stm;
            Statement stm2;
            Op03SimpleStatement statement;
            if (!(stm = (statement = i$.next()).getStatement() instanceof AssignmentSimple) || statement.getTargets().size() != 1) continue;
            if ((statement2 = statement.getTargets().get(0)).getSources().size() != 1) continue;
            if (!(stm2 = statement2.getStatement() instanceof AssignmentSimple)) continue;
            Op03SimpleStatement.applyLValueCondense((AssignmentSimple)stm, (AssignmentSimple)stm2, statement, statement2);
        }
    }

    public static void applyLValueCondense(AssignmentSimple a1, AssignmentSimple a2, Op03SimpleStatement stm1, Op03SimpleStatement stm2) {
        Expression r1 = a1.getRValue();
        Expression r2 = a2.getRValue();
        LValue l1 = a1.getCreatedLValue();
        LValue l2 = a2.getCreatedLValue();
        if (!r2.equals(new LValueExpression(l1))) {
            return;
        }
        stm1.nopOut();
        AbstractAssignmentExpression newRhs = null;
        if (r1 instanceof ArithmeticOperation && ((ArithmeticOperation)r1).isMutationOf(l1)) {
            AbstractMutatingAssignmentExpression me;
            ArithmeticOperation ar1 = (ArithmeticOperation)r1;
            newRhs = me = ar1.getMutationOf(l1);
        }
        if (newRhs == null) {
            newRhs = new AssignmentExpression(l1, r1, true);
        }
        stm2.replaceStatement(new AssignmentSimple(l2, newRhs));
    }

    public static void determineFinal(List<Op03SimpleStatement> statements, VariableFactory variableFactory) {
    }

    public static void condenseLValues(List<Op03SimpleStatement> statements) {
        LValueAssignmentAndAliasCondenser.MutationRewriterFirstPass firstPassRewriter;
        AccountingRewriter accountingRewriter = new AccountingRewriter();
        for (Op03SimpleStatement statement : statements) {
            statement.rewrite(accountingRewriter);
        }
        accountingRewriter.flush();
        LValueAssignmentAndAliasCondenser lValueAssigmentCollector = new LValueAssignmentAndAliasCondenser();
        for (Op03SimpleStatement statement2 : statements) {
            statement2.collect(lValueAssigmentCollector);
        }
        if ((firstPassRewriter = lValueAssigmentCollector.getMutationRewriterFirstPass()) != null) {
            LValueAssignmentAndAliasCondenser.MutationRewriterSecondPass secondPassRewriter;
            for (Op03SimpleStatement statement3 : statements) {
                statement3.condense(firstPassRewriter);
            }
            if ((secondPassRewriter = firstPassRewriter.getSecondPassRewriter()) != null) {
                for (Op03SimpleStatement statement42 : statements) {
                    statement42.condense(secondPassRewriter);
                }
            }
        }
        LValueAssignmentAndAliasCondenser.AliasRewriter multiRewriter = lValueAssigmentCollector.getAliasRewriter();
        for (Op03SimpleStatement statement42 : statements) {
            statement42.condense(multiRewriter);
        }
        multiRewriter.inferAliases();
        for (Op03SimpleStatement statement42 : statements) {
            statement42.condense(lValueAssigmentCollector);
        }
    }

    private static void replacePostChangeAssignment(Op03SimpleStatement statement) {
        Expression incrRValue;
        AssignmentSimple assignmentSimplePrior;
        ArithmeticOperation arithOp;
        Op03SimpleStatement prior;
        ArithOp op;
        LValue tmp;
        Statement statementPrior;
        AssignmentSimple assignmentSimple = (AssignmentSimple)statement.containedStatement;
        LValue postIncLValue = assignmentSimple.getCreatedLValue();
        if (statement.sources.size() != 1) {
            return;
        }
        if (!(statementPrior = (prior = statement.sources.get(0)).getStatement() instanceof AssignmentSimple)) {
            return;
        }
        if (!(tmp = (assignmentSimplePrior = (AssignmentSimple)statementPrior).getCreatedLValue() instanceof StackSSALabel)) {
            return;
        }
        if (!assignmentSimplePrior.getRValue().equals(new LValueExpression(postIncLValue))) {
            return;
        }
        StackSSALabel tmpStackVar = (StackSSALabel)tmp;
        StackValue stackValue = new StackValue(tmpStackVar);
        if (!(incrRValue = assignmentSimple.getRValue() instanceof ArithmeticOperation)) {
            return;
        }
        if (!((op = (arithOp = (ArithmeticOperation)incrRValue).getOp()).equals((Object)ArithOp.PLUS) || op.equals((Object)ArithOp.MINUS))) {
            return;
        }
        Expression lhs = arithOp.getLhs();
        Expression rhs = arithOp.getRhs();
        if (stackValue.equals(lhs)) {
            if (!Literal.equalsAnyOne(rhs)) {
                return;
            }
        } else {
            if (!stackValue.equals(rhs)) return;
            if (!Literal.equalsAnyOne(lhs)) {
                return;
            }
            if (op.equals((Object)ArithOp.MINUS)) {
                return;
            }
        }
        ArithmeticPostMutationOperation postMutationOperation = new ArithmeticPostMutationOperation(postIncLValue, op);
        prior.replaceStatement(new AssignmentSimple(tmp, postMutationOperation));
        statement.nopOut();
    }

    private static boolean replacePreChangeAssignment(Op03SimpleStatement statement) {
        ArithmeticOperation arithmeticOperation;
        AssignmentSimple assignmentSimple = (AssignmentSimple)statement.containedStatement;
        LValue lValue = assignmentSimple.getCreatedLValue();
        Expression rValue = assignmentSimple.getRValue();
        if (!(rValue instanceof ArithmeticOperation)) {
            return false;
        }
        if (!(arithmeticOperation = (ArithmeticOperation)rValue).isMutationOf(lValue)) {
            return false;
        }
        AbstractMutatingAssignmentExpression mutationOperation = arithmeticOperation.getMutationOf(lValue);
        AssignmentPreMutation res = new AssignmentPreMutation(lValue, mutationOperation);
        statement.replaceStatement(res);
        return true;
    }

    public static void replacePrePostChangeAssignments(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> assignments = Functional.filter(statements, new TypeFilter(AssignmentSimple.class));
        for (Op03SimpleStatement assignment : assignments) {
            if (Op03SimpleStatement.replacePreChangeAssignment(assignment)) continue;
            Op03SimpleStatement.replacePostChangeAssignment(assignment);
        }
    }

    private static void eliminateCatchTemporary(Op03SimpleStatement catchh) {
        StackSSALabel catchingSSA;
        LValue catching;
        CatchStatement catchStatement;
        WildcardMatch match;
        if (catchh.targets.size() != 1) {
            return;
        }
        Op03SimpleStatement maybeAssign = catchh.targets.get(0);
        if (!(catching = (catchStatement = (CatchStatement)catchh.getStatement()).getCreatedLValue() instanceof StackSSALabel)) {
            return;
        }
        if ((catchingSSA = (StackSSALabel)catching).getStackEntry().getUsageCount() != 1) {
            return;
        }
        while (maybeAssign.getStatement() instanceof TryStatement) {
            maybeAssign = maybeAssign.targets.get(0);
        }
        if (!(match = new WildcardMatch()).match(new AssignmentSimple((match = new WildcardMatch()).getLValueWildCard("caught"), new StackValue(catchingSSA)), maybeAssign.getStatement())) {
            return;
        }
        catchh.replaceStatement(new CatchStatement(catchStatement.getExceptions(), match.getLValueWildCard("caught").getMatch()));
        maybeAssign.nopOut();
    }

    public static void eliminateCatchTemporaries(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> catches = Functional.filter(statements, new TypeFilter(CatchStatement.class));
        for (Op03SimpleStatement catchh : catches) {
            Op03SimpleStatement.eliminateCatchTemporary(catchh);
        }
    }

    public static void removePointlessExpressionStatements(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> exrps = Functional.filter(statements, new TypeFilter(ExpressionStatement.class));
        Iterator<Op03SimpleStatement> i$ = exrps.iterator();
        while (i$.hasNext()) {
            ExpressionStatement es;
            Op03SimpleStatement esc;
            Expression expression;
            if (!(expression = (es = (ExpressionStatement)(esc = i$.next()).getStatement()).getExpression() instanceof LValueExpression) && !(expression instanceof StackValue) && !(expression instanceof Literal)) continue;
            esc.nopOut();
        }
        List<Op03SimpleStatement> sas = Functional.filter(statements, new TypeFilter(AssignmentSimple.class));
        for (Op03SimpleStatement ass : sas) {
            Expression rValue;
            LValueExpression lValueExpression;
            AssignmentSimple assignmentSimple = (AssignmentSimple)ass.containedStatement;
            LValue lValue = assignmentSimple.getCreatedLValue();
            if ((rValue = assignmentSimple.getRValue()).getClass() != LValueExpression.class || !(lValueExpression = (LValueExpression)rValue).getLValue().equals(lValue)) continue;
            ass.nopOut();
        }
    }

    private static void pushPreChangeBack(Op03SimpleStatement preChange) {
        AssignmentPreMutation mutation = (AssignmentPreMutation)preChange.containedStatement;
        Op03SimpleStatement current = preChange;
        LValue mutatedLValue = mutation.getCreatedLValue();
        LValueExpression lvalueExpression = new LValueExpression(mutatedLValue);
        UsageWatcher usageWatcher = new UsageWatcher(mutatedLValue, null);
        do {
            Statement innerStatement;
            List<Op03SimpleStatement> sources;
            AssignmentSimple assignmentSimple;
            if ((sources = current.getSources()).size() != 1) {
                return;
            }
            if (innerStatement = (current = sources.get(0)).getStatement() instanceof AssignmentSimple && (assignmentSimple = (AssignmentSimple)innerStatement).getRValue().equals(lvalueExpression)) {
                LValue tgt = assignmentSimple.getCreatedLValue();
                preChange.nopOut();
                current.replaceStatement(new AssignmentSimple(tgt, mutation.getPostMutation()));
                return;
            }
            current.condense(usageWatcher);
        } while (!usageWatcher.isFound());
    }

    public static void pushPreChangeBack(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> assignments = Functional.filter(statements, new TypeFilter(AssignmentPreMutation.class));
        assignments = Functional.filter(assignments, new StatementCanBePostMutation(null));
        for (Op03SimpleStatement assignment : assignments) {
            Op03SimpleStatement.pushPreChangeBack(assignment);
        }
    }

    public static void condenseConstruction(DCCommonState state, Method method, List<Op03SimpleStatement> statements) {
        CreationCollector creationCollector = new CreationCollector();
        for (Op03SimpleStatement statement : statements) {
            statement.findCreation(creationCollector);
        }
        creationCollector.condenseConstructions(method, state);
    }

    private static void rollAssignmentsIntoConditional(Op03SimpleStatement conditional) {
    }

    public static void rollAssignmentsIntoConditionals(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> conditionals = Functional.filter(statements, new TypeFilter(IfStatement.class));
        for (Op03SimpleStatement conditional : conditionals) {
            Op03SimpleStatement.rollAssignmentsIntoConditional(conditional);
        }
    }

    public static void condenseConditionals(List<Op03SimpleStatement> statements) {
        for (int x = 0; x < statements.size(); ++x) {
            boolean retry = false;
            do {
                Op03SimpleStatement op03SimpleStatement;
                retry = false;
                if (!(op03SimpleStatement = statements.get(x)).condenseWithNextConditional()) continue;
                retry = true;
                while (!(statements.get(--x).isNop() && x > 0)) {
                }
            } while (retry);
        }
    }

    public static void simplifyConditionals(List<Op03SimpleStatement> statements) {
        for (Op03SimpleStatement statement : statements) {
            statement.simplifyConditional();
        }
    }

    public static boolean condenseConditionals2(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> ifStatements = Functional.filter(statements, new TypeFilter(IfStatement.class));
        boolean result = false;
        for (Op03SimpleStatement ifStatement : ifStatements) {
            if (!Op03SimpleStatement.condenseConditional2_type1(ifStatement, statements)) continue;
            result = true;
        }
        return result;
    }

    private static void replaceReturningIf(Op03SimpleStatement ifStatement, boolean aggressive) {
        Op03SimpleStatement tgt;
        if (ifStatement.containedStatement.getClass() != IfStatement.class) {
            return;
        }
        IfStatement innerIf = (IfStatement)ifStatement.containedStatement;
        if (ifStatement.getTargets().size() != 2) {
            boolean x = true;
        }
        Op03SimpleStatement origtgt = tgt = ifStatement.getTargets().get(1);
        boolean requireJustOneSource = !aggressive;
        do {
            Op03SimpleStatement next = Op03SimpleStatement.followNopGoto(tgt, requireJustOneSource, aggressive);
            if (next == tgt) break;
            tgt = next;
        } while (true);
        Statement tgtStatement = tgt.containedStatement;
        if (!(tgtStatement instanceof ReturnStatement)) {
            return;
        }
        ifStatement.replaceStatement(new IfExitingStatement(innerIf.getCondition(), tgtStatement));
        origtgt.removeSource(ifStatement);
        ifStatement.removeTarget(origtgt);
    }

    private static void replaceReturningGoto(Op03SimpleStatement gotoStatement, boolean aggressive) {
        Op03SimpleStatement tgt;
        if (gotoStatement.containedStatement.getClass() != GotoStatement.class) {
            return;
        }
        Op03SimpleStatement origtgt = tgt = gotoStatement.getTargets().get(0);
        boolean requireJustOneSource = !aggressive;
        do {
            Op03SimpleStatement next = Op03SimpleStatement.followNopGoto(tgt, requireJustOneSource, aggressive);
            if (next == tgt) break;
            tgt = next;
        } while (true);
        Statement tgtStatement = tgt.containedStatement;
        if (!(tgtStatement instanceof ReturnStatement)) {
            return;
        }
        gotoStatement.replaceStatement(tgtStatement);
        origtgt.removeSource(gotoStatement);
        gotoStatement.removeTarget(origtgt);
    }

    public static void replaceReturningIfs(List<Op03SimpleStatement> statements, boolean aggressive) {
        List<Op03SimpleStatement> ifStatements = Functional.filter(statements, new TypeFilter(IfStatement.class));
        for (Op03SimpleStatement ifStatement : ifStatements) {
            Op03SimpleStatement.replaceReturningIf(ifStatement, aggressive);
        }
    }

    public static void propagateToReturn(Method method, List<Op03SimpleStatement> statements) {
        boolean success = false;
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement stm;
            Statement inner;
            if ((inner = (stm = i$.next()).getStatement()).getClass() != AssignmentSimple.class) continue;
            if (stm.getTargets().size() != 1) continue;
            AssignmentSimple assignmentSimple = (AssignmentSimple)inner;
            LValue lValue = assignmentSimple.getCreatedLValue();
            Expression rValue = assignmentSimple.getRValue();
            if (!(lValue instanceof StackSSALabel) && !(lValue instanceof LocalVariable)) continue;
            Map display = MapFactory.newMap();
            if (rValue instanceof Literal) {
                display.put((LValue)lValue, (Literal)((Literal)rValue));
            }
            success|=Op03SimpleStatement.propagateLiteral(method, stm, stm.getTargets().get(0), lValue, rValue, display);
        }
        if (!success) return;
        Op03SimpleStatement.replaceReturningIfs(statements, true);
    }

    public static void propagateToReturn2(Method method, List<Op03SimpleStatement> statements) {
        boolean success = false;
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement stm;
            Statement inner;
            if (!(inner = (stm = i$.next()).getStatement() instanceof ReturnStatement)) continue;
            success|=Op03SimpleStatement.pushReturnBack(method, stm);
        }
        if (!success) return;
        Op03SimpleStatement.replaceReturningIfs(statements, true);
    }

    private static boolean pushReturnBack(Method method, Op03SimpleStatement stm) {
        ReturnStatement returnStatement = (ReturnStatement)stm.getStatement();
        List toRemove = null;
        for (Op03SimpleStatement src : stm.getSources()) {
            if (src.getStatement().getClass() != GotoStatement.class) continue;
            if (toRemove == null) {
                toRemove = ListFactory.newList();
            }
            toRemove.add((Op03SimpleStatement)src);
        }
        if (toRemove == null) {
            return false;
        }
        CloneHelper cloneHelper = new CloneHelper();
        for (Op03SimpleStatement remove2 : toRemove) {
            remove2.replaceStatement((Statement)returnStatement.deepClone(cloneHelper));
            remove2.removeTarget(stm);
            stm.removeSource(remove2);
        }
        for (Op03SimpleStatement remove2 : toRemove) {
            Op03SimpleStatement.pushReturnBack(method, remove2);
        }
        return true;
    }

    private static boolean propagateLiteral(Method method, Op03SimpleStatement original, Op03SimpleStatement orignext, LValue originalLValue, Expression originalRValue, Map<LValue, Literal> display) {
        Op03SimpleStatement current = orignext;
        Set seen = SetFactory.newSet();
        do {
            Boolean bool;
            Literal literal;
            IfStatement ifStatement;
            if (!seen.add((Op03SimpleStatement)current)) {
                return false;
            }
            cls = current.getStatement().getClass();
            List<Op03SimpleStatement> curTargets = current.getTargets();
            int nTargets = curTargets.size();
            if (cls == Nop.class) {
                if (nTargets != 1) {
                    return false;
                }
                current = curTargets.get(0);
                continue;
            }
            if (cls == ReturnNothingStatement.class) break;
            if (cls == ReturnValueStatement.class) break;
            if (cls == GotoStatement.class || cls == MonitorExitStatement.class) {
                if (nTargets != 1) {
                    return false;
                }
                current = curTargets.get(0);
                continue;
            }
            if (cls == AssignmentSimple.class) {
                AssignmentSimple assignmentSimple;
                Literal literal2;
                LValue lValue;
                if (!(lValue = (assignmentSimple = (AssignmentSimple)current.getStatement()).getCreatedLValue() instanceof StackSSALabel || lValue instanceof LocalVariable)) {
                    return false;
                }
                if ((literal2 = assignmentSimple.getRValue().getComputedLiteral(display)) == null) {
                    return false;
                }
                display.put(lValue, literal2);
                current = curTargets.get(0);
                continue;
            }
            if (cls != IfStatement.class) return false;
            if ((literal = (ifStatement = (IfStatement)current.getStatement()).getCondition().getComputedLiteral(display)) == null) {
                return false;
            }
            if ((bool = literal.getValue().getMaybeBoolValue()) == null) {
                return false;
            }
            if (bool.booleanValue()) {
                current = curTargets.get(1);
                continue;
            }
            current = curTargets.get(0);
        } while (true);
        Class cls = current.getStatement().getClass();
        if (cls == ReturnNothingStatement.class) {
            if (!(originalRValue instanceof Literal)) {
                return false;
            }
            original.replaceStatement(new ReturnNothingStatement());
            orignext.removeSource(original);
            original.removeTarget(orignext);
            return true;
        }
        if (cls != ReturnValueStatement.class) return false;
        ReturnValueStatement returnValueStatement = (ReturnValueStatement)current.getStatement();
        if (originalRValue instanceof Literal) {
            Literal e;
            if ((e = returnValueStatement.getReturnValue().getComputedLiteral(display)) == null) {
                return false;
            }
            original.replaceStatement(new ReturnValueStatement(e, returnValueStatement.getFnReturnType()));
        } else {
            Expression ret;
            LValue retLValue;
            if (!(ret = returnValueStatement.getReturnValue() instanceof LValueExpression)) {
                return false;
            }
            if (!(retLValue = ((LValueExpression)ret).getLValue()).equals(originalLValue)) {
                return false;
            }
            original.replaceStatement(new ReturnValueStatement(originalRValue, returnValueStatement.getFnReturnType()));
        }
        orignext.removeSource(original);
        original.removeTarget(orignext);
        return true;
    }

    private static Op03SimpleStatement followNopGoto(Op03SimpleStatement in, boolean requireJustOneSource, boolean aggressive) {
        Statement statement;
        if (in == null) {
            return null;
        }
        if (requireJustOneSource && in.sources.size() != 1) {
            return in;
        }
        if (in.targets.size() != 1) {
            return in;
        }
        if (!(statement = in.getStatement() instanceof Nop) && !(statement instanceof GotoStatement) && (!aggressive || !(statement instanceof CaseStatement)) && (!aggressive || !(statement instanceof MonitorExitStatement))) return in;
        in = in.targets.get(0);
        return in;
    }

    public static Op03SimpleStatement followNopGotoChain(Op03SimpleStatement in, boolean requireJustOneSource, boolean skipLabels) {
        if (in == null) {
            return null;
        }
        Set seen = SetFactory.newSet();
        while (seen.add((Op03SimpleStatement)in)) {
            Op03SimpleStatement next = Op03SimpleStatement.followNopGoto(in, requireJustOneSource, skipLabels);
            if (next == in) {
                return in;
            }
            in = next;
        }
        return in;
    }

    private static boolean condenseConditional2_type2(Op03SimpleStatement ifStatement) {
        return false;
    }

    private static boolean condenseConditional2_type1(Op03SimpleStatement ifStatement, List<Op03SimpleStatement> allStatements) {
        Op03SimpleStatement nottaken3;
        Op03SimpleStatement nottaken2;
        Op03SimpleStatement next;
        if (!(ifStatement.containedStatement instanceof IfStatement)) {
            return false;
        }
        Op03SimpleStatement taken1 = ifStatement.getTargets().get(1);
        Op03SimpleStatement nottaken1 = ifStatement.getTargets().get(0);
        if (!(nottaken1.containedStatement instanceof IfStatement)) {
            return false;
        }
        Op03SimpleStatement ifStatement2 = nottaken1;
        Op03SimpleStatement taken2 = ifStatement2.getTargets().get(1);
        Op03SimpleStatement nottaken2Immed = nottaken2 = ifStatement2.getTargets().get(0);
        if (nottaken2Immed.sources.size() != 1) {
            return false;
        }
        Op03SimpleStatement notTaken2Source = ifStatement2;
        nottaken2 = Op03SimpleStatement.followNopGotoChain(nottaken2, true, false);
        do {
            Op03SimpleStatement nontaken2rewrite = Op03SimpleStatement.followNopGoto(nottaken2, true, false);
            if (nontaken2rewrite == nottaken2) break;
            notTaken2Source = nottaken2;
            nottaken2 = nontaken2rewrite;
        } while (true);
        if (!(taken1.containedStatement instanceof IfStatement)) {
            return false;
        }
        if (taken1.sources.size() != 1) {
            return false;
        }
        Op03SimpleStatement ifStatement3 = taken1;
        Op03SimpleStatement taken3 = ifStatement3.getTargets().get(1);
        Op03SimpleStatement nottaken3Immed = nottaken3 = ifStatement3.getTargets().get(0);
        Op03SimpleStatement notTaken3Source = ifStatement3;
        do {
            Op03SimpleStatement nontaken3rewrite = Op03SimpleStatement.followNopGoto(nottaken3, true, false);
            if (nontaken3rewrite == nottaken3) break;
            notTaken3Source = nottaken3;
            nottaken3 = nontaken3rewrite;
        } while (true);
        if (nottaken2 != nottaken3) {
            return false;
        }
        if (taken2 != taken3) {
            return false;
        }
        IfStatement if1 = (IfStatement)ifStatement.containedStatement;
        IfStatement if2 = (IfStatement)ifStatement2.containedStatement;
        IfStatement if3 = (IfStatement)ifStatement3.containedStatement;
        ConditionalExpression newCond = new BooleanExpression(new TernaryExpression(if1.getCondition().getNegated().simplify(), if2.getCondition().getNegated().simplify(), if3.getCondition().getNegated().simplify())).getNegated();
        ifStatement.replaceTarget(taken1, taken3);
        taken3.addSource(ifStatement);
        taken3.removeSource(ifStatement2);
        taken3.removeSource(ifStatement3);
        nottaken1.sources.remove(ifStatement);
        nottaken2Immed.replaceSource(ifStatement2, ifStatement);
        ifStatement.replaceTarget(nottaken1, nottaken2Immed);
        nottaken3.removeSource(notTaken3Source);
        ifStatement2.replaceStatement(new Nop());
        ifStatement3.replaceStatement(new Nop());
        ifStatement2.removeTarget(taken3);
        ifStatement3.removeTarget(taken3);
        ifStatement.replaceStatement(new IfStatement(newCond));
        if (nottaken2Immed.sources.size() != 1 || !nottaken2Immed.sources.get(0).getIndex().isBackJumpFrom(nottaken2Immed) || nottaken2Immed.containedStatement.getClass() != GotoStatement.class) return true;
        Op03SimpleStatement nottaken2ImmedTgt = nottaken2Immed.targets.get(0);
        int idx = allStatements.indexOf(nottaken2Immed);
        int idx2 = idx + 1;
        do {
            next = allStatements.get(idx2);
            if (!(next.containedStatement instanceof Nop)) break;
            ++idx2;
        } while (true);
        if (next != nottaken2ImmedTgt) return true;
        nottaken2ImmedTgt.replaceSource(nottaken2Immed, ifStatement);
        ifStatement.replaceTarget(nottaken2Immed, nottaken2ImmedTgt);
        return true;
    }

    private static boolean appropriateForIfAssignmentCollapse1(Op03SimpleStatement statement) {
        boolean extraCondSeen = false;
        boolean preCondAssignmentSeen = false;
        while (statement.sources.size() == 1) {
            Op03SimpleStatement source;
            Statement contained;
            if (statement.getIndex().isBackJumpFrom(source = statement.sources.get(0))) break;
            if (contained = source.containedStatement instanceof AbstractAssignment) {
                preCondAssignmentSeen|=!extraCondSeen;
            } else {
                if (!(contained instanceof IfStatement)) break;
                extraCondSeen = true;
            }
            statement = source;
        }
        if (!preCondAssignmentSeen) {
            return false;
        }
        if (extraCondSeen) {
            return false;
        }
        InstrIndex statementIndex = statement.getIndex();
        for (Op03SimpleStatement source : statement.sources) {
            if (!statementIndex.isBackJumpFrom(source)) continue;
            return true;
        }
        return false;
    }

    private static boolean appropriateForIfAssignmentCollapse2(Op03SimpleStatement statement) {
        boolean extraCondSeen = false;
        boolean preCondAssignmentSeen = false;
        while (statement.sources.size() == 1) {
            Op03SimpleStatement source;
            Statement contained;
            if ((source = statement.sources.get(0)).getTargets().size() != 1) break;
            if (contained = source.containedStatement instanceof AbstractAssignment) {
                preCondAssignmentSeen = true;
            }
            statement = source;
        }
        if (preCondAssignmentSeen) return true;
        return false;
    }

    /*
     * Enabled aggressive block sorting
     */
    private static void collapseAssignmentsIntoConditional(Op03SimpleStatement ifStatement, boolean testEclipse) {
        WildcardMatch wcm = new WildcardMatch();
        if (!(Op03SimpleStatement.appropriateForIfAssignmentCollapse1(ifStatement) || Op03SimpleStatement.appropriateForIfAssignmentCollapse2(ifStatement))) {
            return;
        }
        IfStatement innerIf = (IfStatement)ifStatement.containedStatement;
        ConditionalExpression conditionalExpression = innerIf.getCondition();
        boolean eclipseHeuristic = testEclipse && ifStatement.getTargets().get(1).getIndex().isBackJumpFrom(ifStatement);
        if (!eclipseHeuristic) {
            Op03SimpleStatement statement = ifStatement;
            Set visited = SetFactory.newSet();
            block0 : do {
                Statement opStatement;
                if (statement.sources.size() > 1) {
                    InstrIndex statementIndex = statement.index;
                    for (Op03SimpleStatement source : statement.sources) {
                        if (!statementIndex.isBackJumpFrom(source)) continue;
                        break block0;
                    }
                }
                if (statement.sources.isEmpty()) {
                    return;
                }
                if (!visited.add((Op03SimpleStatement)(statement = statement.sources.get(0)))) {
                    return;
                }
                if (opStatement = statement.getStatement() instanceof IfStatement) break;
                if (opStatement instanceof Nop) continue;
                if (!(opStatement instanceof AbstractAssignment)) return;
            } while (true);
        }
        Op03SimpleStatement previousSource = null;
        while (ifStatement.sources.size() == 1) {
            LValueAssignmentExpressionRewriter rewriter;
            Expression replacement;
            Op03SimpleStatement source = ifStatement.sources.get(0);
            if (source == previousSource) return;
            previousSource = source;
            if (!(source.containedStatement instanceof AbstractAssignment)) {
                return;
            }
            LValue lValue = source.getCreatedLValue();
            LValueUsageCollectorSimple lvc = new LValueUsageCollectorSimple();
            conditionalExpression.collectUsedLValues((LValueUsageCollector)lvc);
            if (!lvc.isUsed(lValue)) {
                return;
            }
            AbstractAssignment assignment = (AbstractAssignment)source.containedStatement;
            AbstractAssignmentExpression assignmentExpression = assignment.getInliningExpression();
            LValueUsageCollectorSimple assignmentLVC = new LValueUsageCollectorSimple();
            assignmentExpression.collectUsedLValues((LValueUsageCollector)assignmentLVC);
            Set<LValue> used = SetFactory.newSet(assignmentLVC.getUsedLValues());
            used.remove(lValue);
            Set<LValue> usedComparison = SetFactory.newSet(lvc.getUsedLValues());
            if (SetUtil.hasIntersection(used, usedComparison)) {
                return;
            }
            if (!ifStatement.getSSAIdentifiers().isValidReplacement(lValue, source.getSSAIdentifiers())) {
                return;
            }
            if ((replacement = conditionalExpression.replaceSingleUsageLValues((LValueRewriter)(rewriter = new LValueAssignmentExpressionRewriter(lValue, assignmentExpression, source)), ifStatement.getSSAIdentifiers(), (StatementContainer)ifStatement)) == null) {
                return;
            }
            if (!(replacement instanceof ConditionalExpression)) {
                return;
            }
            innerIf.setCondition((ConditionalExpression)replacement);
        }
    }

    public static void collapseAssignmentsIntoConditionals(List<Op03SimpleStatement> statements, Options options) {
        List<Op03SimpleStatement> ifStatements = Functional.filter(statements, new TypeFilter(IfStatement.class));
        boolean testEclipse = (Boolean)options.getOption(OptionsImpl.ECLIPSE);
        for (Op03SimpleStatement statement : ifStatements) {
            Op03SimpleStatement.collapseAssignmentsIntoConditional(statement, testEclipse);
        }
    }

    public static List<Op03SimpleStatement> removeUnreachableCode(List<Op03SimpleStatement> statements, boolean checkBackJumps) {
        Set reachable = SetFactory.newSet();
        reachable.add((Op03SimpleStatement)statements.get(0));
        GraphVisitorDFS<Op03SimpleStatement> gv = new GraphVisitorDFS<Op03SimpleStatement>(statements.get(0), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(reachable, checkBackJumps){
            final /* synthetic */ Set val$reachable;
            final /* synthetic */ boolean val$checkBackJumps;

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                this.val$reachable.add(arg1);
                arg2.enqueue(arg1.getTargets());
                for (Op03SimpleStatement source : arg1.getSources()) {
                    if (source.getTargets().contains(arg1)) continue;
                    throw new IllegalStateException("Inconsistent graph " + source + " does not have a target of " + arg1);
                }
                for (Op03SimpleStatement test : arg1.getTargets()) {
                    Statement argContained = arg1.getStatement();
                    if (this.val$checkBackJumps && !(argContained instanceof JumpingStatement) && !(argContained instanceof WhileStatement) && test.getIndex().isBackJumpFrom(arg1)) {
                        throw new IllegalStateException("Backjump on non jumping statement " + arg1);
                    }
                    if (test.getSources().contains(arg1)) continue;
                    throw new IllegalStateException("Inconsistent graph " + test + " does not have a source " + arg1);
                }
            }
        });
        gv.process();
        List result = ListFactory.newList();
        for (Op03SimpleStatement statement : statements) {
            if (!reachable.contains(statement)) continue;
            result.add((Op03SimpleStatement)statement);
        }
        for (Op03SimpleStatement res1 : result) {
            List<Op03SimpleStatement> sources = ListFactory.newList(res1.getSources());
            for (Op03SimpleStatement source : sources) {
                if (reachable.contains(source)) continue;
                res1.removeSource(source);
            }
        }
        return result;
    }

    public static List<Op03SimpleStatement> renumber(List<Op03SimpleStatement> statements) {
        boolean nonNopSeen = false;
        List result = ListFactory.newList();
        for (Op03SimpleStatement statement : statements) {
            if (statement.isNop() && nonNopSeen) continue;
            result.add((Op03SimpleStatement)statement);
            if (statement.isNop()) continue;
            nonNopSeen = true;
        }
        Op03SimpleStatement.renumberInPlace(result);
        return result;
    }

    public static void renumberInPlace(List<Op03SimpleStatement> statements) {
        Collections.sort(statements, new CompareByIndex());
        Op03SimpleStatement.reindexInPlace(statements);
    }

    public static void reindexInPlace(List<Op03SimpleStatement> statements) {
        int newIndex = 0;
        Op03SimpleStatement prev = null;
        for (Op03SimpleStatement statement : statements) {
            statement.linearlyPrevious = prev;
            statement.linearlyNext = null;
            if (prev != null) {
                prev.linearlyNext = statement;
            }
            statement.setIndex(new InstrIndex(newIndex++));
            prev = statement;
        }
    }

    public static void removePointlessJumps(List<Op03SimpleStatement> statements) {
        Statement innerStatement;
        Op03SimpleStatement statement;
        int x;
        int size = statements.size() - 1;
        for (x = 0; x < size - 1; ++x) {
            Op03SimpleStatement a = statements.get(x);
            Op03SimpleStatement b = statements.get(x + 1);
            if (a.containedStatement.getClass() != GotoStatement.class || b.containedStatement.getClass() != GotoStatement.class || a.targets.get(0) != b.targets.get(0)) continue;
            Op03SimpleStatement realTgt = a.targets.get(0);
            realTgt.removeSource(a);
            a.replaceTarget(realTgt, b);
            b.addSource(a);
            a.nopOut();
        }
        for (x = 0; x < size; ++x) {
            Op03SimpleStatement maybeJump = statements.get(x);
            if (maybeJump.containedStatement.getClass() != GotoStatement.class || maybeJump.targets.size() != 1 || maybeJump.targets.get(0) != statements.get(x + 1)) continue;
            maybeJump.nopOut();
        }
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            Statement jumpingInnerPriorTarget;
            Op03SimpleStatement prior;
            Statement innerPrior;
            JumpingStatement jumpInnerPrior;
            if (!(innerStatement = (statement = i$.next()).getStatement() instanceof JumpingStatement) || statement.getSources().size() != 1 || statement.getTargets().size() != 1 || !(innerPrior = (prior = statement.getSources().get(0)).getStatement() instanceof JumpingStatement) || (jumpingInnerPriorTarget = (jumpInnerPrior = (JumpingStatement)innerPrior).getJumpTarget()) != innerStatement) continue;
            statement.nopOut();
        }
        block6 : for (int x2 = statements.size() - 1; x2 >= 0; --x2) {
            if ((innerStatement = (statement = statements.get(x2)).getStatement()).getClass() == GotoStatement.class) {
                GotoStatement innerGoto = (GotoStatement)innerStatement;
                switch (innerGoto.getJumpType()) {
                    case BREAK: {
                        break;
                    }
                    default: {
                        target = statement.targets.get(0);
                        ultimateTarget = Op03SimpleStatement.followNopGotoChain(target, false, false);
                        if (target == ultimateTarget) continue block6;
                        ultimateTarget = Op03SimpleStatement.maybeMoveTarget(ultimateTarget, statement, statements);
                        target.removeSource(statement);
                        statement.replaceTarget(target, ultimateTarget);
                        ultimateTarget.addSource(statement);
                    }
                }
                continue;
            }
            if (innerStatement.getClass() != IfStatement.class) continue;
            IfStatement ifStatement = (IfStatement)innerStatement;
            Op03SimpleStatement target = statement.targets.get(1);
            Op03SimpleStatement ultimateTarget = Op03SimpleStatement.followNopGotoChain(target, false, false);
            if (target == ultimateTarget) continue;
            ultimateTarget = Op03SimpleStatement.maybeMoveTarget(ultimateTarget, statement, statements);
            target.removeSource(statement);
            statement.replaceTarget(target, ultimateTarget);
            ultimateTarget.addSource(statement);
        }
    }

    private static void extractExceptionJumps(Op03SimpleStatement tryi, List<Op03SimpleStatement> in) {
        Op03SimpleStatement next;
        List<Op03SimpleStatement> tryTargets = tryi.getTargets();
        Op03SimpleStatement uniqueForwardTarget = null;
        Set relevantBlocks = SetFactory.newSet();
        Op03SimpleStatement lastEnd = null;
        int lpidx = 0;
        for (Op03SimpleStatement tgt : tryTargets) {
            BlockIdentifier block = Op03SimpleStatement.getBlockStart((lpidx++ == 0 ? tryi : tgt).getStatement());
            if (block == null) {
                return;
            }
            relevantBlocks.add((BlockIdentifier)block);
            Op03SimpleStatement lastStatement = Op03SimpleStatement.getLastContiguousBlockStatement(block, in, tgt);
            if (lastStatement == null) {
                return;
            }
            if (lastStatement.getStatement().getClass() == GotoStatement.class) {
                Op03SimpleStatement lastTgt = lastStatement.getTargets().get(0);
                if (uniqueForwardTarget != null) {
                    if (uniqueForwardTarget != lastTgt) {
                        return;
                    }
                } else {
                    uniqueForwardTarget = lastTgt;
                }
            }
            lastEnd = lastStatement;
        }
        if (uniqueForwardTarget == null) {
            return;
        }
        if (!uniqueForwardTarget.getBlockIdentifiers().equals(tryi.getBlockIdentifiers())) {
            return;
        }
        int idx = in.indexOf(lastEnd);
        if (idx >= in.size() - 1) {
            return;
        }
        if ((next = in.get(idx + 1)) == uniqueForwardTarget) {
            return;
        }
        for (Op03SimpleStatement source : next.getSources()) {
            if (!SetUtil.hasIntersection(source.getBlockIdentifiers(), relevantBlocks)) continue;
            return;
        }
        LinkedList blockSources = ListFactory.newLinkedList();
        for (Op03SimpleStatement source2 : uniqueForwardTarget.getSources()) {
            if (!SetUtil.hasIntersection(source2.getBlockIdentifiers(), relevantBlocks)) continue;
            blockSources.add((Op03SimpleStatement)source2);
        }
        Op03SimpleStatement indirect = new Op03SimpleStatement(next.getBlockIdentifiers(), new GotoStatement(), next.getIndex().justBefore());
        Iterator i$ = blockSources.iterator();
        while (i$.hasNext()) {
            Statement srcStatement;
            Op03SimpleStatement source3;
            if (srcStatement = (source3 = (Op03SimpleStatement)i$.next()).getStatement() instanceof GotoStatement) {
                ((GotoStatement)srcStatement).setJumpType(JumpType.GOTO_OUT_OF_TRY);
            }
            uniqueForwardTarget.removeSource(source3);
            source3.replaceTarget(uniqueForwardTarget, indirect);
            indirect.addSource(source3);
        }
        indirect.addTarget(uniqueForwardTarget);
        uniqueForwardTarget.addSource(indirect);
        in.add(idx + 1, indirect);
    }

    private static BlockIdentifier getBlockStart(Statement statement) {
        Class clazz = statement.getClass();
        if (clazz == TryStatement.class) {
            TryStatement tryStatement = (TryStatement)statement;
            return tryStatement.getBlockIdentifier();
        }
        if (clazz == CatchStatement.class) {
            CatchStatement catchStatement = (CatchStatement)statement;
            return catchStatement.getCatchBlockIdent();
        }
        if (clazz != FinallyStatement.class) return null;
        FinallyStatement finallyStatement = (FinallyStatement)statement;
        return finallyStatement.getFinallyBlockIdent();
    }

    public static void extractExceptionJumps(List<Op03SimpleStatement> in) {
        List<Op03SimpleStatement> tries = Functional.filter(in, new TypeFilter(TryStatement.class));
        for (Op03SimpleStatement tryi : tries) {
            Op03SimpleStatement.extractExceptionJumps(tryi, in);
        }
    }

    private static Op03SimpleStatement maybeMoveTarget(Op03SimpleStatement expectedRetarget, Op03SimpleStatement source, List<Op03SimpleStatement> statements) {
        int startIdx;
        if (expectedRetarget.getBlockIdentifiers().equals(source.getBlockIdentifiers())) {
            return expectedRetarget;
        }
        int idx = startIdx = statements.indexOf(expectedRetarget);
        Op03SimpleStatement maybe = null;
        while (idx <= 0 || !(statements.get(--idx).getStatement() instanceof TryStatement) || (maybe = statements.get(idx)).getBlockIdentifiers().equals(source.getBlockIdentifiers())) {
        }
        if (maybe != null) return maybe;
        return expectedRetarget;
    }

    public static void rewriteNegativeJumps(List<Op03SimpleStatement> statements) {
        List removeThese = ListFactory.newList();
        for (int x = 0; x < statements.size() - 2; ++x) {
            Op03SimpleStatement aStatement;
            Statement innerZStatement;
            Statement innerAStatement;
            if (!(innerAStatement = (aStatement = statements.get(x)).getStatement() instanceof IfStatement)) continue;
            Op03SimpleStatement zStatement = statements.get(x + 1);
            Op03SimpleStatement xStatement = statements.get(x + 2);
            if (aStatement.targets.get(0) != zStatement || aStatement.targets.get(1) != xStatement || (innerZStatement = zStatement.getStatement()).getClass() != GotoStatement.class) continue;
            Op03SimpleStatement yStatement = zStatement.targets.get(0);
            aStatement.replaceTarget(xStatement, yStatement);
            aStatement.replaceTarget(zStatement, xStatement);
            yStatement.replaceSource(zStatement, aStatement);
            zStatement.sources.clear();
            zStatement.targets.clear();
            zStatement.containedStatement = new Nop();
            removeThese.add((Op03SimpleStatement)zStatement);
            IfStatement innerAIfStatement = (IfStatement)innerAStatement;
            innerAIfStatement.negateCondition();
        }
        statements.removeAll(removeThese);
    }

    private static boolean isDirectParentWithoutPassing(Op03SimpleStatement child, Op03SimpleStatement parent, Op03SimpleStatement barrier) {
        LinkedList tests = ListFactory.newLinkedList();
        Set seen = SetFactory.newSet();
        tests.add((Op03SimpleStatement)child);
        seen.add((Op03SimpleStatement)child);
        boolean hitParent = false;
        while (!tests.isEmpty()) {
            Op03SimpleStatement node;
            if ((node = (Op03SimpleStatement)tests.removeFirst()) == barrier) continue;
            if (node == parent) {
                hitParent = true;
                continue;
            }
            List<Op03SimpleStatement> localParents = node.getSources();
            for (Op03SimpleStatement localParent : localParents) {
                if (!seen.add((Op03SimpleStatement)localParent)) continue;
                tests.add((Op03SimpleStatement)localParent);
            }
        }
        return hitParent;
    }

    private static Op03SimpleStatement getForInvariant(Op03SimpleStatement start, LValue invariant, BlockIdentifier whileLoop) {
        Op03SimpleStatement current = start;
        while (current.containedInBlocks.contains(whileLoop)) {
            LValue assigned;
            Op03SimpleStatement next;
            AbstractAssignment assignment;
            if (current.containedStatement instanceof AbstractAssignment && invariant.equals(assigned = (assignment = (AbstractAssignment)current.containedStatement).getCreatedLValue()) && assignment.isSelfMutatingOperation()) {
                return current;
            }
            if (current.sources.size() > 1) throw new ConfusedCFRException("Shouldn't be able to get here.");
            if (!current.index.isBackJumpTo(next = current.sources.get(0))) throw new ConfusedCFRException("Shouldn't be able to get here.");
            current = next;
        }
        throw new ConfusedCFRException("Shouldn't be able to get here.");
    }

    private static Set<LValue> findForInvariants(Op03SimpleStatement start, BlockIdentifier whileLoop) {
        Set res = SetFactory.newSet();
        Op03SimpleStatement current = start;
        while (current.containedInBlocks.contains(whileLoop)) {
            Op03SimpleStatement next;
            AbstractAssignment assignment;
            if (current.containedStatement instanceof AbstractAssignment && (assignment = (AbstractAssignment)current.containedStatement).isSelfMutatingOperation()) {
                res.add((LValue)assignment.getCreatedLValue());
            }
            if (current.sources.size() > 1) return res;
            if (!current.index.isBackJumpTo(next = current.sources.get(0))) return res;
            current = next;
        }
        return res;
    }

    private static Op03SimpleStatement findSingleBackSource(Op03SimpleStatement start) {
        List<Op03SimpleStatement> startSources = Functional.filter(start.sources, new IsForwardJumpTo(start.index));
        if (startSources.size() == 1) return startSources.get(0);
        Op03SimpleStatement.logger.info("** Too many back sources");
        return null;
    }

    private static Op03SimpleStatement findMovableAssignment(Op03SimpleStatement start, LValue lValue) {
        Op03SimpleStatement current = Op03SimpleStatement.findSingleBackSource(start);
        if (current == null) {
            return null;
        }
        do {
            AssignmentSimple assignmentSimple;
            if (current.containedStatement instanceof AssignmentSimple && (assignmentSimple = (AssignmentSimple)current.containedStatement).getCreatedLValue().equals(lValue)) {
                Expression rhs = assignmentSimple.getRValue();
                LValueUsageCollectorSimple lValueUsageCollector = new LValueUsageCollectorSimple();
                rhs.collectUsedLValues(lValueUsageCollector);
                if (SSAIdentifierUtils.isMovableUnder(lValueUsageCollector.getUsedLValues(), start.ssaIdentifiers, current.ssaIdentifiers)) {
                    return current;
                }
                Op03SimpleStatement.logger.info("** incompatible sources");
                return null;
            }
            if (current.sources.size() == 1) continue;
            Op03SimpleStatement.logger.info("** too many sources");
            return null;
        } while ((current = current.sources.get(0)) != null);
        return null;
    }

    private static void rewriteWhileAsFor(Op03SimpleStatement statement, List<Op03SimpleStatement> statements) {
        Op03SimpleStatement incrStatement;
        List<Op03SimpleStatement> backSources = Functional.filter(statement.sources, new IsBackJumpTo(statement.index));
        WhileStatement whileStatement = (WhileStatement)statement.containedStatement;
        ConditionalExpression condition = whileStatement.getCondition();
        Set<LValue> loopVariablePossibilities = condition.getLoopLValues();
        if (loopVariablePossibilities.isEmpty()) {
            Op03SimpleStatement.logger.info("No loop variable possibilities\n");
            return;
        }
        BlockIdentifier whileBlockIdentifier = whileStatement.getBlockIdentifier();
        Set<LValue> mutatedPossibilities = null;
        for (Op03SimpleStatement source : backSources) {
            Set<LValue> incrPoss = Op03SimpleStatement.findForInvariants(source, whileBlockIdentifier);
            if (mutatedPossibilities == null) {
                mutatedPossibilities = incrPoss;
            } else {
                mutatedPossibilities.retainAll(incrPoss);
            }
            if (!mutatedPossibilities.isEmpty()) continue;
            Op03SimpleStatement.logger.info("No invariant possibilities on source\n");
            return;
        }
        if (mutatedPossibilities == null || mutatedPossibilities.isEmpty()) {
            Op03SimpleStatement.logger.info("No invariant intersection\n");
            return;
        }
        loopVariablePossibilities.retainAll(mutatedPossibilities);
        if (loopVariablePossibilities.isEmpty()) {
            Op03SimpleStatement.logger.info("No invariant intersection\n");
            return;
        }
        if (loopVariablePossibilities.size() > 1) {
            Op03SimpleStatement.logger.info("Multiple invariant intersection\n");
            return;
        }
        LValue loopVariable = loopVariablePossibilities.iterator().next();
        List mutations = ListFactory.newList();
        for (Op03SimpleStatement source2 : backSources) {
            incrStatement = Op03SimpleStatement.getForInvariant(source2, loopVariable, whileBlockIdentifier);
            mutations.add((Op03SimpleStatement)incrStatement);
        }
        Op03SimpleStatement baseline = (Op03SimpleStatement)mutations.get(0);
        Iterator i$ = mutations.iterator();
        while (i$.hasNext()) {
            if (baseline.equals(incrStatement = (Op03SimpleStatement)i$.next())) continue;
            Op03SimpleStatement.logger.info("Incompatible constant mutations.");
            return;
        }
        Op03SimpleStatement initialValue = Op03SimpleStatement.findMovableAssignment(statement, loopVariable);
        AssignmentSimple initalAssignmentSimple = null;
        if (initialValue != null) {
            initalAssignmentSimple = (AssignmentSimple)initialValue.containedStatement;
            initialValue.nopOut();
        }
        AbstractAssignment updateAssignment = (AbstractAssignment)baseline.containedStatement;
        for (Op03SimpleStatement incrStatement2 : mutations) {
            incrStatement2.nopOut();
        }
        whileBlockIdentifier.setBlockType(BlockType.FORLOOP);
        whileStatement.replaceWithForLoop(initalAssignmentSimple, updateAssignment.getInliningExpression());
        for (Op03SimpleStatement source3 : backSources) {
            if (!source3.containedInBlocks.contains(whileBlockIdentifier)) continue;
            List<Op03SimpleStatement> ssources = ListFactory.newList(source3.getSources());
            for (Op03SimpleStatement ssource : ssources) {
                Statement sstatement;
                JumpingStatement jumpingStatement;
                if (!ssource.containedInBlocks.contains(whileBlockIdentifier) || !(sstatement = ssource.getStatement() instanceof JumpingStatement) || (jumpingStatement = (JumpingStatement)sstatement).getJumpTarget().getContainer() != source3) continue;
                ((JumpingStatement)sstatement).setJumpType(JumpType.CONTINUE);
                ssource.replaceTarget(source3, statement);
                statement.addSource(ssource);
                source3.removeSource(ssource);
            }
        }
    }

    public static void rewriteWhilesAsFors(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> whileStarts = Functional.filter(statements, new Predicate<Op03SimpleStatement>(){

            @Override
            public boolean test(Op03SimpleStatement in) {
                return in.containedStatement instanceof WhileStatement && ((WhileStatement)in.containedStatement).getBlockIdentifier().getBlockType() == BlockType.WHILELOOP;
            }
        });
        for (Op03SimpleStatement whileStart : whileStarts) {
            Op03SimpleStatement.rewriteWhileAsFor(whileStart, statements);
        }
    }

    private static void rewriteDoWhileTruePredAsWhile(Op03SimpleStatement end, List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> endTargets;
        WhileStatement whileStatement = (WhileStatement)end.getStatement();
        if (null != whileStatement.getCondition()) {
            return;
        }
        if ((endTargets = end.getTargets()).size() != 1) {
            return;
        }
        Op03SimpleStatement loopStart = endTargets.get(0);
        Statement loopBodyStartStatement = loopStart.getStatement();
        BlockIdentifier whileBlockIdentifier = whileStatement.getBlockIdentifier();
        Op03SimpleStatement doStart = null;
        Iterator<Op03SimpleStatement> i$ = loopStart.getSources().iterator();
        while (i$.hasNext()) {
            Statement statement;
            DoStatement doStatement;
            Op03SimpleStatement source;
            if ((statement = (source = i$.next()).getStatement()).getClass() != DoStatement.class || (doStatement = (DoStatement)statement).getBlockIdentifier() != whileBlockIdentifier) continue;
            doStart = source;
        }
        if (doStart == null) {
            return;
        }
        if (loopBodyStartStatement.getClass() == IfStatement.class) {
            return;
        }
        if (loopBodyStartStatement.getClass() != IfExitingStatement.class) return;
        IfExitingStatement ifExitingStatement = (IfExitingStatement)loopBodyStartStatement;
        Statement exitStatement = ifExitingStatement.getExitStatement();
        ConditionalExpression conditionalExpression = ifExitingStatement.getCondition();
        WhileStatement replacementWhile = new WhileStatement(conditionalExpression.getNegated(), whileBlockIdentifier);
        GotoStatement endGoto = new GotoStatement();
        endGoto.setJumpType(JumpType.CONTINUE);
        end.replaceStatement(endGoto);
        Op03SimpleStatement after = new Op03SimpleStatement(doStart.getBlockIdentifiers(), exitStatement, end.getIndex().justAfter());
        int endIdx = statements.indexOf(end);
        if (endIdx < statements.size() - 2) {
            Op03SimpleStatement shuffled = statements.get(endIdx + 1);
            for (Op03SimpleStatement shuffledSource : shuffled.sources) {
                JumpingStatement jumpingStatement;
                if (!(shuffledSource.getStatement() instanceof JumpingStatement) || (jumpingStatement = (JumpingStatement)shuffledSource.getStatement()).getJumpType() != JumpType.BREAK) continue;
                jumpingStatement.setJumpType(JumpType.GOTO);
            }
        }
        statements.add(endIdx + 1, after);
        doStart.addTarget(after);
        after.addSource(doStart);
        doStart.replaceStatement(replacementWhile);
        Op03SimpleStatement afterLoopStart = loopStart.getTargets().get(0);
        doStart.replaceTarget(loopStart, afterLoopStart);
        afterLoopStart.replaceSource(loopStart, doStart);
        loopStart.removeSource(doStart);
        loopStart.removeTarget(afterLoopStart);
        for (Op03SimpleStatement otherSource : loopStart.getSources()) {
            otherSource.replaceTarget(loopStart, doStart);
            doStart.addSource(otherSource);
        }
        loopStart.getSources().clear();
        loopStart.nopOut();
        whileBlockIdentifier.setBlockType(BlockType.WHILELOOP);
    }

    public static void rewriteDoWhileTruePredAsWhile(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> doWhileEnds = Functional.filter(statements, new Predicate<Op03SimpleStatement>(){

            @Override
            public boolean test(Op03SimpleStatement in) {
                return in.containedStatement instanceof WhileStatement && ((WhileStatement)in.containedStatement).getBlockIdentifier().getBlockType() == BlockType.UNCONDITIONALDOLOOP;
            }
        });
        if (doWhileEnds.isEmpty()) {
            return;
        }
        for (Op03SimpleStatement whileEnd : doWhileEnds) {
            Op03SimpleStatement.rewriteDoWhileTruePredAsWhile(whileEnd, statements);
        }
    }

    public static void rewriteBreakStatements(List<Op03SimpleStatement> statements) {
        Op03SimpleStatement.reindexInPlace(statements);
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            JumpingStatement jumpingStatement;
            Statement innerStatement;
            Set<BlockIdentifier> blocksEnded;
            BlockIdentifier outermostContainedIn;
            Op03SimpleStatement statement;
            if (!(innerStatement = (statement = i$.next()).getStatement() instanceof JumpingStatement) || !(jumpingStatement = (JumpingStatement)innerStatement).getJumpType().isUnknown()) continue;
            Statement targetInnerStatement = jumpingStatement.getJumpTarget();
            Op03SimpleStatement targetStatement = (Op03SimpleStatement)targetInnerStatement.getContainer();
            if (targetStatement.thisComparisonBlock != null) {
                BlockType blockType = targetStatement.thisComparisonBlock.getBlockType();
                int n = 26.$SwitchMap$org$benf$cfr$reader$bytecode$analysis$parse$utils$BlockType[blockType.ordinal()];
                if (BlockIdentifier.blockIsOneOf(targetStatement.thisComparisonBlock, statement.containedInBlocks)) {
                    jumpingStatement.setJumpType(JumpType.CONTINUE);
                    continue;
                }
            }
            if (targetStatement.getBlockStarted() != null && targetStatement.getBlockStarted().getBlockType() == BlockType.UNCONDITIONALDOLOOP && BlockIdentifier.blockIsOneOf(targetStatement.getBlockStarted(), statement.containedInBlocks)) {
                jumpingStatement.setJumpType(JumpType.CONTINUE);
                continue;
            }
            if ((blocksEnded = targetStatement.getBlocksEnded()).isEmpty() || (outermostContainedIn = BlockIdentifier.getOutermostContainedIn(blocksEnded, statement.containedInBlocks)) == null) continue;
            jumpingStatement.setJumpType(JumpType.BREAK);
        }
    }

    private static boolean classifyTryCatchLeaveGoto(Op03SimpleStatement gotoStm, Set<BlockIdentifier> blocks, int idx, Set<BlockIdentifier> tryBlockIdents, Map<BlockIdentifier, Op03SimpleStatement> tryStatementsByBlock, Map<BlockIdentifier, List<BlockIdentifier>> catchStatementByBlock, List<Op03SimpleStatement> in) {
        Set<BlockIdentifier> afterBlocks;
        Set<BlockIdentifier> tryBlocks;
        Op03SimpleStatement after;
        int idxtgt;
        Op03SimpleStatement prev;
        List<BlockIdentifier> catchForThis;
        Op03SimpleStatement gotoTgt;
        Op03SimpleStatement tryStatement;
        BlockIdentifier left;
        Set<BlockIdentifier> gotoTgtIdents;
        if (idx >= in.size() - 1) {
            return false;
        }
        GotoStatement gotoStatement = (GotoStatement)gotoStm.getStatement();
        if ((tryBlocks = SetUtil.intersectionOrNull(blocks, tryBlockIdents)) == null) {
            return false;
        }
        if ((afterBlocks = SetUtil.intersectionOrNull((after = in.get(idx + 1)).getBlockIdentifiers(), tryBlockIdents)) != null) {
            tryBlocks.removeAll(afterBlocks);
        }
        if (tryBlocks.size() != 1) {
            return false;
        }
        if ((tryStatement = tryStatementsByBlock.get(left = tryBlocks.iterator().next())) == null) {
            return false;
        }
        if ((catchForThis = catchStatementByBlock.get(left)) == null) {
            return false;
        }
        if (SetUtil.hasIntersection(gotoTgtIdents = (gotoTgt = gotoStm.getTargets().get(0)).getBlockIdentifiers(), catchForThis)) {
            return false;
        }
        if ((idxtgt = in.indexOf(gotoTgt)) == 0) {
            return false;
        }
        if (!SetUtil.hasIntersection((prev = in.get(idxtgt - 1)).getBlockIdentifiers(), catchForThis)) {
            return false;
        }
        gotoStatement.setJumpType(JumpType.GOTO_OUT_OF_TRY);
        return true;
    }

    private static boolean classifyTryLeaveGoto(Op03SimpleStatement gotoStm, int idx, Set<BlockIdentifier> tryBlockIdents, Map<BlockIdentifier, Op03SimpleStatement> tryStatementsByBlock, Map<BlockIdentifier, List<BlockIdentifier>> catchStatementByBlock, List<Op03SimpleStatement> in) {
        Set<BlockIdentifier> blocks = gotoStm.getBlockIdentifiers();
        return Op03SimpleStatement.classifyTryCatchLeaveGoto(gotoStm, blocks, idx, tryBlockIdents, tryStatementsByBlock, catchStatementByBlock, in);
    }

    private static boolean classifyCatchLeaveGoto(Op03SimpleStatement gotoStm, int idx, Set<BlockIdentifier> tryBlockIdents, Map<BlockIdentifier, Op03SimpleStatement> tryStatementsByBlock, Map<BlockIdentifier, List<BlockIdentifier>> catchStatementByBlock, Map<BlockIdentifier, Set<BlockIdentifier>> catchBlockToTryBlocks, List<Op03SimpleStatement> in) {
        Set<BlockIdentifier> inBlocks = gotoStm.getBlockIdentifiers();
        Set blocks = SetFactory.newOrderedSet();
        for (BlockIdentifier block : inBlocks) {
            if (!catchBlockToTryBlocks.containsKey(block)) continue;
            Set<BlockIdentifier> catchToTries = catchBlockToTryBlocks.get(block);
            blocks.addAll(catchToTries);
        }
        return Op03SimpleStatement.classifyTryCatchLeaveGoto(gotoStm, blocks, idx, tryBlockIdents, tryStatementsByBlock, catchStatementByBlock, in);
    }

    public static boolean classifyGotos(List<Op03SimpleStatement> in) {
        Op03SimpleStatement stm;
        boolean result = false;
        List gotos = ListFactory.newList();
        Map tryStatementsByBlock = MapFactory.newMap();
        Map catchStatementsByBlock = MapFactory.newMap();
        Map catchToTries = MapFactory.newLazyMap(new UnaryFunction<BlockIdentifier, Set<BlockIdentifier>>(){

            @Override
            public Set<BlockIdentifier> invoke(BlockIdentifier arg) {
                return SetFactory.newOrderedSet();
            }
        });
        int len = in.size();
        for (int x = 0; x < len; ++x) {
            Class clz;
            Statement statement;
            GotoStatement gotoStatement;
            if ((clz = (statement = (stm = in.get(x)).getStatement()).getClass()) == TryStatement.class) {
                TryStatement tryStatement = (TryStatement)statement;
                BlockIdentifier tryBlockIdent = tryStatement.getBlockIdentifier();
                tryStatementsByBlock.put((BlockIdentifier)tryBlockIdent, (Op03SimpleStatement)stm);
                List<Op03SimpleStatement> targets = stm.getTargets();
                List catchBlocks = ListFactory.newList();
                catchStatementsByBlock.put((BlockIdentifier)tryStatement.getBlockIdentifier(), catchBlocks);
                int len2 = targets.size();
                for (int y = 1; y < len2; ++y) {
                    Statement statement2;
                    if ((statement2 = targets.get(y).getStatement()).getClass() != CatchStatement.class) continue;
                    BlockIdentifier catchBlockIdent = ((CatchStatement)statement2).getCatchBlockIdent();
                    catchBlocks.add((BlockIdentifier)catchBlockIdent);
                    ((Set)catchToTries.get(catchBlockIdent)).add(tryBlockIdent);
                }
                continue;
            }
            if (clz != GotoStatement.class || !(gotoStatement = (GotoStatement)statement).getJumpType().isUnknown()) continue;
            gotos.add(Pair.make(stm, x));
        }
        if (tryStatementsByBlock.isEmpty()) return result;
        for (Pair goto_ : gotos) {
            int idx;
            stm = (Op03SimpleStatement)goto_.getFirst();
            if (!Op03SimpleStatement.classifyTryLeaveGoto(stm, idx = ((Integer)goto_.getSecond()).intValue(), tryStatementsByBlock.keySet(), tryStatementsByBlock, catchStatementsByBlock, in) && !Op03SimpleStatement.classifyCatchLeaveGoto(stm, idx, tryStatementsByBlock.keySet(), tryStatementsByBlock, catchStatementsByBlock, catchToTries, in)) continue;
            result = true;
        }
        return result;
    }

    public static boolean classifyAnonymousBlockGotos(List<Op03SimpleStatement> in) {
        boolean result = false;
        Iterator<Op03SimpleStatement> i$ = in.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement targetStatement;
            Set<BlockIdentifier> targetBlocks;
            Statement inner;
            JumpingStatement jumpingStatement;
            Op03SimpleStatement statement;
            boolean isForwardJump;
            JumpType jumpType;
            Set<BlockIdentifier> srcBlocks;
            if (!(inner = (statement = i$.next()).getStatement() instanceof JumpingStatement)) continue;
            if ((jumpType = (jumpingStatement = (JumpingStatement)inner).getJumpType()) != JumpType.GOTO) continue;
            if (!(isForwardJump = (targetStatement = (Op03SimpleStatement)jumpingStatement.getJumpTarget().getContainer()).getIndex().isBackJumpTo(statement)) || (targetBlocks = targetStatement.getBlockIdentifiers()).size() >= (srcBlocks = statement.getBlockIdentifiers()).size() || !srcBlocks.containsAll(targetBlocks) || targetBlocks.size() >= (srcBlocks = Functional.filterSet(srcBlocks, new Predicate<BlockIdentifier>(){

                @Override
                public boolean test(BlockIdentifier in) {
                    BlockType blockType = in.getBlockType();
                    if (blockType == BlockType.CASE) {
                        return false;
                    }
                    if (blockType != BlockType.SWITCH) return true;
                    return false;
                }
            })).size() || !srcBlocks.containsAll(targetBlocks)) continue;
            jumpingStatement.setJumpType(JumpType.BREAK_ANONYMOUS);
            result = true;
        }
        return result;
    }

    public static Op04StructuredStatement createInitialStructuredBlock(List<Op03SimpleStatement> statements) {
        GraphConversionHelper conversionHelper = new GraphConversionHelper();
        List containers = ListFactory.newList();
        for (Op03SimpleStatement statement : statements) {
            Op04StructuredStatement unstructuredStatement = statement.getStructuredStatementPlaceHolder();
            containers.add((Op04StructuredStatement)unstructuredStatement);
            conversionHelper.registerOriginalAndNew(statement, unstructuredStatement);
        }
        conversionHelper.patchUpRelations();
        return Op04StructuredStatement.buildNestedBlocks(containers);
    }

    public static void identifyLoops1(Method method, List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory) {
        List<Op03SimpleStatement> pathtests = Functional.filter(statements, new TypeFilter(GotoStatement.class));
        for (Op03SimpleStatement start : pathtests) {
            Op03SimpleStatement.considerAsPathologicalLoop(start, statements);
        }
        List<Op03SimpleStatement> backjumps = Functional.filter(statements, new HasBackJump(null));
        List starts = Functional.uniqAll(Functional.map(backjumps, new GetBackJump(null)));
        Map blockEndsCache = MapFactory.newMap();
        Collections.sort(starts, new CompareByIndex());
        List loopResults = ListFactory.newList();
        Set relevantBlocks = SetFactory.newSet();
        Iterator i$ = starts.iterator();
        while (i$.hasNext()) {
            BlockIdentifier blockIdentifier;
            Op03SimpleStatement start2;
            if ((blockIdentifier = Op03SimpleStatement.considerAsWhileLoopStart(method, start2 = (Op03SimpleStatement)i$.next(), statements, blockIdentifierFactory, blockEndsCache)) == null) {
                blockIdentifier = Op03SimpleStatement.considerAsDoLoopStart(start2, statements, blockIdentifierFactory, blockEndsCache);
            }
            if (blockIdentifier == null) continue;
            loopResults.add((LoopResult)new LoopResult(blockIdentifier, start2, null));
            relevantBlocks.add((BlockIdentifier)blockIdentifier);
        }
        if (loopResults.isEmpty()) {
            return;
        }
        Collections.reverse(loopResults);
        Op03SimpleStatement.fixLoopOverlaps(statements, loopResults, relevantBlocks);
    }

    private static void fixLoopOverlaps(List<Op03SimpleStatement> statements, List<LoopResult> loopResults, Set<BlockIdentifier> relevantBlocks) {
        Map requiredExtents = MapFactory.newLazyMap(new UnaryFunction<BlockIdentifier, List<BlockIdentifier>>(){

            @Override
            public List<BlockIdentifier> invoke(BlockIdentifier arg) {
                return ListFactory.newList();
            }
        });
        Map lastForBlock = MapFactory.newMap();
        for (LoopResult loopResult : loopResults) {
            List<Op03SimpleStatement> backSources;
            Set<BlockIdentifier> backIn;
            Op03SimpleStatement start = loopResult.blockStart;
            BlockIdentifier testBlockIdentifier = loopResult.blockIdentifier;
            Set<BlockIdentifier> startIn = SetUtil.intersectionOrNull(start.getBlockIdentifiers(), relevantBlocks);
            if ((backSources = Functional.filter(start.sources, new Predicate<Op03SimpleStatement>(testBlockIdentifier, start){
                final /* synthetic */ BlockIdentifier val$testBlockIdentifier;
                final /* synthetic */ Op03SimpleStatement val$start;

                @Override
                public boolean test(Op03SimpleStatement in) {
                    return in.getBlockIdentifiers().contains(this.val$testBlockIdentifier) && in.getIndex().isBackJumpTo(this.val$start);
                }
            })).isEmpty()) continue;
            Collections.sort(backSources, new CompareByIndex());
            Op03SimpleStatement lastBackSource = backSources.get(backSources.size() - 1);
            lastForBlock.put((BlockIdentifier)testBlockIdentifier, (Op03SimpleStatement)lastBackSource);
            if (startIn == null) continue;
            if ((backIn = SetUtil.intersectionOrNull(lastBackSource.getBlockIdentifiers(), relevantBlocks)) == null) continue;
            if (backIn.containsAll(startIn)) continue;
            Set<BlockIdentifier> startMissing = SetFactory.newSet(startIn);
            startMissing.removeAll(backIn);
            for (BlockIdentifier missing : startMissing) {
                ((List)requiredExtents.get(missing)).add(testBlockIdentifier);
            }
        }
        if (requiredExtents.isEmpty()) {
            return;
        }
        List extendBlocks = ListFactory.newList(requiredExtents.keySet());
        Collections.sort(extendBlocks, new Comparator<BlockIdentifier>(){

            @Override
            public int compare(BlockIdentifier blockIdentifier, BlockIdentifier blockIdentifier2) {
                return blockIdentifier.getIndex() - blockIdentifier2.getIndex();
            }
        });
        CompareByIndex comparator = new CompareByIndex();
        Iterator i$ = extendBlocks.iterator();
        while (i$.hasNext()) {
            List possibleEnds;
            BlockIdentifier extendThis;
            if ((possibleEnds = (List)requiredExtents.get(extendThis = (BlockIdentifier)i$.next())).isEmpty()) continue;
            List possibleEndOps = ListFactory.newList();
            for (BlockIdentifier end : possibleEnds) {
                possibleEndOps.add(lastForBlock.get(end));
            }
            Collections.sort(possibleEndOps, comparator);
            Op03SimpleStatement extendTo = (Op03SimpleStatement)possibleEndOps.get(possibleEndOps.size() - 1);
            Op03SimpleStatement oldEnd = (Op03SimpleStatement)lastForBlock.get(extendThis);
            int start = statements.indexOf(oldEnd);
            int end2 = statements.indexOf(extendTo);
            for (int x = start; x <= end2; ++x) {
                statements.get(x).getBlockIdentifiers().add(extendThis);
            }
            Op03SimpleStatement.rewriteEndLoopOverlapStatement(oldEnd, extendThis);
        }
    }

    private static void rewriteEndLoopOverlapStatement(Op03SimpleStatement oldEnd, BlockIdentifier loopBlock) {
        Statement statement = oldEnd.getStatement();
        Class clazz = statement.getClass();
        if (clazz == WhileStatement.class) {
            WhileStatement whileStatement = (WhileStatement)statement;
            ConditionalExpression condition = whileStatement.getCondition();
            if (oldEnd.targets.size() == 2) {
                IfStatement repl = new IfStatement(condition);
                repl.setKnownBlocks(loopBlock, null);
                repl.setJumpType(JumpType.CONTINUE);
                oldEnd.replaceStatement(repl);
                if (oldEnd.thisComparisonBlock != loopBlock) return;
                oldEnd.thisComparisonBlock = null;
                return;
            }
            if (oldEnd.targets.size() != 1 || condition != null) return;
            GotoStatement repl = new GotoStatement();
            repl.setJumpType(JumpType.CONTINUE);
            oldEnd.replaceStatement(repl);
            if (oldEnd.thisComparisonBlock != loopBlock) return;
            oldEnd.thisComparisonBlock = null;
            return;
        }
        boolean x = true;
    }

    private static Op03SimpleStatement findFirstConditional(Op03SimpleStatement start) {
        Set visited = SetFactory.newSet();
        do {
            List<Op03SimpleStatement> targets;
            Statement innerStatement;
            if (innerStatement = start.getStatement() instanceof IfStatement) {
                return start;
            }
            if ((targets = start.getTargets()).size() != 1) {
                return null;
            }
            if (visited.contains(start = targets.get(0))) {
                return null;
            }
            visited.add((Op03SimpleStatement)start);
        } while (start != null);
        return null;
    }

    public static List<Op03SimpleStatement> pushThroughGoto(Method method, List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> pathtests = Functional.filter(statements, new ExactTypeFilter(GotoStatement.class));
        boolean success = false;
        for (Op03SimpleStatement gotostm : pathtests) {
            if (!gotostm.getTargets().get(0).getIndex().isBackJumpTo(gotostm) || !Op03SimpleStatement.pushThroughGoto(method, gotostm, statements)) continue;
            success = true;
        }
        if (!success) return statements;
        statements = Op03SimpleStatement.renumber(statements);
        Op03SimpleStatement.rewriteNegativeJumps(statements);
        Op03SimpleStatement.rewriteNegativeJumps(statements);
        return statements;
    }

    private static boolean moveable(Statement statement) {
        Class clazz = statement.getClass();
        if (clazz == Nop.class) {
            return true;
        }
        if (clazz == AssignmentSimple.class) {
            return true;
        }
        if (clazz == CommentStatement.class) {
            return true;
        }
        if (clazz != ExpressionStatement.class) return false;
        return true;
    }

    private static boolean pushThroughGoto(Method method, Op03SimpleStatement forwardGoto, List<Op03SimpleStatement> statements) {
        Op03SimpleStatement tgt;
        Op03SimpleStatement before;
        static class IsLoopBlock
        implements Predicate<BlockIdentifier> {
            IsLoopBlock() {
            }

            @Override
            public boolean test(BlockIdentifier in) {
                BlockType blockType = in.getBlockType();
                switch (blockType) {
                    case WHILELOOP: 
                    case DOLOOP: {
                        return true;
                    }
                }
                return false;
            }
        }
        static class IsExceptionBlock
        implements Predicate<BlockIdentifier> {
            IsExceptionBlock() {
            }

            @Override
            public boolean test(BlockIdentifier in) {
                BlockType blockType = in.getBlockType();
                switch (blockType) {
                    case TRYBLOCK: 
                    case SWITCH: 
                    case CATCHBLOCK: 
                    case CASE: {
                        return true;
                    }
                }
                return false;
            }
        }
        Set<BlockIdentifier> tgtLoopBlocks;
        int idx;
        if (forwardGoto.sources.size() != 1) {
            return false;
        }
        if ((idx = statements.indexOf(tgt = forwardGoto.getTargets().get(0))) == 0) {
            return false;
        }
        if (tgt.getSources().contains(before = statements.get(idx - 1))) {
            return false;
        }
        InstrIndex beforeTgt = tgt.getIndex().justBefore();
        Op03SimpleStatement last = forwardGoto;
        IsLoopBlock isLoopBlock = new IsLoopBlock();
        Set<BlockIdentifier> beforeLoopBlocks = SetFactory.newSet(Functional.filterSet(before.getBlockIdentifiers(), isLoopBlock));
        if (!beforeLoopBlocks.equals(tgtLoopBlocks = SetFactory.newSet(Functional.filterSet(tgt.getBlockIdentifiers(), isLoopBlock)))) {
            return false;
        }
        before.getBlockIdentifiers();
        IsExceptionBlock exceptionFilter = new IsExceptionBlock();
        Set<BlockIdentifier> exceptionBlocks = SetFactory.newSet(Functional.filterSet(tgt.getBlockIdentifiers(), exceptionFilter));
        int nextCandidateIdx = statements.indexOf(forwardGoto) - 1;
        Op03SimpleStatement lastTarget = tgt;
        Set seen = SetFactory.newSet();
        boolean success = false;
        Op03SimpleStatement tryMoveThis;
        while (Op03SimpleStatement.moveable((tryMoveThis = forwardGoto.sources.get(0)).getStatement())) {
            Set<BlockIdentifier> moveEB;
            if (!seen.add((Op03SimpleStatement)tryMoveThis)) {
                return success;
            }
            if (statements.get(nextCandidateIdx) != tryMoveThis) {
                return success;
            }
            if (tryMoveThis.targets.size() != 1) {
                return success;
            }
            if (tryMoveThis.sources.size() != 1) {
                return success;
            }
            Op03SimpleStatement beforeTryMove = tryMoveThis.sources.get(0);
            if (!(moveEB = SetFactory.newSet(Functional.filterSet(forwardGoto.getBlockIdentifiers(), exceptionFilter))).equals(exceptionBlocks)) {
                return success;
            }
            beforeTryMove.replaceTarget(tryMoveThis, forwardGoto);
            forwardGoto.replaceSource(tryMoveThis, beforeTryMove);
            forwardGoto.replaceTarget(lastTarget, tryMoveThis);
            tryMoveThis.replaceSource(beforeTryMove, forwardGoto);
            tryMoveThis.replaceTarget(forwardGoto, lastTarget);
            lastTarget.replaceSource(forwardGoto, tryMoveThis);
            tryMoveThis.index = beforeTgt;
            beforeTgt = beforeTgt.justBefore();
            tryMoveThis.containedInBlocks.clear();
            tryMoveThis.containedInBlocks.addAll(lastTarget.containedInBlocks);
            lastTarget = tryMoveThis;
            --nextCandidateIdx;
            success = true;
        }
        return success;
    }

    public static void eclipseLoopPass(List<Op03SimpleStatement> statements) {
        boolean effect = false;
        int len = statements.size() - 1;
        for (int x = 0; x < len; ++x) {
            Statement tgtInr;
            Op03SimpleStatement statement;
            Statement inr;
            IfStatement ifStatement;
            Op03SimpleStatement bodyStart;
            if ((inr = (statement = statements.get(x)).getStatement()).getClass() != GotoStatement.class) continue;
            Op03SimpleStatement target = statement.getTargets().get(0);
            if (target == statement) continue;
            if (target.getIndex().isBackJumpFrom(statement)) continue;
            if ((tgtInr = target.getStatement()).getClass() != IfStatement.class) continue;
            if ((bodyStart = statements.get(x + 1)) != (ifStatement = (IfStatement)tgtInr).getJumpTarget().getContainer()) continue;
            Iterator<Op03SimpleStatement> i$ = target.getSources().iterator();
            while (i$.hasNext()) {
                Op03SimpleStatement source;
                InstrIndex sourceIdx;
                if (!(sourceIdx = (source = i$.next()).getIndex()).isBackJumpFrom(statement) && !sourceIdx.isBackJumpTo(target)) continue;
            }
            Op03SimpleStatement afterTest = target.getTargets().get(0);
            IfStatement topTest = new IfStatement(ifStatement.getCondition().getNegated().simplify());
            statement.replaceStatement(topTest);
            statement.replaceTarget(target, bodyStart);
            bodyStart.addSource(statement);
            statement.addTarget(afterTest);
            afterTest.replaceSource(target, statement);
            target.replaceStatement(new Nop());
            target.removeSource(statement);
            target.removeTarget(afterTest);
            target.replaceTarget(bodyStart, statement);
            target.replaceStatement(new GotoStatement());
            bodyStart.removeSource(target);
            statement.addSource(target);
            effect = true;
        }
        if (!effect) return;
        Op03SimpleStatement.removePointlessJumps(statements);
    }

    private static boolean considerAsPathologicalLoop(Op03SimpleStatement start, List<Op03SimpleStatement> statements) {
        if (start.containedStatement.getClass() != GotoStatement.class) {
            return false;
        }
        if (start.targets.get(0) != start) {
            return false;
        }
        Op03SimpleStatement next = new Op03SimpleStatement(start.getBlockIdentifiers(), new GotoStatement(), start.getIndex().justAfter());
        start.replaceStatement(new CommentStatement("Infinite loop"));
        start.replaceTarget(start, next);
        start.replaceSource(start, next);
        next.addSource(start);
        next.addTarget(start);
        statements.add(statements.indexOf(start) + 1, next);
        return true;
    }

    private static BlockIdentifier considerAsDoLoopStart(Op03SimpleStatement start, List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory, Map<BlockIdentifier, Op03SimpleStatement> postBlockCache) {
        Op03SimpleStatement postBlock;
        int endIdx;
        InstrIndex startIndex = start.getIndex();
        List<Op03SimpleStatement> backJumpSources = start.getSources();
        if (backJumpSources.isEmpty()) {
            throw new ConfusedCFRException("Node doesn't have ANY sources! " + start);
        }
        backJumpSources = Functional.filter(backJumpSources, new Predicate<Op03SimpleStatement>(startIndex){
            final /* synthetic */ InstrIndex val$startIndex;

            @Override
            public boolean test(Op03SimpleStatement in) {
                return in.getIndex().compareTo(this.val$startIndex) >= 0;
            }
        });
        Collections.sort(backJumpSources, new CompareByIndex());
        if (backJumpSources.isEmpty()) {
            throw new ConfusedCFRException("Node should have back jump sources.");
        }
        Op03SimpleStatement lastJump = backJumpSources.get(backJumpSources.size() - 1);
        boolean conditional = false;
        if (lastJump.containedStatement instanceof IfStatement) {
            IfStatement ifStatement;
            conditional = true;
            if ((ifStatement = (IfStatement)lastJump.containedStatement).getJumpTarget().getContainer() != start) {
                return null;
            }
        }
        int startIdx = statements.indexOf(start);
        if (startIdx >= (endIdx = statements.indexOf(lastJump))) {
            return null;
        }
        BlockIdentifier blockIdentifier = blockIdentifierFactory.getNextBlockIdentifier(conditional ? BlockType.DOLOOP : BlockType.UNCONDITIONALDOLOOP);
        try {
            Op03SimpleStatement.validateAndAssignLoopIdentifier(statements, startIdx, endIdx + 1, blockIdentifier, start);
        }
        catch (CannotPerformDecode e) {
            return null;
        }
        Op03SimpleStatement doStatement = new Op03SimpleStatement(start.containedInBlocks, new DoStatement(blockIdentifier), start.index.justBefore());
        doStatement.containedInBlocks.remove(blockIdentifier);
        List<Op03SimpleStatement> startSources = ListFactory.newList(start.sources);
        for (Op03SimpleStatement source : startSources) {
            if (source.containedInBlocks.contains(blockIdentifier)) continue;
            source.replaceTarget(start, doStatement);
            start.removeSource(source);
            doStatement.addSource(source);
        }
        doStatement.addTarget(start);
        start.addSource(doStatement);
        if (conditional) {
            postBlock = lastJump.getTargets().get(0);
        } else {
            int newIdx = statements.indexOf(lastJump) + 1;
            if (newIdx >= statements.size()) {
                postBlock = new Op03SimpleStatement(SetFactory.newSet(), new ReturnNothingStatement(), lastJump.getIndex().justAfter());
                statements.add(postBlock);
            } else {
                postBlock = statements.get(newIdx);
            }
        }
        if (start.firstStatementInThisBlock != null) {
            BlockIdentifier outer = Op03SimpleStatement.findOuterBlock(start.firstStatementInThisBlock, blockIdentifier, statements);
            if (blockIdentifier == outer) {
                throw new UnsupportedOperationException();
            }
            doStatement.firstStatementInThisBlock = start.firstStatementInThisBlock;
            start.firstStatementInThisBlock = blockIdentifier;
        }
        if (!conditional) {
            Set<BlockIdentifier> lastContent = SetFactory.newSet(lastJump.getBlockIdentifiers());
            lastContent.removeAll(start.getBlockIdentifiers());
            Set<BlockIdentifier> internalTryBlocks = SetFactory.newOrderedSet(Functional.filterSet(lastContent, new Predicate<BlockIdentifier>(){

                @Override
                public boolean test(BlockIdentifier in) {
                    return in.getBlockType() == BlockType.TRYBLOCK;
                }
            }));
            if (!internalTryBlocks.isEmpty()) {
                int postBlockIdx;
                int lastPostBlock = postBlockIdx = statements.indexOf(postBlock);
                while (lastPostBlock + 1 < statements.size()) {
                    List tryBlocks;
                    int currentIdx;
                    Op03SimpleStatement stm;
                    if (!((stm = statements.get(lastPostBlock)).getStatement() instanceof CatchStatement)) break;
                    CatchStatement catchStatement = (CatchStatement)stm.getStatement();
                    BlockIdentifier catchBlockIdent = catchStatement.getCatchBlockIdent();
                    if (!internalTryBlocks.containsAll(tryBlocks = Functional.map(catchStatement.getExceptions(), new UnaryFunction<ExceptionGroup.Entry, BlockIdentifier>(){

                        @Override
                        public BlockIdentifier invoke(ExceptionGroup.Entry arg) {
                            return arg.getTryBlockIdentifier();
                        }
                    }))) break;
                    for (currentIdx = lastPostBlock + 1; currentIdx < statements.size() - 1 && statements.get(currentIdx).getBlockIdentifiers().contains(catchBlockIdent); ++currentIdx) {
                    }
                    lastPostBlock = currentIdx;
                }
                if (lastPostBlock != postBlockIdx) {
                    Op03SimpleStatement afterNewJump = statements.get(lastPostBlock);
                    Op03SimpleStatement newBackJump = new Op03SimpleStatement(afterNewJump.getBlockIdentifiers(), new GotoStatement(), afterNewJump.getIndex().justBefore());
                    newBackJump.addTarget(start);
                    newBackJump.addSource(lastJump);
                    lastJump.replaceTarget(start, newBackJump);
                    start.replaceSource(lastJump, newBackJump);
                    Op03SimpleStatement preNewJump = statements.get(lastPostBlock - 1);
                    if (afterNewJump.getSources().contains(preNewJump)) {
                        Op03SimpleStatement interstit = new Op03SimpleStatement(preNewJump.getBlockIdentifiers(), new GotoStatement(), newBackJump.getIndex().justBefore());
                        preNewJump.replaceTarget(afterNewJump, interstit);
                        afterNewJump.replaceSource(preNewJump, interstit);
                        interstit.addSource(preNewJump);
                        interstit.addTarget(afterNewJump);
                        statements.add(lastPostBlock, interstit);
                        ++lastPostBlock;
                    }
                    statements.add(lastPostBlock, newBackJump);
                    lastJump = newBackJump;
                    postBlock = afterNewJump;
                    for (int idx = postBlockIdx; idx <= lastPostBlock; ++idx) {
                        statements.get(idx).markBlock(blockIdentifier);
                    }
                }
            }
        }
        statements.add(statements.indexOf(start), doStatement);
        lastJump.markBlockStatement(blockIdentifier, null, lastJump, statements);
        start.markFirstStatementInBlock(blockIdentifier);
        postBlockCache.put(blockIdentifier, postBlock);
        return blockIdentifier;
    }

    private static BlockIdentifier findOuterBlock(BlockIdentifier b1, BlockIdentifier b2, List<Op03SimpleStatement> statements) {
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            Set<BlockIdentifier> contained;
            Op03SimpleStatement s;
            if ((contained = (s = i$.next()).getBlockIdentifiers()).contains(b1)) {
                if (contained.contains(b2)) continue;
                return b1;
            }
            if (!contained.contains(b2)) continue;
            return b2;
        }
        return b1;
    }

    private static BlockIdentifier considerAsWhileLoopStart(Method method, Op03SimpleStatement start, List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory, Map<BlockIdentifier, Op03SimpleStatement> postBlockCache) {
        int idxAfterEnd;
        Op03SimpleStatement loopBreak;
        int lastIdx;
        InstrIndex startIndex = start.getIndex();
        List<Op03SimpleStatement> backJumpSources = start.getSources();
        backJumpSources = Functional.filter(backJumpSources, new Predicate<Op03SimpleStatement>(startIndex){
            final /* synthetic */ InstrIndex val$startIndex;

            @Override
            public boolean test(Op03SimpleStatement in) {
                return in.getIndex().compareTo(this.val$startIndex) >= 0;
            }
        });
        Collections.sort(backJumpSources, new CompareByIndex());
        Op03SimpleStatement conditional = Op03SimpleStatement.findFirstConditional(start);
        if (conditional == null) {
            Op03SimpleStatement.logger.info("Can't find a conditional");
            return null;
        }
        Op03SimpleStatement lastJump = backJumpSources.get(backJumpSources.size() - 1);
        List<Op03SimpleStatement> conditionalTargets = conditional.getTargets();
        if ((loopBreak = conditionalTargets.get(1)) == conditional && start == conditional) {
            Op03SimpleStatement backJump = new Op03SimpleStatement(conditional.getBlockIdentifiers(), new GotoStatement(), conditional.getIndex().justAfter());
            Op03SimpleStatement notTaken = conditional.targets.get(0);
            conditional.replaceTarget(notTaken, backJump);
            conditional.replaceSource(conditional, backJump);
            conditional.replaceTarget(conditional, notTaken);
            backJump.addSource(conditional);
            backJump.addTarget(conditional);
            statements.add(statements.indexOf(conditional) + 1, backJump);
            conditionalTargets = conditional.getTargets();
            loopBreak = notTaken;
        }
        if (loopBreak.getIndex().compareTo(lastJump.getIndex()) <= 0 && loopBreak.getIndex().compareTo(startIndex) >= 0) {
            return null;
        }
        if (start != conditional) {
            return null;
        }
        int idxConditional = statements.indexOf(start);
        if ((idxAfterEnd = statements.indexOf(loopBreak)) < idxConditional) {
            Op03SimpleStatement endOfOuter;
            Op03SimpleStatement startOfOuterLoop = statements.get(idxAfterEnd);
            if (startOfOuterLoop.thisComparisonBlock == null) {
                return null;
            }
            if ((endOfOuter = postBlockCache.get(startOfOuterLoop.thisComparisonBlock)) == null) {
                throw new ConfusedCFRException("BlockIdentifier doesn't exist in blockEndsCache");
            }
            idxAfterEnd = statements.indexOf(endOfOuter);
        }
        if (idxConditional >= idxAfterEnd) {
            return null;
        }
        BlockIdentifier blockIdentifier = blockIdentifierFactory.getNextBlockIdentifier(BlockType.WHILELOOP);
        try {
            lastIdx = Op03SimpleStatement.validateAndAssignLoopIdentifier(statements, idxConditional + 1, idxAfterEnd, blockIdentifier, start);
        }
        catch (CannotPerformDecode e) {
            return null;
        }
        Op03SimpleStatement lastInBlock = statements.get(lastIdx);
        Op03SimpleStatement blockEnd = statements.get(idxAfterEnd);
        start.markBlockStatement(blockIdentifier, lastInBlock, blockEnd, statements);
        statements.get(idxConditional + 1).markFirstStatementInBlock(blockIdentifier);
        postBlockCache.put(blockIdentifier, blockEnd);
        Op03SimpleStatement afterLastInBlock = lastIdx + 1 < statements.size() ? statements.get(lastIdx + 1) : null;
        loopBreak = conditional.getTargets().get(1);
        if (afterLastInBlock == loopBreak) return blockIdentifier;
        Op03SimpleStatement newAfterLast = new Op03SimpleStatement(afterLastInBlock.getBlockIdentifiers(), new GotoStatement(), lastInBlock.getIndex().justAfter());
        conditional.replaceTarget(loopBreak, newAfterLast);
        newAfterLast.addSource(conditional);
        loopBreak.replaceSource(conditional, newAfterLast);
        newAfterLast.addTarget(loopBreak);
        statements.add(newAfterLast);
        return blockIdentifier;
    }

    private static int getFarthestReachableInRange(List<Op03SimpleStatement> statements, int start, int afterEnd) {
        Map instrToIdx = MapFactory.newMap();
        for (int x = start; x < afterEnd; ++x) {
            Op03SimpleStatement statement = statements.get(x);
            instrToIdx.put((Op03SimpleStatement)statement, x);
        }
        Set reachableNodes = SetFactory.newSortedSet();
        GraphVisitorReachableInThese graphVisitorCallee = new GraphVisitorReachableInThese(reachableNodes, instrToIdx);
        GraphVisitorDFS<Op03SimpleStatement> visitor = new GraphVisitorDFS<Op03SimpleStatement>(statements.get(start), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)graphVisitorCallee);
        visitor.process();
        int first = start;
        int last = -1;
        boolean foundLast = false;
        for (int x2 = first; x2 < afterEnd; ++x2) {
            if (reachableNodes.contains(x2) || statements.get(x2).isNop()) {
                if (!foundLast) continue;
                throw new CannotPerformDecode("reachable test BLOCK was exited and re-entered.");
            }
            if (!foundLast) {
                last = x2 - 1;
            }
            foundLast = true;
        }
        if (last != -1) return last;
        last = afterEnd - 1;
        return last;
    }

    private static int validateAndAssignLoopIdentifier(List<Op03SimpleStatement> statements, int idxTestStart, int idxAfterEnd, BlockIdentifier blockIdentifier, Op03SimpleStatement start) {
        int last = Op03SimpleStatement.getFarthestReachableInRange(statements, idxTestStart, idxAfterEnd);
        Op03SimpleStatement discoveredLast = statements.get(last);
        Set<BlockIdentifier> lastBlocks = SetFactory.newSet(discoveredLast.containedInBlocks);
        lastBlocks.removeAll(start.getBlockIdentifiers());
        Set<BlockIdentifier> catches = SetFactory.newSet(Functional.filterSet(lastBlocks, new Predicate<BlockIdentifier>(){

            @Override
            public boolean test(BlockIdentifier in) {
                return in.getBlockType() == BlockType.CATCHBLOCK;
            }
        }));
        int newlast = last;
        while (!catches.isEmpty()) {
            Op03SimpleStatement stm = statements.get(newlast);
            catches.retainAll(stm.getBlockIdentifiers());
            if (catches.isEmpty()) break;
            last = newlast++;
            if (newlast < statements.size() - 1) continue;
        }
        for (int x = idxTestStart; x <= last; ++x) {
            statements.get(x).markBlock(blockIdentifier);
        }
        return last;
    }

    private JumpType getJumpType() {
        if (!(this.containedStatement instanceof JumpingStatement)) return JumpType.NONE;
        return ((JumpingStatement)this.containedStatement).getJumpType();
    }

    private static void markWholeBlock(List<Op03SimpleStatement> statements, BlockIdentifier blockIdentifier) {
        Op03SimpleStatement start = statements.get(0);
        start.markFirstStatementInBlock(blockIdentifier);
        for (Op03SimpleStatement statement : statements) {
            statement.markBlock(blockIdentifier);
        }
    }

    private static DiscoveredTernary testForTernary(List<Op03SimpleStatement> ifBranch, List<Op03SimpleStatement> elseBranch, Op03SimpleStatement leaveIfBranch) {
        if (ifBranch == null || elseBranch == null) {
            return null;
        }
        if (leaveIfBranch == null) {
            return null;
        }
        TypeFilter notNops = new TypeFilter(Nop.class, false);
        ifBranch = Functional.filter(ifBranch, notNops);
        switch (ifBranch.size()) {
            case 1: {
                break;
            }
            case 2: {
                if (ifBranch.get(1) == leaveIfBranch) break;
                return null;
            }
            default: {
                return null;
            }
        }
        elseBranch = Functional.filter(elseBranch, notNops);
        if (elseBranch.size() != 1) {
            return null;
        }
        Op03SimpleStatement s1 = ifBranch.get(0);
        Op03SimpleStatement s2 = elseBranch.get(0);
        if (s2.sources.size() != 1) {
            return null;
        }
        LValue l1 = s1.containedStatement.getCreatedLValue();
        LValue l2 = s2.containedStatement.getCreatedLValue();
        if (l1 == null || l2 == null) {
            return null;
        }
        if (l2.equals(l1)) return new DiscoveredTernary(l1, s1.containedStatement.getRValue(), s2.containedStatement.getRValue(), null);
        return null;
    }

    private static boolean considerAsTrivialIf(Op03SimpleStatement ifStatement, List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory, Set<Op03SimpleStatement> ignoreTheseJumps) {
        Op03SimpleStatement takenTarget = ifStatement.targets.get(1);
        Op03SimpleStatement notTakenTarget = ifStatement.targets.get(0);
        int idxTaken = statements.indexOf(takenTarget);
        int idxNotTaken = statements.indexOf(notTakenTarget);
        if (idxTaken != idxNotTaken + 1) {
            return false;
        }
        if (takenTarget.getStatement().getClass() != GotoStatement.class || notTakenTarget.getStatement().getClass() != GotoStatement.class || takenTarget.targets.get(0) != notTakenTarget.targets.get(0)) {
            return false;
        }
        notTakenTarget.replaceStatement(new CommentStatement("empty if block"));
        return false;
    }

    private static boolean considerAsDexIf(Op03SimpleStatement ifStatement, List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory, Set<Op03SimpleStatement> ignoreTheseJumps) {
        int cidx;
        int bidx;
        InstrIndex bIndex;
        Set<Op03SimpleStatement> permittedSources;
        int didx;
        Statement innerStatement = ifStatement.getStatement();
        if (innerStatement.getClass() != IfStatement.class) {
            return false;
        }
        IfStatement innerIfStatement = (IfStatement)innerStatement;
        int startIdx = statements.indexOf(ifStatement);
        if ((bidx = statements.indexOf(ifStatement.getTargets().get(1))) <= startIdx) {
            return false;
        }
        InstrIndex startIndex = ifStatement.getIndex();
        if (startIndex.compareTo(bIndex = ifStatement.getTargets().get(1).getIndex()) >= 0) {
            return false;
        }
        int aidx = startIdx + 1;
        if ((cidx = Op03SimpleStatement.findOverIdx(bidx, statements)) == -1) {
            return false;
        }
        if ((didx = Op03SimpleStatement.findOverIdx(cidx, statements)) == -1) {
            return false;
        }
        if (didx <= cidx) {
            return false;
        }
        if (!Op03SimpleStatement.isRangeOnlyReachable(aidx, bidx, cidx, statements, permittedSources = SetFactory.newSet(ifStatement))) {
            return false;
        }
        if (!Op03SimpleStatement.isRangeOnlyReachable(bidx, cidx, didx, statements, permittedSources)) {
            return false;
        }
        List<Op03SimpleStatement> alist = statements.subList(aidx, bidx);
        List<Op03SimpleStatement> blist = statements.subList(bidx, cidx);
        alist.get(alist.size() - 1).nopOut();
        List<Op03SimpleStatement> ifTargets = ifStatement.getTargets();
        Op03SimpleStatement tgtA = ifTargets.get(0);
        Op03SimpleStatement tgtB = ifTargets.get(1);
        ifTargets.set(0, tgtB);
        ifTargets.set(1, tgtA);
        innerIfStatement.setCondition(innerIfStatement.getCondition().getNegated().simplify());
        List<Op03SimpleStatement> acopy = ListFactory.newList(alist);
        blist.addAll(acopy);
        alist = statements.subList(aidx, bidx);
        alist.clear();
        Op03SimpleStatement.reindexInPlace(statements);
        return true;
    }

    private static int findOverIdx(int startNext, List<Op03SimpleStatement> statements) {
        Op03SimpleStatement next = statements.get(startNext);
        Op03SimpleStatement cStatement = null;
        for (int gSearch = startNext - 1; gSearch >= 0; --gSearch) {
            Statement s;
            Op03SimpleStatement tgtC;
            Op03SimpleStatement stm;
            if (s = (stm = statements.get(gSearch)).getStatement() instanceof Nop) continue;
            if (s.getClass() != GotoStatement.class) return -1;
            if ((tgtC = stm.getTargets().get(0)).getIndex().isBackJumpFrom(next)) {
                return -1;
            }
            cStatement = tgtC;
            break;
        }
        if (cStatement == null) {
            return -1;
        }
        int cidx = statements.indexOf(cStatement);
        return cidx;
    }

    private static boolean isRangeOnlyReachable(int startIdx, int endIdx, int tgtIdx, List<Op03SimpleStatement> statements, Set<Op03SimpleStatement> permittedSources) {
        Set reachable = SetFactory.newSet();
        Op03SimpleStatement startStatement = statements.get(startIdx);
        Op03SimpleStatement endStatement = statements.get(endIdx);
        Op03SimpleStatement tgtStatement = statements.get(tgtIdx);
        InstrIndex startIndex = startStatement.getIndex();
        InstrIndex endIndex = endStatement.getIndex();
        InstrIndex finalTgtIndex = tgtStatement.getIndex();
        reachable.add((Op03SimpleStatement)statements.get(startIdx));
        boolean foundEnd = false;
        for (int idx = startIdx; idx < endIdx; ++idx) {
            Op03SimpleStatement stm;
            if (!reachable.contains(stm = statements.get(idx))) {
                return false;
            }
            Iterator<Op03SimpleStatement> i$ = stm.getSources().iterator();
            while (i$.hasNext()) {
                InstrIndex sourceIndex;
                Op03SimpleStatement source;
                if (!((sourceIndex = (source = i$.next()).getIndex()).compareTo(startIndex) >= 0 || permittedSources.contains(source))) {
                    return false;
                }
                if (sourceIndex.compareTo(endIndex) < 0) continue;
                return false;
            }
            i$ = stm.getTargets().iterator();
            while (i$.hasNext()) {
                InstrIndex tgtIndex;
                Op03SimpleStatement target;
                if ((tgtIndex = (target = i$.next()).getIndex()).compareTo(startIndex) < 0) {
                    return false;
                }
                if (tgtIndex.compareTo(endIndex) >= 0) {
                    if (tgtIndex != finalTgtIndex) {
                        return false;
                    }
                    foundEnd = true;
                }
                reachable.add((Op03SimpleStatement)target);
            }
        }
        return foundEnd;
    }

    private static boolean considerAsSimpleIf(Op03SimpleStatement ifStatement, List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory, Set<Op03SimpleStatement> ignoreTheseJumps) {
        DiscoveredTernary ternary;
        Op03SimpleStatement statementCurrent;
        Set<BlockIdentifier> blocksAtEnd;
        Op03SimpleStatement takenTarget = ifStatement.targets.get(1);
        Op03SimpleStatement notTakenTarget = ifStatement.targets.get(0);
        int idxTaken = statements.indexOf(takenTarget);
        int idxNotTaken = statements.indexOf(notTakenTarget);
        IfStatement innerIfStatement = (IfStatement)ifStatement.containedStatement;
        Set ignoreLocally = SetFactory.newSet();
        boolean takenAction = false;
        int idxCurrent = idxNotTaken;
        if (idxCurrent > idxTaken) {
            return false;
        }
        int idxEnd = idxTaken;
        int maybeElseEndIdx = -1;
        Op03SimpleStatement maybeElseEnd = null;
        boolean maybeSimpleIfElse = false;
        boolean extractCommonEnd = false;
        GotoStatement leaveIfBranchGoto = null;
        Op03SimpleStatement leaveIfBranchHolder = null;
        List ifBranch = ListFactory.newList();
        List elseBranch = null;
        Set<BlockIdentifier> blocksAtStart = ifStatement.containedInBlocks;
        if (idxCurrent == idxEnd) {
            Op03SimpleStatement taken = new Op03SimpleStatement(blocksAtStart, new CommentStatement("empty if block"), notTakenTarget.index.justBefore());
            taken.addSource(ifStatement);
            taken.addTarget(notTakenTarget);
            Op03SimpleStatement emptyTarget = ifStatement.targets.get(0);
            if (notTakenTarget != emptyTarget) {
                notTakenTarget.addSource(taken);
            }
            emptyTarget.replaceSource(ifStatement, taken);
            ifStatement.targets.set(0, taken);
            statements.add(idxTaken, taken);
            BlockIdentifier ifBlockLabel = blockIdentifierFactory.getNextBlockIdentifier(BlockType.SIMPLE_IF_TAKEN);
            taken.markFirstStatementInBlock(ifBlockLabel);
            taken.getBlockIdentifiers().add(ifBlockLabel);
            innerIfStatement.setKnownBlocks(ifBlockLabel, null);
            innerIfStatement.setJumpType(JumpType.GOTO_OUT_OF_IF);
            return true;
        }
        Set validForwardParents = SetFactory.newSet();
        validForwardParents.add((Op03SimpleStatement)ifStatement);
        Op03SimpleStatement stmtLastBlock = statements.get(idxTaken - 1);
        Op03SimpleStatement stmtLastBlockRewrite = null;
        Statement stmtLastBlockInner = stmtLastBlock.getStatement();
        if (stmtLastBlockInner.getClass() == GotoStatement.class) {
            stmtLastBlockRewrite = stmtLastBlock;
        }
        do {
            statementCurrent = statements.get(idxCurrent);
            InstrIndex currentIndex = statementCurrent.getIndex();
            for (Op03SimpleStatement source : statementCurrent.sources) {
                if (!currentIndex.isBackJumpTo(source) || validForwardParents.contains(source)) continue;
                Op03SimpleStatement newJump = new Op03SimpleStatement(ifStatement.containedInBlocks, new GotoStatement(), statementCurrent.getIndex().justBefore());
                if (statementCurrent == ifStatement.targets.get(0)) continue;
                Op03SimpleStatement oldTarget = ifStatement.targets.get(1);
                newJump.addTarget(oldTarget);
                newJump.addSource(ifStatement);
                ifStatement.replaceTarget(oldTarget, newJump);
                oldTarget.replaceSource(ifStatement, newJump);
                statements.add(idxCurrent, newJump);
                return true;
            }
            validForwardParents.add((Op03SimpleStatement)statementCurrent);
            ifBranch.add((Op03SimpleStatement)statementCurrent);
            JumpType jumpType = statementCurrent.getJumpType();
            if (jumpType.isUnknown() && !ignoreTheseJumps.contains(statementCurrent)) {
                GotoStatement gotoStatement;
                if (idxCurrent == idxTaken - 1) {
                    Statement mGotoStatement;
                    if ((mGotoStatement = statementCurrent.containedStatement).getClass() != GotoStatement.class) {
                        return false;
                    }
                    gotoStatement = (GotoStatement)mGotoStatement;
                    maybeElseEnd = statementCurrent.getTargets().get(0);
                    maybeElseEndIdx = statements.indexOf(maybeElseEnd);
                    if (maybeElseEnd.getIndex().compareTo(takenTarget.getIndex()) <= 0) {
                        return false;
                    }
                } else {
                    if (stmtLastBlockRewrite == null) {
                        Op03SimpleStatement tgtContainer;
                        if ((tgtContainer = statementCurrent.getTargets().get(0)) != takenTarget) return false;
                        ++idxCurrent;
                        continue;
                    }
                    List<Op03SimpleStatement> targets = statementCurrent.getTargets();
                    Op03SimpleStatement eventualTarget = stmtLastBlockRewrite.getTargets().get(0);
                    boolean found = false;
                    for (int x = 0; x < targets.size(); ++x) {
                        Op03SimpleStatement target;
                        if ((target = targets.get(x)) != eventualTarget || target == stmtLastBlockRewrite) continue;
                        targets.set(x, stmtLastBlockRewrite);
                        stmtLastBlockRewrite.addSource(statementCurrent);
                        if (eventualTarget.sources.contains(stmtLastBlockRewrite)) {
                            eventualTarget.removeSource(statementCurrent);
                        } else {
                            eventualTarget.replaceSource(statementCurrent, stmtLastBlockRewrite);
                        }
                        found = true;
                    }
                    return found;
                }
                leaveIfBranchHolder = statementCurrent;
                leaveIfBranchGoto = gotoStatement;
                maybeSimpleIfElse = true;
            }
            ++idxCurrent;
        } while (idxCurrent != idxEnd);
        if (maybeSimpleIfElse) {
            elseBranch = ListFactory.newList();
            idxCurrent = idxTaken;
            idxEnd = maybeElseEndIdx;
            do {
                Statement mGotoStatement;
                statementCurrent = statements.get(idxCurrent);
                elseBranch.add((Op03SimpleStatement)statementCurrent);
                JumpType jumpType = statementCurrent.getJumpType();
                if (!jumpType.isUnknown()) continue;
                if ((mGotoStatement = statementCurrent.containedStatement).getClass() != GotoStatement.class) {
                    return false;
                }
                GotoStatement gotoStatement = (GotoStatement)mGotoStatement;
                if (statementCurrent.targets.get(0) != maybeElseEnd) {
                    return false;
                }
                idxEnd = idxCurrent--;
                leaveIfBranchHolder.replaceTarget(maybeElseEnd, statementCurrent);
                statementCurrent.addSource(leaveIfBranchHolder);
                maybeElseEnd.removeSource(leaveIfBranchHolder);
                elseBranch.remove(statementCurrent);
                takenAction = true;
            } while (++idxCurrent != idxEnd);
        }
        Op03SimpleStatement realEnd = statements.get(idxEnd);
        if (!(blocksAtStart.containsAll(blocksAtEnd = realEnd.containedInBlocks) && blocksAtEnd.size() == blocksAtStart.size())) {
            return takenAction;
        }
        if ((ternary = Op03SimpleStatement.testForTernary(ifBranch, elseBranch, leaveIfBranchHolder)) != null) {
            for (Op03SimpleStatement statement2 : ifBranch) {
                statement2.nopOut();
            }
            for (Op03SimpleStatement statement2 : elseBranch) {
                statement2.nopOut();
            }
            ifStatement.ssaIdentifiers = leaveIfBranchHolder.ssaIdentifiers;
            ConditionalExpression conditionalExpression = innerIfStatement.getCondition().getNegated().simplify();
            Expression rhs = ternary.isPointlessBoolean() ? conditionalExpression : new TernaryExpression(conditionalExpression, ternary.e1, ternary.e2);
            ifStatement.replaceStatement(new AssignmentSimple(ternary.lValue, rhs));
            if (ternary.lValue instanceof StackSSALabel) {
                StackSSALabel stackSSALabel = (StackSSALabel)ternary.lValue;
                StackEntry stackEntry = stackSSALabel.getStackEntry();
                stackEntry.decSourceCount();
            }
            List<Op03SimpleStatement> tmp = ListFactory.uniqueList(ifStatement.targets);
            ifStatement.targets.clear();
            ifStatement.targets.addAll(tmp);
            if (ifStatement.targets.size() != 1) {
                throw new ConfusedCFRException("If statement should only have one target after dedup");
            }
            Op03SimpleStatement joinStatement = ifStatement.targets.get(0);
            tmp = ListFactory.uniqueList(joinStatement.sources);
            joinStatement.sources.clear();
            joinStatement.sources.addAll(tmp);
            Op03SimpleStatement.condenseLValues(statements);
            return true;
        }
        BlockIdentifier ifBlockLabel = blockIdentifierFactory.getNextBlockIdentifier(BlockType.SIMPLE_IF_TAKEN);
        Op03SimpleStatement.markWholeBlock(ifBranch, ifBlockLabel);
        BlockIdentifier elseBlockLabel = null;
        if (maybeSimpleIfElse) {
            elseBlockLabel = blockIdentifierFactory.getNextBlockIdentifier(BlockType.SIMPLE_IF_ELSE);
            if (elseBranch.isEmpty()) {
                elseBlockLabel = null;
                maybeSimpleIfElse = false;
            } else {
                Op03SimpleStatement.markWholeBlock(elseBranch, elseBlockLabel);
            }
        }
        if (leaveIfBranchGoto != null) {
            leaveIfBranchGoto.setJumpType(JumpType.GOTO_OUT_OF_IF);
        }
        innerIfStatement.setJumpType(JumpType.GOTO_OUT_OF_IF);
        innerIfStatement.setKnownBlocks(ifBlockLabel, elseBlockLabel);
        ignoreTheseJumps.addAll(ignoreLocally);
        return true;
    }

    public static void identifyNonjumpingConditionals(List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory) {
        boolean success = false;
        Set ignoreTheseJumps = SetFactory.newSet();
        do {
            success = false;
            List<Op03SimpleStatement> forwardIfs = Functional.filter(statements, new IsForwardIf(null));
            Collections.reverse(forwardIfs);
            for (Op03SimpleStatement forwardIf : forwardIfs) {
                if (!Op03SimpleStatement.considerAsTrivialIf(forwardIf, statements, blockIdentifierFactory, ignoreTheseJumps) && !Op03SimpleStatement.considerAsSimpleIf(forwardIf, statements, blockIdentifierFactory, ignoreTheseJumps) && !Op03SimpleStatement.considerAsDexIf(forwardIf, statements, blockIdentifierFactory, ignoreTheseJumps)) continue;
                success = true;
            }
        } while (success);
    }

    public static List<Op03SimpleStatement> removeUselessNops(List<Op03SimpleStatement> in) {
        return Functional.filter(in, new Predicate<Op03SimpleStatement>(){

            @Override
            public boolean test(Op03SimpleStatement in) {
                return !(in.sources.isEmpty() && in.targets.isEmpty());
            }
        });
    }

    public static List<Op03SimpleStatement> rewriteWith(List<Op03SimpleStatement> in, ExpressionRewriter expressionRewriter) {
        for (Op03SimpleStatement op03SimpleStatement : in) {
            op03SimpleStatement.rewrite(expressionRewriter);
        }
        return in;
    }

    private static void combineTryCatchBlocks(Op03SimpleStatement tryStatement, List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory) {
        Set<BlockIdentifier> tryBlocks;
        Set allStatements = SetFactory.newSet();
        TryStatement innerTryStatement = (TryStatement)tryStatement.getStatement();
        allStatements.addAll(new GraphVisitorBlockReachable(innerTryStatement.getBlockIdentifier(), null).run());
        for (Op03SimpleStatement target : tryStatement.getTargets()) {
            if (!(target.containedStatement instanceof CatchStatement)) continue;
            CatchStatement catchStatement = (CatchStatement)target.containedStatement;
            allStatements.addAll(new GraphVisitorBlockReachable(catchStatement.getCatchBlockIdent(), null).run());
        }
        if ((tryBlocks = tryStatement.containedInBlocks).isEmpty()) {
            return;
        }
        for (Op03SimpleStatement statement : allStatements) {
            statement.containedInBlocks.addAll(tryBlocks);
        }
    }

    public static void combineTryCatchBlocks(List<Op03SimpleStatement> in, BlockIdentifierFactory blockIdentifierFactory) {
        List<Op03SimpleStatement> tries = Functional.filter(in, new TypeFilter(TryStatement.class));
        for (Op03SimpleStatement tryStatement : tries) {
            Op03SimpleStatement.combineTryCatchBlocks(tryStatement, in, blockIdentifierFactory);
        }
    }

    private static void combineTryCatchEnds(Op03SimpleStatement tryStatement, List<Op03SimpleStatement> in) {
        TryStatement innerTryStatement = (TryStatement)tryStatement.getStatement();
        List lastStatements = ListFactory.newList();
        lastStatements.add((Op03SimpleStatement)Op03SimpleStatement.getLastContiguousBlockStatement(innerTryStatement.getBlockIdentifier(), in, tryStatement));
        int len = tryStatement.targets.size();
        for (int x = 1; x < len; ++x) {
            Op03SimpleStatement statementContainer;
            Statement statement;
            if (!(statement = (statementContainer = tryStatement.targets.get(x)).getStatement() instanceof CatchStatement)) {
                if (!(statement instanceof FinallyStatement)) return;
                return;
            }
            lastStatements.add((Op03SimpleStatement)Op03SimpleStatement.getLastContiguousBlockStatement(((CatchStatement)statement).getCatchBlockIdent(), in, statementContainer));
        }
        if (lastStatements.size() <= 1) {
            return;
        }
        Iterator i$ = lastStatements.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement last;
            if ((last = (Op03SimpleStatement)i$.next()) == null) {
                return;
            }
            if (last.getStatement().getClass() == GotoStatement.class) continue;
            return;
        }
        Op03SimpleStatement target = ((Op03SimpleStatement)lastStatements.get(0)).getTargets().get(0);
        Iterator i$2 = lastStatements.iterator();
        while (i$2.hasNext()) {
            Op03SimpleStatement last;
            if ((last = (Op03SimpleStatement)i$2.next()).getTargets().get(0) == target) continue;
            return;
        }
        Op03SimpleStatement finalStatement = (Op03SimpleStatement)lastStatements.get(lastStatements.size() - 1);
        int beforeTgt = in.indexOf(finalStatement);
        Op03SimpleStatement proxy = new Op03SimpleStatement(tryStatement.getBlockIdentifiers(), new GotoStatement(), finalStatement.getIndex().justAfter());
        in.add(beforeTgt + 1, proxy);
        proxy.addTarget(target);
        target.addSource(proxy);
        Set seen = SetFactory.newSet();
        Iterator i$3 = lastStatements.iterator();
        while (i$3.hasNext()) {
            Op03SimpleStatement last;
            if (!seen.add((Op03SimpleStatement)(last = (Op03SimpleStatement)i$3.next()))) continue;
            GotoStatement gotoStatement = (GotoStatement)last.containedStatement;
            gotoStatement.setJumpType(JumpType.END_BLOCK);
            last.replaceTarget(target, proxy);
            target.removeSource(last);
            proxy.addSource(last);
        }
    }

    private static void rewriteTryBackJump(Op03SimpleStatement stm) {
        InstrIndex idx = stm.getIndex();
        TryStatement tryStatement = (TryStatement)stm.getStatement();
        Op03SimpleStatement firstbody = stm.getTargets().get(0);
        BlockIdentifier blockIdentifier = tryStatement.getBlockIdentifier();
        Iterator<Op03SimpleStatement> sourceIter = stm.sources.iterator();
        while (sourceIter.hasNext()) {
            Op03SimpleStatement source;
            if (!idx.isBackJumpFrom(source = sourceIter.next()) || !source.getBlockIdentifiers().contains(blockIdentifier)) continue;
            source.replaceTarget(stm, firstbody);
            firstbody.addSource(source);
            sourceIter.remove();
        }
    }

    public static void rewriteTryBackJumps(List<Op03SimpleStatement> in) {
        List<Op03SimpleStatement> tries = Functional.filter(in, new TypeFilter(TryStatement.class));
        for (Op03SimpleStatement trystm : tries) {
            Op03SimpleStatement.rewriteTryBackJump(trystm);
        }
    }

    public static void combineTryCatchEnds(List<Op03SimpleStatement> in) {
        List<Op03SimpleStatement> tries = Functional.filter(in, new TypeFilter(TryStatement.class));
        for (Op03SimpleStatement tryStatement : tries) {
            Op03SimpleStatement.combineTryCatchEnds(tryStatement, in);
        }
    }

    private static Op03SimpleStatement insertBlockPadding(String comment, Op03SimpleStatement insertAfter, Op03SimpleStatement insertBefore, BlockIdentifier blockIdentifier, List<Op03SimpleStatement> statements) {
        Op03SimpleStatement between = new Op03SimpleStatement(insertAfter.getBlockIdentifiers(), new CommentStatement(comment), insertAfter.getIndex().justAfter());
        insertAfter.replaceTarget(insertBefore, between);
        insertBefore.replaceSource(insertAfter, between);
        between.addSource(insertAfter);
        between.addTarget(insertBefore);
        between.getBlockIdentifiers().add(blockIdentifier);
        statements.add(between);
        return between;
    }

    private static void identifyCatchBlock(Op03SimpleStatement start, BlockIdentifier blockIdentifier, List<Op03SimpleStatement> statements) {
        Set knownMembers = SetFactory.newSet();
        Set seen = SetFactory.newSet();
        seen.add((Op03SimpleStatement)start);
        knownMembers.add((Op03SimpleStatement)start);
        LinkedList pendingPossibilities = ListFactory.newLinkedList();
        if (start.targets.size() != 1) {
            throw new ConfusedCFRException("Catch statement with multiple targets");
        }
        for (Op03SimpleStatement target : start.targets) {
            pendingPossibilities.add((Op03SimpleStatement)target);
            seen.add((Op03SimpleStatement)target);
        }
        Map allows = MapFactory.newLazyMap(new UnaryFunction<Op03SimpleStatement, Set<Op03SimpleStatement>>(){

            @Override
            public Set<Op03SimpleStatement> invoke(Op03SimpleStatement ignore) {
                return SetFactory.newSet();
            }
        });
        int sinceDefinite = 0;
        while (!(pendingPossibilities.isEmpty() || sinceDefinite > pendingPossibilities.size())) {
            Op03SimpleStatement maybe = (Op03SimpleStatement)pendingPossibilities.removeFirst();
            boolean definite = true;
            for (Op03SimpleStatement source : maybe.sources) {
                if (knownMembers.contains(source) || source.getIndex().isBackJumpTo(maybe)) continue;
                definite = false;
                ((Set)allows.get(source)).add(maybe);
            }
            if (definite) {
                sinceDefinite = 0;
                knownMembers.add((Op03SimpleStatement)maybe);
                Set allowedBy = (Set)allows.get(maybe);
                pendingPossibilities.addAll(allowedBy);
                allowedBy.clear();
                for (Op03SimpleStatement target2 : maybe.targets) {
                    if (seen.contains(target2)) continue;
                    seen.add((Op03SimpleStatement)target2);
                    if (!target2.getIndex().isBackJumpTo(start)) continue;
                    pendingPossibilities.add((Op03SimpleStatement)target2);
                }
                continue;
            }
            ++sinceDefinite;
            pendingPossibilities.add((Op03SimpleStatement)maybe);
        }
        knownMembers.remove(start);
        if (knownMembers.isEmpty()) {
            List<Op03SimpleStatement> targets;
            if ((targets = start.getTargets()).size() != 1) {
                throw new ConfusedCFRException("Synthetic catch block has multiple targets");
            }
            knownMembers.add((Op03SimpleStatement)Op03SimpleStatement.insertBlockPadding("empty catch block", start, targets.get(0), blockIdentifier, statements));
        }
        List knownMemberList = ListFactory.newList(knownMembers);
        Collections.sort(knownMemberList, new CompareByIndex());
        List truncatedKnownMembers = ListFactory.newList();
        List flushNops = ListFactory.newList();
        int l = statements.size();
        for (int x = statements.indexOf(knownMemberList.get((int)0)); x < l; ++x) {
            Op03SimpleStatement statement;
            if ((statement = statements.get(x)).isNop()) {
                flushNops.add((Op03SimpleStatement)statement);
                continue;
            }
            if (!knownMembers.contains(statement)) break;
            truncatedKnownMembers.add((Op03SimpleStatement)statement);
            if (flushNops.isEmpty()) continue;
            truncatedKnownMembers.addAll(flushNops);
            flushNops.clear();
        }
        for (Op03SimpleStatement inBlock : truncatedKnownMembers) {
            inBlock.containedInBlocks.add(blockIdentifier);
        }
        Op03SimpleStatement first = start.getTargets().get(0);
        first.markFirstStatementInBlock(blockIdentifier);
    }

    public static void identifyCatchBlocks(List<Op03SimpleStatement> in, BlockIdentifierFactory blockIdentifierFactory) {
        List<Op03SimpleStatement> catchStarts = Functional.filter(in, new TypeFilter(CatchStatement.class));
        for (Op03SimpleStatement catchStart : catchStarts) {
            CatchStatement catchStatement;
            if ((catchStatement = (CatchStatement)catchStart.containedStatement).getCatchBlockIdent() != null) continue;
            BlockIdentifier blockIdentifier = blockIdentifierFactory.getNextBlockIdentifier(BlockType.CATCHBLOCK);
            catchStatement.setCatchBlockIdent(blockIdentifier);
            Op03SimpleStatement.identifyCatchBlock(catchStart, blockIdentifier, in);
        }
    }

    private static Op03SimpleStatement getLastContiguousBlockStatement(BlockIdentifier blockIdentifier, List<Op03SimpleStatement> in, Op03SimpleStatement preBlock) {
        if (preBlock.targets.isEmpty()) {
            return null;
        }
        Op03SimpleStatement currentStatement = preBlock.targets.get(0);
        int x = in.indexOf(currentStatement);
        if (!currentStatement.getBlockIdentifiers().contains(blockIdentifier)) {
            return null;
        }
        Op03SimpleStatement last = currentStatement;
        while (currentStatement.getBlockIdentifiers().contains(blockIdentifier)) {
            if (++x >= in.size()) return last;
            last = currentStatement;
            currentStatement = in.get(x);
        }
        return last;
    }

    private static void extendTryBlock(Op03SimpleStatement tryStatement, List<Op03SimpleStatement> in, DCCommonState dcCommonState) {
        TryStatement tryStatementInner = (TryStatement)tryStatement.getStatement();
        BlockIdentifier tryBlockIdent = tryStatementInner.getBlockIdentifier();
        Op03SimpleStatement currentStatement = tryStatement.targets.get(0);
        int x = in.indexOf(currentStatement);
        while (currentStatement.getBlockIdentifiers().contains(tryBlockIdent)) {
            if (++x >= in.size()) {
                return;
            }
            currentStatement = in.get(x);
        }
        Set caught = SetFactory.newSet();
        List<Op03SimpleStatement> targets = tryStatement.targets;
        int len = targets.size();
        for (int i = 1; i < len; ++i) {
            Statement statement;
            if (!(statement = targets.get(i).getStatement() instanceof CatchStatement)) continue;
            CatchStatement catchStatement = (CatchStatement)statement;
            List<ExceptionGroup.Entry> exceptions = catchStatement.getExceptions();
            for (ExceptionGroup.Entry entry : exceptions) {
                caught.add((JavaRefTypeInstance)entry.getCatchType());
            }
        }
        ExceptionCheckImpl exceptionCheck = new ExceptionCheckImpl(dcCommonState, caught);
        block3 : while (!currentStatement.getStatement().canThrow(exceptionCheck)) {
            Op03SimpleStatement nextStatement;
            Set validBlocks = SetFactory.newSet();
            validBlocks.add((BlockIdentifier)tryBlockIdent);
            int len2 = tryStatement.targets.size();
            for (int i2 = 1; i2 < len2; ++i2) {
                Statement tgtStatement;
                Op03SimpleStatement tgt;
                if (!(tgtStatement = (tgt = tryStatement.targets.get(i2)).getStatement() instanceof CatchStatement)) {
                    if (!(tgtStatement instanceof FinallyStatement)) {
                        return;
                    }
                } else {
                    validBlocks.add((BlockIdentifier)((CatchStatement)tgtStatement).getCatchBlockIdent());
                }
                validBlocks.add((BlockIdentifier)((FinallyStatement)tgtStatement).getFinallyBlockIdent());
            }
            boolean foundSource = false;
            for (Op03SimpleStatement source : currentStatement.sources) {
                if (!SetUtil.hasIntersection(validBlocks, source.getBlockIdentifiers())) {
                    return;
                }
                if (!source.getBlockIdentifiers().contains(tryBlockIdent)) continue;
                foundSource = true;
            }
            if (!foundSource) {
                return;
            }
            currentStatement.getBlockIdentifiers().add(tryBlockIdent);
            if (++x >= in.size()) return;
            if (!currentStatement.getTargets().contains(nextStatement = in.get(x))) {
                for (Op03SimpleStatement source2 : nextStatement.getSources()) {
                    if (source2.getBlockIdentifiers().contains(tryBlockIdent)) continue;
                    break block3;
                }
            }
            currentStatement = nextStatement;
        }
    }

    public static void extendTryBlocks(DCCommonState dcCommonState, List<Op03SimpleStatement> in) {
        List<Op03SimpleStatement> tries = Functional.filter(in, new TypeFilter(TryStatement.class));
        for (Op03SimpleStatement tryStatement : tries) {
            Op03SimpleStatement.extendTryBlock(tryStatement, in, dcCommonState);
        }
    }

    public static void identifyFinally(Options options, Method method, List<Op03SimpleStatement> in, BlockIdentifierFactory blockIdentifierFactory) {
        boolean continueLoop;
        boolean bl;
        if (!((Boolean)options.getOption(OptionsImpl.DECODE_FINALLY)).booleanValue()) {
            return;
        }
        Set analysedTries = SetFactory.newSet();
        do {
            List<Op03SimpleStatement> tryStarts = Functional.filter(in, new Predicate<Op03SimpleStatement>(analysedTries){
                final /* synthetic */ Set val$analysedTries;

                @Override
                public boolean test(Op03SimpleStatement in) {
                    if (!(in.getStatement() instanceof TryStatement) || this.val$analysedTries.contains(in)) return false;
                    return true;
                }
            });
            for (Op03SimpleStatement tryS : tryStarts) {
                FinalAnalyzer.identifyFinally(method, tryS, in, blockIdentifierFactory, analysedTries);
            }
            if (!tryStarts.isEmpty()) {
                bl = true;
                continue;
            }
            bl = false;
        } while (continueLoop = bl);
    }

    public static List<Op03SimpleStatement> removeRedundantTries(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> tryStarts = Functional.filter(statements, new TypeFilter(TryStatement.class));
        boolean effect = false;
        Collections.reverse(tryStarts);
        LinkedList starts = ListFactory.newLinkedList();
        starts.addAll(tryStarts);
        while (!starts.isEmpty()) {
            Statement stm;
            Op03SimpleStatement trys;
            if (!(stm = (trys = (Op03SimpleStatement)starts.removeFirst()).getStatement() instanceof TryStatement)) continue;
            TryStatement tryStatement = (TryStatement)stm;
            BlockIdentifier tryBlock = tryStatement.getBlockIdentifier();
            if (!trys.targets.isEmpty() && trys.targets.get(0).getBlockIdentifiers().contains(tryBlock)) continue;
            Op03SimpleStatement codeTarget = trys.targets.get(0);
            for (Op03SimpleStatement target : trys.targets) {
                target.removeSource(trys);
            }
            trys.targets.clear();
            for (Op03SimpleStatement source : trys.sources) {
                source.replaceTarget(trys, codeTarget);
                codeTarget.addSource(source);
            }
            trys.sources.clear();
            effect = true;
        }
        if (!effect) return statements;
        statements = Op03SimpleStatement.removeUnreachableCode(statements, false);
        statements = Op03SimpleStatement.renumber(statements);
        return statements;
    }

    private static boolean verifyLinearBlock(Op03SimpleStatement current, BlockIdentifier block, int num) {
        for (; num >= 0; --num) {
            if (num > 0) {
                if (current.getStatement() instanceof Nop && current.targets.size() == 0) break;
                if (current.targets.size() != 1) {
                    return false;
                }
                if (!current.containedInBlocks.contains(block)) {
                    return false;
                }
            } else {
                if (current.containedInBlocks.contains(block)) continue;
                return false;
            }
            current = current.targets.get(0);
        }
        for (Op03SimpleStatement target : current.targets) {
            if (!target.containedInBlocks.contains(block)) continue;
            return false;
        }
        return true;
    }

    private static boolean removeSynchronizedCatchBlock(Op03SimpleStatement start, List<Op03SimpleStatement> statements) {
        Op03SimpleStatement variableAss;
        Op03SimpleStatement monitorExit;
        Op03SimpleStatement rethrow;
        BlockIdentifier block = start.firstStatementInThisBlock;
        if (start.sources.size() != 1) {
            return false;
        }
        Op03SimpleStatement catchStatementContainer = start.sources.get(0);
        if (catchStatementContainer.sources.size() != 1) {
            return false;
        }
        Statement catchOrFinally = catchStatementContainer.containedStatement;
        boolean isFinally = false;
        if (catchOrFinally instanceof CatchStatement) {
            ExceptionGroup.Entry exception;
            CatchStatement catchStatement;
            List<ExceptionGroup.Entry> exceptions;
            if ((exceptions = (catchStatement = (CatchStatement)catchStatementContainer.containedStatement).getExceptions()).size() != 1) {
                return false;
            }
            if (!(exception = exceptions.get(0)).isJustThrowable()) {
                return false;
            }
        } else {
            if (!(catchOrFinally instanceof FinallyStatement)) {
                return false;
            }
            isFinally = true;
        }
        if (!Op03SimpleStatement.verifyLinearBlock(start, block, 2)) {
            return false;
        }
        if (isFinally) {
            monitorExit = start;
            variableAss = null;
            rethrow = null;
        } else {
            variableAss = start;
            monitorExit = start.targets.get(0);
            rethrow = monitorExit.targets.get(0);
        }
        WildcardMatch wildcardMatch = new WildcardMatch();
        if (!(isFinally || wildcardMatch.match(new AssignmentSimple(wildcardMatch.getLValueWildCard("var"), wildcardMatch.getExpressionWildCard("e")), variableAss.containedStatement))) {
            return false;
        }
        if (!wildcardMatch.match(new MonitorExitStatement(wildcardMatch.getExpressionWildCard("lock")), monitorExit.containedStatement)) {
            return false;
        }
        if (!(isFinally || wildcardMatch.match(new ThrowStatement(new LValueExpression(wildcardMatch.getLValueWildCard("var"))), rethrow.containedStatement))) {
            return false;
        }
        Op03SimpleStatement tryStatementContainer = catchStatementContainer.sources.get(0);
        if (isFinally) {
            MonitorExitStatement monitorExitStatement = (MonitorExitStatement)monitorExit.getStatement();
            TryStatement tryStatement = (TryStatement)tryStatementContainer.getStatement();
            tryStatement.addExitMutex(monitorExitStatement.getMonitor());
        }
        tryStatementContainer.removeTarget(catchStatementContainer);
        catchStatementContainer.removeSource(tryStatementContainer);
        catchStatementContainer.nopOut();
        if (!isFinally) {
            variableAss.nopOut();
        }
        monitorExit.nopOut();
        if (!isFinally) {
            for (Op03SimpleStatement target : rethrow.targets) {
                target.removeSource(rethrow);
                rethrow.removeTarget(target);
            }
            rethrow.nopOut();
        }
        if (tryStatementContainer.targets.size() != 1 || isFinally) return true;
        TryStatement tryStatement = (TryStatement)tryStatementContainer.containedStatement;
        BlockIdentifier tryBlock = tryStatement.getBlockIdentifier();
        tryStatementContainer.nopOut();
        for (Op03SimpleStatement statement : statements) {
            statement.containedInBlocks.remove(tryBlock);
        }
        return true;
    }

    public static void commentMonitors(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> monitors = Functional.filter(statements, new TypeFilter(MonitorStatement.class));
        if (monitors.isEmpty()) {
            return;
        }
        for (Op03SimpleStatement monitor2 : monitors) {
            monitor2.replaceStatement(new CommentStatement(monitor2.getStatement()));
        }
        for (Op03SimpleStatement monitor2 : monitors) {
            Op03SimpleStatement target = monitor2.getTargets().get(0);
            Set<BlockIdentifier> monitorLast = SetFactory.newSet(monitor2.getBlockIdentifiers());
            monitorLast.removeAll(target.getBlockIdentifiers());
            if (monitorLast.isEmpty()) continue;
            Iterator<Op03SimpleStatement> i$ = ListFactory.newList(monitor2.sources).iterator();
            while (i$.hasNext()) {
                Op03SimpleStatement source;
                Set<BlockIdentifier> sourceBlocks;
                if ((sourceBlocks = (source = i$.next()).getBlockIdentifiers()).containsAll(monitorLast)) continue;
                source.replaceTarget(monitor2, target);
                monitor2.removeSource(source);
                target.addSource(source);
            }
        }
    }

    public static void removeSynchronizedCatchBlocks(Options options, List<Op03SimpleStatement> in) {
        List<Op03SimpleStatement> catchStarts;
        if (!((Boolean)options.getOption(OptionsImpl.TIDY_MONITORS)).booleanValue()) {
            return;
        }
        if ((catchStarts = Functional.filter(in, new FindBlockStarts(BlockType.CATCHBLOCK))).isEmpty()) {
            return;
        }
        boolean effect = false;
        Iterator<Op03SimpleStatement> i$ = catchStarts.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement catchStart;
            effect = Op03SimpleStatement.removeSynchronizedCatchBlock(catchStart = i$.next(), in) || effect;
        }
        if (!effect) return;
        Op03SimpleStatement.removePointlessJumps(in);
    }

    public static void replaceRawSwitch(Op03SimpleStatement swatch, List<Op03SimpleStatement> in, BlockIdentifierFactory blockIdentifierFactory) {
        List<Op03SimpleStatement> targets = swatch.targets;
        RawSwitchStatement switchStatement = (RawSwitchStatement)swatch.containedStatement;
        DecodedSwitch switchData = switchStatement.getSwitchData();
        BlockIdentifier switchBlockIdentifier = blockIdentifierFactory.getNextBlockIdentifier(BlockType.SWITCH);
        Op03SimpleStatement oneTarget = targets.get(0);
        boolean mismatch = false;
        for (int x = 1; x < targets.size(); ++x) {
            Op03SimpleStatement target;
            if ((target = targets.get(x)) == oneTarget) continue;
            mismatch = true;
            break;
        }
        if (!mismatch) {
            swatch.replaceStatement(new GotoStatement());
            return;
        }
        List<DecodedSwitchEntry> entries = switchData.getJumpTargets();
        InferredJavaType caseType = switchStatement.getSwitchOn().getInferredJavaType();
        Map firstPrev = MapFactory.newMap();
        for (int x2 = 0; x2 < targets.size(); ++x2) {
            Op03SimpleStatement target;
            InstrIndex tindex;
            if (firstPrev.containsKey(tindex = (target = targets.get(x2)).getIndex())) {
                target = (Op03SimpleStatement)firstPrev.get(tindex);
            }
            List expression = ListFactory.newList();
            if (x2 != 0) {
                List<Integer> vals = entries.get(x2 - 1).getValue();
                Iterator<Integer> i$ = vals.iterator();
                while (i$.hasNext()) {
                    int val = i$.next();
                    expression.add((Literal)new Literal(TypedLiteral.getInt(val)));
                }
            }
            Set<BlockIdentifier> blocks = SetFactory.newSet(target.getBlockIdentifiers());
            blocks.add(switchBlockIdentifier);
            BlockIdentifier caseIdentifier = blockIdentifierFactory.getNextBlockIdentifier(BlockType.CASE);
            Op03SimpleStatement caseStatement = new Op03SimpleStatement(blocks, new CaseStatement(expression, caseType, switchBlockIdentifier, caseIdentifier), target.getIndex().justBefore());
            Iterator<Op03SimpleStatement> iterator = target.sources.iterator();
            while (iterator.hasNext()) {
                Op03SimpleStatement source;
                if (swatch.getIndex().isBackJumpTo(source = iterator.next())) continue;
                if (source.getIndex().isBackJumpTo(target)) continue;
                source.replaceTarget(target, caseStatement);
                caseStatement.addSource(source);
                iterator.remove();
            }
            target.sources.add(caseStatement);
            caseStatement.addTarget(target);
            in.add(caseStatement);
            firstPrev.put((InstrIndex)tindex, (Op03SimpleStatement)caseStatement);
        }
        Op03SimpleStatement.renumberInPlace(in);
        Op03SimpleStatement.buildSwitchCases(swatch, targets, switchBlockIdentifier, in);
        swatch.replaceStatement(switchStatement.getSwitchStatement(switchBlockIdentifier));
        Collections.sort(swatch.targets, new CompareByIndex());
    }

    public static void rebuildSwitches(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> switchStatements = Functional.filter(statements, new TypeFilter(SwitchStatement.class));
        for (Op03SimpleStatement switchStatement2 : switchStatements) {
            SwitchStatement switchStatementInr = (SwitchStatement)switchStatement2.getStatement();
            Set allBlocks = SetFactory.newSet();
            allBlocks.add((BlockIdentifier)switchStatementInr.getSwitchBlock());
            Iterator<Op03SimpleStatement> i$ = switchStatement2.targets.iterator();
            while (i$.hasNext()) {
                Op03SimpleStatement target;
                Statement stmTgt;
                if (!(stmTgt = (target = i$.next()).getStatement() instanceof CaseStatement)) continue;
                allBlocks.add((BlockIdentifier)((CaseStatement)stmTgt).getCaseBlock());
            }
            for (Op03SimpleStatement stm : statements) {
                stm.getBlockIdentifiers().removeAll(allBlocks);
            }
            Op03SimpleStatement.buildSwitchCases(switchStatement2, switchStatement2.getTargets(), switchStatementInr.getSwitchBlock(), statements);
        }
        for (Op03SimpleStatement switchStatement2 : switchStatements) {
            Op03SimpleStatement.examineSwitchContiguity(switchStatement2, statements);
            Op03SimpleStatement.moveJumpsToTerminalIfEmpty(switchStatement2, statements);
        }
    }

    private static void buildSwitchCases(Op03SimpleStatement swatch, List<Op03SimpleStatement> targets, BlockIdentifier switchBlockIdentifier, List<Op03SimpleStatement> in) {
        Set caseIdentifiers = SetFactory.newSet();
        Set<Op03SimpleStatement> caseTargets = SetFactory.newSet(targets);
        Map lastStatementBefore = MapFactory.newMap();
        for (Op03SimpleStatement target : targets) {
            InstrIndex prev;
            CaseStatement caseStatement = (CaseStatement)target.getStatement();
            BlockIdentifier caseBlock = caseStatement.getCaseBlock();
            NodeReachable nodeReachable = new NodeReachable(caseTargets, target, swatch, null);
            GraphVisitorDFS<Op03SimpleStatement> gv = new GraphVisitorDFS<Op03SimpleStatement>(target, (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)nodeReachable);
            gv.process();
            List backReachable = Functional.filter(nodeReachable.reaches, new IsForwardJumpTo(target.getIndex()));
            if (backReachable.isEmpty()) continue;
            if (backReachable.size() != 1) continue;
            Op03SimpleStatement backTarget = (Op03SimpleStatement)backReachable.get(0);
            boolean contiguous = Op03SimpleStatement.blockIsContiguous(in, target, nodeReachable.inBlock);
            if (target.getSources().size() != 1) {
                if (!contiguous) continue;
                for (Op03SimpleStatement reachable : nodeReachable.inBlock) {
                    reachable.markBlock(switchBlockIdentifier);
                    if (caseTargets.contains(reachable) || SetUtil.hasIntersection(reachable.getBlockIdentifiers(), caseIdentifiers)) continue;
                    reachable.markBlock(caseBlock);
                }
                continue;
            }
            if (!contiguous) continue;
            if ((prev = (InstrIndex)lastStatementBefore.get(backTarget)) == null) {
                prev = backTarget.getIndex().justBefore();
            }
            int idx = in.indexOf(target) + nodeReachable.inBlock.size() - 1;
            int len = nodeReachable.inBlock.size();
            for (int i = 0; i < len; ++i) {
                in.get(idx).setIndex(prev);
                prev = prev.justBefore();
                --idx;
            }
            lastStatementBefore.put((Op03SimpleStatement)backTarget, (InstrIndex)prev);
        }
    }

    private static boolean blockIsContiguous(List<Op03SimpleStatement> in, Op03SimpleStatement start, Set<Op03SimpleStatement> blockContent) {
        int idx = in.indexOf(start);
        int len = blockContent.size();
        if (idx + blockContent.size() > in.size()) {
            return false;
        }
        for (int found = 1; found < len; ++found) {
            Op03SimpleStatement next;
            if (!blockContent.contains(next = in.get(idx))) {
                return false;
            }
            ++idx;
        }
        return true;
    }

    private static void moveJumpsToTerminalIfEmpty(Op03SimpleStatement switchStatement, List<Op03SimpleStatement> statements) {
        Op03SimpleStatement following;
        int idx;
        List<Op03SimpleStatement> forwardJumpSources;
        SwitchStatement swatch = (SwitchStatement)switchStatement.getStatement();
        Op03SimpleStatement lastTgt = switchStatement.targets.get(switchStatement.targets.size() - 1);
        BlockIdentifier switchBlock = swatch.getSwitchBlock();
        if (!lastTgt.getBlockIdentifiers().contains(switchBlock)) {
            return;
        }
        if (lastTgt.targets.size() != 1) {
            return;
        }
        if (lastTgt.sources.size() == 1) {
            return;
        }
        if ((following = lastTgt.targets.get(0)).getBlockIdentifiers().contains(switchBlock)) {
            return;
        }
        if ((forwardJumpSources = Functional.filter(lastTgt.sources, new IsForwardJumpTo(lastTgt.getIndex()))).size() <= 1) {
            return;
        }
        if ((idx = statements.indexOf(lastTgt)) == 0) {
            return;
        }
        Op03SimpleStatement justBefore = statements.get(idx - 1);
        if (idx >= statements.size() - 1) {
            return;
        }
        if (statements.get(idx + 1) != following) {
            return;
        }
        for (Op03SimpleStatement forwardJumpSource : forwardJumpSources) {
            Statement forwardJump;
            JumpingStatement jumpingStatement;
            JumpType jumpType;
            if (forwardJumpSource == switchStatement) continue;
            if (forwardJumpSource == justBefore) continue;
            forwardJumpSource.replaceTarget(lastTgt, following);
            lastTgt.removeSource(forwardJumpSource);
            following.addSource(forwardJumpSource);
            if (!(forwardJump = forwardJumpSource.getStatement() instanceof JumpingStatement) || !(jumpType = (jumpingStatement = (JumpingStatement)forwardJump).getJumpType()).isUnknown()) continue;
            jumpingStatement.setJumpType(JumpType.BREAK);
        }
    }

    private static boolean examineSwitchContiguity(Op03SimpleStatement switchStatement, List<Op03SimpleStatement> statements) {
        Op03SimpleStatement lastInThis;
        Op03SimpleStatement statement;
        int y;
        Set forwardTargets = SetFactory.newSet();
        List<Op03SimpleStatement> targets = ListFactory.newList(switchStatement.targets);
        Collections.sort(targets, new CompareByIndex());
        int idxFirstCase = statements.indexOf(targets.get(0));
        if (idxFirstCase != statements.indexOf(switchStatement) + 1) {
            throw new ConfusedCFRException("First case is not immediately after switch.");
        }
        BlockIdentifier switchBlock = ((SwitchStatement)switchStatement.containedStatement).getSwitchBlock();
        int indexLastInLastBlock = 0;
        for (int x = 0; x < targets.size() - 1; ++x) {
            Statement maybeCaseStatement;
            Op03SimpleStatement thisCase = targets.get(x);
            Op03SimpleStatement nextCase = targets.get(x + 1);
            int indexThisCase = statements.indexOf(thisCase);
            int indexNextCase = statements.indexOf(nextCase);
            InstrIndex nextCaseIndex = nextCase.getIndex();
            if (!(maybeCaseStatement = thisCase.containedStatement instanceof CaseStatement)) continue;
            CaseStatement caseStatement = (CaseStatement)maybeCaseStatement;
            BlockIdentifier caseBlock = caseStatement.getCaseBlock();
            int indexLastInThis = Op03SimpleStatement.getFarthestReachableInRange(statements, indexThisCase, indexNextCase);
            if (indexLastInThis != indexNextCase - 1) {
                // empty if block
            }
            indexLastInLastBlock = indexLastInThis;
            for (int y2 = indexThisCase + 1; y2 <= indexLastInThis; ++y2) {
                Op03SimpleStatement statement2 = statements.get(y2);
                statement2.markBlock(caseBlock);
                statement2.markBlock(switchBlock);
                if (!statement2.getJumpType().isUnknown()) continue;
                for (Op03SimpleStatement innerTarget : statement2.targets) {
                    if (!nextCaseIndex.isBackJumpFrom(innerTarget = Op03SimpleStatement.followNopGoto(innerTarget, false, false))) continue;
                    forwardTargets.add((Op03SimpleStatement)innerTarget);
                }
            }
        }
        Op03SimpleStatement lastCase = targets.get(targets.size() - 1);
        int indexLastCase = statements.indexOf(lastCase);
        int breakTarget = -1;
        BlockIdentifier caseBlock = null;
        int indexLastInThis = 0;
        boolean retieEnd = false;
        if (!forwardTargets.isEmpty()) {
            List lstFwdTargets = ListFactory.newList(forwardTargets);
            Collections.sort(lstFwdTargets, new CompareByIndex());
            Op03SimpleStatement afterCaseGuess = (Op03SimpleStatement)lstFwdTargets.get(0);
            int indexAfterCase = statements.indexOf(afterCaseGuess);
            CaseStatement caseStatement = (CaseStatement)lastCase.containedStatement;
            caseBlock = caseStatement.getCaseBlock();
            try {
                indexLastInThis = Op03SimpleStatement.getFarthestReachableInRange(statements, indexLastCase, indexAfterCase);
            }
            catch (CannotPerformDecode e) {
                forwardTargets.clear();
            }
            if (indexLastInThis != indexAfterCase - 1) {
                retieEnd = true;
            }
        }
        if (forwardTargets.isEmpty()) {
            for (y = idxFirstCase; y <= indexLastInLastBlock; ++y) {
                statement = statements.get(y);
                statement.markBlock(switchBlock);
            }
            if (indexLastCase != indexLastInLastBlock + 1) {
                throw new ConfusedCFRException("Extractable last case doesn't follow previous");
            }
            lastCase.markBlock(switchBlock);
            breakTarget = indexLastCase + 1;
        } else {
            for (y = indexLastCase + 1; y <= indexLastInThis; ++y) {
                statement = statements.get(y);
                statement.markBlock(caseBlock);
            }
            for (y = idxFirstCase; y <= indexLastInThis; ++y) {
                statement = statements.get(y);
                statement.markBlock(switchBlock);
            }
            breakTarget = indexLastInThis + 1;
        }
        Op03SimpleStatement breakStatementTarget = statements.get(breakTarget);
        if (retieEnd && (lastInThis = statements.get(indexLastInThis)).getStatement().getClass() == GotoStatement.class) {
            Set<BlockIdentifier> blockIdentifiers = SetFactory.newSet(lastInThis.getBlockIdentifiers());
            blockIdentifiers.remove(caseBlock);
            blockIdentifiers.remove(switchBlock);
            Op03SimpleStatement retie = new Op03SimpleStatement(blockIdentifiers, new GotoStatement(), lastInThis.getIndex().justAfter());
            Op03SimpleStatement target = lastInThis.targets.get(0);
            Iterator<Op03SimpleStatement> iterator = target.sources.iterator();
            while (iterator.hasNext()) {
                Op03SimpleStatement source;
                if (!(source = iterator.next()).getBlockIdentifiers().contains(switchBlock)) continue;
                iterator.remove();
                retie.addSource(source);
                source.replaceTarget(target, retie);
            }
            if (!retie.sources.isEmpty()) {
                retie.targets.add(target);
                target.addSource(retie);
                statements.add(breakTarget, retie);
                breakStatementTarget = retie;
            }
        }
        for (Op03SimpleStatement breakSource : breakStatementTarget.sources) {
            if (!breakSource.getBlockIdentifiers().contains(switchBlock) || !breakSource.getJumpType().isUnknown()) continue;
            ((JumpingStatement)breakSource.containedStatement).setJumpType(JumpType.BREAK);
        }
        return true;
    }

    public static void replaceRawSwitches(List<Op03SimpleStatement> in, BlockIdentifierFactory blockIdentifierFactory) {
        List<Op03SimpleStatement> switchStatements = Functional.filter(in, new TypeFilter(RawSwitchStatement.class));
        for (Op03SimpleStatement switchStatement2 : switchStatements) {
            Op03SimpleStatement.replaceRawSwitch(switchStatement2, in, blockIdentifierFactory);
        }
        Collections.sort(in, new CompareByIndex());
        switchStatements = Functional.filter(in, new TypeFilter(SwitchStatement.class));
        for (Op03SimpleStatement switchStatement2 : switchStatements) {
            Op03SimpleStatement.examineSwitchContiguity(switchStatement2, in);
            Op03SimpleStatement.moveJumpsToTerminalIfEmpty(switchStatement2, in);
        }
    }

    private static void optimiseForTypes(Op03SimpleStatement statement) {
        IfStatement ifStatement = (IfStatement)statement.containedStatement;
        ifStatement.optimiseForTypes();
    }

    public static void optimiseForTypes(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> conditionals = Functional.filter(statements, new TypeFilter(IfStatement.class));
        for (Op03SimpleStatement conditional : conditionals) {
            Op03SimpleStatement.optimiseForTypes(conditional);
        }
    }

    private static boolean findHiddenIter(Statement statement, LValue lValue, Expression rValue) {
        AssignmentExpression needle = new AssignmentExpression(lValue, rValue, true);
        NOPSearchingExpressionRewriter finder = new NOPSearchingExpressionRewriter(needle);
        statement.rewriteExpressions(finder, statement.getContainer().getSSAIdentifiers());
        return finder.isFound();
    }

    private static void replaceHiddenIter(Statement statement, LValue lValue, Expression rValue) {
        AssignmentExpression needle = new AssignmentExpression(lValue, rValue, true);
        ExpressionReplacingRewriter finder = new ExpressionReplacingRewriter(needle, new LValueExpression(lValue));
        statement.rewriteExpressions(finder, statement.getContainer().getSSAIdentifiers());
    }

    private static boolean rewriteArrayForLoop(Op03SimpleStatement loop, List<Op03SimpleStatement> statements) {
        WildcardMatch wildcardMatch;
        AbstractAssignmentExpression assignment;
        boolean incrMatch;
        LValue originalLoopVariable;
        ForStatement forStatement;
        Op03SimpleStatement preceeding = Op03SimpleStatement.findSingleBackSource(loop);
        if (preceeding == null) {
            return false;
        }
        if (!(wildcardMatch = new WildcardMatch()).match(new AssignmentSimple((wildcardMatch = new WildcardMatch()).getLValueWildCard("iter"), new Literal(TypedLiteral.getInt(0))), (forStatement = (ForStatement)loop.containedStatement).getInitial())) {
            return false;
        }
        if (!(incrMatch = (assignment = forStatement.getAssignment()).isSelfMutatingOp1(originalLoopVariable = wildcardMatch.getLValueWildCard("iter").getMatch(), ArithOp.PLUS))) {
            return false;
        }
        if (!wildcardMatch.match(new ComparisonOperation(new LValueExpression(originalLoopVariable), new LValueExpression(wildcardMatch.getLValueWildCard("bound")), CompOp.LT), forStatement.getCondition())) {
            return false;
        }
        LValue originalLoopBound = wildcardMatch.getLValueWildCard("bound").getMatch();
        if (!wildcardMatch.match(new AssignmentSimple(originalLoopBound, new ArrayLength(new LValueExpression(wildcardMatch.getLValueWildCard("array")))), preceeding.containedStatement)) {
            return false;
        }
        LValue originalArray = wildcardMatch.getLValueWildCard("array").getMatch();
        Expression arrayStatement = new LValueExpression(originalArray);
        Op03SimpleStatement prepreceeding = null;
        if (preceeding.sources.size() == 1 && wildcardMatch.match(new AssignmentSimple(originalArray, wildcardMatch.getExpressionWildCard("value")), preceeding.sources.get((int)0).containedStatement)) {
            prepreceeding = preceeding.sources.get(0);
            arrayStatement = wildcardMatch.getExpressionWildCard("value").getMatch();
        }
        Op03SimpleStatement loopStart = loop.getTargets().get(0);
        WildcardMatch.LValueWildcard sugariterWC = wildcardMatch.getLValueWildCard("sugariter");
        ArrayIndex arrIndex = new ArrayIndex(new LValueExpression(originalArray), new LValueExpression(originalLoopVariable));
        boolean hiddenIter = false;
        if (!wildcardMatch.match(new AssignmentSimple(sugariterWC, arrIndex), loopStart.containedStatement)) {
            if (!Op03SimpleStatement.findHiddenIter(loopStart.containedStatement, sugariterWC, arrIndex)) {
                return false;
            }
            hiddenIter = true;
        }
        LValue sugarIter = sugariterWC.getMatch();
        BlockIdentifier forBlock = forStatement.getBlockIdentifier();
        List<Op03SimpleStatement> statementsInBlock = Functional.filter(statements, new Predicate<Op03SimpleStatement>(forBlock){
            final /* synthetic */ BlockIdentifier val$forBlock;

            @Override
            public boolean test(Op03SimpleStatement in) {
                return in.containedInBlocks.contains(this.val$forBlock);
            }
        });
        LValueUsageCollectorSimple usageCollector = new LValueUsageCollectorSimple();
        Set<LValue> cantUpdate = SetFactory.newSet(originalArray, originalLoopBound, originalLoopVariable);
        for (Op03SimpleStatement inBlock : statementsInBlock) {
            LValue updated;
            if (inBlock == loopStart) continue;
            Statement inStatement = inBlock.containedStatement;
            inStatement.collectLValueUsage(usageCollector);
            for (LValue cantUse : cantUpdate) {
                if (!usageCollector.isUsed(cantUse)) continue;
                return false;
            }
            if ((updated = inStatement.getCreatedLValue()) == null) continue;
            if (!cantUpdate.contains(updated)) continue;
            return false;
        }
        AtomicBoolean res = new AtomicBoolean();
        GraphVisitorDFS<Op03SimpleStatement> graphVisitor = new GraphVisitorDFS<Op03SimpleStatement>(loop, (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(){

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                if (!(Op03SimpleStatement.this == arg1 || arg1.getBlockIdentifiers().contains(forBlock))) {
                    Statement inStatement;
                    AssignmentSimple assignmentSimple;
                    if (inStatement = arg1.getStatement() instanceof AssignmentSimple && cantUpdate.contains((assignmentSimple = (AssignmentSimple)inStatement).getCreatedLValue())) {
                        return;
                    }
                    LValueUsageCollectorSimple usageCollector = new LValueUsageCollectorSimple();
                    inStatement.collectLValueUsage(usageCollector);
                    Iterator i$ = cantUpdate.iterator();
                    while (i$.hasNext()) {
                        LValue cantUse;
                        if (!usageCollector.isUsed(cantUse = (LValue)i$.next())) continue;
                        res.set(true);
                        return;
                    }
                }
                for (Op03SimpleStatement target : arg1.getTargets()) {
                    arg2.enqueue(target);
                }
            }
        });
        graphVisitor.process();
        if (res.get()) {
            return false;
        }
        loop.replaceStatement(new ForIterStatement(forBlock, sugarIter, arrayStatement));
        if (hiddenIter) {
            Op03SimpleStatement.replaceHiddenIter(loopStart.containedStatement, sugariterWC.getMatch(), arrIndex);
        } else {
            loopStart.nopOut();
        }
        preceeding.nopOut();
        if (prepreceeding == null) return true;
        prepreceeding.nopOut();
        return true;
    }

    public static void rewriteArrayForLoops(List<Op03SimpleStatement> statements) {
        for (Op03SimpleStatement loop : Functional.filter(statements, new TypeFilter(ForStatement.class))) {
            Op03SimpleStatement.rewriteArrayForLoop(loop, statements);
        }
    }

    private static void rewriteIteratorWhileLoop(Op03SimpleStatement loop, List<Op03SimpleStatement> statements) {
        WildcardMatch wildcardMatch;
        WhileStatement whileStatement = (WhileStatement)loop.containedStatement;
        Op03SimpleStatement preceeding = Op03SimpleStatement.findSingleBackSource(loop);
        if (preceeding == null) {
            return;
        }
        if (!(wildcardMatch = new WildcardMatch()).match(new BooleanExpression((wildcardMatch = new WildcardMatch()).getMemberFunction("hasnextfn", "hasNext", (Expression)new LValueExpression((wildcardMatch = new WildcardMatch()).getLValueWildCard("iterable")))), whileStatement.getCondition())) {
            return;
        }
        LValue iterable = wildcardMatch.getLValueWildCard("iterable").getMatch();
        Op03SimpleStatement loopStart = loop.getTargets().get(0);
        boolean isCastExpression = false;
        boolean hiddenIter = false;
        WildcardMatch.LValueWildcard sugariterWC = wildcardMatch.getLValueWildCard("sugariter");
        WildcardMatch.MemberFunctionInvokationWildcard nextCall = wildcardMatch.getMemberFunction("nextfn", "next", (Expression)new LValueExpression(wildcardMatch.getLValueWildCard("iterable")));
        if (!wildcardMatch.match(new AssignmentSimple(sugariterWC, nextCall), loopStart.containedStatement)) {
            if (wildcardMatch.match(new AssignmentSimple(sugariterWC, wildcardMatch.getCastExpressionWildcard("cast", nextCall)), loopStart.containedStatement)) {
                isCastExpression = true;
            } else {
                if (!Op03SimpleStatement.findHiddenIter(loopStart.containedStatement, sugariterWC, nextCall)) {
                    return;
                }
                hiddenIter = true;
            }
        }
        LValue sugarIter = wildcardMatch.getLValueWildCard("sugariter").getMatch();
        if (!wildcardMatch.match(new AssignmentSimple(wildcardMatch.getLValueWildCard("iterable"), wildcardMatch.getMemberFunction("iterator", "iterator", (Expression)wildcardMatch.getExpressionWildCard("iteratorsource"))), preceeding.containedStatement)) {
            return;
        }
        Expression iterSource = wildcardMatch.getExpressionWildCard("iteratorsource").getMatch();
        BlockIdentifier blockIdentifier = whileStatement.getBlockIdentifier();
        List<Op03SimpleStatement> statementsInBlock = Functional.filter(statements, new Predicate<Op03SimpleStatement>(blockIdentifier){
            final /* synthetic */ BlockIdentifier val$blockIdentifier;

            @Override
            public boolean test(Op03SimpleStatement in) {
                return in.containedInBlocks.contains(this.val$blockIdentifier);
            }
        });
        LValueUsageCollectorSimple usageCollector = new LValueUsageCollectorSimple();
        for (Op03SimpleStatement inBlock : statementsInBlock) {
            LValue updated;
            if (inBlock == loopStart) continue;
            Statement inStatement = inBlock.containedStatement;
            inStatement.collectLValueUsage(usageCollector);
            if (usageCollector.isUsed(iterable)) {
                return;
            }
            if ((updated = inStatement.getCreatedLValue()) == null) continue;
            if (!updated.equals(sugarIter) && !updated.equals(iterable)) continue;
            return;
        }
        AtomicBoolean res = new AtomicBoolean();
        GraphVisitorDFS<Op03SimpleStatement> graphVisitor = new GraphVisitorDFS<Op03SimpleStatement>(loop, (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(){

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                if (!(Op03SimpleStatement.this == arg1 || arg1.getBlockIdentifiers().contains(blockIdentifier))) {
                    Statement inStatement;
                    AssignmentSimple assignmentSimple;
                    if (inStatement = arg1.getStatement() instanceof AssignmentSimple && iterable.equals((assignmentSimple = (AssignmentSimple)inStatement).getCreatedLValue())) {
                        return;
                    }
                    LValueUsageCollectorSimple usageCollector = new LValueUsageCollectorSimple();
                    inStatement.collectLValueUsage(usageCollector);
                    if (usageCollector.isUsed(iterable)) {
                        res.set(true);
                        return;
                    }
                }
                for (Op03SimpleStatement target : arg1.getTargets()) {
                    arg2.enqueue(target);
                }
            }
        });
        graphVisitor.process();
        if (res.get()) {
            return;
        }
        loop.replaceStatement(new ForIterStatement(blockIdentifier, sugarIter, iterSource));
        if (hiddenIter) {
            Op03SimpleStatement.replaceHiddenIter(loopStart.containedStatement, sugariterWC.getMatch(), nextCall);
        } else {
            loopStart.nopOut();
        }
        preceeding.nopOut();
    }

    public static void rewriteIteratorWhileLoops(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> loops = Functional.filter(statements, new TypeFilter(WhileStatement.class));
        for (Op03SimpleStatement loop : loops) {
            Op03SimpleStatement.rewriteIteratorWhileLoop(loop, statements);
        }
    }

    private static boolean anyOpHasEffect(List<Op03SimpleStatement> ops) {
        Iterator<Op03SimpleStatement> i$ = ops.iterator();
        while (i$.hasNext()) {
            Op03SimpleStatement op;
            Statement stm;
            Class stmcls;
            if ((stmcls = (stm = (op = i$.next()).getStatement()).getClass()) == GotoStatement.class) continue;
            if (stmcls == ThrowStatement.class) continue;
            if (stmcls == CommentStatement.class) continue;
            if (stm instanceof ReturnStatement) continue;
            return true;
        }
        return false;
    }

    public static void findSynchronizedRange(Op03SimpleStatement start, Expression monitor) {
        Set addToBlock = SetFactory.newSet();
        Set foundExits = SetFactory.newSet();
        Set extraNodes = SetFactory.newSet();
        Set leaveExitsMutex = SetFactory.newSet();
        GraphVisitorDFS<Op03SimpleStatement> marker = new GraphVisitorDFS<Op03SimpleStatement>(start.getTargets(), (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(monitor, leaveExitsMutex, foundExits, addToBlock, extraNodes){
            final /* synthetic */ Expression val$monitor;
            final /* synthetic */ Set val$leaveExitsMutex;
            final /* synthetic */ Set val$foundExits;
            final /* synthetic */ Set val$addToBlock;
            final /* synthetic */ Set val$extraNodes;

            @Override
            public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                TryStatement tryStatement;
                Set<Expression> tryMonitors;
                Statement statement = arg1.getStatement();
                if (statement instanceof TryStatement && (tryMonitors = (tryStatement = (TryStatement)statement).getMonitors()).contains(this.val$monitor)) {
                    this.val$leaveExitsMutex.add(tryStatement.getBlockIdentifier());
                    List<Op03SimpleStatement> tgts = arg1.getTargets();
                    int len = tgts.size();
                    for (int x = 1; x < len; ++x) {
                        Statement innerS;
                        if (innerS = tgts.get(x).getStatement() instanceof CatchStatement) {
                            this.val$leaveExitsMutex.add(((CatchStatement)innerS).getCatchBlockIdent());
                            continue;
                        }
                        if (!(innerS instanceof FinallyStatement)) continue;
                        this.val$leaveExitsMutex.add(((FinallyStatement)innerS).getFinallyBlockIdent());
                    }
                }
                if (statement instanceof MonitorExitStatement && this.val$monitor.equals(((MonitorExitStatement)statement).getMonitor())) {
                    Statement targetStatement;
                    this.val$foundExits.add(arg1);
                    this.val$addToBlock.add(arg1);
                    if (arg1.targets.size() != 1 || !(targetStatement = (arg1 = (Op03SimpleStatement)arg1.targets.get(0)).containedStatement instanceof ReturnStatement) && !(targetStatement instanceof ThrowStatement) && !(targetStatement instanceof Nop) && !(targetStatement instanceof GotoStatement)) return;
                    this.val$extraNodes.add(arg1);
                    return;
                }
                this.val$addToBlock.add(arg1);
                if (SetUtil.hasIntersection(arg1.getBlockIdentifiers(), this.val$leaveExitsMutex)) {
                    for (Op03SimpleStatement tgt : arg1.getTargets()) {
                        if (!SetUtil.hasIntersection(tgt.getBlockIdentifiers(), this.val$leaveExitsMutex)) continue;
                        arg2.enqueue(tgt);
                    }
                } else {
                    arg2.enqueue(arg1.getTargets());
                }
            }
        });
        marker.process();
        addToBlock.remove(start);
        Set requiredComments = SetFactory.newSet();
        Iterator foundExitIter = foundExits.iterator();
        while (foundExitIter.hasNext()) {
            Op03SimpleStatement foundExit = (Op03SimpleStatement)foundExitIter.next();
            Set<BlockIdentifier> exitBlocks = SetFactory.newSet(foundExit.getBlockIdentifiers());
            exitBlocks.removeAll(start.getBlockIdentifiers());
            List added = ListFactory.newList();
            GraphVisitorDFS<Op03SimpleStatement> additional = new GraphVisitorDFS<Op03SimpleStatement>(foundExit, (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)new BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>(exitBlocks, foundExit, addToBlock, added){
                final /* synthetic */ Set val$exitBlocks;
                final /* synthetic */ Op03SimpleStatement val$foundExit;
                final /* synthetic */ Set val$addToBlock;
                final /* synthetic */ List val$added;

                @Override
                public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
                    if (!SetUtil.hasIntersection(this.val$exitBlocks, arg1.getBlockIdentifiers())) return;
                    if (arg1 == this.val$foundExit) {
                        arg2.enqueue(arg1.getTargets());
                    } else {
                        if (!this.val$addToBlock.add(arg1)) return;
                        this.val$added.add(arg1);
                        arg2.enqueue(arg1.getTargets());
                    }
                }
            });
            additional.process();
            if (!Op03SimpleStatement.anyOpHasEffect(added)) continue;
            requiredComments.add((Op03SimpleStatement)foundExit);
            foundExitIter.remove();
        }
        MonitorEnterStatement monitorEnterStatement = (MonitorEnterStatement)start.containedStatement;
        BlockIdentifier blockIdentifier = monitorEnterStatement.getBlockIdentifier();
        for (Op03SimpleStatement contained : addToBlock) {
            contained.containedInBlocks.add(blockIdentifier);
        }
        for (Op03SimpleStatement exit2 : foundExits) {
            exit2.nopOut();
        }
        for (Op03SimpleStatement exit2 : requiredComments) {
            exit2.replaceStatement(new CommentStatement("MONITOREXIT " + exit2));
        }
        for (Op03SimpleStatement extra : extraNodes) {
            boolean allParents = true;
            for (Op03SimpleStatement source : extra.sources) {
                if (source.containedInBlocks.contains(blockIdentifier)) continue;
                allParents = false;
            }
            if (!allParents) continue;
            extra.containedInBlocks.add(blockIdentifier);
        }
    }

    public static void findSynchronizedBlocks(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> enters = Functional.filter(statements, new TypeFilter(MonitorEnterStatement.class));
        for (Op03SimpleStatement enter : enters) {
            MonitorEnterStatement monitorExitStatement = (MonitorEnterStatement)enter.containedStatement;
            Op03SimpleStatement.findSynchronizedRange(enter, monitorExitStatement.getMonitor());
        }
    }

    public static void rejoinBlocks(List<Op03SimpleStatement> statements) {
        Set lastBlocks = SetFactory.newSet();
        Set haveLeft = SetFactory.newSet();
        Set blackListed = SetFactory.newSet();
        int len = statements.size();
        for (int x = 0; x < len; ++x) {
            Statement stmInner;
            Op03SimpleStatement stm;
            if (stmInner = (stm = statements.get(x)).getStatement() instanceof CatchStatement) {
                CatchStatement catchStatement = (CatchStatement)stmInner;
                for (ExceptionGroup.Entry entry : catchStatement.getExceptions()) {
                    blackListed.add((BlockIdentifier)entry.getTryBlockIdentifier());
                }
            }
            Set<BlockIdentifier> blocks = stm.getBlockIdentifiers();
            blocks.removeAll(blackListed);
            Iterator<ExceptionGroup.Entry> i$ = blocks.iterator();
            block2 : while (i$.hasNext()) {
                BlockIdentifier ident;
                if (!haveLeft.contains(ident = (BlockIdentifier)i$.next())) continue;
                for (int y = x - 1; y >= 0; --y) {
                    Op03SimpleStatement backFill;
                    if (!(backFill = statements.get(y)).getBlockIdentifiers().add(ident)) continue block2;
                }
            }
            i$ = lastBlocks.iterator();
            while (i$.hasNext()) {
                BlockIdentifier wasIn;
                if (blocks.contains(wasIn = (BlockIdentifier)i$.next())) continue;
                haveLeft.add((BlockIdentifier)wasIn);
            }
            lastBlocks = blocks;
        }
    }

    private static void removePointlessSwitchDefault(Op03SimpleStatement swtch) {
        SwitchStatement switchStatement = (SwitchStatement)swtch.getStatement();
        BlockIdentifier switchBlock = switchStatement.getSwitchBlock();
        Iterator<Op03SimpleStatement> i$ = swtch.getTargets().iterator();
        while (i$.hasNext()) {
            Statement statement;
            Op03SimpleStatement tgt;
            CaseStatement caseStatement;
            if (!(statement = (tgt = i$.next()).getStatement() instanceof CaseStatement) || (caseStatement = (CaseStatement)statement).getSwitchBlock() != switchBlock || !caseStatement.isDefault()) continue;
            if (tgt.targets.size() != 1) {
                return;
            }
            Op03SimpleStatement afterTgt = tgt.targets.get(0);
            if (afterTgt.containedInBlocks.contains(switchBlock)) {
                return;
            }
            tgt.nopOut();
            return;
        }
    }

    public static void removePointlessSwitchDefaults(List<Op03SimpleStatement> statements) {
        List<Op03SimpleStatement> switches = Functional.filter(statements, new TypeFilter(SwitchStatement.class));
        for (Op03SimpleStatement swtch : switches) {
            Op03SimpleStatement.removePointlessSwitchDefault(swtch);
        }
    }

    private static boolean resugarAnonymousArray(Op03SimpleStatement newArray, List<Op03SimpleStatement> statements) {
        LValue arrayLValue;
        Expression dimSize0;
        Literal lit;
        AbstractNewArray arrayDef;
        AssignmentSimple assignmentSimple = (AssignmentSimple)newArray.containedStatement;
        WildcardMatch start = new WildcardMatch();
        if (!start.match(new AssignmentSimple(start.getLValueWildCard("array"), start.getNewArrayWildCard("def")), assignmentSimple)) {
            throw new ConfusedCFRException("Expecting new array");
        }
        if (!(arrayLValue = start.getLValueWildCard("array").getMatch() instanceof StackSSALabel || arrayLValue instanceof LocalVariable)) {
            return false;
        }
        LValue array = arrayLValue;
        if (!(dimSize0 = (arrayDef = start.getNewArrayWildCard("def").getMatch()).getDimSize(0) instanceof Literal)) {
            return false;
        }
        if ((lit = (Literal)dimSize0).getValue().getType() != TypedLiteral.LiteralType.Integer) {
            return false;
        }
        int bound = (Integer)lit.getValue().getValue();
        Op03SimpleStatement next = newArray;
        List anon = ListFactory.newList();
        List anonAssigns = ListFactory.newList();
        AbstractExpression arrayExpression = null;
        arrayExpression = array instanceof StackSSALabel ? new StackValue((StackSSALabel)array) : new LValueExpression(array);
        for (int x = 0; x < bound; ++x) {
            WildcardMatch testAnon;
            Literal idx;
            if (next.targets.size() != 1) {
                return false;
            }
            next = next.targets.get(0);
            if (!(testAnon = new WildcardMatch()).match(new AssignmentSimple(new ArrayVariable(new ArrayIndex(arrayExpression, idx = new Literal(TypedLiteral.getInt(x)))), (testAnon = new WildcardMatch()).getExpressionWildCard("val")), next.containedStatement)) {
                return false;
            }
            anon.add((Expression)testAnon.getExpressionWildCard("val").getMatch());
            anonAssigns.add((Op03SimpleStatement)next);
        }
        AssignmentSimple replacement = new AssignmentSimple(arrayLValue.getInferredJavaType(), assignmentSimple.getCreatedLValue(), new NewAnonymousArray(arrayDef.getInferredJavaType(), arrayDef.getNumDims(), anon, false));
        newArray.replaceStatement(replacement);
        if (array instanceof StackSSALabel) {
            StackEntry arrayStackEntry = ((StackSSALabel)array).getStackEntry();
            for (Op03SimpleStatement create : anonAssigns) {
                arrayStackEntry.decrementUsage();
            }
        }
        for (Op03SimpleStatement create : anonAssigns) {
            create.nopOut();
        }
        return true;
    }

    public static void resugarAnonymousArrays(List<Op03SimpleStatement> statements) {
        boolean success = false;
        do {
            List<Op03SimpleStatement> assignments = Functional.filter(statements, new TypeFilter(AssignmentSimple.class));
            assignments = Functional.filter(assignments, new Predicate<Op03SimpleStatement>(){

                @Override
                public boolean test(Op03SimpleStatement in) {
                    AssignmentSimple assignmentSimple = (AssignmentSimple)in.containedStatement;
                    WildcardMatch wildcardMatch = new WildcardMatch();
                    return wildcardMatch.match(new AssignmentSimple(wildcardMatch.getLValueWildCard("array"), wildcardMatch.getNewArrayWildCard("def", 1, null)), assignmentSimple);
                }
            });
            success = false;
            for (Op03SimpleStatement assignment : assignments) {
                success|=Op03SimpleStatement.resugarAnonymousArray(assignment, statements);
            }
            if (!success) continue;
            Op03SimpleStatement.condenseLValues(statements);
        } while (success);
    }

    public static void inferGenericObjectInfoFromCalls(List<Op03SimpleStatement> statements) {
        List memberFunctionInvokations = ListFactory.newList();
        Iterator<Op03SimpleStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            Expression e;
            Statement contained;
            Op03SimpleStatement statement;
            if (contained = (statement = i$.next()).getStatement() instanceof ExpressionStatement) {
                if (!(e = ((ExpressionStatement)contained).getExpression() instanceof MemberFunctionInvokation)) continue;
                memberFunctionInvokations.add((MemberFunctionInvokation)((MemberFunctionInvokation)e));
                continue;
            }
            if (!(contained instanceof AssignmentSimple) || !(e = ((AssignmentSimple)contained).getRValue() instanceof MemberFunctionInvokation)) continue;
            memberFunctionInvokations.add((MemberFunctionInvokation)((MemberFunctionInvokation)e));
        }
        TreeMap byTypKey = MapFactory.newTreeMap();
        Functional.groupToMapBy(memberFunctionInvokations, byTypKey, new UnaryFunction<MemberFunctionInvokation, Integer>(){

            @Override
            public Integer invoke(MemberFunctionInvokation arg) {
                return arg.getObject().getInferredJavaType().getLocalId();
            }
        });
        block1 : for (Map.Entry entry : byTypKey.entrySet()) {
            List invokations;
            Expression obj0;
            JavaGenericBaseInstance genericType;
            JavaTypeInstance type;
            GenericTypeBinder gtb0;
            Integer key = (Integer)entry.getKey();
            if ((invokations = (List)entry.getValue()).isEmpty()) continue;
            if (!(type = (obj0 = ((MemberFunctionInvokation)invokations.get(0)).getObject()).getInferredJavaType().getJavaTypeInstance() instanceof JavaGenericBaseInstance)) continue;
            if (!(genericType = (JavaGenericBaseInstance)type).hasUnbound()) continue;
            if ((gtb0 = Op03SimpleStatement.getGtb((MemberFunctionInvokation)invokations.get(0))) == null) continue;
            int len = invokations.size();
            for (int x = 1; x < len; ++x) {
                GenericTypeBinder gtb;
                if ((gtb = Op03SimpleStatement.getGtb((MemberFunctionInvokation)invokations.get(x))) == null) continue block1;
                if ((gtb0 = gtb0.mergeWith(gtb, true)) == null) continue block1;
            }
            obj0.getInferredJavaType().deGenerify(gtb0.getBindingFor(obj0.getInferredJavaType().getJavaTypeInstance()));
        }
    }

    public static boolean checkTypeClashes(List<Op03SimpleStatement> statements, BytecodeMeta bytecodeMeta) {
        LValueTypeClashCheck clashCheck = new LValueTypeClashCheck();
        for (Op03SimpleStatement statement : statements) {
            statement.getStatement().collectLValueUsage(clashCheck);
        }
        if (clashCheck.clashes.isEmpty()) return false;
        bytecodeMeta.informLivenessClashes(clashCheck.clashes);
        return true;
    }

    public static void labelAnonymousBlocks(List<Op03SimpleStatement> statements, BlockIdentifierFactory blockIdentifierFactory) {
        List<Op03SimpleStatement> anonBreaks = Functional.filter(statements, new Predicate<Op03SimpleStatement>(){

            @Override
            public boolean test(Op03SimpleStatement in) {
                JumpType jumpType;
                Statement statement = in.getStatement();
                if (!(statement instanceof JumpingStatement)) {
                    return false;
                }
                return (jumpType = ((JumpingStatement)statement).getJumpType()) == JumpType.BREAK_ANONYMOUS;
            }
        });
        if (anonBreaks.isEmpty()) {
            return;
        }
        Set targets = SetFactory.newOrderedSet();
        for (Op03SimpleStatement anonBreak : anonBreaks) {
            JumpingStatement jumpingStatement = (JumpingStatement)anonBreak.getStatement();
            targets.add((Op03SimpleStatement)((Op03SimpleStatement)jumpingStatement.getJumpTarget().getContainer()));
        }
        boolean idx = false;
        for (Op03SimpleStatement target : targets) {
            BlockIdentifier blockIdentifier = blockIdentifierFactory.getNextBlockIdentifier(BlockType.ANONYMOUS);
            InstrIndex targetIndex = target.getIndex();
            Op03SimpleStatement anonTarget = new Op03SimpleStatement(target.getBlockIdentifiers(), new AnonBreakTarget(blockIdentifier), targetIndex.justBefore());
            List<Op03SimpleStatement> sources = ListFactory.newList(target.getSources());
            for (Op03SimpleStatement source : sources) {
                if (!targetIndex.isBackJumpTo(source)) continue;
                target.removeSource(source);
                source.replaceTarget(target, anonTarget);
                anonTarget.addSource(source);
            }
            target.addSource(anonTarget);
            anonTarget.addTarget(target);
            int pos = statements.indexOf(target);
            statements.add(pos, anonTarget);
        }
    }

    public static void replaceStackVarsWithLocals(List<Op03SimpleStatement> statements) {
        StackVarToLocalRewriter rewriter = new StackVarToLocalRewriter();
        for (Op03SimpleStatement statement : statements) {
            statement.rewrite(rewriter);
        }
    }

    static GenericTypeBinder getGtb(MemberFunctionInvokation m) {
        return m.getMethodPrototype().getTypeBinderFor(m.getArgs());
    }

    public String toString() {
        Set blockIds = SetFactory.newSet();
        for (BlockIdentifier b : this.containedInBlocks) {
            blockIds.add(b.getIndex());
        }
        return "" + blockIds + " " + this.index + " : " + this.containedStatement;
    }

    public static class LValueTypeClashCheck
    implements LValueUsageCollector {
        Set<Integer> clashes = SetFactory.newSet();

        @Override
        public void collect(LValue lValue) {
            int idx;
            lValue.collectLValueUsage(this);
            InferredJavaType inferredJavaType = lValue.getInferredJavaType();
            if (inferredJavaType == null || !inferredJavaType.isClash() || !(lValue instanceof LocalVariable) || (idx = ((LocalVariable)lValue).getIdx()) < 0) return;
            this.clashes.add(idx);
        }
    }

    static class NodeReachable
    implements BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>> {
        private final Set<Op03SimpleStatement> otherCases;
        private final Op03SimpleStatement switchStatement;
        private final Op03SimpleStatement start;
        private final List<Op03SimpleStatement> reaches = ListFactory.newList();
        private final Set<Op03SimpleStatement> inBlock = SetFactory.newSet();

        private NodeReachable(Set<Op03SimpleStatement> otherCases, Op03SimpleStatement start, Op03SimpleStatement switchStatement) {
            this.otherCases = otherCases;
            this.switchStatement = switchStatement;
            this.start = start;
        }

        @Override
        public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
            if (arg1 == this.switchStatement) {
                return;
            }
            if (arg1.getIndex().isBackJumpFrom(this.start) && arg1.getIndex().isBackJumpFrom(this.switchStatement)) {
                return;
            }
            if (arg1 != this.start && this.otherCases.contains(arg1)) {
                this.reaches.add(arg1);
                return;
            }
            this.inBlock.add(arg1);
            arg2.enqueue(arg1.getTargets());
        }

        /* synthetic */ NodeReachable(Set x0, Op03SimpleStatement x1, Op03SimpleStatement x2,  x3) {
            this(x0, x1, x2);
        }
    }

    static final class FindBlockStarts
    implements Predicate<Op03SimpleStatement> {
        private final BlockType blockType;

        public FindBlockStarts(BlockType blockType) {
            this.blockType = blockType;
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            BlockIdentifier blockIdentifier = in.firstStatementInThisBlock;
            if (blockIdentifier == null) {
                return false;
            }
            return blockIdentifier.getBlockType() == this.blockType;
        }
    }

    class GraphVisitorBlockReachable
    implements BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>> {
        private final Op03SimpleStatement start;
        private final BlockIdentifier blockIdentifier;
        private final Set<Op03SimpleStatement> found = SetFactory.newSet();

        private GraphVisitorBlockReachable(Op03SimpleStatement start, BlockIdentifier blockIdentifier) {
            this.start = start;
            this.blockIdentifier = blockIdentifier;
        }

        @Override
        public void call(Op03SimpleStatement arg1, GraphVisitor<Op03SimpleStatement> arg2) {
            if (arg1 != this.start && !arg1.getBlockIdentifiers().contains(this.blockIdentifier)) return;
            this.found.add(arg1);
            for (Op03SimpleStatement target : arg1.getTargets()) {
                arg2.enqueue(target);
            }
        }

        public Set<Op03SimpleStatement> run() {
            GraphVisitorDFS<Op03SimpleStatement> reachableInBlock = new GraphVisitorDFS<Op03SimpleStatement>(this.start, (BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>>)this);
            reachableInBlock.process();
            return this.found;
        }

        /* synthetic */ GraphVisitorBlockReachable(Op03SimpleStatement x0, BlockIdentifier x1,  x2) {
            this(x0, x1);
        }
    }

    public static class ExactTypeFilter<T>
    implements Predicate<Op03SimpleStatement> {
        private final Class<T> clazz;
        private final boolean positive;

        public ExactTypeFilter(Class<T> clazz) {
            this.clazz = clazz;
            this.positive = true;
        }

        public ExactTypeFilter(Class<T> clazz, boolean positive) {
            this.clazz = clazz;
            this.positive = positive;
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            return this.positive == this.clazz == in.containedStatement.getClass();
        }
    }

    public static class TypeFilter<T>
    implements Predicate<Op03SimpleStatement> {
        private final Class<T> clazz;
        private final boolean positive;

        public TypeFilter(Class<T> clazz) {
            this.clazz = clazz;
            this.positive = true;
        }

        public TypeFilter(Class<T> clazz, boolean positive) {
            this.clazz = clazz;
            this.positive = positive;
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            return this.positive == this.clazz.isInstance(in.containedStatement);
        }
    }

    static class DiscoveredTernary {
        LValue lValue;
        Expression e1;
        Expression e2;

        private DiscoveredTernary(LValue lValue, Expression e1, Expression e2) {
            this.lValue = lValue;
            this.e1 = e1;
            this.e2 = e2;
        }

        private static Troolean isOneOrZeroLiteral(Expression e) {
            int iValue;
            TypedLiteral typedLiteral;
            Object value;
            if (!(e instanceof Literal)) {
                return Troolean.NEITHER;
            }
            if (!(value = (typedLiteral = ((Literal)e).getValue()).getValue() instanceof Integer)) {
                return Troolean.NEITHER;
            }
            if ((iValue = ((Integer)value).intValue()) == 1) {
                return Troolean.TRUE;
            }
            if (iValue != 0) return Troolean.NEITHER;
            return Troolean.FALSE;
        }

        private boolean isPointlessBoolean() {
            if (this.e1.getInferredJavaType().getRawType() != RawJavaType.BOOLEAN || this.e2.getInferredJavaType().getRawType() != RawJavaType.BOOLEAN) {
                return false;
            }
            if (DiscoveredTernary.isOneOrZeroLiteral(this.e1) != Troolean.TRUE) {
                return false;
            }
            if (DiscoveredTernary.isOneOrZeroLiteral(this.e2) == Troolean.FALSE) return true;
            return false;
        }

        /* synthetic */ DiscoveredTernary(LValue x0, Expression x1, Expression x2,  x3) {
            this(x0, x1, x2);
        }
    }

    static class IsForwardIf
    implements Predicate<Op03SimpleStatement> {
        private IsForwardIf() {
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            IfStatement ifStatement;
            if (!(in.containedStatement instanceof IfStatement)) {
                return false;
            }
            if (!(ifStatement = (IfStatement)in.containedStatement).getJumpType().isUnknown()) {
                return false;
            }
            if (((Op03SimpleStatement)in.targets.get(1)).index.compareTo(in.index) > 0) return true;
            return false;
        }

        /* synthetic */ IsForwardIf( x0) {
            this();
        }
    }

    static class LoopResult {
        final BlockIdentifier blockIdentifier;
        final Op03SimpleStatement blockStart;

        private LoopResult(BlockIdentifier blockIdentifier, Op03SimpleStatement blockStart) {
            this.blockIdentifier = blockIdentifier;
            this.blockStart = blockStart;
        }

        /* synthetic */ LoopResult(BlockIdentifier x0, Op03SimpleStatement x1,  x2) {
            this(x0, x1);
        }
    }

    static class GraphVisitorReachableInThese
    implements BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>> {
        private final Set<Integer> reachable;
        private final Map<Op03SimpleStatement, Integer> instrToIdx;

        public GraphVisitorReachableInThese(Set<Integer> reachable, Map<Op03SimpleStatement, Integer> instrToIdx) {
            this.reachable = reachable;
            this.instrToIdx = instrToIdx;
        }

        @Override
        public void call(Op03SimpleStatement node, GraphVisitor<Op03SimpleStatement> graphVisitor) {
            Integer idx = this.instrToIdx.get(node);
            if (idx == null) {
                return;
            }
            this.reachable.add(idx);
            for (Op03SimpleStatement target : node.targets) {
                graphVisitor.enqueue(target);
            }
        }
    }

    static class GetBackJump
    implements UnaryFunction<Op03SimpleStatement, Op03SimpleStatement> {
        private GetBackJump() {
        }

        @Override
        public Op03SimpleStatement invoke(Op03SimpleStatement in) {
            InstrIndex inIndex = in.getIndex();
            List<Op03SimpleStatement> targets = in.getTargets();
            for (Op03SimpleStatement target : targets) {
                if (target.getIndex().compareTo(inIndex) > 0) continue;
                return target;
            }
            throw new ConfusedCFRException("No back index.");
        }

        /* synthetic */ GetBackJump( x0) {
            this();
        }
    }

    static class HasBackJump
    implements Predicate<Op03SimpleStatement> {
        private HasBackJump() {
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            InstrIndex inIndex = in.getIndex();
            List<Op03SimpleStatement> targets = in.getTargets();
            for (Op03SimpleStatement target : targets) {
                if (target.getIndex().compareTo(inIndex) > 0) continue;
                if (in.containedStatement instanceof JumpingStatement) return true;
                if (!(in.containedStatement instanceof JSRRetStatement) && !(in.containedStatement instanceof WhileStatement)) throw new ConfusedCFRException("Invalid back jump on " + in.containedStatement);
                return false;
            }
            return false;
        }

        /* synthetic */ HasBackJump( x0) {
            this();
        }
    }

    public static class IsForwardJumpTo
    implements Predicate<Op03SimpleStatement> {
        private final InstrIndex thisIndex;

        public IsForwardJumpTo(InstrIndex thisIndex) {
            this.thisIndex = thisIndex;
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            return this.thisIndex.isBackJumpTo(in);
        }
    }

    public static class IsBackJumpTo
    implements Predicate<Op03SimpleStatement> {
        private final InstrIndex thisIndex;

        public IsBackJumpTo(InstrIndex thisIndex) {
            this.thisIndex = thisIndex;
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            return this.thisIndex.isBackJumpFrom(in);
        }
    }

    static class StatementCanBePostMutation
    implements Predicate<Op03SimpleStatement> {
        private StatementCanBePostMutation() {
        }

        @Override
        public boolean test(Op03SimpleStatement in) {
            AssignmentPreMutation assignmentPreMutation = (AssignmentPreMutation)in.getStatement();
            LValue lValue = assignmentPreMutation.getCreatedLValue();
            return assignmentPreMutation.isSelfMutatingOp1(lValue, ArithOp.PLUS) || assignmentPreMutation.isSelfMutatingOp1(lValue, ArithOp.MINUS);
        }

        /* synthetic */ StatementCanBePostMutation( x0) {
            this();
        }
    }

    static class UsageWatcher
    implements LValueRewriter<Statement> {
        private final LValue needle;
        boolean found = false;

        private UsageWatcher(LValue needle) {
            this.needle = needle;
        }

        @Override
        public Expression getLValueReplacement(LValue lValue, SSAIdentifiers ssaIdentifiers, StatementContainer<Statement> statementContainer) {
            return null;
        }

        @Override
        public boolean explicitlyReplaceThisLValue(LValue lValue) {
            return true;
        }

        public boolean isFound() {
            return this.found;
        }

        /* synthetic */ UsageWatcher(LValue x0,  x1) {
            this(x0);
        }
    }

    public static class CompareByIndex
    implements Comparator<Op03SimpleStatement> {
        @Override
        public int compare(Op03SimpleStatement a, Op03SimpleStatement b) {
            int res = a.getIndex().compareTo(b.getIndex());
            if (res != 0) return res;
            throw new ConfusedCFRException("Can't sort instructions:\n" + a + "\n" + b);
        }
    }

    public class GraphVisitorCallee
    implements BinaryProcedure<Op03SimpleStatement, GraphVisitor<Op03SimpleStatement>> {
        private final List<Op03SimpleStatement> reachableNodes;

        public GraphVisitorCallee(List<Op03SimpleStatement> reachableNodes) {
            this.reachableNodes = reachableNodes;
        }

        @Override
        public void call(Op03SimpleStatement node, GraphVisitor<Op03SimpleStatement> graphVisitor) {
            this.reachableNodes.add(node);
            for (Op03SimpleStatement target : node.targets) {
                graphVisitor.enqueue(target);
            }
        }
    }

}

