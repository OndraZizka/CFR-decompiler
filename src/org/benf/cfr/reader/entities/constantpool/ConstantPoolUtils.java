/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.constantpool;

import java.util.List;
import java.util.logging.Logger;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDelta;
import org.benf.cfr.reader.bytecode.analysis.stack.StackDeltaImpl;
import org.benf.cfr.reader.bytecode.analysis.types.ClassSignature;
import org.benf.cfr.reader.bytecode.analysis.types.FormalTypeParameter;
import org.benf.cfr.reader.bytecode.analysis.types.JavaArrayTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericPlaceholderTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaGenericRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaWildcardTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.StackTypes;
import org.benf.cfr.reader.bytecode.analysis.types.WildcardType;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.entities.constantpool.ConstantPool;
import org.benf.cfr.reader.entities.constantpool.ConstantPoolEntryUTF8;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.LoggerFactory;

public class ConstantPoolUtils {
    private static final Logger logger = LoggerFactory.create(ConstantPoolUtils.class);

    private static JavaTypeInstance parseRefType(String tok, ConstantPool cp, boolean isTemplate) {
        int idxGen = tok.indexOf(60);
        int idxStart = 0;
        if (idxGen != -1) {
            List<JavaTypeInstance> genericTypes = null;
            String already = "";
            while (idxGen != -1) {
                String pre = tok.substring(idxStart, idxGen);
                already = already + pre;
                String gen = tok.substring(idxGen + 1, tok.length() - 1);
                Pair<List<JavaTypeInstance>, Integer> genericTypePair = ConstantPoolUtils.parseTypeList(gen, cp);
                genericTypes = genericTypePair.getFirst();
                if ((idxStart = idxGen + genericTypePair.getSecond() + 1) >= gen.length()) break;
                if (tok.charAt(idxStart) != '>') {
                    throw new IllegalStateException();
                }
                if ((idxGen = tok.indexOf(60, ++idxStart)) != -1) continue;
                already = already + tok.substring(idxStart, tok.length());
                return cp.getClassCache().getRefClassFor(already);
            }
            JavaRefTypeInstance clazzType = cp.getClassCache().getRefClassFor(already);
            return new JavaGenericRefTypeInstance(clazzType, genericTypes);
        }
        if (!isTemplate) return cp.getClassCache().getRefClassFor(tok);
        return new JavaGenericPlaceholderTypeInstance(tok, cp);
    }

    public static JavaTypeInstance decodeTypeTok(String tok, ConstantPool cp) {
        int idx = 0;
        int numArrayDims = 0;
        char c = tok.charAt(idx);
        WildcardType wildcardType = WildcardType.NONE;
        if (c == '-' || c == '+') {
            wildcardType = c == '+' ? WildcardType.EXTENDS : WildcardType.SUPER;
            c = tok.charAt(++idx);
        }
        while (c == '[') {
            ++numArrayDims;
            c = tok.charAt(++idx);
        }
        JavaTypeInstance javaTypeInstance = null;
        switch (c) {
            case '*': {
                javaTypeInstance = new JavaGenericPlaceholderTypeInstance("?", cp);
                break;
            }
            case 'L': {
                javaTypeInstance = ConstantPoolUtils.parseRefType(tok.substring(idx + 1, tok.length() - 1), cp, false);
                break;
            }
            case 'T': {
                javaTypeInstance = ConstantPoolUtils.parseRefType(tok.substring(idx + 1, tok.length() - 1), cp, true);
                break;
            }
            case 'B': 
            case 'C': 
            case 'D': 
            case 'F': 
            case 'I': 
            case 'J': 
            case 'S': 
            case 'Z': {
                javaTypeInstance = ConstantPoolUtils.decodeRawJavaType(c);
                break;
            }
            default: {
                throw new ConfusedCFRException("Invalid type string " + tok);
            }
        }
        if (numArrayDims > 0) {
            javaTypeInstance = new JavaArrayTypeInstance(numArrayDims, javaTypeInstance);
        }
        if (wildcardType == WildcardType.NONE) return javaTypeInstance;
        javaTypeInstance = new JavaWildcardTypeInstance(wildcardType, javaTypeInstance);
        return javaTypeInstance;
    }

