/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.util;

public class ClassFileVersion {
    private final int major;
    private final int minor;
    private final String name;
    public static ClassFileVersion JAVA_1_2 = new ClassFileVersion(46, 0, "1.2");
    public static ClassFileVersion JAVA_1_3 = new ClassFileVersion(47, 0, "1.3");
    public static ClassFileVersion JAVA_1_4 = new ClassFileVersion(48, 0, "1.4");
    public static ClassFileVersion JAVA_5 = new ClassFileVersion(49, 0, "5");
    public static ClassFileVersion JAVA_6 = new ClassFileVersion(50, 0, "6");
    public static ClassFileVersion JAVA_7 = new ClassFileVersion(51, 0, "7");
    public static ClassFileVersion JAVA_8 = new ClassFileVersion(52, 0, "8");

    public ClassFileVersion(int major, int minor) {
        this(major, minor, null);
    }

    public ClassFileVersion(int major, int minor, String name) {
        this.major = major;
        this.minor = minor;
        this.name = name;
    }

    public boolean equalOrLater(ClassFileVersion other) {
        if (this.major < other.major) {
            return false;
        }
        if (this.major > other.major) {
            return true;
        }
        if (this.minor >= other.minor) return true;
        return false;
    }

    public boolean before(ClassFileVersion other) {
        return !this.equalOrLater(other);
    }

    public String toString() {
        return "" + this.major + "." + this.minor + (this.name == null ? "" : new StringBuilder().append(" (Java ").append(this.name).append(")").toString());
    }
}

