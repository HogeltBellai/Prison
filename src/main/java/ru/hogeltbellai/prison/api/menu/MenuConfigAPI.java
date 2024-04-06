package ru.hogeltbellai.prison.api.menu;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lombok.RequiredArgsConstructor;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.chatcolor.ChatColorAPI;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.items.ItemsAPI;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;
import ru.hogeltbellai.prison.api.pet.PetAPI;
import ru.hogeltbellai.prison.api.task.ItemTaskAPI;
import ru.hogeltbellai.prison.api.task.PlayerTaskAPI;
import ru.hogeltbellai.prison.api.task.TaskConfiguration;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
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
                ItemStack itemStack;
                if (itemSection.contains("value")) {
                    String value = itemSection.getString("value");
                    itemStack = createHeadItem(value, displayName, loreString);
                } else {
                    itemStack = createItem(material, displayName, loreString);
                }
                MenuAPI.setMenuItem(player, title, slot, itemStack, () -> executeAction(player, itemSection));
            }
        }
    }

    private static ItemStack createItem(Material material, String displayName, String lore) {
        return new ItemsAPI.Builder().material(material).displayName(displayName).lore(lore.split("\n")).hideFlags().build().getItem();
    }

    private static ItemStack createHeadItem(String texture, String displayName, String lore) {
        ItemStack headItem = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) headItem.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put("textures", new Property("textures", texture));
        try {
            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        meta.setDisplayName(displayName);
        String[] loreArray = lore.split("\n");
        List<String> coloredLore = new ArrayList<>();
        for (String line : loreArray) {
            coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        meta.setLore(coloredLore);
        headItem.setItemMeta(meta);
        return headItem;
    }

    private static void executeAction(Player player, ConfigurationSection itemSection) {
        if (itemSection.isConfigurationSection("action")) {
            ConfigurationSection actionSection = itemSection.getConfigurationSection("action");
            if (actionSection == null) return;

            List<String> commands = actionSection.getStringList("command");
            List<String> messages = actionSection.getStringList("message");
            List<String> events = actionSection.getStringList("event");

            commands.forEach(command -> Bukkit.dispatchCommand(player, command.replace("%user%", player.getName())));
            messages.forEach(message -> player.sendMessage(message.replace("&", "§")));
            events.forEach(event -> {
                String[] eventParts = event.split(":");
                String actionType = eventParts[0];
                if (eventParts.length > 1) {
                    String[] args = Arrays.copyOfRange(eventParts, 1, eventParts.length);
                    ActionType.getAction(actionType).performAction(player, args);
                } else {
                    ActionType.getAction(actionType).performAction(player);
                }
            });
        }
    }

    @RequiredArgsConstructor
    public enum ActionType {
        UPDATE_LEVEL((Player player, String... arg) -> {
            int level = new PlayerAPI().getLevel(player) + 1;
            TaskConfiguration currentTask = PlayerTaskAPI.TaskManager.getTask(level, "levels");

            if(currentTask != null) {
                if (PlayerTaskAPI.TaskManager.isTaskCompleted(player, currentTask)) {
                    new PlayerAPI().setLevel(player, "+", Integer.parseInt(arg[0]));
                    new PlayerAPI().setMoney(player, "-", currentTask.getMoney());
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
                    player.sendTitle("", new ChatColorAPI().getColoredString("&aУровень персонажа повышен!"), 1, 80, 10);
                } else {
                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.levelup.no_task"));
                }
            } else {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.levelup.max_level"));
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
        BUY_PET((player, arg) -> {
            BigDecimal amount = BigDecimal.valueOf(Double.parseDouble(arg[1]));
            if (new PlayerAPI().getMoney(player).compareTo(amount) >= 0) {
                if(new PlayerAPI().getPet(player) == null) {
                    PetAPI petAPI = Prison.getInstance().getPet().loadPetData(arg[0]);
                    //Prison.getInstance().getPet().spawnPet(player, petAPI);
                    new PlayerAPI().setPet(player, arg[0]);
                    new PlayerAPI().setMoney(player, "-", amount);
                } else {
                    player.sendMessage("У вас уже есть этот питомец");
                }
            } else {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.money.no_money"));
            }
        }),
        SET_AUTOSELL((player, arg) -> {
            if (arg.length == 0) {
                PlayerAPI playerAPI = new PlayerAPI();

                if (player.hasPermission("prison.autosell")) {
                    if (playerAPI.hasAutosell(player)) {
                        playerAPI.setAutosell(player, 0);
                        player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.autosell.disable"));
                    } else {
                        playerAPI.setAutosell(player, 1);
                        player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.autosell.allow"));
                    }
                } else {
                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.autosell.no_perms"));
                }
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
        CLOSE_MENU((player, arg) -> {
            if (arg.length == 0) {
                player.closeInventory();
            }
        });

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
                return;
            }
            int itemLevel = ItemsConfigAPI.getLevelFromLore(itemInHand) + 1;
            TaskConfiguration itemTask = ItemTaskAPI.ItemTaskManager.getItemTask(itemName, itemLevel);
            if (itemTask == null) {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.upgrade.max_level"));
                return;
            }
            if(!ItemTaskAPI.ItemTaskManager.isTaskCompleted(player, new PlayerAPI().getId(player), itemTask)) {
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
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
            player.sendTitle("", new ChatColorAPI().getColoredString("&aУровень предмета повышен!"), 1, 80, 10);
        }

        private static void teleport(Player player, String... arg) {
            if (arg.length >= 6) {
                try {
                    double x = Double.parseDouble(arg[0]);
                    double y = Double.parseDouble(arg[1]);
                    double z = Double.parseDouble(arg[2]);
                    float yaw = Float.parseFloat(arg[3]);
                    float pitch = Float.parseFloat(arg[4]);
                    String worldName = arg[5];

                    World world = Bukkit.getWorld(worldName);

                    if (world != null) {
                        Location location = new Location(world, x, y, z, yaw, pitch);
                        if (arg.length >= 7 && arg[6].equalsIgnoreCase("podval")) {
                            if (arg.length >= 8) {
                                int requiredLevel = Integer.parseInt(arg[7]);
                                int playerLevel = new PlayerAPI().getLevel(player);
                                if (new PlayerAPI().hasPodval(player)) {
                                    if (playerLevel >= requiredLevel) {
                                        player.teleport(location);
                                        player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.teleport"));
                                    } else {
                                        player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.level_teleport").replace("%level_required%", String.valueOf(requiredLevel)));
                                    }
                                } else {
                                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.podval_teleport"));
                                }
                            } else {
                                if (new PlayerAPI().hasPodval(player)) {
                                    player.teleport(location);
                                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.teleport"));
                                } else {
                                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.podval_teleport"));
                                }
                            }
                        } else {
                            if (arg.length == 7) {
                                int requiredLevel = Integer.parseInt(arg[6]);
                                int playerLevel = new PlayerAPI().getLevel(player);
                                if (playerLevel >= requiredLevel) {
                                    player.teleport(location);
                                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.teleport"));
                                } else {
                                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.level_teleport").replace("%level_required%", String.valueOf(requiredLevel)));
                                }
                            } else {
                                player.teleport(location);
                                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.teleport"));
                            }
                        }
                    } else {
                        player.sendMessage("Ошибка: мир " + worldName + " не существует!");
                    }
                } catch (NumberFormatException e) {
                    player.sendMessage("Ошибка: неверный формат аргументов для телепортации!");
                }
            } else {
                player.sendMessage("Ошибка: неверное количество аргументов для телепортации!");
            }
        }

        private static void selectFraction(Player player, String... arg) {
            if (arg.length != 2) {
                new PlayerAPI().setFraction(player, null);
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