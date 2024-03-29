package ru.hogeltbellai.prison.listener;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.lootnetwork.LootNetwork;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.location.LocationAPI;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;
import ru.hogeltbellai.prison.utils.CaseManager;
import ru.hogeltbellai.prison.utils.ChatManager;
import ru.hogeltbellai.prison.utils.InventoryManager;
import ru.hogeltbellai.prison.utils.PodvalManager;

import java.util.Objects;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!player.hasPlayedBefore()) {
            Prison.getInstance().getDatabase().query("INSERT INTO users (name, level, blocks, money, fraction, booster, has_podval) SELECT ?, ?, ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM users WHERE name = ?)", player.getName(), 1, 0, 0, null, 1, 0, player.getName());

            String teleportLocation = Prison.getInstance().getConfig().getString("prison.spawn");
            player.teleport(Objects.requireNonNull(LocationAPI.deserializeLocationPlayer(teleportLocation)));
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

        String teleportLocation = Prison.getInstance().getConfig().getString("prison.spawn");
        event.setRespawnLocation(Objects.requireNonNull(LocationAPI.deserializeLocationPlayer(teleportLocation)));
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();

        if(!LootNetwork.getInstance().getPunishmentAPI().isMuted(player.getName())) {
            if (message.startsWith("!")) {
                new ChatManager().broadcastGlobalMessage(player, message.substring(1));
            } else {
                new ChatManager().broadcastLocalMessage(player, message);
            }
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCraft(CraftItemEvent event) {
        if(event.getWhoClicked().getGameMode() != GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getItem() != null && event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            new PodvalManager().getPodval(player);
        }

        if (event.getClickedBlock() != null) {
            Block clickedBlock = event.getClickedBlock();
            if (Prison.getInstance().getCaseManager().isCase(clickedBlock.getLocation())) {
                event.setCancelled(true);
                CaseManager.openCaseMenu(player, "test");
            }
        }
    }
}
