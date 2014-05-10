/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.annotations;

import java.util.List;
import org.benf.cfr.reader.entities.annotations.ElementValue;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.output.CommaHelp;
import org.benf.cfr.reader.util.output.Dumper;

public class ElementValueArray
implements ElementValue {
    private final List<ElementValue> content;

    public ElementValueArray(List<ElementValue> content) {
        this.content = content;
    }

    @Override
    public Dumper dump(Dumper d) {
        d.print('{');
        boolean first = true;
        for (ElementValue value : this.content) {
            first = CommaHelp.comma(first, d);
            value.dump(d);
        }
        d.print('}');
        return d;
    }

    @Override
    public void collectTypeUsages(TypeUsageCollector collector) {
        for (ElementValue e : this.content) {
            e.collectTypeUsages(collector);
        }
    }
}

