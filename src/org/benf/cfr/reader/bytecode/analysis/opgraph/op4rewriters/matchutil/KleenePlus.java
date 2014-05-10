/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil;

import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleeneN;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;

public class KleenePlus
extends KleeneN {
    public KleenePlus(Matcher<StructuredStatement> inner) {
        super(1, inner);
    }

    public /* varargs */ KleenePlus(Matcher<StructuredStatement> ... matchers) {
        super(1, matchers);
    }
}

