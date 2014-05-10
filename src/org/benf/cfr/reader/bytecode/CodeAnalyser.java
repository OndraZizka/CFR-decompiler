/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.benf.cfr.reader.bytecode.BytecodeMeta;
import org.benf.cfr.reader.bytecode.RecoveryOption;
import org.benf.cfr.reader.bytecode.RecoveryOptions;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op02WithProcessedDataAndRefs;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03Blocks;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op2rewriters.Op02LambdaRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchEnumRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchStringRewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.checker.LooseCatchChecker;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.StringBuilderRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.XorRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFakeDecompFailure;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableFactory;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.attributes.AttributeCode;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.exceptions.ExceptionAggregator;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.state.TypeUsageInformationEmpty;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.DecompilerComments;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.Troolean;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.bytestream.OffsettingByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.LoggerFactory;
import org.benf.cfr.reader.util.output.StdIODumper;

public class CodeAnalyser {
    private static final int SHOW_L2_RAW = 1;
    private static final int SHOW_L2_OPS = 2;
    private static final int SHOW_L3_RAW = 3;
    private static final int SHOW_L3_ORDERED = 4;
    private static final int SHOW_L3_CAUGHT = 5;
    private static final int SHOW_L3_JUMPS = 6;
    private static final int SHOW_L3_LOOPS1 = 7;
    private static final int SHOW_L3_EXCEPTION_BLOCKS = 8;
    private static final int SHOW_L4_FINAL_OP3 = 9;
    private static final Logger logger = LoggerFactory.create(CodeAnalyser.class);
    private final AttributeCode originalCodeAttribute;
    private final ConstantPool cp;
    private Method method;
    private Op04StructuredStatement analysed;
    private static final RecoveryOptions recover0 = new RecoveryOptions(new RecoveryOption.TrooleanRO(OptionsImpl.RECOVER_TYPECLASHES, Troolean.TRUE, BytecodeMeta.testFlag(BytecodeMeta.CodeInfoFlag.LIVENESS_CLASH)));
    private static final RecoveryOptions recover1 = new RecoveryOptions(CodeAnalyser.recover0, (RecoveryOption<?>[])new RecoveryOption[]{new RecoveryOption.TrooleanRO(OptionsImpl.FORCE_TOPSORT, Troolean.TRUE, DecompilerComment.AGGRESSIVE_TOPOLOGICAL_SORT), new RecoveryOption.BooleanRO(OptionsImpl.LENIENT, Boolean.TRUE), new RecoveryOption.TrooleanRO(OptionsImpl.FORCE_RET_PROPAGATE, Troolean.TRUE), new RecoveryOption.TrooleanRO(OptionsImpl.FORCE_PRUNE_EXCEPTIONS, Troolean.TRUE, BytecodeMeta.testFlag(BytecodeMeta.CodeInfoFlag.USES_EXCEPTIONS), DecompilerComment.PRUNE_EXCEPTIONS), new RecoveryOption.TrooleanRO(OptionsImpl.FORCE_AGGRESSIVE_EXCEPTION_AGG, Troolean.TRUE, BytecodeMeta.testFlag(BytecodeMeta.CodeInfoFlag.USES_EXCEPTIONS))});
    private static final RecoveryOptions recover2 = new RecoveryOptions(CodeAnalyser.recover1, (RecoveryOption<?>[])new RecoveryOption[]{new RecoveryOption.BooleanRO(OptionsImpl.COMMENT_MONITORS, Boolean.TRUE, BytecodeMeta.testFlag(BytecodeMeta.CodeInfoFlag.USES_MONITORS), DecompilerComment.COMMENT_MONITORS)});
    private static final RecoveryOptions[] recoveryOptionsArr = new RecoveryOptions[]{CodeAnalyser.recover0, CodeAnalyser.recover1, CodeAnalyser.recover2};

