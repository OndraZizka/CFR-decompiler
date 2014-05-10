/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class MatchOneOf
implements Matcher<StructuredStatement> {
    private final Matcher<StructuredStatement>[] matchers;

    public /* varargs */ MatchOneOf(Matcher<StructuredStatement> ... matchers) {
        this.matchers = matchers;
    }

    @Override
    public boolean match(MatchIterator<StructuredStatement> matchIterator, MatchResultCollector matchResultCollector) {
        for (Matcher<StructuredStatement> matcher : this.matchers) {
            MatchIterator<StructuredStatement> mi;
            if (!matcher.match(mi = matchIterator.copy(), matchResultCollector)) continue;
            matchIterator.advanceTo(mi);
            return true;
        }
        return false;
    }
}

