package ru.hogeltbellai.prison.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.task.ItemTaskAPI;
import ru.hogeltbellai.prison.api.task.PlayerTaskAPI;
import ru.hogeltbellai.prison.api.task.TaskConfiguration;
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
            String fraction = new PlayerAPI().getFraction(player);
            return fraction != null ? fraction : "Нет";
        }

        if (identifier.startsWith("block")) {
            String[] parts = identifier.split("_");
            StringBuilder loreBuilder = new StringBuilder();

            if (parts.length == 2) {
                String arg = parts[1];
                int level = 0;
                TaskConfiguration task = null;

                if(arg.equalsIgnoreCase("upgrade")) {
                    level = ItemsConfigAPI.getLevelFromLore(player.getInventory().getItemInMainHand());
                    task = ItemTaskAPI.ItemTaskManager.getItemTask(ItemsConfigAPI.getItemNameByMaterial(player.getInventory().getItemInMainHand()), level + 1);
                } else if(arg.equalsIgnoreCase("level")) {
                    int playerLevel = new PlayerAPI().getLevel(player) + 1;
                    task = PlayerTaskAPI.TaskManager.getTask(playerLevel, "levels");
                }

                if (task != null && task.getBlocks() != null) {
                    List<String> loreLines = task.getBlocks().entrySet().stream()
                            .map(entry -> {
                                String blockType = entry.getKey();
                                int playerBlockCount = new PlayerAPI().getDataBlock(new PlayerAPI().getId(player), blockType);
                                int requiredBlockCount = entry.getValue();

                                String status = (playerBlockCount >= requiredBlockCount) ? "§a✔" : "§c✘";

                                return "§f" + blockType + "§7: §e" + playerBlockCount + "§7/§f" + requiredBlockCount + " " + status;
                            })
                            .collect(Collectors.toList());

                    loreBuilder.append(String.join("\n", loreLines));
                } else loreBuilder.append("§cВы достигли максимальный уровень");
            }

            return loreBuilder.toString();
        }

        if (identifier.startsWith("money")) {
            String[] parts = identifier.split("_");

            if (parts.length == 2) {
                String arg = parts[1];
                int level = 0;
                TaskConfiguration task = null;

                if (arg.equalsIgnoreCase("upgrade")) {
                    level = ItemsConfigAPI.getLevelFromLore(player.getInventory().getItemInMainHand());
                    task = ItemTaskAPI.ItemTaskManager.getItemTask(ItemsConfigAPI.getItemNameByMaterial(player.getInventory().getItemInMainHand()), level + 1);
                } else if (arg.equalsIgnoreCase("level")) {
                    int playerLevel = new PlayerAPI().getLevel(player) + 1;
                    task = PlayerTaskAPI.TaskManager.getTask(playerLevel, "levels");
                }

                if (task != null && task.getMoney() != null) {
                    BigDecimal playerMoneyCount = new PlayerAPI().getMoney(player);
                    BigDecimal requiredMoneyCount = task.getMoney();

                    String status = (playerMoneyCount.compareTo(requiredMoneyCount) >= 0) ? "§a✔" : "§c✘";

                    return "§fДенег§7: §e" + playerMoneyCount + "§7/§f" + requiredMoneyCount + " " + status;
                } else return "";
            }
        }
        return null;
    }
}
