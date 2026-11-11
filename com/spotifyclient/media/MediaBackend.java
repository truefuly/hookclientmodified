package com.spotifyclient.media;

/** Runtime-selectable local playback source. Windows media is the default. */
public final class MediaBackend {
    private static volatile String mode = "Windows";

    private MediaBackend() {}

    public static boolean isLinux() {
        return "Linux".equals(mode);
    }

    public static void setLinux(boolean enabled) {
        mode = enabled ? "Linux" : "Windows";
    }

    public static boolean isBrowser() {
        return "Browser".equals(mode) || "YouTube Music Browser".equals(mode);
    }

    public static boolean isWindows() {
        return "Windows".equals(mode);
    }

    public static void setMode(String selected) {
        mode = selected == null ? "Windows" : selected;
    }
}
