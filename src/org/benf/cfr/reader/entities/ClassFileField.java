/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities;

import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.Literal;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.entities.Field;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class ClassFileField {
    private final Field field;
    private Expression initialValue;
    private boolean isHidden;
    private boolean isSyntheticOuterRef;
    private String overriddenName;

    public ClassFileField(Field field) {
        this.field = field;
        TypedLiteral constantValue = field.getConstantValue();
        this.initialValue = constantValue == null ? null : new Literal(constantValue);
        this.isHidden = false;
        this.isSyntheticOuterRef = false;
    }

    public Field getField() {
        return this.field;
    }

    public Expression getInitialValue() {
        return this.initialValue;
    }

    public void setInitialValue(Expression rValue) {
        this.initialValue = rValue;
    }

    public boolean shouldNotDisplay() {
        return this.isHidden || this.isSyntheticOuterRef;
    }

    public boolean isSyntheticOuterRef() {
        return this.isSyntheticOuterRef;
    }

    public void markHidden() {
        this.isHidden = true;
    }

    public void markSyntheticOuterRef() {
        this.isSyntheticOuterRef = true;
    }

    public void overrideName(String override) {
        this.overriddenName = override;
    }

    public String getFieldName() {
        if (this.overriddenName == null) return this.field.getFieldName();
        return this.overriddenName;
    }

    public void dump(Dumper d) {
        this.field.dump(d, this.getFieldName());
        if (this.initialValue != null) {
            d.print(" = ").dump(this.initialValue);
        }
        d.endCodeln();
    }
}

