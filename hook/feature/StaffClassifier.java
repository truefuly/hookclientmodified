/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import java.util.regex.Pattern;

public final class StaffClassifier {
    private static final Pattern FORMATTING = Pattern.compile("(?i)\u00a7[0-9a-fk-or]");
    private static final Pattern ROLE = Pattern.compile("(?i)\\b(helper|sr\\.?\\s*helper|srhelper|mod|moderator|sr\\.?\\s*mod|srmod|admin|administrator|sr\\.?\\s*admin|sradmin|manager|dev|developer|owner|founder|staff)\\b");
    private static final Pattern BRACKET = Pattern.compile("(?i)\\[.*?(helper|mod|admin|manager|dev|staff|owner).*?\\]");
    private static final String MARKERS = "\u2605\u2606\u2b50\u272a\u2726\u2727\u272f\u2730\u272c\u272d";

    private StaffClassifier() {
    }

    public static boolean knownName(String name) {
        return "DrDonutt".equalsIgnoreCase(name);
    }

    public static boolean marked(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        String clean = FORMATTING.matcher(text).replaceAll("").trim();
        return ROLE.matcher(clean).find() || BRACKET.matcher(clean).find() || clean.codePoints().anyMatch(codePoint -> MARKERS.indexOf(codePoint) >= 0);
    }

    public static boolean isStaff(String name, String displayName, String teamName, String prefix, String suffix) {
        return StaffClassifier.knownName(name) || StaffClassifier.marked(displayName) || StaffClassifier.marked(teamName) || StaffClassifier.marked(prefix) || StaffClassifier.marked(suffix);
    }
}

