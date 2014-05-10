/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.StaticVariable;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredComment;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.util.Functional;
import org.benf.cfr.reader.util.Predicate;

public class StaticLifter {
    private final ClassFile classFile;

    public StaticLifter(ClassFile classFile) {
        this.classFile = classFile;
    }

    public void liftStatics(Method staticInit) {
        List<Op04StructuredStatement> statements;
        LinkedList<ClassFileField> classFileFields = new LinkedList<ClassFileField>(Functional.filter(this.classFile.getFields(), new Predicate<ClassFileField>(){

            @Override
            public boolean test(ClassFileField in) {
                if (!in.getField().testAccessFlag(AccessFlag.ACC_STATIC)) {
                    return false;
                }
                if (in.getField().testAccessFlag(AccessFlag.ACC_SYNTHETIC)) {
                    return false;
                }
                if (in.getInitialValue() == null) return true;
                return false;
            }
        }));
        if (classFileFields.isEmpty()) {
            return;
        }
        if ((statements = MiscStatementTools.getBlockStatements(staticInit.getAnalysis())) == null) {
            return;
        }
        Iterator<Op04StructuredStatement> iterator = statements.iterator();
        while (iterator.hasNext()) {
            StructuredStatement structuredStatement;
            StructuredAssignment assignment;
            Op04StructuredStatement statement;
            if (structuredStatement = (statement = iterator.next()).getStatement() instanceof StructuredComment) continue;
            if (!(structuredStatement instanceof StructuredAssignment)) return;
            if (this.liftStatic(assignment = (StructuredAssignment)structuredStatement, classFileFields)) continue;
            return;
        }
    }

    private boolean liftStatic(StructuredAssignment assignment, LinkedList<ClassFileField> classFileFields) {
        ClassFileField field;
        LValue lValue = assignment.getLvalue();
        if (!(lValue instanceof StaticVariable)) {
            return false;
        }
        StaticVariable fieldVariable = (StaticVariable)lValue;
        try {
            field = this.classFile.getFieldByName(fieldVariable.getVarName());
        }
        catch (NoSuchFieldException e) {
            return false;
        }
        if (classFileFields.isEmpty()) {
            return false;
        }
        if (field != classFileFields.getFirst()) {
            return false;
        }
        classFileFields.removeFirst();
        if (field.getInitialValue() != null) {
            return false;
        }
        field.setInitialValue(assignment.getRvalue());
        assignment.getContainer().nopOut();
        return true;
    }

}

