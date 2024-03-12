package ru.hogeltbellai.prison.api.message;

import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.api.config.ConfigAPI;

public class MessageAPI {

    public String getMessage(ConfigAPI configAPI, Player player, String dir) {
        String message = configAPI.getConfig().getString(dir);
        message = message.replace("&", "§")
                .replace("%player%", player.getName());
        return message;
    }
}
