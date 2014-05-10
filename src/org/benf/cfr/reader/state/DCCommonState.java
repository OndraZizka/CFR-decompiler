/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.state;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.benf.cfr.reader.bytecode.analysis.types.ClassNameUtils;
import org.benf.cfr.reader.bytecode.analysis.types.JavaRefTypeInstance;
import org.benf.cfr.reader.bytecode.analysis.types.JavaTypeInstance;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.state.ClassCache;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.ConfusedCFRException;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.bytestream.BaseByteData;
import org.benf.cfr.reader.util.bytestream.ByteData;
import org.benf.cfr.reader.util.configuration.ConfigCallback;
import org.benf.cfr.reader.util.functors.UnaryFunction;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class DCCommonState {
    private final ClassCache classCache;
    private final Options options;
    private boolean initiallyConfigured;
    private boolean unexpectedDirectory;
    private String pathPrefix;
    private String classRemovePrefix;
    private transient LinkedHashSet<String> couldNotLoadClasses;
    private Map<String, ClassFile> classFileCache;
    private Map<String, String> classToPathMap;

    public DCCommonState(Options options) {
        this.classCache = new ClassCache(this);
        this.unexpectedDirectory = false;
        this.pathPrefix = "";
        this.classRemovePrefix = "";
        this.couldNotLoadClasses = new LinkedHashSet<String>();
        this.classFileCache = MapFactory.newExceptionRetainingLazyMap(new UnaryFunction<String, ClassFile>(){

            @Override
            public ClassFile invoke(String arg) {
                return DCCommonState.this.loadClassFileAtPath(arg);
            }
        });
        this.options = options;
    }

    public void configureWith(ClassFile classFile) {
        new Configurator(null).configureWith(classFile);
    }

    public Set<String> getCouldNotLoadClasses() {
        return this.couldNotLoadClasses;
    }

    private byte[] getBytesFromFile(InputStream is, long length) throws IOException {
        int offset;
        if (length > Integer.MAX_VALUE) {
            // empty if block
        }
        byte[] bytes = new byte[(int)length];
        int numRead = 0;
        for (offset = 0; offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0; offset+=numRead) {
        }
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file");
        }
        is.close();
        return bytes;
    }

    private ClassFile loadClassFileAtPath(String path) {
        ClassFile classFile;
        File file;
        Map<String, String> classPathFiles = this.getClassPathClasses();
        String jarName = classPathFiles.get(path);
        ZipFile zipFile = null;
        InputStream is = null;
        long length = 0;
        String usePath = path;
        if (this.unexpectedDirectory) {
            if (usePath.startsWith(this.classRemovePrefix)) {
                usePath = usePath.substring(this.classRemovePrefix.length());
            }
            usePath = this.pathPrefix + usePath;
        }
        if (!(file = new File(usePath)).exists()) {
            if (jarName == null) {
                throw new IOException("No such file");
            }
        } else {
            is = new FileInputStream(file);
            length = file.length();
        }
        zipFile = new ZipFile(new File(jarName), 1);
        ZipEntry zipEntry = zipFile.getEntry(path);
        length = zipEntry.getSize();
        is = zipFile.getInputStream(zipEntry);
        try {
            ClassFile res;
            byte[] content = this.getBytesFromFile(is, length);
            BaseByteData data = new BaseByteData(content);
            classFile = res = new ClassFile(data, usePath, this);
            if (zipFile == null) return classFile;
        }
        catch (Throwable var14_13) {
            try {
                if (zipFile == null) throw var14_13;
                zipFile.close();
                throw var14_13;
            }
            catch (IOException e) {
                this.couldNotLoadClasses.add((Object)path);
                throw new CannotLoadClassException(path, e);
            }
        }
        zipFile.close();
        return classFile;
    }

    private boolean processClassPathFile(File file, String path, Map<String, String> classToPathMap, boolean dump) {
        try {
            ZipFile zipFile = new ZipFile(file, 1);
            try {
                Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                while (enumeration.hasMoreElements()) {
                    String name;
                    ZipEntry entry;
                    if ((entry = (ZipEntry)enumeration.nextElement()).isDirectory()) continue;
                    if ((name = entry.getName()).endsWith(".class")) {
                        if (dump) {
                            System.out.println("  " + name);
                        }
                        classToPathMap.put(name, path);
                        continue;
                    }
                    if (!dump) continue;
                    System.out.println("  [ignoring] " + name);
                }
            }
            finally {
                zipFile.close();
            }
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }

    public List<JavaTypeInstance> explicitlyLoadJar(String path) {
        Map thisJar;
        File file = new File(path);
        if (!file.exists()) {
            throw new ConfusedCFRException("No such jar file " + path);
        }
        if (!this.processClassPathFile(file, path, thisJar = MapFactory.newLinkedMap(), false)) {
            throw new ConfusedCFRException("Failed to load jar " + path);
        }
        this.classToPathMap = null;
        this.getClassPathClasses();
        List output = ListFactory.newList();
        Iterator i$ = thisJar.entrySet().iterator();
        while (i$.hasNext()) {
            String classPath;
            Map.Entry entry;
            if (!(classPath = (String)(entry = i$.next()).getKey()).toLowerCase().endsWith(".class")) continue;
            this.classToPathMap.put(classPath, (String)entry.getValue());
            output.add((JavaRefTypeInstance)this.classCache.getRefClassFor(classPath.substring(0, classPath.length() - 6)));
        }
        return output;
    }

    private Map<String, String> getClassPathClasses() {
        if (this.classToPathMap != null) return this.classToPathMap;
        boolean dump = (Boolean)this.options.getOption(OptionsImpl.DUMP_CLASS_PATH);
        this.classToPathMap = MapFactory.newMap();
        String classPath = System.getProperty("java.class.path") + ":" + System.getProperty("sun.boot.class.path");
        if (dump) {
            System.out.println("/* ClassPath Diagnostic - searching :" + classPath);
        }
        for (String path : classPaths = classPath.split("" + File.pathSeparatorChar)) {
            File f;
            if (dump) {
                System.out.println(" " + path);
            }
            if ((f = new File(path)).exists()) {
                if (f.isDirectory()) {
                    if (dump) {
                        System.out.println(" (Directory)");
                    }
                    for (File file : f.listFiles()) {
                        this.processClassPathFile(file, file.getAbsolutePath(), this.classToPathMap, dump);
                    }
                    continue;
                }
                this.processClassPathFile(f, path, this.classToPathMap, dump);
                continue;
            }
            if (!dump) continue;
            System.out.println(" (Can't access)");
        }
        if (!dump) return this.classToPathMap;
        System.out.println(" */");
        return this.classToPathMap;
    }

    public ClassFile getClassFile(String path) throws CannotLoadClassException {
        return this.classFileCache.get(path);
    }

    public JavaRefTypeInstance getClassTypeOrNull(String path) {
        try {
            ClassFile classFile = this.getClassFile(path);
            return (JavaRefTypeInstance)classFile.getClassType();
        }
        catch (CannotLoadClassException e) {
            return null;
        }
    }

    public ClassFile getClassFile(JavaTypeInstance classInfo) throws CannotLoadClassException {
        String path = classInfo.getRawName();
        path = ClassNameUtils.convertToPath(path) + ".class";
        return this.getClassFile(path);
    }

    public ClassFile getClassFileMaybePath(String pathOrName) throws CannotLoadClassException {
        File f;
        if (pathOrName.endsWith(".class")) {
            return this.getClassFile(pathOrName);
        }
        if (!(f = new File(pathOrName)).exists()) return this.getClassFile(ClassNameUtils.convertToPath(pathOrName) + ".class");
        f = null;
        return this.getClassFile(pathOrName);
    }

    public ClassCache getClassCache() {
        return this.classCache;
    }

    public Options getOptions() {
        return this.options;
    }

    public boolean isJar(String path) {
        return path.toLowerCase().endsWith(".jar");
    }

    class Configurator
    implements ConfigCallback {
        private Configurator() {
        }

        private void reverse(String[] in) {
            List<String> l = Arrays.asList(in);
            Collections.reverse(l);
            l.toArray(in);
        }

        private String join(String[] in, String sep) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String s : in) {
                if (first) {
                    first = false;
                } else {
                    sb.append(sep);
                }
                sb.append(s);
            }
            return sb.toString();
        }

        private void getCommonRoot(String filePath, String classPath) {
            int diffpt;
            String npath = filePath.replace('\\', '/');
            String[] fileParts = npath.split("/");
            String[] classParts = classPath.split("/");
            this.reverse(fileParts);
            this.reverse(classParts);
            int min = Math.min(fileParts.length, classParts.length);
            for (diffpt = 0; diffpt < min && fileParts[diffpt].equals(classParts[diffpt]); ++diffpt) {
            }
            fileParts = Arrays.copyOfRange(fileParts, diffpt, fileParts.length);
            classParts = Arrays.copyOfRange(classParts, diffpt, classParts.length);
            this.reverse(fileParts);
            this.reverse(classParts);
            this$0.pathPrefix = fileParts.length == 0 ? "" : this.join(fileParts, "/") + "/";
            this$0.classRemovePrefix = classParts.length == 0 ? "" : this.join(classParts, "/") + "/";
        }

        @Override
        public void configureWith(ClassFile partiallyConstructedClassFile) {
            String path = partiallyConstructedClassFile.getUsePath();
            JavaRefTypeInstance refTypeInstance = (JavaRefTypeInstance)partiallyConstructedClassFile.getClassType();
            String actualPath = partiallyConstructedClassFile.getFilePath();
            if (!actualPath.equals(path)) {
                this$0.unexpectedDirectory = true;
                if (path.endsWith(actualPath)) {
                    this$0.pathPrefix = path.substring(0, path.length() - actualPath.length());
                } else {
                    this.getCommonRoot(path, actualPath);
                }
            }
            this$0.initiallyConfigured = true;
        }

        /* synthetic */ Configurator(DCCommonState x0,  x1) {
            this();
        }
    }

}