    public static RawJavaType decodeRawJavaType(char c) {
        RawJavaType javaTypeInstance;
        switch (c) {
            case 'B': {
                javaTypeInstance = RawJavaType.BYTE;
                break;
            }
            case 'C': {
                javaTypeInstance = RawJavaType.CHAR;
                break;
            }
            case 'I': {
                javaTypeInstance = RawJavaType.INT;
                break;
            }
            case 'S': {
                javaTypeInstance = RawJavaType.SHORT;
                break;
            }
            case 'Z': {
                javaTypeInstance = RawJavaType.BOOLEAN;
                break;
            }
            case 'F': {
                javaTypeInstance = RawJavaType.FLOAT;
                break;
            }
            case 'D': {
                javaTypeInstance = RawJavaType.DOUBLE;
                break;
            }
            case 'J': {
                javaTypeInstance = RawJavaType.LONG;
                break;
            }
            default: {
                throw new ConfusedCFRException("Illegal raw java type");
            }
        }
        return javaTypeInstance;
    }

    private static String getNextTypeTok(String proto, int curridx) {
        int startidx = curridx;
        char c = proto.charAt(curridx);
        if (c == '-' || c == '+') {
            c = proto.charAt(++curridx);
        }
        while (c == '[') {
            c = proto.charAt(++curridx);
        }
        switch (c) {
            case '*': {
                ++curridx;
                break;
            }
            case 'L': 
            case 'T': {
                int openBra = 0;
                do {
                    c = proto.charAt(++curridx);
                    switch (c) {
                        case '<': {
                            ++openBra;
                            break;
                        }
                        case '>': {
                            --openBra;
                        }
                    }
                } while (openBra > 0 || c != ';');
                ++curridx;
                break;
            }
            case 'B': 
            case 'C': 
            case 'D': 
            case 'F': 
            case 'I': 
            case 'J': 
            case 'S': 
            case 'Z': {
                ++curridx;
                break;
            }
            default: {
                throw new ConfusedCFRException("Can't parse proto : " + proto + " starting " + proto.substring(startidx));
            }
        }
        return proto.substring(startidx, curridx);
    }

    private static String getNextFormalTypeTok(String proto, int curridx) {
        int startidx = curridx;
        while (proto.charAt(curridx) != ':') {
            ++curridx;
        }
        if (proto.charAt(++curridx) != ':') {
            String classBound = ConstantPoolUtils.getNextTypeTok(proto, curridx);
            curridx+=classBound.length();
        }
        if (proto.charAt(curridx) != ':') return proto.substring(startidx, curridx);
        String interfaceBound = ConstantPoolUtils.getNextTypeTok(proto, ++curridx);
        curridx+=interfaceBound.length();
        return proto.substring(startidx, curridx);
    }

    private static FormalTypeParameter decodeFormalTypeTok(String tok, ConstantPool cp, int idx) {
        while (tok.charAt(idx) != ':') {
            ++idx;
        }
        String name = tok.substring(0, idx);
        JavaTypeInstance classBound = null;
        if (tok.charAt(++idx) != ':') {
            String classBoundTok = ConstantPoolUtils.getNextTypeTok(tok, idx);
            classBound = ConstantPoolUtils.decodeTypeTok(classBoundTok, cp);
            idx+=classBoundTok.length();
        }
        JavaTypeInstance interfaceBound = null;
        if (idx >= tok.length() || tok.charAt(idx) != ':') return new FormalTypeParameter(name, classBound, interfaceBound);
        String interfaceBoundTok = ConstantPoolUtils.getNextTypeTok(tok, ++idx);
        interfaceBound = ConstantPoolUtils.decodeTypeTok(interfaceBoundTok, cp);
        idx+=interfaceBoundTok.length();
        return new FormalTypeParameter(name, classBound, interfaceBound);
    }

    public static ClassSignature parseClassSignature(ConstantPoolEntryUTF8 signature, ConstantPool cp) {
        String sig = signature.getValue();
        int curridx = 0;
        List formalTypeParameters = null;
        if (sig.charAt(curridx) == '<') {
            formalTypeParameters = ListFactory.newList();
            ++curridx;
            while (sig.charAt(curridx) != '>') {
                String formalTypeTok;
                FormalTypeParameter formalTypeParameter;
                if (!(formalTypeParameter = ConstantPoolUtils.decodeFormalTypeTok(formalTypeTok = ConstantPoolUtils.getNextFormalTypeTok(sig, curridx), cp, 0)).getName().equals("")) {
                    formalTypeParameters.add((FormalTypeParameter)formalTypeParameter);
                }
                curridx+=formalTypeTok.length();
            }
            ++curridx;
        }
        String superClassSignatureTok = ConstantPoolUtils.getNextTypeTok(sig, curridx);
        curridx+=superClassSignatureTok.length();
        JavaTypeInstance superClassSignature = ConstantPoolUtils.decodeTypeTok(superClassSignatureTok, cp);
        List interfaceClassSignatures = ListFactory.newList();
        for (; curridx < sig.length(); curridx+=interfaceSignatureTok.length()) {
            interfaceSignatureTok = ConstantPoolUtils.getNextTypeTok(sig, curridx);
            interfaceClassSignatures.add((JavaTypeInstance)ConstantPoolUtils.decodeTypeTok(interfaceSignatureTok, cp));
        }
        return new ClassSignature(formalTypeParameters, superClassSignature, interfaceClassSignatures);
    }

