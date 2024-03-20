package ru.hogeltbellai.prison.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.Prison;

public class ChatManager {

    public void broadcastGlobalMessage(Player sender, String message) {
        String prefix = Prison.getInstance().getLuckPermsAPI().getGroupManager().getGroup(Prison.getInstance().getLuckPermsAPI().getUserManager().getUser(sender.getUniqueId()).getPrimaryGroup()).getCachedData().getMetaData().getPrefix();
        String formattedMessage = Prison.getInstance().getConfig().getString("chat.globalformat").replace("%player%", sender.getName()).replace("%group%", prefix).replace("%message%", message).replace("&", "§");
        Bukkit.broadcastMessage(PlaceholderAPI.setPlaceholders(sender, formattedMessage));
    }

    public void broadcastLocalMessage(Player sender, String message) {
        String prefix = Prison.getInstance().getLuckPermsAPI().getGroupManager().getGroup(Prison.getInstance().getLuckPermsAPI().getUserManager().getUser(sender.getUniqueId()).getPrimaryGroup()).getCachedData().getMetaData().getPrefix();
        String formattedMessage = Prison.getInstance().getConfig().getString("chat.localformat").replace("%player%", sender.getName()).replace("%group%", prefix).replace("%message%", message).replace("&", "§");
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.getLocation().distance(sender.getLocation()) <= Prison.getInstance().getConfig().getInt("chat.radius")) {
                recipient.sendMessage(PlaceholderAPI.setPlaceholders(sender, formattedMessage));
            }
        }
    }
}
