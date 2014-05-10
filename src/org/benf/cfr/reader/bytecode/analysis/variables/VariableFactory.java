/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.variables;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.parse.LValue;
import org.benf.cfr.reader.bytecode.analysis.parse.lvalue.LocalVariable;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.bytecode.analysis.types.RawJavaType;
import org.benf.cfr.reader.bytecode.analysis.types.StackType;
import org.benf.cfr.reader.bytecode.analysis.types.discovery.InferredJavaType;
import org.benf.cfr.reader.bytecode.analysis.variables.Ident;
import org.benf.cfr.reader.bytecode.analysis.variables.NamedVariable;
import org.benf.cfr.reader.bytecode.analysis.variables.VariableNamer;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.MapFactory;

public class VariableFactory {
    private final VariableNamer variableNamer;
    private final Map<Integer, InferredJavaType> typedArgs;
    private final Method method;
    private final Map<LValue, LValue> cache = MapFactory.newMap();

    public VariableFactory(Method method) {
        this.variableNamer = method.getVariableNamer();
        if (method == null) {
            throw new ConfusedCFRException("No method signature for a variable factory");
        }
        MethodPrototype methodPrototype = method.getMethodPrototype();
        List<JavaTypeInstance> args = methodPrototype.getArgs();
        this.typedArgs = MapFactory.newMap();
        int offset = 0;
        if (methodPrototype.isInstanceMethod()) {
            JavaTypeInstance thisType = method.getClassFile().getClassType();
            this.typedArgs.put(offset++, new InferredJavaType(thisType, InferredJavaType.Source.UNKNOWN, true));
        }
        for (JavaTypeInstance arg : args) {
            this.typedArgs.put(offset, new InferredJavaType(arg, InferredJavaType.Source.UNKNOWN, true));
            offset+=arg.getStackType().getComputationCategory();
        }
        this.method = method;
    }

    public JavaTypeInstance getReturn() {
        return this.method.getMethodPrototype().getReturnType();
    }

    public LValue localVariable(int stackPosition, Ident ident, int origCodeRawOffset, boolean guessedFinal) {
        LocalVariable tmp;
        InferredJavaType varType;
        LValue val;
        if (ident == null) {
            throw new IllegalStateException();
        }
        if ((varType = this.typedArgs.get(stackPosition)) == null) {
            varType = new InferredJavaType(RawJavaType.VOID, InferredJavaType.Source.UNKNOWN);
        }
        if ((val = this.cache.get(tmp = new LocalVariable(stackPosition, ident, this.variableNamer, origCodeRawOffset, varType, guessedFinal))) != null) return val;
        this.cache.put(tmp, tmp);
        val = tmp;
        return val;
    }

    public void mutatingRenameUnClash(LocalVariable toRename) {
        this.variableNamer.mutatingRenameUnClash(toRename.getName());
    }
}

