/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.lvalue;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.StatementContainer;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.AbstractLValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.FieldVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.CloneHelper;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriterFlags;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueAssignmentCollector;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.LValueRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifierFactory;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdentifiers;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryClass;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryFieldRef;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryNameAndType;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.output.Dumper;

public class StaticVariable
extends AbstractLValue {
    private final ConstantPoolEntryFieldRef field;
    private final JavaTypeInstance clazz;
    private final String varName;

    public StaticVariable(ClassFile classFile, ConstantPool cp, ConstantPoolEntry field) {
        super(FieldVariable.getFieldType((ConstantPoolEntryFieldRef)field));
        this.field = (ConstantPoolEntryFieldRef)field;
        this.clazz = this.field.getClassEntry().getTypeInstance();
        this.varName = this.field.getLocalName();
    }

    public StaticVariable(InferredJavaType type, JavaTypeInstance clazz, String varName) {
        super(type);
        this.field = null;
        this.varName = varName;
        this.clazz = clazz;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.clazz);
        super.collectTypeUsages(collector);
    }

    @Override
    public int getNumberOfCreators() {
        throw new ConfusedCFRException("NYI");
    }

    public JavaTypeInstance getOwningClassTypeInstance() {
        return this.clazz;
    }

    public ConstantPoolEntryNameAndType getNameAndTypeEntry() {
        return this.field.getNameAndTypeEntry();
    }

    public ConstantPoolEntryClass getClassEntry() {
        return this.field.getClassEntry();
    }

    public String getVarName() {
        return this.varName;
    }

    @Override
    public Precedence getPrecedence() {
        return Precedence.HIGHEST;
    }

    @Override
    public Dumper dumpInner(Dumper d) {
        return d.dump(this.clazz).print(".").print(this.varName);
    }

    @Override
    public LValue deepClone(CloneHelper cloneHelper) {
        return this;
    }

    public void collectLValueAssignments(Expression assignedTo, StatementContainer statementContainer, LValueAssignmentCollector lValueAssigmentCollector) {
    }

    @Override
    public LValue replaceSingleUsageLValues(LValueRewriter lValueRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer) {
        return this;
    }

    @Override
    public LValue applyExpressionRewriter(ExpressionRewriter expressionRewriter, SSAIdentifiers ssaIdentifiers, StatementContainer statementContainer, ExpressionRewriterFlags flags) {
        return this;
    }

    @Override
    public SSAIdentifiers<LValue> collectVariableMutation(SSAIdentifierFactory<LValue> ssaIdentifierFactory) {
        return new SSAIdentifiers<LValue>(this, ssaIdentifierFactory);
    }

    public boolean equals(Object o) {
        if (!(o instanceof StaticVariable)) {
            return false;
        }
        StaticVariable other = (StaticVariable)o;
        if (other.clazz.equals(this.clazz)) return other.varName.equals(this.varName);
        return false;
    }

    public int hashCode() {
        int hashcode = this.clazz.hashCode();
        hashcode = 13 * hashcode + this.varName.hashCode();
        return hashcode;
    }
}

