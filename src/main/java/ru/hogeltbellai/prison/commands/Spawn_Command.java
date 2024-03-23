package ru.hogeltbellai.prison.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.hogeltbellai.prison.Prison;
import ru.hogeltbellai.prison.api.config.ConfigAPI;
import ru.hogeltbellai.prison.api.location.LocationAPI;
import ru.hogeltbellai.prison.api.menu.MenuConfigAPI;
import ru.hogeltbellai.prison.api.message.MessageAPI;

import java.util.Objects;

public class Spawn_Command implements CommandExecutor {

    public Spawn_Command() {
        Prison.getInstance().getCommand("spawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) { return true; }

        Player player = (Player) sender;

        if(args.length == 0) {
            String teleportLocation = Prison.getInstance().getConfig().getString("prison.spawn");
            player.teleport(Objects.requireNonNull(LocationAPI.deserializeLocationPlayer(teleportLocation)));
            player.sendMessage(new MessageAPI().getMessage(new ConfigAPI("config"), player, "messages.teleport"));
        }
        return false;
    }
}
