package ru.hogeltbellai.prison.commands.admin;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.chatcolor.ChatColorAPI;
import ru.hogeltbellai.prison.api.mine.MineAPI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Admin_Command implements CommandExecutor {

    Location pos1;
    Location pos2;

    public Admin_Command() {
        Prison.getInstance().getCommand("prison").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;

        if(player.hasPermission("prison.admin")) {
            if (args.length == 0) {
                player.sendMessage(new ChatColorAPI().getColoredString("&7Указать позиция - &f/prison pos1/pos2"));
                player.sendMessage(new ChatColorAPI().getColoredString("&7Создать шахту - &f/prison create <название> <блок1:шанс> <блок2:шанс>"));
                player.sendMessage(new ChatColorAPI().getColoredString("&7Удалить шахту - &f/prison delete <название>"));
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("pos1")) {
                pos1 = player.getLocation();
                player.sendMessage("Позиция 1 установлена.");
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("pos2")) {
                pos2 = player.getLocation();
                player.sendMessage("Позиция 2 установлена.");
            }

            if (args.length > 3 && args[0].equalsIgnoreCase("create")) {
                if (pos1 == null || pos2 == null) {
                    player.sendMessage("Сначала установите обе позиции pos1 и pos2");
                    return true;
                }
                String name = args[1];
                if (!(new MineAPI().getAllMines().stream().anyMatch(mine -> mine.getName().equalsIgnoreCase(name)))) {
                    List<String> blockChances = new ArrayList<>(Arrays.asList(Arrays.copyOfRange(args, 2, args.length)));

                    if (new MineAPI().createMine(player, name, pos1, pos2, blockChances)) {
                        player.sendMessage("Шахта успешно создана");
                    } else {
                        player.sendMessage("Не удалось создать шахту");
                    }
                } else {
                    player.sendMessage("Шахта с таким названием уже существует");
                    return true;
                }
            }
        }
        return false;
    }
}
