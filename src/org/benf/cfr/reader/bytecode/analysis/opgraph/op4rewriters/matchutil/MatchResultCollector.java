/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public interface MatchResultCollector {
    public void clear();

    public void collectStatement(String var1, StructuredStatement var2);

    public void collectStatementRange(String var1, MatchIterator<StructuredStatement> var2, MatchIterator<StructuredStatement> var3);

    public void collectMatches(String var1, WildcardMatch var2);
}

