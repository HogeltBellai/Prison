package ru.hogeltbellai.prison.api.menu;

import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.items.ItemsAPI;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;
import ru.hogeltbellai.prison.api.task.ItemTaskAPI;
import ru.hogeltbellai.prison.api.task.PlayerTaskAPI;
import ru.hogeltbellai.prison.api.task.TaskConfiguration;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MenuConfigAPI {

    public static void createMenuConfig(Player player, String menuName) {
        FileConfiguration config = new ConfigAPI("menus").getConfig();
        ConfigurationSection menuSection = config.getConfigurationSection("menus." + menuName);

        if (menuSection == null) return;

        String title = ChatColor.translateAlternateColorCodes('&', menuSection.getString("title", "Menu"));
        int size = menuSection.getInt("size", 9);
        MenuAPI.createMenu(player, title, size);

        ConfigurationSection itemsSection = menuSection.getConfigurationSection("items");
        if (itemsSection == null) return;

        for (String key : itemsSection.getKeys(false)) {
            ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
            if (itemSection != null) {
                int slot = itemSection.getInt("slot", 0);
                Material material = Material.matchMaterial(itemSection.getString("material", "STONE"));
                String displayName = ChatColor.translateAlternateColorCodes('&', itemSection.getString("display_name", ""));
                List<String> lore = itemSection.getStringList("lore");
                String loreString = lore.stream().map(line -> PlaceholderAPI.setPlaceholders(player, line)).collect(Collectors.joining("\n"));
                ItemsAPI item = new ItemsAPI.Builder().material(material).displayName(displayName).lore(loreString.split("\n")).hideFlags().build();
                MenuAPI.setMenuItem(player, title, slot, item.getItem(), () -> executeAction(player, itemSection));
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
                    String[] args = Arrays.copyOfRange(eventParts, 1, eventParts.length);
                    ActionType action = ActionType.getAction(actionType);
                    if (action != null) {
                        action.performAction(player, args);
                    }
                }
            }
        }
    }

    @RequiredArgsConstructor
    public enum ActionType {
        UPDATE_LEVEL((Player player, String... arg) -> {
            int level = new PlayerAPI().getLevel(player) + 1;
            TaskConfiguration currentTask = PlayerTaskAPI.TaskManager.getTask(level, "levels");

            if (currentTask != null && PlayerTaskAPI.TaskManager.isTaskCompleted(player, new PlayerAPI().getId(player), currentTask)) {
                new PlayerAPI().setLevel(player, "+", Integer.parseInt(arg[0]));
                new PlayerAPI().setMoney(player, "-", currentTask.getMoney());
            } else {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.levelup.no_task"));
            }
        }),
        REMOVE_MONEY((player, arg) -> {
            BigDecimal money = BigDecimal.valueOf(Double.parseDouble(arg[0]));
            if (new PlayerAPI().getMoney(player).compareTo(money) >= 0) {
                new PlayerAPI().setMoney(player, "-", money);
            }
        }),
        GIVE_ITEM((player, arg) -> ItemsConfigAPI.giveItem(player, arg[0])),
        BUY_ITEM((player, arg) -> {
            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(arg[1]));
            if (new PlayerAPI().getMoney(player).compareTo(amount) >= 0) {
                new PlayerAPI().setMoney(player, "-", amount);
                ItemsConfigAPI.giveItem(player, arg[0]);
            } else {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.money.no_money"));
            }
        }),
        UPGRADE_ITEM(ActionType::upgradeItem),
        TELEPORT(ActionType::teleport),
        SELECT_FRACTION(ActionType::selectFraction),
        OPEN_MENU((player, arg) -> {
            if (arg.length > 0) {
                MenuConfigAPI.createMenuConfig(player, arg[0]);
            }
        }),
        CLOSE_MENU((player, arg) -> player.closeInventory());

        public static ActionType getAction(String value) {
            try {
                return ActionType.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        private final ActionPerformer actionPerformer;

        public void performAction(Player player, String... arg) {
            actionPerformer.perform(player, arg);
        }

        @FunctionalInterface
        private interface ActionPerformer {
            void perform(Player player, String... arg);
        }

        public static void removeEnchantments(ItemStack item) {
            for (Map.Entry<Enchantment, Integer> e : item.getEnchantments().entrySet()) {
                item.removeEnchantment(e.getKey());
            }
        }

        // Доп. методы, для enum

        private static void upgradeItem(Player player, String... arg) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            String itemName = ItemsConfigAPI.getItemNameByMaterial(itemInHand);
            if (itemName == null) {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.upgrade.max_level"));
                return;
            }
            int itemLevel = ItemsConfigAPI.getLevelFromLore(itemInHand) + 1;
            TaskConfiguration itemTask = ItemTaskAPI.ItemTaskManager.getItemTask(itemName, itemLevel);
            if (itemTask == null || !PlayerTaskAPI.TaskManager.isTaskCompleted(player, new PlayerAPI().getId(player), itemTask)) {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.upgrade.no_task"));
                return;
            }
            ItemsConfigAPI.setLevelToLore(itemInHand, itemLevel);
            new PlayerAPI().setMoney(player, "-", itemTask.getMoney());
            if (itemTask.getMaterial() != null) itemInHand.setType(Material.valueOf(itemTask.getMaterial()));
            ItemMeta itemMeta = itemInHand.getItemMeta();
            if (itemMeta != null && itemTask.getDisplayName() != null) {
                itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemTask.getDisplayName()));
                itemInHand.setItemMeta(itemMeta);
            }
            if (itemTask.getEnchantments() != null) {
                itemTask.getEnchantments().forEach((enchantKey, value) -> {
                    Enchantment enchantment = Enchantment.getByName(enchantKey);
                    if (enchantment != null && value instanceof Integer) {
                        itemInHand.addEnchantment(enchantment, (int) value);
                    }
                });
            } else {
                itemInHand.getEnchantments().keySet().forEach(itemInHand::removeEnchantment);
            }
        }

        private static void teleport(Player player, String... arg) {
            if (arg.length < 6) {
                player.sendMessage("Ошибка: неверное количество аргументов для телепортации!");
                return;
            }
            try {
                double x = Double.parseDouble(arg[0]);
                double y = Double.parseDouble(arg[1]);
                double z = Double.parseDouble(arg[2]);
                float yaw = Float.parseFloat(arg[3]);
                float pitch = Float.parseFloat(arg[4]);
                String worldName = arg[5];
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    player.sendMessage("Ошибка: мир " + worldName + " не существует!");
                    return;
                }
                Location location = new Location(world, x, y, z, yaw, pitch);
                if (arg.length >= 7 && arg[6].equalsIgnoreCase("podval")) {
                    if (!new PlayerAPI().hasPodval(player)) {
                        player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.podval_teleport"));
                        return;
                    }
                    int requiredLevel = arg.length >= 8 ? Integer.parseInt(arg[7]) : 0;
                    int playerLevel = new PlayerAPI().getLevel(player);
                    if (playerLevel < requiredLevel) {
                        player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.level_teleport").replace("%level_required%", String.valueOf(requiredLevel)));
                        return;
                    }
                }
                player.teleport(location);
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.teleport"));
            } catch (NumberFormatException e) {
                player.sendMessage("Ошибка: неверный формат аргументов для телепортации!");
            }
        }

        private static void selectFraction(Player player, String... arg) {
            if (arg.length != 2) {
                player.sendMessage("Ошибка: не указано имя фракции для открытия!");
                return;
            }
            int requiredLevel = Integer.parseInt(arg[1]);
            int playerLevel = new PlayerAPI().getLevel(player);
            if (playerLevel < requiredLevel) {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.fraction.level").replace("%level_required%", String.valueOf(requiredLevel)));
                return;
            }
            if (new PlayerAPI().getFraction(player) != null && !new PlayerAPI().getFraction(player).isEmpty()) {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.fraction.has_fraction"));
                return;
            }
            new PlayerAPI().setFraction(player, arg[0]);
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.fraction.join").replace("%fraction%", new PlayerAPI().getFraction(player)));
        }
    }
}
