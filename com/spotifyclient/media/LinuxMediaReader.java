package com.spotifyclient.media;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Reads Spotify's current playback from the Linux user session's MPRIS D-Bus service. */
final class LinuxMediaReader {
    private static final String SPOTIFY = "org.mpris.MediaPlayer2.spotify";
    private static final String OBJECT = "/org/mpris/MediaPlayer2";
    private static final String PROPERTIES = "org.freedesktop.DBus.Properties";
    private static final Pattern DBUS_STRING = Pattern.compile("string\\s+\"((?:\\\\.|[^\"\\\\])*)\"");

    private LinuxMediaReader() {}

    static WindowsMediaReader.Snapshot readSnapshot() {
        try {
            String names = call("org.freedesktop.DBus", "/org/freedesktop/DBus",
                    "org.freedesktop.DBus.ListNames");
            if (!names.contains("string \"" + SPOTIFY + "\"")) return null;

            String properties = call(SPOTIFY, OBJECT, PROPERTIES + ".GetAll",
                    "string:org.mpris.MediaPlayer2.Player");
            String title = propertyString(properties, "xesam:title");
            if (title == null || title.isBlank()) return null;
            String artist = propertyString(properties, "xesam:artist");
            String album = propertyString(properties, "xesam:album");
            String playback = propertyString(properties, "PlaybackStatus");
            String positionReply = call(SPOTIFY, OBJECT, PROPERTIES + ".Get",
                    "string:org.mpris.MediaPlayer2.Player", "string:Position");
            long positionMs = Math.max(0, number(positionReply) / 1_000L);
            long durationMs = Math.max(0, numberForKey(properties, "mpris:length") / 1_000L);
            return new WindowsMediaReader.Snapshot(title, artist == null ? "" : artist,
                    album == null ? "" : album, positionMs, durationMs,
                    "Playing".equalsIgnoreCase(playback), System.currentTimeMillis());
        } catch (IOException | InterruptedException | RuntimeException ignored) {
            if (ignored instanceof InterruptedException) Thread.currentThread().interrupt();
            return null;
        }
    }

    private static String call(String destination, String object, String method, String... args)
            throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add("dbus-send");
        command.add("--session");
        command.add("--print-reply");
        command.add("--dest=" + destination);
        command.add(object);
        command.add(method);
        command.addAll(List.of(args));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        if (!process.waitFor(1500, TimeUnit.MILLISECONDS)) {
            process.destroyForcibly();
            return "";
        }
        return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String propertyString(String text, String key) {
        int keyAt = text.indexOf("string \"" + key + "\"");
        if (keyAt < 0) return null;
        String tail = text.substring(keyAt + key.length() + 9);
        Matcher matcher = DBUS_STRING.matcher(tail);
        if (!matcher.find()) return null;
        return unescape(matcher.group(1));
    }

    private static long numberForKey(String text, String key) {
        int keyAt = text.indexOf("string \"" + key + "\"");
        return keyAt < 0 ? 0 : number(text.substring(keyAt + key.length() + 9));
    }

    private static long number(String text) {
        Matcher matcher = Pattern.compile("(?:int64|uint64)\\s+(-?\\d+)").matcher(text);
        if (!matcher.find()) return 0;
        try { return Long.parseLong(matcher.group(1)); }
        catch (NumberFormatException ignored) { return 0; }
    }

    private static String unescape(String value) {
        StringBuilder result = new StringBuilder(value.length());
        boolean escaped = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (escaped) {
                result.append(switch (c) {
                    case 'n' -> '\n'; case 'r' -> '\r'; case 't' -> '\t';
                    default -> c;
                });
                escaped = false;
            } else if (c == '\\') escaped = true;
            else result.append(c);
        }
        if (escaped) result.append('\\');
        return result.toString();
    }
}
