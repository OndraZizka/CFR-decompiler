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
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractAssignmentExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArithmeticPreMutationOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayIndex;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ArrayLength;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CompOp;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ComparisonOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.statement.AssignmentSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredDo;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredFor;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIter;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredReturn;
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

public class ArrayIterRewriter
implements Op04Rewriter {
    private final Options options;
    private final ClassFileVersion classFileVersion;

    public ArrayIterRewriter(Options options, ClassFileVersion classFileVersion) {
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
        WildcardMatch.LValueWildcard iter_lv = wcm.getLValueWildCard("i");
        LValueExpression iter = new LValueExpression(iter_lv);
        WildcardMatch.ExpressionWildcard orig_array = wcm.getExpressionWildCard("array");
        WildcardMatch.LValueWildcard i$_lv = wcm.getLValueWildCard("i$");
        LValueExpression i$ = new LValueExpression(i$_lv);
        WildcardMatch.LValueWildcard len$_lv = wcm.getLValueWildCard("len$");
        LValueExpression len$ = new LValueExpression(len$_lv);
        LValueExpression array$ = new LValueExpression(array$_lv);
        ResetAfterTest m = new ResetAfterTest(wcm, new MatchSequence(new CollectMatch("array$", new StructuredAssignment(array$_lv, orig_array)), (Matcher<StructuredStatement>)new CollectMatch("len$", new StructuredAssignment(len$_lv, new ArrayLength(array$))), (Matcher<StructuredStatement>)new MatchOneOf(new MatchSequence(new CollectMatch("i$", new StructuredAssignment(i$_lv, new Literal(TypedLiteral.getInt(0)))), (Matcher<StructuredStatement>)new CollectMatch("do", new StructuredDo(null, null, wcm.getBlockIdentifier("doblockident"))), (Matcher<StructuredStatement>)new BeginBlock(wcm.getBlockWildcard("doblock")), (Matcher<StructuredStatement>)new CollectMatch("exit", new StructuredIf(new ComparisonOperation(i$, len$, CompOp.GTE), null)), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new MatchOneOf(new StructuredBreak(wcm.getBlockIdentifier("doblockident"), false), new StructuredReturn(wcm.getExpressionWildCard("returnvalue"), null), new StructuredReturn(null, null)), (Matcher<StructuredStatement>)new EndBlock(null), (Matcher<StructuredStatement>)new CollectMatch("assigniter", new StructuredAssignment(iter_lv, new ArrayIndex(array$, i$))), (Matcher<StructuredStatement>)new CollectMatchRange("body", new KleenePlus((Matcher<StructuredStatement>)new Negated(new MatchOneOf(new StructuredExpressionStatement(new ArithmeticPreMutationOperation(i$_lv, ArithOp.PLUS), true), new EndBlock(wcm.getBlockWildcard("doblock")))))), (Matcher<StructuredStatement>)new CollectMatch("incr", new StructuredExpressionStatement(new ArithmeticPreMutationOperation(i$_lv, ArithOp.PLUS), true)), (Matcher<StructuredStatement>)new EndBlock(wcm.getBlockWildcard("doblock"))), new MatchSequence(new CollectMatch("for", new StructuredFor(new ComparisonOperation(i$, len$, CompOp.LT), new AssignmentSimple(i$_lv, new Literal(TypedLiteral.getInt(0))), new ArithmeticPreMutationOperation(i$_lv, ArithOp.PLUS), null, wcm.getBlockIdentifier("doblockident"))), (Matcher<StructuredStatement>)new BeginBlock(wcm.getBlockWildcard("forblock")), (Matcher<StructuredStatement>)new CollectMatch("assigniter", new StructuredAssignment(iter_lv, new ArrayIndex(array$, i$))), (Matcher<StructuredStatement>)new CollectMatchRange("body", new KleenePlus((Matcher<StructuredStatement>)new Negated(new EndBlock(wcm.getBlockWildcard("doblock"))))), (Matcher<StructuredStatement>)new EndBlock(wcm.getBlockWildcard("forblock"))))));
        IterMatchResultCollector matchResultCollector = new IterMatchResultCollector(null);
        while (mi.hasNext()) {
            mi.advance();
            matchResultCollector.clear();
            if (!m.match(mi, matchResultCollector)) continue;
            switch (matchResultCollector.getMatchType()) {
                case FOR_LOOP: {
                    this.validateAndRewriteFor(matchResultCollector);
                    break;
                }
                case DO_LOOP: {
                    this.validateAndRewriteDo(matchResultCollector);
                }
            }
            mi.rewind1();
        }
    }

    private boolean validateAndRewriteFor(IterMatchResultCollector matchResultCollector) {
        StructuredFor structuredFor = matchResultCollector.forStatement;
        Op04StructuredStatement forContainer = structuredFor.getContainer();
        Op04StructuredStatement forBody = structuredFor.getBody();
        if (!(forBody.getStatement() instanceof Block)) {
            return false;
        }
        BlockIdentifier blockidentifier = structuredFor.getBlock();
        LValue iter_lv = matchResultCollector.iter_lv;
        Expression array = matchResultCollector.orig_array;
        matchResultCollector.arraySetup.getContainer().nopOut();
        matchResultCollector.lenSetup.getContainer().nopOut();
        matchResultCollector.assignIter.getContainer().nopOut();
        StructuredIter forIter = new StructuredIter(blockidentifier, iter_lv, array, forBody);
        forContainer.replaceContainedStatement(forIter);
        return true;
    }

    private boolean validateAndRewriteDo(IterMatchResultCollector matchResultCollector) {
        StructuredDo doStatement = matchResultCollector.doStatement;
        Op04StructuredStatement doContainer = doStatement.getContainer();
        Op04StructuredStatement doBody = doStatement.getBody();
        if (!(doBody.getStatement() instanceof Block)) {
            return false;
        }
        matchResultCollector.incrStatement.getContainer().nopOut();
        LValue iter_lv = matchResultCollector.iter_lv;
        Expression array = matchResultCollector.orig_array;
        matchResultCollector.iSetup.getContainer().nopOut();
        matchResultCollector.arraySetup.getContainer().nopOut();
        matchResultCollector.lenSetup.getContainer().nopOut();
        matchResultCollector.exit.getContainer().nopOut();
        matchResultCollector.assignIter.getContainer().nopOut();
        BlockIdentifier blockidentifier = doStatement.getBlock();
        StructuredIter forIter = new StructuredIter(blockidentifier, iter_lv, array, doBody);
        doContainer.replaceContainedStatement(forIter);
        return true;
    }

    static class IterMatchResultCollector
    extends AbstractMatchResultIterator {
        MatchType matchType;
        StructuredAssignment arraySetup;
        StructuredAssignment lenSetup;
        StructuredAssignment iSetup;
        StructuredStatement exit;
        StructuredStatement incrStatement;
        StructuredStatement assignIter;
        StructuredDo doStatement;
        StructuredFor forStatement;
        LValue iter_lv;
        Expression orig_array;

        private IterMatchResultCollector() {
        }

        @Override
        public void clear() {
            this.matchType = MatchType.NONE;
            this.iSetup = null;
            this.lenSetup = null;
            this.arraySetup = null;
            this.exit = null;
            this.doStatement = null;
            this.forStatement = null;
            this.incrStatement = null;
        }

        /*
         * Enabled aggressive block sorting
         */
        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            if (name.equals("array$")) {
                this.arraySetup = (StructuredAssignment)statement;
                return;
            } else if (name.equals("len$")) {
                this.lenSetup = (StructuredAssignment)statement;
                return;
            } else if (name.equals("i$")) {
                this.iSetup = (StructuredAssignment)statement;
                return;
            } else if (name.equals("do")) {
                this.matchType = MatchType.DO_LOOP;
                this.doStatement = (StructuredDo)statement;
                return;
            } else if (name.equals("for")) {
                this.matchType = MatchType.FOR_LOOP;
                this.forStatement = (StructuredFor)statement;
                return;
            } else if (name.equals("exit")) {
                this.exit = statement;
                return;
            } else if (name.equals("incr")) {
                this.incrStatement = statement;
                return;
            } else {
                if (!name.equals("assigniter")) throw new UnsupportedOperationException("Unexpected match " + name);
                this.assignIter = statement;
            }
        }

        @Override
        public void collectStatementRange(String name, MatchIterator<StructuredStatement> start, MatchIterator<StructuredStatement> end) {
            if (name.equals("body")) return;
            throw new UnsupportedOperationException("Unexpected match " + name);
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
            this.iter_lv = wcm.getLValueWildCard("i").getMatch();
            this.orig_array = wcm.getExpressionWildCard("array").getMatch();
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
        FOR_LOOP,
        DO_LOOP;
        

        private MatchType() {
        }
    }

}

