package ru.hogeltbellai.prison.api.menu;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.items.ItemsAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.List;

public class MenuConfigAPI {

    public static void createMenuConfig(Player player, String menuName) {
        FileConfiguration fileConfiguration = new ConfigAPI("menus").getConfig();
        ConfigurationSection menuSection = fileConfiguration.getConfigurationSection("menus." + menuName);

        if (menuSection != null) {
            String title = ChatColor.translateAlternateColorCodes('&', menuSection.getString("title", "Menu"));
            int size = menuSection.getInt("size", 9);

            MenuAPI.createMenu(player, title, size);

            ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                    if (itemSection != null) {
                        int slot = itemSection.getInt("slot", 0);
                        Material material = Material.matchMaterial(itemSection.getString("material", "STONE"));
                        String displayName = ChatColor.translateAlternateColorCodes('&', itemSection.getString("display_name", ""));
                        List<String> lore = itemSection.getStringList("lore");

                        ItemsAPI item = new ItemsAPI.Builder().material(material).displayName(displayName).lore(lore).hideFlags().build();

                        MenuAPI.setMenuItem(player, title, slot, item.getItem(), () -> {
                            executeAction(player, itemSection);
                            player.closeInventory();
                        });
                    }
                }
            }
        }
    }

    private static void executeAction(Player player, ConfigurationSection itemSection) {
        if (itemSection.isConfigurationSection("action")) {
            ConfigurationSection actionSection = itemSection.getConfigurationSection("action");
            String command = actionSection.getString("command", "");
            String event = actionSection.getString("event", "");
            if (!command.isEmpty()) {
                command = command.replace("%user%", player.getName());
                Bukkit.dispatchCommand(player, command);
            }
            String[] eventParts = event.split(":");
            if (eventParts.length == 3) {
                String actionType = eventParts[0];
                String math = eventParts[1];
                String amount = eventParts[2];
                ActionType action = ActionType.getAction(actionType);
                if (action != null) {

                    action.performAction(player, math, amount);
                }
            }
        }
    }

    public enum ActionType {
        UPDATE_LEVEL {
            @Override
            public void performAction(Player player, String math, String amount) {
                new PlayerAPI().setLevel(player, math, Integer.parseInt(amount));
            }
        },
        SET_MONEY {
            @Override
            public void performAction(Player player, String math, String amount) {
                new PlayerAPI().setMoney(player, math, BigDecimal.valueOf(Double.parseDouble(amount)));
            }
        };

        public static ActionType getAction(String value) {
            try {
                return ActionType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public abstract void performAction(Player player, String math, String amount);
    }
}
