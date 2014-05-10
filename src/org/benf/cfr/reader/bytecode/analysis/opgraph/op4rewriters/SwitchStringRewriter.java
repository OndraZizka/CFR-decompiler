/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.Op04Rewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.AbstractMatchResultIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.CollectMatch;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleenePlus;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleeneStar;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.BooleanExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.NotOperation;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.AbstractStructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredBreak;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredCase;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredIf;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredSwitch;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.BeginBlock;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.placeholder.EndBlock;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class SwitchStringRewriter
implements Op04Rewriter {
    private final Options options;
    private final ClassFileVersion classFileVersion;

    public SwitchStringRewriter(Options options, ClassFileVersion classFileVersion) {
        this.options = options;
        this.classFileVersion = classFileVersion;
    }

    @Override
    public void rewrite(Op04StructuredStatement root) {
        List<StructuredStatement> structuredStatements;
        if (!this.options.getOption(OptionsImpl.STRING_SWITCH, this.classFileVersion).booleanValue()) {
            return;
        }
        if ((structuredStatements = MiscStatementTools.linearise(root)) == null) {
            return;
        }
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(structuredStatements);
        WildcardMatch wcm1 = new WildcardMatch();
        WildcardMatch wcm2 = new WildcardMatch();
        WildcardMatch wcm3 = new WildcardMatch();
        ResetAfterTest m = new ResetAfterTest(wcm1, new MatchSequence(new CollectMatch("ass1", new StructuredAssignment(wcm1.getLValueWildCard("stringobject"), wcm1.getExpressionWildCard("originalstring"))), (Matcher<StructuredStatement>)new CollectMatch("ass2", new StructuredAssignment(wcm1.getLValueWildCard("intermed"), wcm1.getExpressionWildCard("defaultintermed"))), (Matcher<StructuredStatement>)new CollectMatch("switch1", new StructuredSwitch(wcm1.getMemberFunction("switch", "hashCode", (Expression)new LValueExpression(wcm1.getLValueWildCard("stringobject"))), null, wcm1.getBlockIdentifier("switchblock"))), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new KleenePlus((Matcher<StructuredStatement>)new ResetAfterTest(wcm2, new MatchSequence(new StructuredCase(wcm2.getList("hashvals"), null, null, wcm2.getBlockIdentifier("case")), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new KleeneStar((Matcher<StructuredStatement>)new ResetAfterTest(wcm3, new MatchSequence(new StructuredIf(new BooleanExpression(wcm3.getMemberFunction("collision", "equals", new LValueExpression(wcm1.getLValueWildCard("stringobject")), wcm3.getExpressionWildCard("stringvalue"))), null), (Matcher<StructuredStatement>)new BeginBlock(null), (Matcher<StructuredStatement>)new StructuredAssignment(wcm1.getLValueWildCard("intermed"), wcm3.getExpressionWildCard("case2id")), (Matcher<StructuredStatement>)new StructuredBreak(wcm1.getBlockIdentifier("switchblock"), true), (Matcher<StructuredStatement>)new EndBlock(null)))), (Matcher<StructuredStatement>)new StructuredIf(new NotOperation(new BooleanExpression(wcm2.getMemberFunction("anticollision", "equals", new LValueExpression(wcm1.getLValueWildCard("stringobject")), wcm2.getExpressionWildCard("stringvalue")))), null), (Matcher<StructuredStatement>)new StructuredBreak(wcm1.getBlockIdentifier("switchblock"), true), (Matcher<StructuredStatement>)new StructuredAssignment(wcm1.getLValueWildCard("intermed"), wcm2.getExpressionWildCard("case2id")), (Matcher<StructuredStatement>)new KleeneStar((Matcher<StructuredStatement>)new StructuredBreak(wcm1.getBlockIdentifier("switchblock"), true)), (Matcher<StructuredStatement>)new EndBlock(null)))), (Matcher<StructuredStatement>)new EndBlock(null), (Matcher<StructuredStatement>)new CollectMatch("switch2", new StructuredSwitch(new LValueExpression(wcm1.getLValueWildCard("intermed")), null, wcm1.getBlockIdentifier("switchblock2")))));
        SwitchStringMatchResultCollector matchResultCollector = new SwitchStringMatchResultCollector(wcm1, wcm2, wcm3, null);
        while (mi.hasNext()) {
            mi.advance();
            matchResultCollector.clear();
            if (!m.match(mi, matchResultCollector)) continue;
            StructuredSwitch firstSwitch = (StructuredSwitch)matchResultCollector.getStatementByName("switch1");
            StructuredSwitch secondSwitch = (StructuredSwitch)matchResultCollector.getStatementByName("switch2");
            StructuredSwitch replacement = this.rewriteSwitch(secondSwitch, matchResultCollector);
            secondSwitch.getContainer().replaceContainedStatement(replacement);
            firstSwitch.getContainer().nopThis();
            ((AbstractStructuredStatement)matchResultCollector.getStatementByName("ass1")).getContainer().nopThis();
            ((AbstractStructuredStatement)matchResultCollector.getStatementByName("ass2")).getContainer().nopThis();
        }
    }

    private StructuredSwitch rewriteSwitch(StructuredSwitch original, SwitchStringMatchResultCollector matchResultCollector) {
        Op04StructuredStatement body = original.getBody();
        BlockIdentifier blockIdentifier = original.getBlockIdentifier();
        StructuredStatement inner = body.getStatement();
        if (!(inner instanceof Block)) {
            throw new FailedRewriteException("Switch body is not a block, is a " + inner.getClass());
        }
        Block block = (Block)inner;
        Map<Integer, List<String>> replacements = matchResultCollector.getValidatedHashes();
        List<Op04StructuredStatement> caseStatements = block.getBlockStatements();
        LinkedList tgt = ListFactory.newLinkedList();
        InferredJavaType typeOfSwitch = matchResultCollector.getStringExpression().getInferredJavaType();
        Iterator<Op04StructuredStatement> i$ = caseStatements.iterator();
        while (i$.hasNext()) {
            Op04StructuredStatement op04StructuredStatement;
            if (!(inner = (op04StructuredStatement = i$.next()).getStatement() instanceof StructuredCase)) {
                throw new FailedRewriteException("Block member is not a case, it's a " + inner.getClass());
            }
            StructuredCase structuredCase = (StructuredCase)inner;
            List<Expression> values = structuredCase.getValues();
            List transformedValues = ListFactory.newList();
            Iterator<Expression> i$2 = values.iterator();
            while (i$2.hasNext()) {
                List<String> replacementStrings;
                Integer i;
                Expression value;
                if ((replacementStrings = replacements.get(i = SwitchStringRewriter.getInt(value = i$2.next()))) == null) {
                    throw new FailedRewriteException("No replacements for " + i);
                }
                for (String s : replacementStrings) {
                    transformedValues.add((Literal)new Literal(TypedLiteral.getString(s)));
                }
            }
            StructuredCase replacementStructuredCase = new StructuredCase(transformedValues, typeOfSwitch, structuredCase.getBody(), structuredCase.getBlockIdentifier());
            tgt.add((Op04StructuredStatement)new Op04StructuredStatement(replacementStructuredCase));
        }
        Block newBlock = new Block(tgt, true);
        return new StructuredSwitch(matchResultCollector.getStringExpression(), new Op04StructuredStatement(newBlock), blockIdentifier);
    }

    static String getString(Expression e) {
        TypedLiteral typedLiteral;
        Literal l;
        if (!(e instanceof Literal)) {
            throw new TooOptimisticMatchException(null);
        }
        if ((typedLiteral = (l = (Literal)e).getValue()).getType() != TypedLiteral.LiteralType.String) {
            throw new TooOptimisticMatchException(null);
        }
        String s = (String)typedLiteral.getValue();
        return s;
    }

    static Integer getInt(Expression e) {
        TypedLiteral typedLiteral;
        Literal l;
        if (!(e instanceof Literal)) {
            throw new TooOptimisticMatchException(null);
        }
        if ((typedLiteral = (l = (Literal)e).getValue()).getType() == TypedLiteral.LiteralType.Integer) return (Integer)typedLiteral.getValue();
        throw new TooOptimisticMatchException(null);
    }

    class 1 {
    }

    static class FailedRewriteException
    extends IllegalStateException {
        public FailedRewriteException(String s) {
            super(s);
        }
    }

    static class TooOptimisticMatchException
    extends IllegalStateException {
        private TooOptimisticMatchException() {
        }

        /* synthetic */ TooOptimisticMatchException(1 x0) {
            this();
        }
    }

    static class SwitchStringMatchResultCollector
    extends AbstractMatchResultIterator {
        private final WildcardMatch wholeBlock;
        private final WildcardMatch caseStatement;
        private final WildcardMatch hashCollision;
        private Expression stringExpression = null;
        private final List<Pair<String, Integer>> pendingHashCode = ListFactory.newList();
        private final Map<Integer, List<String>> validatedHashes;
        private final Map<String, StructuredStatement> collectedStatements;

        private SwitchStringMatchResultCollector(WildcardMatch wholeBlock, WildcardMatch caseStatement, WildcardMatch hashCollision) {
            this.validatedHashes = MapFactory.newLazyMap(new UnaryFunction<Integer, List<String>>(){

                @Override
                public List<String> invoke(Integer arg) {
                    return ListFactory.newList();
                }
            });
            this.collectedStatements = MapFactory.newMap();
            this.wholeBlock = wholeBlock;
            this.caseStatement = caseStatement;
            this.hashCollision = hashCollision;
        }

        @Override
        public void clear() {
            this.stringExpression = null;
            this.pendingHashCode.clear();
            this.validatedHashes.clear();
            this.collectedStatements.clear();
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            this.collectedStatements.put(name, statement);
        }

        /*
         * Enabled aggressive block sorting
         */
        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
            if (wcm == this.wholeBlock) {
                Expression stringObject;
                this.stringExpression = stringObject = wcm.getExpressionWildCard("originalstring").getMatch();
                return;
            } else if (wcm == this.caseStatement) {
                List hashvals = wcm.getList("hashvals").getMatch();
                Expression case2id = wcm.getExpressionWildCard("case2id").getMatch();
                Expression stringValue = wcm.getExpressionWildCard("stringvalue").getMatch();
                this.pendingHashCode.add(Pair.make(SwitchStringRewriter.getString(stringValue), SwitchStringRewriter.getInt(case2id)));
                this.processPendingWithHashCode(hashvals);
                return;
            } else {
                if (wcm != this.hashCollision) throw new IllegalStateException();
                Expression case2id = wcm.getExpressionWildCard("case2id").getMatch();
                Expression stringValue = wcm.getExpressionWildCard("stringvalue").getMatch();
                this.pendingHashCode.add(Pair.make(SwitchStringRewriter.getString(stringValue), SwitchStringRewriter.getInt(case2id)));
            }
        }

        void processPendingWithHashCode(List<Expression> hashVals) {
            for (Pair<String, Integer> pair : this.pendingHashCode) {
                this.validatedHashes.get(pair.getSecond()).add(pair.getFirst());
            }
            this.pendingHashCode.clear();
        }

        public Expression getStringExpression() {
            return this.stringExpression;
        }

        public Map<Integer, List<String>> getValidatedHashes() {
            return this.validatedHashes;
        }

        public StructuredStatement getStatementByName(String name) {
            StructuredStatement structuredStatement = this.collectedStatements.get(name);
            if (structuredStatement != null) return structuredStatement;
            throw new IllegalArgumentException("No collected statement " + name);
        }

        /* synthetic */ SwitchStringMatchResultCollector(WildcardMatch x0, WildcardMatch x1, WildcardMatch x2, 1 x3) {
            this(x0, x1, x2);
        }

    }

}

