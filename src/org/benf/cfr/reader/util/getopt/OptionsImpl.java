/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.util.ClassFileVersion;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.Troolean;
import org.benf.cfr.reader.util.getopt.BadParametersException;
import org.benf.cfr.reader.util.getopt.GetOptSinkFactory;
import org.benf.cfr.reader.util.getopt.OptionDecoder;
import org.benf.cfr.reader.util.getopt.OptionDecoderParam;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class OptionsImpl
implements Options {
    private final String fileName;
    private final String methodName;
    private final Map<String, String> opts;
    private static final OptionDecoder<Integer> default0intDecoder = new OptionDecoder<Integer>(){

        @Override
        public Integer invoke(String arg, Void ignore) {
            int x;
            if (arg == null) {
                return 0;
            }
            if ((x = Integer.parseInt(arg)) >= 0) return x;
            throw new IllegalArgumentException("required int >= 0");
        }

        @Override
        public String getRangeDescription() {
            return "int >= 0";
        }

        @Override
        public String getDefaultValue() {
            return "0";
        }
    };
    private static final OptionDecoder<Troolean> defaultNeitherTrooleanDecoder = new OptionDecoder<Troolean>(){

        @Override
        public Troolean invoke(String arg, Void ignore) {
            if (arg != null) return Troolean.get(Boolean.parseBoolean(arg));
            return Troolean.NEITHER;
        }

        @Override
        public String getRangeDescription() {
            return "boolean";
        }

        @Override
        public String getDefaultValue() {
            return null;
        }
    };
    private static final OptionDecoder<Boolean> defaultTrueBooleanDecoder = new OptionDecoder<Boolean>(){

        @Override
        public Boolean invoke(String arg, Void ignore) {
            if (arg != null) return Boolean.parseBoolean(arg);
            return true;
        }

        @Override
        public String getRangeDescription() {
            return "boolean";
        }

        @Override
        public String getDefaultValue() {
            return "true";
        }
    };
    private static final OptionDecoder<Boolean> defaultFalseBooleanDecoder = new OptionDecoder<Boolean>(){

        @Override
        public Boolean invoke(String arg, Void ignore) {
            if (arg != null) return Boolean.parseBoolean(arg);
            return false;
        }

        @Override
        public String getRangeDescription() {
            return "boolean";
        }

        @Override
        public String getDefaultValue() {
            return "false";
        }
    };
    private static final OptionDecoder<String> defaultNullStringDecoder = new OptionDecoder<String>(){

        @Override
        public String invoke(String arg, Void ignore) {
            return arg;
        }

        @Override
        public String getRangeDescription() {
            return "string";
        }

        @Override
        public String getDefaultValue() {
            return null;
        }
    };
    private static final String CFR_WEBSITE = "http://www.benf.org/other/cfr/";
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> SUGAR_STRINGBUFFER = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("stringbuffer", new VersionSpecificDefaulter(ClassFileVersion.JAVA_5, false, null), "Convert new Stringbuffer().add.add.add to string + string + string - see http://www.benf.org/other/cfr/stringbuilder-vs-concatenation.html");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> SUGAR_STRINGBUILDER = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("stringbuilder", new VersionSpecificDefaulter(ClassFileVersion.JAVA_5, true, null), "Convert new Stringbuilder().add.add.add to string + string + string - see http://www.benf.org/other/cfr/stringbuilder-vs-concatenation.html");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> ENUM_SWITCH = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("decodeenumswitch", new VersionSpecificDefaulter(ClassFileVersion.JAVA_5, true, null), "Re-sugar switch on enum - see http://www.benf.org/other/cfr/switch-on-enum.html");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> ENUM_SUGAR = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("sugarenums", new VersionSpecificDefaulter(ClassFileVersion.JAVA_5, true, null), "Re-sugar enums - see http://www.benf.org/other/cfr/how-are-enums-implemented.html");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> STRING_SWITCH = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("decodestringswitch", new VersionSpecificDefaulter(ClassFileVersion.JAVA_7, true, null), "Re-sugar switch on String - see http://www.benf.org/other/cfr/java7switchonstring.html");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> ARRAY_ITERATOR = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("arrayiter", new VersionSpecificDefaulter(ClassFileVersion.JAVA_5, true, null), "Re-sugar array based iteration.");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> COLLECTION_ITERATOR = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("collectioniter", new VersionSpecificDefaulter(ClassFileVersion.JAVA_5, true, null), "Re-sugar collection based iteration");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> REWRITE_LAMBDAS = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("decodelambdas", new VersionSpecificDefaulter(ClassFileVersion.JAVA_8, true, null), "Re-build lambda functions");
    public static final PermittedOptionProvider.Argument<Boolean> DECOMPILE_INNER_CLASSES = new PermittedOptionProvider.Argument<Boolean>("innerclasses", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Decompile innter classes");
    public static final PermittedOptionProvider.Argument<Boolean> HIDE_UTF8 = new PermittedOptionProvider.Argument<Boolean>("hideutf", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Hide UTF8 characters - quote them instead of showing the raw characters");
    public static final PermittedOptionProvider.Argument<Boolean> HIDE_LONGSTRINGS = new PermittedOptionProvider.Argument<Boolean>("hidelongstrings", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultFalseBooleanDecoder, "Hide very long strings - useful if obfuscators have placed fake code in strings");
    public static final PermittedOptionProvider.Argument<Boolean> REMOVE_BOILERPLATE = new PermittedOptionProvider.Argument<Boolean>("removeboilerplate", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Remove boilderplate functions - constructor boilerplate, lambda deserialisation etc");
    public static final PermittedOptionProvider.Argument<Boolean> REMOVE_INNER_CLASS_SYNTHETICS = new PermittedOptionProvider.Argument<Boolean>("removeinnerclasssynthetics", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Remove (where possible) implicit outer class references in inner classes");
    public static final PermittedOptionProvider.Argument<Boolean> HIDE_BRIDGE_METHODS = new PermittedOptionProvider.Argument<Boolean>("hidebridgemethods", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Hide bridge methods");
    public static final PermittedOptionProvider.Argument<Boolean> LIFT_CONSTRUCTOR_INIT = new PermittedOptionProvider.Argument<Boolean>("liftconstructorinit", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Lift initialisation code common to all constructors into member initialisation");
    public static final PermittedOptionProvider.Argument<Boolean> REMOVE_DEAD_METHODS = new PermittedOptionProvider.Argument<Boolean>("removedeadmethods", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Remove pointless methods - default constructor etc");
    public static final PermittedOptionProvider.Argument<Boolean> REMOVE_BAD_GENERICS = new PermittedOptionProvider.Argument<Boolean>("removebadgenerics", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Hide generics where we've obviously got it wrong, and fallback to non-generic");
    public static final PermittedOptionProvider.Argument<Boolean> SUGAR_ASSERTS = new PermittedOptionProvider.Argument<Boolean>("sugarasserts", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Re-sugar assert calls");
    public static final PermittedOptionProvider.Argument<Boolean> SUGAR_BOXING = new PermittedOptionProvider.Argument<Boolean>("sugarboxing", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Where possible, remove pointless boxing wrappers");
    public static final PermittedOptionProvider.Argument<Boolean> SHOW_CFR_VERSION = new PermittedOptionProvider.Argument<Boolean>("showversion", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Show CFR version used in header (handy to turn off when regression testing)");
    public static final PermittedOptionProvider.Argument<Boolean> DECODE_FINALLY = new PermittedOptionProvider.Argument<Boolean>("decodefinally", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Re-sugar finally statements");
    public static final PermittedOptionProvider.Argument<Boolean> TIDY_MONITORS = new PermittedOptionProvider.Argument<Boolean>("tidymonitors", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Remove support code for monitors - eg catch blocks just to exit a monitor");
    public static final PermittedOptionProvider.Argument<Boolean> COMMENT_MONITORS = new PermittedOptionProvider.Argument<Boolean>("commentmonitors", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultFalseBooleanDecoder, "Replace monitors with comments - useful if we're completely confused");
    public static final PermittedOptionProvider.Argument<Boolean> LENIENT = new PermittedOptionProvider.Argument<Boolean>("lenient", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultFalseBooleanDecoder, "Be a bit more lenient in situations where we'd normally throw an exception");
    public static final PermittedOptionProvider.Argument<Boolean> DUMP_CLASS_PATH = new PermittedOptionProvider.Argument<Boolean>("dumpclasspath", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultFalseBooleanDecoder, "Dump class path for debugging purposes");
    public static final PermittedOptionProvider.Argument<Boolean> DECOMPILER_COMMENTS = new PermittedOptionProvider.Argument<Boolean>("comments", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Output comments describing decompiler status, fallback flags etc");
    public static final PermittedOptionProvider.Argument<Troolean> FORCE_TOPSORT = new PermittedOptionProvider.Argument<Troolean>("forcetopsort", (OptionDecoderParam<Troolean, Void>)OptionsImpl.defaultNeitherTrooleanDecoder, "Force basic block sorting.  Usually not necessary for code emitted directly from javac, but required in the case of obfuscation (or dex2jar!).  Will be enabled in recovery.");
    public static final PermittedOptionProvider.Argument<Troolean> FORCE_RET_PROPAGATE = new PermittedOptionProvider.Argument<Troolean>("forceretpropagate", (OptionDecoderParam<Troolean, Void>)OptionsImpl.defaultNeitherTrooleanDecoder, "Force returns to be emitted as early as possible, at site which jumps to them");
    public static final PermittedOptionProvider.Argument<Troolean> FORCE_PRUNE_EXCEPTIONS = new PermittedOptionProvider.Argument<Troolean>("forceexceptionprune", (OptionDecoderParam<Troolean, Void>)OptionsImpl.defaultNeitherTrooleanDecoder, "Try to extend and merge exceptions more aggressively");
    public static final PermittedOptionProvider.Argument<Troolean> FORCE_AGGRESSIVE_EXCEPTION_AGG = new PermittedOptionProvider.Argument<Troolean>("aexagg", (OptionDecoderParam<Troolean, Void>)OptionsImpl.defaultNeitherTrooleanDecoder, "Remove nested exception handlers if they don't change semantics");
    public static final PermittedOptionProvider.Argument<Troolean> RECOVER_TYPECLASHES = new PermittedOptionProvider.Argument<Troolean>("recovertypeclash", (OptionDecoderParam<Troolean, Void>)OptionsImpl.defaultNeitherTrooleanDecoder, "Split lifetimes where analysis caused type clash");
    public static final PermittedOptionProvider.Argument<String> OUTPUT_DIR = new PermittedOptionProvider.Argument<String>("outputdir", (OptionDecoderParam<String, Void>)OptionsImpl.defaultNullStringDecoder, "Decompile to files in [directory]");
    public static final PermittedOptionProvider.Argument<Integer> SHOWOPS = new PermittedOptionProvider.Argument<Integer>("showops", (OptionDecoderParam<Integer, Void>)OptionsImpl.default0intDecoder, "Show some (cryptic!) debug");
    public static final PermittedOptionProvider.Argument<Boolean> SILENT = new PermittedOptionProvider.Argument<Boolean>("silent", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultFalseBooleanDecoder, "Don't display state while decompiling");
    public static final PermittedOptionProvider.Argument<Boolean> RECOVER = new PermittedOptionProvider.Argument<Boolean>("recover", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Allow more and more aggressive options to be set if decompilation fails");
    public static final PermittedOptionProvider.Argument<Boolean> ECLIPSE = new PermittedOptionProvider.Argument<Boolean>("eclipse", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Enable transformations to handle eclipse code better");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> OVERRIDES = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("override", new VersionSpecificDefaulter(ClassFileVersion.JAVA_6, true, null), "Generate @Override annotations (if method is seen to implement interface method, or override a base class method)");
    public static final PermittedOptionProvider.Argument<String> HELP = new PermittedOptionProvider.Argument<String>("help", (OptionDecoderParam<String, Void>)OptionsImpl.defaultNullStringDecoder, "Show help for a given parameter");
    public static final PermittedOptionProvider.Argument<Boolean> ALLOW_CORRECTING = new PermittedOptionProvider.Argument<Boolean>("allowcorrecting", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Allow transformations which correct errors, potentially at the cost of altering emitted code behaviour.  An example would be removing impossible (in java!) exception handling - if this has any effect, a warning will be emitted.");
    public static final PermittedOptionProvider.Argument<Boolean> LABELLED_BLOCKS = new PermittedOptionProvider.Argument<Boolean>("labelledblocks", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Allow code to be emitted which uses labelled blocks, (handling odd forward gotos)");
    public static final PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion> JAVA_4_CLASS_OBJECTS = new PermittedOptionProvider.ArgumentParam<Boolean, ClassFileVersion>("j14classobj", new VersionSpecificDefaulter(ClassFileVersion.JAVA_5, false, null), "Reverse java 1.4 class object construction");
    public static final PermittedOptionProvider.Argument<Boolean> HIDE_LANG_IMPORTS = new PermittedOptionProvider.Argument<Boolean>("hidelangimports", (OptionDecoderParam<Boolean, Void>)OptionsImpl.defaultTrueBooleanDecoder, "Hide imports from java.lang.");
    public static final PermittedOptionProvider.Argument<Integer> FORCE_PASS = new PermittedOptionProvider.Argument<Integer>("recpass", (OptionDecoderParam<Integer, Void>)OptionsImpl.default0intDecoder, "Decompile specifically with recovery options from pass #X. (really only useful for debugging)", true);

    public OptionsImpl(String fileName, String methodName, Map<String, String> opts) {
        this.fileName = fileName;
        this.methodName = methodName;
        this.opts = opts;
    }

    @Override
    public String getFileName() {
        return this.fileName;
    }

    @Override
    public String getMethodName() {
        return this.methodName;
    }

    @Override
    public <T> T getOption(PermittedOptionProvider.ArgumentParam<T, Void> option) {
        return option.getFn().invoke((Object)this.opts.get(option.getName()), (Object)null);
    }

    @Override
    public <T, A> T getOption(PermittedOptionProvider.ArgumentParam<T, A> option, A arg) {
        return option.getFn().invoke((Object)this.opts.get(option.getName()), arg);
    }

    @Override
    public boolean optionIsSet(PermittedOptionProvider.ArgumentParam<?, ?> option) {
        return this.opts.get(option.getName()) != null;
    }

    public static GetOptSinkFactory<Options> getFactory() {
        return new CFRFactory(null);
    }

    static class CFRFactory
    implements GetOptSinkFactory<Options> {
        private CFRFactory() {
        }

        @Override
        public List<String> getFlags() {
            return ListFactory.newList();
        }

        @Override
        public List<? extends PermittedOptionProvider.ArgumentParam<?, ?>> getArguments() {
            return ListFactory.newList(new PermittedOptionProvider.ArgumentParam[]{OptionsImpl.SHOWOPS, OptionsImpl.ENUM_SWITCH, OptionsImpl.ENUM_SUGAR, OptionsImpl.STRING_SWITCH, OptionsImpl.ARRAY_ITERATOR, OptionsImpl.COLLECTION_ITERATOR, OptionsImpl.DECOMPILE_INNER_CLASSES, OptionsImpl.REMOVE_BOILERPLATE, OptionsImpl.REMOVE_INNER_CLASS_SYNTHETICS, OptionsImpl.REWRITE_LAMBDAS, OptionsImpl.HIDE_BRIDGE_METHODS, OptionsImpl.LIFT_CONSTRUCTOR_INIT, OptionsImpl.REMOVE_DEAD_METHODS, OptionsImpl.REMOVE_BAD_GENERICS, OptionsImpl.SUGAR_ASSERTS, OptionsImpl.SUGAR_BOXING, OptionsImpl.SHOW_CFR_VERSION, OptionsImpl.DECODE_FINALLY, OptionsImpl.TIDY_MONITORS, OptionsImpl.LENIENT, OptionsImpl.DUMP_CLASS_PATH, OptionsImpl.DECOMPILER_COMMENTS, OptionsImpl.FORCE_TOPSORT, OptionsImpl.FORCE_PRUNE_EXCEPTIONS, OptionsImpl.OUTPUT_DIR, OptionsImpl.SUGAR_STRINGBUFFER, OptionsImpl.SUGAR_STRINGBUILDER, OptionsImpl.SILENT, OptionsImpl.RECOVER, OptionsImpl.ECLIPSE, OptionsImpl.OVERRIDES, OptionsImpl.FORCE_AGGRESSIVE_EXCEPTION_AGG, OptionsImpl.FORCE_RET_PROPAGATE, OptionsImpl.HIDE_UTF8, OptionsImpl.HIDE_LONGSTRINGS, OptionsImpl.COMMENT_MONITORS, OptionsImpl.ALLOW_CORRECTING, OptionsImpl.LABELLED_BLOCKS, OptionsImpl.JAVA_4_CLASS_OBJECTS, OptionsImpl.HIDE_LANG_IMPORTS, OptionsImpl.FORCE_PASS, OptionsImpl.RECOVER_TYPECLASHES, OptionsImpl.HELP});
        }

        @Override
        public Options create(List<String> args, Map<String, String> opts) {
            String fname = null;
            String methodName = null;
            switch (args.size()) {
                case 0: {
                    break;
                }
                case 1: {
                    fname = args.get(0);
                    break;
                }
                case 2: {
                    fname = args.get(0);
                    methodName = args.get(1);
                    break;
                }
                default: {
                    throw new BadParametersException("Too many unqualified parameters", this);
                }
            }
            return new OptionsImpl(fname, methodName, opts);
        }

        /* synthetic */ CFRFactory( x0) {
            this();
        }
    }

    static class VersionSpecificDefaulter
    implements OptionDecoderParam<Boolean, ClassFileVersion> {
        public ClassFileVersion versionGreaterThanOrEqual;
        public boolean resultIfGreaterThanOrEqual;

        private VersionSpecificDefaulter(ClassFileVersion versionGreaterThanOrEqual, boolean resultIfGreaterThanOrEqual) {
            this.versionGreaterThanOrEqual = versionGreaterThanOrEqual;
            this.resultIfGreaterThanOrEqual = resultIfGreaterThanOrEqual;
        }

        @Override
        public Boolean invoke(String arg, ClassFileVersion classFileVersion) {
            if (arg != null) {
                return Boolean.parseBoolean(arg);
            }
            if (classFileVersion == null) {
                throw new IllegalStateException();
            }
            return classFileVersion.equalOrLater(this.versionGreaterThanOrEqual) ? this.resultIfGreaterThanOrEqual : !this.resultIfGreaterThanOrEqual;
        }

        @Override
        public String getRangeDescription() {
            return "boolean";
        }

        @Override
        public String getDefaultValue() {
            return "" + this.resultIfGreaterThanOrEqual + " if class file from version " + this.versionGreaterThanOrEqual + " or greater";
        }

        /* synthetic */ VersionSpecificDefaulter(ClassFileVersion x0, boolean x1,  x2) {
            this(x0, x1);
        }
    }

}

