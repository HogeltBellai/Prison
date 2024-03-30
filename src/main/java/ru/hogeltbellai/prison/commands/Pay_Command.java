package ru.hogeltbellai.prison.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;
import ru.hogeltbellai.prison.api.player.PlayerAPI;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class Pay_Command implements CommandExecutor {

    private static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public Pay_Command() {
        Prison.getInstance().getCommand("pay").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.help"));
            return true;
        }

        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.target_isonline").replace("%target%", target.getName()));
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.format"));
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.format"));
            return true;
        }

        double commission = amount * Prison.getInstance().getConfig().getDouble("prison.pay.commission");
        double totalAmount = amount - commission;

        if(player.getName().equals(target.getName())) {
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.no_self"));
            return true;
        }

        double minimum = Prison.getInstance().getConfig().getDouble("prison.pay.minimum");

        if (BigDecimal.valueOf(amount).compareTo(BigDecimal.valueOf(minimum)) >= 0) {
            if (new PlayerAPI().getMoney(player).compareTo(BigDecimal.valueOf(amount)) >= 0) {
                new PlayerAPI().setMoney(player, "-", BigDecimal.valueOf(amount));
                new PlayerAPI().setMoney(target, "+", BigDecimal.valueOf(totalAmount));

                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.send_money").replace("%target%", target.getName()).replace("%amount%", decimalFormat.format(totalAmount)));
                target.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.get_money").replace("%player%", player.getName()).replace("%amount%", decimalFormat.format(totalAmount)));
            } else {
                player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.money.no_money"));
            }
        } else {
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.pay.minimum").replace("%amount%", decimalFormat.format(minimum)));
        }
        return true;
    }
}
