/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.benf.cfr.reader.bytecode.analysis.parse.utils.Pair;
import org.benf.cfr.reader.bytecode.analysis.types.ClassNameUtils;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.TypeUsageInformation;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.StreamDumper;
import org.benf.cfr.reader.util.output.SummaryDumper;

public class FileDumper
extends StreamDumper {
    private final JavaTypeInstance type;
    private final SummaryDumper summaryDumper;
    private final BufferedWriter writer;
    private static final int MAX_FILE_LEN_MINUS_EXT = 249;
    private static final int TRUNC_PREFIX_LEN = 150;
    private static int truncCount = 0;

    private File mkFilename(String dir, Pair<String, String> names, JavaTypeInstance type, SummaryDumper summaryDumper) {
        String className;
        String packageName = names.getFirst();
        String outDir = dir;
        if (!packageName.isEmpty()) {
            outDir = outDir + File.separator + packageName.replace((CharSequence)".", (CharSequence)File.separator);
        }
        if ((className = names.getSecond()).length() > 249) {
            className = className.substring(0, 150) + "_cfr_" + FileDumper.truncCount++;
            summaryDumper.notify("Class name " + names.getSecond() + " was shortened to " + className + " due to filesystem limitations.");
        }
        return new File(dir + File.separator + packageName.replace((CharSequence)".", (CharSequence)File.separator) + (packageName.length() == 0 ? "" : File.separator) + className + ".java");
    }

    public FileDumper(String dir, JavaTypeInstance type, SummaryDumper summaryDumper, TypeUsageInformation typeUsageInformation) {
        super(typeUsageInformation);
        this.type = type;
        this.summaryDumper = summaryDumper;
        Pair<String, String> names = ClassNameUtils.getPackageAndClassNames(type.getRawName());
        try {
            File file = this.mkFilename(dir, names, type, summaryDumper);
            File parent = file.getParentFile();
            if (!(parent.exists() || parent.mkdirs())) {
                throw new IllegalStateException("Couldn't create dir: " + parent);
            }
            this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
        }
        catch (FileNotFoundException e) {
            throw new Dumper.CannotCreate(e);
        }
    }

    @Override
    public void close() {
        try {
            this.writer.close();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected void write(String s) {
        try {
            this.writer.write(s);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void addSummaryError(Method method, String s) {
        this.summaryDumper.notifyError(this.type, method, s);
    }
}

