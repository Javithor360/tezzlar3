package com.panita.tezzlar3.core.chat;

import java.util.HashMap;
import java.util.Map;

public class LegacyToMiniConverter {
    private static final Map<Character, String> legacyToMini = new HashMap<>();

    static {
        // Colors
        legacyToMini.put('0', "<black>");
        legacyToMini.put('1', "<dark_blue>");
        legacyToMini.put('2', "<dark_green>");
        legacyToMini.put('3', "<dark_aqua>");
        legacyToMini.put('4', "<dark_red>");
        legacyToMini.put('5', "<dark_purple>");
        legacyToMini.put('6', "<gold>");
        legacyToMini.put('7', "<gray>");
        legacyToMini.put('8', "<dark_gray>");
        legacyToMini.put('9', "<blue>");
        legacyToMini.put('a', "<green>");
        legacyToMini.put('b', "<aqua>");
        legacyToMini.put('c', "<red>");
        legacyToMini.put('d', "<light_purple>");
        legacyToMini.put('e', "<yellow>");
        legacyToMini.put('f', "<white>");

        // Formats
        legacyToMini.put('k', "<obfuscated>");
        legacyToMini.put('l', "<bold>");
        legacyToMini.put('m', "<strikethrough>");
        legacyToMini.put('n', "<underlined>");
        legacyToMini.put('o', "<italic>");
        legacyToMini.put('r', "<reset>");
    }

    public static String convert(String input) {
        StringBuilder builder = new StringBuilder();
        char[] chars = input.toCharArray();
        boolean found = false;

        for (int i = 0; i < chars.length; i++) {
            if ((chars[i] == '&' || chars[i] == '§') && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);
                String replacement = legacyToMini.get(code);
                if (replacement != null) {
                    builder.append(replacement);
                    i++; // Skip the next char (the formatting code)
                    found = true;
                    continue;
                }
            }
            builder.append(chars[i]);
        }

        return builder.toString();
    }
}

