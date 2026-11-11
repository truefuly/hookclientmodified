/*
 * Decompiled with CFR 0.152.
 */
package hook.feature;

import java.text.Normalizer;
import java.util.Locale;

public final class VanishChatParser {
    private VanishChatParser() {
    }

    public static String normalize(String text) {
        int codePoint;
        if (text == null || text.isBlank()) {
            return "";
        }
        String decomposed = Normalizer.normalize(VanishChatParser.stripLegacyCodes(text), Normalizer.Form.NFKD).toLowerCase(Locale.ROOT);
        StringBuilder normalized = new StringBuilder(decomposed.length());
        boolean atSeparator = true;
        for (int index = 0; index < decomposed.length(); index += Character.charCount(codePoint)) {
            codePoint = decomposed.codePointAt(index);
            if (VanishChatParser.isMark(codePoint) || VanishChatParser.isFormatCode(codePoint)) continue;
            int folded = VanishChatParser.foldConfusable(codePoint);
            if ((folded < 97 || folded > 122) && (folded < 48 || folded > 57)) continue;
            normalized.appendCodePoint(folded);
            atSeparator = false;
        }
        int length = normalized.length();
        if (length > 0 && normalized.charAt(length - 1) == ' ') {
            normalized.setLength(length - 1);
        }
        return normalized.toString();
    }

    public static boolean containsPlayerName(String message, String playerName) {
        String rawMessage = VanishChatParser.stripLegacyCodes(message == null ? "" : message).toLowerCase(Locale.ROOT);
        String rawName = VanishChatParser.stripLegacyCodes(playerName == null ? "" : playerName).trim().toLowerCase(Locale.ROOT);
        if (rawName.matches("[a-z0-9_]{1,16}")) {
            int start = 0;
            while (start <= rawMessage.length() - rawName.length()) {
                boolean rightBoundary;
                int match = rawMessage.indexOf(rawName, start);
                if (match < 0) {
                    return false;
                }
                int end = match + rawName.length();
                boolean leftBoundary = match == 0 || !VanishChatParser.isUsernameCharacter(rawMessage.charAt(match - 1));
                boolean bl = rightBoundary = end == rawMessage.length() || !VanishChatParser.isUsernameCharacter(rawMessage.charAt(end));
                if (leftBoundary && rightBoundary) {
                    return true;
                }
                start = match + 1;
            }
            return false;
        }
        String normalizedMessage = VanishChatParser.normalize(message);
        String normalizedName = VanishChatParser.normalize(playerName);
        return !normalizedMessage.isBlank() && !normalizedName.isBlank() && (normalizedMessage.equals(normalizedName) || (" " + normalizedMessage + " ").contains(" " + normalizedName + " "));
    }

    public static boolean looksLikeLeaveMessage(String message, String playerName) {
        if (!VanishChatParser.containsPlayerName(message, playerName)) {
            return false;
        }
        String normalizedMessage = " " + VanishChatParser.normalize(message) + " ";
        String normalizedName = VanishChatParser.normalize(playerName);
        if (normalizedName.isBlank()) {
            return false;
        }
        String prefix = " " + normalizedName + " ";
        return normalizedMessage.contains(prefix + "left the game ") || normalizedMessage.contains(prefix + "left ") || normalizedMessage.contains(prefix + "has left ") || normalizedMessage.contains(prefix + "quit ") || normalizedMessage.contains(prefix + "has quit ") || normalizedMessage.contains(prefix + "disconnected ") || normalizedMessage.contains(prefix + "logged out ");
    }

    public static boolean isPlausiblePlayerName(String name) {
        if (name == null) {
            return false;
        }
        String trimmed = name.trim();
        return trimmed.length() >= 3 && trimmed.length() <= 16 && trimmed.indexOf(32) < 0;
    }

    public static boolean isUsername(String name) {
        if (name == null || name.length() < 2 || name.length() > 32) {
            return false;
        }
        boolean hasLetterOrDigit = false;
        for (int index = 0; index < name.length(); ++index) {
            char character = name.charAt(index);
            if (character >= 'A' && character <= 'Z' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9') {
                hasLetterOrDigit = true;
                continue;
            }
            if (character == '_' || character == '.' || character == ' ') continue;
            return false;
        }
        return hasLetterOrDigit;
    }

    private static boolean isUsernameCharacter(char character) {
        return character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '_';
    }

    private static boolean isMark(int codePoint) {
        int type = Character.getType(codePoint);
        return type == 6 || type == 8 || type == 7;
    }

    private static boolean isFormatCode(int codePoint) {
        return codePoint == 167 || codePoint == 8203 || codePoint == 8204 || codePoint == 8205 || codePoint == 65279;
    }

    private static String stripLegacyCodes(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        StringBuilder clean = new StringBuilder(text.length());
        for (int index = 0; index < text.length(); ++index) {
            char code;
            char character = text.charAt(index);
            if (character == '\u00a7' && index + 1 < text.length() && "0123456789abcdefklmnorx".indexOf(code = Character.toLowerCase(text.charAt(index + 1))) >= 0) {
                ++index;
                continue;
            }
            clean.append(character);
        }
        return clean.toString();
    }

    private static int foldConfusable(int codePoint) {
        return switch (codePoint) {
            case 593, 945, 1072, 7424 -> 97;
            case 609, 610 -> 103;
            case 618, 953, 1110 -> 105;
            case 628, 1400 -> 110;
            case 640 -> 114;
            case 655, 1091 -> 121;
            case 665, 946, 1074 -> 98;
            case 668, 1085 -> 104;
            case 671 -> 108;
            case 949, 1077, 7431 -> 101;
            case 954, 1082, 7435 -> 107;
            case 957, 7456 -> 118;
            case 959, 1086, 7439 -> 111;
            case 961, 1088, 7448 -> 112;
            case 964, 1090, 7451 -> 116;
            case 965, 7452 -> 117;
            case 1010, 1089, 7428 -> 99;
            case 1084, 7437 -> 109;
            case 1093 -> 120;
            case 1109, 42801 -> 115;
            case 1112, 7434 -> 106;
            case 1121, 7457 -> 119;
            case 1281, 7429 -> 100;
            case 1307 -> 113;
            case 7458, 7459 -> 122;
            case 42800 -> 102;
            default -> codePoint;
        };
    }
}

