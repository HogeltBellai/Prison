package ru.hogeltbellai.prison.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.items.ItemsConfigAPI;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;

public class Upgrade_Command implements CommandExecutor {

    public Upgrade_Command() {
        Prison.getInstance().getCommand("upgrade").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;

        if(args.length == 0) {
            if (ItemsConfigAPI.getLevelFromLore(player.getInventory().getItemInMainHand()) == 0) { return true; }
            MenuConfigAPI.createMenuConfig(player, "upgrade");
        }
        return false;
    }
}
