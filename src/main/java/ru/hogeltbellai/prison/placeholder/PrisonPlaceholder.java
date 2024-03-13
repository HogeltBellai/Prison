package ru.hogeltbellai.prison.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.api.block.BlockAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;
import ru.hogeltbellai.prison.api.task.Task;
import ru.hogeltbellai.prison.api.task.TaskAPI;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PrisonPlaceholder extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "prison";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HogeltBellai";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.2";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) { return ""; }

        PlayerAPI playerAPI = new PlayerAPI();

        if (identifier.equals("level")) {
            return String.valueOf(playerAPI.getLevel(player));
        }

        if (identifier.equals("money")) {
            return String.valueOf(playerAPI.getMoney(player));
        }

        if (identifier.equals("block")) {
            return String.valueOf(playerAPI.getBlock(player));
        }

        if (identifier.equals("fraction")) {
            if(playerAPI.getFraction(player) != null) {
                return String.valueOf(playerAPI.getFraction(player));
            }
            return "Нет";
        }

        if (identifier.equals("task_block")) {
            StringBuilder loreBuilder = new StringBuilder();
            int playerLevel = new PlayerAPI().getLevel(player) + 1;
            Task task = TaskAPI.TaskManager.getTask(playerLevel);

            if (task != null && task.getBlocks() != null) {
                List<String> loreLines = task.getBlocks().entrySet().stream()
                        .map(entry -> {
                            String blockType = entry.getKey();
                            int playerBlockCount = new PlayerAPI().getDataBlock(new PlayerAPI().getId(player), blockType);
                            int requiredBlockCount = entry.getValue();

                            String status;
                            if (playerBlockCount >= requiredBlockCount) {
                                status = "§a✔";
                            } else {
                                status = "§c✘";
                            }

                            return  "§f" + blockType + "§7: §e" + playerBlockCount + "§7/§f" + requiredBlockCount + " " + status;
                        })
                        .collect(Collectors.toList());

                loreBuilder.append(String.join("\n", loreLines));
            }
            return loreBuilder.toString();
        }

        if (identifier.equals("task_money")) {
            int playerLevel = new PlayerAPI().getLevel(player) + 1;
            Task task = TaskAPI.TaskManager.getTask(playerLevel);

            if (task != null && task.getMoney() != null) {
                BigDecimal playerMoneyCount = new PlayerAPI().getMoney(player);
                BigDecimal requiredMoneyCount = task.getMoney();

                String status;
                if (playerMoneyCount.compareTo(requiredMoneyCount) >= 0) {
                    status = "§a✔";
                } else {
                    status = "§c✘";
                }

                return  "§fДенег§7: §e" + playerMoneyCount + "§7/§f" + requiredMoneyCount + " " + status;
            }
        }
        return null;
    }
}
