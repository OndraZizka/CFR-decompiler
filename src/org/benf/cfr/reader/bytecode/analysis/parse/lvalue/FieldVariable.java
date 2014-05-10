/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.lvalue;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueUsageCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryFieldRef;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public class FieldVariable
extends AbstractLValue {
    private Expression object;
    private final ClassFile classFile;
    private final ClassFileField classFileField;
    private final String failureName;

    public FieldVariable(Expression object, ClassFile classFile, ConstantPoolEntry field) {
        super(FieldVariable.getFieldType((ConstantPoolEntryFieldRef)field));
        this.classFile = classFile;
        this.object = object;
        ConstantPoolEntryFieldRef fieldRef = (ConstantPoolEntryFieldRef)field;
        this.classFileField = FieldVariable.getField(fieldRef);
        this.failureName = fieldRef.getLocalName();
    }

    private FieldVariable(InferredJavaType type, Expression object, ClassFile classFile, ClassFileField classFileField, String failureName) {
        super(type);
        this.object = object;
        this.classFile = classFile;
        this.classFileField = classFileField;
        this.failureName = failureName;
    }

    @Override
    public LValue deepClone(CloneHelper cloneHelper) {
        return new FieldVariable(this.getInferredJavaType(), cloneHelper.replaceOrClone(this.object), this.classFile, this.classFileField, this.failureName);
    }

    static ClassFileField getField(ConstantPoolEntryFieldRef fieldRef) {
        String name = fieldRef.getLocalName();
        JavaRefTypeInstance ref = (JavaRefTypeInstance)fieldRef.getClassEntry().getTypeInstance();
        try {
            ClassFile classFile = ref.getClassFile();
            if (classFile == null) {
                return null;
            }
            ClassFileField field = classFile.getFieldByName(name);
            return field;
        }
        catch (NoSuchFieldException ignore) {
        }
        catch (CannotLoadClassException ignore) {
            // empty catch block
        }
        return null;
    }

    static InferredJavaType getFieldType(ConstantPoolEntryFieldRef fieldRef) {
        String name = fieldRef.getLocalName();
        JavaRefTypeInstance ref = (JavaRefTypeInstance)fieldRef.getClassEntry().getTypeInstance();
        try {
            ClassFile classFile = ref.getClassFile();
            if (classFile == null) return new InferredJavaType(fieldRef.getJavaTypeInstance(), InferredJavaType.Source.FIELD);
            Field field = classFile.getFieldByName(name).getField();
            return new InferredJavaType(field.getJavaTypeInstance(), InferredJavaType.Source.FIELD);
        }
        catch (CannotLoadClassException e) {
        }
        catch (NoSuchFieldException ignore) {
            // empty catch block
        }
        return new InferredJavaType(fieldRef.getJavaTypeInstance(), InferredJavaType.Source.FIELD);
    }

    public ClassFileField getClassFileField() {
        return this.classFileField;
    }

    @Override
    public int getNumberOfCreators() {
        throw new ConfusedCFRException("NYI");
    }

    public JavaTypeInstance getOwningClassType() {
        return this.classFile.getClassType();
    }

    public boolean isOuterRef() {
        return this.classFileField != null && this.classFileField.isSyntheticOuterRef();
    }

    public String getFieldName() {
        if (this.classFileField != null) return this.classFileField.getFieldName();
        return this.failureName;
    }

    public Expression getObject() {
        return this.object;
    }

    private boolean objectIsThis() {
        LValue lValue;
        if (!(this.object instanceof LValueExpression) || !(lValue = ((LValueExpression)this.object).getLValue() instanceof LocalVariable)) return false;
        return ((LocalVariable)lValue).getName().getStringName().equals("this");
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.PAREN_SUB_MEMBER;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        if (this.isOuterRef() && this.objectIsThis()) {
            return d.print(this.getFieldName());
        }
        this.object.dumpWithOuterPrecedence(d, this.getPrecedence());
        return d.print(".").print(this.getFieldName());
    }

    @Override
    public SSAIdentifiers<LValue> collectVariableMutation(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        return new SSAIdentifiers<LValue>(this, ssaIdentifierFactory);
    }

    public void collectLValueAssignments(Expression assignedTo, StatementContainer statementContainer, LValueAssignmentCollector lValueAssigmentCollector) {
    }

    @Override
    public void collectLValueUsage(LValueUsageCollector lValueUsageCollector) {
        this.object.collectUsedLValues(lValueUsageCollector);
    }

    @Override
    public LValue replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        this.object = this.object.replaceSingleUsageLValues(lValueRewriter, ssaIdentifiers, statementContainer);
        return this;
    }

    @Override
    public LValue applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        this.object = expressionRewriter.rewriteExpression(this.object, ssaIdentifiers, statementContainer, flags);
        return this;
    }

    public void rewriteLeftNestedSyntheticOuterRefs() {
        LValue lValueLhs;
        FieldVariable lhs;
        while (this.isOuterRef() && this.object instanceof LValueExpression && lValueLhs = ((LValueExpression)this.object).getLValue() instanceof FieldVariable && (lhs = (FieldVariable)lValueLhs).isOuterRef()) {
            this.object = lhs.object;
        }
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (!(o instanceof FieldVariable)) {
            return false;
        }
        FieldVariable other = (FieldVariable)o;
        if (!this.object.equals(other.object)) {
            return false;
        }
        if (this.getFieldName().equals(other.getFieldName())) return true;
        return false;
    }
}

