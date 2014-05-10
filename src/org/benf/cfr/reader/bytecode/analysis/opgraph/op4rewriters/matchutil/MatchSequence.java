/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class MatchSequence
implements Matcher<StructuredStatement> {
    private final Matcher<StructuredStatement>[] inner;
    private final String name;

    public /* varargs */ MatchSequence(Matcher<StructuredStatement> ... inner) {
        this.inner = inner;
        this.name = "";
    }

    public /* varargs */ MatchSequence(String name, Matcher<StructuredStatement> ... inner) {
        this.inner = inner;
        this.name = name;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        MatchIterator<StructuredStatement> mi = matchIterator.copy();
        for (Matcher<StructuredStatement> matcher : this.inner) {
            if (matcher.match(mi, matchResultCollector)) continue;
            return false;
        }
        matchIterator.advanceTo(mi);
        return true;
    }
}

