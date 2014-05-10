/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.benf.cfr.reader.bytecode.analysis.variables.Ident;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariableFromHint;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamerDefault;
import org.benf.cfr.reader.entities.attributes.LocalVariableEntry;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class VariableNamerHinted
implements VariableNamer {
    private int genIdx = 0;
    private final VariableNamer missingNamer = new VariableNamerDefault();
    private final OrderLocalVariables orderLocalVariable = new OrderLocalVariables(null);
    private final Map<Short, TreeSet<LocalVariableEntry>> localVariableEntryTreeSet;
    private final Map<LocalVariableEntry, NamedVariable> cache;
    private final ConstantPool cp;

    public VariableNamerHinted(List<LocalVariableEntry> entryList, ConstantPool cp) {
        this.localVariableEntryTreeSet = MapFactory.newLazyMap(new UnaryFunction<Short, TreeSet<LocalVariableEntry>>(){

            @Override
            public TreeSet<LocalVariableEntry> invoke(Short arg) {
                return new TreeSet(VariableNamerHinted.this.orderLocalVariable);
            }
        });
        this.cache = MapFactory.newMap();
        for (LocalVariableEntry e : entryList) {
            this.localVariableEntryTreeSet.get(e.getIndex()).add(e);
        }
        this.cp = cp;
    }

    @Override
    public NamedVariable getName(int originalRawOffset, Ident ident, long stackPosition) {
        NamedVariable namedVariable;
        LocalVariableEntry key;
        LocalVariableEntry lve;
        LocalVariableEntry tmp;
        originalRawOffset+=2;
        short sstackPos = (short)stackPosition;
        if (!this.localVariableEntryTreeSet.containsKey(sstackPos)) {
            return this.missingNamer.getName(0, ident, sstackPos);
        }
        if ((lve = this.localVariableEntryTreeSet.get(sstackPos).floor(tmp = new LocalVariableEntry((short)originalRawOffset, 1, -1, -1, (short)stackPosition))) == null) {
            return this.missingNamer.getName(0, ident, sstackPos);
        }
        if ((namedVariable = this.cache.get(key = lve)) != null) return namedVariable;
        namedVariable = new NamedVariableFromHint(this.cp.getUTF8Entry(lve.getNameIndex()).getValue(), lve.getIndex(), this.genIdx);
        this.cache.put(key, namedVariable);
        return namedVariable;
    }

    @Override
    public List<NamedVariable> getNamedVariables() {
        return ListFactory.newList(this.cache.values());
    }

    @Override
    public void forceName(Ident ident, long stackPosition, String name) {
    }

    @Override
    public void mutatingRenameUnClash(NamedVariable toRename) {
        Map namedVariableMap = MapFactory.newMap();
        for (NamedVariable var2 : this.cache.values()) {
            namedVariableMap.put((String)var2.getStringName(), (NamedVariable)var2);
        }
        for (NamedVariable var2 : this.missingNamer.getNamedVariables()) {
            namedVariableMap.put((String)var2.getStringName(), (NamedVariable)var2);
        }
        String name = toRename.getStringName();
        Pattern p = Pattern.compile("^(.*[^\\d]+)([\\d]+)$");
        Matcher m = p.matcher((CharSequence)name);
        int start = 2;
        String prefix = name;
        if (m.matches()) {
            prefix = m.group(0);
            start = Integer.parseInt(m.group(1));
            ++start;
        }
        do {
            String name2;
            if (!namedVariableMap.containsKey(name2 = prefix + start)) {
                toRename.forceName(name2);
                return;
            }
            ++start;
        } while (true);
    }

    static class OrderLocalVariables
    implements Comparator<LocalVariableEntry> {
        private OrderLocalVariables() {
        }

        @Override
        public int compare(LocalVariableEntry a, LocalVariableEntry b) {
            int x = a.getIndex() - b.getIndex();
            if (x == 0) return a.getStartPc() - b.getStartPc();
            return x;
        }

        /* synthetic */ OrderLocalVariables( x0) {
            this();
        }
    }

}

