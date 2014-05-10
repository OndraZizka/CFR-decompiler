/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils;

import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.SSAIdent;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.functors.UnaryFunction;

public class SSAIdentifierFactory<KEYTYPE> {
    private final Map<KEYTYPE, Integer> nextIdentFor;

    public SSAIdentifierFactory() {
        this.nextIdentFor = MapFactory.newLazyMap(MapFactory.newLinkedMap(), new UnaryFunction<KEYTYPE, Integer>(){

            @Override
            public Integer invoke(KEYTYPE ignore) {
                return 0;
            }
        });
    }

    public SSAIdent getIdent(KEYTYPE lValue) {
        int val = this.nextIdentFor.get(lValue);
        this.nextIdentFor.put(lValue, val + 1);
        return new SSAIdent(val);
    }

}