    public static MethodPrototype parseJavaMethodPrototype(ClassFile classFile, JavaTypeInstance classType, String name, boolean instanceMethod, Method.MethodConstructor constructorFlag, ConstantPoolEntryUTF8 prototype, ConstantPool cp, boolean varargs, VariableNamer variableNamer) {
        String proto = prototype.getValue();
        int curridx = 0;
        List formalTypeParameters = null;
        if (proto.charAt(curridx) == '<') {
            formalTypeParameters = ListFactory.newList();
            ++curridx;
            while (proto.charAt(curridx) != '>') {
                String formalTypeTok = ConstantPoolUtils.getNextFormalTypeTok(proto, curridx);
                formalTypeParameters.add((FormalTypeParameter)ConstantPoolUtils.decodeFormalTypeTok(formalTypeTok, cp, 0));
                curridx+=formalTypeTok.length();
            }
            ++curridx;
        }
        if (proto.charAt(curridx) != '(') {
            throw new ConfusedCFRException("Prototype " + proto + " is invalid");
        }
        ++curridx;
        List args = ListFactory.newList();
        while (proto.charAt(curridx) != ')') {
            String typeTok = ConstantPoolUtils.getNextTypeTok(proto, curridx);
            args.add((JavaTypeInstance)ConstantPoolUtils.decodeTypeTok(typeTok, cp));
            curridx+=typeTok.length();
        }
        JavaTypeInstance resultType = RawJavaType.VOID;
        switch (proto.charAt(++curridx)) {
            case 'V': {
                break;
            }
            default: {
                resultType = ConstantPoolUtils.decodeTypeTok(ConstantPoolUtils.getNextTypeTok(proto, curridx), cp);
            }
        }
        MethodPrototype res = new MethodPrototype(classFile, classType, name, instanceMethod, constructorFlag, formalTypeParameters, args, resultType, varargs, variableNamer, cp);
        return res;
    }

    public static Pair<List<JavaTypeInstance>, Integer> parseTypeList(String proto, ConstantPool cp) {
        int curridx2;
        int len = proto.length();
        List res = ListFactory.newList();
        for (int curridx2 = 0; curridx2 < len && proto.charAt(curridx2) != '>'; curridx2+=typeTok.length()) {
            typeTok = ConstantPoolUtils.getNextTypeTok(proto, curridx2);
            res.add((JavaTypeInstance)ConstantPoolUtils.decodeTypeTok(typeTok, cp));
        }
        return Pair.make(res, curridx2);
    }

    public static StackDelta parseMethodPrototype(boolean member, ConstantPoolEntryUTF8 prototype, ConstantPool cp) {
        String proto = prototype.getValue();
        int curridx = 1;
        if (!proto.startsWith("(")) {
            throw new ConfusedCFRException("Prototype " + proto + " is invalid");
        }
        StackTypes argumentTypes = new StackTypes(new StackType[0]);
        if (member) {
            argumentTypes.add((Object)StackType.REF);
        }
        while (proto.charAt(curridx) != ')') {
            String typeTok = ConstantPoolUtils.getNextTypeTok(proto, curridx);
            argumentTypes.add((Object)ConstantPoolUtils.decodeTypeTok(typeTok, cp).getStackType());
            curridx+=typeTok.length();
        }
        StackTypes resultType = StackTypes.EMPTY;
        switch (proto.charAt(++curridx)) {
            case 'V': {
                break;
            }
            default: {
                resultType = ConstantPoolUtils.decodeTypeTok(ConstantPoolUtils.getNextTypeTok(proto, curridx), cp).getStackType().asList();
            }
        }
        StackDeltaImpl res = new StackDeltaImpl(argumentTypes, resultType);
        return res;
    }
}

