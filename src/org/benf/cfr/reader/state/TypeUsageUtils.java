/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.util.LinkedList;
import org.benf.cfr.reader.bytecode.analysis.types.InnerClassInfo;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.CommaHelp;

public class TypeUsageUtils {
    public static String generateInnerClassShortName(JavaRefTypeInstance clazz, JavaRefTypeInstance analysisType) {
        String clazzRawName;
        JavaRefTypeInstance currentClass;
        String analysisTypeRawName;
        boolean analysisTypeFound;
        int idx;
        block6 : {
            InnerClassInfo innerClassInfo;
            LinkedList classStack = ListFactory.newLinkedList();
            analysisTypeFound = false;
            if (clazz.getRawName().startsWith(analysisType.getRawName())) {
                analysisTypeFound = true;
            }
            currentClass = clazz;
            do {
                if (!(innerClassInfo = currentClass.getInnerClassHereInfo()).isAnonymousClass()) {
                    classStack.addFirst((JavaRefTypeInstance)currentClass);
                }
                if (!innerClassInfo.isInnerClass()) break block6;
            } while (!(currentClass = innerClassInfo.getOuterClass()).equals(analysisType));
            analysisTypeFound = true;
        }
        if (analysisTypeFound == currentClass.equals(analysisType)) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (JavaRefTypeInstance stackClass : classStack) {
                first = CommaHelp.dot(first, sb);
                sb.append(stackClass.getRawShortName());
            }
            return sb.toString();
        }
        if ((clazzRawName = clazz.getRawName()).equals(analysisTypeRawName = analysisType.getRawName()) && (idx = clazzRawName.lastIndexOf(46)) >= 1 && idx < clazzRawName.length() - 1) {
            return clazzRawName.substring(idx + 1);
        }
        if (analysisTypeRawName.length() < clazzRawName.length() - 1) return clazzRawName.substring(analysisType.getRawName().length() + 1);
        return clazzRawName;
    }
}

