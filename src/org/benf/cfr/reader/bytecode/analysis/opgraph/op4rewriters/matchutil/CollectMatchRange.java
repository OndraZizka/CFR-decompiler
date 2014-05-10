/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class CollectMatchRange
implements Matcher<StructuredStatement> {
    private final Matcher<StructuredStatement> inner;
    private final String name;

    public CollectMatchRange(String name, Matcher<StructuredStatement> inner) {
        this.inner = inner;
        this.name = name;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        MatchIterator<StructuredStatement> orig = matchIterator.copy();
        boolean res = this.inner.match(matchIterator, matchResultCollector);
        if (!res) return res;
        MatchIterator<StructuredStatement> end = matchIterator.copy();
        matchResultCollector.collectStatementRange(this.name, orig, end);
        return res;
    }
}

