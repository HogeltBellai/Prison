package ru.hogeltbellai.prison.api.chatcolor;

import net.md_5.bungee.api.ChatColor;

import java.util.Arrays;

public class ChatColorAPI {

    public String getColoredString(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public String getColoredString(String... str) {
        return ChatColor.translateAlternateColorCodes('&', Arrays.toString(str));
    }
}
