/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Logger;
import org.benf.cfr.reader.bytecode.BytecodeMeta;
import org.benf.cfr.reader.bytecode.analysis.opgraph.InstrIndex;
import org.benf.cfr.reader.bytecode.analysis.opgraph.MutableGraph;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.InnerClassConstructorRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LValueReplacingRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.LambdaRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.PrimitiveBoxingRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.RedundantSuperRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SyntheticAccessorRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SyntheticOuterRefRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.VarArgsRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.checker.Op04Checker;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.BadLoopPrettifier;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.CanRemovePointlessBlock;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.TypedBooleanTidier;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.VariableNameTidier;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnoynmousInner;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.FieldVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscovererImpl;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredAnonymousBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredCatch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredGoto;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.UnstructuredWhile;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableFactory;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.DecompilerComments;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.SetFactory;
import org.benf.cfr.reader.util.StackFactory;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.LoggerFactory;

public class Op04StructuredStatement
implements MutableGraph<Op04StructuredStatement>,
Dumpable,
StatementContainer<StructuredStatement>,
TypeUsageCollectable {
    private static final Logger logger = LoggerFactory.create(Op04StructuredStatement.class);
    private InstrIndex instrIndex;
    private List<Op04StructuredStatement> sources = ListFactory.newList();
    private List<Op04StructuredStatement> targets = ListFactory.newList();
    private StructuredStatement structuredStatement;
    private Set<BlockIdentifier> blockMembership;
    private static final Set<BlockIdentifier> EMPTY_BLOCKSET = SetFactory.newSet();

    private static Set<BlockIdentifier> blockSet(Collection<BlockIdentifier> in) {
        if (in != null && !in.isEmpty()) return SetFactory.newSet(in);
        return Op04StructuredStatement.EMPTY_BLOCKSET;
    }

    public Op04StructuredStatement(StructuredStatement justStatement) {
        this.structuredStatement = justStatement;
        this.instrIndex = new InstrIndex(-1000);
        this.blockMembership = Op04StructuredStatement.EMPTY_BLOCKSET;
        justStatement.setContainer(this);
    }

    public Op04StructuredStatement(InstrIndex instrIndex, Collection<BlockIdentifier> blockMembership, StructuredStatement structuredStatement) {
        this.instrIndex = instrIndex;
        this.structuredStatement = structuredStatement;
        this.blockMembership = Op04StructuredStatement.blockSet(blockMembership);
        structuredStatement.setContainer(this);
    }

    public Op04StructuredStatement nopThisAndReplace() {
        Op04StructuredStatement replacement = new Op04StructuredStatement(this.instrIndex, (Collection<BlockIdentifier>)this.blockMembership, this.structuredStatement);
        this.replaceStatementWithNOP("");
        Op04StructuredStatement.replaceInSources(this, replacement);
        Op04StructuredStatement.replaceInTargets(this, replacement);
        return replacement;
    }

    public void nopThis() {
        this.replaceStatementWithNOP("");
    }

    @Override
    public StructuredStatement getStatement() {
        return this.structuredStatement;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        this.structuredStatement.collectTypeUsages(collector);
    }

    @Override
    public StructuredStatement getTargetStatement(int idx) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLabel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InstrIndex getIndex() {
        return this.instrIndex;
    }

    @Override
    public void nopOut() {
        this.replaceStatementWithNOP("");
    }

    @Override
    public void replaceStatement(StructuredStatement newTarget) {
        this.structuredStatement = newTarget;
    }

    @Override
    public void nopOutConditional() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SSAIdentifiers getSSAIdentifiers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<BlockIdentifier> getBlockIdentifiers() {
        return this.blockMembership;
    }

    @Override
    public BlockIdentifier getBlockStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<BlockIdentifier> getBlocksEnded() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void copyBlockInformationFrom(StatementContainer<StructuredStatement> other) {
        throw new UnsupportedOperationException();
    }

    private boolean hasUnstructuredSource() {
        for (Op04StructuredStatement source : this.sources) {
            if (source.structuredStatement.isProperlyStructured()) continue;
            return true;
        }
        return false;
    }

    public Collection<BlockIdentifier> getBlockMembership() {
        return this.blockMembership;
    }

    @Override
    public Dumper dump(Dumper dumper) {
        if (this.hasUnstructuredSource()) {
            dumper.printLabel(this.instrIndex.toString() + ": // " + this.sources.size() + " sources");
        }
        this.structuredStatement.dump(dumper);
        return dumper;
    }

    @Override
    public List<Op04StructuredStatement> getSources() {
        return this.sources;
    }

    @Override
    public List<Op04StructuredStatement> getTargets() {
        return this.targets;
    }

    @Override
    public void addSource(Op04StructuredStatement source) {
        this.sources.add(source);
    }

    @Override
    public void addTarget(Op04StructuredStatement target) {
        this.targets.add(target);
    }

    public String getTargetLabel(int idx) {
        return this.targets.get((int)idx).instrIndex.toString();
    }

    public void traceLocalVariableScope(LValueScopeDiscoverer scopeDiscoverer) {
        this.structuredStatement.traceLocalVariableScope(scopeDiscoverer);
    }

    private void replaceAsSource(Op04StructuredStatement old) {
        Op04StructuredStatement.replaceInSources(old, this);
        this.addTarget(old);
        old.addSource(this);
    }

    public void replaceTarget(Op04StructuredStatement from, Op04StructuredStatement to) {
        int index = this.targets.indexOf(from);
        if (index == -1) {
            throw new ConfusedCFRException("Invalid target.  Trying to replace " + from + " -> " + to);
        }
        this.targets.set(index, to);
    }

    public void replaceSource(Op04StructuredStatement from, Op04StructuredStatement to) {
        int index = this.sources.indexOf(from);
        if (index == -1) {
            throw new ConfusedCFRException("Invalid source");
        }
        this.sources.set(index, to);
    }

    public void setSources(List<Op04StructuredStatement> sources) {
        this.sources = sources;
    }

    public void setTargets(List<Op04StructuredStatement> targets) {
        this.targets = targets;
    }

    public static void replaceInSources(Op04StructuredStatement original, Op04StructuredStatement replacement) {
        for (Op04StructuredStatement source : original.getSources()) {
            source.replaceTarget(original, replacement);
        }
        replacement.setSources(original.getSources());
        original.setSources(ListFactory.newList());
    }

    public static void replaceInTargets(Op04StructuredStatement original, Op04StructuredStatement replacement) {
        for (Op04StructuredStatement target : original.getTargets()) {
            target.replaceSource(original, replacement);
        }
        replacement.setTargets(original.getTargets());
        original.setTargets(ListFactory.newList());
    }

    public void linearizeStatementsInto(List<StructuredStatement> out) {
        this.structuredStatement.linearizeInto(out);
    }

    public void removeLastContinue(BlockIdentifier block) {
        if (!(this.structuredStatement instanceof Block)) {
            throw new ConfusedCFRException("Trying to remove last continue, but statement isn't block");
        }
        boolean removed = ((Block)this.structuredStatement).removeLastContinue(block);
        Op04StructuredStatement.logger.info("Removing last continue for " + block + " succeeded? " + removed);
    }

    public void removeLastGoto() {
        if (!(this.structuredStatement instanceof Block)) {
            throw new ConfusedCFRException("Trying to remove last goto, but statement isn't a block!");
        }
        ((Block)this.structuredStatement).removeLastGoto();
    }

    public void removeLastGoto(Op04StructuredStatement toHere) {
        if (!(this.structuredStatement instanceof Block)) {
            throw new ConfusedCFRException("Trying to remove last goto, but statement isn't a block!");
        }
        ((Block)this.structuredStatement).removeLastGoto(toHere);
    }

    public UnstructuredWhile removeLastEndWhile() {
        if (!(this.structuredStatement instanceof Block)) return null;
        return ((Block)this.structuredStatement).removeLastEndWhile();
    }

    public void informBlockMembership(Vector<BlockIdentifier> currentlyIn) {
        StructuredStatement replacement = this.structuredStatement.informBlockHeirachy(currentlyIn);
        if (replacement == null) {
            return;
        }
        this.structuredStatement = replacement;
        replacement.setContainer(this);
    }

    public String toString() {
        return this.structuredStatement.toString();
    }

    public void replaceStatementWithNOP(String comment) {
        this.structuredStatement = new StructuredComment(comment);
        this.structuredStatement.setContainer(this);
    }

    private boolean claimBlock(Op04StructuredStatement innerBlock, BlockIdentifier thisBlock, Vector<BlockIdentifier> currentlyIn) {
        StructuredStatement replacement;
        int idx = this.targets.indexOf(innerBlock);
        if (idx == -1) {
            return false;
        }
        if ((replacement = this.structuredStatement.claimBlock(innerBlock, thisBlock, currentlyIn)) == null) {
            return false;
        }
        this.structuredStatement = replacement;
        replacement.setContainer(this);
        return true;
    }

    public void replaceContainedStatement(StructuredStatement structuredStatement) {
        this.structuredStatement = structuredStatement;
        this.structuredStatement.setContainer(this);
    }

    private static Set<BlockIdentifier> getEndingBlocks(Stack<BlockIdentifier> wasIn, Set<BlockIdentifier> nowIn) {
        Set<BlockIdentifier> wasCopy = SetFactory.newSet(wasIn);
        wasCopy.removeAll(nowIn);
        return wasCopy;
    }

    private static BlockIdentifier getStartingBlocks(Stack<BlockIdentifier> wasIn, Set<BlockIdentifier> nowIn) {
        if (nowIn.size() <= wasIn.size()) {
            return null;
        }
        Set<BlockIdentifier> nowCopy = SetFactory.newSet(nowIn);
        nowCopy.removeAll(wasIn);
        if (nowCopy.size() == 1) return nowCopy.iterator().next();
        throw new ConfusedCFRException("Started " + nowCopy.size() + " blocks at once");
    }

    public static void processEndingBlocks(Set<BlockIdentifier> endOfTheseBlocks, Stack<BlockIdentifier> blocksCurrentlyIn, Stack<StackedBlock> stackedBlocks, MutableProcessingBlockState mutableProcessingBlockState) {
        Op04StructuredStatement.logger.fine("statement is last statement in these blocks " + endOfTheseBlocks);
        while (!endOfTheseBlocks.isEmpty()) {
            BlockIdentifier popBlockIdentifier;
            if (mutableProcessingBlockState.currentBlockIdentifier == null) {
                throw new ConfusedCFRException("Trying to end block, but not in any!");
            }
            if (!endOfTheseBlocks.remove(mutableProcessingBlockState.currentBlockIdentifier)) {
                throw new ConfusedCFRException("Tried to end blocks " + endOfTheseBlocks + ", but top level block is " + mutableProcessingBlockState.currentBlockIdentifier);
            }
            if ((popBlockIdentifier = blocksCurrentlyIn.pop()) != mutableProcessingBlockState.currentBlockIdentifier) {
                throw new ConfusedCFRException("Tried to end blocks " + endOfTheseBlocks + ", but top level block is " + mutableProcessingBlockState.currentBlockIdentifier);
            }
            LinkedList<Op04StructuredStatement> blockJustEnded = mutableProcessingBlockState.currentBlock;
            StackedBlock popBlock = stackedBlocks.pop();
            mutableProcessingBlockState.currentBlock = popBlock.statements;
            Op04StructuredStatement finishedBlock = new Op04StructuredStatement(new Block(blockJustEnded, true));
            finishedBlock.replaceAsSource(blockJustEnded.getFirst());
            Op04StructuredStatement blockStartContainer = popBlock.outerStart;
            if (!blockStartContainer.claimBlock(finishedBlock, mutableProcessingBlockState.currentBlockIdentifier, (Vector<BlockIdentifier>)blocksCurrentlyIn)) {
                mutableProcessingBlockState.currentBlock.add(finishedBlock);
            }
            mutableProcessingBlockState.currentBlockIdentifier = popBlock.blockIdentifier;
        }
    }

    public boolean isFullyStructured() {
        return this.structuredStatement.isRecursivelyStructured();
    }

    public static Op04StructuredStatement buildNestedBlocks(List<Op04StructuredStatement> containers) {
        Stack blocksCurrentlyIn = StackFactory.newStack();
        LinkedList outerBlock = ListFactory.newLinkedList();
        Stack stackedBlocks = StackFactory.newStack();
        MutableProcessingBlockState mutableProcessingBlockState = new MutableProcessingBlockState(null);
        mutableProcessingBlockState.currentBlock = outerBlock;
        for (Op04StructuredStatement container : containers) {
            Set<BlockIdentifier> endOfTheseBlocks;
            BlockIdentifier startsThisBlock;
            if (!(endOfTheseBlocks = Op04StructuredStatement.getEndingBlocks(blocksCurrentlyIn, container.blockMembership)).isEmpty()) {
                Op04StructuredStatement.processEndingBlocks(endOfTheseBlocks, blocksCurrentlyIn, stackedBlocks, mutableProcessingBlockState);
            }
            if ((startsThisBlock = Op04StructuredStatement.getStartingBlocks(blocksCurrentlyIn, container.blockMembership)) != null) {
                Op04StructuredStatement.logger.fine("Starting block " + startsThisBlock);
                BlockType blockType = startsThisBlock.getBlockType();
                Op04StructuredStatement blockClaimer = mutableProcessingBlockState.currentBlock.getLast();
                stackedBlocks.push((StackedBlock)new StackedBlock(mutableProcessingBlockState.currentBlockIdentifier, mutableProcessingBlockState.currentBlock, blockClaimer, null));
                mutableProcessingBlockState.currentBlock = ListFactory.newLinkedList();
                mutableProcessingBlockState.currentBlockIdentifier = startsThisBlock;
                blocksCurrentlyIn.push((BlockIdentifier)mutableProcessingBlockState.currentBlockIdentifier);
            }
            container.informBlockMembership(blocksCurrentlyIn);
            mutableProcessingBlockState.currentBlock.add(container);
        }
        if (!stackedBlocks.isEmpty()) {
            Op04StructuredStatement.processEndingBlocks(SetFactory.newSet(blocksCurrentlyIn), blocksCurrentlyIn, stackedBlocks, mutableProcessingBlockState);
        }
        Block result = new Block(outerBlock, true);
        return new Op04StructuredStatement(result);
    }

    public static StructuredStatement transformStructuredGotoWithScope(StructuredScope scope, StructuredStatement stm) {
        Set<Op04StructuredStatement> nextFallThrough = scope.getNextFallThrough(stm);
        List<Op04StructuredStatement> targets = stm.getContainer().getTargets();
        Op04StructuredStatement target = targets.isEmpty() ? null : targets.get(0);
        if (!nextFallThrough.contains(target)) return stm;
        if (!scope.statementIsLast(stm)) return stm;
        return new StructuredComment("");
    }

    public void transform(StructuredStatementTransformer transformer, StructuredScope scope) {
        StructuredStatement old = this.structuredStatement;
        this.structuredStatement = transformer.transform(this.structuredStatement, scope);
        if (this.structuredStatement == old || this.structuredStatement == null) return;
        this.structuredStatement.setContainer(this);
    }

    public static void insertAnonymousBlocks(Op04StructuredStatement root) {
        root.transform(new AnonymousBlockExtractor(null), new StructuredScope());
    }

    public static void tidyEmptyCatch(Op04StructuredStatement root) {
        root.transform(new EmptyCatchTidier(null), new StructuredScope());
    }

    public static void tidyTryCatch(Op04StructuredStatement root) {
        root.transform(new TryCatchTidier(null), new StructuredScope());
    }

    public static void inlinePossibles(Op04StructuredStatement root) {
        root.transform(new Inliner(null), new StructuredScope());
    }

    public static void tidyVariableNames(Method method, Op04StructuredStatement root) {
        new VariableNameTidier(method).transform(root);
    }

    public static void removePointlessReturn(Op04StructuredStatement root) {
        StructuredStatement statement = root.getStatement();
        if (!(statement instanceof Block)) return;
        Block block = (Block)statement;
        block.removeLastNVReturn();
    }

    public static void tidyTypedBooleans(Op04StructuredStatement root) {
        new TypedBooleanTidier().transform(root);
    }

    public static void prettifyBadLoops(Op04StructuredStatement root) {
        new BadLoopPrettifier().transform(root);
    }

    public static void removeStructuredGotos(Op04StructuredStatement root) {
        root.transform(new StructuredGotoRemover(null), new StructuredScope());
    }

    public static void removePointlessBlocks(Op04StructuredStatement root) {
        root.transform(new PointlessBlockRemover(null), new StructuredScope());
    }

    public static void discoverVariableScopes(Method method, Op04StructuredStatement root, VariableFactory variableFactory) {
        LValueScopeDiscovererImpl scopeDiscoverer = new LValueScopeDiscovererImpl(method.getMethodPrototype(), variableFactory);
        root.traceLocalVariableScope(scopeDiscoverer);
        scopeDiscoverer.markDiscoveredCreations();
    }

    public static boolean checkTypeClashes(Op04StructuredStatement block, BytecodeMeta bytecodeMeta) {
        LValueTypeClashCheck clashCheck = new LValueTypeClashCheck();
        block.traceLocalVariableScope(clashCheck);
        if (clashCheck.clashes.isEmpty()) return false;
        bytecodeMeta.informLivenessClashes(clashCheck.clashes);
        return true;
    }

    private static LValue removeSyntheticConstructorParams(Method method, Op04StructuredStatement root, boolean isInstance) {
        List<ConstructorInvokationAnoynmousInner> usages;
        MethodPrototype prototype = method.getMethodPrototype();
        List<LocalVariable> vars = prototype.getComputedParameters();
        if (vars.isEmpty()) {
            return null;
        }
        FieldVariable matchedLValue = null;
        Map replacements = MapFactory.newMap();
        ConstructorInvokationAnoynmousInner usage = (usages = method.getClassFile().getAnonymousUsages()).size() == 1 ? usages.get(0) : null;
        if (isInstance) {
            LocalVariable outerThis = vars.get(0);
            InnerClassConstructorRewriter innerClassConstructorRewriter = new InnerClassConstructorRewriter(method.getClassFile(), outerThis);
            innerClassConstructorRewriter.rewrite(root);
            matchedLValue = innerClassConstructorRewriter.getMatchedField();
            if (matchedLValue != null) {
                ClassFileField classFileField = matchedLValue.getClassFileField();
                classFileField.markHidden();
                classFileField.markSyntheticOuterRef();
                replacements.put((LocalVariable)outerThis, (FieldVariable)matchedLValue);
                innerClassConstructorRewriter.getAssignmentStatement().getContainer().nopOut();
                prototype.hide(0);
            }
        }
        if (usage != null) {
            List actualArgs;
            if ((actualArgs = usage.getArgs()).size() != vars.size()) {
                throw new IllegalStateException();
            }
            int n = isInstance ? 1 : 0;
            int len = vars.size();
            for (int x = start = (v20315); x < len; ++x) {
                LocalVariable protoVar = vars.get(x);
                Expression arg = (Expression)actualArgs.get(x);
                if (!(arg = CastExpression.removeImplicit(arg) instanceof LValueExpression)) continue;
                LValue lValueArg = ((LValueExpression)arg).getLValue();
                Object name = null;
                if (!(lValueArg instanceof LocalVariable)) continue;
                LocalVariable localVariable = (LocalVariable)lValueArg;
                InnerClassConstructorRewriter innerClassConstructorRewriter = new InnerClassConstructorRewriter(method.getClassFile(), protoVar);
                innerClassConstructorRewriter.rewrite(root);
                FieldVariable matchedField = innerClassConstructorRewriter.getMatchedField();
                if (matchedField == null) continue;
                innerClassConstructorRewriter.getAssignmentStatement().getContainer().nopOut();
                ClassFileField classFileField = matchedField.getClassFileField();
                classFileField.overrideName(localVariable.getName().getStringName());
                classFileField.markSyntheticOuterRef();
                classFileField.markHidden();
                prototype.hide(x);
            }
        }
        if (replacements.isEmpty()) return matchedLValue;
        LValueReplacingRewriter lValueReplacingRewriter = new LValueReplacingRewriter(replacements);
        MiscStatementTools.applyExpressionRewriter(root, lValueReplacingRewriter);
        return matchedLValue;
    }

    public static LValue fixInnerClassConstruction(Method method, Op04StructuredStatement root) {
        ClassFile classFile;
        LValue res = null;
        if (!method.isConstructor() || !(classFile = method.getClassFile()).isInnerClass()) return res;
        res = Op04StructuredStatement.removeSyntheticConstructorParams(method, root, !classFile.testAccessFlag(AccessFlag.ACC_STATIC));
        return res;
    }

    public static void inlineSyntheticAccessors(DCCommonState state, Method method, Op04StructuredStatement root) {
        JavaTypeInstance classType = method.getClassFile().getClassType();
        new SyntheticAccessorRewriter(state, classType).rewrite(root);
    }

    public static void removeConstructorBoilerplate(Op04StructuredStatement root) {
        new RedundantSuperRewriter().rewrite(root);
    }

    public static void rewriteLambdas(DCCommonState state, Method method, Op04StructuredStatement root) {
        Options options = state.getOptions();
        if (!options.getOption(OptionsImpl.REWRITE_LAMBDAS, method.getClassFile().getClassFileVersion()).booleanValue()) {
            return;
        }
        new LambdaRewriter(state, method.getClassFile()).rewrite(root);
    }

    public static void removeUnnecessaryVarargArrays(Options options, Method method, Op04StructuredStatement root) {
        new VarArgsRewriter().rewrite(root);
    }

    public static void removePrimitiveDeconversion(Options options, Method method, Op04StructuredStatement root) {
        if (!((Boolean)options.getOption(OptionsImpl.SUGAR_BOXING)).booleanValue()) {
            return;
        }
        root.transform(new PrimitiveBoxingRewriter(), new StructuredScope());
    }

    public static void replaceNestedSyntheticOuterRefs(Op04StructuredStatement root) {
        List<StructuredStatement> statements = MiscStatementTools.linearise(root);
        if (statements == null) {
            return;
        }
        SyntheticOuterRefRewriter syntheticOuterRefRewriter = new SyntheticOuterRefRewriter();
        for (StructuredStatement statement : statements) {
            statement.rewriteExpressions(syntheticOuterRefRewriter);
        }
    }

    public static void applyChecker(Op04Checker checker, Op04StructuredStatement root, DecompilerComments comments) {
        StructuredScope structuredScope = new StructuredScope();
        root.transform(checker, structuredScope);
        checker.commentInto(comments);
    }

    class 1 {
    }

    public static class LValueTypeClashCheck
    implements LValueScopeDiscoverer,
    StructuredStatementTransformer {
        Set<Integer> clashes = SetFactory.newSet();

        @Override
        public void enterBlock(StructuredStatement structuredStatement) {
        }

        @Override
        public void leaveBlock(StructuredStatement structuredStatement) {
        }

        @Override
        public void collect(StackSSALabel lValue, StatementContainer<StructuredStatement> statementContainer, Expression value) {
            this.collect(lValue);
        }

        @Override
        public void collectMultiUse(StackSSALabel lValue, StatementContainer<StructuredStatement> statementContainer, Expression value) {
            this.collect(lValue);
        }

        @Override
        public void collectMutatedLValue(LValue lValue, StatementContainer<StructuredStatement> statementContainer, Expression value) {
            this.collect(lValue);
        }

        @Override
        public void collectLocalVariableAssignment(LocalVariable localVariable, StatementContainer<StructuredStatement> statementContainer, Expression value) {
            this.collect(localVariable);
        }

        @Override
        public void collect(LValue lValue) {
            int idx;
            lValue.collectLValueUsage(this);
            InferredJavaType inferredJavaType = lValue.getInferredJavaType();
            if (inferredJavaType == null || !inferredJavaType.isClash() || !(lValue instanceof LocalVariable) || (idx = ((LocalVariable)lValue).getIdx()) < 0) return;
            this.clashes.add(idx);
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            in.traceLocalVariableScope(this);
            in.transformStructuredChildren(this, scope);
            return in;
        }
    }

    static class PointlessBlockRemover
    implements StructuredStatementTransformer {
        private PointlessBlockRemover() {
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            in.transformStructuredChildren(this, scope);
            if (!(in instanceof CanRemovePointlessBlock)) return in;
            ((CanRemovePointlessBlock)in).removePointlessBlocks(scope);
            return in;
        }

        /* synthetic */ PointlessBlockRemover(1 x0) {
            this();
        }
    }

    static class StructuredGotoRemover
    implements StructuredStatementTransformer {
        private StructuredGotoRemover() {
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            in.transformStructuredChildren(this, scope);
            if (!(in instanceof UnstructuredGoto) && !(in instanceof UnstructuredAnonymousBreak)) return in;
            in = Op04StructuredStatement.transformStructuredGotoWithScope(scope, in);
            return in;
        }

        /* synthetic */ StructuredGotoRemover(1 x0) {
            this();
        }
    }

    static class Inliner
    implements StructuredStatementTransformer {
        private Inliner() {
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            in.transformStructuredChildren(this, scope);
            if (!(in instanceof Block)) return in;
            Block block = (Block)in;
            block.combineInlineable();
            return in;
        }

        /* synthetic */ Inliner(1 x0) {
            this();
        }
    }

    static class TryCatchTidier
    implements StructuredStatementTransformer {
        private TryCatchTidier() {
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            if (in instanceof Block) {
                Block block = (Block)in;
                block.combineTryCatch();
            }
            in.transformStructuredChildren(this, scope);
            return in;
        }

        /* synthetic */ TryCatchTidier(1 x0) {
            this();
        }
    }

    static class EmptyCatchTidier
    implements StructuredStatementTransformer {
        private EmptyCatchTidier() {
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            if (in instanceof UnstructuredCatch) {
                return ((UnstructuredCatch)in).getCatchForEmpty();
            }
            in.transformStructuredChildren(this, scope);
            return in;
        }

        /* synthetic */ EmptyCatchTidier(1 x0) {
            this();
        }
    }

    static class AnonymousBlockExtractor
    implements StructuredStatementTransformer {
        private AnonymousBlockExtractor() {
        }

        @Override
        public StructuredStatement transform(StructuredStatement in, StructuredScope scope) {
            if (in instanceof Block) {
                Block block = (Block)in;
                block.extractAnonymousBlocks();
            }
            in.transformStructuredChildren(this, scope);
            return in;
        }

        /* synthetic */ AnonymousBlockExtractor(1 x0) {
            this();
        }
    }

    static class MutableProcessingBlockState {
        BlockIdentifier currentBlockIdentifier = null;
        LinkedList<Op04StructuredStatement> currentBlock = ListFactory.newLinkedList();

        private MutableProcessingBlockState() {
        }

        /* synthetic */ MutableProcessingBlockState(1 x0) {
            this();
        }
    }

    static class StackedBlock {
        BlockIdentifier blockIdentifier;
        LinkedList<Op04StructuredStatement> statements;
        Op04StructuredStatement outerStart;

        private StackedBlock(BlockIdentifier blockIdentifier, LinkedList<Op04StructuredStatement> statements, Op04StructuredStatement outerStart) {
            this.blockIdentifier = blockIdentifier;
            this.statements = statements;
            this.outerStart = outerStart;
        }

        /* synthetic */ StackedBlock(BlockIdentifier x0, LinkedList x1, Op04StructuredStatement x2, 1 x3) {
            this(x0, x1, x2);
        }

        static class BlockIdentifierGetter
        implements UnaryFunction<StackedBlock, BlockIdentifier> {
            private BlockIdentifierGetter() {
            }

            @Override
            public BlockIdentifier invoke(StackedBlock arg) {
                return arg.blockIdentifier;
            }
        }

    }

}

