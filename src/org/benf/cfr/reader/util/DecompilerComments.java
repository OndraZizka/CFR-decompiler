/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

import java.util.Collection;
import java.util.List;
import org.benf.cfr.reader.util.DecompilerComment;
import org.benf.cfr.reader.util.ListFactory;
import org.benf.cfr.reader.util.output.Dumpable;
import org.benf.cfr.reader.util.output.Dumper;

public class DecompilerComments
implements Dumpable {
    List<DecompilerComment> commentList = ListFactory.newList();

    public void addComment(String comment) {
        DecompilerComment decompilerComment = new DecompilerComment(comment);
        this.commentList.add(decompilerComment);
    }

    public void addComment(DecompilerComment comment) {
        this.commentList.add(comment);
    }

    public void addComments(Collection<DecompilerComment> comments) {
        this.commentList.addAll(comments);
    }

    @Override
    public Dumper dump(Dumper d) {
        if (this.commentList.isEmpty()) {
            return d;
        }
        d.print("/*").newln();
        for (DecompilerComment comment : this.commentList) {
            d.print(" * ").dump(comment).newln();
        }
        d.print(" */").newln();
        return d;
    }

    public List<DecompilerComment> getCommentList() {
        return this.commentList;
    }
}

