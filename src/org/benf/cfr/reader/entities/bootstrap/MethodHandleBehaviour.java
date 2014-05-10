/*
 * Decompiled with CFR 0_78.
 */
package org.benf.cfr.reader.entities.bootstrap;

public enum MethodHandleBehaviour {
    GET_FIELD,
    GET_STATIC,
    PUT_FIELD,
    PUT_STATIC,
    INVOKE_VIRTUAL,
    INVOKE_STATIC,
    INVOKE_SPECIAL,
    NEW_INVOKE_SPECIAL,
    INVOKE_INTERFACE;
    

    private MethodHandleBehaviour() {
    }

    public static MethodHandleBehaviour decode(byte value) {
        switch (value) {
            case 1: {
                return MethodHandleBehaviour.GET_FIELD;
            }
            case 2: {
                return MethodHandleBehaviour.GET_STATIC;
            }
            case 3: {
                return MethodHandleBehaviour.PUT_FIELD;
            }
            case 4: {
                return MethodHandleBehaviour.PUT_STATIC;
            }
            case 5: {
                return MethodHandleBehaviour.INVOKE_VIRTUAL;
            }
            case 6: {
                return MethodHandleBehaviour.INVOKE_STATIC;
            }
            case 7: {
                return MethodHandleBehaviour.INVOKE_SPECIAL;
            }
            case 8: {
                return MethodHandleBehaviour.NEW_INVOKE_SPECIAL;
            }
            case 9: {
                return MethodHandleBehaviour.INVOKE_INTERFACE;
            }
        }
        throw new IllegalArgumentException("Unknown method handle behaviour " + value);
    }
}

