/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.structured;

import java.util.List;
import java.util.Vector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.transformers.StructuredStatementTransformer;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.rewriters.ExpressionRewriter;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.BlockIdentifier;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.scope.LValueScopeDiscoverer;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredScope;
import org.benf.cfr.reader.util.Predicate;
import org.benf.cfr.reader.util.TypeUsageCollectable;
import org.benf.cfr.reader.util.output.Dumpable;

public interface StructuredStatement
extends Dumpable,
TypeUsageCollectable,
Matcher<StructuredStatement> {
    public Op04StructuredStatement getContainer();

    public void setContainer(Op04StructuredStatement var1);

    public StructuredStatement claimBlock(Op04StructuredStatement var1, BlockIdentifier var2, Vector<BlockIdentifier> var3);

    public StructuredStatement informBlockHeirachy(Vector<BlockIdentifier> var1);

    public void transformStructuredChildren(StructuredStatementTransformer var1, StructuredScope var2);

    public void rewriteExpressions(ExpressionRewriter var1);

    public boolean isProperlyStructured();

    public boolean isRecursivelyStructured();

    public void linearizeInto(List<StructuredStatement> var1);

    public void traceLocalVariableScope(LValueScopeDiscoverer var1);

    public void markCreator(LValue var1);

    public boolean alwaysDefines(LValue var1);

    public boolean inlineable();

    public Op04StructuredStatement getInline();

    public boolean isEffectivelyNOP();

    public boolean fallsNopToNext();

    public List<LValue> findCreatedHere();

    public String suggestName(LocalVariable var1, Predicate<String> var2);
}

