package ru.hogeltbellai.prison.api.chatcolor;

import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ChatColorAPI {

    public String getColoredString(@NotNull String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }

    public List<String> getColoredStrings(@NotNull String... lore) {
        return Arrays.stream(lore)
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList());
    }
}
