/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.StreamDumper;

public class PluginRunner {
    private DCCommonState dcCommonState = PluginRunner.initDCState();

    public void addJarPaths(String[] jarPaths) {
        for (String jarPath : jarPaths) {
            this.addJarPath(jarPath);
        }
    }

    public void addJarPath(String jarPath) {
        try {
            this.dcCommonState.explicitlyLoadJar(jarPath);
        }
        catch (Exception e) {
            // empty catch block
        }
    }

    public String getDecompilationFor(String className) {
        try {
            static class StringStreamDumper
            extends StreamDumper {
                final /* synthetic */ StringBuffer val$outBuffer;
                final /* synthetic */ PluginRunner this$0;

                /*
                 * WARNING - Possible parameter corruption
                 */
                public StringStreamDumper(TypeUsageInformation var1_1) {
                    this.this$0 = var1_1;
                    this.val$outBuffer = typeUsageInformation2;
                    super((TypeUsageInformation)typeUsageInformation);
                }

                @Override
                protected void write(String s) {
                    this.val$outBuffer.append(s);
                }

                @Override
                public void close() {
                }

                @Override
                public void addSummaryError(Method method, String s) {
                }
            }
            ClassFile c = this.dcCommonState.getClassFile(className);
            c = this.dcCommonState.getClassFile(c.getClassType());
            c.loadInnerClasses(this.dcCommonState);
            c.analyseTop(this.dcCommonState);
            TypeUsageCollector collectingDumper = new TypeUsageCollector(c);
            c.collectTypeUsages(collectingDumper);
            StringBuffer outBuffer = new StringBuffer();
            StringStreamDumper d = new StringStreamDumper(this, collectingDumper.getTypeUsageInformation(), outBuffer);
            c.dump(d);
            return outBuffer.toString();
        }
        catch (Exception e) {
            return e.toString();
        }
    }

    private static DCCommonState initDCState() {
        OptionsImpl options = new OptionsImpl(null, null, MapFactory.newMap());
        DCCommonState dcCommonState = new DCCommonState(options);
        return dcCommonState;
    }

}

