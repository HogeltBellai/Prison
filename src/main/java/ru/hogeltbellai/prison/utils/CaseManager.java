package ru.hogeltbellai.prison.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;

import java.util.Random;
import java.util.Set;

public class CaseManager {

    public static void openCaseMenu(Player player, String caseName) {
        if (player.getItemInHand() != null) {
            ItemStack heldItem = player.getItemInHand();
            String heldItemName = ItemsConfigAPI.getItemNameByMaterial(heldItem);
            if (heldItemName != null && heldItemName.equalsIgnoreCase("key1")) {
                ConfigurationSection caseConfig = Prison.getInstance().getConfig().getConfigurationSection("cases." + caseName + ".loot");
                if (caseConfig != null) {
                    Inventory inventory = Bukkit.createInventory(player, 27, "Кейс:");
                    randomLoot(inventory, caseConfig);
                    player.openInventory(inventory);
                    if (heldItem != null && !heldItem.getType().isAir()) {
                        heldItem.setAmount(heldItem.getAmount() - 1);
                        if (heldItem.getAmount() <= 0) {
                            player.getInventory().removeItem(heldItem);
                        } else {
                            player.getInventory().setItemInMainHand(heldItem);
                        }
                    }
                }
            } else {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.case.no_item"));
            }
        } else {
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.case.no_item"));
        }
    }

    private static void randomLoot(Inventory inventory, ConfigurationSection lootConfig) {
        Random random = new Random();
        ItemStack itemStack = null;

        for (String itemKey : lootConfig.getKeys(false)) {
            String itemName = itemKey;
            double chance = lootConfig.getDouble(itemKey);
            if (random.nextDouble() * 100 <= chance) {
                itemStack = ItemsConfigAPI.getItem(itemName);
                break;
            }
        }

        if (itemStack != null) {
            int centerSlot = 13;
            inventory.setItem(centerSlot, itemStack);
        }
    }

    public static void dropKey(Player player) {
        Random random = new Random();
        int chance = random.nextInt(3000 - 2000 + 1) + 2000;
        if (random.nextInt(chance) == 0) {
            ItemStack keyItem = ItemsConfigAPI.getItem("key1");
            if (keyItem != null) {
                player.getInventory().addItem(keyItem);
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.case.key_drop"));
            }
        }
    }

    public static String getCaseName(Location location) {
        ConfigurationSection casesSection = Prison.getInstance().getConfig().getConfigurationSection("cases");
        if (casesSection != null) {
            Set<String> caseNames = casesSection.getKeys(false);
            for (String caseName : caseNames) {
                Location caseLocation = getLocationFromConfig(caseName);
                if (caseLocation != null && caseLocation.getWorld().equals(location.getWorld()) &&
                        caseLocation.getBlockX() == location.getBlockX() &&
                        caseLocation.getBlockY() == location.getBlockY() &&
                        caseLocation.getBlockZ() == location.getBlockZ()) {
                    return caseName;
                }
            }
        }
        return null;
    }

    public boolean isCase(Location location) {
        ConfigurationSection casesSection = Prison.getInstance().getConfig().getConfigurationSection("cases");
        if (casesSection != null) {
            Set<String> caseNames = casesSection.getKeys(false);
            for (String caseName : caseNames) {
                Location caseLocation = getLocationFromConfig(caseName);
                if (caseLocation != null && caseLocation.getWorld().equals(location.getWorld()) &&
                        caseLocation.getBlockX() == location.getBlockX() &&
                        caseLocation.getBlockY() == location.getBlockY() &&
                        caseLocation.getBlockZ() == location.getBlockZ()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Location getLocationFromConfig(String caseName) {
        String world = Prison.getInstance().getConfig().getString("cases." + caseName + ".world");
        int x = Prison.getInstance().getConfig().getInt("cases." + caseName + ".x");
        int y = Prison.getInstance().getConfig().getInt("cases." + caseName + ".y");
        int z = Prison.getInstance().getConfig().getInt("cases." + caseName + ".z");
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
