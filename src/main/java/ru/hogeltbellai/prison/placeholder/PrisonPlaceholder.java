package ru.hogeltbellai.prison.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.task.ItemTaskAPI;
import ru.hogeltbellai.prison.api.task.PlayerTaskAPI;
import ru.hogeltbellai.prison.api.task.TaskConfiguration;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
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

        if (identifier.equals("booster")) {
            double booster = new PlayerAPI().getBooster(player);
            return booster != 1 ? "x" + booster : "Нет";
        }

        if (identifier.equals("autosell")) {
            boolean booster = new PlayerAPI().hasAutosell(player);
            return booster ? "§aВключено" : "§cВыключено";
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
                                int playerBlockAll = new PlayerAPI().getBlock(player);
                                int playerBlockCount = new PlayerAPI().getDataBlock(new PlayerAPI().getId(player), blockType);
                                int requiredBlockCount = entry.getValue();

                                if(blockType.equalsIgnoreCase("Блоков")) {
                                    String status = (playerBlockAll >= requiredBlockCount) ? "§a✔" : "§c✘";
                                    return "§f" + blockType + "§7: §e" + playerBlockAll + "§7/§f" + requiredBlockCount + " " + status;
                                } else {
                                    String status = (playerBlockCount >= requiredBlockCount) ? "§a✔" : "§c✘";
                                    return "§f" + blockType + "§7: §e" + playerBlockCount + "§7/§f" + requiredBlockCount + " " + status;
                                }
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
        // %prison_top_users_money_1_name%
        if (identifier.startsWith("top")) {
            String[] parts = identifier.split("_");
            if (parts.length == 5) {
                String base = parts[1];
                String value = parts[2];
                Integer pos = Integer.parseInt(parts[3]);
                String res = null;

                if (Objects.equals(parts[4], "name")) {
                    res = Prison.getInstance().getDatabase().getVaule("SELECT name FROM " + base + " ORDER BY " + value + " DESC LIMIT 1 OFFSET " + (pos - 1), String.class);
                }
                if (Objects.equals(parts[4], "value")) {
                    res = String.valueOf(Prison.getInstance().getDatabase().getVaule("SELECT " + value + " FROM " + base + " ORDER BY " + value + " DESC LIMIT 1 OFFSET " + (pos - 1), BigDecimal.class));
                }
                if (String.valueOf(res).equals("null")) {
                    return "пусто";
                }
                return res;
            }
        }

        return null;
    }
}
