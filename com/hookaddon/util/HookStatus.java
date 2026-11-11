/*
 * Decompiled with CFR 0.152.
 */
package com.hookaddon.util;

import hook.compat.meteorclient.systems.modules.Module;
import java.util.LinkedHashMap;
import java.util.Map;

public final class HookStatus {
    private static final long PROGRESS_INTERVAL_MS = 2000L;
    private static final Map<String, Long> lastProgress = new LinkedHashMap<String, Long>();

    private HookStatus() {
    }

    public static void info(Module module, String message, Object ... args) {
        module.info(message, args);
    }

    public static void warn(Module module, String message, Object ... args) {
        module.warning(message, args);
    }

    public static void error(Module module, String message, Object ... args) {
        module.error(message, args);
    }

    public static void progress(Module module, String key, String message, Object ... args) {
        long now = System.currentTimeMillis();
        Long previous = lastProgress.get(key);
        if (previous != null && now - previous < 2000L) {
            return;
        }
        lastProgress.put(key, now);
        module.info(message, args);
    }

    public static void progressNow(Module module, String key, String message, Object ... args) {
        lastProgress.put(key, System.currentTimeMillis());
        module.info(message, args);
    }

    public static void reset(String key) {
        lastProgress.remove(key);
    }

    public static void resetAll() {
        lastProgress.clear();
    }
}

