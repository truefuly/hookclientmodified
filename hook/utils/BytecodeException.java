/*
 * Decompiled with CFR 0.152.
 */
package hook.utils;

public final class BytecodeException {
    private BytecodeException() {
    }

    public static <E extends Throwable> RuntimeException unchecked(Throwable cause) throws E {
        throw cause;
    }
}

