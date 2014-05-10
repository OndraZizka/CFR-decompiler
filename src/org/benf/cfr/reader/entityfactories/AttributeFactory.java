/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entityfactories;

import org.benf.cfr.reader.entities.attributes.Attribute;
import org.benf.cfr.reader.entities.attributes.AttributeAnnotationDefault;
import org.benf.cfr.reader.entities.attributes.AttributeBootstrapMethods;
import org.benf.cfr.reader.entities.attributes.AttributeCode;
import org.benf.cfr.reader.entities.attributes.AttributeConstantValue;
import org.benf.cfr.reader.entities.attributes.AttributeDeprecated;
import org.benf.cfr.reader.entities.attributes.AttributeExceptions;
import org.benf.cfr.reader.entities.attributes.AttributeInnerClasses;
import org.benf.cfr.reader.entities.attributes.AttributeLineNumberTable;
import org.benf.cfr.reader.entities.attributes.AttributeLocalVariableTable;
import org.benf.cfr.reader.entities.attributes.AttributeLocalVariableTypeTable;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeInvisibleAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeInvisibleParameterAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeVisibleAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeRuntimeVisibleParameterAnnotations;
import org.benf.cfr.reader.entities.attributes.AttributeSignature;
import org.benf.cfr.reader.entities.attributes.AttributeSourceFile;
import org.benf.cfr.reader.entities.attributes.AttributeStackMapTable;
import org.benf.cfr.reader.entities.attributes.AttributeSynthetic;
import org.benf.cfr.reader.entities.attributes.AttributeUnknown;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class AttributeFactory {
    private static final long OFFSET_OF_ATTRIBUTE_NAME_INDEX = 0;

    public static Attribute build(ByteData raw, ConstantPool cp) {
        short nameIndex = raw.getS2At(0);
        ConstantPoolEntryUTF8 name = (ConstantPoolEntryUTF8)cp.getEntry(nameIndex);
        String attributeName = name.getValue();
        if ("Code".equals(attributeName)) {
            return new AttributeCode(raw, cp);
        }
        if ("LocalVariableTable".equals(attributeName)) {
            return new AttributeLocalVariableTable(raw, cp);
        }
        if ("Signature".equals(attributeName)) {
            return new AttributeSignature(raw, cp);
        }
        if ("ConstantValue".equals(attributeName)) {
            return new AttributeConstantValue(raw, cp);
        }
        if ("LineNumberTable".equals(attributeName)) {
            return new AttributeLineNumberTable(raw, cp);
        }
        if ("Exceptions".equals(attributeName)) {
            return new AttributeExceptions(raw, cp);
        }
        if ("Deprecated".equals(attributeName)) {
            return new AttributeDeprecated(raw, cp);
        }
        if ("RuntimeVisibleAnnotations".equals(attributeName)) {
            return new AttributeRuntimeVisibleAnnotations(raw, cp);
        }
        if ("RuntimeInvisibleAnnotations".equals(attributeName)) {
            return new AttributeRuntimeInvisibleAnnotations(raw, cp);
        }
        if ("RuntimeVisibleParameterAnnotations".equals(attributeName)) {
            return new AttributeRuntimeVisibleParameterAnnotations(raw, cp);
        }
        if ("RuntimeInvisibleParameterAnnotations".equals(attributeName)) {
            return new AttributeRuntimeInvisibleParameterAnnotations(raw, cp);
        }
        if ("SourceFile".equals(attributeName)) {
            return new AttributeSourceFile(raw, cp);
        }
        if ("InnerClasses".equals(attributeName)) {
            return new AttributeInnerClasses(raw, cp);
        }
        if ("BootstrapMethods".equals(attributeName)) {
            return new AttributeBootstrapMethods(raw, cp);
        }
        if ("AnnotationDefault".equals(attributeName)) {
            return new AttributeAnnotationDefault(raw, cp);
        }
        if ("LocalVariableTypeTable".equals(attributeName)) {
            return new AttributeLocalVariableTypeTable(raw, cp);
        }
        if ("StackMapTable".equals(attributeName)) {
            return new AttributeStackMapTable(raw, cp);
        }
        if (!"Synthetic".equals(attributeName)) return new AttributeUnknown(raw, attributeName);
        return new AttributeSynthetic(raw, cp);
    }

    public static UnaryFunction<ByteData, Attribute> getBuilder(ConstantPool cp) {
        return new AttributeBuilder(cp);
    }

    static class AttributeBuilder
    implements UnaryFunction<ByteData, Attribute> {
        private final ConstantPool cp;

        public AttributeBuilder(ConstantPool cp) {
            this.cp = cp;
        }

        @Override
        public Attribute invoke(ByteData arg) {
            return AttributeFactory.build(arg, this.cp);
        }
    }

}