    public CodeAnalyser(AttributeCode attributeCode) {
        this.originalCodeAttribute = attributeCode;
        this.cp = attributeCode.getConstantPool();
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Op04StructuredStatement getAnalysis(DCCommonState dcCommonState) {
        if (this.analysed != null) {
            return this.analysed;
        }
        Options options = dcCommonState.getOptions();
        List<Op01WithProcessedDataAndByteJumps> instrs = this.getInstrs();
        AnalysisResult res = null;
        BytecodeMeta bytecodeMeta = new BytecodeMeta(instrs, this.originalCodeAttribute);
        if (options.optionIsSet(OptionsImpl.FORCE_PASS)) {
            int pass;
            if ((pass = ((Integer)options.getOption(OptionsImpl.FORCE_PASS)).intValue()) < 0 || pass >= CodeAnalyser.recoveryOptionsArr.length) {
                throw new IllegalArgumentException("Illegal recovery pass idx");
            }
            RecoveryOptions.Applied applied = CodeAnalyser.recoveryOptionsArr[pass].apply(dcCommonState, options, bytecodeMeta);
            res = this.getAnalysisOrWrapFail(pass, instrs, dcCommonState, applied.options, applied.comments, bytecodeMeta);
        } else {
            res = this.getAnalysisOrWrapFail(0, instrs, dcCommonState, options, null, bytecodeMeta);
            if (res.failed && ((Boolean)options.getOption(OptionsImpl.RECOVER)).booleanValue()) {
                int passIdx = 1;
                for (RecoveryOptions recoveryOptions : CodeAnalyser.recoveryOptionsArr) {
                    AnalysisResult nextRes;
                    RecoveryOptions.Applied applied = recoveryOptions.apply(dcCommonState, options, bytecodeMeta);
                    if (!applied.valid) continue;
                    if ((nextRes = this.getAnalysisOrWrapFail(passIdx++, instrs, dcCommonState, applied.options, applied.comments, bytecodeMeta)) != null) {
                        res = nextRes;
                    }
                    if (!res.failed) break;
                }
            }
        }
        if (res.comments != null) {
            this.method.setComments(res.comments);
        }
        this.analysed = res.code;
        return this.analysed;
    }

    private List<Op01WithProcessedDataAndByteJumps> getInstrs() {
        int length;
        ByteData rawCode = this.originalCodeAttribute.getRawData();
        long codeLength = this.originalCodeAttribute.getCodeLength();
        ArrayList<Op01WithProcessedDataAndByteJumps> instrs = new ArrayList<Op01WithProcessedDataAndByteJumps>();
        OffsettingByteData bdCode = rawCode.getOffsettingOffsetData(0);
        int offset = 0;
        instrs.add(JVMInstr.NOP.createOperation(null, this.cp, -1));
        do {
            JVMInstr instr = JVMInstr.find(bdCode.getS1At(0));
            Op01WithProcessedDataAndByteJumps oc = instr.createOperation(bdCode, this.cp, offset);
            length = oc.getInstructionLength();
            instrs.add(oc);
            bdCode.advance(length);
        } while ((long)(offset+=length) < codeLength);
        return instrs;
    }

    private AnalysisResult getAnalysisOrWrapFail(int passIdx, List<Op01WithProcessedDataAndByteJumps> instrs, DCCommonState commonState, Options options, List<DecompilerComment> extraComments, BytecodeMeta bytecodeMeta) {
        try {
            AnalysisResult res = this.getAnalysisInner(instrs, commonState, options, bytecodeMeta, passIdx);
            if (extraComments == null) return res;
            res.comments.addComments((Collection<DecompilerComment>)extraComments);
            return res;
        }
        catch (RuntimeException e) {
            Op04StructuredStatement coderes = new Op04StructuredStatement(new StructuredFakeDecompFailure(e));
            DecompilerComments comments = new DecompilerComments();
            comments.addComment(new DecompilerComment("Exception decompiling", e));
            return new AnalysisResult(comments, coderes, null);
        }
    }

    private AnalysisResult getAnalysisInner(List<Op01WithProcessedDataAndByteJumps> instrs, DCCommonState dcCommonState, Options options, BytecodeMeta bytecodeMeta, int passIdx) {
        int x;
        boolean willSort = options.getOption(OptionsImpl.FORCE_TOPSORT) == Troolean.TRUE;
        int showOpsLevel = (Integer)options.getOption(OptionsImpl.SHOWOPS);
        ClassFile classFile = this.method.getClassFile();
        ClassFileVersion classFileVersion = classFile.getClassFileVersion();
        DecompilerComments comments = new DecompilerComments();
        StdIODumper debugDumper = new StdIODumper(new TypeUsageInformationEmpty());
        HashMap<Integer, Integer> lutByOffset = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> lutByIdx = new HashMap<Integer, Integer>();
        int idx2 = 0;
        int offset2 = -1;
        for (Op01WithProcessedDataAndByteJumps op : instrs) {
            lutByOffset.put(offset2, idx2);
            lutByIdx.put(idx2, offset2);
            offset2+=op.getInstructionLength();
            ++idx2;
        }
        lutByIdx.put(0, -1);
        lutByOffset.put(-1, 0);
        List op1list = ListFactory.newList();
        List op2list = ListFactory.newList();
        for (x = 0; x < instrs.size(); ++x) {
            Op01WithProcessedDataAndByteJumps op1 = instrs.get(x);
            op1list.add((Op01WithProcessedDataAndByteJumps)op1);
            Op02WithProcessedDataAndRefs op2 = op1.createOp2(this.cp, x);
            op2list.add((Op02WithProcessedDataAndRefs)op2);
        }
        int len = instrs.size();
        for (x = 0; x < len; ++x) {
            int offsetOfThisInstruction = (Integer)lutByIdx.get(x);
            int[] targetIdxs = ((Op01WithProcessedDataAndByteJumps)op1list.get(x)).getAbsoluteIndexJumps(offsetOfThisInstruction, (Map<Integer, Integer>)lutByOffset);
            Op02WithProcessedDataAndRefs source = (Op02WithProcessedDataAndRefs)op2list.get(x);
            for (int targetIdx : targetIdxs) {
                if (targetIdx >= len) continue;
                Op02WithProcessedDataAndRefs target = (Op02WithProcessedDataAndRefs)op2list.get(targetIdx);
                source.addTarget(target);
                target.addSource(source);
            }
        }
        BlockIdentifierFactory blockIdentifierFactory = new BlockIdentifierFactory();
        ExceptionAggregator exceptions = new ExceptionAggregator(this.originalCodeAttribute.getExceptionTableEntries(), blockIdentifierFactory, (Map<Integer, Integer>)lutByOffset, (Map<Integer, Integer>)lutByIdx, instrs, options, this.cp, this.method);
        if (showOpsLevel == 1) {
            debugDumper.print("Op2 statements:\n");
            debugDumper.dump(op2list);
            debugDumper.newln().newln();
        }
        if (options.getOption(OptionsImpl.FORCE_PRUNE_EXCEPTIONS) == Troolean.TRUE) {
            exceptions.aggressivePruning((Map<Integer, Integer>)lutByOffset, (Map<Integer, Integer>)lutByIdx, instrs);
            exceptions.removeSynchronisedHandlers((Map<Integer, Integer>)lutByOffset, (Map<Integer, Integer>)lutByIdx, instrs);
        }
        if (options.getOption(OptionsImpl.REWRITE_LAMBDAS, classFileVersion).booleanValue() && bytecodeMeta.has(BytecodeMeta.CodeInfoFlag.USES_INVOKEDYNAMIC)) {
            Op02LambdaRewriter.removeInvokeGetClass(classFile, op2list);
        }
        long codeLength = this.originalCodeAttribute.getCodeLength();
        op2list = Op02WithProcessedDataAndRefs.insertExceptionBlocks(op2list, exceptions, lutByOffset, this.cp, codeLength, dcCommonState, options);
        lutByOffset = null;
        Op02WithProcessedDataAndRefs.populateStackInfo(op2list, this.method);
        if (showOpsLevel == 2) {
            debugDumper.print("Op2 statements:\n");
            debugDumper.dump(op2list);
            debugDumper.newln().newln();
        }
        if (Op02WithProcessedDataAndRefs.processJSR(op2list)) {
            Op02WithProcessedDataAndRefs.populateStackInfo(op2list, this.method);
        }
        Op02WithProcessedDataAndRefs.unlinkUnreachable(op2list);
        Op02WithProcessedDataAndRefs.discoverStorageLiveness(this.method, comments, op2list, bytecodeMeta, options);
        VariableFactory variableFactory = new VariableFactory(this.method);
        List<Op03SimpleStatement> op03SimpleParseNodes = Op02WithProcessedDataAndRefs.convertToOp03List(op2list, this.method, variableFactory, blockIdentifierFactory, dcCommonState);
        if (showOpsLevel == 3) {
            debugDumper.print("Raw Op3 statements:\n");
            for (Op03SimpleStatement node : op03SimpleParseNodes) {
                node.dumpInner(debugDumper);
            }
            debugDumper.print("\n\n");
        }
        if (showOpsLevel == 4) {
            debugDumper.newln().newln();
            debugDumper.print("Linked Op3 statements:\n");
            op03SimpleParseNodes.get(0).dump(debugDumper);
            debugDumper.print("\n\n");
        }
        Op03SimpleStatement.flattenCompoundStatements(op03SimpleParseNodes);
        Op03SimpleStatement.inferGenericObjectInfoFromCalls(op03SimpleParseNodes);
        Op03SimpleStatement.replaceRawSwitches(op03SimpleParseNodes, blockIdentifierFactory);
        op03SimpleParseNodes = Op03SimpleStatement.renumber(op03SimpleParseNodes);
        Op03SimpleStatement.removePointlessJumps(op03SimpleParseNodes);
        op03SimpleParseNodes = Op03SimpleStatement.renumber(op03SimpleParseNodes);
        Op03SimpleStatement.assignSSAIdentifiers(this.method, op03SimpleParseNodes);
        Op03SimpleStatement.condenseLValues(op03SimpleParseNodes);
        op03SimpleParseNodes = Op03SimpleStatement.renumber(op03SimpleParseNodes);
        Op03SimpleStatement.eliminateCatchTemporaries(op03SimpleParseNodes);
        CodeAnalyser.logger.info("identifyCatchBlocks");
        Op03SimpleStatement.identifyCatchBlocks(op03SimpleParseNodes, blockIdentifierFactory);
        Op03SimpleStatement.combineTryCatchBlocks(op03SimpleParseNodes, blockIdentifierFactory);
        if (((Boolean)options.getOption(OptionsImpl.COMMENT_MONITORS)).booleanValue()) {
            Op03SimpleStatement.commentMonitors(op03SimpleParseNodes);
        }
        if (showOpsLevel == 5) {
            debugDumper.newln().newln();
            debugDumper.print("After catchblocks.:\n");
            op03SimpleParseNodes.get(0).dump(debugDumper);
        }
        Op03SimpleStatement.condenseConstruction(dcCommonState, this.method, op03SimpleParseNodes);
        Op03SimpleStatement.condenseLValues(op03SimpleParseNodes);
        Op03SimpleStatement.condenseLValueChain1(op03SimpleParseNodes);
        op03SimpleParseNodes = Op03SimpleStatement.removeRedundantTries(op03SimpleParseNodes);
        Op03SimpleStatement.identifyFinally(options, this.method, op03SimpleParseNodes, blockIdentifierFactory);
        op03SimpleParseNodes = Op03SimpleStatement.removeUnreachableCode(op03SimpleParseNodes, !willSort);
        op03SimpleParseNodes = Op03SimpleStatement.renumber(op03SimpleParseNodes);
        Op03SimpleStatement.extendTryBlocks(dcCommonState, op03SimpleParseNodes);
        Op03SimpleStatement.combineTryCatchEnds(op03SimpleParseNodes);
        Op03SimpleStatement.removePointlessExpressionStatements(op03SimpleParseNodes);
        op03SimpleParseNodes = Op03SimpleStatement.removeUnreachableCode(op03SimpleParseNodes, !willSort);
        Op03SimpleStatement.replacePrePostChangeAssignments(op03SimpleParseNodes);
        Op03SimpleStatement.pushPreChangeBack(op03SimpleParseNodes);
        Op03SimpleStatement.condenseLValueChain2(op03SimpleParseNodes);
        Op03SimpleStatement.condenseLValues(op03SimpleParseNodes);
        op03SimpleParseNodes = Op03SimpleStatement.renumber(op03SimpleParseNodes);
        if (options.getOption(OptionsImpl.FORCE_TOPSORT) == Troolean.TRUE) {
            Op03SimpleStatement.replaceReturningIfs(op03SimpleParseNodes, true);
            op03SimpleParseNodes = Op03SimpleStatement.removeUnreachableCode(op03SimpleParseNodes, false);
            op03SimpleParseNodes = Op03Blocks.topologicalSort(this.method, op03SimpleParseNodes, comments, options);
            Op03SimpleStatement.removePointlessJumps(op03SimpleParseNodes);
            Op03SimpleStatement.rebuildSwitches(op03SimpleParseNodes);
            Op03SimpleStatement.rejoinBlocks(op03SimpleParseNodes);
            Op03SimpleStatement.extendTryBlocks(dcCommonState, op03SimpleParseNodes);
            op03SimpleParseNodes = Op03Blocks.combineTryBlocks(this.method, op03SimpleParseNodes);
            Op03SimpleStatement.combineTryCatchEnds(op03SimpleParseNodes);
            Op03SimpleStatement.rewriteTryBackJumps(op03SimpleParseNodes);
            Op03SimpleStatement.identifyFinally(options, this.method, op03SimpleParseNodes, blockIdentifierFactory);
            Op03SimpleStatement.replaceReturningIfs(op03SimpleParseNodes, true);
        }
        if (options.getOption(OptionsImpl.FORCE_RET_PROPAGATE) == Troolean.TRUE) {
            Op03SimpleStatement.propagateToReturn(this.method, op03SimpleParseNodes);
        }
        Op03SimpleStatement.determineFinal(op03SimpleParseNodes, variableFactory);
        CodeAnalyser.logger.info("sugarAnyonymousArrays");
        Op03SimpleStatement.resugarAnonymousArrays(op03SimpleParseNodes);
        boolean reloop = false;
        do {
            CodeAnalyser.logger.info("collapseAssignmentsIntoConditionals");
            Op03SimpleStatement.collapseAssignmentsIntoConditionals(op03SimpleParseNodes, options);
            CodeAnalyser.logger.info("condenseConditionals");
            Op03SimpleStatement.condenseConditionals(op03SimpleParseNodes);
            reloop = Op03SimpleStatement.condenseConditionals2(op03SimpleParseNodes);
            op03SimpleParseNodes = Op03SimpleStatement.removeUnreachableCode(op03SimpleParseNodes, true);
        } while (reloop);
        CodeAnalyser.logger.info("simplifyConditionals");
        Op03SimpleStatement.simplifyConditionals(op03SimpleParseNodes);
        op03SimpleParseNodes = Op03SimpleStatement.renumber(op03SimpleParseNodes);
        CodeAnalyser.logger.info("rewriteNegativeJumps");
        Op03SimpleStatement.rewriteNegativeJumps(op03SimpleParseNodes);
        Op03SimpleStatement.optimiseForTypes(op03SimpleParseNodes);
        if (showOpsLevel == 6) {
            debugDumper.newln().newln();
            debugDumper.print("After jumps.:\n");
            op03SimpleParseNodes.get(0).dump(debugDumper);
        }
        if (((Boolean)options.getOption(OptionsImpl.ECLIPSE)).booleanValue()) {
            Op03SimpleStatement.eclipseLoopPass(op03SimpleParseNodes);
        }
        CodeAnalyser.logger.info("identifyLoops1");
        Op03SimpleStatement.identifyLoops1(this.method, op03SimpleParseNodes, blockIdentifierFactory);
        op03SimpleParseNodes = Op03SimpleStatement.pushThroughGoto(this.method, op03SimpleParseNodes);
        Op03SimpleStatement.replaceReturningIfs(op03SimpleParseNodes, false);
        if (showOpsLevel == 7) {
            debugDumper.newln().newln();
            debugDumper.print("After loops.:\n");
            op03SimpleParseNodes.get(0).dump(debugDumper);
        }
        op03SimpleParseNodes = Op03SimpleStatement.renumber(op03SimpleParseNodes);
        op03SimpleParseNodes = Op03SimpleStatement.removeUnreachableCode(op03SimpleParseNodes, true);
        if (showOpsLevel == 8) {
            debugDumper.newln().newln();
            debugDumper.print("After exception.:\n");
            op03SimpleParseNodes.get(0).dump(debugDumper);
        }
        CodeAnalyser.logger.info("rewriteBreakStatements");
        Op03SimpleStatement.rewriteBreakStatements(op03SimpleParseNodes);
        CodeAnalyser.logger.info("rewriteWhilesAsFors");
        Op03SimpleStatement.rewriteDoWhileTruePredAsWhile(op03SimpleParseNodes);
        Op03SimpleStatement.rewriteWhilesAsFors(op03SimpleParseNodes);
        CodeAnalyser.logger.info("removeSynchronizedCatchBlocks");
        Op03SimpleStatement.removeSynchronizedCatchBlocks(options, op03SimpleParseNodes);
        CodeAnalyser.logger.info("identifyNonjumpingConditionals");
        op03SimpleParseNodes = Op03SimpleStatement.removeUselessNops(op03SimpleParseNodes);
        Op03SimpleStatement.removePointlessJumps(op03SimpleParseNodes);
        Op03SimpleStatement.extractExceptionJumps(op03SimpleParseNodes);
        Op03SimpleStatement.identifyNonjumpingConditionals(op03SimpleParseNodes, blockIdentifierFactory);
        Op03SimpleStatement.condenseLValues(op03SimpleParseNodes);
        if (options.getOption(OptionsImpl.FORCE_RET_PROPAGATE) == Troolean.TRUE) {
            Op03SimpleStatement.propagateToReturn2(this.method, op03SimpleParseNodes);
        }
        CodeAnalyser.logger.info("removeUselessNops");
        op03SimpleParseNodes = Op03SimpleStatement.removeUselessNops(op03SimpleParseNodes);
        CodeAnalyser.logger.info("removePointlessJumps");
        Op03SimpleStatement.removePointlessJumps(op03SimpleParseNodes);
        CodeAnalyser.logger.info("rewriteBreakStatements");
        Op03SimpleStatement.rewriteBreakStatements(op03SimpleParseNodes);
        Op03SimpleStatement.classifyGotos(op03SimpleParseNodes);
        if (((Boolean)options.getOption(OptionsImpl.LABELLED_BLOCKS)).booleanValue()) {
            Op03SimpleStatement.classifyAnonymousBlockGotos(op03SimpleParseNodes);
        }
        Op03SimpleStatement.identifyNonjumpingConditionals(op03SimpleParseNodes, blockIdentifierFactory);
        CodeAnalyser.logger.info("rewriteArrayForLoops");
        if (options.getOption(OptionsImpl.ARRAY_ITERATOR, classFileVersion).booleanValue()) {
            Op03SimpleStatement.rewriteArrayForLoops(op03SimpleParseNodes);
        }
        CodeAnalyser.logger.info("rewriteIteratorWhileLoops");
        if (options.getOption(OptionsImpl.COLLECTION_ITERATOR, classFileVersion).booleanValue()) {
            Op03SimpleStatement.rewriteIteratorWhileLoops(op03SimpleParseNodes);
        }
        CodeAnalyser.logger.info("findSynchronizedBlocks");
        Op03SimpleStatement.findSynchronizedBlocks(op03SimpleParseNodes);
        CodeAnalyser.logger.info("removePointlessSwitchDefaults");
        Op03SimpleStatement.removePointlessSwitchDefaults(op03SimpleParseNodes);
        CodeAnalyser.logger.info("removeUselessNops");
        op03SimpleParseNodes = Op03SimpleStatement.removeUselessNops(op03SimpleParseNodes);
        Op03SimpleStatement.rewriteWith(op03SimpleParseNodes, new StringBuilderRewriter(options, classFileVersion));
        Op03SimpleStatement.rewriteWith(op03SimpleParseNodes, new XorRewriter());
        if (showOpsLevel == 9) {
            debugDumper.newln().newln();
            debugDumper.print("Final Op3 statements:\n");
            op03SimpleParseNodes.get(0).dump(debugDumper);
        }
        op03SimpleParseNodes = Op03SimpleStatement.removeUnreachableCode(op03SimpleParseNodes, true);
        if (((Boolean)options.getOption(OptionsImpl.LABELLED_BLOCKS)).booleanValue()) {
            Op03SimpleStatement.labelAnonymousBlocks(op03SimpleParseNodes, blockIdentifierFactory);
        }
        Op03SimpleStatement.replaceStackVarsWithLocals(op03SimpleParseNodes);
        Op03SimpleStatement.reindexInPlace(op03SimpleParseNodes);
        Op04StructuredStatement block = Op03SimpleStatement.createInitialStructuredBlock(op03SimpleParseNodes);
        Op04StructuredStatement.tidyEmptyCatch(block);
        Op04StructuredStatement.tidyTryCatch(block);
        Op04StructuredStatement.inlinePossibles(block);
        Op04StructuredStatement.removeStructuredGotos(block);
        Op04StructuredStatement.removePointlessBlocks(block);
        Op04StructuredStatement.removePointlessReturn(block);
        Op04StructuredStatement.removePrimitiveDeconversion(options, this.method, block);
        if (((Boolean)options.getOption(OptionsImpl.LABELLED_BLOCKS)).booleanValue()) {
            Op04StructuredStatement.insertAnonymousBlocks(block);
        }
        if (!block.isFullyStructured()) {
            comments.addComment(DecompilerComment.UNABLE_TO_STRUCTURE);
        } else {
            Op04StructuredStatement.tidyTypedBooleans(block);
            Op04StructuredStatement.prettifyBadLoops(block);
            new SwitchStringRewriter(options, classFileVersion).rewrite(block);
            new SwitchEnumRewriter(dcCommonState, classFileVersion).rewrite(block);
            Op04StructuredStatement.discoverVariableScopes(this.method, block, variableFactory);
            if (((Boolean)options.getOption(OptionsImpl.REMOVE_BOILERPLATE)).booleanValue() && this.method.isConstructor()) {
                Op04StructuredStatement.removeConstructorBoilerplate(block);
            }
            Op04StructuredStatement.rewriteLambdas(dcCommonState, this.method, block);
            Op04StructuredStatement.removeUnnecessaryVarargArrays(options, this.method, block);
            Op04StructuredStatement.removePrimitiveDeconversion(options, this.method, block);
            Op04StructuredStatement.tidyVariableNames(this.method, block);
            Op04StructuredStatement.applyChecker(new LooseCatchChecker(), block, comments);
        }
        if (passIdx != 0 || !Op04StructuredStatement.checkTypeClashes(block, bytecodeMeta)) return new AnalysisResult(comments, block, null);
        comments.addComment(DecompilerComment.TYPE_CLASHES);
        return new AnalysisResult(comments, block, null);
    }

    public void dump(Dumper d) {
        d.newln();
        this.analysed.dump(d);
    }

    class 1 {
    }

    static class AnalysisResult {
        public DecompilerComments comments;
        public Op04StructuredStatement code;
        public boolean failed;

        private AnalysisResult(DecompilerComments comments, Op04StructuredStatement code) {
            this.comments = comments;
            this.code = code;
            boolean failed = false;
            for (DecompilerComment comment : comments.getCommentList()) {
                if (!comment.isFailed()) continue;
                failed = true;
            }
            this.failed = failed;
        }

        /* synthetic */ AnalysisResult(DecompilerComments x0, Op04StructuredStatement x1, 1 x2) {
            this(x0, x1);
        }
    }

}

