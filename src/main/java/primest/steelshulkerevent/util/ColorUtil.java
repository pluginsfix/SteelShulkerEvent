package primest.steelshulkerevent.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ColorUtil {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String color(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        while (matcher.find()) {
            String hex = matcher.group(1);
            message = message.replace("&#" + hex, ChatColor.of((String)("#" + hex)).toString());
        }
        return ChatColor.translateAlternateColorCodes((char)'&', (String)message);
    }
}

