/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.expression;

import org.benf.cfr.reader.bytecode.analysis.parse.expression.misc.Precedence;
import org.benf.cfr.reader.bytecode.opcode.JVMInstr;
import org.benf.cfr.reader.util.ConfusedCFRException;

public enum ArithOp {
    LCMP("LCMP", true, Precedence.WEAKEST),
    DCMPL("DCMPL", true, Precedence.WEAKEST),
    DCMPG("DCMPG", true, Precedence.WEAKEST),
    FCMPL("FCMPL", true, Precedence.WEAKEST),
    FCMPG("FCMPG", true, Precedence.WEAKEST),
    PLUS("+", false, Precedence.ADD_SUB),
    MINUS("-", false, Precedence.ADD_SUB),
    MULTIPLY("*", false, Precedence.MUL_DIV_MOD),
    DIVIDE("/", false, Precedence.MUL_DIV_MOD),
    REM("%", false, Precedence.MUL_DIV_MOD),
    OR("|", false, Precedence.BIT_OR),
    AND("&", false, Precedence.BIT_AND),
    SHR(">>", false, Precedence.BITWISE_SHIFT),
    SHL("<<", false, Precedence.BITWISE_SHIFT),
    SHRU(">>>", false, Precedence.BITWISE_SHIFT),
    XOR("^", false, Precedence.BIT_XOR),
    NEG("~", false, Precedence.UNARY_OTHER);
    
    private final String showAs;
    private final boolean temporary;
    private final Precedence precedence;

    private ArithOp(String showAs, boolean temporary, Precedence precedence) {
        this.showAs = showAs;
        this.temporary = temporary;
        this.precedence = precedence;
    }

    public String getShowAs() {
        return this.showAs;
    }

    public boolean isTemporary() {
        return this.temporary;
    }

    public Precedence getPrecedence() {
        return this.precedence;
    }

    public static ArithOp getOpFor(JVMInstr instr) {
        switch (instr) {
            case LCMP: {
                return ArithOp.LCMP;
            }
            case DCMPG: {
                return ArithOp.DCMPG;
            }
            case DCMPL: {
                return ArithOp.DCMPL;
            }
            case FCMPG: {
                return ArithOp.FCMPG;
            }
            case FCMPL: {
                return ArithOp.FCMPL;
            }
            case ISUB: 
            case LSUB: 
            case FSUB: 
            case DSUB: {
                return ArithOp.MINUS;
            }
            case IMUL: 
            case LMUL: 
            case FMUL: 
            case DMUL: {
                return ArithOp.MULTIPLY;
            }
            case IADD: 
            case LADD: 
            case FADD: 
            case DADD: {
                return ArithOp.PLUS;
            }
            case LDIV: 
            case IDIV: 
            case FDIV: 
            case DDIV: {
                return ArithOp.DIVIDE;
            }
            case LOR: 
            case IOR: {
                return ArithOp.OR;
            }
            case LAND: 
            case IAND: {
                return ArithOp.AND;
            }
            case IREM: 
            case LREM: 
            case FREM: 
            case DREM: {
                return ArithOp.REM;
            }
            case ISHR: 
            case LSHR: {
                return ArithOp.SHR;
            }
            case IUSHR: 
            case LUSHR: {
                return ArithOp.SHRU;
            }
            case ISHL: 
            case LSHL: {
                return ArithOp.SHL;
            }
            case IXOR: 
            case LXOR: {
                return ArithOp.XOR;
            }
        }
        throw new ConfusedCFRException("Don't know arith op for " + (Object)instr);
    }

}

