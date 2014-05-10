/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.opcode;

import java.util.HashMap;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op01WithProcessedDataAndByteJumps;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackSim;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.bytecode.opcode.OperationFactory;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryCPEntryW;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryConditionalJump;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDefault;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDup;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDup2;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDup2X1;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDup2X2;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDupX1;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryDupX2;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryFakeCatch;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryGetField;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryGetStatic;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryGoto;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryGotoW;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryInvoke;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryInvokeDynamic;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryInvokeInterface;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryLDC;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryLDC2W;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryLDCW;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryLookupSwitch;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryMultiANewArray;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryNew;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryPop;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryPop2;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryPutField;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryPutStatic;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryReturn;
import org.benf.cfr.reader.bytecode.opcode.OperationFactorySwap;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryTableSwitch;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryThrow;
import org.benf.cfr.reader.bytecode.opcode.OperationFactoryWide;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntry;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.bytestream.ByteData;

public enum JVMInstr {
    AALOAD(50, 0, new StackTypes(StackType.REF, StackType.INT), StackType.REF.asList(), RawJavaType.VOID),
    AASTORE(83, 0, new StackTypes(StackType.REF, StackType.INT, StackType.REF), StackTypes.EMPTY, RawJavaType.VOID),
    ACONST_NULL(1, 0, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.NULL, true),
    ALOAD(25, 1, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.VOID, true),
    ALOAD_WIDE(-1, 3, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.VOID, true),
    ALOAD_0(42, 0, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.REF, true),
    ALOAD_1(43, 0, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.REF, true),
    ALOAD_2(44, 0, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.REF, true),
    ALOAD_3(45, 0, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.REF, true),
    ANEWARRAY(189, 2, StackType.INT.asList(), StackType.REF.asList(), (RawJavaType)null, new OperationFactoryCPEntryW()),
    ARETURN(176, 0, StackType.REF.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn(), true),
    ARRAYLENGTH(190, 0, StackType.REF.asList(), StackType.INT.asList(), RawJavaType.INT),
    ASTORE(58, 1, StackType.RETURNADDRESSORREF.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ASTORE_WIDE(-1, 3, StackType.RETURNADDRESSORREF.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ASTORE_0(75, 0, StackType.RETURNADDRESSORREF.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ASTORE_1(76, 0, StackType.RETURNADDRESSORREF.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ASTORE_2(77, 0, StackType.RETURNADDRESSORREF.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ASTORE_3(78, 0, StackType.RETURNADDRESSORREF.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ATHROW(191, 0, StackType.REF.asList(), StackType.REF.asList(), RawJavaType.VOID, new OperationFactoryThrow()),
    BALOAD(51, 0, new StackTypes(StackType.REF, StackType.INT), StackType.INT.asList(), null),
    BASTORE(84, 0, new StackTypes(StackType.REF, StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID),
    BIPUSH(16, 1, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.VOID),
    CALOAD(52, 0, new StackTypes(StackType.REF, StackType.INT), StackType.INT.asList(), RawJavaType.CHAR),
    CASTORE(85, 0, new StackTypes(StackType.REF, StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID),
    CHECKCAST(192, 2, StackType.REF.asList(), StackType.REF.asList(), RawJavaType.REF, new OperationFactoryCPEntryW()),
    D2F(144, 0, StackType.DOUBLE.asList(), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    D2I(142, 0, StackType.DOUBLE.asList(), StackType.INT.asList(), RawJavaType.INT),
    D2L(143, 0, StackType.DOUBLE.asList(), StackType.LONG.asList(), RawJavaType.LONG),
    DADD(99, 0, new StackTypes(StackType.DOUBLE, StackType.DOUBLE), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    DALOAD(49, 0, new StackTypes(StackType.REF, StackType.INT), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    DASTORE(82, 0, new StackTypes(StackType.REF, StackType.INT, StackType.DOUBLE), StackTypes.EMPTY, RawJavaType.VOID),
    DCMPG(152, 0, new StackTypes(StackType.DOUBLE, StackType.DOUBLE), StackType.INT.asList(), RawJavaType.INT),
    DCMPL(151, 0, new StackTypes(StackType.DOUBLE, StackType.DOUBLE), StackType.INT.asList(), RawJavaType.INT),
    DCONST_0(14, 0, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DCONST_1(15, 0, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DDIV(111, 0, new StackTypes(StackType.DOUBLE, StackType.DOUBLE), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    DLOAD(24, 1, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DLOAD_WIDE(-1, 3, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DLOAD_0(38, 0, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DLOAD_1(39, 0, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DLOAD_2(40, 0, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DLOAD_3(41, 0, StackTypes.EMPTY, StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    DMUL(107, 0, new StackTypes(StackType.DOUBLE, StackType.DOUBLE), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    DNEG(119, 0, StackType.DOUBLE.asList(), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    DREM(115, 0, new StackTypes(StackType.DOUBLE, StackType.DOUBLE), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    DRETURN(175, 0, StackType.DOUBLE.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn()),
    DSTORE(57, 1, StackType.DOUBLE.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    DSTORE_WIDE(-1, 3, StackType.DOUBLE.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    DSTORE_0(71, 0, StackType.DOUBLE.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    DSTORE_1(72, 0, StackType.DOUBLE.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    DSTORE_2(73, 0, StackType.DOUBLE.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    DSTORE_3(74, 0, StackType.DOUBLE.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    DSUB(103, 0, new StackTypes(StackType.DOUBLE, StackType.DOUBLE), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    DUP(89, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryDup()),
    DUP_X1(90, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryDupX1()),
    DUP_X2(91, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryDupX2()),
    DUP2(92, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryDup2()),
    DUP2_X1(93, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryDup2X1()),
    DUP2_X2(94, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryDup2X2()),
    F2D(141, 0, StackType.FLOAT.asList(), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    F2I(139, 0, StackType.FLOAT.asList(), StackType.INT.asList(), RawJavaType.INT),
    F2L(140, 0, StackType.FLOAT.asList(), StackType.LONG.asList(), RawJavaType.LONG),
    FADD(98, 0, new StackTypes(StackType.FLOAT, StackType.FLOAT), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    FALOAD(48, 0, new StackTypes(StackType.REF, StackType.INT), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    FASTORE(81, 0, new StackTypes(StackType.REF, StackType.INT, StackType.FLOAT), StackTypes.EMPTY, RawJavaType.VOID),
    FCMPG(150, 0, new StackTypes(StackType.FLOAT, StackType.FLOAT), StackType.INT.asList(), RawJavaType.INT),
    FCMPL(149, 0, new StackTypes(StackType.FLOAT, StackType.FLOAT), StackType.INT.asList(), RawJavaType.INT),
    FCONST_0(11, 0, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FCONST_1(12, 0, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FCONST_2(13, 0, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FDIV(110, 0, new StackTypes(StackType.FLOAT, StackType.FLOAT), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    FLOAD(23, 1, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FLOAD_WIDE(-1, 3, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FLOAD_0(34, 0, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FLOAD_1(35, 0, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FLOAD_2(36, 0, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FLOAD_3(37, 0, StackTypes.EMPTY, StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    FMUL(106, 0, new StackTypes(StackType.FLOAT, StackType.FLOAT), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    FNEG(118, 0, StackType.FLOAT.asList(), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    FREM(114, 0, new StackTypes(StackType.FLOAT, StackType.FLOAT), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    FRETURN(174, 0, StackType.FLOAT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn(), true),
    FSTORE(56, 1, StackType.FLOAT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    FSTORE_WIDE(-1, 3, StackType.FLOAT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    FSTORE_0(67, 0, StackType.FLOAT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    FSTORE_1(68, 0, StackType.FLOAT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    FSTORE_2(69, 0, StackType.FLOAT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    FSTORE_3(70, 0, StackType.FLOAT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    FSUB(102, 0, new StackTypes(StackType.FLOAT, StackType.FLOAT), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    GETFIELD(180, 2, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryGetField()),
    GETSTATIC(178, 2, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryGetStatic()),
    GOTO(167, 2, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryGoto(), true),
    GOTO_W(200, 4, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryGotoW(), true),
    I2B(145, 0, StackType.INT.asList(), StackType.INT.asList(), RawJavaType.BYTE),
    I2C(146, 0, StackType.INT.asList(), StackType.INT.asList(), RawJavaType.CHAR),
    I2D(135, 0, StackType.INT.asList(), StackType.DOUBLE.asList(), RawJavaType.DOUBLE),
    I2F(134, 0, StackType.INT.asList(), StackType.FLOAT.asList(), RawJavaType.FLOAT),
    I2L(133, 0, StackType.INT.asList(), StackType.LONG.asList(), RawJavaType.LONG, true),
    I2S(147, 0, StackType.INT.asList(), StackType.INT.asList(), RawJavaType.SHORT),
    IADD(96, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IALOAD(46, 0, new StackTypes(StackType.REF, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IAND(126, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IASTORE(79, 0, new StackTypes(StackType.REF, StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID),
    ICONST_M1(2, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ICONST_0(3, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ICONST_1(4, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ICONST_2(5, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ICONST_3(6, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ICONST_4(7, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ICONST_5(8, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    IDIV(108, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IF_ACMPEQ(165, 2, new StackTypes(StackType.REF, StackType.REF), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump()),
    IF_ACMPNE(166, 2, new StackTypes(StackType.REF, StackType.REF), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump()),
    IF_ICMPEQ(159, 2, new StackTypes(StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IF_ICMPNE(160, 2, new StackTypes(StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IF_ICMPLT(161, 2, new StackTypes(StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IF_ICMPGE(162, 2, new StackTypes(StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IF_ICMPGT(163, 2, new StackTypes(StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IF_ICMPLE(164, 2, new StackTypes(StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFEQ(153, 2, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFNE(154, 2, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFLT(155, 2, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFGE(156, 2, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFGT(157, 2, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFLE(158, 2, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFNONNULL(199, 2, StackType.REF.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IFNULL(198, 2, StackType.REF.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryConditionalJump(), true),
    IINC(132, 2, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID),
    IINC_WIDE(-1, 5, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID),
    ILOAD(21, 1, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ILOAD_WIDE(-1, 3, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ILOAD_0(26, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ILOAD_1(27, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ILOAD_2(28, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    ILOAD_3(29, 0, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.INT, true),
    IMUL(104, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    INEG(116, 0, StackType.INT.asList(), StackType.INT.asList(), RawJavaType.INT),
    INSTANCEOF(193, 2, StackType.REF.asList(), StackType.INT.asList(), RawJavaType.BOOLEAN, new OperationFactoryCPEntryW()),
    INVOKEDYNAMIC(186, 4, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryInvokeDynamic()),
    INVOKEINTERFACE(185, 4, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryInvokeInterface()),
    INVOKESPECIAL(183, 2, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryInvoke(true)),
    INVOKESTATIC(184, 2, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryInvoke(false)),
    INVOKEVIRTUAL(182, 2, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryInvoke(true)),
    IOR(128, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IREM(112, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IRETURN(172, 0, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn(), true),
    ISHL(120, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    ISHR(122, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    ISTORE(54, 1, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ISTORE_WIDE(-1, 3, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ISTORE_0(59, 0, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ISTORE_1(60, 0, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ISTORE_2(61, 0, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ISTORE_3(62, 0, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    ISUB(100, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IUSHR(124, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    IXOR(130, 0, new StackTypes(StackType.INT, StackType.INT), StackType.INT.asList(), RawJavaType.INT),
    JSR(168, 2, StackTypes.EMPTY, StackType.RETURNADDRESS.asList(), RawJavaType.RETURNADDRESS, new OperationFactoryGoto()),
    JSR_W(201, 4, StackTypes.EMPTY, StackType.RETURNADDRESS.asList(), RawJavaType.VOID),
    L2D(138, 0, StackType.LONG.asList(), StackType.DOUBLE.asList(), RawJavaType.DOUBLE, true),
    L2F(137, 0, StackType.LONG.asList(), StackType.FLOAT.asList(), RawJavaType.FLOAT, true),
    L2I(136, 0, StackType.LONG.asList(), StackType.INT.asList(), RawJavaType.INT, true),
    LADD(97, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG, true),
    LALOAD(47, 0, new StackTypes(StackType.REF, StackType.INT), StackType.LONG.asList(), RawJavaType.LONG),
    LAND(127, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG),
    LASTORE(80, 0, new StackTypes(StackType.REF, StackType.INT, StackType.LONG), StackTypes.EMPTY, RawJavaType.VOID),
    LCMP(148, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.INT.asList(), RawJavaType.INT, true),
    LCONST_0(9, 0, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LCONST_1(10, 0, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LDC(18, 1, null, null, null, new OperationFactoryLDC(), true),
    LDC_W(19, 2, null, null, null, new OperationFactoryLDCW(), true),
    LDC2_W(20, 2, null, null, null, new OperationFactoryLDC2W(), true),
    LDIV(109, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG),
    LLOAD(22, 1, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LLOAD_WIDE(-1, 3, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LLOAD_0(30, 0, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LLOAD_1(31, 0, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LLOAD_2(32, 0, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LLOAD_3(33, 0, StackTypes.EMPTY, StackType.LONG.asList(), RawJavaType.LONG, true),
    LMUL(105, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG),
    LNEG(117, 0, StackType.LONG.asList(), StackType.LONG.asList(), RawJavaType.LONG),
    LOOKUPSWITCH(171, -1, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryLookupSwitch()),
    LOR(129, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG),
    LREM(113, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG),
    LRETURN(173, 0, StackType.LONG.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn(), true),
    LSHL(121, 0, new StackTypes(StackType.LONG, StackType.INT), StackType.LONG.asList(), RawJavaType.LONG),
    LSHR(123, 0, new StackTypes(StackType.LONG, StackType.INT), StackType.LONG.asList(), RawJavaType.LONG),
    LSTORE(55, 1, StackType.LONG.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    LSTORE_WIDE(-1, 3, StackType.LONG.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    LSTORE_0(63, 0, StackType.LONG.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    LSTORE_1(64, 0, StackType.LONG.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    LSTORE_2(65, 0, StackType.LONG.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    LSTORE_3(66, 0, StackType.LONG.asList(), StackTypes.EMPTY, RawJavaType.VOID, true),
    LSUB(101, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG),
    LUSHR(125, 0, new StackTypes(StackType.LONG, StackType.INT), StackType.LONG.asList(), RawJavaType.LONG),
    LXOR(131, 0, new StackTypes(StackType.LONG, StackType.LONG), StackType.LONG.asList(), RawJavaType.LONG),
    MONITORENTER(194, 0, StackType.REF.asList(), StackTypes.EMPTY, RawJavaType.VOID),
    MONITOREXIT(195, 0, StackType.REF.asList(), StackTypes.EMPTY, RawJavaType.VOID),
    MULTIANEWARRAY(197, 3, (StackTypes)null, (StackTypes)null, RawJavaType.REF, new OperationFactoryMultiANewArray()),
    NEW(187, 2, StackTypes.EMPTY, StackType.REF.asList(), (RawJavaType)null, new OperationFactoryNew()),
    NEWARRAY(188, 1, StackType.INT.asList(), StackType.REF.asList(), null),
    NOP(0, 0, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID),
    POP(87, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryPop()),
    POP2(88, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryPop2()),
    PUTFIELD(181, 2, (StackTypes)null, (StackTypes)null, RawJavaType.VOID, new OperationFactoryPutField()),
    PUTSTATIC(179, 2, (StackTypes)null, (StackTypes)null, RawJavaType.VOID, new OperationFactoryPutStatic()),
    RET(169, 1, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn()),
    RET_WIDE(-1, 3, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn()),
    RETURN(177, 0, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryReturn(), true),
    SALOAD(53, 0, new StackTypes(StackType.REF, StackType.INT), StackType.INT.asList(), RawJavaType.SHORT),
    SASTORE(86, 0, new StackTypes(StackType.REF, StackType.INT, StackType.INT), StackTypes.EMPTY, RawJavaType.VOID),
    SIPUSH(17, 2, StackTypes.EMPTY, StackType.INT.asList(), RawJavaType.SHORT),
    SWAP(95, 0, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactorySwap()),
    TABLESWITCH(170, -1, StackType.INT.asList(), StackTypes.EMPTY, RawJavaType.VOID, new OperationFactoryTableSwitch()),
    WIDE(196, -1, (StackTypes)null, (StackTypes)null, (RawJavaType)null, new OperationFactoryWide()),
    FAKE_TRY(-1, 0, StackTypes.EMPTY, StackTypes.EMPTY, RawJavaType.VOID),
    FAKE_CATCH(-1, 0, StackTypes.EMPTY, StackType.REF.asList(), RawJavaType.REF, new OperationFactoryFakeCatch());
    
    private final int opcode;
    private final int bytes;
    private final StackTypes stackPopped;
    private final StackTypes stackPushed;
    private final RawJavaType rawJavaType;
    private final String name;
    private final OperationFactory handler;
    private final boolean noThrow;
    private static final Map<Integer, JVMInstr> opcodeLookup;

    private JVMInstr(int opcode, int bytes, StackTypes popped, StackTypes pushed, RawJavaType rawJavaType) {
        this(opcode, bytes, popped, pushed, rawJavaType, OperationFactoryDefault.Handler.INSTANCE.getHandler(), false);
    }

    private JVMInstr(int opcode, int bytes, StackTypes popped, StackTypes pushed, RawJavaType rawJavaType, boolean noThrow) {
        this(opcode, bytes, popped, pushed, rawJavaType, OperationFactoryDefault.Handler.INSTANCE.getHandler(), noThrow);
    }

    private JVMInstr(int opcode, int bytes, StackTypes popped, StackTypes pushed, RawJavaType rawJavaType, OperationFactory handler) {
        this(opcode, bytes, popped, pushed, rawJavaType, handler, false);
    }

    private JVMInstr(int opcode, int bytes, StackTypes popped, StackTypes pushed, RawJavaType rawJavaType, OperationFactory handler, boolean noThrow) {
        this.opcode = opcode;
        this.bytes = bytes;
        this.stackPopped = popped;
        this.stackPushed = pushed;
        this.name = super.toString().toLowerCase();
        this.handler = handler;
        this.rawJavaType = rawJavaType;
        this.noThrow = noThrow;
    }

    public int getOpcode() {
        return this.opcode;
    }

    public String getName() {
        return this.name;
    }

    public static JVMInstr find(int opcode) {
        Integer iOpcode;
        if (opcode < 0) {
            opcode+=256;
        }
        if (JVMInstr.opcodeLookup.containsKey(iOpcode = Integer.valueOf(opcode))) return JVMInstr.opcodeLookup.get(opcode);
        throw new ConfusedCFRException("Unknown opcode [" + opcode + "]");
    }

    protected int getRawLength() {
        return this.bytes;
    }

    public StackTypes getRawStackPushed() {
        return this.stackPushed;
    }

    public StackTypes getRawStackPopped() {
        return this.stackPopped;
    }

    public StackDelta getStackDelta(byte[] data, ConstantPoolEntry[] constantPoolEntries, StackSim stackSim, Method method) {
        return this.handler.getStackDelta(this, data, constantPoolEntries, stackSim, method);
    }

    public Op01WithProcessedDataAndByteJumps createOperation(ByteData bd, ConstantPool cp, int offset) {
        Op01WithProcessedDataAndByteJumps res = this.handler.createOperation(this, bd, cp, offset);
        return res;
    }

    public RawJavaType getRawJavaType() {
        return this.rawJavaType;
    }

    public boolean isNoThrow() {
        return this.noThrow;
    }

    static {
        JVMInstr.opcodeLookup = new HashMap<Integer, JVMInstr>();
        for (JVMInstr i : JVMInstr.values()) {
            JVMInstr.opcodeLookup.put(i.getOpcode(), i);
        }
    }
}

