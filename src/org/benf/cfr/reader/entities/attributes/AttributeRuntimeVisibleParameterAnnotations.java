/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.attributes;

import org.benf.cfr.reader.entities.attributes.AttributeParameterAnnotations;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.util.bytestream.ByteData;

public class AttributeRuntimeVisibleParameterAnnotations
extends AttributeParameterAnnotations {
    public static final String ATTRIBUTE_NAME = "RuntimeVisibleParameterAnnotations";

    public AttributeRuntimeVisibleParameterAnnotations(ByteData raw, ConstantPool cp) {
        super(raw, cp);
    }

    @Override
    public String getRawName() {
        return "RuntimeVisibleParameterAnnotations";
    }
}

