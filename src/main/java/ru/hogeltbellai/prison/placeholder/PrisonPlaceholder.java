package ru.hogeltbellai.prison.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

public class PrisonPlaceholder extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "prison";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HogeltBellai";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.1";
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) { return ""; }

        PlayerAPI playerAPI = new PlayerAPI();

        if (identifier.equals("money")) {
            return String.valueOf(playerAPI.getMoney(player));
        }

        if (identifier.equals("block")) {
            return String.valueOf(playerAPI.getBlock(player));
        }
        return null;
    }
}
