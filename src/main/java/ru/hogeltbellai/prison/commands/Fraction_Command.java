package ru.hogeltbellai.prison.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;

public class Fraction_Command implements CommandExecutor {

    public Fraction_Command() {
        Prison.getInstance().getCommand("fraction").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;


        if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
            if (new PlayerAPI().getFraction(player) != null && !new PlayerAPI().getFraction(player).isEmpty()) {
                if (new PlayerAPI().getMoney(player).compareTo(BigDecimal.valueOf(1000)) >= 0) {
                    MenuConfigAPI.createMenuConfig(player, "fractionleave");
                } else {
                    player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.money.no_money"));
                }
                return true;
            }
        }

        MenuConfigAPI.createMenuConfig(player, "fraction");
        return false;
    }
}
