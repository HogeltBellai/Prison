package ru.hogeltbellai.prison.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;

public class Fraction_Command implements CommandExecutor {

    public Fraction_Command() {
        Prison.getInstance().getCommand("fraction").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;

        if(args.length == 0) {
            MenuConfigAPI.createMenuConfig(player, "fraction");
        }
        return false;
    }
}
