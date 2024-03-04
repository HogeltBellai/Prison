package ru.hogeltbellai.prison.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.menu.MenuAPI;
import ru.hogeltbellai.prison.api.items.ItemsAPI;

public class LevelUP_Command implements CommandExecutor {

    public LevelUP_Command() {
        Prison.getInstance().getCommand("levelup").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;

        ItemsAPI item1 = new ItemsAPI.Builder().material(Material.HONEY_BOTTLE).displayName("&eПовысить уровень!").lore(" ", "&fОписание").hideFlags().build();

        if(args.length == 0) {
            MenuAPI.createMenu(player, "Тест", 27);

            MenuAPI.setMenuItem(player, "Тест", 13, item1.getItem(), player::closeInventory);
        }
        return false;
    }
}
