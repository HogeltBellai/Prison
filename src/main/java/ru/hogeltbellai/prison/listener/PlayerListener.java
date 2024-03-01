package ru.hogeltbellai.prison.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.hogeltbellai.prison.Prison;

public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!player.hasPlayedBefore()) {
            Prison.getInstance().getDatabase().query("INSERT INTO users (name, level, blocks, money, fraction) SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS (SELECT 1 FROM users WHERE name = ?)", player.getName(), 1, 0, 0, null, player.getName());
        }
    }
}
