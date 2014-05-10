/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters;

import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.Op04Rewriter;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.AbstractMatchResultIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.CollectMatch;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchIterator;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchResultCollector;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.Matcher;
import org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.util.MiscStatementTools;
import org.benf.cfr.reader.bytecode.analysis.parse.Expression;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.LValueExpression;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.FieldVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.parse.wildcard.WildcardMatch;
import org.benf.cfr.reader.bytecode.analysis.structured.StructuredStatement;
import org.benf.cfr.reader.bytecode.analysis.structured.statement.StructuredAssignment;
import org.benf.cfr.reader.entities.AccessFlag;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.ClassFileField;
import org.benf.cfr.reader.entities.Field;

public class InnerClassConstructorRewriter
implements Op04Rewriter {
    private final ClassFile classFile;
    private final LocalVariable outerArg;
    private FieldVariable matchedField;
    private StructuredStatement assignmentStatement;

    public InnerClassConstructorRewriter(ClassFile classFile, LocalVariable outerArg) {
        this.outerArg = outerArg;
        this.classFile = classFile;
    }

    @Override
    public void rewrite(Op04StructuredStatement root) {
        List<StructuredStatement> structuredStatements = MiscStatementTools.linearise(root);
        if (root == null) {
            return;
        }
        WildcardMatch wcm1 = new WildcardMatch();
        CollectMatch m = new CollectMatch("ass1", new StructuredAssignment(wcm1.getLValueWildCard("outercopy"), new LValueExpression(this.outerArg)));
        MatchIterator<StructuredStatement> mi = new MatchIterator<StructuredStatement>(structuredStatements);
        ConstructResultCollector collector = new ConstructResultCollector(wcm1, null);
        while (mi.hasNext()) {
            LValue lValue;
            mi.advance();
            if (!m.match(mi, collector)) continue;
            if (!(lValue = wcm1.getLValueWildCard("outercopy").getMatch() instanceof FieldVariable)) return;
            try {
                ClassFileField classField = this.classFile.getFieldByName(((FieldVariable)lValue).getFieldName());
                Field field = classField.getField();
                if (!field.testAccessFlag(AccessFlag.ACC_SYNTHETIC) || !field.testAccessFlag(AccessFlag.ACC_FINAL)) return;
                this.assignmentStatement = collector.assignmentStatement;
                this.matchedField = (FieldVariable)lValue;
            }
            catch (NoSuchFieldException e) {
                // empty catch block
            }
            return;
        }
    }

    public FieldVariable getMatchedField() {
        return this.matchedField;
    }

    public StructuredStatement getAssignmentStatement() {
        return this.assignmentStatement;
    }

    class 1 {
    }

    static class ConstructResultCollector
    extends AbstractMatchResultIterator {
        private final WildcardMatch wcm;
        private StructuredStatement assignmentStatement;

        private ConstructResultCollector(WildcardMatch wcm) {
            this.wcm = wcm;
        }

        @Override
        public void clear() {
            this.assignmentStatement = null;
        }

        @Override
        public void collectStatement(String name, StructuredStatement statement) {
            this.assignmentStatement = statement;
        }

        @Override
        public void collectMatches(String name, WildcardMatch wcm) {
        }

        /* synthetic */ ConstructResultCollector(WildcardMatch x0, 1 x1) {
            this(x0);
        }
    }

}

