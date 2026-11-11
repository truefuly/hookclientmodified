/*
 * Decompiled with CFR 0.152.
 */
package hook.utils;

public final class BytecodeException {
    private BytecodeException() {
    }

    @SuppressWarnings("unchecked")
    public static <E extends Throwable> RuntimeException unchecked(Throwable cause) throws E {
        throw (E) cause; // "sneaky throw": rethrows any exception without declaring it
    }
}

