/*
 * Decompiled with CFR 0.152.
 */
package hook.utils;

import java.awt.Desktop;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BrowserWindow {
    private static Process process;

    private BrowserWindow() {
    }

    public static synchronized boolean open(String rawUrl) {
        String browser;
        String url = BrowserWindow.normalize(rawUrl);
        if (url == null) {
            return false;
        }
        BrowserWindow.close();
        browser = BrowserWindow.isWindows() ? BrowserWindow.findWindowsBrowser() : BrowserWindow.findLinuxBrowser();
        if (browser != null && BrowserWindow.launchApp(browser, url)) {
            return true;
        }
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return false;
    }

    public static synchronized void close() {
        if (process == null) {
            return;
        }
        try {
            process.descendants().forEach(ProcessHandle::destroy);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            process.destroy();
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        process = null;
    }

    public static synchronized boolean isOpen() {
        return process != null && process.isAlive();
    }

    public static String normalize(String rawUrl) {
        if (rawUrl == null) {
            return null;
        }
        Object value = rawUrl.trim();
        if (((String)value).isEmpty()) {
            return null;
        }
        if (((String)value).startsWith("//")) {
            value = "https:" + (String)value;
        } else if (((String)value).matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            if (!((String)value).matches("(?i)^https?://.*")) {
                return null;
            }
        } else {
            if (((String)value).matches("(?i)^(javascript|data|file|vbscript|about|blob|mailto|tel):.*")) {
                return null;
            }
            value = "https://" + (String)value;
        }
        try {
            URI uri = URI.create((String)value);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null || host.isBlank()) {
                return null;
            }
            if (!(scheme = scheme.toLowerCase(Locale.ROOT)).equals("http") && !scheme.equals("https")) {
                return null;
            }
            return uri.toString();
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static String findWindowsBrowser() {
        ArrayList<String> candidates = new ArrayList<String>();
        String programFiles = System.getenv("ProgramFiles");
        String programFilesX86 = System.getenv("ProgramFiles(x86)");
        String localAppData = System.getenv("LOCALAPPDATA");
        BrowserWindow.addCandidate(candidates, programFilesX86, "Microsoft\\Edge\\Application\\msedge.exe");
        BrowserWindow.addCandidate(candidates, programFilesX86, "Google\\Chrome\\Application\\chrome.exe");
        BrowserWindow.addCandidate(candidates, programFiles, "Microsoft\\Edge\\Application\\msedge.exe");
        BrowserWindow.addCandidate(candidates, programFiles, "Google\\Chrome\\Application\\chrome.exe");
        BrowserWindow.addCandidate(candidates, programFiles, "BraveSoftware\\Brave-Browser\\Application\\brave.exe");
        BrowserWindow.addCandidate(candidates, programFiles, "Vivaldi\\Application\\vivaldi.exe");
        BrowserWindow.addCandidate(candidates, localAppData, "Google\\Chrome\\Application\\chrome.exe");
        BrowserWindow.addCandidate(candidates, localAppData, "Microsoft\\Edge\\Application\\msedge.exe");
        for (String candidate : candidates) {
            try {
                if (!Files.isRegularFile(Path.of(candidate, new String[0]), new LinkOption[0])) continue;
                return candidate;
            }
            catch (Throwable throwable) {
            }
        }
        return null;
    }

    private static String findLinuxBrowser() {
        String[] candidates = new String[]{"google-chrome", "google-chrome-stable", "chromium", "chromium-browser", "microsoft-edge", "brave-browser", "vivaldi"};
        String path = System.getenv("PATH");
        if (path == null) return null;
        for (String directory : path.split(java.io.File.pathSeparator)) {
            for (String name : candidates) {
                Path candidate = Path.of(directory, name);
                if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) return candidate.toString();
            }
        }
        return null;
    }

    private static boolean launchApp(String browser, String url) {
        try {
            Path profile = Path.of(System.getProperty("java.io.tmpdir"), "hook-youtube-window");
            Files.createDirectories(profile, new FileAttribute[0]);
            ArrayList<String> command = new ArrayList<String>();
            command.add(browser);
            command.add("--app=" + url);
            command.add("--new-window");
            command.add("--window-size=960,540");
            command.add("--user-data-dir=" + String.valueOf(profile.toAbsolutePath()));
            command.add("--no-first-run");
            command.add("--no-default-browser-check");
            if (url.startsWith("https://music.youtube.com")) {
                command.add("--remote-debugging-address=127.0.0.1");
                command.add("--remote-debugging-port=9223");
                command.add("--remote-allow-origins=*");
            }
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            process = builder.start();
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static void addCandidate(List<String> candidates, String base, String relative) {
        if (base != null && !base.isBlank()) {
            candidates.add(base + "\\" + relative);
        }
    }
}

