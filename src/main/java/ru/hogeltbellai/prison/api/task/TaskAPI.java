package ru.hogeltbellai.prison.api.task;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskAPI {

    ConfigAPI config = new ConfigAPI("levels");

    public void loadConfig() {
        ConfigurationSection taskSection = config.getConfig().getConfigurationSection("tasks");

        if (taskSection != null) {
            for (String key : taskSection.getKeys(false)) {
                int level = Integer.parseInt(key);
                ConfigurationSection levelSection = taskSection.getConfigurationSection(key);

                if (levelSection != null) {
                    Task task = parseTask(levelSection);
                    TaskManager.addTask(level, task);
                }
            }
        }
    }

    private Task parseTask(ConfigurationSection section) {
        Map<String, Integer> blocks = new HashMap<>();
        ConfigurationSection blocksSection = section.getConfigurationSection("blocks");

        if (blocksSection != null) {
            for (String key : blocksSection.getKeys(false)) {
                blocks.put(key, blocksSection.getInt(key));
            }
        }

        double money = section.getDouble("money");

        return new Task(blocks, BigDecimal.valueOf(money));
    }

    public static class TaskManager {

        private static final Map<Integer, Task> tasks = new HashMap<>();

        public static void addTask(int level, Task task) {
            tasks.put(level, task);
        }

        public static Task getTask(int level) {
            return tasks.get(level);
        }

        public static boolean isTaskCompleted(Player player, int playerId, Task task) {
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
