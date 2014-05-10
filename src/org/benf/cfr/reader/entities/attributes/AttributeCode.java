/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import java.util.ArrayList;
import java.util.List;
import org.benf.cfr.reader.bytecode.CodeAnalyser;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.attributes.AttributeLocalVariableTable;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.exceptions.ExceptionTableEntry;
import org.benf.cfr.reader.entityfactories.AttributeFactory;
import org.benf.cfr.reader.entityfactories.ContiguousEntityFactory;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.output.Dumper;

public class AttributeCode
extends Attribute {
    public static final String ATTRIBUTE_NAME = "Code";
    private static final long OFFSET_OF_ATTRIBUTE_LENGTH = 2;
    private static final long OFFSET_OF_MAX_STACK = 6;
    private static final long OFFSET_OF_MAX_LOCALS = 8;
    private static final long OFFSET_OF_CODE_LENGTH = 10;
    private static final long OFFSET_OF_CODE = 14;
    private final int length;
    private final short maxStack;
    private final short maxLocals;
    private final int codeLength;
    private final List<ExceptionTableEntry> exceptionTableEntries;
    private final List<Attribute> attributes;
    private final ConstantPool cp;
    private final ByteData rawData;
    private final CodeAnalyser codeAnalyser;

    public AttributeCode(ByteData raw, ConstantPool cp) {
        this.cp = cp;
        this.length = raw.getS4At(2);
        this.maxStack = raw.getS2At(6);
        this.maxLocals = raw.getS2At(8);
        this.codeLength = raw.getS4At(10);
        long OFFSET_OF_EXCEPTION_TABLE_LENGTH = 14 + (long)this.codeLength;
        long OFFSET_OF_EXCEPTION_TABLE = OFFSET_OF_EXCEPTION_TABLE_LENGTH + 2;
        ArrayList<ExceptionTableEntry> etis = new ArrayList<ExceptionTableEntry>();
        short numExceptions = raw.getS2At(OFFSET_OF_EXCEPTION_TABLE_LENGTH);
        etis.ensureCapacity(numExceptions);
        long numBytesExceptionInfo = ContiguousEntityFactory.buildSized(raw.getOffsetData(OFFSET_OF_EXCEPTION_TABLE), numExceptions, 8, etis, ExceptionTableEntry.getBuilder(cp));
        this.exceptionTableEntries = etis;
        long OFFSET_OF_ATTRIBUTES_COUNT = OFFSET_OF_EXCEPTION_TABLE + numBytesExceptionInfo;
        long OFFSET_OF_ATTRIBUTES = OFFSET_OF_ATTRIBUTES_COUNT + 2;
        short numAttributes = raw.getS2At(OFFSET_OF_ATTRIBUTES_COUNT);
        ArrayList<Attribute> tmpAttributes = new ArrayList<Attribute>();
        tmpAttributes.ensureCapacity(numAttributes);
        ContiguousEntityFactory.build(raw.getOffsetData(OFFSET_OF_ATTRIBUTES), numAttributes, tmpAttributes, AttributeFactory.getBuilder(cp));
        this.attributes = tmpAttributes;
        this.rawData = raw.getOffsetData(14);
        this.codeAnalyser = new CodeAnalyser(this);
    }

    public void setMethod(Method method) {
        this.codeAnalyser.setMethod(method);
    }

    public Op04StructuredStatement analyse() {
        return this.codeAnalyser.getAnalysis(this.getConstantPool().getDCCommonState());
    }

    public ConstantPool getConstantPool() {
        return this.cp;
    }

    public AttributeLocalVariableTable getLocalVariableTable() {
        for (Attribute attribute : this.attributes) {
            if (!(attribute instanceof AttributeLocalVariableTable)) continue;
            return (AttributeLocalVariableTable)attribute;
        }
        return null;
    }

    public ByteData getRawData() {
        return this.rawData;
    }

    public List<ExceptionTableEntry> getExceptionTableEntries() {
        return this.exceptionTableEntries;
    }

    public short getMaxStack() {
        return this.maxStack;
    }

    public short getMaxLocals() {
        return this.maxLocals;
    }

    public int getCodeLength() {
        return this.codeLength;
    }

    @Override
    public Dumper dump(Dumper d) {
        return this.codeAnalyser.getAnalysis(this.getConstantPool().getDCCommonState()).dump(d);
    }

    @Override
    public long getRawByteLength() {
        return 6 + (long)this.length;
    }

    @Override
    public String getRawName() {
        return "Code";
    }
}

