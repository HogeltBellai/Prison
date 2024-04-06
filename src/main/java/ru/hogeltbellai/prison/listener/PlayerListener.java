package ru.hogeltbellai.prison.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import ru.hogeltbellai.lootnetwork.LootNetwork;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.entity.CustomPet;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.location.LocationAPI;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;
import ru.hogeltbellai.prison.api.pet.Pet;
import ru.hogeltbellai.prison.api.pet.PetAPI;
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
            Prison.getInstance().getDatabase().query("INSERT INTO users (name, level, blocks, money, fraction, booster, autosell, pet, has_podval) SELECT ?, ?, ?, ?, ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM users WHERE name = ?)", player.getName(), 1, 0, 0, null, 1, 0, null, 0, player.getName());

            String teleportLocation = Prison.getInstance().getConfig().getString("prison.spawn");
            player.teleport(Objects.requireNonNull(LocationAPI.deserializeLocationPlayer(teleportLocation)));
        }

        if(new PlayerAPI().getPet(player) != null) {
            PetAPI petAPI = Prison.getInstance().getPet().loadPetData(new PlayerAPI().getPet(player));
            Prison.getInstance().getPet().spawnPet(player, petAPI);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        CustomPet.removePetForPlayer(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        InventoryManager.saveInventory(player, event);

        InventoryManager.dropMoney(killer, player, 0.5);
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
                CaseManager.openCaseMenu(player, CaseManager.getCaseName(clickedBlock.getLocation()));
            }
        }

        if (event.getItem() != null && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) && event.getPlayer().isSneaking()) {
            if (ItemsConfigAPI.getLevelFromLore(player.getInventory().getItemInMainHand()) == 0) { return; }
            MenuConfigAPI.createMenuConfig(player, "upgrade");
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        CustomPet pet = CustomPet.playerPets.get(player);
        if (pet != null) {
            double distance = pet.getPreviousLocation().distance(to);

            if (distance >= 20) {
                pet.removePet();

                CustomPet finalPet = pet;

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PetAPI petAPI = Prison.getInstance().getPet().loadPetData(new PlayerAPI().getPet(player));
                        Prison.getInstance().getPet().spawnPet(player, petAPI);

                        finalPet.setPreviousLocation(to);

                        PacketContainer teleportPacket = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
                        finalPet.move(teleportPacket, to);
                        ProtocolLibrary.getProtocolManager().broadcastServerPacket(teleportPacket);
                    }
                }.runTaskLater(Prison.getInstance(), 10);
            }
        }
    }
}
