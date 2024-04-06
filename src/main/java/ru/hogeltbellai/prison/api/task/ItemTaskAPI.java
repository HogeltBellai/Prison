package ru.hogeltbellai.prison.api.task;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class ItemTaskAPI {

    public void loadConfig(String itemsConfigName) {
        ConfigAPI itemsConfig = new ConfigAPI(itemsConfigName);
        ConfigurationSection itemTasksSection = itemsConfig.getConfig().getConfigurationSection("tasks");

        if (itemTasksSection != null) {
            for (String itemName : itemTasksSection.getKeys(false)) {
                ConfigurationSection itemTasksConfigSection = itemTasksSection.getConfigurationSection(itemName);
                loadItemTask(itemTasksConfigSection, itemName);
            }
        }
    }

    private void loadItemTask(ConfigurationSection section, String itemName) {
        Map<Integer, TaskConfiguration> itemTasks = new HashMap<>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection taskConfigSection = section.getConfigurationSection(key);
            TaskConfiguration task = parseTask(taskConfigSection, itemName);
            if (task != null) {
                int level = Integer.parseInt(key);
                itemTasks.put(level, task);
            }
        }
        ItemTaskManager.addItemTasks(itemName, itemTasks);
    }

    private TaskConfiguration parseTask(ConfigurationSection section, String itemName) {
        Map<String, Integer> blocks = new HashMap<>();
        ConfigurationSection blocksSection = section.getConfigurationSection("blocks");

        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                blocks.put(key, blocksSection.getInt(key));
            }
        }

        BigDecimal money = BigDecimal.valueOf(section.getDouble("money"));

        TaskConfiguration task = new TaskConfiguration(itemName, blocks, money);

        if (section.contains("enchant")) {
            task.setEnchantments(section.getConfigurationSection("enchant").getValues(false));
        }
        if (section.contains("material")) {
            task.setMaterial(section.getString("material"));
        }
        if (section.contains("display_name")) {
            task.setDisplayName(section.getString("display_name"));
        }

        return task;
    }

    public static class ItemTaskManager {

        private static final Map<String, Map<Integer, TaskConfiguration>> itemTasksByItem = new HashMap<>();

        public static void addItemTasks(String itemName, Map<Integer, TaskConfiguration> itemTasks) {
            itemTasksByItem.put(itemName, itemTasks);
        }

        public static TaskConfiguration getItemTask(String itemName, int level) {
            Map<Integer, TaskConfiguration> itemTasks = itemTasksByItem.get(itemName);
            if (itemTasks != null) {
                return itemTasks.get(level);
            }
            return null;
        }

        public static boolean isTaskCompleted(Player player, int playerId, TaskConfiguration task) {
            if (task == null) return false;

            Map<String, Integer> requiredBlocks = task.getBlocks();

            for (Map.Entry<String, Integer> entry : requiredBlocks.entrySet()) {
                String blockType = entry.getKey();
                int requiredAmount = entry.getValue();
                int actualAmount = new PlayerAPI().getDataBlock(playerId, blockType);
                if (actualAmount < requiredAmount) {
                    return false;
                }
            }

            BigDecimal requiredMoney = task.getMoney();
            if (requiredMoney != null) {
                BigDecimal actualMoney = new PlayerAPI().getMoney(player);
                if (actualMoney.compareTo(requiredMoney) < 0) {
                    return false;
                }
            }

            return true;
        }
    }
}