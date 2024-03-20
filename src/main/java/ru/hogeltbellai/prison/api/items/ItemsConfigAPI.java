package ru.hogeltbellai.prison.api.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;

import java.util.List;
import java.util.Objects;

public class ItemsConfigAPI {

    static ConfigAPI config = new ConfigAPI("items");

    public static ItemStack getItem(String itemName) {
        if (!config.getConfig().contains(itemName)) {
            return null;
        }

        String materialString = config.getConfig().getString(itemName + ".material");
        Material material = Material.matchMaterial(materialString);
        if (material == null) {
            return null;
        }

        String displayName = config.getConfig().getString(itemName + ".display_name").replace("&", "§");
        List<String> lore = config.getConfig().getStringList(itemName + ".lore");
        if (!lore.contains("§fУровень: 1")) {
            lore.add(" ");
            lore.add("§fУровень: 1");
        }
        return new ItemsAPI.Builder().material(material).displayName(displayName).lore(lore).hideFlags().build().getItem();
    }

    public static void giveItem(Player player, String itemName) {
        ItemStack item = getItem(itemName);
        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.items.give").replace("%item%", item.getItemMeta().getDisplayName()));
        }
    }

    public static void giveItem(Player player, String itemName, int amount) {
        ItemStack item = getItem(itemName);
        if (item != null) {
            item.setAmount(amount);
            player.getInventory().addItem(item);
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.items.give").replace("%item%", item.getItemMeta().getDisplayName()).replace("%amount%", String.valueOf(amount)));
        }
    }

    public static String getItemNameByMaterial(ItemStack itemStack) {
        for (String itemName : config.getConfig().getKeys(false)) {
            if (Objects.requireNonNull(config.getConfig().getString(itemName + ".material")).equalsIgnoreCase(itemStack.getType().toString()) && Objects.requireNonNull(config.getConfig().getString(itemName + ".display_name")).replace('&', '§').equalsIgnoreCase(Objects.requireNonNull(itemStack.getItemMeta()).getDisplayName())) {
                return itemName;
            }
        }
        return null;
    }

    public static int getLevelFromLore(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) {
            return 0;
        }

        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (String line : lore) {
                    if (line.startsWith("§fУровень: ")) {
                        try {
                            return Integer.parseInt(line.substring(11).trim());
                        } catch (NumberFormatException e) {
                            return 1;
                        }
                    }
                }
            }
        }
        return 1;
    }

    public static void setLevelToLore(ItemStack itemStack, int level) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null) {
                for (int i = 0; i < lore.size(); i++) {
                    String line = lore.get(i);
                    if (line.startsWith("§fУровень: ")) {
                        lore.set(i, "§fУровень: " + level);
                        meta.setLore(lore);
                        itemStack.setItemMeta(meta);
                        return;
                    }
                }
            }
        }
    }

    public static Boolean getShouldDrop(String itemName) {
        if (config.getConfig().contains(itemName + ".drop")) {
            return config.getConfig().getBoolean(itemName + ".drop");
        } else {
            return true;
        }
    }
}
