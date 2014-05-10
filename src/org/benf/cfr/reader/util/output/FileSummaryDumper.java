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
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.MethodPrototype;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.SummaryDumper;

public class FileSummaryDumper
implements SummaryDumper {
    private final BufferedWriter writer;
    private transient JavaTypeInstance lastControllingType = null;
    private transient Method lastMethod = null;

    public FileSummaryDumper(String dir) {
        String fileName = dir + File.separator + "summary.txt";
        try {
            File file = new File(fileName);
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
    public void notify(String message) {
        try {
            this.writer.write(message + "\n");
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void notifyError(JavaTypeInstance controllingType, Method method, String error) {
        try {
            if (this.lastControllingType != controllingType) {
                this.lastControllingType = controllingType;
                this.lastMethod = null;
                this.writer.write("\n\n" + controllingType.getRawName() + "\n----------------------------\n\n");
            }
            if (method != this.lastMethod) {
                this.writer.write(method.getMethodPrototype().toString() + "\n");
                this.lastMethod = method;
            }
            this.writer.write("  " + error + "\n");
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
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
}

