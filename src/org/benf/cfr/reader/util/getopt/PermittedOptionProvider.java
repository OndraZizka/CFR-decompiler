/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util.getopt;

import java.util.List;
import org.benf.cfr.reader.util.getopt.OptionDecoderParam;

public interface PermittedOptionProvider {
    public List<String> getFlags();

    public List<? extends ArgumentParam<?, ?>> getArguments();

    public static class Argument<X>
    extends ArgumentParam<X, Void> {
        public Argument(String name, OptionDecoderParam<X, Void> fn, String help, boolean hidden) {
            super(name, fn, help, hidden);
        }

        public Argument(String name, OptionDecoderParam<X, Void> fn, String help) {
            super(name, fn, help, false);
        }
    }

    public static class ArgumentParam<X, InputType> {
        private final String name;
        private final OptionDecoderParam<X, InputType> fn;
        private final String help;
        private final boolean hidden;

        public ArgumentParam(String name, OptionDecoderParam<X, InputType> fn, String help) {
            this(name, fn, help, false);
        }

        public ArgumentParam(String name, OptionDecoderParam<X, InputType> fn, String help, boolean hidden) {
            this.name = name;
            this.fn = fn;
            this.help = help;
            this.hidden = hidden;
        }

        public String getName() {
            return this.name;
        }

        public OptionDecoderParam<X, InputType> getFn() {
            return this.fn;
        }

        public boolean isHidden() {
            return this.hidden;
        }

        public String describe() {
            String defaultVal;
            StringBuilder sb = new StringBuilder();
            sb.append("'" + this.name + "':\n\n");
            sb.append(this.help).append('\u000a');
            String range = this.fn.getRangeDescription();
            if (!(range == null || range.isEmpty())) {
                sb.append("\nRange : ").append(range).append("\n");
            }
            if ((defaultVal = this.fn.getDefaultValue()) == null || defaultVal.isEmpty()) return sb.toString();
            sb.append("\nDefault : ").append(defaultVal).append("\n");
            return sb.toString();
        }

        public String shortDescribe() {
            StringBuilder sb = new StringBuilder();
            String defaultVal = this.fn.getDefaultValue();
            String range = this.fn.getRangeDescription();
            if (!(range == null || range.isEmpty())) {
                sb.append(" (").append(range).append(") ");
            }
            if (defaultVal == null || defaultVal.isEmpty()) return sb.toString();
            sb.append(" default: ").append(defaultVal).append("");
            return sb.toString();
        }
    }

}

