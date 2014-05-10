/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.MapFactory;
import org.benf.cfr.reader.util.getopt.BadParametersException;
import org.benf.cfr.reader.util.getopt.GetOptSinkFactory;
import org.benf.cfr.reader.util.getopt.OptionDecoderParam;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.PermittedOptionProvider;

public class GetOptParser {
    public static String getHelp(PermittedOptionProvider permittedOptionProvider) {
        PermittedOptionProvider.ArgumentParam param;
        StringBuilder sb = new StringBuilder();
        for (String flag : permittedOptionProvider.getFlags()) {
            sb.append("   --").append(flag).append("\n");
        }
        int max = 10;
        Iterator i$ = permittedOptionProvider.getArguments().iterator();
        while (i$.hasNext()) {
            int len;
            max = (len = (param = (PermittedOptionProvider.ArgumentParam)i$.next()).getName().length()) > max ? len : max;
        }
        max+=4;
        i$ = permittedOptionProvider.getArguments().iterator();
        while (i$.hasNext()) {
            if ((param = (PermittedOptionProvider.ArgumentParam)i$.next()).isHidden()) continue;
            String name = param.getName();
            int pad = max - name.length();
            sb.append("   --").append(param.getName());
            for (int x = 0; x < pad; ++x) {
                sb.append(' ');
            }
            sb.append(param.shortDescribe()).append("\n");
        }
        return sb.toString();
    }

    private static Map<String, OptData> buildOptTypeMap(PermittedOptionProvider optionProvider) {
        Map optTypeMap = MapFactory.newMap();
        for (String flagName : optionProvider.getFlags()) {
            optTypeMap.put((String)flagName, (OptData)new OptData(flagName, (1)null));
        }
        for (PermittedOptionProvider.ArgumentParam arg : optionProvider.getArguments()) {
            optTypeMap.put((String)arg.getName(), (OptData)new OptData(arg, (1)null));
        }
        return optTypeMap;
    }

    public <T> T parse(String[] args, GetOptSinkFactory<T> getOptSinkFactory) {
        int start;
        List unFlagged = ListFactory.newList();
        for (start = 0; start < args.length; ++start) {
            String arg;
            if ((arg = args[start]).startsWith("-")) break;
            unFlagged.add((String)arg);
        }
        Map<String, String> processed = this.process(Arrays.copyOfRange(args, start, args.length), getOptSinkFactory);
        return getOptSinkFactory.create(unFlagged, processed);
    }

    public <T> void showHelp(PermittedOptionProvider permittedOptionProvider) {
        System.err.println("CFR 0_78\n");
        System.err.println(GetOptParser.getHelp(permittedOptionProvider));
    }

    public <T> void showHelp(PermittedOptionProvider permittedOptionProvider, Options options, PermittedOptionProvider.ArgumentParam<String, Void> helpArg) {
        System.err.println("CFR 0_78\n");
        String relevantOption = options.getOption(helpArg);
        List possible = permittedOptionProvider.getArguments();
        Iterator i$ = possible.iterator();
        while (i$.hasNext()) {
            PermittedOptionProvider.ArgumentParam opt;
            if (!(opt = (PermittedOptionProvider.ArgumentParam)i$.next()).getName().equals(relevantOption)) continue;
            System.err.println(opt.describe());
            return;
        }
        System.err.println(GetOptParser.getHelp(permittedOptionProvider));
        System.err.println("No such argument '" + relevantOption + "'");
    }

    private Map<String, String> process(String[] in, PermittedOptionProvider optionProvider) {
        Map<String, OptData> optTypeMap = GetOptParser.buildOptTypeMap(optionProvider);
        Map res = MapFactory.newMap();
        for (int x = 0; x < in.length; ++x) {
            OptData optData;
            String name;
            if (!in[x].startsWith("--")) throw new BadParametersException("Unexpected argument " + in[x], optionProvider);
            if ((optData = optTypeMap.get(name = in[x].substring(2))) == null) {
                throw new BadParametersException("Unknown argument " + name, optionProvider);
            }
            if (!optData.isFlag()) {
                if (x >= in.length - 1) {
                    throw new BadParametersException("parameter " + name + " requires argument", optionProvider);
                }
            } else {
                res.put((String)name, null);
            }
            res.put((String)name, (String)in[++x]);
            optData.getArgument().getFn().invoke(res.get(name), (Object)null);
        }
        return res;
    }

    class 1 {
    }

    static class OptData {
        private final boolean isFlag;
        private final String name;
        private final PermittedOptionProvider.ArgumentParam<?, ?> argument;

        private OptData(String name) {
            this.name = name;
            this.isFlag = true;
            this.argument = null;
        }

        private OptData(PermittedOptionProvider.ArgumentParam<?, ?> argument) {
            this.argument = argument;
            this.isFlag = false;
            this.name = argument.getName();
        }

        public boolean isFlag() {
            return this.isFlag;
        }

        public String getName() {
            return this.name;
        }

        public PermittedOptionProvider.ArgumentParam<?, ?> getArgument() {
            return this.argument;
        }

        /* synthetic */ OptData(String x0, 1 x1) {
            this(x0);
        }

        /* synthetic */ OptData(PermittedOptionProvider.ArgumentParam x0, 1 x1) {
            this(x0);
        }
    }

}

