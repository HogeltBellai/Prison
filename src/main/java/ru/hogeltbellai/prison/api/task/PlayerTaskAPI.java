package ru.hogeltbellai.prison.api.task;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PlayerTaskAPI {

    public void loadConfig(String nameConfig) {
        ConfigAPI config = new ConfigAPI(nameConfig);
        ConfigurationSection taskSection = config.getConfig().getConfigurationSection("tasks");

        if (taskSection != null) {
            for (String key : taskSection.getKeys(false)) {
                ConfigurationSection taskConfigSection = taskSection.getConfigurationSection(key);
                TaskConfiguration task = parseTask(taskConfigSection, nameConfig);
                if (task != null) {
                    int level = Integer.parseInt(key);
                    PlayerTaskAPI.TaskManager.addTask(level, task, nameConfig);
                }
            }
        }
    }

    private TaskConfiguration parseTask(ConfigurationSection section, String configName) {
        Map<String, Integer> blocks = new HashMap<>();
        int totalBlocks = section.getInt("blocks");

        blocks.put("Блоков", totalBlocks);

        BigDecimal money = BigDecimal.valueOf(section.getDouble("money"));

        return new TaskConfiguration(configName, blocks, money);
    }

    public static class TaskManager {

        private static final Map<String, Map<Integer, TaskConfiguration>> tasksByConfig = new HashMap<>();

        public static void addTask(int level, TaskConfiguration task, String configName) {
            Map<Integer, TaskConfiguration> tasks = tasksByConfig.computeIfAbsent(configName, k -> new HashMap<>());
            tasks.put(level, task);
        }

        public static TaskConfiguration getTask(int level, String configName) {
            Map<Integer, TaskConfiguration> tasks = tasksByConfig.get(configName);
            if (tasks != null) {
                return tasks.get(level);
            }
            return null;
        }

        public static boolean isTaskCompleted(Player player, TaskConfiguration task) {
            if (task == null) return false;

            Map<String, Integer> requiredBlocks = task.getBlocks();
            int requiredTotalBlocks = requiredBlocks.getOrDefault("Блоков", 0);

            int actualTotalBlocks = new PlayerAPI().getBlock(player);
            if (actualTotalBlocks < requiredTotalBlocks) {
                return false;
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