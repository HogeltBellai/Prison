package ru.hogeltbellai.prison.listener;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.utils.InventoryManager;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!player.hasPlayedBefore()) {
            Prison.getInstance().getDatabase().query("INSERT INTO users (name, level, blocks, money, fraction) SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM users WHERE name = ?)", player.getName(), 1, 0, 0, null, player.getName());
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        InventoryManager.saveInventory(player, event);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player p = event.getPlayer();
        InventoryManager.restoreItemsOnRespawn(p);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if (message.startsWith("!")) {
            broadcastGlobalMessage(player, message.substring(1));
        } else {
            broadcastLocalMessage(player, message);
        }
        event.setCancelled(true);
    }

    private void broadcastGlobalMessage(Player sender, String message) {
        String prefix = Prison.getInstance().getLuckPerms().getGroupManager().getGroup(Prison.getInstance().getLuckPerms().getUserManager().getUser(sender.getUniqueId()).getPrimaryGroup()).getCachedData().getMetaData().getPrefix();
        String formattedMessage = Prison.getInstance().getConfig().getString("chat.globalformat").replace("%player%", sender.getName()).replace("%group%", prefix).replace("%message%", message).replace("&", "§");
        Bukkit.broadcastMessage(formattedMessage);
    }

    private void broadcastLocalMessage(Player sender, String message) {
        String prefix = Prison.getInstance().getLuckPerms().getGroupManager().getGroup(Prison.getInstance().getLuckPerms().getUserManager().getUser(sender.getUniqueId()).getPrimaryGroup()).getCachedData().getMetaData().getPrefix();
        String formattedMessage = Prison.getInstance().getConfig().getString("chat.localformat").replace("%player%", sender.getName()).replace("%group%", prefix).replace("%message%", message).replace("&", "§");
        for (Player recipient : Bukkit.getOnlinePlayers()) {
            if (recipient.getLocation().distance(sender.getLocation()) <= Prison.getInstance().getConfig().getInt("chat.radius")) {
                recipient.sendMessage(formattedMessage);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCraft(CraftItemEvent event) {
        if(event.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }
}
