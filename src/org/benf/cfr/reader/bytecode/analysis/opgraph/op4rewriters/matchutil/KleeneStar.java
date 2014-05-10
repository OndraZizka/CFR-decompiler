/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleeneN;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class KleeneStar
extends KleeneN {
    public KleeneStar(Matcher<StructuredStatement> inner) {
        super(0, inner);
    }

    public /* varargs */ KleeneStar(Matcher<StructuredStatement> ... matchers) {
        super(0, matchers);
    }
}

