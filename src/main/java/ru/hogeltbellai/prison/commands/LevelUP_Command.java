package ru.hogeltbellai.prison.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.menu.MenuAPI;

public class LevelUP_Command implements CommandExecutor {

    public LevelUP_Command() {
        Prison.getInstance().getCommand("levelup").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;

        if(args.length == 0) {
            MenuAPI.createMenu(player, "Тест", 27);
            MenuAPI.setMenuItem(player, "Тест", 0, new ItemStack(Material.BARRIER), player::closeInventory);
        }
        return false;
    }
}