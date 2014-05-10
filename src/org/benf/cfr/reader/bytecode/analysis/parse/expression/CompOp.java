/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.util.ConfusedCFRException;

public enum CompOp {
    LT("<", Precedence.REL_CMP_INSTANCEOF),
    GT(">", Precedence.REL_CMP_INSTANCEOF),
    LTE("<=", Precedence.REL_CMP_INSTANCEOF),
    GTE(">=", Precedence.REL_CMP_INSTANCEOF),
    EQ("==", Precedence.REL_EQ),
    NE("!=", Precedence.REL_EQ);
    
    private final String showAs;
    private final Precedence precedence;

    private CompOp(String showAs, Precedence precedence) {
        this.showAs = showAs;
        this.precedence = precedence;
    }

    public String getShowAs() {
        return this.showAs;
    }

    public Precedence getPrecedence() {
        return this.precedence;
    }

    public CompOp getInverted() {
        switch (this) {
            case LT: {
                return CompOp.GTE;
            }
            case GT: {
                return CompOp.LTE;
            }
            case GTE: {
                return CompOp.LT;
            }
            case LTE: {
                return CompOp.GT;
            }
            case EQ: {
                return CompOp.NE;
            }
            case NE: {
                return CompOp.EQ;
            }
        }
        throw new ConfusedCFRException("Can't invert CompOp " + (Object)this);
    }

    public static CompOp getOpFor(JVMInstr instr) {
        switch (instr) {
            case IF_ICMPEQ: 
            case IF_ACMPEQ: {
                return CompOp.EQ;
            }
            case IF_ICMPLT: {
                return CompOp.LT;
            }
            case IF_ICMPGE: {
                return CompOp.GTE;
            }
            case IF_ICMPGT: {
                return CompOp.GT;
            }
            case IF_ICMPNE: 
            case IF_ACMPNE: {
                return CompOp.NE;
            }
            case IF_ICMPLE: {
                return CompOp.LTE;
            }
            case IFEQ: {
                return CompOp.EQ;
            }
            case IFNE: {
                return CompOp.NE;
            }
            case IFLE: {
                return CompOp.LTE;
            }
            case IFLT: {
                return CompOp.LT;
            }
            case IFGE: {
                return CompOp.GTE;
            }
            case IFGT: {
                return CompOp.GT;
            }
        }
        throw new ConfusedCFRException("Don't know comparison op for " + (Object)instr);
    }

}

