/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.parse.literal.TypedLiteral;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.attributes.AttributeConstantValue;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeInvisibleAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeVisibleAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeSignature;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolUtils;
import org.benf.cfr.reader.entityfactories.AttributeFactory;
import org.benf.cfr.reader.entityfactories.ContiguousEntityFactory;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.CollectionUtils;
import org.benf.cfr.reader.util.KnowsRawSize;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.output.Dumper;

public class Field
implements KnowsRawSize,
TypeUsageCollectable {
    private static final long OFFSET_OF_ACCESS_FLAGS = 0;
    private static final long OFFSET_OF_NAME_INDEX = 2;
    private static final long OFFSET_OF_DESCRIPTOR_INDEX = 4;
    private static final long OFFSET_OF_ATTRIBUTES_COUNT = 6;
    private static final long OFFSET_OF_ATTRIBUTES = 8;
    private final ConstantPool cp;
    private final long length;
    private final short descriptorIndex;
    private final Set<AccessFlag> accessFlags;
    private final Map<String, Attribute> attributes;
    private final TypedLiteral constantValue;
    private final String fieldName;
    private transient JavaTypeInstance cachedDecodedType;

    public Field(ByteData raw, ConstantPool cp) {
        this.cp = cp;
        this.accessFlags = AccessFlag.build(raw.getS2At(0));
        short attributes_count = raw.getS2At(6);
        ArrayList tmpAttributes = new ArrayList();
        tmpAttributes.ensureCapacity(attributes_count);
        long attributesLength = ContiguousEntityFactory.build(raw.getOffsetData(8), attributes_count, tmpAttributes, new UnaryFunction<ByteData, Attribute>(){

            @Override
            public Attribute invoke(ByteData arg) {
                return AttributeFactory.build(arg, cp);
            }
        });
        this.attributes = ContiguousEntityFactory.addToMap(new HashMap(), tmpAttributes);
        AccessFlag.applyAttributes(this.attributes, this.accessFlags);
        this.descriptorIndex = raw.getS2At(4);
        short nameIndex = raw.getS2At(2);
        this.length = 8 + attributesLength;
        Attribute cvAttribute = this.attributes.get("ConstantValue");
        this.constantValue = cvAttribute == null ? null : TypedLiteral.getConstantPoolEntry(cp, ((AttributeConstantValue)cvAttribute).getValue());
        this.fieldName = cp.getUTF8Entry(nameIndex).getValue();
    }

    @Override
    public long getRawByteLength() {
        return this.length;
    }

    private AttributeSignature getSignatureAttribute() {
        Attribute attribute = this.attributes.get("Signature");
        if (attribute != null) return (AttributeSignature)attribute;
        return null;
    }

    public JavaTypeInstance getJavaTypeInstance() {
        AttributeSignature sig;
        if (this.cachedDecodedType != null) return this.cachedDecodedType;
        ConstantPoolEntryUTF8 signature = (sig = this.getSignatureAttribute()) == null ? null : sig.getSignature();
        ConstantPoolEntryUTF8 descriptor = this.cp.getUTF8Entry(this.descriptorIndex);
        ConstantPoolEntryUTF8 prototype = null;
        prototype = signature == null ? descriptor : signature;
        this.cachedDecodedType = ConstantPoolUtils.decodeTypeTok(prototype.getValue(), this.cp);
        return this.cachedDecodedType;
    }

    public String getFieldName() {
        return this.fieldName;
    }

    public boolean testAccessFlag(AccessFlag accessFlag) {
        return this.accessFlags.contains((Object)accessFlag);
    }

    public TypedLiteral getConstantValue() {
        return this.constantValue;
    }

    private <T extends Attribute> T getAttributeByName(String name) {
        Attribute attribute = this.attributes.get(name);
        if (attribute == null) {
            return null;
        }
        Attribute tmp = attribute;
        return tmp;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        collector.collect(this.getJavaTypeInstance());
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeVisibleAnnotations"));
        collector.collectFrom((TypeUsageCollectable)this.getAttributeByName("RuntimeInvisibleAnnotations"));
    }

    public void dump(Dumper d, String name) {
        String prefix;
        AttributeRuntimeVisibleAnnotations runtimeVisibleAnnotations = (AttributeRuntimeVisibleAnnotations)this.getAttributeByName("RuntimeVisibleAnnotations");
        AttributeRuntimeInvisibleAnnotations runtimeInvisibleAnnotations = (AttributeRuntimeInvisibleAnnotations)this.getAttributeByName("RuntimeInvisibleAnnotations");
        if (runtimeVisibleAnnotations != null) {
            runtimeVisibleAnnotations.dump(d);
        }
        if (runtimeInvisibleAnnotations != null) {
            runtimeInvisibleAnnotations.dump(d);
        }
        if (!(prefix = CollectionUtils.join(this.accessFlags, " ")).isEmpty()) {
            d.print(prefix).print(' ');
        }
        JavaTypeInstance type = this.getJavaTypeInstance();
        d.dump(type).print(' ').print(name);
    }

}

