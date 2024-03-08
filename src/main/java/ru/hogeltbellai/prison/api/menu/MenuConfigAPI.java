package ru.hogeltbellai.prison.api.menu;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.items.ItemsAPI;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
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
            assert actionSection != null;
            List<String> commands = actionSection.getStringList("command");
            List<String> messages = actionSection.getStringList("message");
            List<String> events = actionSection.getStringList("event");
            if (!commands.isEmpty() || !events.isEmpty() || !messages.isEmpty()) {
                for (String command : commands) {
                    command = command.replace("%user%", player.getName());
                    Bukkit.dispatchCommand(player, command);
                }
                for (String message : messages) {
                    message = message.replace("&", "§");
                    player.sendMessage(message);
                }
                for (String event : events) {
                    String[] eventParts = event.split(":");
                    String actionType = eventParts[0];
                    String arg = eventParts[1];
                    ActionType action = ActionType.getAction(actionType);
                    if (action != null) {
                        if(eventParts.length == 2) {
                            action.performAction(player, arg);
                        } else if(eventParts.length == 3) {
                            action.performAction(player, arg, eventParts[2]);
                        }
                    }
                }
            }
        }
    }

    public enum ActionType {
        UPDATE_LEVEL {
            @Override
            public void performAction(Player player, String... arg) {
                new PlayerAPI().setLevel(player, "+", Integer.parseInt(arg[0]));
            }
        },
        REMOVE_MONEY {
            @Override
            public void performAction(Player player, String... arg) {
                if(new PlayerAPI().getMoney(player).compareTo(BigDecimal.valueOf(Double.parseDouble(arg[0]))) >= 0) {
                    new PlayerAPI().setMoney(player, "-", BigDecimal.valueOf(Double.parseDouble(arg[0])));
                }
            }
        },
        GIVE_ITEM {
            @Override
            public void performAction(Player player, String... arg) {
                ItemsConfigAPI.giveItem(player, arg[0]);
            }
        },
        BUY_ITEM {
            @Override
            public void performAction(Player player, String... arg) {
                if(new PlayerAPI().getMoney(player).compareTo(BigDecimal.valueOf(Double.parseDouble(arg[1]))) >= 0) {
                    new PlayerAPI().setMoney(player, "-", BigDecimal.valueOf(Double.parseDouble(arg[1])));
                    ItemsConfigAPI.giveItem(player, arg[0]);
                }
            }
        };

        public static ActionType getAction(String value) {
            try {
                return ActionType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public abstract void performAction(Player player, String... arg);
    }
}
