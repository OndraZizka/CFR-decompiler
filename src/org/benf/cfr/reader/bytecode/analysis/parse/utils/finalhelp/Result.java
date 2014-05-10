/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.bytecode.analysis.parse.utils.finalhelp;

import java.util.Set;
import org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement;

public class Result {
    public static Result FAIL = new Result();
    private final boolean res;
    private final Set<Op03SimpleStatement> toRemove;
    private final Op03SimpleStatement start;
    private final Op03SimpleStatement afterEnd;

    private Result() {
        this.res = false;
        this.toRemove = null;
        this.start = null;
        this.afterEnd = null;
    }

    public Result(Set<Op03SimpleStatement> toRemove, Op03SimpleStatement start, Op03SimpleStatement afterEnd) {
        this.res = true;
        this.toRemove = toRemove;
        this.start = start;
        this.afterEnd = afterEnd;
    }

    public boolean isFail() {
        return !this.res;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Result result = (Result)o;
        if (this.res != result.res) {
            return false;
        }
        if (!(this.start != null ? !this.start.equals(result.start) : result.start != null)) return true;
        return false;
    }

    public int hashCode() {
        int result = this.res ? 1 : 0;
        result = 31 * result + (this.start != null ? this.start.hashCode() : 0);
        return result;
    }

    public Set<Op03SimpleStatement> getToRemove() {
        return this.toRemove;
    }

    public Op03SimpleStatement getStart() {
        return this.start;
    }

    public Op03SimpleStatement getAfterEnd() {
        return this.afterEnd;
    }
}

