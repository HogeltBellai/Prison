package ru.hogeltbellai.prison.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;

public class Pay_Command implements CommandExecutor {

    public Pay_Command() {
        Prison.getInstance().getCommand("pay").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if (args.length != 2) {
            sender.sendMessage("Использование: /pay <игрок> <сумма>");
            return true;
        }

        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("Игрок " + args[0] + " не найден.");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Неправильный формат суммы.");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("Сумма должна быть положительным числом.");
            return true;
        }

        sender.sendMessage("Вы успешно перевели " + amount + " виртуальных денег игроку " + target.getName() + ".");
        target.sendMessage("Вам было переведено " + amount + " виртуальных денег от игрока " + player.getName() + ".");
        return true;
    }
}
