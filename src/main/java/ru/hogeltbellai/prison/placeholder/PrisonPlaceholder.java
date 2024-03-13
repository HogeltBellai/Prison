package ru.hogeltbellai.prison.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.api.newtask.TaskAPI;
import ru.hogeltbellai.prison.api.newtask.TaskConfiguration;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.List;
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

        if (identifier.startsWith("task_block") || identifier.startsWith("task_money")) {
            String[] parts = identifier.split("_");
            if (parts.length == 3) {
                String configName = parts[2];
                int playerLevel = new PlayerAPI().getLevel(player) + 1;
                TaskConfiguration task = TaskAPI.TaskManager.getTask(playerLevel, configName);

                if (task != null) {
                    String result;
                    if (identifier.startsWith("task_block")) {
                        result = task.getBlocks().entrySet().stream()
                                .map(entry -> {
                                    String blockType = entry.getKey();
                                    int playerBlockCount = new PlayerAPI().getDataBlock(new PlayerAPI().getId(player), blockType);
                                    int requiredBlockCount = entry.getValue();

                                    String status = playerBlockCount >= requiredBlockCount ? "§a✔" : "§c✘";
                                    return String.format("§f%s§7: §e%d§7/§f%d %s", blockType, playerBlockCount, requiredBlockCount, status);
                                })
                                .collect(Collectors.joining("\n"));
                    } else {
                        BigDecimal playerMoneyCount = new PlayerAPI().getMoney(player);
                        BigDecimal requiredMoneyCount = task.getMoney();

                        String status = playerMoneyCount.compareTo(requiredMoneyCount) >= 0 ? "§a✔" : "§c✘";
                        result = String.format("§fДенег§7: §e%s§7/§f%s %s", playerMoneyCount, requiredMoneyCount, status);
                    }
                    return result;
                } else {
                    return identifier.startsWith("task_block") ? "§cВы достигли максимальный уровень" : "";
                }
            }
        }
        return null;
    }
}
