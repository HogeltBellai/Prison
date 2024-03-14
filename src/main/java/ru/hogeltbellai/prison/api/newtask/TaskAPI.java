package ru.hogeltbellai.prison.api.newtask;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class TaskAPI {

    public void loadConfig(String nameConfig) {
        ConfigAPI config = new ConfigAPI(nameConfig);
        ConfigurationSection taskSection = config.getConfig().getConfigurationSection("tasks");

        if (taskSection != null) {
            for (String key : taskSection.getKeys(false)) {
                int level = Integer.parseInt(key);
                ConfigurationSection levelSection = taskSection.getConfigurationSection(key);

                if (levelSection != null) {
                    TaskConfiguration task = parseTask(levelSection, nameConfig);
                    TaskAPI.TaskManager.addTask(level, task, nameConfig);
                }
            }
        }
    }

    private TaskConfiguration parseTask(ConfigurationSection section, String configName) {
        Map<String, Integer> blocks = new HashMap<>();
        ConfigurationSection blocksSection = section.getConfigurationSection("blocks");

        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                blocks.put(key, blocksSection.getInt(key));
            }
        }

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