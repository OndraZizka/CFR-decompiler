/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader;

import java.io.PrintStream;
import java.util.List;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.getopt.BadParametersException;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.GetOptSinkFactory;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.DumperFactory;
import org.benf.cfr.reader.util.output.NopSummaryDumper;
import org.benf.cfr.reader.util.output.SummaryDumper;
import org.benf.cfr.reader.util.output.ToStringDumper;

public class Main {
    public static void doClass(DCCommonState dcCommonState, String path) {
        Options options = dcCommonState.getOptions();
        Dumper d = new ToStringDumper();
        try {
            NopSummaryDumper summaryDumper = new NopSummaryDumper();
            ClassFile c = dcCommonState.getClassFileMaybePath(path);
            dcCommonState.configureWith(c);
            try {
                c = dcCommonState.getClassFile(c.getClassType());
            }
            catch (CannotLoadClassException e) {
                // empty catch block
            }
            if (((Boolean)options.getOption(OptionsImpl.DECOMPILE_INNER_CLASSES)).booleanValue()) {
                c.loadInnerClasses(dcCommonState);
            }
            c.analyseTop(dcCommonState);
            TypeUsageCollector collectingDumper = new TypeUsageCollector(c);
            c.collectTypeUsages(collectingDumper);
            d = DumperFactory.getNewTopLevelDumper(options, c.getClassType(), summaryDumper, collectingDumper.getTypeUsageInformation());
            String methname = options.getMethodName();
            if (methname != null) {
                try {
                    for (Method method : c.getMethodByName(methname)) {
                        method.dump(d, true);
                    }
                }
                catch (NoSuchMethodException e) {
                    throw new BadParametersException("No such method '" + methname + "'.", OptionsImpl.getFactory());
                }
            } else {
                c.dump(d);
            }
            d.print("");
        }
        catch (ConfusedCFRException e) {
            System.err.println(e.toString());
            for (StackTraceElement x : e.getStackTrace()) {
                System.err.println(x);
            }
        }
        catch (CannotLoadClassException e) {
            System.out.println("Can't load the class specified:");
            System.out.println(e.toString());
        }
        catch (RuntimeException e) {
            System.err.println(e.toString());
            for (StackTraceElement x : e.getStackTrace()) {
                System.err.println(x);
            }
        }
        finally {
            d.close();
        }
    }

    public static void doJar(DCCommonState dcCommonState, String path) {
        Options options = dcCommonState.getOptions();
        SummaryDumper summaryDumper = null;
        boolean silent = true;
        try {
            silent = (Boolean)options.getOption(OptionsImpl.SILENT);
            summaryDumper = DumperFactory.getSummaryDumper(options);
            List<JavaTypeInstance> types = dcCommonState.explicitlyLoadJar(path);
            summaryDumper.notify("Summary for " + path);
            summaryDumper.notify("Decompiled with CFR 0_78");
            if (!silent) {
                System.err.println("Processing " + path + " (use " + OptionsImpl.SILENT.getName() + " to silence)");
            }
            int fatal = 0;
            int succeded = 0;
            for (JavaTypeInstance type : types) {
                Dumper d = new ToStringDumper();
                try {
                    ClassFile c = dcCommonState.getClassFile(type);
                    if (c.isInnerClass()) continue;
                    if (!silent) {
                        System.err.println("Processing " + type.getRawName());
                    }
                    if (((Boolean)options.getOption(OptionsImpl.DECOMPILE_INNER_CLASSES)).booleanValue()) {
                        c.loadInnerClasses(dcCommonState);
                    }
                    c.analyseTop(dcCommonState);
                    TypeUsageCollector collectingDumper = new TypeUsageCollector(c);
                    c.collectTypeUsages(collectingDumper);
                    d = DumperFactory.getNewTopLevelDumper(options, c.getClassType(), summaryDumper, collectingDumper.getTypeUsageInformation());
                    c.dump(d);
                    ++succeded;
                    d.print("\n\n");
                    continue;
                }
                catch (Dumper.CannotCreate e) {
                    throw e;
                }
                catch (RuntimeException e) {
                    ++fatal;
                    d.print(e.toString()).print("\n\n\n");
                    continue;
                }
                finally {
                    d.close();
                    continue;
                }
            }
        }
        catch (RuntimeException e) {
            String err = "Exception analysing jar " + e;
            System.err.print(err);
            if (summaryDumper == null) return;
            summaryDumper.notify(err);
        }
        finally {
            if (summaryDumper != null) {
                summaryDumper.close();
            }
        }
    }

    public static void main(String[] args) {
        String path;
        GetOptParser getOptParser = new GetOptParser();
        Options options = null;
        try {
            options = getOptParser.parse(args, OptionsImpl.getFactory());
        }
        catch (BadParametersException e) {
            getOptParser.showHelp(OptionsImpl.getFactory());
            System.exit(1);
        }
        catch (Exception e) {
            System.err.print(e);
            System.exit(1);
        }
        if (options.optionIsSet(OptionsImpl.HELP) || options.getFileName() == null) {
            getOptParser.showHelp(OptionsImpl.getFactory(), options, (PermittedOptionProvider.ArgumentParam<String, Void>)OptionsImpl.HELP);
            return;
        }
        DCCommonState dcCommonState = new DCCommonState(options);
        if (dcCommonState.isJar(path = options.getFileName())) {
            Main.doJar(dcCommonState, path);
        } else {
            Main.doClass(dcCommonState, path);
        }
    }
}

