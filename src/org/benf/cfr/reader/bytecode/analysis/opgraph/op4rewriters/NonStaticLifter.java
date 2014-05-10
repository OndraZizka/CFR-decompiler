/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.MemberFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.SuperFunctionInvokation;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.FieldVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollectorSimple;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredExpressionStatement;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.Predicate;

public class NonStaticLifter {
    private final ClassFile classFile;
    private final ConstantPool cp;

    public NonStaticLifter(ClassFile classFile) {
        this.classFile = classFile;
        this.cp = classFile.getConstantPool();
    }

    private boolean isDelegating(Method constructor) {
        List<Op04StructuredStatement> statements = MiscStatementTools.getBlockStatements(constructor.getAnalysis());
        if (statements == null) {
            return false;
        }
        Iterator<Op04StructuredStatement> i$ = statements.iterator();
        while (i$.hasNext()) {
            StructuredStatement structuredStatement;
            Op04StructuredStatement statement;
            WildcardMatch wcm1;
            StructuredExpressionStatement structuredExpressionStatement;
            StructuredExpressionStatement test;
            if (structuredStatement = (statement = i$.next()).getStatement() instanceof StructuredComment) continue;
            if (!(structuredStatement instanceof StructuredExpressionStatement)) {
                return false;
            }
            if (!(test = new StructuredExpressionStatement((wcm1 = new WildcardMatch()).getMemberFunction("m", (String)null, true, (Expression)new LValueExpression((wcm1 = new WildcardMatch()).getLValueWildCard("o")), (List)null), false)).equals(structuredExpressionStatement = (StructuredExpressionStatement)structuredStatement)) return false;
            MemberFunctionInvokation m = wcm1.getMemberFunction("m").getMatch();
            MethodPrototype prototype = m.getMethodPrototype();
            return true;
        }
        return false;
    }

    public void liftNonStatics() {
        LinkedList<ClassFileField> classFileFields = new LinkedList<ClassFileField>(Functional.filter(this.classFile.getFields(), new Predicate<ClassFileField>(){

            @Override
            public boolean test(ClassFileField in) {
                if (in.getField().testAccessFlag(AccessFlag.ACC_STATIC)) {
                    return false;
                }
                if (!in.getField().testAccessFlag(AccessFlag.ACC_SYNTHETIC)) return true;
                return false;
            }
        }));
        if (classFileFields.isEmpty()) {
            return;
        }
        Map fieldMap = MapFactory.newMap();
        int len = classFileFields.size();
        for (int x = 0; x < len; ++x) {
            ClassFileField classFileField = classFileFields.get(x);
            fieldMap.put((String)classFileField.getField().getFieldName(), Pair.make(x, classFileField));
        }
        List<Method> constructors = Functional.filter(this.classFile.getConstructors(), new Predicate<Method>(){

            @Override
            public boolean test(Method in) {
                return !NonStaticLifter.this.isDelegating(in);
            }
        });
        List constructorCodeList = ListFactory.newList();
        int minSize = Integer.MAX_VALUE;
        Iterator<Method> i$ = constructors.iterator();
        while (i$.hasNext()) {
            Expression expression;
            StructuredStatement superTest;
            Method constructor;
            List<Op04StructuredStatement> blockStatements;
            if ((blockStatements = MiscStatementTools.getBlockStatements((constructor = i$.next()).getAnalysis())) == null) {
                return;
            }
            if ((blockStatements = Functional.filter(blockStatements, new Predicate<Op04StructuredStatement>(){

                @Override
                public boolean test(Op04StructuredStatement in) {
                    return !(in.getStatement() instanceof StructuredComment);
                }
            })).isEmpty()) {
                return;
            }
            if (superTest = blockStatements.get(0).getStatement() instanceof StructuredExpressionStatement && expression = ((StructuredExpressionStatement)superTest).getExpression() instanceof SuperFunctionInvokation) {
                blockStatements.remove(0);
            }
            constructorCodeList.add(blockStatements);
            if (blockStatements.size() >= minSize) continue;
            minSize = blockStatements.size();
        }
        if (constructorCodeList.isEmpty()) {
            return;
        }
        int numConstructors = constructorCodeList.size();
        List constructorCode = (List)constructorCodeList.get(0);
        for (int x2 = 0; x2 < minSize; ++x2) {
            StructuredAssignment structuredAssignment;
            LValue lValue;
            if (constructorCode.isEmpty()) {
                return;
            }
            StructuredStatement s1 = ((Op04StructuredStatement)constructorCode.get(x2)).getStatement();
            for (int y = 1; y < numConstructors; ++y) {
                StructuredStatement sOther;
                if (s1.equals(sOther = ((Op04StructuredStatement)((List)constructorCodeList.get(y)).get(x2)).getStatement())) continue;
                return;
            }
            if (!(s1 instanceof StructuredAssignment)) {
                return;
            }
            if (!(lValue = (structuredAssignment = (StructuredAssignment)s1).getLvalue() instanceof FieldVariable)) {
                return;
            }
            if (!this.fromThisClass((FieldVariable)lValue)) {
                return;
            }
            if (!this.tryLift((FieldVariable)lValue, structuredAssignment.getRvalue(), fieldMap)) {
                return;
            }
            for (List constructorCodeLst1 : constructorCodeList) {
                ((Op04StructuredStatement)constructorCodeLst1.get(x2)).nopOut();
            }
        }
    }

    private boolean fromThisClass(FieldVariable fv) {
        return fv.getOwningClassType().equals(this.classFile.getClassType());
    }

    private boolean tryLift(FieldVariable lValue, Expression rValue, Map<String, Pair<Integer, ClassFileField>> fieldMap) {
        Pair<Integer, ClassFileField> thisField = fieldMap.get(lValue.getFieldName());
        if (thisField == null) {
            return false;
        }
        ClassFileField classFileField = thisField.getSecond();
        int thisIdx = thisField.getFirst();
        LValueUsageCollectorSimple usageCollector = new LValueUsageCollectorSimple();
        rValue.collectUsedLValues(usageCollector);
        for (LValue usedLValue : usageCollector.getUsedLValues()) {
            int usedIdx;
            Pair<Integer, ClassFileField> usedField;
            FieldVariable usedFieldVariable;
            if (usedLValue instanceof StaticVariable) continue;
            if (!(usedLValue instanceof FieldVariable)) return false;
            if (!this.fromThisClass(usedFieldVariable = (FieldVariable)usedLValue)) {
                return false;
            }
            if ((usedField = fieldMap.get(usedFieldVariable.getFieldName())) == null) {
                return false;
            }
            ClassFileField usedClassFileField = usedField.getSecond();
            if ((usedIdx = usedField.getFirst().intValue()) >= thisIdx) {
                return false;
            }
            if (usedClassFileField.getInitialValue() != null) continue;
            return false;
        }
        classFileField.setInitialValue(rValue);
        return true;
    }

}

