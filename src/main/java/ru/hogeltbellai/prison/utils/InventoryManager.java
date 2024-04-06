package ru.hogeltbellai.prison.utils;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;

public class InventoryManager {

    public static HashMap<String, ItemStack[]> inventoryContents = new HashMap<String, ItemStack[]>();
    public static HashMap<String, ItemStack[]> inventoryArmorContents = new HashMap<String, ItemStack[]>();

    public static void saveInventory(Player player, PlayerDeathEvent event) {
        String playerName = player.getName();
        inventoryContents.put(playerName, player.getInventory().getContents());
        inventoryArmorContents.put(playerName, player.getInventory().getArmorContents());
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        event.getDrops().removeIf(item -> {
            if (item != null) {
                String itemName = ItemsConfigAPI.getItemNameByMaterial(item);
                Boolean shouldDrop = ItemsConfigAPI.getShouldDrop(itemName);
                return !shouldDrop;
            }
            return false;
        });
    }

    public static void restoreItemsOnRespawn(Player p) {
        if (inventoryContents.containsKey(p.getName())) {
            p.getInventory().clear();
            ItemStack[] savedContents = inventoryContents.get(p.getName());
            ItemStack[] savedArmorContents = inventoryArmorContents.get(p.getName());

            for (int i = 0; i < savedContents.length; i++) {
                ItemStack savedItem = savedContents[i];
                if (savedItem != null) {
                    String itemName = ItemsConfigAPI.getItemNameByMaterial(savedItem);
                    Boolean shouldDrop = ItemsConfigAPI.getShouldDrop(itemName);
                    if (shouldDrop == false) {
                        p.getInventory().setItem(i, savedItem);
                    }
                }
            }

            for (int i = 0; i < savedArmorContents.length; i++) {
                ItemStack savedArmorItem = savedArmorContents[i];
                if (savedArmorItem != null) {
                    String armorName = ItemsConfigAPI.getItemNameByMaterial(savedArmorItem);
                    Boolean shouldDrop = ItemsConfigAPI.getShouldDrop(armorName);
                    if (shouldDrop == null || shouldDrop) {
                        p.getInventory().setArmorContents(savedArmorContents);
                    }
                }
            }
        }
    }


    public static void dropMoney(Player killer, Player player, double percentage) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        BigDecimal balance = new PlayerAPI().getMoney(player);
        BigDecimal amountToDrop = balance.multiply(BigDecimal.valueOf(percentage / 100));

        if (balance.compareTo(BigDecimal.valueOf(10)) >= 0) {
            if (killer != null && killer instanceof Player) {
                new PlayerAPI().setMoney(killer, "+", amountToDrop);
            }
            new PlayerAPI().setMoney(player, "-", amountToDrop);
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.death.player_killed").replace("%amount%", decimalFormat.format(amountToDrop)));
        }
    }
}
