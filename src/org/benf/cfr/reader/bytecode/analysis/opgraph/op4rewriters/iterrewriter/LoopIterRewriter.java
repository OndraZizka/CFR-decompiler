/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.iterrewriter;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.Op04Rewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.AbstractMatchResultIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.CollectMatch;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.CollectMatchRange;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleenePlus;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchOneOf;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Negated;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NotOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredDo;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIter;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredWhile;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class LoopIterRewriter
implements Op04Rewriter {
    private final Options options;
    private final ClassFileVersion classFileVersion;

    public LoopIterRewriter(Options options, ClassFileVersion classFileVersion) {
        this.options = options;
        this.classFileVersion = classFileVersion;
    }

    @Override
    public void rewrite(Op04StructuredStatement root) {
        List<StructuredStatement> structuredStatements;
        if (!this.options.getOption(OptionsImpl.ARRAY_ITERATOR, this.classFileVersion).booleanValue()) {
            return;
        }
        if ((structuredStatements = MiscStatementTools.linearise(root)) == null) {
            return;
        }
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(structuredStatements);
        WildcardMatch wcm = new WildcardMatch();
        WildcardMatch.LValueWildcard array$_lv = wcm.getLValueWildCard("arr$", new Predicate<LValue>(){

            @Override
            public boolean test(LValue in) {
                JavaTypeInstance type = in.getInferredJavaType().getJavaTypeInstance();
                if (!(type instanceof JavaArrayTypeInstance)) return false;
                return true;
            }
        });
        WildcardMatch.ExpressionWildcard collection = wcm.getExpressionWildCard("collection");
        WildcardMatch.LValueWildcard i$_lv = wcm.getLValueWildCard("i$");
        LValueExpression i$ = new LValueExpression(i$_lv);
        WildcardMatch.LValueWildcard iter_lv = wcm.getLValueWildCard("iter");
        LValueExpression iter = new LValueExpression(i$_lv);
        ResetAfterTest m = new ResetAfterTest(wcm, new MatchSequence(new CollectMatch("i$", new StructuredAssignment(i$_lv, wcm.getMemberFunction("iterfn", "iterator", (Expression)collection))), (Matcher<StructuredStatement>)new MatchOneOf(new MatchSequence(new CollectMatch("do", new StructuredDo(null, null, wcm.getBlockIdentifier("doblockident"))), (Matcher<StructuredStatement>)new BeginBlock(wcm.getBlockWildcard("doblock")), (Matcher<StructuredStatement>)new CollectMatch("exit", new StructuredIf(new NotOperation(new BooleanExpression(wcm.getMemberFunction("hasnext", "hasNext", (Expression)i$))), null)), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new CollectMatch("exitinner", new MatchOneOf(new StructuredBreak(wcm.getBlockIdentifier("doblockident"), false), new StructuredReturn(wcm.getExpressionWildCard("returnvalue"), null), new StructuredReturn(null, null))), (Matcher<StructuredStatement>)new EndBlock(null), (Matcher<StructuredStatement>)new CollectMatch("incr", new StructuredAssignment(iter_lv, wcm.getMemberFunction("getnext", "next", (Expression)i$))), (Matcher<StructuredStatement>)new CollectMatchRange("body", new KleenePlus((Matcher<StructuredStatement>)new Negated(new EndBlock(wcm.getBlockWildcard("doblock"))))), (Matcher<StructuredStatement>)new EndBlock(wcm.getBlockWildcard("doblock"))), new MatchSequence(new CollectMatch("while", new StructuredWhile(new BooleanExpression(wcm.getMemberFunction("hasnext", "hasNext", (Expression)i$)), null, wcm.getBlockIdentifier("whileblockident"))), (Matcher<StructuredStatement>)new BeginBlock(wcm.getBlockWildcard("whileblockident")), (Matcher<StructuredStatement>)new CollectMatch("incr", new StructuredAssignment(iter_lv, wcm.getMemberFunction("getnext", "next", (Expression)i$))), (Matcher<StructuredStatement>)new CollectMatchRange("body", new KleenePlus((Matcher<StructuredStatement>)new Negated(new EndBlock(wcm.getBlockWildcard("whileblockident"))))), (Matcher<StructuredStatement>)new EndBlock(wcm.getBlockWildcard("whileblockident"))))));
        IterMatchResultCollector matchResultCollector = new IterMatchResultCollector(null);
        while (mi.hasNext()) {
            mi.advance();
            matchResultCollector.clear();
            if (!m.match(mi, matchResultCollector)) continue;
            switch (matchResultCollector.getMatchType()) {
                case WHILE_LOOP: {
                    this.validateAndRewriteWhile(mi, matchResultCollector);
                    break;
                }
                case DO_LOOP: {
                    this.validateAndRewriteDo(mi, matchResultCollector);
                }
            }
            mi.rewind1();
        }
    }

    private boolean validateAndRewriteDo(MatchIterator<StructuredStatement> mi, IterMatchResultCollector matchResultCollector) {
        StructuredDo doStatement = matchResultCollector.doStatement;
        Op04StructuredStatement doContainer = doStatement.getContainer();
        Op04StructuredStatement doBody = doStatement.getBody();
        if (!(doBody.getStatement() instanceof Block)) {
            return false;
        }
        StructuredStatement exitInner = matchResultCollector.exitinner;
        boolean copyexit = false;
        if (exitInner instanceof StructuredReturn) {
            boolean legitReturn = false;
            if (mi.isFinished()) {
                legitReturn = true;
            } else {
                int remaining;
                StructuredStatement afterLoop = mi.getCurrent();
                if ((remaining = mi.getRemaining()) == 1 && afterLoop instanceof EndBlock) {
                    copyexit = true;
                    legitReturn = true;
                } else if (afterLoop.equals(exitInner)) {
                    legitReturn = true;
                }
            }
            if (!legitReturn) {
                return false;
            }
        }
        matchResultCollector.incrStatement.getContainer().nopOut();
        LValue iter_lv = matchResultCollector.iter_lv;
        Expression collection = matchResultCollector.collection;
        matchResultCollector.iSetup.getContainer().nopOut();
        matchResultCollector.exit.getContainer().nopOut();
        BlockIdentifier blockidentifier = doStatement.getBlock();
        AbstractStructuredStatement replacement = new StructuredIter(blockidentifier, iter_lv, collection, doBody);
        if (copyexit) {
            replacement = Block.getBlockFor(false, replacement, exitInner);
        }
        doContainer.replaceContainedStatement(replacement);
        return true;
    }

    private boolean validateAndRewriteWhile(MatchIterator<StructuredStatement> mi, IterMatchResultCollector matchResultCollector) {
        StructuredWhile whileStatement = matchResultCollector.whileStatement;
        Op04StructuredStatement whileContainer = whileStatement.getContainer();
        Op04StructuredStatement whileBody = whileStatement.getBody();
        matchResultCollector.incrStatement.getContainer().nopOut();
        LValue iter_lv = matchResultCollector.iter_lv;
        Expression collection = matchResultCollector.collection;
        matchResultCollector.iSetup.getContainer().nopOut();
        BlockIdentifier blockidentifier = whileStatement.getBlock();
        StructuredIter replacement = new StructuredIter(blockidentifier, iter_lv, collection, whileBody);
        whileContainer.replaceContainedStatement(replacement);
        return true;
    }

    static class IterMatchResultCollector
    extends AbstractMatchResultIterator {
        MatchType matchType;
        StructuredAssignment iSetup;
        StructuredStatement exit;
        StructuredStatement exitinner;
        StructuredStatement incrStatement;
        StructuredDo doStatement;
        StructuredWhile whileStatement;
        LValue iter_lv;
        Expression collection;

        private IterMatchResultCollector() {
        }

        @Override
        public void clear() {
            this.matchType = MatchType.NONE;
            this.iSetup = null;
            this.exit = null;
            this.doStatement = null;
            this.whileStatement = null;
            this.incrStatement = null;
        }

        /*
         * Enabled aggressive block sorting
         */
        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            if (name.equals("i$")) {
                this.iSetup = (StructuredAssignment)statement;
                return;
            } else if (name.equals("do")) {
                this.matchType = MatchType.DO_LOOP;
                this.doStatement = (StructuredDo)statement;
                return;
            } else if (name.equals("while")) {
                this.matchType = MatchType.WHILE_LOOP;
                this.whileStatement = (StructuredWhile)statement;
                return;
            } else if (name.equals("exit")) {
                this.exit = statement;
                return;
            } else if (name.equals("exitinner")) {
                this.exitinner = statement;
                return;
            } else {
                if (!name.equals("incr")) throw new UnsupportedOperationException("Unexpected match " + name);
                this.incrStatement = statement;
            }
        }

        @Override
        public void collectStatementRange(String name, MatchIterator<StructuredStatement> start, MatchIterator<StructuredStatement> end) {
            if (name.equals("body")) return;
            throw new UnsupportedOperationException("Unexpected match " + name);
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
            this.iter_lv = wcm.getLValueWildCard("iter").getMatch();
            this.collection = wcm.getExpressionWildCard("collection").getMatch();
        }

        private MatchType getMatchType() {
            return this.matchType;
        }

        /* synthetic */ IterMatchResultCollector( x0) {
            this();
        }
    }

    static enum MatchType {
        NONE,
        WHILE_LOOP,
        DO_LOOP;
        

        private MatchType() {
        }
    }

}

