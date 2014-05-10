/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamerDefault;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamerHinted;
import org.benf.cfr.reader.entities.attributes.AttributeLocalVariableTable;
import org.benf.cfr.reader.entities.attributes.LocalVariableEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;

public class VariableNamerFactory {
    public static VariableNamer getNamer(AttributeLocalVariableTable source, ConstantPool cp) {
        if (source != null) return new VariableNamerHinted(source.getLocalVariableEntryList(), cp);
        return new VariableNamerDefault();
    }
}

