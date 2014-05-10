/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.wildcard;

import java.util.AbstractList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.CastExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConditionalExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationAnoynmousInner;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.ConstructorInvokationSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.StaticFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.SuperFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StackSSALabel;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockType;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.EquivalenceConstraint;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.Wildcard;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.Block;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.exceptions.ExceptionCheck;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class WildcardMatch {
    private Map<String, LValueWildcard> lValueMap = MapFactory.newMap();
    private Map<String, StackLabelWildCard> lStackValueMap = MapFactory.newMap();
    private Map<String, ExpressionWildcard> expressionMap = MapFactory.newMap();
    private Map<String, NewArrayWildcard> newArrayWildcardMap = MapFactory.newMap();
    private Map<String, MemberFunctionInvokationWildcard> memberFunctionMap = MapFactory.newMap();
    private Map<String, SuperFunctionInvokationWildcard> superFunctionMap = MapFactory.newMap();
    private Map<String, StaticFunctionInvokationWildcard> staticFunctionMap = MapFactory.newMap();
    private Map<String, BlockIdentifierWildcard> blockIdentifierWildcardMap = MapFactory.newMap();
    private Map<String, ListWildcard> listMap = MapFactory.newMap();
    private Map<String, StaticVariableWildcard> staticVariableWildcardMap = MapFactory.newMap();
    private Map<String, ConstructorInvokationSimpleWildcard> constructorWildcardMap = MapFactory.newMap();
    private Map<String, ConstructorInvokationAnonymousInnerWildcard> constructorAnonymousWildcardMap = MapFactory.newMap();
    private Map<String, CastExpressionWildcard> castWildcardMap = MapFactory.newMap();
    private Map<String, ConditionalExpressionWildcard> conditionalWildcardMap = MapFactory.newMap();
    private Map<String, BlockWildcard> blockWildcardMap = MapFactory.newMap();

    private <T> void reset(Collection<? extends Wildcard<T>> coll) {
        for (Wildcard item : coll) {
            item.resetMatch();
        }
    }

    public void reset() {
        this.reset((Collection<? extends Wildcard<T>>)this.lValueMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.lStackValueMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.expressionMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.newArrayWildcardMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.memberFunctionMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.blockIdentifierWildcardMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.listMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.staticFunctionMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.staticVariableWildcardMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.superFunctionMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.constructorWildcardMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.constructorAnonymousWildcardMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.castWildcardMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.conditionalWildcardMap.values());
        this.reset((Collection<? extends Wildcard<T>>)this.blockWildcardMap.values());
    }

    public BlockWildcard getBlockWildcard(String name) {
        BlockWildcard res = this.blockWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new BlockWildcard();
        this.blockWildcardMap.put(name, res);
        return res;
    }

    public StackLabelWildCard getStackLabelWildcard(String name) {
        StackLabelWildCard res = this.lStackValueMap.get(name);
        if (res != null) {
            return res;
        }
        res = new StackLabelWildCard();
        this.lStackValueMap.put(name, res);
        return res;
    }

    public ConditionalExpressionWildcard getConditionalExpressionWildcard(String name) {
        ConditionalExpressionWildcard res = this.conditionalWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new ConditionalExpressionWildcard();
        this.conditionalWildcardMap.put(name, res);
        return res;
    }

    public ConstructorInvokationSimpleWildcard getConstructorSimpleWildcard(String name) {
        ConstructorInvokationSimpleWildcard res = this.constructorWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new ConstructorInvokationSimpleWildcard(null, null);
        this.constructorWildcardMap.put(name, res);
        return res;
    }

    public ConstructorInvokationSimpleWildcard getConstructorSimpleWildcard(String name, JavaTypeInstance clazz) {
        ConstructorInvokationSimpleWildcard res = this.constructorWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new ConstructorInvokationSimpleWildcard(clazz, null);
        this.constructorWildcardMap.put(name, res);
        return res;
    }

    public ConstructorInvokationAnonymousInnerWildcard getConstructorAnonymousWildcard(String name) {
        ConstructorInvokationAnonymousInnerWildcard res = this.constructorAnonymousWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new ConstructorInvokationAnonymousInnerWildcard(null, null);
        this.constructorAnonymousWildcardMap.put(name, res);
        return res;
    }

    public ConstructorInvokationAnonymousInnerWildcard getConstructorAnonymousWildcard(String name, JavaTypeInstance clazz) {
        ConstructorInvokationAnonymousInnerWildcard res = this.constructorAnonymousWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new ConstructorInvokationAnonymousInnerWildcard(clazz, null);
        this.constructorAnonymousWildcardMap.put(name, res);
        return res;
    }

    public LValueWildcard getLValueWildCard(String name, Predicate<LValue> test) {
        LValueWildcard res = this.lValueMap.get(name);
        if (res != null) {
            return res;
        }
        res = new LValueWildcard(name, test, null);
        this.lValueMap.put(name, res);
        return res;
    }

    public LValueWildcard getLValueWildCard(String name) {
        LValueWildcard res = this.lValueMap.get(name);
        if (res != null) {
            return res;
        }
        res = new LValueWildcard(name, null, null);
        this.lValueMap.put(name, res);
        return res;
    }

    public ExpressionWildcard getExpressionWildCard(String name) {
        ExpressionWildcard res = this.expressionMap.get(name);
        if (res != null) {
            return res;
        }
        res = new ExpressionWildcard(name);
        this.expressionMap.put(name, res);
        return res;
    }

    public CastExpressionWildcard getCastExpressionWildcard(String name, Expression expression) {
        CastExpressionWildcard res = this.castWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new CastExpressionWildcard(null, expression);
        this.castWildcardMap.put(name, res);
        return res;
    }

    public CastExpressionWildcard getCastExpressionWildcard(String name) {
        return this.castWildcardMap.get(name);
    }

    public NewArrayWildcard getNewArrayWildCard(String name) {
        return this.getNewArrayWildCard(name, 1, null);
    }

    public NewArrayWildcard getNewArrayWildCard(String name, int numSizedDims, Integer numTotalDims) {
        NewArrayWildcard res = this.newArrayWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new NewArrayWildcard(name, numSizedDims, numTotalDims);
        this.newArrayWildcardMap.put(name, res);
        return res;
    }

    public SuperFunctionInvokationWildcard getSuperFunction(String name) {
        return this.getSuperFunction(name, null);
    }

    public SuperFunctionInvokationWildcard getSuperFunction(String name, List<Expression> args) {
        SuperFunctionInvokationWildcard res = this.superFunctionMap.get(name);
        if (res != null) {
            return res;
        }
        res = new SuperFunctionInvokationWildcard(args);
        this.superFunctionMap.put(name, res);
        return res;
    }

    public MemberFunctionInvokationWildcard getMemberFunction(String name) {
        return this.memberFunctionMap.get(name);
    }

    public MemberFunctionInvokationWildcard getMemberFunction(String name, boolean isInitMethod, Expression object) {
        return this.getMemberFunction(name, (String)null, isInitMethod, object, ListFactory.newList());
    }

    public MemberFunctionInvokationWildcard getMemberFunction(String name, String methodname, Expression object) {
        return this.getMemberFunction(name, methodname, false, object, ListFactory.newList());
    }

    public /* varargs */ MemberFunctionInvokationWildcard getMemberFunction(String name, String methodname, Expression object, Expression ... args) {
        return this.getMemberFunction(name, methodname, false, object, ListFactory.newList(args));
    }

    public MemberFunctionInvokationWildcard getMemberFunction(String name, String methodname, boolean isInitMethod, Expression object, List<Expression> args) {
        MemberFunctionInvokationWildcard res = this.memberFunctionMap.get(name);
        if (res != null) {
            return res;
        }
        res = new MemberFunctionInvokationWildcard(methodname, isInitMethod, object, args);
        this.memberFunctionMap.put(name, res);
        return res;
    }

    public StaticFunctionInvokationWildcard getStaticFunction(String name, JavaTypeInstance clazz, String methodname) {
        return this.getStaticFunction(name, clazz, methodname, ListFactory.newList());
    }

    public /* varargs */ StaticFunctionInvokationWildcard getStaticFunction(String name, JavaTypeInstance clazz, String methodname, Expression ... args) {
        return this.getStaticFunction(name, clazz, methodname, ListFactory.newList(args));
    }

    public StaticFunctionInvokationWildcard getStaticFunction(String name, JavaTypeInstance clazz, String methodname, List<Expression> args) {
        StaticFunctionInvokationWildcard res = this.staticFunctionMap.get(name);
        if (res != null) {
            return res;
        }
        res = new StaticFunctionInvokationWildcard(methodname, clazz, args);
        this.staticFunctionMap.put(name, res);
        return res;
    }

    public StaticFunctionInvokationWildcard getStaticFunction(String name) {
        return this.staticFunctionMap.get(name);
    }

    public StaticVariableWildcard getStaticVariable(String name) {
        return this.staticVariableWildcardMap.get(name);
    }

    public StaticVariableWildcard getStaticVariable(String name, JavaTypeInstance clazz, InferredJavaType varType) {
        return this.getStaticVariable(name, clazz, varType, true);
    }

    public StaticVariableWildcard getStaticVariable(String name, JavaTypeInstance clazz, InferredJavaType varType, boolean requireTypeMatch) {
        StaticVariableWildcard res = this.staticVariableWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new StaticVariableWildcard(varType, clazz, requireTypeMatch);
        this.staticVariableWildcardMap.put(name, res);
        return res;
    }

    public BlockIdentifierWildcard getBlockIdentifier(String name) {
        BlockIdentifierWildcard res = this.blockIdentifierWildcardMap.get(name);
        if (res != null) {
            return res;
        }
        res = new BlockIdentifierWildcard();
        this.blockIdentifierWildcardMap.put(name, res);
        return res;
    }

    public <T> ListWildcard getList(String name) {
        ListWildcard res = this.listMap.get(name);
        if (res != null) {
            return res;
        }
        res = new ListWildcard();
        this.listMap.put(name, res);
        return res;
    }

    public boolean match(Object pattern, Object test) {
        return pattern.equals(test);
    }

    class 1 {
    }

    public class BlockWildcard
    extends Block
    implements Wildcard<Block> {
        private Block match;

        public BlockWildcard() {
            super(null, false);
        }

        @Override
        public Block getMatch() {
            return this.match;
        }

        @Override
        public void resetMatch() {
            this.match = null;
        }

        public boolean equals(Object o) {
            Block other;
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof Block)) {
                return false;
            }
            if (this.match != null) {
                return this.match == o;
            }
            this.match = other = (Block)o;
            return true;
        }
    }

    public class ConditionalExpressionWildcard
    extends AbstractBaseExpressionWildcard
    implements ConditionalExpression,
    Wildcard<ConditionalExpression> {
        private ConditionalExpression matchedValue;

        public ConditionalExpressionWildcard() {
            super(null);
        }

        @Override
        public ConditionalExpression getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }

        public boolean equals(Object o) {
            ConditionalExpression other;
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof ConditionalExpression)) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            this.matchedValue = other = (ConditionalExpression)o;
            return true;
        }

        @Override
        public ConditionalExpression simplify() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConditionalExpression optimiseForType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<LValue> getLoopLValues() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConditionalExpression getDemorganApplied(boolean amNegating) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ConditionalExpression getNegated() {
            throw new UnsupportedOperationException();
        }
    }

    public class CastExpressionWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<CastExpression> {
        private final JavaTypeInstance clazz;
        private CastExpression matchedValue;
        private Expression expression;

        public CastExpressionWildcard(JavaTypeInstance clazz, Expression expression) {
            super(null);
            this.clazz = clazz;
            this.expression = expression;
        }

        @Override
        public CastExpression getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof CastExpression)) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            CastExpression other = (CastExpression)o;
            if (!(this.clazz == null || this.clazz.equals(other.getInferredJavaType().getJavaTypeInstance()))) {
                return false;
            }
            if (!this.expression.equals(other.getChild())) {
                return false;
            }
            this.matchedValue = other;
            return true;
        }
    }

    public class ConstructorInvokationAnonymousInnerWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<ConstructorInvokationAnoynmousInner> {
        private ConstructorInvokationAnoynmousInner matchedValue;
        private final JavaTypeInstance clazz;
        private final List<Expression> args;

        public ConstructorInvokationAnonymousInnerWildcard(JavaTypeInstance clazz, List<Expression> args) {
            super(null);
            this.clazz = clazz;
            this.args = args;
        }

        @Override
        public ConstructorInvokationAnoynmousInner getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof ConstructorInvokationAnoynmousInner)) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            ConstructorInvokationAnoynmousInner other = (ConstructorInvokationAnoynmousInner)o;
            JavaTypeInstance otherType = other.getTypeInstance();
            if (!(this.clazz == null || this.clazz.equals(otherType))) {
                return false;
            }
            if (this.args != null && this.args.equals((Object)other.getArgs())) {
                return false;
            }
            this.matchedValue = other;
            return true;
        }
    }

    public class ConstructorInvokationSimpleWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<ConstructorInvokationSimple> {
        private ConstructorInvokationSimple matchedValue;
        private final JavaTypeInstance clazz;
        private final List<Expression> args;

        public ConstructorInvokationSimpleWildcard(JavaTypeInstance clazz, List<Expression> args) {
            super(null);
            this.clazz = clazz;
            this.args = args;
        }

        @Override
        public ConstructorInvokationSimple getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }

        public boolean equals(Object o) {
            ConstructorInvokationSimple other;
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof ConstructorInvokationSimple)) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            if (!this.clazz.equals((other = (ConstructorInvokationSimple)o).getTypeInstance())) {
                return false;
            }
            if (this.args != null && this.args.equals((Object)other.getArgs())) {
                return false;
            }
            this.matchedValue = other;
            return true;
        }
    }

    public class StaticVariableWildcard
    extends StaticVariable
    implements Wildcard<StaticVariable> {
        private StaticVariable matchedValue;
        private final boolean requireTypeMatch;

        public StaticVariableWildcard(InferredJavaType type, JavaTypeInstance clazz, boolean requireTypeMatch) {
            super(type, clazz, (String)null);
            this.requireTypeMatch = requireTypeMatch;
        }

        @Override
        public StaticVariable getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }

        @Override
        public boolean equals(Object o) {
            StaticVariable other;
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            if (!(o instanceof StaticVariable)) {
                return false;
            }
            if (!this.getOwningClassTypeInstance().equals((other = (StaticVariable)o).getOwningClassTypeInstance())) {
                return false;
            }
            JavaTypeInstance thisType = this.getInferredJavaType().getJavaTypeInstance();
            JavaTypeInstance otherType = other.getInferredJavaType().getJavaTypeInstance();
            if (this.requireTypeMatch && !thisType.equals(otherType)) {
                return false;
            }
            this.matchedValue = other;
            return true;
        }
    }

    public class ListWildcard
    extends AbstractList
    implements Wildcard<List> {
        private List matchedValue;

        @Override
        public Object get(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            List other;
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            if (!(o instanceof List)) {
                return false;
            }
            this.matchedValue = other = (List)o;
            return true;
        }

        @Override
        public List getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    public class BlockIdentifierWildcard
    extends BlockIdentifier
    implements Wildcard<BlockIdentifier> {
        private BlockIdentifier matchedValue;

        public BlockIdentifierWildcard() {
            super(0, null);
        }

        public boolean equals(Object o) {
            BlockIdentifier other;
            if (o == this) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            if (!(o instanceof BlockIdentifier)) {
                return false;
            }
            this.matchedValue = other = (BlockIdentifier)o;
            return true;
        }

        @Override
        public BlockIdentifier getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    public class StaticFunctionInvokationWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<StaticFunctionInvokation> {
        private final String name;
        private final JavaTypeInstance clazz;
        private final List<Expression> args;
        private transient StaticFunctionInvokation matchedValue;

        public StaticFunctionInvokationWildcard(String name, JavaTypeInstance clazz, List<Expression> args) {
            super(null);
            this.name = name;
            this.clazz = clazz;
            this.args = args;
        }

        public boolean equals(Object o) {
            if (!(o instanceof StaticFunctionInvokation)) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            StaticFunctionInvokation other = (StaticFunctionInvokation)o;
            if (!(this.name == null || this.name.equals(other.getName()))) {
                return false;
            }
            if (!this.clazz.equals(other.getClazz())) {
                return false;
            }
            List<Expression> otherArgs = other.getArgs();
            if (this.args != null) {
                if (this.args.size() != otherArgs.size()) {
                    return false;
                }
                for (int x = 0; x < this.args.size(); ++x) {
                    Expression hisArg;
                    Expression myArg = this.args.get(x);
                    if (myArg.equals(hisArg = otherArgs.get(x))) continue;
                    return false;
                }
            }
            this.matchedValue = other;
            return true;
        }

        @Override
        public StaticFunctionInvokation getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    public class SuperFunctionInvokationWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<SuperFunctionInvokation> {
        private final List<Expression> args;
        private transient SuperFunctionInvokation matchedValue;

        public SuperFunctionInvokationWildcard(List<Expression> args) {
            super(null);
            this.args = args;
        }

        public boolean equals(Object o) {
            if (!(o instanceof SuperFunctionInvokation)) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            SuperFunctionInvokation other = (SuperFunctionInvokation)o;
            if (this.args != null) {
                List otherArgs;
                if (this.args.size() != (otherArgs = other.getArgs()).size()) {
                    return false;
                }
                for (int x = 0; x < this.args.size(); ++x) {
                    Expression hisArg;
                    Expression myArg = this.args.get(x);
                    if (myArg.equals(hisArg = (Expression)otherArgs.get(x))) continue;
                    return false;
                }
            }
            this.matchedValue = other;
            return true;
        }

        @Override
        public SuperFunctionInvokation getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    public class MemberFunctionInvokationWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<MemberFunctionInvokation> {
        private final String name;
        private final boolean isInitMethod;
        private final Expression object;
        private final List<Expression> args;
        private transient MemberFunctionInvokation matchedValue;

        public MemberFunctionInvokationWildcard(String name, boolean isInitMethod, Expression object, List<Expression> args) {
            super(null);
            this.name = name;
            this.isInitMethod = isInitMethod;
            this.object = object;
            this.args = args;
        }

        public boolean equals(Object o) {
            MemberFunctionInvokation other;
            if (!(o instanceof MemberFunctionInvokation)) {
                return false;
            }
            if (this.matchedValue != null) {
                return this.matchedValue.equals(o);
            }
            if (this.isInitMethod != (other = (MemberFunctionInvokation)o).isInitMethod()) {
                return false;
            }
            if (this.name != null) {
                if (!this.name.equals(other.getName())) {
                    return false;
                }
            }
            if (!this.object.equals(other.getObject())) {
                return false;
            }
            List otherArgs = other.getArgs();
            if (this.args != null) {
                if (this.args.size() != otherArgs.size()) {
                    return false;
                }
                for (int x = 0; x < this.args.size(); ++x) {
                    Expression hisArg;
                    Expression myArg = this.args.get(x);
                    if (myArg.equals(hisArg = (Expression)otherArgs.get(x))) continue;
                    return false;
                }
            }
            this.matchedValue = (MemberFunctionInvokation)o;
            return true;
        }

        @Override
        public MemberFunctionInvokation getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    public class NewArrayWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<AbstractNewArray> {
        private final String name;
        private final int numSizedDims;
        private final Integer numTotalDims;
        private transient AbstractNewArray matchedValue;

        public NewArrayWildcard(String name, int numSizedDims, Integer numTotalDims) {
            super(null);
            this.name = name;
            this.numSizedDims = numSizedDims;
            this.numTotalDims = numTotalDims;
        }

        public boolean equals(Object o) {
            AbstractNewArray abstractNewArray;
            if (!(o instanceof AbstractNewArray)) {
                return false;
            }
            if (this.matchedValue != null) return this.matchedValue.equals(o);
            if (this.numSizedDims != (abstractNewArray = (AbstractNewArray)o).getNumSizedDims()) {
                return false;
            }
            if (this.numTotalDims != null && this.numTotalDims.intValue() != abstractNewArray.getNumDims()) {
                return false;
            }
            this.matchedValue = abstractNewArray;
            return true;
        }

        @Override
        public AbstractNewArray getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    public class ExpressionWildcard
    extends AbstractBaseExpressionWildcard
    implements Wildcard<Expression> {
        private final String name;
        private transient Expression matchedValue;

        public ExpressionWildcard(String name) {
            super(null);
            this.name = name;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Expression)) {
                return false;
            }
            if (this.matchedValue != null) return this.matchedValue.equals(o);
            this.matchedValue = (Expression)o;
            return true;
        }

        @Override
        public Expression getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    abstract class AbstractBaseExpressionWildcard
    extends DebugDumpable
    implements Expression {
        private AbstractBaseExpressionWildcard() {
            super(null);
        }

        @Override
        public Expression replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSimple() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void collectUsedLValues(LValueUsageCollector lValueUsageCollector) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean canPushDownInto() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression pushDown(Expression toPush, Expression parent) {
            throw new UnsupportedOperationException();
        }

        @Override
        public InferredJavaType getInferredJavaType() {
            return InferredJavaType.IGNORE;
        }

        @Override
        public Expression deepClone(CloneHelper cloneHelper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Expression outerDeepClone(CloneHelper cloneHelper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Precedence getPrecedence() {
            return Precedence.WEAKEST;
        }

        @Override
        public Dumper dumpWithOuterPrecedence(Dumper d, Precedence outerPrecedence) {
            return this.dump(d);
        }

        @Override
        public boolean equivalentUnder(Object o, EquivalenceConstraint constraint) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void collectTypeUsages(TypeUsageCollector collector) {
        }

        @Override
        public boolean canThrow(ExceptionCheck caught) {
            return true;
        }

        @Override
        public Literal getComputedLiteral(Map<LValue, Literal> display) {
            return null;
        }

        /* synthetic */ AbstractBaseExpressionWildcard(WildcardMatch x0, 1 x1) {
            this();
        }
    }

    public class StackLabelWildCard
    extends StackSSALabel
    implements Wildcard<StackSSALabel> {
        private transient StackSSALabel matchedValue;

        public StackLabelWildCard() {
            super(new InferredJavaType(RawJavaType.INT, InferredJavaType.Source.TEST));
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof StackSSALabel)) {
                return false;
            }
            if (this.matchedValue != null) return this.matchedValue.equals(o);
            this.matchedValue = (StackSSALabel)o;
            return true;
        }

        @Override
        public StackSSALabel getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }
    }

    public class LValueWildcard
    extends DebugDumpable
    implements LValue,
    Wildcard<LValue> {
        private final String name;
        private final Predicate<LValue> test;
        private transient LValue matchedValue;

        private LValueWildcard(String name, Predicate<LValue> test) {
            super(null);
            this.name = name;
            this.test = test;
        }

        @Override
        public void collectTypeUsages(TypeUsageCollector collector) {
        }

        @Override
        public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        }

        @Override
        public LValue deepClone(CloneHelper cloneHelper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LValue outerDeepClone(CloneHelper cloneHelper) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getNumberOfCreators() {
            throw new UnsupportedOperationException();
        }

        public void collectLValueAssignments(Expression assignedTo, StatementContainer statementContainer, LValueAssignmentCollector lValueAssigmentCollector) {
            throw new UnsupportedOperationException();
        }

        @Override
        public SSAIdentifiers<LValue> collectVariableMutation(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LValue replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LValue applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
            return this;
        }

        @Override
        public InferredJavaType getInferredJavaType() {
            return InferredJavaType.IGNORE;
        }

        @Override
        public Precedence getPrecedence() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Dumper dumpWithOuterPrecedence(Dumper d, Precedence outerPrecedence) {
            return d;
        }

        @Override
        public boolean canThrow(ExceptionCheck caught) {
            return true;
        }

        public boolean equals(Object o) {
            if (!(o instanceof LValue)) {
                return false;
            }
            if (this.matchedValue != null) return this.matchedValue.equals(o);
            if (this.test != null && !this.test.test((LValue)o)) return false;
            this.matchedValue = (LValue)o;
            return true;
        }

        @Override
        public LValue getMatch() {
            return this.matchedValue;
        }

        @Override
        public void resetMatch() {
            this.matchedValue = null;
        }

        /* synthetic */ LValueWildcard(WildcardMatch x0, String x1, Predicate x2, 1 x3) {
            this(x1, x2);
        }
    }

    static class DebugDumpable
    implements Dumpable {
        private DebugDumpable() {
        }

        @Override
        public Dumper dump(Dumper dumper) {
            return dumper.print("" + this.getClass() + " : " + this.toString());
        }

        /* synthetic */ DebugDumpable(1 x0) {
            this();
        }
    }

}

