/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.FileDumper;
import org.benf.cfr.reader.util.output.FileSummaryDumper;
import org.benf.cfr.reader.util.output.NopSummaryDumper;
import org.benf.cfr.reader.util.output.StdIODumper;
import org.benf.cfr.reader.util.output.SummaryDumper;

public class DumperFactory {
    public static Dumper getNewTopLevelDumper(Options options, JavaTypeInstance classType, SummaryDumper summaryDumper, TypeUsageInformation typeUsageInformation) {
        if (options.optionIsSet(OptionsImpl.OUTPUT_DIR)) return new FileDumper((String)options.getOption(OptionsImpl.OUTPUT_DIR), classType, summaryDumper, typeUsageInformation);
        return new StdIODumper(typeUsageInformation);
    }

    public static SummaryDumper getSummaryDumper(Options options) {
        if (options.optionIsSet(OptionsImpl.OUTPUT_DIR)) return new FileSummaryDumper((String)options.getOption(OptionsImpl.OUTPUT_DIR));
        return new NopSummaryDumper();
    }
}

