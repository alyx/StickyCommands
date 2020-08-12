package com.dumbdogdiner.stickycommands.utils;

import java.lang.Character;
import java.util.Map;

public class TranslationUtil {

    // Java apparently has no capabiliy to do something even a simple language like
    // C can do
    // which is parse hexidecimal numbers and tell me if they're fucking valid. This
    // function
    // will do exactly what I need by checking if the char is A through F, 0 through
    // 9.
    /**
     * Checks if the character is a valid minecraft color code
     * 
     * @param ch The character to check for a valid color code char
     * @return True if the character is valid minecraft colorcode
     * @deprecated Since Minecraft 1.16 supports 32 bit colors, this function will
     *             be deprecated.
     */
    public static boolean isxdigit(char ch) {
        // First check if the char is a digit, Java can manage to do this one amazingly.
        if (!Character.isDigit(ch)) {
            // If it's not a number between 0 through 9, check if it's A through F
            // If we are lower case, switch to upper and compare that way.
            if (ch >= 97)
                ch -= 32;

            // Minecraft uses some new special chars for formatting so we have
            // to account for those too.
            if (ch == 'R' || (ch < 80 && ch > 74))
                return true;

            // if they're greater than 70 (aka 'F') but less than 65 (aka 'A')
            // then it's not valid hexidecimal.
            if (ch > 71 || ch < 65)
                return false;
        }

        return true;
    }

    public static final char SPECIAL_CHAR = '\u00A7';

    /**
     * Replace the character sequence in `chars` to swap out with the minecraft
     * color char while also validating that the color code sequence is valid.
     * 
     * @param chars   Character sequence to replace with the section character
     *                minecraft uses for color codes
     * @param message Message containing sequences of `chars` in it
     * @return A color formatted message for Minecraft clients.
     */
    public static String TranslateColors(String chars, String message) {
        if (message == null)
            return null;

        if (chars == null)
            return message;

        // Don't allocate if we don't have to.
        if (!message.contains(chars))
            return message;

        StringBuilder retstr = new StringBuilder(message);
        for (int pos = message.indexOf(chars); pos != -1; pos = message.indexOf(chars, pos)) {
            if (pos + 1 > message.length())
                break;

            // Make sure the next char is valid hex as Minecraft uses a hexidecimal number
            if (TranslationUtil.isxdigit(message.charAt(pos + 1))) {
                // Now we replace the starting char with our special char.
                retstr.setCharAt(pos, SPECIAL_CHAR);
                pos += 2;
            } else // Skip 2 characters, invalid sequence.
                pos += 2;
        }

        return retstr.toString();
    }

    // Used to replace variables inside of strings.
    // {Player} was last seen on {Time}
    // NotZachery was last seen on 04-18-2020 
    /**
     * Replace all placeholders in a string, executing placeholder functions in the
     * process to format strings with variables provided.
     * 
     * @param message   The message to have placeholders replaced
     * @param Variables The variables to be utilized in this message for the
     *                  placeholders and their functions
     * @return Formatted string with all placeholders from Variables replaced.
     */
    public static String TranslateVariables(String message, Map<String, String> Variables) {
        // If it doesn't have the starting char for variables, skip it.
        if (!message.contains("{") || Variables == null)
            return message;

        String retstr = message;
        // Try and iterate over all our variables.
        for (int pos = retstr.indexOf("{"), pos2 = retstr.indexOf("}", pos); pos != -1
                && pos2 != -1; pos = retstr.indexOf("{", pos + 1), pos2 = retstr.indexOf("}", pos + 1)) {
            // If we're longer than we should be.
            if (pos + 1 > retstr.length() || pos2 + 1 > retstr.length())
                break;

            // Substring.
            String variable = retstr.substring(pos + 1, pos2);
            String replacement = null;
            if (Variables.containsKey(variable)) {
                // Now we replace it with our value from the map.
                replacement = Variables.get(variable);
            }

            if (replacement != null)
                retstr = retstr.substring(0, pos) + replacement + retstr.substring(pos2 + 1);
        }
        return retstr;
    }

    /**
     * Translate the preformatted string to a fully formatted string ready for
     * players to see, switching out color codes and placeholders.
     * 
     * @param message    The message containing placeholders and untranslated color
     *                   code sequences
     * @param ColorChars The character used as the prefix for color strings
     *                   (bukkit/spigot use `&amp;` and so do we most of the time)
     * @param Variables  A list of variables to be parsed by the placeholder
     * @return A string with color sequences and placeholders translated to their
     *         formatted message ready for the player.
     * @see TranslateVariables
     */
    public static String Translate(String message, String ColorChars, Map<String, String> Variables) {
        String retstr = TranslationUtil.TranslateColors(ColorChars, message);
        retstr = TranslationUtil.TranslateVariables(retstr, Variables);
        return retstr;
    }

    public static boolean isInteger(String s) {
        return isInteger(s,10);
    }
    
    public static boolean isInteger(String s, int radix) {
        if(s.isEmpty()) return false;
        for(int i = 0; i < s.length(); i++) {
            if(i == 0 && s.charAt(i) == '-') {
                if(s.length() == 1) return false;
                else continue;
            }
            if(Character.digit(s.charAt(i),radix) < 0) return false;
        }
        return true;
    }
}