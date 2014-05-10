/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Graph;
import org.benf.cfr.reader.bytecode.analysis.opgraph.MutableGraph;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.MapFactory;

public class GraphConversionHelper<X extends Graph<X>, Y extends MutableGraph<Y>> {
    private final Map<X, Y> correspondance = MapFactory.newMap();

    private Y findEntry(X key, X orig, String dbg) {
        MutableGraph value = (MutableGraph)this.correspondance.get(key);
        if (value != null) return value;
        throw new ConfusedCFRException("Missing key when tying up graph " + key + ", was " + dbg + " of " + orig);
    }

    public void patchUpRelations() {
        for (Map.Entry<X, Y> entry : this.correspondance.entrySet()) {
            Graph orig = (Graph)entry.getKey();
            MutableGraph newnode = (MutableGraph)entry.getValue();
            for (Graph source : orig.getSources()) {
                newnode.addSource(this.findEntry(source, orig, "source"));
            }
            for (Graph target : orig.getTargets()) {
                newnode.addTarget(this.findEntry(target, orig, "target"));
            }
        }
    }

    public void registerOriginalAndNew(X original, Y newnode) {
        this.correspondance.put(original, newnode);
    }
}

